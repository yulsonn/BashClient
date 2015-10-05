package ru.loftschool.bashclient.rest;

import java.util.List;

import ru.loftschool.bashclient.rest.model.StoryModel;

public class RestService {

    RestClient restClient;

    public RestService() {
        restClient = new RestClient();
    }

    public List<StoryModel> getStories(String site, String name, int num) {
        return restClient.getGetStoriesAPI().getStories(site, name, num);
    }
}
