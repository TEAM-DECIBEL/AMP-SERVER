package com.amp.domain.user.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("AUDIENCE")
@SuperBuilder
@NoArgsConstructor
public class Audience extends User {
}
