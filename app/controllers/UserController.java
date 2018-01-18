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
import play.twirl.api.Content;

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
                    new ErrorObject(ErrorObject.DUPLICATE_USER,
                            getMessage("duplicate_user")).toJson());
        }
    }

    @Security.Authenticated(Authorization.class)
    public Result retrieveUser(Long id) {
        String key = getSingleUserCacheKey(id);
        User user = cache.get(key);
        if (user == null) {
            user = User.findById(id);
            cache.set(key, user);
        }

        if (user == null) {
            return Results.notFound();
        }

        if (request().accepts("application/json")) {
            key = getSingleUserResponseCacheKey(id, "json");
            JsonNode json = cache.get(key);
            if (json == null) {
                json = user.toJson();
                cache.set(key, json);
            }
            return Results.ok(json);
        } else if (request().accepts("application/xml"))  {
            key = getSingleUserResponseCacheKey(id, "xml");
            Content content = cache.get(key);
            if (content == null) {
                content = views.xml.user.render(user);
                cache.set(key, content);
            }
            return Results.ok(content);
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
            deleteUserFromCache(id);
            return Results.ok();
        } else {
            return Results.status(409,
                    new ErrorObject(ErrorObject.DUPLICATE_USER,
                            getMessage("duplicate_user")).toJson());
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
                    deleteUserFromCache(user.getId());
                    return Results.ok();
                } else {
                    return Results.status(409,
                            new ErrorObject(ErrorObject.DUPLICATE_USER,
                                    getMessage("duplicate_user")).toJson());
                }
            }
        }

        return Results.badRequest();
    }

    @Security.Authenticated(Authorization.class)
    public Result deleteUser() {
        User user = getLoggedUser();
        deleteUserFromCache(user.getId());
        deleteUserRecipesFromCache(user);
        if (!user.delete()) {
            return Results.internalServerError();
        }

        return Results.ok();
    }

    @Security.Authenticated(Authorization.class)
    public Result retrieveUserCollection(Integer page) {
        String key = getPagedUserCollectionCacheKey(page);
        PagedList<User> list = cache.get(key);
        if (list == null) {
            list = User.findAll(page);
            cache.set(key, list, 2 * 60);
        }

        return displayUsers(list, page);
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

        String key = getPagedUserRecipeCollectionCacheKey(id, page);
        PagedList<Recipe> list = cache.get(key);
        if (list == null) {
            list = Recipe.findByUser(id, page);
            cache.set(key, list, 2 * 60);
        }

        return RecipeController.displayRecipes(list, page);
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

    private String getSingleUserCacheKey(Long id) {
        return "user-" + id;
    }

    private String getSingleUserResponseCacheKey(Long id, String format) {
        return "user-" + id + "-" + format;
    }

    private String getPagedUserCollectionCacheKey(Integer page) {
        return "users-" + page;
    }

    private String getPagedUserRecipeCollectionCacheKey(Long id, Integer page) {
        return "user-" + id + "-recipes-" + page;
    }

    private void deleteUserFromCache(Long id) {
        cache.remove(getSingleUserCacheKey(id));
        cache.remove(getSingleUserResponseCacheKey(id, "json"));
        cache.remove(getSingleUserResponseCacheKey(id, "xml"));
    }

    private void deleteUserRecipesFromCache(User user) {
        for (Recipe recipe : user.getRecipes()) {
            deleteRecipeFromCache(recipe.getId());
        }
    }
}
