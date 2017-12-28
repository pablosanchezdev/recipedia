package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.PagedList;
import models.Recipe;
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
            return Results.status(409, form.errorsAsJson());
        }

        Recipe recipe = form.get();
        if (recipe.validateAndSave()) {
            return Results.created();
        } else {
            return Results.status(409,
                    new ErrorObject("1", "The recipe already exists").toJson());
        }
    }

    public Result retrieveRecipe(Long id) {
        Recipe recipe = Recipe.findById(id);

        if (recipe == null) {
            return Results.notFound();
        }

        return Results.ok(recipe.toJson());
    }

    public Result updateRecipe(Long id) {
        Form<Recipe> form = formFactory
                .form(Recipe.class)
                .bindFromRequest();

        if (form.hasErrors()) {
            return Results.status(409, form.errorsAsJson());
        }

        Recipe newRecipe = form.get();
        if (Recipe.findById(id) == null) {
            return Results.notFound();
        }

        newRecipe.setId(id);
        newRecipe.update();

        return Results.ok();
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

        ObjectNode json = Json.newObject();
        json.put("page", page);
        json.put("total", list.getTotalCount());
        json.putPOJO("recipes", recipes);

        return Results.ok(json);
    }
}
