package recipes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import users.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "recipes")
public class Recipe {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotBlank
    @Column(name = "name")
    private String name;

    @NotBlank
    @Column(name = "category")
    private String category;

    @UpdateTimestamp
    @Column(name = "date")
    private LocalDateTime date;

    @NotBlank
    @Column(name = "description")
    private String description;

    @NotEmpty
    @ElementCollection
    @CollectionTable(name = "INGREDIENTS", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "ingredients")
    private List<String> ingredients;

    @NotEmpty
    @ElementCollection
    @CollectionTable(name = "DIRECTIONS", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "directions")
    private List<String> directions;

}
