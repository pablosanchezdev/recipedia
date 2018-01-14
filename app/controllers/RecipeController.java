package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.PagedList;
import models.Recipe;
import models.Review;
import models.User;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

import java.util.List;

@Security.Authenticated(Authorization.class)
public class RecipeController extends BaseController {

    public Result createRecipe() {
        Form<Recipe> form = formFactory
                .form(Recipe.class)
                .bindFromRequest();

        if (form.hasErrors()) {
            return Results.badRequest(form.errorsAsJson());
        }

        Recipe recipe = form.get();
        recipe.setUser(getLoggedUser());
        if (recipe.validateAndSave()) {
            return Results.created();
        } else {
            return Results.status(409,
                    new ErrorObject("1", getMessage("duplicate_recipe")).toJson());
        }
    }

    public Result retrieveRecipe(Long id) {
        Recipe recipe = Recipe.findById(id);

        if (recipe == null) {
            return Results.notFound();
        }

        if (request().accepts("application/json")) {
            return Results.ok(recipe.toJson());
        } else if (request().accepts("application/xml")) {
            return Results.ok(views.xml.recipe.render(recipe));
        } else {
            return Results.status(415);
        }
    }

    public Result updateRecipe(Long id) {
        Form<Recipe> form = formFactory
                .form(Recipe.class)
                .bindFromRequest();

        if (form.hasErrors()) {
            return Results.badRequest(form.errorsAsJson());
        }

        Recipe oldRecipe = Recipe.findById(id);
        if (oldRecipe == null) {
            return Results.notFound();
        }

        User user = getLoggedUser();
        if (isUserUnauthorized(oldRecipe, user)) {
            return Results.unauthorized(
                    new ErrorObject("6", getMessage("update_unauthorized")).toJson());
        }

        Recipe newRecipe = form.get();
        newRecipe.setId(id);
        newRecipe.setUser(user);
        if (newRecipe.validateAndUpdate()) {
            return Results.ok();
        } else {
            return Results.status(409,
                    new ErrorObject("1", getMessage("duplicate_recipe")).toJson());
        }
    }

    public Result partialUpdateRecipe(Long id) {
        Recipe recipe = Recipe.findById(id);
        if (recipe == null) {
            return Results.notFound();
        }

        if (isUserUnauthorized(recipe, getLoggedUser())) {
            return Results.unauthorized(
                    new ErrorObject("6", getMessage("update_unauthorized")).toJson());
        }

        if (request().body() != null && request().body().asJson() != null) {
            JsonNode body = request().body().asJson();
            boolean modified = false;
            if (body.has("name")) {
                recipe.setName(body.get("name").asText());
                modified = true;
            }
            if (body.has("description")) {
                recipe.setDescription(body.get("description").asText());
                modified = true;
            }
            if (body.has("difficulty")) {
                recipe.difficulty = Recipe.Difficulty.valueOf(body.get("difficulty").asText());
                modified = true;
            }
            if (body.has("steps")) {
                recipe.setSteps(body.get("steps").asText());
                modified = true;
            }
            if (body.has("kitchen")) {
                recipe.setKitchen(body.get("kitchen").asText());
                modified = true;
            }
            if (body.has("rations")) {
                recipe.setRations(body.get("rations").asInt());
                modified = true;
            }
            if (body.has("time")) {
                recipe.setTime(body.get("time").asInt());
                modified = true;
            }
            if (body.has("type")) {
                recipe.type = Recipe.Type.valueOf(body.get("type").asText());
                modified = true;
            }

            if (modified) {
                if (recipe.validateAndUpdate()) {
                    return Results.ok();
                } else {
                    return Results.status(409,
                            new ErrorObject("1", getMessage("duplicate_recipe")).toJson());
                }
            }
        }

        return Results.badRequest();
    }

    public Result deleteRecipe(Long id) {
        Recipe recipe = Recipe.findById(id);
        if (recipe != null) {
            if (isUserUnauthorized(recipe, getLoggedUser())) {
                return Results.unauthorized(
                        new ErrorObject("7", getMessage("delete_unauthorized")).toJson());
            }
            if (!recipe.delete()) {
                return Results.internalServerError();
            }
        }

        return Results.ok();
    }

    public Result retrieveRecipeCollection(Integer page) {
        PagedList<Recipe> list = Recipe.findAll(page);

        return displayRecipes(list, page);
    }

    public Result addIngredient(Long recipeId, String ingredient) {
        Recipe recipe = Recipe.findById(recipeId);
        if (recipe == null) {
            return Results.notFound();
        }

        if (isUserUnauthorized(recipe, getLoggedUser())) {
            return Results.unauthorized(
                    new ErrorObject("6", getMessage("update_unauthorized")).toJson());
        }

        if (recipe.validateIngredientAndSave(ingredient)) {
            return Results.created();
        } else {
            return Results.status(409,
                    new ErrorObject("2", getMessage("duplicate_ingredient")).toJson());
        }
    }

    public Result deleteIngredient(Long recipeId, String ingredient) {
        Recipe recipe = Recipe.findById(recipeId);
        if (recipe != null) {
            if (isUserUnauthorized(recipe, getLoggedUser())) {
                return Results.unauthorized(
                        new ErrorObject("6", getMessage("update_unauthorized")).toJson());
            }
            recipe.deleteIngredientAndSave(ingredient);
        }

        return Results.ok();
    }

    public Result addTag(Long recipeId, String tagName) {
        Recipe recipe = Recipe.findById(recipeId);
        if (recipe == null) {
            return Results.notFound();
        }

        if (isUserUnauthorized(recipe, getLoggedUser())) {
            return Results.unauthorized(
                    new ErrorObject("6", getMessage("update_unauthorized")).toJson());
        }

        if (recipe.validateTagAndSave(tagName)) {
            return Results.created();
        } else {
            return Results.status(409,
                    new ErrorObject("3", getMessage("duplicate_tag")).toJson());
        }
    }

    public Result deleteTag(Long recipeId, String tagName) {
        Recipe recipe = Recipe.findById(recipeId);
        if (recipe != null) {
            if (isUserUnauthorized(recipe, getLoggedUser())) {
                return Results.unauthorized(
                        new ErrorObject("6", getMessage("update_unauthorized")).toJson());
            }
            recipe.deleteTagAndSave(tagName);
        }

        return Results.ok();
    }

    public Result addReview(Long id) {
        Form<Review> form = formFactory
                .form(Review.class)
                .bindFromRequest();

        if (form.hasErrors()) {
            return Results.badRequest(form.errorsAsJson());
        }

        Recipe recipe = Recipe.findById(id);
        if (recipe == null) {
            return Results.notFound();
        }

        Review review = form.get();
        review.setUser(getLoggedUser());
        if (recipe.addReview(review)) {
            return Results.created();
        } else {
            return Results.status(409,
                    new ErrorObject("4", getMessage("duplicate_review")).toJson());
        }
    }

    public Result searchRecipes() {
        String name = request().getQueryString("name");
        String description = request().getQueryString("description");
        String difficulty = request().getQueryString("difficulty");
        String userId = request().getQueryString("userId");
        String kitchen = request().getQueryString("kitchen");
        String rations = request().getQueryString("rations");
        String time = request().getQueryString("time");
        String type = request().getQueryString("type");
        String ingredient = request().getQueryString("ingredient");
        String tag = request().getQueryString("tag");
        String sortBy = request().getQueryString("sortBy");
        String pageRequested = request().getQueryString("page");
        Integer page = (pageRequested != null) ? Integer.parseInt(pageRequested) : 0;

        PagedList<Recipe> recipes = Recipe.findBy(name, description, difficulty, userId, kitchen,
                (rations != null) ? rations.split(":") : null, (time != null) ? time.split(":") : null,
                type, ingredient, tag, (sortBy != null) ? sortBy.split(":") : null, page);

        return displayRecipes(recipes, page);
    }

    public static Result displayRecipes(PagedList<Recipe> list, Integer page) {
        List<Recipe> recipes = list.getList();

        if (request().accepts("application/json")) {
            ObjectNode json = Json.newObject();
            json.put("page", page);
            json.put("total", list.getTotalCount());
            json.putPOJO("recipes", recipes);
            return Results.ok(json);
        } else if (request().accepts("application/xml")) {
            return Results.ok(views.xml.recipes.render(page, list.getTotalCount(), recipes));
        } else {
            return Results.status(415);
        }
    }

    private boolean isUserUnauthorized(Recipe recipe, User user) {
        return !recipe.getUser().getId().equals(user.getId());
    }
}
