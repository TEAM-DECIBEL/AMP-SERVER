package com.amp.domain.userFestival.repository;

import com.amp.domain.festival.entity.UserFestival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface UserFestivalRepository extends JpaRepository<UserFestival, Long> {

    @Query("SELECT uf.festival.id FROM UserFestival uf WHERE uf.user.id = :userId AND uf.wishList = true")
    Set<Long> findAllFestivalIdsByUserId(@Param("userId") Long userId);
}
