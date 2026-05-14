package com.mediatrack.domain.media;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "album")
@DiscriminatorValue("ALBUM")
@Getter
@Setter
public class Album extends Media {

    private Integer trackCount;

    /** Total album duration in seconds */
    private Integer duration;
}
