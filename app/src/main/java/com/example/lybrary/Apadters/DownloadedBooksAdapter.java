package com.example.lybrary.Apadters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lybrary.Activities.PDFViewActivity;
import com.example.lybrary.R;
import com.example.lybrary.Utils.Book;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
public class DownloadedBooksAdapter extends RecyclerView.Adapter<DownloadedBooksAdapter.ViewHolder> implements Filterable {

    private List<Book> bookList; //список доступных книг
    private List<Book> bookListFull; //полный набор книг для фильтрации
    Activity activity;
    Context context;

    public DownloadedBooksAdapter(List<Book> bookList, Activity activity, Context context) {
        this.bookList = bookList;
        this.activity = activity;
        this.context = context;
        bookListFull = new ArrayList<>(bookList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.downloaded_books_recyclerview_layout,parent,false);
        return new ViewHolder(view);
    }

    //связываем данные из bookList во ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //загрузка изображения книги
        Picasso.get().load(bookList.get(position).getPhotoPath()).into(holder.book_image_downloaded);
        holder.book_name_downloaded.setText(bookList.get(position).getBookName());

        holder.displayBtn.setOnClickListener(view -> {
            displayBook(bookList.get(position).getBookName());
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    //фильтрация списка
    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Book> bookListFiltered = new ArrayList<>();

            // если юзер не ввел текст, показываем все
            if(charSequence == null || charSequence.length() == 0){
                bookListFiltered.addAll(bookListFull);
            }else{
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(Book book : bookListFull){
                    if(book.getBookName().toLowerCase().contains(filterPattern)){
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

    //инициализируем элементы представления, используемые для отображения книги.
    public class ViewHolder extends  RecyclerView.ViewHolder{

        TextView book_name_downloaded;
        CircleImageView book_image_downloaded;
        CardView cardview_downloaded_books;
        Button displayBtn;

        ViewHolder(View itemView){
            super(itemView);

            book_name_downloaded = itemView.findViewById(R.id.book_name_downloaded);
            cardview_downloaded_books = itemView.findViewById(R.id.cardview_downloaded_books);
            book_image_downloaded = itemView.findViewById(R.id.book_image_downloaded);
            displayBtn = itemView.findViewById(R.id.display_btn);

        }
    }

    // запускаем PDFViewActivity и передаем имя книги
    private void displayBook(String book_name){
        Intent intent = new Intent(context, PDFViewActivity.class);
        intent.putExtra("book_name",book_name);
        activity.startActivity(intent);
    }
}