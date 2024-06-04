package com.example.lybrary.Services;


import  com.example.lybrary.Utils.Book;
import com.example.lybrary.Utils.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserService {

    @GET("users/{userId}")  // получаем юзера
    Call<User> getUser(@Path("userId") int userId);

    @POST("users") // регистрируем юзера
    Call<User> addUser(@Body User user);

    @GET("users/{userId}/books")  // получаем книги юзера
    Call<List<Book>> getAllBooksForUser(@Path("userId") int userId);

    @DELETE("users/{userId}/{bookId}")
    Call<String> deleteBookForUser(@Path("userId") int userId,
                                   @Path("bookId") int bookId);
}
