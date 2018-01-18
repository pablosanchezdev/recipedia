package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.Ebean;
import io.ebean.ExpressionList;
import io.ebean.Finder;
import io.ebean.PagedList;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.libs.Json;
import validators.DNI;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseModel {

    @Required
    @DNI(message = "error.invalid")
    @JsonIgnore
    private String dni;

    @Required
    @Pattern(value = "[a-zA-Záéíóú\\s]{1,255}+", message = "error.invalid")
    private String name;

    @Required
    @MaxLength(255)
    private String city;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    private Token token;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    @JsonBackReference
    private List<Recipe> recipes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    @JsonBackReference
    private List<Review> reviews = new ArrayList<>();

    private static final Finder<Long, User> find =
            new Finder<>(User.class);

    public User() {
        super();
    }

    public static User findById(Long id) {
        return find.byId(id);
    }

    private static User findByDni(String dni) {
        return find
                .query()
                .where()
                    .ieq("dni", dni)
                .findOne();
    }

    public static PagedList<User> findAll(Integer page) {
        return find
                .query()
                .setMaxRows(PAGE_SIZE)
                .setFirstRow(PAGE_SIZE * page)
                .findPagedList();
    }

    public static PagedList<User> findBy(String name, String city, String[] sortBy, Integer page) {
        ExpressionList<User> searchQuery = find
                .query()
                .where();

        if (name != null) {
            searchQuery.icontains("name", name);
        }
        if (city != null) {
            searchQuery.icontains("city", city);
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
        if (isUserDuplicated()) {
            return false;
        }

        Ebean.beginTransaction();
        try {
            generateToken();
            this.save();

            Ebean.commitTransaction();
        } finally {
            Ebean.endTransaction();
        }

        return true;
    }

    public boolean validateAndUpdate() {
        if (isUserDuplicated()) {
            return false;
        }

        this.update();

        return true;
    }

    // There can be no more than one user with same dni
    private boolean isUserDuplicated() {
        User user = User.findByDni(this.dni);

        if (this.getId() != null) {
            return user != null && !user.getId().equals(this.getId());
        }

        return user != null;
    }

    public String resetToken() {
        Ebean.beginTransaction();
        try {
            Token oldToken = this.getToken();
            generateToken();
            this.update();
            oldToken.delete();

            Ebean.commitTransaction();
        } finally {
            Ebean.endTransaction();
        }

        return this.getToken().getToken();
    }

    private void generateToken() {
        Token token = new Token();
        token.setToken(Token.generateToken());
        token.setUser(this);
        token.save();

        this.setToken(token);
    }

    public JsonNode toJson() {
        return Json.toJson(this);
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
