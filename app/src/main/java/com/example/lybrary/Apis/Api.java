package com.example.lybrary.Apis;


import com.example.lybrary.Services.BookService;
import com.example.lybrary.Services.UserService;

public class Api {

    static final String URL = "http://10.40.105.241:8084/api/";

    public static UserService getUserService() {
        return Client.getClient(URL).create(UserService.class);
    }

    public static BookService getBookService() {

        return Client.getClient(URL).create(BookService.class);

    }
}