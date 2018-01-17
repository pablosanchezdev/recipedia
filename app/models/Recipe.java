package models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Ebean;
import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.PagedList;
import io.ebean.annotation.EnumValue;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.libs.Json;

import javax.persistence.*;
import javax.validation.constraints.Min;
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
    @Enumerated(EnumType.STRING)
    public Difficulty difficulty;

    public enum Difficulty {
        @EnumValue("Alta")
        Alta,
        @EnumValue("Media")
        Media,
        @EnumValue("Baja")
        Baja,
    }

    @Required
    @Column(columnDefinition = "text")
    private String steps;

    @ManyToOne
    @JsonManagedReference
    private User user;

    @Required
    @MaxLength(255)
    private String kitchen;

    @Required
    @Min(1)
    private Integer rations;

    @Required
    @Min(1)
    private Integer time;

    @Required
    @Enumerated(EnumType.STRING)
    public Type type;

    public enum Type {
        @EnumValue("Entrante")
        Entrante,
        @EnumValue("Primero")
        Primero,
        @EnumValue("Segundo")
        Segundo,
        @EnumValue("Postre")
        Postre
    }

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

    public Recipe() {
        super();
    }

    public static Recipe findById(Long id) {
        return find.byId(id);
    }

    private static Recipe findByNameAndUser(String name, User user) {
        return find
                .query()
                .where()
                    .eq("name", name)
                    .eq("user.id", user.getId())
                .findOne();
    }

    public static PagedList<Recipe> findByUser(Long userId, Integer page) {
        return find
                .query()
                .where()
                    .eq("user.id", userId)
                .setMaxRows(PAGE_SIZE)
                .setFirstRow(PAGE_SIZE * page)
                .findPagedList();
    }

    public static PagedList<Recipe> findAll(Integer page) {
        return find
                .query()
                .setMaxRows(PAGE_SIZE)
                .setFirstRow(PAGE_SIZE * page)
                .findPagedList();
    }

    public static PagedList<Recipe> findBy(String name, String description, String difficulty,
                                           String userId, String kitchen, String[] rations,
                                           String[] time, String type, String ingredient,
                                           String tag, String[] sortBy, Integer page) {
        ExpressionList<Recipe> searchQuery = find
                .query()
                .where();

        if (name != null) {
            searchQuery.icontains("name", name);
        }
        if (description != null) {
            searchQuery.icontains("description", description);
        }
        if (difficulty != null) {
            searchQuery.ieq("difficulty", difficulty);
        }
        if (userId != null) {
            searchQuery.icontains("user.id", userId);
        }
        if (kitchen != null) {
            searchQuery.icontains("kitchen", kitchen);
        }
        if (rations != null && rations.length == 2) {
            if (rations[1].equalsIgnoreCase("eq")) {
                searchQuery.eq("rations", rations[0]);
            } else if (rations[1].equalsIgnoreCase("gt")) {
                searchQuery.gt("rations", rations[0]);
            } else if (rations[1].equalsIgnoreCase("lt")) {
                searchQuery.lt("rations", rations[0]);
            }
        }
        if (time != null && time.length == 2) {
            if (time[1].equalsIgnoreCase("eq")) {
                searchQuery.eq("time", time[0]);
            } else if (time[1].equalsIgnoreCase("gt")) {
                searchQuery.gt("time", time[0]);
            } else if (time[1].equalsIgnoreCase("lt")) {
                searchQuery.lt("time", time[0]);
            }
        }
        if (type != null) {
            searchQuery.ieq("type", type);
        }
        if (ingredient != null) {
            searchQuery.ieq("ingredients.name", ingredient);
        }
        if (tag != null) {
            searchQuery.ieq("tags.name", tag);
        }
        if (sortBy != null && sortBy.length == 2 && (sortBy[1].equalsIgnoreCase("asc")
                || sortBy[1].equalsIgnoreCase("desc"))) {
            searchQuery.query().orderBy(sortBy[0] + " " + sortBy[1]);
        }

        return searchQuery
                .query()
                .setMaxRows(PAGE_SIZE)
                .setFirstRow(PAGE_SIZE * page)
                .findPagedList();
    }

    public boolean validateAndSave() {
        if (isRecipeDuplicated()) {
            return false;
        }

        checkRecipeIntegrity();
        this.save();

        return true;
    }

    public boolean validateAndUpdate() {
        if (isRecipeDuplicated()) {
            return false;
        }

        checkRecipeIntegrity();
        this.update();

        return true;
    }

    private boolean isRecipeDuplicated() {
        Recipe recipe = Recipe.findByNameAndUser(this.name, this.user);

        if (this.getId() != null) {
            return recipe != null && !recipe.getId().equals(this.getId());
        }

        return recipe != null;
    }

    private void checkRecipeIntegrity() {
        if (!this.getIngredients().isEmpty()) {
            this.getIngredients().clear();
        }
        if (!this.getTags().isEmpty()) {
            this.getTags().clear();
        }
        if (!this.getReviews().isEmpty()) {
            this.getReviews().clear();
        }
    }

    public boolean validateIngredientAndSave(String ingrName) {
        Ingredient ingredient = Ingredient.findByName(ingrName);

        Ebean.beginTransaction();
        try {
            if (ingredient != null) {
                if (ingredient.getRecipes().contains(this)
                        && this.getIngredients().contains(ingredient)) {
                    Ebean.endTransaction();
                    return false;
                }
            } else {
                ingredient = new Ingredient();
                ingredient.setName(toCamelCase(ingrName));
                ingredient.save();
            }

            ingredient.getRecipes().add(this);
            this.getIngredients().add(ingredient);
            this.save();

            Ebean.commitTransaction();
        } finally {
            Ebean.endTransaction();
        }

        return true;
    }

    public void deleteIngredientAndSave(String ingrName) {
        Ingredient ingredient = Ingredient.findByName(ingrName);
        if (ingredient != null) {
            ingredient.getRecipes().remove(this);
            this.getIngredients().remove(ingredient);
            this.update();
        }
    }

    public boolean validateTagAndSave(String tagName) {
        Tag tag = Tag.findByName(tagName);

        Ebean.beginTransaction();
        try {
            if (tag != null) {
                if (tag.getRecipes().contains(this)
                        && this.getTags().contains(tag)) {
                    Ebean.endTransaction();
                    return false;
                }
            } else {
                tag = new Tag();
                tag.setName(toCamelCase(tagName));
                tag.save();
            }

            tag.getRecipes().add(this);
            this.getTags().add(tag);
            this.save();

            Ebean.commitTransaction();
        } finally {
            Ebean.endTransaction();
        }

        return true;
    }

    public void deleteTagAndSave(String tagName) {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
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
