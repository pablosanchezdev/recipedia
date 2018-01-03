package models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Finder;
import io.ebean.PagedList;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Min;
import play.data.validation.Constraints.Required;
import play.libs.Json;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipe extends BaseModel {

    @Required
    private String name;

    @Required
    @MaxLength(255)
    private String description;

    @Required
    @Column(columnDefinition = "text")
    private String steps;

    @Required
    private String author;

    @Required
    @MaxLength(60)
    private String kitchen;

    @Required
    @Min(1)
    private Integer rations;

    @Required
    @Min(1)
    private Integer elaborationTime;

    @Required
    @Min(1)
    private Integer cookingTime;

    @ManyToMany(cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Ingredient> ingredients = new ArrayList<>();

    private static final Finder<Long, Recipe> find =
            new Finder<>(Recipe.class);

    private static final int PAGE_SIZE = 25;  // Number of recipes per page

    public static Recipe findById(Long id) {
        return find.byId(id);
    }

    public static Recipe findByNameAndAuthor(String name, String author) {
        return find
                .query()
                .where()
                    .eq("name", name)
                    .eq("author", author)
                .findOne();
    }

    public static PagedList<Recipe> findAll(Integer page) {
        return find
                .query()
                .setMaxRows(PAGE_SIZE)
                .setFirstRow(PAGE_SIZE * page)
                .findPagedList();
    }

    public boolean validateAndSave() {
        if (Recipe.findByNameAndAuthor(this.name, this.author) != null) {
            return false;
        }

        checkRecipeIntegrity();
        this.save();

        return true;
    }

    private void checkRecipeIntegrity() {
        if (!this.getIngredients().isEmpty()) {
            this.getIngredients().clear();
        }
    }

    public void addIngredientAndSave(String ingrName) {
        Ingredient ingredient = Ingredient.findByName(ingrName);
        if (ingredient == null) {
            ingredient = new Ingredient();
            ingredient.setName(camelCaseIngredient(ingrName));
            ingredient.save();
        }

        ingredient.getRecipes().add(this);
        this.getIngredients().add(ingredient);

        this.save();
    }

    public void deleteIngredientAndSave(String ingrName) {
        Ingredient ingredient = Ingredient.findByName(ingrName);
        if (ingredient != null) {
            ingredient.getRecipes().remove(this);
            this.getIngredients().remove(ingredient);
            this.update();
        }
    }

    private String camelCaseIngredient(String ingrName) {
        return ingrName.substring(0, 1).toUpperCase()
                + ingrName.substring(1, ingrName.length()).toLowerCase();
    }

    public JsonNode toJson() {
        return Json.toJson(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getKitchen() {
        return kitchen;
    }

    public void setKitchen(String kitchen) {
        this.kitchen = kitchen;
    }

    public Integer getRations() {
        return rations;
    }

    public void setRations(Integer rations) {
        this.rations = rations;
    }

    public Integer getElaborationTime() {
        return elaborationTime;
    }

    public void setElaborationTime(Integer elaborationTime) {
        this.elaborationTime = elaborationTime;
    }

    public Integer getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(Integer cookingTime) {
        this.cookingTime = cookingTime;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
}
