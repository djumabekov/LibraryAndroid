package com.example.lybrary.Activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.lybrary.Apadters.CustomViewPager;
import com.example.lybrary.Fragment.AvailableBooksFragment;
import com.example.lybrary.Fragment.DowloadedBooksFragment;
import com.example.lybrary.Utils.Book;
import com.example.lybrary.Utils.User;
import com.example.lybrary.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int STORAGE_PERMISSION_CODE = 1; //код запроса разрешения на чтение и запись
    private List<User> listUser = new ArrayList<>(); //список юзеров
    private List<Book> listBook = new ArrayList<>(); //список книг
    private CustomViewPager customViewPager; //адаптер для viewPager
    private TabLayout tabLayout; //вкладки
    private ViewPager viewPager; //переключение между фрагментами
    private MaterialToolbar toolbar;
    private FirebaseAuth firebaseAuth; //аутентификация Firebase


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        replaceFragments(new AvailableBooksFragment());
        setFragments();
        runTimePermissions();
    }

    private void init(){
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.view_pager);
        firebaseAuth = FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
    }

    // переключение между фрагментами
    public void replaceFragments(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_main,fragment);
        fragmentTransaction.commit();
    }

    // установка фрагмента во вью
    private void setFragments(){
        customViewPager = new CustomViewPager(getSupportFragmentManager());
        customViewPager.addFragment(new AvailableBooksFragment(),"Доступные книги");
        customViewPager.addFragment(new DowloadedBooksFragment(),"Скачанные книги");

        viewPager.setAdapter(customViewPager);

        tabLayout.setupWithViewPager(viewPager);
    }

    // установка тулбара
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar, menu);

        // находим элемент меню "добавить книгу"
        MenuItem addBookMenuItem = menu.findItem(R.id.add_book);
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // проверяем текущего пользователя
        if (currentUser != null && currentUser.getEmail().equals("admin@mail.ru")) {
            // если админ отображаем "добавить книгу"
            addBookMenuItem.setVisible(true);
        } else {
            // иначе скрываем "добавить книгу"
            addBookMenuItem.setVisible(false);
        }
        return true;
    }

    // обработка нажатия
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // действие по выбору опций тулбара
        switch (item.getItemId()) {
            case R.id.search:
                Toast.makeText(getApplicationContext(),"ПОИСК",Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(getApplicationContext(), PDFViewActivity.class));
                return true;

            case R.id.log_out:
                firebaseAuth.signOut();
                startActivity(new Intent(MainActivity.this, LogInActivity.class));
                return true;

            case R.id.add_book:
                startActivity(new Intent(MainActivity.this, AddBookActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // проверка разрешений для чтения и записи в сторадж
    private void runTimePermissions(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(),"пользователь уже авторизован",Toast.LENGTH_LONG).show();
        }else{
            requestStoragePermission();
        }
    }

    //обработка результатов запроса разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),"АВТОРИЗАЦИЯ ПРОШЛА УСПЕШНО",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(),"ОШИБКА АВТОРИЗАЦИИ",Toast.LENGTH_LONG).show();

            }
        }

    }

    // заправшиваем разрешение на чтение и запись в каталог
    private void requestStoragePermission(){

        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                && ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ){
            new AlertDialog.Builder(this).setTitle("Необходимы разрешения")
                    .setMessage("Необходимы разрешения для чтения pdf с хранилища")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                    {Manifest.permission.READ_EXTERNAL_STORAGE
                                            , Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);

                        }
                    })
                    .setNegativeButton("отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.READ_EXTERNAL_STORAGE
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }
}