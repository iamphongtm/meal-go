package com.mealgo.restaurantservice.domain.model.geography;

import com.mealgo.restaurantservice.domain.shared.BusinessException;
import com.mealgo.restaurantservice.domain.shared.Entity;
import lombok.Getter;

import java.util.Objects;

import static com.mealgo.restaurantservice.domain.shared.ErrorCode.WARD_PROVINCE_MISMATCH;

@Getter
public final class Ward extends Entity<Integer> {
    private final String name;
    private final String divisionType;
    private final String codename;
    private final Integer provinceCode;

    public Ward(Integer code, String name, String divisionType, String codename, Integer provinceCode) {
        super(code);
        this.name = name;
        this.divisionType = divisionType;
        this.codename = codename;
        this.provinceCode = provinceCode;
    }

    public Integer getCode() { return getId(); }

    public void validateBelongsTo(Integer provinceCode) {
        if (!Objects.equals(this.provinceCode, provinceCode)) {
            throw new BusinessException(
                    WARD_PROVINCE_MISMATCH,
                    getCode(),
                    provinceCode
            );
        }
    }
}
