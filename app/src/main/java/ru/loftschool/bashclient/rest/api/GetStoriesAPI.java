package ru.loftschool.bashclient.rest.api;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;
import ru.loftschool.bashclient.rest.model.StoryModel;

public interface GetStoriesAPI {

    @GET("/api/get")
    List<StoryModel> getStories(@Query("site") String site,
                                @Query("name") String name,
                                @Query("num") int num);


}
