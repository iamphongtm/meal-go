package com.mealgo.restaurantservice.infrastructure.persistence.entity;

import com.mealgo.restaurantservice.domain.enums.OptionGroupStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "option_groups")
@Getter
@Setter
@NoArgsConstructor
public class OptionGroupJpaEntity extends BaseJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantJpaEntity restaurant;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OptionGroupStatus status;

    @OneToMany(mappedBy = "optionGroup", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @BatchSize(size = 50)
    @OrderBy("displayOrder ASC, id ASC")
    private List<OptionJpaEntity> options = new ArrayList<>();

    public List<OptionJpaEntity> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
