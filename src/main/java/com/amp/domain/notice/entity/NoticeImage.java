package com.amp.domain.notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "notice_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeImage {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int imageOrder;

    private NoticeImage(Notice notice, String imageUrl, int imageOrder) {
        this.notice = notice;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
    }

    public static NoticeImage of(Notice notice, String imageUrl, int imageOrder) {
        return new NoticeImage(notice, imageUrl, imageOrder);
    }

    public void updateOrder(int imageOrder) {
        this.imageOrder = imageOrder;
    }
}
