package com.example.lybrary.Activities;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lybrary.Apis.Api;
import com.example.lybrary.R;
import com.example.lybrary.Services.BookService;
import com.example.lybrary.Utils.Book;
import com.example.lybrary.Utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddBookActivity extends AppCompatActivity {

    private static final int PICK_PDF_FILE_REQUEST = 1;
    private static final int PICK_JPG_FILE_REQUEST = 2;
    private Uri filePdfUri;
    private Uri fileJpgUri;
    private StorageReference storageReference;
    private EditText _bookName;
    private EditText _link;
    private EditText _pages;
    private EditText _photoPath;
    private EditText _title;
    private Button addBookButton; //кнопка добавления книги
    private MaterialToolbar toolbar; //заголовок и кнопка назад
    private ProgressBar progressBarAddBook; // прогрессбар добавления книги
    BookService bookService; //апи
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        init();
        addBook();
        selectBook();
        selectImg();
    }

    private void init(){
        storageReference = FirebaseStorage.getInstance().getReference();
        _bookName = findViewById(R.id.book_name);
        _link = findViewById(R.id.link);
        _pages = findViewById(R.id.pages);
        _photoPath = findViewById(R.id.photo_path);
        _title = findViewById(R.id.title);
        addBookButton = findViewById(R.id.add_book_button);
        progressBarAddBook = findViewById(R.id.progressBarAddBook);
        toolbar = findViewById(R.id.toolBar_add_book);
        setSupportActionBar(toolbar);
        toolbar.setTitle("ДОБАВИТЬ КНИГУ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void selectBook(){
        _link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Выберите файл"), PICK_PDF_FILE_REQUEST);
            }
        });
    }

    private void selectImg(){
        _photoPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Выберите файл"), PICK_JPG_FILE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_PDF_FILE_REQUEST) {
                // Обработка выбора PDF файла
                if (requestCode == PICK_PDF_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                    filePdfUri = data.getData();
                    _link.setText(getFileName(filePdfUri)); // Отобразить имя выбранного PDF файла
                }
            } else if (requestCode == PICK_JPG_FILE_REQUEST) {
                // Обработка выбора JPG файла
                if (requestCode == PICK_JPG_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                    fileJpgUri = data.getData();
                    _photoPath.setText(getFileName(fileJpgUri)); // Отобразить имя выбранного JPG файла
                }
            }
        }
    }



    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void addBook() {
        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bookName = _bookName.getText().toString().trim();
                String link = _link.getText().toString().trim();
                String pages = _pages.getText().toString().trim();
                String photoPath = _photoPath.getText().toString().trim();
                String title = _title.getText().toString().trim();

                if (TextUtils.isEmpty(bookName)) {
                    _bookName.setError("Введите название книги");
                    return;
                }
                if (TextUtils.isEmpty(link)) {
                    _link.setError("Введите файл pdf");
                    return;
                }
                if (TextUtils.isEmpty(pages)) {
                    _pages.setError("Введите кол-во страниц");
                    return;
                }
                if (TextUtils.isEmpty(photoPath)) {
                    _photoPath.setError("Введите изображение");
                    return;
                }
                if (TextUtils.isEmpty(title)) {
                    _title.setError("Введите заглавие");
                    return;
                }
                // отображаем прогрессбар
                progressBarAddBook.setVisibility(View.VISIBLE);

                if (filePdfUri != null && fileJpgUri != null) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        StorageReference filePdfReference = storageReference.child("pdf/" + System.currentTimeMillis() + "." + getFileExtension(filePdfUri));
                        StorageReference fileJpgReference = storageReference.child("photo/" + System.currentTimeMillis() + "." + getFileExtension(fileJpgUri));

                        filePdfReference.putFile(filePdfUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    // Получаем ссылку на загруженный PDF файл
                                    filePdfReference.getDownloadUrl().addOnSuccessListener(pdfUri -> {
                                        // Загружаем JPG файл
                                        fileJpgReference.putFile(fileJpgUri)
                                                .addOnSuccessListener(taskSnapshot1 -> {
                                                    // Получаем ссылку на загруженный JPG файл
                                                    fileJpgReference.getDownloadUrl().addOnSuccessListener(jpgUri -> {
                                                        String pdfDownloadUrl = pdfUri.toString();
                                                        String jpgDownloadUrl = jpgUri.toString();
                                                        int pagesInt = Integer.parseInt(pages);
                                                        Book book = new Book(jpgDownloadUrl, bookName, pdfDownloadUrl, title, pagesInt);
                                                        System.out.println(book);
                                                        postAddBookToApi(book);

                                                        Toast.makeText(AddBookActivity.this, "Книга успешно добавлена!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                    });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(AddBookActivity.this, "Ошибка загрузки JPG файла: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddBookActivity.this, "Ошибка загрузки PDF файла: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(AddBookActivity.this, "Файлы не выбраны", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //метод добавления книги в БД
    private void postAddBookToApi(Book book) {

        bookService = Api.getBookService();
        Call<Book> bookCall = bookService.addBook(book);
        bookCall.enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {

            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                System.out.println("Ошибка");
            }
        });
    }
}