package com.interviewtest.techTask;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface APIInterface {

    @GET("/api/users?")
    Call<Model.ModelValues> doGetListResources(@Query("page") int page);
}