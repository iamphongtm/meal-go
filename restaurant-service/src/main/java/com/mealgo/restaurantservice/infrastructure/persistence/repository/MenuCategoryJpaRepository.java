package com.mealgo.restaurantservice.infrastructure.persistence.repository;

import com.mealgo.restaurantservice.infrastructure.persistence.entity.MenuCategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MenuCategoryJpaRepository extends JpaRepository<MenuCategoryJpaEntity, UUID> {
    @Query("""
            select distinct c from MenuCategoryJpaEntity c
            left join fetch c.items
            where c.restaurant.id = :restaurantId
              and c.status = com.mealgo.restaurantservice.domain.enums.CategoryStatus.ACTIVE
            order by c.displayOrder, c.id
            """)
    java.util.List<MenuCategoryJpaEntity> findCustomerMenu(@Param("restaurantId") UUID restaurantId);

    boolean existsByRestaurantIdAndName(UUID restaurantId, String name);

    boolean existsByIdAndRestaurantId(UUID id, UUID restaurantId);

    @Query("""
        select coalesce(max(c.displayOrder), 0)
        from MenuCategoryJpaEntity c
        where c.restaurant.id = :restaurantId
""")
    int getMaxDisplayOrderByRestaurantId(@Param("restaurantId") UUID restaurantId);
}
