package com.example.wildgurd_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfile extends AppCompatActivity {

    private TextInputEditText editTextUpdateFirstName, editTextUpdateLastName, editTextUpdateTelephoneNo, editTextUpdateEmail, editTextUpdatePassword, editConfirmTextUpdatePassword;
    private String firstName, lastName, telNo, email, password, confirmPassword;
    private ImageView imageView;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("Update Profile");

        editTextUpdateFirstName = findViewById(R.id.update_first_name);
        editTextUpdateLastName = findViewById(R.id.update_second_name);
        editTextUpdateTelephoneNo = findViewById(R.id.update_tel_no);
        editTextUpdateEmail = findViewById(R.id.update_email);
        editTextUpdatePassword = findViewById(R.id.update_password);
        editConfirmTextUpdatePassword = findViewById(R.id.update_confirmPassword);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        //Show profile data
        showProfile(firebaseUser);

        //Upload profile pic
        Button btnUploadProfilePic = findViewById(R.id.btn_update_profile_pic);
        btnUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfile.this, UploadUserProfile.class);
                startActivity(intent);
                finish();

            }
        });

        //Update email
        Button btnUpdateEmail = findViewById(R.id.btn_update_email);
        btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfile.this, UpdateEmail.class);
                startActivity(intent);
                finish();

            }
        });

        //Update Profile
        Button btnUpdateProfile = findViewById(R.id.btn_update_profile);
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });
    }

    //Update Profile
    private void updateProfile(FirebaseUser firebaseUser) {

        //Validate mobile number using matcher and pattern(Regular Expression)
        String mobileRegex = " @\"^(?:7|0|(?:\\+94))[0-9]{9,10}$\""; //First no can be [6.8.9] and rest 9 nos, can be any no.
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher((CharSequence) editTextUpdateTelephoneNo);

        if (TextUtils.isEmpty(firstName)){
            Toast.makeText(UpdateProfile.this, "Please Enter First Name", Toast.LENGTH_SHORT).show();
            editTextUpdateFirstName.setError("First Name is required");
            editTextUpdateFirstName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(lastName)){
            Toast.makeText(UpdateProfile.this, "Please Enter Last Name", Toast.LENGTH_SHORT).show();
            editTextUpdateLastName.setError("Last Name is required");
            editTextUpdateLastName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(telNo)){
            Toast.makeText(UpdateProfile.this, "Please Enter Tel No", Toast.LENGTH_SHORT).show();
            editTextUpdateTelephoneNo.setError("Tel No is required");
            editTextUpdateTelephoneNo.requestFocus();
        } else if (telNo.length() != 10) {
            Toast.makeText(UpdateProfile.this, "Please Re-enter Tel No", Toast.LENGTH_SHORT).show();
            editTextUpdateTelephoneNo.setError("Tel no should be 10 digits");
            editTextUpdateTelephoneNo.requestFocus();
        } else if (!mobileMatcher.find()) {
            Toast.makeText(UpdateProfile.this, "Please Re-enter Tel No", Toast.LENGTH_SHORT).show();
            editTextUpdateTelephoneNo.setError("Tel no is not valid");
            editTextUpdateTelephoneNo.requestFocus();
        }

        if (TextUtils.isEmpty(email)){
            Toast.makeText(UpdateProfile.this, "Please Enter Email", Toast.LENGTH_SHORT).show();
            editTextUpdateEmail.setError("Email is required");
            editTextUpdateEmail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(UpdateProfile.this, "Please Re-enter Email", Toast.LENGTH_SHORT).show();
            editTextUpdateEmail.setError("Valid Email is required");
            editTextUpdateEmail.requestFocus();
        }

        if (TextUtils.isEmpty(password)){
            Toast.makeText(UpdateProfile.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
            editTextUpdatePassword.setError("Password is required");
            editTextUpdatePassword.requestFocus();
        }else if (editTextUpdatePassword.length() < 6) {
            Toast.makeText(UpdateProfile.this, "Password should be 10 digits", Toast.LENGTH_SHORT).show();
            editTextUpdateEmail.setError("Password too week");
            editTextUpdateEmail.requestFocus();
        }

        if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(UpdateProfile.this, "Please Enter Confirm Password", Toast.LENGTH_SHORT).show();
            editConfirmTextUpdatePassword.setError("Confirm Password is required");
            editConfirmTextUpdatePassword.requestFocus();
        }else if (!editTextUpdatePassword.equals(editConfirmTextUpdatePassword)) {
            Toast.makeText(UpdateProfile.this, "Please give same password", Toast.LENGTH_SHORT).show();
            editTextUpdateEmail.setError("Password confirmation is required");
            editTextUpdateEmail.requestFocus();

            //clear the entered password.
            editTextUpdatePassword.clearComposingText();
            editConfirmTextUpdatePassword.clearComposingText();

        } else {
            //Obtain data entered by user
            firstName = editTextUpdateFirstName.getText().toString();
            lastName = editTextUpdateLastName.getText().toString();
            telNo = editTextUpdateTelephoneNo.getText().toString();
            email = editTextUpdateEmail.getText().toString();
            password = editTextUpdatePassword.getText().toString();
            confirmPassword = editConfirmTextUpdatePassword.getText().toString();

            //Enter user data into realtime database, setup dependencies
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(firstName, lastName, telNo, email, password, confirmPassword);

            //Extract user reference from database for "Registered Users"
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
            String userID = firebaseUser.getUid();

            reference.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //Setting new display name
                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(firstName).build();
                        UserProfileChangeRequest profileChangeRequest1 = new UserProfileChangeRequest.Builder().setDisplayName(lastName).build();

                        firebaseUser.updateProfile(profileChangeRequest);
                        firebaseUser.updateProfile(profileChangeRequest1);

                        Toast.makeText(UpdateProfile.this, "Update successful", Toast.LENGTH_LONG).show();

                        //Stop user for returning to UpdateProfile on pressing back button and close activity
                        Intent intent = new Intent(UpdateProfile.this, Profile.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(UpdateProfile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

    }

    //Fetch data from Firebase and display
    private void showProfile(FirebaseUser firebaseUser) {

        String userIDofRegistered = firebaseUser.getUid();

        //Extracting user reference from database for "Registered Users"
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readWriteUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if (readWriteUserDetails != null){
                    firstName = firebaseUser.getDisplayName();
                    lastName = firebaseUser.getDisplayName();
                    telNo = readWriteUserDetails.telNo;
                    email = readWriteUserDetails.email;
                    password = readWriteUserDetails.password;
                    confirmPassword = readWriteUserDetails.confirmPassword;

                    editTextUpdateFirstName.setText(firstName);
                    editTextUpdateLastName.setText(lastName);
                    editTextUpdateTelephoneNo.setText(telNo);
                    editTextUpdateEmail.setText(email);
                    editTextUpdatePassword.setText(password);
                    editConfirmTextUpdatePassword.setText(confirmPassword);

                } else {
                    Toast.makeText(UpdateProfile.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfile.this, "Something went wrong", Toast.LENGTH_LONG).show();
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
        if (id == R.id.bottomAppBar){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(UpdateProfile.this, UpdateProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(UpdateProfile.this, UpdateEmail.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UpdateProfile.this, DeleteProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_change_pasword) {
            Intent intent = new Intent(UpdateProfile.this, ChangePassword.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_create_compliance) {
            Intent intent = new Intent(UpdateProfile.this, Compliance.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_setting) {
            Toast.makeText(UpdateProfile.this, "menu_setting", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateProfile.this, Setting.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_home) {
            Intent intent = new Intent(UpdateProfile.this, MainActivity.class);
            startActivity(intent);
        }else if (id == R.id.menu_log_out) {
            mAuth.signOut();
            Toast.makeText(UpdateProfile.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateProfile.this, Login2.class);

            //Clear stack to prevent user coming back to MainActivity on pressing back button after login out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UpdateProfile.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}