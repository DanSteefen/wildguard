package com.example.wildgurd_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

public class UpdateEmail extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private TextView textViewAuthenticate;
    private String userOldEmail, userNewEmail, userPassword;
    private Button btnUpdateEmail;
    private EditText editTextNewEmail, editTextPassword;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        getSupportActionBar().setTitle("Update Email");

        editTextPassword = findViewById(R.id.authenticate_password);
        editTextNewEmail = findViewById(R.id.new_email);
        textViewAuthenticate = findViewById(R.id.authenticate_email);
        btnUpdateEmail = findViewById(R.id.btn_verify_authenticate);

        btnUpdateEmail.setEnabled(false); // Make btn disable in the beginning until the user isn't authenticate.
        editTextNewEmail.setEnabled(false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        //Set old email ID on TextView
        userOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.authenticate_email);
        textViewOldEmail.setText(userOldEmail);

        if (firebaseUser.equals("")){
            Toast.makeText(UpdateEmail.this, "Something went wrong! User's details not available", Toast.LENGTH_LONG).show();
        } else {
            reAuthenticate(firebaseUser);
        }
    }

    //ReAuthenticate/Verify User before updating email
    private void reAuthenticate(FirebaseUser firebaseUser) {
        Button btnVerifyUser = findViewById(R.id.btn_verify_authenticate);
        btnVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtain password for authentication
                userPassword = editTextPassword.getText().toString();

                if (TextUtils.isEmpty(userPassword)){
                    Toast.makeText(UpdateEmail.this, "Password is needed to continue", Toast.LENGTH_SHORT).show();
                    editTextPassword.setError("Please enter your password for authentication");
                    editTextPassword.requestFocus();
                } else {
                    AuthCredential credential = EmailAuthProvider.getCredential(userOldEmail, userPassword);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                Toast.makeText(UpdateEmail.this, "Password has been verified." + "You can update email now",
                                        Toast.LENGTH_LONG).show();

                                //Set TextView to show that user is authenticated
                                textViewAuthenticate.setText("You are authenticated. Now you can update your email.");

                                //Disable EditText foe password, byn to verify user and enable editText for new email and update email btn
                                editTextNewEmail.setEnabled(true);
                                editTextPassword.setEnabled(false);
                                btnVerifyUser.setEnabled(false);
                                btnUpdateEmail.setEnabled(true);

                                //Change color of Update Email btn
                                btnUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmail.this, R.color.lavender));

                                btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        userNewEmail = editTextNewEmail.getText().toString();
                                        if (TextUtils.isEmpty(userNewEmail)){
                                            Toast.makeText(UpdateEmail.this, "New Email is required", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please enter new email");
                                            editTextNewEmail.requestFocus();
                                        } else if (!Patterns.EMAIL_ADDRESS.matcher(userNewEmail).matches()) {
                                            Toast.makeText(UpdateEmail.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please provide valid email");
                                            editTextNewEmail.requestFocus();
                                        } else if (userOldEmail.matches(userNewEmail)) {
                                            Toast.makeText(UpdateEmail.this, "New Email can't be same as old email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please enter new email");
                                            editTextNewEmail.requestFocus();
                                        } else {
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            }else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(UpdateEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(userNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()){

                    //Verify email
                    firebaseUser.sendEmailVerification();

                    Toast.makeText(UpdateEmail.this, "Email has been updated. Please verify your new email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmail.this, Profile.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e){
                        Toast.makeText(UpdateEmail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //create actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate menu items
        getMenuInflater().inflate(R.menu.bottom_nav_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //When any menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.bottomAppBar){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(UpdateEmail.this, UpdateProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UpdateEmail.this, DeleteProfile.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(UpdateEmail.this, UpdateEmail.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_change_pasword){
            Intent intent = new Intent(UpdateEmail.this, ChangePassword.class);
            startActivity(intent);
            finish();
        }  else if (id == R.id.menu_create_compliance) {
            Intent intent = new Intent(UpdateEmail.this, Compliance.class);
            startActivity(intent);
        }else if (id == R.id.menu_setting) {
            Toast.makeText(UpdateEmail.this, "menu_setting", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateEmail.this, Setting.class);
            startActivity(intent);
        }else if (id == R.id.menu_home) {
            Intent intent = new Intent(UpdateEmail.this, MainActivity.class);
            startActivity(intent);
        }else if (id == R.id.menu_log_out) {
            auth.signOut();
            Toast.makeText(UpdateEmail.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateEmail.this, Login2.class);

            //Clear stack to prevent user coming back to MainActivity on pressing back button after login out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UpdateEmail.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}