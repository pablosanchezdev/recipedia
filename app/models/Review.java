package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.ebean.Finder;
import org.hibernate.validator.constraints.Range;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "reviews")
public class Review extends BaseModel {

    @Required
    @MaxLength(255)
    private String comment;

    @Required
    @Range(min = 0, max = 5)
    private Float rating;

    @Required
    @MaxLength(255)
    private String author;

    @ManyToOne
    @JsonBackReference
    private Recipe recipe;

    private static final Finder<Long, Review> find =
            new Finder<>(Review.class);

    public Review() {
        super();
    }

    private static Review findByAuthorAndRecipeId(String author, Long recipeId) {
        return find
                .query()
                .where()
                    .eq("author", author)
                    .eq("recipe_id", recipeId)
                .findOne();
    }

    public boolean validateAndSave() {
        if (Review.findByAuthorAndRecipeId(this.author, this.recipe.getId()) != null) {
            return false;
        }

        this.save();
        return true;
    }

    @JsonIgnore
    @Override
    public Long getId() {
        return super.getId();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}
