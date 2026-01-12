package com.amp.domain.category.entity;

import com.amp.domain.festival.entity.Festival;
import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "festival_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FestivalCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "festival_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public FestivalCategory(Festival festival, Category category, Integer displayOrder) {
        this.festival = festival;
        this.category = category;
        this.displayOrder = displayOrder;
        this.isActive = true;
    }
}
