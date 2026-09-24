package com.mealgo.restaurant_service.domain.model;

import com.mealgo.restaurant_service.domain.enums.OptionGroupStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "option_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class OptionGroup extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    MenuItem menuItem;

    String name;

    @Column(nullable = false)
    Integer minSelect;

    @Column(nullable = false)
    Integer maxSelect;

    Integer displayOrder;

    @Enumerated(EnumType.STRING)
    OptionGroupStatus status;

    @OneToMany(mappedBy = "optionGroup")
    List<Option> options;
}
