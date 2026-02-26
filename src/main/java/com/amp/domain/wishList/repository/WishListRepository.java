package com.amp.domain.wishList.repository;

import com.amp.domain.festival.entity.AudienceFestival;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.user.entity.Audience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WishListRepository extends JpaRepository<AudienceFestival, Long> {

    @Query("""
            SELECT uf.festival
            FROM AudienceFestival uf
            WHERE uf.audience.id = :userId
            AND uf.wishList = true
            AND uf.festival.endDate >= :today
            AND uf.festival.deletedAt IS NULL
            ORDER BY
                uf.festival.startDate ASC,
                uf.festival.startTime ASC,
                uf.festival.title ASC
            """)
    List<Festival> findUpcomingWishlistFestivals(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );

    @Query("SELECT uf.festival.id " +
            "FROM AudienceFestival uf " +
            "WHERE uf.audience.id = :userId " +
            "AND uf.wishList = true")
    Set<Long> findAllFestivalIdsByUserId(@Param("userId") Long userId);

    Optional<AudienceFestival> findByAudienceAndFestival(Audience audience, Festival festival);

    @Query("SELECT uf FROM AudienceFestival uf " +
            "JOIN FETCH uf.festival f " +
            "WHERE uf.audience.id = :userId " +
            "AND uf.wishList = true " +
            "AND f.deletedAt IS NULL " +
            "AND f.endDate >= CURRENT_DATE " +
            "ORDER BY f.startDate ASC, f.startTime ASC, f.title ASC")
    Page<AudienceFestival> findAllByUserIdAndWishListTrue(Long userId, Pageable pageable);

    @Query("SELECT uf FROM AudienceFestival uf JOIN FETCH uf.festival f " +
            "WHERE uf.audience.id = :userId AND uf.wishList = true " +
            "AND f.deletedAt IS NULL " +
            "ORDER BY f.endDate DESC , f.startTime DESC , f.title ASC")
    Page<AudienceFestival> findAllWishListHistory(@Param("userId") Long userId, Pageable pageable);
}
