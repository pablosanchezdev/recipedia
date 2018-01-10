package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.PagedList;
import models.*;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

import java.util.List;

public class RecipeController extends BaseController {

    public Result createRecipe() {
        Form<Recipe> form = formFactory
                .form(Recipe.class)
                .bindFromRequest();

        if (form.hasErrors()) {
            return Results.badRequest(form.errorsAsJson());
        }

        Recipe recipe = form.get();
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

        if (Recipe.findById(id) == null) {
            return Results.notFound();
        }

        Recipe newRecipe = form.get();
        newRecipe.setId(id);
        newRecipe.update();

        return Results.ok();
    }

    public Result partialUpdateRecipe(Long id) {
        Recipe recipe = Recipe.findById(id);
        if (recipe == null) {
            return Results.notFound();
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

            if (modified) {
                recipe.update();
            }

            return Results.ok();
        }

        return Results.badRequest();
    }

    public Result deleteRecipe(Long id) {
        Recipe recipe = Recipe.findById(id);
        if (recipe != null) {
            if (!recipe.delete()) {
                return Results.internalServerError();
            }
        }

        return Results.ok();
    }

    public Result retrieveRecipeCollection(Integer page) {
        PagedList<Recipe> list = Recipe.findAll(page);
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

    public Result addIngredient(Long recipeId, String ingredient) {
        Recipe recipe = Recipe.findById(recipeId);
        if (recipe == null) {
            return Results.notFound();
        }

        if (recipe.validateIngredientAndSave(ingredient)) {
            return Results.created();
        } else {
            return Results.status(409,
                    new ErrorObject("2", getMessage("duplicate_ingredient")).toJson());
        }
    }

    public Result removeIngredient(Long recipeId, String ingredient) {
        Recipe recipe = Recipe.findById(recipeId);
        if (recipe != null) {
            recipe.deleteIngredientAndSave(ingredient);
        }

        return Results.ok();
    }

    public Result addTag(Long recipeId, String tagName) {
        Recipe recipe = Recipe.findById(recipeId);
        if (recipe == null) {
            return Results.notFound();
        }

        if (recipe.validateTagAndSave(tagName)) {
            return Results.created();
        } else {
            return Results.status(409,
                    new ErrorObject("3", getMessage("duplicate_tag")).toJson());
        }
    }

    public Result removeTag(Long recipeId, String tagName) {
        Recipe recipe = Recipe.findById(recipeId);
        if (recipe != null) {
            recipe.removeTagAndSave(tagName);
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
        String author = request().getQueryString("author");
        String kitchen = request().getQueryString("kitchen");
        String rations = request().getQueryString("rations");
        String time = request().getQueryString("time");
        String type = request().getQueryString("type");
        String ingredient = request().getQueryString("ingredient");
        String tag = request().getQueryString("tag");
        String page = request().getQueryString("page");
        String sortBy = request().getQueryString("sortBy");

        PagedList<Recipe> recipes = Recipe.findBy(name, description, difficulty, author,
                kitchen, (rations != null) ? rations.split(":") : null,
                (time != null) ? time.split(":") : null, type, ingredient, tag, page, (sortBy != null) ? sortBy.split(":") : null);
        ObjectNode json = Json.newObject();
        json.put("page", (page != null) ? Integer.parseInt(page) : 0);
        json.put("total", recipes.getTotalCount());
        json.putPOJO("recipes", recipes.getList());
        return Results.ok(json);
    }
}
