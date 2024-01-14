package com.example.wildgurd_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    private TextInputEditText editTextFirstName, editTextLastName, editTextTelephoneNo, editTextEmail, editTextPassword, editConfirmTextPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    private static final String TAG = "Register";

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
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        editTextFirstName = findViewById(R.id.first_name);
        editTextLastName = findViewById(R.id.second_name);
        editTextTelephoneNo = findViewById(R.id.tel_no);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editConfirmTextPassword = findViewById(R.id.confirmPassword);
        buttonReg = findViewById(R.id.button_reg);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login2.class);
                startActivity(intent);
                finish();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String first_name, second_name, tel_no, email, password, confirmPassword;
                first_name = editTextFirstName.getText().toString();
                second_name = editTextLastName.getText().toString();
                tel_no = editTextTelephoneNo.getText().toString();
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();
                confirmPassword = editConfirmTextPassword.getText().toString();

                //Validate mobile number using matcher and pattern(Regular Expression)
                String mobileRegex = " @\"^(?:7|0|(?:\\+94))[0-9]{9,10}$\""; //First no can be [6.8.9] and rest 9 nos, can be any no.
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher((CharSequence) editTextTelephoneNo);

                if (TextUtils.isEmpty(first_name)){
                    Toast.makeText(Register.this, "Please Enter First Name", Toast.LENGTH_SHORT).show();
                    editTextFirstName.setError("First Name is required");
                    editTextFirstName.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(second_name)){
                    Toast.makeText(Register.this, "Please Enter Last Name", Toast.LENGTH_SHORT).show();
                    editTextLastName.setError("Last Name is required");
                    editTextLastName.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(tel_no)){
                    Toast.makeText(Register.this, "Please Enter Tel No", Toast.LENGTH_SHORT).show();
                    editTextTelephoneNo.setError("Tel No is required");
                    editTextTelephoneNo.requestFocus();
                } else if (tel_no.length() != 10) {
                    Toast.makeText(Register.this, "Please Re-enter Tel No", Toast.LENGTH_SHORT).show();
                    editTextTelephoneNo.setError("Tel no should be 10 digits");
                    editTextTelephoneNo.requestFocus();
                } else if (!mobileMatcher.find()) {
                    Toast.makeText(Register.this, "Please Re-enter Tel No", Toast.LENGTH_SHORT).show();
                    editTextTelephoneNo.setError("Tel no is not valid");
                    editTextTelephoneNo.requestFocus();
                }

                if (TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Please Enter Email", Toast.LENGTH_SHORT).show();
                    editTextEmail.setError("Email is required");
                    editTextEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Register.this, "Please Re-enter Email", Toast.LENGTH_SHORT).show();
                    editTextEmail.setError("Valid Email is required");
                    editTextEmail.requestFocus();
                }

                if (TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
                    editTextPassword.setError("Password is required");
                    editTextPassword.requestFocus();
                }else if (editTextPassword.length() < 6) {
                    Toast.makeText(Register.this, "Password should be 10 digits", Toast.LENGTH_SHORT).show();
                    editTextEmail.setError("Password too week");
                    editTextEmail.requestFocus();
                }

                if (TextUtils.isEmpty(confirmPassword)){
                    Toast.makeText(Register.this, "Please Enter Confirm Password", Toast.LENGTH_SHORT).show();
                    editConfirmTextPassword.setError("Confirm Password is required");
                    editConfirmTextPassword.requestFocus();
                }else if (!editTextPassword.equals(editConfirmTextPassword)) {
                    Toast.makeText(Register.this, "Please give same password", Toast.LENGTH_SHORT).show();
                    editTextEmail.setError("Password confirmation is required");
                    editTextEmail.requestFocus();

                    //clear the entered password.
                    editTextPassword.clearComposingText();
                    editConfirmTextPassword.clearComposingText();
                }

                        //User profile creation
                        mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()){

                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                                    //Update user's name in display
                                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(String.valueOf(editTextFirstName)).build();
                                    UserProfileChangeRequest profileChangeRequest1 = new UserProfileChangeRequest.Builder().setDisplayName(String.valueOf(editTextLastName)).build();
                                    firebaseUser.updateProfile(profileChangeRequest);
                                    firebaseUser.updateProfile(profileChangeRequest1);

                                    //Enter user data into the firebase realtime database
                                    ReadWriteUserDetails readWriteUserDetails = new ReadWriteUserDetails(editTextTelephoneNo, editTextEmail, editTextPassword);

                                    //Extracting user reference from database for "registered users"
                                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                                    referenceProfile.child(firebaseUser.getUid()).setValue(readWriteUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                //send verification email
                                                firebaseUser.sendEmailVerification();

                                                Toast.makeText(Register.this, "User registration is successfully, Please verify your email",
                                                        Toast.LENGTH_LONG).show();

                                                //open user profile after successful registration
                                                Intent intent = new Intent(Register.this, Profile.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else{
                                                Toast.makeText(Register.this, "User registration is failed, Please try again",
                                                        Toast.LENGTH_LONG).show();

                                            }
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                                } else{
                                    try{
                                        throw task.getException();
                                    }catch (FirebaseAuthWeakPasswordException e){
                                        editTextPassword.setError("Your password is too week, Kindly use a of alphabets, numbers and special characters");
                                        editTextPassword.requestFocus();
                                    }catch (FirebaseAuthInvalidCredentialsException e){
                                        editTextPassword.setError("Your email is invalid or already in-use, Kindly re-enter");
                                        editTextPassword.requestFocus();
                                    }catch (FirebaseAuthUserCollisionException e){
                                        editTextPassword.setError("User is already registered with this email, Use another email");
                                        editTextPassword.requestFocus();
                                    }catch (Exception e){
                                        Log.e(TAG, e.getMessage());
                                        Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
    }
}