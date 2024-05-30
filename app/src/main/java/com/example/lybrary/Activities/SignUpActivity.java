package com.example.lybrary.Activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lybrary.Apis.Api;
import com.example.lybrary.R;
import com.example.lybrary.Services.UserService;
import com.example.lybrary.Utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private EditText passwordSignUp, passwordSignUp_1, emailSignUp, name, editText_last_name;
    private TextView loginText;
    private Button signUpbutton;
    private ImageView ImageViewSignUp;
    private ProgressBar progressBarSignUp;
    private MaterialToolbar toolbar;
    FirebaseAuth auth;
    DatabaseReference reference;
    FirebaseUser user;
    UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
        signUp();
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LogInActivity.class));
            }
        });
    }

    private void init(){
        passwordSignUp = findViewById(R.id.passwordSignUp);
        passwordSignUp_1 = findViewById(R.id.passwordSignUp_1);
        emailSignUp = findViewById(R.id.emailSignUp);
        loginText = findViewById(R.id.loginText);
        signUpbutton = findViewById(R.id.signUpbutton);
        ImageViewSignUp = findViewById(R.id.ImageViewSignUp);
        progressBarSignUp = findViewById(R.id.progressBarSignUp);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        user = auth.getCurrentUser();
        name = findViewById(R.id.name);
        editText_last_name = findViewById(R.id.editText_last_name);
        toolbar = findViewById(R.id.toolBar_sign_up);
        setSupportActionBar(toolbar);
        toolbar.setTitle("РЕГИСТРАЦИЯ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // регистрация firebase через почту и пароль
    private void signUp(){
        signUpbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String namee = name.getText().toString().trim();
                String last_name = editText_last_name.getText().toString().trim();
                String email = emailSignUp.getText().toString().trim();
                String password = passwordSignUp.getText().toString().trim();
                String password2 = passwordSignUp_1.getText().toString().trim();


                if(TextUtils.isEmpty(namee)){
                    name.setError("Введите имя");
                    return;
                }
                if(TextUtils.isEmpty(last_name)){
                    name.setError("Введите фамилию");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    emailSignUp.setError("Введите эл.почту");
                    return;

                }
                if (TextUtils.isEmpty(password)) {
                    passwordSignUp.setError("Введите пароль");
                    return;
                }
                if (password.length() < 8) {
                    passwordSignUp_1.setError("Пароль должен быть не менее 8 символов");
                    return;
                }
                if (password.equals(password2) == false) {
                    Toast.makeText(SignUpActivity.this, "Пароль должны совпадать!", Toast.LENGTH_SHORT).show();
                    return;
                }


                progressBarSignUp.setVisibility(View.VISIBLE);

                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            setUserInfoIntoDb(namee,last_name, email);
                            //TODO BURASI

                            User user = new User(namee, last_name, email); // create user
                            System.out.println(user);
                            postAddUserToApi(user);


                            Toast.makeText(SignUpActivity.this, "Регистрация прошла успешно!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LogInActivity.class));

                        } else {
                            Toast.makeText(SignUpActivity.this, "Ошибка!" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            progressBarSignUp.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private void postAddUserToApi(User user) {

        userService = Api.getUserService();
        Call<User> userCall = userService.addUser(user);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                System.out.println("Ошибка");
            }
        });
    }

    // добавляем юзера в firebase
    private void setUserInfoIntoDb(String namee, String last_name, String email){
        reference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                DatabaseReference databaseReference = reference.child("Users").child(String.valueOf(count+1));
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, String> map = new HashMap<>();
                        map.put("name",namee);
                        map.put("last name",last_name);
                        map.put("email",email);
                        databaseReference.setValue(map);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}