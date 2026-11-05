package com.mealgo.restaurantservice.infrastructure.persistence.adapter;

import com.mealgo.restaurantservice.infrastructure.persistence.entity.ProvinceJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.entity.WardJpaEntity;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.ProvinceJpaRepository;
import com.mealgo.restaurantservice.infrastructure.persistence.repository.WardJpaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GeographyQueryAdapterTest {
    private final ProvinceJpaRepository provinces = mock(ProvinceJpaRepository.class);
    private final WardJpaRepository wards = mock(WardJpaRepository.class);
    private final GeographyQueryAdapter adapter = new GeographyQueryAdapter(provinces, wards);

    @Test
    void mapsProvinceNamesWithoutExposingEntities() {
        ProvinceJpaEntity province = new ProvinceJpaEntity();
        province.setCode(1);
        province.setName("Thành phố Hà Nội");
        when(provinces.findAllByOrderByNameAscCodeAsc()).thenReturn(List.of(province));

        var result = adapter.findProvinces();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().code()).isEqualTo(1);
        assertThat(result.getFirst().name()).isEqualTo("Thành phố Hà Nội");
    }

    @Test
    void filtersWardsByProvinceAndMapsDisplayNames() {
        WardJpaEntity ward = new WardJpaEntity();
        ward.setCode(123);
        ward.setName("Xã Hòa Lạc");
        when(wards.findByProvince_CodeOrderByNameAscCodeAsc(1)).thenReturn(List.of(ward));

        var result = adapter.findWards(1);

        assertThat(result.getFirst().code()).isEqualTo(123);
        assertThat(result.getFirst().name()).isEqualTo("Xã Hòa Lạc");
        verify(wards).findByProvince_CodeOrderByNameAscCodeAsc(1);
    }

    @Test
    void checksProvinceExistence() {
        when(provinces.existsById(1)).thenReturn(true);
        assertThat(adapter.provinceExists(1)).isTrue();
    }
}
