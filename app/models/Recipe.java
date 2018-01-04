package models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Finder;
import io.ebean.PagedList;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.libs.Json;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipe extends BaseModel {

    @Required
    @NotBlank
    private String name;

    @Required
    @NotBlank
    @MaxLength(255)
    private String description;

    @Required
    @NotBlank
    @Column(columnDefinition = "text")
    private String steps;

    @Required
    @NotBlank
    @MaxLength(255)
    private String author;

    @Required
    @NotBlank
    @MaxLength(255)
    private String kitchen;

    @Required
    @Range(min = 1, max = 256)
    private Integer rations;

    @Required
    @Range(min = 1, max = 512)
    private Integer elaborationTime;

    @Required
    @Range(min = 1, max = 512)
    private Integer cookingTime;

    @ManyToMany(cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Ingredient> ingredients = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recipe")
    @JsonManagedReference
    private List<Review> reviews = new ArrayList<>();

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
        if (!this.getTags().isEmpty()) {
            this.getTags().clear();
        }
    }

    public int validateIngredientAndSave(String ingrName) {
        if (ingrName.length() > 255) {
            return -1;
        }

        Ingredient ingredient = Ingredient.findByName(ingrName);
        if (ingredient == null) {
            ingredient = new Ingredient();
            ingredient.setName(toCamelCase(ingrName));
            ingredient.save();
        }

        if (ingredient.getRecipes().contains(this)
                && this.getIngredients().contains(ingredient)) {
            return 1;
        }

        ingredient.getRecipes().add(this);
        this.getIngredients().add(ingredient);
        this.save();

        return 0;
    }

    public void deleteIngredientAndSave(String ingrName) {
        Ingredient ingredient = Ingredient.findByName(ingrName);
        if (ingredient != null) {
            ingredient.getRecipes().remove(this);
            this.getIngredients().remove(ingredient);
            this.update();
        }
    }

    public int validateTagAndSave(String tagName) {
        if (tagName.length() > 255) {
            return -1;
        }

        Tag tag = Tag.findByName(tagName);
        if (tag == null) {
            tag = new Tag();
            tag.setName(toCamelCase(tagName));
            tag.save();
        }

        if (tag.getRecipes().contains(this)
                && this.getTags().contains(tag)) {
            return 1;
        }

        tag.getRecipes().add(this);
        this.getTags().add(tag);
        this.save();

        return 0;
    }

    public void removeTagAndSave(String tagName) {
        Tag tag = Tag.findByName(tagName);
        if (tag != null) {
            tag.getRecipes().remove(this);
            this.getTags().remove(tag);
            this.update();
        }
    }

    public boolean addReview(Review review) {
        review.setRecipe(this);
        this.getReviews().add(review);
        return review.validateAndSave();
    }

    private String toCamelCase(String string) {
        return string.substring(0, 1).toUpperCase()
                + string.substring(1, string.length()).toLowerCase();
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

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
