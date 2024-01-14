package com.example.wildgurd_pro;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class DeleteProfile extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private EditText editTextUserPwd;
    private TextView textViewAuthenticated;
    private String userPwd;
    private Button btnReAuthenticate, btnDeleteUser;

    private static final String TAG = "DeleteProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Delete Profile");

        editTextUserPwd = findViewById(R.id.change_current_pwd);
        textViewAuthenticated = findViewById(R.id.text_authenticated);
        btnDeleteUser = findViewById(R.id.btn_delete_profile);
        btnReAuthenticate = findViewById(R.id.btn_authenticate);

        //Disable delete user btn until user is authenticate
        btnDeleteUser.setEnabled(false);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        assert firebaseUser != null;
        if (firebaseUser.equals("")){
            Toast.makeText(DeleteProfile.this, "Something went wrong!"
                    + "User detail's are not available at that moment", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteProfile.this, Profile.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }
    }

    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        btnReAuthenticate.setOnClickListener(v -> {
            userPwd = editTextUserPwd.getText().toString();

            if (TextUtils.isEmpty(userPwd)){
                Toast.makeText(DeleteProfile.this, "Password is needed", Toast.LENGTH_SHORT).show();
                editTextUserPwd.setError("Please enter your current password to authenticate");
                editTextUserPwd.requestFocus();
            } else {
                //ReAuthenticate user now
                AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwd);

                firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            //Disable editText for pwd
                            editTextUserPwd.setEnabled(false);

                            //Enable delete user btn. Disable authenticate btn
                            btnReAuthenticate.setEnabled(false);
                            btnDeleteUser.setEnabled(true);

                            //Set textView to show user is Authenticated/Verified
                            textViewAuthenticated.setText("You are authenticated/verified." + "You can delete your profile and related data now");
                            Toast.makeText(DeleteProfile.this, "Password has been verified" + "You can delete your profile now, Be careful this action is irreversible", Toast.LENGTH_SHORT).show();

                            //Update color of change password btn
                            btnDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(DeleteProfile.this, R.color.lavender));

                            btnDeleteUser.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    showAlertDialog();
                                }
                            });
                        } else {
                            try {
                                throw task.getException();
                            } catch (Exception e){
                                Toast.makeText(DeleteProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

    }

    private void showAlertDialog() {
        //Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfile.this);
        builder.setTitle("Delete user and related data?");
        builder.setMessage("Do you really want to delete your profile and related data? this action is irreversible!");

        //Open email app if user click/taps continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserData(firebaseUser);
            }
        });

        //Return to user profile activity if user presses cancel btn
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DeleteProfile.this, Profile.class);
                startActivity(intent);
                finish();
            }
        });

        //Create the AlertDialog
        AlertDialog alertDialog = builder. create();

        //Change the btn color of continue
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        //Show the AlertDialog
        alertDialog.show();

    }

    private void deleteUser() {
        this.firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
//                    deleteUserData(firebaseUser);
                    auth.signOut();
                    Toast.makeText(DeleteProfile.this, "User has been deleted!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteProfile.this, Login2.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (Exception e){
                        Toast.makeText(DeleteProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //Delete all the data of user
    private void deleteUserData(FirebaseUser firebaseUser) {
        //Delete display pic
        if (firebaseUser.getPhotoUrl() != null){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "OnSuccess: Photo Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(DeleteProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        //Delete data from realtime database
        DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSuccess: User Data Deleted");
                //Finally delete the user after deleting the related data
                deleteUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(DeleteProfile.this, UpdateProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(DeleteProfile.this, DeleteProfile.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_email){
            Intent intent = new Intent(DeleteProfile.this, UpdateEmail.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_change_pasword) {
            Intent intent = new Intent(DeleteProfile.this, ChangePassword.class);
            startActivity(intent);
            finish();
        }  else if (id == R.id.menu_create_compliance) {
            Intent intent = new Intent(DeleteProfile.this, Compliance.class);
            startActivity(intent);
        }else if (id == R.id.menu_setting) {
            Toast.makeText(DeleteProfile.this, "menu_setting", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteProfile.this, Setting.class);
            startActivity(intent);
        }else if (id == R.id.menu_home) {
            Intent intent = new Intent(DeleteProfile.this, MainActivity.class);
            startActivity(intent);
        }else if (id == R.id.menu_log_out) {
            auth.signOut();
            Toast.makeText(DeleteProfile.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DeleteProfile.this, Login2.class);

            //Clear stack to prevent user coming back to MainActivity on pressing back button after login out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(DeleteProfile.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}