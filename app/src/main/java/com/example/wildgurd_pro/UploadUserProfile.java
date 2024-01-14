package com.example.wildgurd_pro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class UploadUserProfile extends AppCompatActivity {

    private ImageView imageViewProfile;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private Uri uriImage;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_user_profile);

        getSupportActionBar().setTitle("Upload profile picture");

        Button btnUploadChooseProPic = findViewById(R.id.btn_choose_picture);
        Button btnUploadPic = findViewById(R.id.btn_upload);
        imageViewProfile = findViewById(R.id.imageView_profile);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        storageReference = FirebaseStorage.getInstance().getReference("Display Pics");

        Uri uri = firebaseUser.getPhotoUrl();

        //Set current user DP in ImageView
        Picasso.with(UploadUserProfile.this).load(uri).into(imageViewProfile);

        //choose upload to image
        btnUploadChooseProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        //upload image
        btnUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPic();
            }
        });

    }

    //choose upload image
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    //Upload image
    private void uploadPic() {
        if (uriImage != null){
            //Save the image with uid of the currently logged user
            StorageReference fileReference = storageReference.child(auth.getCurrentUser().getUid() + "displaypic."
            + getFileExtension(uriImage));

            //Upload image to storage
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = auth.getCurrentUser();
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileChangeRequest);
                        }
                    });

                    Toast.makeText(UploadUserProfile.this, "Picture Upload Successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UploadUserProfile.this, Profile.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadUserProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(UploadUserProfile.this, "File not selected", Toast.LENGTH_SHORT).show();
        }
    }


    //obtain File Extension of the image
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriImage = data.getData();
            imageViewProfile.setImageURI(uriImage);
        }
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
            Intent intent = new Intent(UploadUserProfile.this, UpdateProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UploadUserProfile.this, DeleteProfile.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_email){
            Intent intent = new Intent(UploadUserProfile.this, UpdateEmail.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_change_pasword) {
            Intent intent = new Intent(UploadUserProfile.this, ChangePassword.class);
            startActivity(intent);
            finish();
        }  else if (id == R.id.menu_create_compliance) {
            Intent intent = new Intent(UploadUserProfile.this, Compliance.class);
            startActivity(intent);
        }else if (id == R.id.menu_setting) {
            Toast.makeText(UploadUserProfile.this, "menu_setting", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UploadUserProfile.this, Setting.class);
            startActivity(intent);
        }else if (id == R.id.menu_home) {
            Intent intent = new Intent(UploadUserProfile.this, MainActivity.class);
            startActivity(intent);
        }else if (id == R.id.menu_log_out) {
            auth.signOut();
            Toast.makeText(UploadUserProfile.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UploadUserProfile.this, Login2.class);

            //Clear stack to prevent user coming back to MainActivity on pressing back button after login out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UploadUserProfile.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}