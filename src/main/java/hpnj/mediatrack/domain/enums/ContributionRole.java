package com.mediatrack.domain.enums;

/**
 * Flat enum covering contribution roles across all media types.
 *
 * Movie/Series: DIRECTOR, WRITER, ACTOR, PRODUCER, COMPOSER, CINEMATOGRAPHER, EDITOR
 * Game: DEVELOPER, PUBLISHER, DESIGNER, ARTIST
 * Book: AUTHOR, ILLUSTRATOR, TRANSLATOR, NARRATOR
 * Album: VOCALIST, GUITARIST, BASSIST, DRUMMER, KEYBOARDIST, SONGWRITER, MIXING_ENGINEER, LABEL
 */
public enum ContributionRole {
    // Film / Series
    DIRECTOR,
    WRITER,
    ACTOR,
    PRODUCER,
    COMPOSER,
    CINEMATOGRAPHER,
    EDITOR,

    // Game
    DEVELOPER,
    PUBLISHER,
    DESIGNER,
    ARTIST,

    // Book
    AUTHOR,
    ILLUSTRATOR,
    TRANSLATOR,
    NARRATOR,

    // Album / Music
    VOCALIST,
    GUITARIST,
    BASSIST,
    DRUMMER,
    KEYBOARDIST,
    SONGWRITER,
    MIXING_ENGINEER,
    LABEL,

    // Universal
    OTHER
}
