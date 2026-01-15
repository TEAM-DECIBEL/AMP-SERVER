package com.amp.domain.userFestival.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.UserFestival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserFestivalRepository extends JpaRepository<UserFestival, Long> {

    @Query("""
        SELECT uf.festival 
        FROM UserFestival uf 
        WHERE uf.user.id = :userId 
        AND uf.wishList = true 
        AND uf.festival.endDate >= :today
        AND uf.festival.deletedAt IS NULL
        ORDER BY uf.festival.startDate ASC
        """)
    List<Festival> findUpcomingWishlistFestivals(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );

}
