package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email, reemail, password, repassword;
    private Button btnRegister;
    private TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.etxtEMail);
        reemail = findViewById(R.id.etxtReEMail);
        password = findViewById(R.id.etxtPassword);
        repassword = findViewById(R.id.etxtRePassword);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogIn);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    private void register() {
        String userEmail = email.getText().toString().trim();
        String userReEmail = reemail.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String userRePassword = repassword.getText().toString().trim();

        if(userEmail.equals("")||userPassword.equals("")||userReEmail.equals("")||userRePassword.equals("")) {
            Toast.makeText(RegisterActivity.this, "E-mail and Password fields are mandatory", Toast.LENGTH_LONG).show();
        }
        else{
            if (!userEmail.equals(userReEmail)){
                Toast.makeText(RegisterActivity.this, "E-mail fields don't match!", Toast.LENGTH_LONG).show();
            }
            else{
                if (!userPassword.equals(userRePassword)){
                    Toast.makeText(RegisterActivity.this, "Password fields don't match!", Toast.LENGTH_LONG).show();
                }
                else{
                    mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            }
                            else{
                                Toast.makeText(RegisterActivity.this, "Registration Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        }
    }
}