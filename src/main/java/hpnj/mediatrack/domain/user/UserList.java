package hpnj.mediatrack.domain.user;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_list")
public class UserList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean isPublic = false;

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<UserListItem> items = new ArrayList<>();

    protected UserList() {}

    public UserList(UserAccount user, String name, String description, boolean isPublic) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
    }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public List<UserListItem> getItems() { return items; }
}
