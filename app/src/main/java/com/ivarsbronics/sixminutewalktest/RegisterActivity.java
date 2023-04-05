package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private static final String dbInstance = "https://sixminutewalktest-ff14a-default-rtdb.europe-west1.firebasedatabase.app";

    private FirebaseAuth mAuth;
    private EditText email, reemail, password, repassword;
    private Button btnRegister, btnTermsApproval;
    private TextView txtLogin, txtTestVersionTerms;

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
        txtTestVersionTerms = findViewById(R.id.txtTestVersionTerms);
        btnTermsApproval = findViewById(R.id.btnTermsApproval);

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

        btnTermsApproval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agreeToTermsAndProceed();
            }
        });
    }

    private void agreeToTermsAndProceed() {
        txtTestVersionTerms.setVisibility(View.GONE);
        btnTermsApproval.setVisibility(View.GONE);
        email.setVisibility(View.VISIBLE);
        reemail.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);
        repassword.setVisibility(View.VISIBLE);
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
                reemail.setError("E-mail fields must match");
                reemail.requestFocus();
            }
            else{
                if (!userPassword.equals(userRePassword)){
                    Toast.makeText(RegisterActivity.this, "Password fields don't match!", Toast.LENGTH_LONG).show();
                    repassword.setError("Password fields must match");
                    repassword.requestFocus();
                }
                else{
                    if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                        Toast.makeText(RegisterActivity.this, "E-mail address does not match e-mail format!", Toast.LENGTH_LONG).show();
                        email.setError("E-mail must match e-mail format!");
                        email.requestFocus();
                    }
                    else {
                        mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();

                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    // if it is decided to use real name or username it can be updated for display purposes - also in this case it is not needed to save it separately in realtime db
                                    //UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName("username").build();
                                    //currentUser.updateProfile(profileChangeRequest);

                                    UserInfo userInfo = new UserInfo(null, null, null, null);
                                    //UserInfo userInfo = new UserInfo(age, height, gender, weight);

                                    // get database user reference
                                    DatabaseReference usersReference = FirebaseDatabase.getInstance(dbInstance).getReference("Users");
                                    usersReference.child(currentUser.getUid()).setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // send verification e-mail
                                                //currentUser.sendEmailVerification();
                                                //Toast.makeText(RegisterActivity.this, "Registration Successful! Please Verify Your e-mail!" , Toast.LENGTH_LONG).show();

                                                //without verification e-mail sending
                                                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                                // makes sure that getting to previous activity is not possible, i.e., back button does not return to RegisterActivity
                                                // makes sure that only one HomeActivity instance is available - reusing existing instance or creating new
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                            else{
                                                Log.d(TAG, task.getException().getMessage());
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Registration Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        }
    }
}