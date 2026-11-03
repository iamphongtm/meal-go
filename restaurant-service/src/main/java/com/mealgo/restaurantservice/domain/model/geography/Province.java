package com.mealgo.restaurantservice.domain.model.geography;

import com.mealgo.restaurantservice.domain.shared.Entity;

public final class Province extends Entity<Integer> {
    private final String name;
    private final String divisionType;
    private final String codename;
    private final Integer phoneCode;

    public Province(Integer code, String name, String divisionType, String codename, Integer phoneCode) {
        super(code);
        this.name = name;
        this.divisionType = divisionType;
        this.codename = codename;
        this.phoneCode = phoneCode;
    }

    public Integer getCode() { return getId(); }
    public String getName() { return name; }
    public String getDivisionType() { return divisionType; }
    public String getCodename() { return codename; }
    public Integer getPhoneCode() { return phoneCode; }
}
