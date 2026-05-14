package com.mediatrack.domain.media;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "book")
@DiscriminatorValue("BOOK")
@Getter
@Setter
public class Book extends Media {

    private String isbn;

    private Integer pageCount;

    private String language;
}
