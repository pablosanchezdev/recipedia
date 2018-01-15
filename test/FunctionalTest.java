import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
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

    /* @Test
    public void renderTemplate() {
        // If you are calling out to Assets, then you must instantiate an application
        // because it makes use of assets metadata that is configured from
        // the application.

        Content html = views.html.index.render("Your new application is ready.");
        assertThat("text/html").isEqualTo(html.contentType());
        assertThat(html.body()).contains("Your new application is ready.");
    } */

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(Helpers.inMemoryDatabase());
    }





    @Test
    public void testUserCreationBadRequest() {
        // Create json
        ObjectNode json = getTestUserJson();

        // Remove dni
        json.remove("dni");

        // Generate request
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user")
                .header("Content-Type", "application/json")
                .bodyJson(json);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(400);
    }



    @Test
    public void testUserCreationOk() {
        // Create json
        ObjectNode json = getTestUserJson();

        // Generate request
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user")
                .header("Content-Type", "application/json")
                .bodyJson(json);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(201);
        assertThat(r.header("Authorization").isPresent());
        assertThat(r.header("Authorization").get().length()).isEqualTo(20);
    }



    @Test
    public void testUserCreationConflict() {
        // Create json
        ObjectNode json = getTestUserJson();

        // Generate request
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user")
                .header("Content-Type", "application/json")
                .bodyJson(json);

        Helpers.route(app, req);

        // Repeat same request
        req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user")
                .header("Content-Type", "application/json")
                .bodyJson(json);

        Result r = Helpers.route(app, req);

        // Assert result
        assertThat(r.status()).isEqualTo(409);
    }



    @Test
    public void testUserRetrievalUnauthorized() {

        User user = getTestUser();
        user.validateAndSave();


        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user.getId());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(401);
    }



    @Test
    public void testUserRetrievalNotFound() {
        User user = getTestUser();
        user.validateAndSave();


        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user.getId() + 1)
                .header("Authorization", user.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(404);
    }



    @Test
    public void testUserRetrievalOk() {
        // Create user and save
        User user = getTestUser();
        user.validateAndSave();

        // Generate request
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user.getId())
                .header("Authorization", user.getToken().getToken());

        Result r = Helpers.route(app, req);

        // Assert result
        assertThat(r.status()).isEqualTo(200);
    }



    @Test
    public void testUserUpdateOk() {
        // Create user and save
        User user = getTestUser();
        user.validateAndSave();

        ObjectNode json = getTestUserJson();

        json.put("name", "Javier García Antúnez");


        // Generate request
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PUT")
                .uri("/user")
                .header("Content-Type", "application/json")
                .header("Authorization", user.getToken().getToken())
                .bodyJson(json);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }


    @Test
    public void testUserUpdateRepeated() {
        // Create 2 users and save
        User user = getTestUser();
        user.validateAndSave();

        User user2 = new User();
        user2.setDni("70899287Q");
        user2.setName("Javier García Antúnez");
        user2.setCity("Salamanca");

        user2.validateAndSave();

        ObjectNode json = Json.newObject();
        json.put("dni", "70899287Q");
        json.put("name", "Javier García Antúnez");
        json.put("city", "Salamanca");


        // Generate request
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("PUT")
                .uri("/user")
                .header("Content-Type", "application/json")
                .header("Authorization", user.getToken().getToken())
                .bodyJson(json);

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(409);
    }



    @Test
    public void testUserDeletedOk() {
        // Create user and save
        User user = getTestUser();
        user.validateAndSave();

        // Generate request
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user.getId())
                .header("Authorization", user.getToken().getToken());

        Helpers.route(app, req);


        req = Helpers.fakeRequest()
                .method("DELETE")
                .uri("/user")
                .header("Authorization", user.getToken().getToken());

        Result r = Helpers.route(app, req);

        // Assert result
        assertThat(r.status()).isEqualTo(200);
    }



    @Test
    public void testUserResetTokenOk() {
        // Create user and save
        User user = getTestUser();
        user.validateAndSave();

        String oldToken = user.getToken().getToken();

        // Generate request
        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/user/" + user.getId())
                .header("Authorization", user.getToken().getToken());

        Helpers.route(app, req);


        req = Helpers.fakeRequest()
                .method("POST")
                .uri("/user/resetToken")
                .header("Authorization", user.getToken().getToken());


        Result r = Helpers.route(app, req);

        String newToken = r.header("Authorization").get();

        assertThat(oldToken.equals(newToken)).isEqualTo(false);
    }



    @Test
    public void testUserRetrieveUserCollectionOk() {
        // Create 2 users and save
        User user = getTestUser();
        user.validateAndSave();

        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/users/" + 0)
                .header("Authorization", user.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }



    @Test public void testUserSearchUsersOk() {
        // Create 2 users and save
        User user = getTestUser();
        user.validateAndSave();

        Http.RequestBuilder req = Helpers.fakeRequest()
                .method("GET")
                .uri("/users/search?city=Salamanca")
                .header("Authorization", user.getToken().getToken());

        Result r = Helpers.route(app, req);

        assertThat(r.status()).isEqualTo(200);
    }





    private ObjectNode getTestUserJson() {
        ObjectNode json = Json.newObject();
        json.put("dni", "70917793F");
        json.put("name", "Pablo Sánchez Egido");
        json.put("city", "Salamanca");

        return json;
    }

    private User getTestUser() {
        User user = new User();
        user.setDni("70917793F");
        user.setName("Pablo Sánchez Egido");
        user.setCity("Salamanca");

        return user;
    }
}