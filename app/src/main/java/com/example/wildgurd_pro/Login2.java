package com.example.wildgurd_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;


public class Login2 extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    TextView forgotPassword;
    private static final String TAG = "Login2";

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        mAuth = FirebaseAuth.getInstance();

        //Reset the password
        Button buttonForgetPassword = findViewById(R.id.btn_forget_password);
        buttonForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Login2.this, "You can reset your password here", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Login2.this, ForgotPassword.class));
            }
        });

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.btnGoLogin);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String first_name, second_name, tel_no, email, password;
                first_name = String.valueOf(editTextEmail.getText());
                second_name = String.valueOf(editTextPassword.getText());
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(first_name)) {
                    Toast.makeText(Login2.this, "Enter Email plz", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(second_name)) {
                    Toast.makeText(Login2.this, "Enter Password plz", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {

                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                                    //Check if email is verified before user can access their profile
                                    if (firebaseUser.isEmailVerified()){
                                        Toast.makeText(Login2.this, "You are logged in now ", Toast.LENGTH_SHORT).show();

                                        //Open user profile
                                    } else {
                                        firebaseUser.sendEmailVerification();
                                        mAuth.signOut();
                                        showAlertDialogBox();
                                    }

                                    Toast.makeText(Login2.this, "Login Successfully.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    try {
                                        throw task.getException();
                                    }catch (FirebaseAuthInvalidUserException e){
                                        editTextEmail.setError("User does not existing or is no valid longer, Please register again");
                                        editTextEmail.requestFocus();
                                    }catch (FirebaseAuthInvalidCredentialsException e){
                                        editTextEmail.setError("Invalid credential, Kindly check and re-enter");
                                        editTextEmail.requestFocus();
                                    }catch (Exception e){
                                        Log.e(TAG, e.getMessage());
                                        Toast.makeText(Login2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });
    }

    private void showAlertDialogBox() {
        //Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(Login2.this);
        builder.setTitle("Email not verify");
        builder.setMessage("Please verify your email now, You can't login without email verification");

        //Open email app if user click/taps continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        //Show the AlertDialog
        alertDialog.show();
    }

    //Check if user is already logged in. In such case, straightway take the user to user's profile

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            Toast.makeText(Login2.this, "Already logged in!", Toast.LENGTH_SHORT).show();

            //Start the profile
            startActivity(new Intent(Login2.this, Profile.class));
            finish();
        } else {
            Toast.makeText(Login2.this, "You can login now!", Toast.LENGTH_SHORT).show();
        }
    }
}