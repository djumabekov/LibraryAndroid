package com.example.lybrary.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.lybrary.Apadters.AvailableBooksAdapter;
import com.example.lybrary.Apis.Api;
import com.example.lybrary.R;
import com.example.lybrary.Services.BookService;
import com.example.lybrary.Utils.Book;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableBooksFragment extends Fragment {
    BookService bookService;

    private List<Book> bookList;
    private RecyclerView recyclerView_available_books;
    AvailableBooksAdapter adapter;
    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_available_books, container, false);

        init();
        getBooks();


        return view;
    }

    private void init() {
        bookList = new ArrayList<>();
        recyclerView_available_books = view.findViewById(R.id.recyclerView_available_books);
        RecyclerView.LayoutManager mng = new GridLayoutManager(getContext(), 1);
        recyclerView_available_books.setLayoutManager(mng);
        adapter = new AvailableBooksAdapter(bookList, getActivity(), getContext());
        recyclerView_available_books.setAdapter(adapter);

    }

    //  получаем книги с БД
    private void getBooks() {

        bookService = Api.getBookService();
        Call<List<Book>> call = bookService.getBooks();

        call.enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bookList = response.body();
                    Log.d("getBooks", "Book list size: " + bookList.size());

                    // Выводим каждый элемент списка в лог
                    for (int i = 0; i < bookList.size(); i++) {
                        Book book = bookList.get(i);
                        Log.d("getBooks", "Book " + i + ": " + book.toString());
                    }

                    adapter = new AvailableBooksAdapter(bookList, getActivity(), getContext());
                    recyclerView_available_books.setAdapter(adapter);
                } else {
                    // Логируем информацию, если ответ не успешен
                    Log.d("getBooks", "Response unsuccessful or body is null");
                    Log.d("getBooks", "Response code: " + response.code());
                    Log.d("getBooks", "Response message: " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.d("getBooks", "Error body: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Log.d("getBooks", "Error: " + t.getMessage());
            }
        });
    }


    // тулбар
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });
    }

}