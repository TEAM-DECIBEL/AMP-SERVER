package com.amp.domain.user.repository;

import com.amp.domain.user.entity.Audience;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT a.nickname FROM Audience a WHERE a.email = :email")
    Optional<String> findNicknameByEmail(@Param("email") String email);

}
