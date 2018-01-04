package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ingredients")
public class Ingredient extends BaseModel {

    private String name;

    @ManyToMany(mappedBy = "ingredients")
    @JsonBackReference
    private List<Recipe> recipes = new ArrayList<>();

    private static final Finder<Long, Ingredient> find =
            new Finder<>(Ingredient.class);

    public Ingredient() {
        super();
    }

    public static Ingredient findByName(String name) {
        return find
                .query()
                .where()
                    .ieq("name", name)
                .findOne();
    }

    @JsonIgnore
    @Override
    public Long getId() {
        return super.getId();
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
