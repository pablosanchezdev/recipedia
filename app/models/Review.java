package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @ManyToOne
    @JsonManagedReference
    private User user;

    @ManyToOne
    @JsonBackReference
    private Recipe recipe;

    private static final Finder<Long, Review> find =
            new Finder<>(Review.class);

    public Review() {
        super();
    }

    private static Review findByUserAndRecipe(User user, Recipe recipe) {
        return find
                .query()
                .where()
                    .eq("user_id", user.getId())
                    .eq("recipe_id", recipe.getId())
                .findOne();
    }

    public boolean validateAndSave() {
        if (isReviewDuplicated()) {
            return false;
        }

        this.save();

        return true;
    }

    private boolean isReviewDuplicated() {
        return Review.findByUserAndRecipe(this.user, this.recipe) != null;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}
