package com.amp.domain.userFestival.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.UserFestival;
import com.amp.domain.user.entity.User;
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
            ORDER BY uf.festival.startDate ASC
            """)
    List<Festival> findUpcomingWishlistFestivals(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );

    @Query("SELECT uf.festival.id FROM UserFestival uf WHERE uf.user.id = :userId AND uf.wishList = true")
    Set<Long> findAllFestivalIdsByUserId(@Param("userId") Long userId);

    Optional<UserFestival> findByUserAndFestival(User user, Festival festival);
}
