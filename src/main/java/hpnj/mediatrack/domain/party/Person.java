package com.mediatrack.domain.party;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "person")
@DiscriminatorValue("PERSON")
@Getter
@Setter
public class Person extends Party {

    private LocalDate birthDate;

    private LocalDate deathDate;

    private String birthCountry;
}
