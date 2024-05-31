package com.example.lybrary.Apadters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lybrary.Apis.Api;
import com.example.lybrary.R;
import com.example.lybrary.Services.BookService;
import com.example.lybrary.Utils.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableBooksAdapter extends RecyclerView.Adapter<AvailableBooksAdapter.ViewHolder> implements Filterable{

    private List<Book> bookList; //список доступных книг
    private List<Book> bookListFull;//полный набор книг для фильтрации
    Activity activity;
    Context context;
    DatabaseReference databaseReference; //Firebase БД
    FirebaseUser firebaseUser; //Firebase пользователя
    BookService bookService;

    public AvailableBooksAdapter(List<Book> bookList, Activity activity, Context context) {
        this.bookList = bookList;
        this.activity = activity;
        this.context = context;
        bookListFull = new ArrayList<>(bookList);
    }


    // создаем представление для RecyclerView
    @NonNull
    @Override
    public AvailableBooksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.available_books_recyclerview_layout, parent, false);

        return new ViewHolder(view);
    }

    //связываем данные из bookList с ViewHolder
    @Override
    public void onBindViewHolder(@NonNull AvailableBooksAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        //загрузка изображения книги
        Picasso.get().load(bookList.get(position).getPhotoPath()).into(holder.book_image_available);
        holder.text_name_view.setText(bookList.get(position).getBookName());
        holder.text_page_view.setText(String.valueOf(bookList.get(position).getPages()));
        holder.text_title_view.setText(bookList.get(position).getTitle());

        //событие на нажатие кнопки загрузки книги
        holder.downloadbtn.setOnClickListener(view -> {
            downloadBook(bookList.get(position).getLink(), bookList.get(position).getBookName()+".pdf");
            getCurrentUserId(bookList.get(position).getId());
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    //инициализируем элементы представления, используемые для отображения книги.
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView book_name_available, text_name_view, text_page_view, text_title_view;
        CircleImageView book_image_available;
        CardView cardview_available_books;
        Button downloadbtn;

        ViewHolder(View itemView) {
            super(itemView);

            book_name_available = itemView.findViewById(R.id.book_name_available);
            cardview_available_books = itemView.findViewById(R.id.cardview_available_books);
            book_image_available = itemView.findViewById(R.id.book_image_available);
            downloadbtn = itemView.findViewById(R.id.download_btn);
            text_name_view = itemView.findViewById(R.id.text_name_view);
            text_name_view.setSelected(true);
            text_page_view = itemView.findViewById(R.id.text_page_view);
            text_title_view = itemView.findViewById(R.id.text_title_view);

        }
    }

    //загружаем книгу
    private void downloadBook(String url, String title) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(title);
        request.setDescription("Книга скачивается...");
        String cookie = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("cookie", cookie);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);

        //используем DownloadManager для загрузки
        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

        Toast.makeText(context, "Загрузка начата", Toast.LENGTH_SHORT).show();

    }

    //получаем текущего юзера
    private void getCurrentUserId(int bookId) {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // получаем юзера по ИД с firebase
        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userId;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("email").getValue().toString().equals(firebaseUser.getEmail())) {
                        userId = dataSnapshot.getKey();
                        addBookForUser(Integer.parseInt(userId),bookId);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //добавляем книгу для юзера
    private void addBookForUser(int userId, int bookId) {
        bookService = Api.getBookService();
        Call<Book> call = bookService.addBookForUser(userId,bookId);
        call.enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {

            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {

            }
        });

    }

    // фильтрация списка
    @Override
    public Filter getFilter() {
        return bookFilter;
    }

    private Filter bookFilter = new Filter() {
        //осуществляем фильтрацию на основе веденного текста
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Book> bookListFiltered = new ArrayList<>();

            if (charSequence == null || charSequence.length() == 0) {
                bookListFiltered.addAll(bookListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase(Locale.getDefault()).trim();
                for (Book book : bookListFull) {
                    if (book.getBookName().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                        bookListFiltered.add(book);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = bookListFiltered;
            return filterResults;
        }

        //обновляем bookList и уведомляем адаптер об изменениях
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            bookList.clear();
            bookList.addAll((List<Book>) filterResults.values);
            notifyDataSetChanged();
        }
    };


}