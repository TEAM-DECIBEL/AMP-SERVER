package com.amp.domain.organizer.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrganizerRepository extends JpaRepository<Organizer, Long> {

    Boolean existsByUser(User user);

    Optional<Organizer> findByUserId(Long userId);

    Optional<Organizer> findByUser(User user);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM Organizer o JOIN o.festivals f " +
            "WHERE f = :festival AND o.user = :user")
    Boolean existsByFestivalAndUser(@Param("festival") Festival festival,
                                    @Param("user") User user);

    boolean existsByOrganizerName(String organizerName);

}
