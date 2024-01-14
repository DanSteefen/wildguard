package com.example.wildgurd_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compliance extends AppCompatActivity {

    private TextInputEditText editTextFirstName, editTextLastName, editTextTelephoneNo, editTextDesc, editYTextTime;
    private String firstName, lastName, telNo, desc, time;
    Button btnSubmit;
    ProgressBar progressBar;
    TextView textView;
    ImageView imageView;
    private ImageView incidentImage;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private Uri uriImage;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compliance);

        mAuth = FirebaseAuth.getInstance();
        editTextFirstName = findViewById(R.id.first_name);
        editTextLastName = findViewById(R.id.second_name);
        editTextTelephoneNo = findViewById(R.id.tel_no);
        editTextDesc = findViewById(R.id.desc);
        editYTextTime = findViewById(R.id.time);
        btnSubmit = findViewById(R.id.button_submit);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.text_location);

        //Set onClickListener on ImageView to open UploadProfile
        imageView= findViewById(R.id.profileImage);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Compliance.this, UploadComoliancePic.class);
            startActivity(intent);
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String first_name, last_name, tel_no, desc, time;
                first_name = editTextFirstName.getText().toString();
                last_name = editTextLastName.getText().toString();
                tel_no = editTextTelephoneNo.getText().toString();
                desc = editTextDesc.getText().toString();
                time = editYTextTime.getText().toString();

                String mobileRegex = " @\"^(?:7|0|(?:\\+94))[0-9]{9,10}$\""; //First no can be [6.8.9] and rest 9 nos, can be any no.
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher((CharSequence) editTextTelephoneNo);

                //Set user DP
                Uri uri = firebaseUser.getPhotoUrl();

                //ImageView and setImageUid should not be used with regular URIs, using picasso.
                Picasso.with(Compliance.this).load(uri).into(imageView);

                if (TextUtils.isEmpty(first_name)){
                    Toast.makeText(Compliance.this, "Please Enter First Name", Toast.LENGTH_SHORT).show();
                    editTextFirstName.setError("First Name is required");
                    editTextFirstName.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(last_name)){
                    Toast.makeText(Compliance.this, "Please Enter Last Name", Toast.LENGTH_SHORT).show();
                    editTextLastName.setError("Last Name is required");
                    editTextLastName.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(tel_no)){
                    Toast.makeText(Compliance.this, "Please Enter Tel No", Toast.LENGTH_SHORT).show();
                    editTextTelephoneNo.setError("Tel No is required");
                    editTextTelephoneNo.requestFocus();
                } else if (tel_no.length() != 10) {
                    Toast.makeText(Compliance.this, "Please Re-enter Tel No", Toast.LENGTH_SHORT).show();
                    editTextTelephoneNo.setError("Tel no should be 10 digits");
                    editTextTelephoneNo.requestFocus();
                } else if (!mobileMatcher.find()) {
                    Toast.makeText(Compliance.this, "Please Re-enter Tel No", Toast.LENGTH_SHORT).show();
                    editTextTelephoneNo.setError("Tel no is not valid");
                    editTextTelephoneNo.requestFocus();
                }
                if (TextUtils.isEmpty(desc)){
                    Toast.makeText(Compliance.this, "Please Enter Description", Toast.LENGTH_SHORT).show();
                    editTextLastName.setError("Description is required");
                    editTextLastName.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(time)){
                    Toast.makeText(Compliance.this, "Please Enter Time Here", Toast.LENGTH_SHORT).show();
                    editTextLastName.setError("Time is required");
                    editTextLastName.requestFocus();
                    return;
                }

                //Enter user data into the firebase realtime database
                ReadWriteUserDetails readWriteUserDetails = new ReadWriteUserDetails(editTextTelephoneNo, editTextFirstName, editTextLastName);

                //Extracting compliance reference from database for "registered compliance"
                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Compliance");
                referenceProfile.child(firebaseUser.getUid()).setValue(readWriteUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            //send verification email
                            firebaseUser.sendEmailVerification();

                            Toast.makeText(Compliance.this, "Compliance added is successfully, Please verify your email",
                                    Toast.LENGTH_LONG).show();

                            //open user profile after successful registration
                            Intent intent = new Intent(Compliance.this, Profile.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else{
                            Toast.makeText(Compliance.this, "Compliance added is failed, Please try again",
                                    Toast.LENGTH_LONG).show();

                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

    }
}