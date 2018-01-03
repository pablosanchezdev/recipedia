package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
public class Tag extends BaseModel {

    private String name;

    @ManyToMany(mappedBy = "tags")
    @JsonBackReference
    private List<Recipe> recipes = new ArrayList<>();

    private static final Finder<Long, Tag> find =
            new Finder<>(Tag.class);

    public Tag() {
        super();
    }

    public static Tag findByName(String name) {
        return find
                .query()
                .where()
                    .ieq("name", name)
                .findOne();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }
}
