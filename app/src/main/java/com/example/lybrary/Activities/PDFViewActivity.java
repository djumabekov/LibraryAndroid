package com.example.lybrary.Activities;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;


import com.example.lybrary.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.pdfview.PDFView;

import java.io.File;

public class PDFViewActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);

        init();
        Intent intent = getIntent();
        String book_name = intent.getStringExtra("book_name"); //извлекаем название книги
        // загружаем PDF из каталога по названию книги
        pdfView.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),book_name+".pdf")).show();
    }

    private void init(){
        toolbar = findViewById(R.id.toolBar_pdfView);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pdfView = findViewById(R.id.pdfView);
    }

}