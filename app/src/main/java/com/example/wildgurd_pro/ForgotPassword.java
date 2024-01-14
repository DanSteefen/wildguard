package com.example.wildgurd_pro;

import static com.example.wildgurd_pro.R.id.btn_reset_password;
import static com.example.wildgurd_pro.R.id.email;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPassword extends AppCompatActivity {

    private Button resetPasswordBtn;
    private EditText editTextResetEmail;
    private FirebaseAuth mAuth;
    private static final String TAG = "ForgotPassword";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        getSupportActionBar().setTitle("Forget Password");

        editTextResetEmail = findViewById(R.id.email);
        resetPasswordBtn = findViewById(btn_reset_password);

        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = editTextResetEmail.getText().toString();

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(ForgotPassword.this, "Please enter your registered email!", Toast.LENGTH_SHORT).show();
                    editTextResetEmail.setError("Email is required");
                    editTextResetEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        Toast.makeText(ForgotPassword.this, "Please enter valid email!", Toast.LENGTH_SHORT).show();
                        editTextResetEmail.setError("Valid email is required");
                        editTextResetEmail.requestFocus();
                } else {
                    resetPassword(email);
                }
            }
        });
    }

    private void resetPassword(String email) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ForgotPassword.this, "Please check your email box for password reset link!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ForgotPassword.this, Login2.class);

                    //Clear stack to prevent user coming back to ForgotPassword on pressing back button after login out
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e){
                        editTextResetEmail.setError("USer does not exists is no longer valid, Please register again");
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(ForgotPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}