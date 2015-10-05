package ru.loftschool.bashclient.rest;

import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import ru.loftschool.bashclient.rest.api.GetStoriesAPI;

public class RestClient {

    private GetStoriesAPI getStoriesAPI;

    RestClient() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(RestGeneralParams.BASE_URL)
                .setClient(new OkClient(new OkHttpClient()))
                .build();

        getStoriesAPI = restAdapter.create(GetStoriesAPI.class);
    }

    public GetStoriesAPI getGetStoriesAPI() {
        return getStoriesAPI;
    }
}
