package com.example.wildgurd_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class Profile extends AppCompatActivity {

    private TextInputEditText editTextFirstName, editTextLastName, editTextTelephoneNo, editTextEmail, editTextPassword, editConfirmTextPassword;
    private String firstName, lastName, telNo, email, password, confirmPassword;
    private ImageView imageView;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Home");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextFirstName = findViewById(R.id.first_name);
        editTextLastName = findViewById(R.id.second_name);
        editTextTelephoneNo = findViewById(R.id.tel_no);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editConfirmTextPassword = findViewById(R.id.confirmPassword);

        //Set onClickListener on ImageView to open UploadProfile
        imageView= findViewById(R.id.profileImage);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, UploadUserProfile.class);
            startActivity(intent);
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null){
            Toast.makeText(Profile.this, "Something went wrong, user;s details are not available the moment!", Toast.LENGTH_LONG).show();
        } else {
            checkIfEmailVerified(firebaseUser);
            showUserProfile(firebaseUser);
        }
    }

    //Users coming to Profile after successful registration
    private void checkIfEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    private void showAlertDialog() {
        //Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setTitle("Email not verify");
        builder.setMessage("Please verify your email now, You can't login without email verification next time");

        //Open email app if user click/taps continue button
        builder.setPositiveButton("Continue", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        //Create the AlertDialog
        AlertDialog alertDialog = builder. create();

        //Show the AlertDialog
        alertDialog.show();
    }

    private void showUserProfile(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();

        //Extracting user reference from database for "registered users"
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered users");
        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readWriteUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readWriteUserDetails != null){
                    firstName = firebaseUser.getDisplayName();
                    lastName = firebaseUser.getDisplayName();
                    telNo = firebaseUser.getDisplayName();
                    email = firebaseUser.getDisplayName();
                    password = firebaseUser.getDisplayName();
                    confirmPassword= firebaseUser.getDisplayName();

                    editTextFirstName.setText(firstName);
                    editTextLastName.setText(lastName);
                    editTextTelephoneNo.setText(telNo);
                    editTextEmail.setText(email);
                    editTextPassword.setText(password);
                    editConfirmTextPassword.setText(confirmPassword);

                    //Set user DP
                    Uri uri = firebaseUser.getPhotoUrl();

                    //ImageView and setImageUid should not be used with regular URIs, using picasso.
                    Picasso.with(Profile.this).load(uri).into(imageView);
                } else {
                    Toast.makeText(Profile.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        });
    }

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
        if (id == android.R.id.home){
            NavUtils.navigateUpFromSameTask(Profile.this);
        }
        if (id == R.id.bottomAppBar){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(Profile.this, UpdateProfile.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(Profile.this, DeleteProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_email){
            Intent intent = new Intent(Profile.this, UpdateEmail.class);
            startActivity(intent);
            finish();
        }  else if (id == R.id.menu_create_compliance) {
            Intent intent = new Intent(Profile.this, Compliance.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_setting) {
            Toast.makeText(Profile.this, "menu_setting", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Profile.this, Setting.class);
            startActivity(intent);
        }else if (id == R.id.menu_home) {
            Intent intent = new Intent(Profile.this, MainActivity.class);
            startActivity(intent);
        }else if (id == R.id.menu_log_out) {
            mAuth.signOut();
            Toast.makeText(Profile.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Profile.this, Login2.class);

            //Clear stack to prevent user coming back to MainActivity on pressing back button after login out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(Profile.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}