package controllers;

import models.Token;
import models.User;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

import java.util.Optional;

public class Authorization extends Security.Authenticator {

    @Override
    public String getUsername(Http.Context ctx) {
        Optional<String> authHeader = ctx.request().header("Authorization");

        if (authHeader.isPresent()) {
            String auth = authHeader.get();
            Token token = Token.findByToken(auth);
            if (token == null) {
                return null;
            }

            User user = token.getUser();
            ctx.args.put("logged-user", user);

            return user.getId().toString();
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return Results.unauthorized();
    }
}
