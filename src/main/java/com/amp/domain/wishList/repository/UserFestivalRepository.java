package com.amp.domain.wishList.repository;

import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.entity.UserFestival;
import com.amp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserFestivalRepository extends JpaRepository<UserFestival, Long> {

    @Query("""
            SELECT uf.festival 
            FROM UserFestival uf 
            WHERE uf.user.id = :userId 
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

    @Query("SELECT uf.festival.id FROM UserFestival uf WHERE uf.user.id = :userId AND uf.wishList = true")
    Set<Long> findAllFestivalIdsByUserId(@Param("userId") Long userId);

    Optional<UserFestival> findByUserAndFestival(User user, Festival festival);

    @Query("SELECT uf FROM UserFestival uf " +
            "JOIN FETCH uf.festival f " +
            "WHERE uf.user.id = :userId " +
            "AND uf.wishList = true " +
            "AND f.deletedAt IS NULL " +
            "ORDER BY f.startDate ASC, f.startTime ASC, f.title ASC")
    Page<UserFestival> findAllByUserIdAndWishListTrue(Long userId, Pageable pageable);
}
