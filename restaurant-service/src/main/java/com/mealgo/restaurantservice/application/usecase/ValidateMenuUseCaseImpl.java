package com.mealgo.restaurantservice.application.usecase;

import com.mealgo.restaurantservice.application.port.in.menu.MenuItemDetailResult;
import com.mealgo.restaurantservice.application.port.in.menu.RestaurantMenuResult;
import com.mealgo.restaurantservice.application.port.in.menu.ValidateMenuQuery;
import com.mealgo.restaurantservice.application.port.in.menu.ValidateMenuResult;
import com.mealgo.restaurantservice.application.port.in.menu.ValidateMenuUseCase;
import com.mealgo.restaurantservice.application.port.out.RestaurantMenuQueryPort;
import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.domain.shared.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidateMenuUseCaseImpl implements ValidateMenuUseCase {

    private static final String CURRENCY = "VND";

    private final RestaurantMenuQueryPort menuQueryPort;

    @Override
    @Transactional(readOnly = true)
    public ValidateMenuResult validate(ValidateMenuQuery query) {
        RestaurantMenuResult menu = menuQueryPort.findMenu(query.restaurantId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESTAURANT_NOT_FOUND,
                        query.restaurantId()
                ));

        if (!menu.restaurant().acceptingOrders()) {
            return invalidResult(
                    query.restaurantId(),
                    List.of(issue(
                            null,
                            null,
                            "RESTAURANT_NOT_ACCEPTING_ORDERS",
                            "Restaurant is not currently accepting orders."
                    ))
            );
        }

        List<ValidateMenuResult.Issue> issues = new ArrayList<>();
        List<ValidateMenuResult.Item> validatedItems = new ArrayList<>();

        Set<UUID> processedLineIds = new HashSet<>();

        Map<UUID, Optional<MenuItemDetailResult>> itemCache =
                new HashMap<>();

        for (ValidateMenuQuery.Item requestedItem : query.items()) {
            int issueCountBeforeLine = issues.size();

            if (!processedLineIds.add(requestedItem.lineId())) {
                issues.add(issue(
                        requestedItem.lineId(),
                        requestedItem.menuItemId(),
                        "DUPLICATE_LINE_ID",
                        "lineId must be unique within the request."
                ));
                continue;
            }

            Optional<MenuItemDetailResult> detailOptional =
                    itemCache.computeIfAbsent(
                            requestedItem.menuItemId(),
                            menuItemId -> menuQueryPort.findMenuItem(
                                    query.restaurantId(),
                                    menuItemId
                            )
                    );

            if (detailOptional.isEmpty()) {
                issues.add(issue(
                        requestedItem.lineId(),
                        requestedItem.menuItemId(),
                        "MENU_ITEM_NOT_FOUND",
                        "Menu item does not exist or does not belong to this restaurant."
                ));
                continue;
            }

            MenuItemDetailResult detail = detailOptional.get();

            if (!detail.available()) {
                issues.add(issue(
                        requestedItem.lineId(),
                        requestedItem.menuItemId(),
                        "MENU_ITEM_UNAVAILABLE",
                        "Menu item is inactive, sold out, or cannot currently be ordered."
                ));
                continue;
            }

            Set<UUID> selectedOptionIds = new LinkedHashSet<>();

            for (UUID optionId : requestedItem.selectedOptionIds()) {
                if (!selectedOptionIds.add(optionId)) {
                    issues.add(issue(
                            requestedItem.lineId(),
                            requestedItem.menuItemId(),
                            "DUPLICATE_OPTION_ID",
                            "The same option cannot be selected more than once."
                    ));
                }
            }

            Map<UUID, OptionContext> optionById = new HashMap<>();

            for (MenuItemDetailResult.OptionGroup group
                    : detail.optionGroups()) {

                for (MenuItemDetailResult.Option option
                        : group.options()) {

                    optionById.put(
                            option.optionId(),
                            new OptionContext(
                                    group.optionGroupId(),
                                    option
                            )
                    );
                }
            }

            Map<UUID, Integer> selectionCountByGroup =
                    new HashMap<>();

            List<ValidateMenuResult.Option> selectedOptions =
                    new ArrayList<>();

            for (UUID optionId : selectedOptionIds) {
                OptionContext context = optionById.get(optionId);

                if (context == null) {
                    issues.add(issue(
                            requestedItem.lineId(),
                            requestedItem.menuItemId(),
                            "OPTION_NOT_CONFIGURED",
                            "Option is not configured for this menu item: "
                                    + optionId
                    ));
                    continue;
                }

                selectionCountByGroup.merge(
                        context.optionGroupId(),
                        1,
                        Integer::sum
                );

                if (!context.option().available()) {
                    issues.add(issue(
                            requestedItem.lineId(),
                            requestedItem.menuItemId(),
                            "OPTION_UNAVAILABLE",
                            "Option is inactive or sold out: "
                                    + optionId
                    ));
                    continue;
                }

                selectedOptions.add(
                        new ValidateMenuResult.Option(
                                context.optionGroupId(),
                                context.option().optionId(),
                                context.option().name(),
                                context.option().additionalPrice()
                        )
                );
            }

            for (MenuItemDetailResult.OptionGroup group
                    : detail.optionGroups()) {

                int selectionCount =
                        selectionCountByGroup.getOrDefault(
                                group.optionGroupId(),
                                0
                        );

                if (selectionCount < group.minSelections()
                        || selectionCount > group.maxSelections()) {

                    issues.add(issue(
                            requestedItem.lineId(),
                            requestedItem.menuItemId(),
                            "OPTION_SELECTION_OUT_OF_RANGE",
                            "Option group "
                                    + group.optionGroupId()
                                    + " requires between "
                                    + group.minSelections()
                                    + " and "
                                    + group.maxSelections()
                                    + " selections."
                    ));
                }
            }

            if (issues.size() == issueCountBeforeLine) {
                BigDecimal optionPrice = selectedOptions.stream()
                        .map(ValidateMenuResult.Option::additionalPrice)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

                BigDecimal unitPrice =
                        detail.basePrice().add(optionPrice);

                validatedItems.add(
                        new ValidateMenuResult.Item(
                                requestedItem.lineId(),
                                detail.menuItemId(),
                                detail.name(),
                                requestedItem.quantity(),
                                detail.basePrice(),
                                List.copyOf(selectedOptions),
                                unitPrice
                        )
                );
            }
        }

        if (!issues.isEmpty()) {
            // Atomic response: không trả giá một phần.
            return invalidResult(
                    query.restaurantId(),
                    issues
            );
        }

        return new ValidateMenuResult(
                true,
                query.restaurantId(),
                CURRENCY,
                List.copyOf(validatedItems),
                List.of()
        );
    }

    private ValidateMenuResult invalidResult(
            UUID restaurantId,
            List<ValidateMenuResult.Issue> issues
    ) {
        return new ValidateMenuResult(
                false,
                restaurantId,
                CURRENCY,
                List.of(),
                List.copyOf(issues)
        );
    }

    private ValidateMenuResult.Issue issue(
            UUID lineId,
            UUID menuItemId,
            String code,
            String message
    ) {
        return new ValidateMenuResult.Issue(
                lineId,
                menuItemId,
                code,
                message
        );
    }

    private record OptionContext(
            UUID optionGroupId,
            MenuItemDetailResult.Option option
    ) {
    }
}
