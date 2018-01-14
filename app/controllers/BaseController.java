package controllers;

import models.User;
import play.cache.SyncCacheApi;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;

import javax.inject.Inject;

abstract class BaseController extends Controller {

    @Inject
    FormFactory formFactory;

    @Inject
    SyncCacheApi cache;

    String getMessage(String key) {
        return Http.Context.current().messages().at(key);
    }

    User getLoggedUser() {
        return (User) Http.Context.current().args.get("logged-user");
    }
}
