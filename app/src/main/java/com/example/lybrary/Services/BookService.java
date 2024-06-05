package com.example.lybrary.Services;


import com.example.lybrary.Utils.Book;
import com.example.lybrary.Utils.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface BookService {

    @GET("books")  // получаем все книги
    Call<List<Book>> getBooks();

    @POST("books/{userId}/{bookId}")  // добавляем книгу юзеру
    Call<Book> addBookForUser(@Path("userId") int userId,
                              @Path("bookId") int bookId);

    @POST("books")  // добавляем книгу
    Call<Book> addBook(@Body Book book);

}
