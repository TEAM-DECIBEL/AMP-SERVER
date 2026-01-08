package com.amp.domain.Notice.entity;

import com.amp.domain.festival.entity.Festival;
import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder
    public void Category(Festival festival, String categoryName, String categoryCode,
                         String description, Integer displayOrder) {
        this.festival = festival;
        this.categoryName = categoryName;
        this.categoryCode = categoryCode;
        this.description = description;
        this.displayOrder = displayOrder;
        this.isActive = true;
    }
}
