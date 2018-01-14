package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.PagedList;
import models.Recipe;
import models.User;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

import java.util.List;

public class UserController extends BaseController {

    public Result createUser() {
        Form<User> form = formFactory
                .form(User.class)
                .bindFromRequest();

        if (form.hasErrors()) {
            return Results.badRequest(form.errorsAsJson());
        }

        User user = form.get();
        if (user.validateAndSave()) {
            return Results
                    .created()
                    .withHeader("Authorization", user.getToken().getToken());
        } else {
            return Results.status(409,
                    new ErrorObject("5", getMessage("duplicate_user")).toJson());
        }
    }

    @Security.Authenticated(Authorization.class)
    public Result retrieveUser(Long id) {
        User user = User.findById(id);

        if (user == null) {
            return Results.notFound();
        }

        if (request().accepts("application/json")) {
            return Results.ok(user.toJson());
        } else if (request().accepts("application/xml"))  {
            return Results.ok(views.xml.user.render(user));
        } else {
            return Results.status(415);
        }
    }

    @Security.Authenticated(Authorization.class)
    public Result updateUser() {
        Form<User> form = formFactory
                .form(User.class)
                .bindFromRequest();

        if (form.hasErrors()) {
            return Results.badRequest(form.errorsAsJson());
        }

        Long id = getLoggedUser().getId();
        User newUser = form.get();
        newUser.setId(id);
        if (newUser.validateAndUpdate()) {
            return Results.ok();
        } else {
            return Results.status(409,
                    new ErrorObject("5", getMessage("duplicate_user")).toJson());
        }
    }

    @Security.Authenticated(Authorization.class)
    public Result partialUpdateUser() {
        if (request().body() != null && request().body().asJson() != null) {
            User user = getLoggedUser();
            JsonNode body = request().body().asJson();
            boolean modified = false;
            if (body.has("name")) {
                user.setName(body.get("name").asText());
                modified = true;
            }
            if (body.has("city")) {
                user.setCity(body.get("city").asText());
                modified = true;
            }

            if (modified) {
                if (user.validateAndUpdate()) {
                    return Results.ok();
                } else {
                    return Results.status(409,
                            new ErrorObject("5", getMessage("duplicate_user")).toJson());
                }
            }
        }

        return Results.badRequest();
    }

    @Security.Authenticated(Authorization.class)
    public Result deleteUser() {
        User user = getLoggedUser();
        if (!user.delete()) {
            return Results.internalServerError();
        }

        return Results.ok();
    }

    @Security.Authenticated(Authorization.class)
    public Result retrieveUserCollection(Integer page) {
        PagedList<User> users = User.findAll(page);

        return displayUsers(users, page);
    }

    @Security.Authenticated(Authorization.class)
    public Result searchUsers() {
        String name = request().getQueryString("name");
        String city = request().getQueryString("city");
        String sortBy = request().getQueryString("sortBy");
        String pageRequested = request().getQueryString("page");
        Integer page = (pageRequested != null) ? Integer.parseInt(pageRequested) : 0;

        PagedList<User> users = User.findBy(name, city,
                (sortBy != null) ? sortBy.split(":") : null, page);

        return displayUsers(users, page);
    }

    @Security.Authenticated(Authorization.class)
    public Result retrieveUserRecipes(Long id, Integer page) {
        if (User.findById(id) == null) {
            return Results.notFound();
        }

        PagedList<Recipe> recipes = Recipe.findByUser(id, page);

        return RecipeController.displayRecipes(recipes, page);
    }

    @Security.Authenticated(Authorization.class)
    public Result resetToken() {
        User user = getLoggedUser();
        String token = user.resetToken();

        return Results
                .ok()
                .withHeader("Authorization", token);
    }

    private Result displayUsers(PagedList<User> list, Integer page) {
        List<User> users = list.getList();

        if (request().accepts("application/json")) {
            ObjectNode json = Json.newObject();
            json.put("page", page);
            json.put("total", list.getTotalCount());
            json.putPOJO("users", users);
            return ok(json);
        } else if (request().accepts("application/xml")) {
            return ok(views.xml.users.render(page, list.getTotalCount(), users));
        } else {
            return Results.status(415);
        }
    }
}
