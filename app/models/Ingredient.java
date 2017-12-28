package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import play.data.validation.Constraints.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ingredients")
public class Ingredient extends BaseModel {

    @Required
    private String name;

    @ManyToMany(mappedBy = "ingredients")
    @JsonBackReference
    private List<Recipe> recipes = new ArrayList<>();

    // Getters and setters
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
