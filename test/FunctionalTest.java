import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Recipe;
import models.Review;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import play.twirl.api.Content;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A functional test starts a Play application for every test.
 *
 * https://www.playframework.com/documentation/latest/JavaFunctionalTest
 */
public class FunctionalTest extends WithApplication {

    private User user1, user2;
    private Recipe recipe1, recipe2;
    private ObjectNode userJson, userPatchJson, userPatchMalformedJson;
    private ObjectNode recipeJson, recipePatchJson, recipePatchMalformedJson;
    private ObjectNode reviewJson, reviewJsonBadRequest;

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(Helpers.inMemoryDatabase());
    }

    @Before
    public void setupTestData() {
        user1 = getUser1();
        user1.validateAndSave();

        user2 = getUser2();
        user2.validateAndSave();

        recipe1 = getRecipe1();
        recipe1.setUser(user1);
        recipe1.validateAndSave();

        recipe2 = getRecipe2();
        recipe2.setUser(user1);
        recipe2.validateAndSave();

        userJson = (ObjectNode) getUserJson();
        userJson.put("dni", "78345637R");

        userPatchJson = Json.newObject();
        userPatchJson.put("name", "Luis Pérez");
        userPatchJson.put("city", "Leganés");

        userPatchMalformedJson = Json.newObject();
        userPatchMalformedJson.put("address", "Paseo de la Castellana, 85");

        recipeJson = (ObjectNode) getRecipeJson();

        recipePatchJson = Json.newObject();
        recipePatchJson.put("name", "Pasta con tomate");
        recipePatchJson.put("difficulty", "Alta");

        recipePatchMalformedJson = Json.newObject();
        recipePatchMalformedJson.put("cookingTime", 60);

        reviewJson = (ObjectNode) getReviewJson();

        reviewJsonBadRequest = (ObjectNode) getReviewJson();
        reviewJsonBadRequest.put("rating", 5.1f);
    }

    @Test
    public void testUserPostBadRequest() {
        userJson.remove("dni");

        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user")
                .header("Content-Type", "application/json")
                .bodyJson(userJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(400);
    }

    @Test
    public void testUserPostOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user")
                .header("Content-Type", "application/json")
                .bodyJson(userJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(201);
        assertThat(r.header("Authorization").orElse("").length()).isEqualTo(20);
    }

    @Test
    public void testUserPostConflict() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user")
                .header("Content-Type", "application/json")
                .bodyJson(userJson);

        Helpers.route(app, req);
        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(409);
    }

    @Test
    public void testUserGetUnauthorized() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user1.getId());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(401);
    }

    @Test
    public void testUserGetNotFound() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + (user2.getId() + 1))
                .header("Authorization", user1.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(404);
    }

    @Test
    public void testUserGetOkJson() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user1.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/json");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/json");

        JsonNode json = user1.toJson();
        assertThat(json).isNotNull();
        assertThat(json.get("id").asInt()).isPositive();
        assertThat(json.get("name").asText()).isNotEmpty();
        assertThat(json.get("city").asText()).isNotEmpty();
    }

    @Test
    public void testUserGetOkXml() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user1.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/xml");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/xml");

        Content xml = views.xml.user.render(user1);
        assertThat(xml).isNotNull();

        String body = xml.body();
        assertThat(body).contains("<id>");
        assertThat(body).contains("<name>");
        assertThat(body).contains("<city>");
    }

    @Test
    public void testUserGetUnsupportedMediaType() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user1.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/html");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(415);
    }

    @Test
    public void testUserPutOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PUT")
                .uri("/user")
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(userJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }

    @Test
    public void testUserPutDuplicated() {
        userJson.put("dni", user2.getDni());

        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PUT")
                .uri("/user")
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(userJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(409);
        assertThat(r.contentType().orElse("")).isEqualTo("application/json");
    }

    @Test
    public void testUserPatchOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PATCH")
                .uri("/user")
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(userPatchJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }

    @Test
    public void testUserPatchBadRequest() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PATCH")
                .uri("/user")
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(userPatchMalformedJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(400);
    }

    @Test
    public void testUserDeleteOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("DELETE")
                .uri("/user")
                .header("Authorization", user1.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }

    @Test
    public void testUserCollectionGetOkJson() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/users/0")
                .header("Authorization", user2.getToken().getToken())
                .header("Accept", "application/json");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/json");
    }

    @Test
    public void testUserCollectionGetOkXml() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/users/0")
                .header("Authorization", user2.getToken().getToken())
                .header("Accept", "application/xml");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/xml");
    }

    @Test
    public void testUserRecipesGetNotFound() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + (user2.getId() + 1) + "/recipes/0")
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/json");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(404);
    }

    @Test
    public void testUserRecipesGetOkJson() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user1.getId() + "/recipes/0")
                .header("Authorization", user2.getToken().getToken())
                .header("Accept", "application/json");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/json");
    }

    @Test
    public void testUserRecipesGetOkXml() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user1.getId() + "/recipes/0")
                .header("Authorization", user2.getToken().getToken())
                .header("Accept", "application/xml");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/xml");
    }

    @Test
    public void testUserFilterSearchOkJson() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/users/search?name=pab&city=salamanca")
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/json");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/json");
    }

    @Test
    public void testUserFilterSearchOkXml() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/users/search?name=pab&city=salamanca")
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/xml");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/xml");
    }

    @Test
    public void testUserResetTokenOk() {
        String oldToken = user1.getToken().getToken();

        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user/resetToken")
                .header("Authorization", user1.getToken().getToken());

        Result r = Helpers.route(app, req);

        String newToken = r.header("Authorization").orElse(oldToken);
        assertThat(oldToken.equals(newToken)).isFalse();
        assertThat(newToken.length()).isEqualTo(20);
    }

    @Test
    public void testRecipePostUnauthorized() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(401);
    }

    @Test
    public void testRecipePostBadRequest() {
        recipeJson.remove("name");

        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe")
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(recipeJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(400);
    }

    @Test
    public void testRecipePostOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe")
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(recipeJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(201);
    }

    @Test
    public void testRecipePostConflict() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe")
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(recipeJson);

        Helpers.route(app, req);
        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(409);
    }

    @Test
    public void testRecipeGetNotFound() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/recipe/" + (recipe2.getId() + 1))
                .header("Authorization", user1.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(404);
    }

    @Test
    public void testRecipeGetOkJson() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/recipe/" + recipe2.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/json");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/json");

        JsonNode json = recipe2.toJson();
        assertThat(json).isNotNull();
        assertThat(json.get("id").asInt()).isPositive();
        assertThat(json.get("name").asText()).isNotEmpty();
        assertThat(json.get("steps").asText()).isNotEmpty();
    }

    @Test
    public void testRecipeGetOkXml() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/recipe/" + recipe2.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/xml");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/xml");

        Content xml = views.xml.recipe.render(recipe2);
        assertThat(xml).isNotNull();
        assertThat(xml.body()).contains("<id>");
        assertThat(xml.body()).contains("<name>");
        assertThat(xml.body()).contains("<steps>");
    }

    @Test
    public void testRecipeGetUnsupportedMediaType() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/recipe/" + recipe2.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/xhtml");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(415);
    }

    @Test
    public void testRecipePutUnauthorized() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PUT")
                .uri("/recipe/" + recipe1.getId())
                .header("Authorization", user2.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(recipeJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(401);
    }

    @Test
    public void testRecipePutOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PUT")
                .uri("/recipe/" + recipe2.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(recipeJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }

    @Test
    public void testRecipePutConflict() {
        recipeJson.put("name", recipe1.getName());

        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PUT")
                .uri("/recipe/" + recipe2.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(recipeJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(409);
    }

    @Test
    public void testRecipePatchUnauthorized() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PATCH")
                .uri("/recipe/" + recipe1.getId())
                .header("Authorization", user2.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(recipePatchJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(401);
    }

    @Test
    public void testRecipePatchOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PATCH")
                .uri("/recipe/" + recipe1.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(recipePatchJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }

    @Test
    public void testRecipePatchConflict() {
        recipePatchJson.put("name", recipe1.getName());

        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PATCH")
                .uri("/recipe/" + recipe2.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(recipePatchJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(409);
    }

    @Test
    public void testRecipePatchBadRequest() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PATCH")
                .uri("/recipe/" + recipe1.getId())
                .header("Authorization", user1.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(recipePatchMalformedJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(400);
    }

    @Test
    public void testRecipeDeleteUnauthorized() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("DELETE")
                .uri("/recipe/" + recipe1.getId())
                .header("Authorization", user2.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(401);
    }

    @Test
    public void testRecipeDeleteOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("DELETE")
                .uri("/recipe/" + recipe1.getId())
                .header("Authorization", user1.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }

    @Test
    public void testRecipeCollectionGetOkJson() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/recipes/0")
                .header("Authorization", user2.getToken().getToken())
                .header("Accept", "application/json");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/json");
    }

    @Test
    public void testRecipeCollectionGetOkXml() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/recipes/0")
                .header("Authorization", user2.getToken().getToken())
                .header("Accept", "application/xml");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/xml");
    }

    @Test
    public void testRecipeIngredientPostUnauthorized() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe1.getId() + "/ingredient/tomate")
                .header("Authorization", user2.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(401);
    }

    @Test
    public void testRecipeIngredientPostOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe1.getId() + "/ingredient/tomate")
                .header("Authorization", user1.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(201);
    }

    @Test
    public void testRecipeIngredientPostConflict() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe1.getId() + "/ingredient/tomate")
                .header("Authorization", user1.getToken().getToken());

        Helpers.route(app, req);
        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(409);
    }

    @Test
    public void testRecipeIngredientDeleteOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe1.getId() + "/ingredient/tomate")
                .header("Authorization", user1.getToken().getToken());

        Helpers.route(app, req);

        req.method("DELETE");
        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }

    @Test
    public void testRecipeTagPostUnauthorized() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe2.getId() + "/tag/celiaco")
                .header("Authorization", user2.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(401);
    }

    @Test
    public void testRecipeTagPostOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe2.getId() + "/tag/celiaco")
                .header("Authorization", user1.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(201);
    }

    @Test
    public void testRecipeTagPostConflict() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe2.getId() + "/tag/celiaco")
                .header("Authorization", user1.getToken().getToken());

        Helpers.route(app, req);
        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(409);
    }

    @Test
    public void testRecipeTagDeleteOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe2.getId() + "/tag/celiaco")
                .header("Authorization", user1.getToken().getToken());

        Helpers.route(app, req);

        req.method("DELETE");
        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }

    @Test
    public void testRecipeReviewPostBadRequest() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe1.getId() + "/review")
                .header("Authorization", user2.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(reviewJsonBadRequest);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(400);
    }

    @Test
    public void testRecipeReviewPostOk() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe1.getId() + "/review")
                .header("Authorization", user2.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(reviewJson);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(201);
    }

    @Test
    public void testRecipeReviewPostConflict() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/recipe/" + recipe1.getId() + "/review")
                .header("Authorization", user2.getToken().getToken())
                .header("Content-Type", "application/json")
                .bodyJson(reviewJson);

        Helpers.route(app, req);
        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(409);
    }

    @Test
    public void testRecipeFilterSearchOkJson() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/recipes/search?name=pastel&time=30:gt")
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/json");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/json");
    }

    @Test
    public void testRecipeFilterSearchOkXml() {
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/recipes/search?name=pastel&time=30:gt")
                .header("Authorization", user1.getToken().getToken())
                .header("Accept", "application/xml");

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
        assertThat(r.contentType().orElse("")).isEqualTo("application/xml");
    }

    private User getUser1() {
        User user = new User();
        user.setDni("70917793F");
        user.setName("Pablo Sánchez Egido");
        user.setCity("Salamanca");

        return user;
    }

    private User getUser2() {
        User user = getUser1();
        user.setDni("70899287Q");

        return user;
    }

    private JsonNode getUserJson() {
        return getUser1().toJson();
    }

    private Recipe getRecipe1() {
        Recipe recipe = new Recipe();
        recipe.setName("Alcachofas guisadas con verduras");
        recipe.setDescription("Delicioso plato de alcachofas cocidas");
        recipe.difficulty = Recipe.Difficulty.Baja;
        recipe.setSteps("Añadir las alcachofas cortadas por la mitad, rehogar durante 1 minuto y cubrir");
        recipe.setKitchen("Española");
        recipe.setRations(4);
        recipe.setTime(60);
        recipe.type = Recipe.Type.Primero;

        return recipe;
    }

    private Recipe getRecipe2() {
        Recipe recipe = getRecipe1();
        recipe.setName("Pastel de bulgur con hummus de garbanzos");

        return recipe;
    }

    private JsonNode getRecipeJson() {
        Recipe recipe = getRecipe1();
        recipe.setName("Migas de bacalao con tomate");

        return recipe.toJson();
    }

    private JsonNode getReviewJson() {
        Review review = new Review();
        review.setComment("Deliciosa receta. Me ha gustado mucho.");
        review.setRating(4.5f);

        return Json.toJson(review);
    }
}
