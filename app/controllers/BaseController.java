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

    static String getMessage(String key) {
        return Http.Context.current().messages().at(key);
    }

    User getLoggedUser() {
        return (User) Http.Context.current().args.get("logged-user");
    }

    String getSingleRecipeCacheKey(Long id) {
        return "recipe-" + id;
    }

    String getSingleRecipeResponseCacheKey(Long id, String format) {
        return "recipe-" + id + "-" + format;
    }

    String getPagedRecipeCollectionCacheKey(Integer page) {
        return "recipes-" + page;
    }

    void deleteRecipeFromCache(Long id) {
        cache.remove(getSingleRecipeCacheKey(id));
        cache.remove(getSingleRecipeResponseCacheKey(id, "json"));
        cache.remove(getSingleRecipeResponseCacheKey(id, "xml"));
    }
}
