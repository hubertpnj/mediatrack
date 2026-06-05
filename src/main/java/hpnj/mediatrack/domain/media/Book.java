package hpnj.mediatrack.domain.media;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("BOOK")
public class Book extends Media {

    private String isbn;
    private Integer pageCount;

    protected Book() {}

    public Book(String title, LocalDate releaseDate, String isbn, Integer pageCount) {
        super(title, releaseDate);
        this.isbn = isbn;
        this.pageCount = pageCount;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
}
