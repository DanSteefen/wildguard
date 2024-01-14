package com.example.wildgurd_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private EditText editTextCurrentPwd, editTextNewPwd, editTextConfirmPwd;
    private TextView textViewAuthenticated;
    private Button btnChangePwd, btnReAuthenticate;
    private String userCurrentPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("Change Password");

        editTextNewPwd = findViewById(R.id.change_current_pwd);
        editTextNewPwd = findViewById(R.id.new_pwd);
        editTextConfirmPwd = findViewById(R.id.confirm_new_pwd);
        textViewAuthenticated = findViewById(R.id.text_authenticated);
        btnReAuthenticate = findViewById(R.id.btn_authenticate);
        btnChangePwd = findViewById(R.id.btn_verify_pwd);

        //Disable editText for new password, Confirm new password and make change pwd btn unclick till user is authenticate
        editTextNewPwd.setEnabled(false);
        editTextConfirmPwd.setEnabled(false);
        btnChangePwd.setEnabled(false);

        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser1 = auth.getCurrentUser();

        if (firebaseUser.equals("")){
            Toast.makeText(ChangePassword.this, "Something went wrong! User's details not available", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePassword.this, Profile.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }
    }

    //ReAuthenticate user before changing pwd
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        btnReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userCurrentPwd = editTextCurrentPwd.getText().toString();

                if (TextUtils.isEmpty(userCurrentPwd)){
                    Toast.makeText(ChangePassword.this, "Password is needed", Toast.LENGTH_SHORT).show();
                    editTextCurrentPwd.setError("Please enter your current password to authenticate");
                    editTextCurrentPwd.requestFocus();
                } else {
                    //ReAuthenticate user now
                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userCurrentPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                //Disable editText for current pwd, Enable editText for new pwd and confirm new pwd
                                editTextCurrentPwd.setEnabled(false);
                                editTextNewPwd.setEnabled(true);
                                editTextConfirmPwd.setEnabled(true);

                                //Enable change pwd btn. Disable authenticate btn
                                btnReAuthenticate.setEnabled(false);
                                btnChangePwd.setEnabled(true);

                                //Set textView to show user is Authenticated/Verified
                                textViewAuthenticated.setText("You are authenticated/verified." + "You can change password now");
                                Toast.makeText(ChangePassword.this, "Password has been verified" + "Change password now", Toast.LENGTH_SHORT).show();

                                //Update color of change password btn
                                btnChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(ChangePassword.this, R.color.lavender));

                                btnChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            } else {
                                try {
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userNewPwd = editTextNewPwd.getText().toString();
        String userNewPwdConfirm = editTextConfirmPwd.getText().toString();

        if (TextUtils.isEmpty(userNewPwd)){
            Toast.makeText(ChangePassword.this, "New password is needed", Toast.LENGTH_SHORT).show();
            editTextNewPwd.setError("Please enter your new password");
            editTextNewPwd.requestFocus();
        } else if (TextUtils.isEmpty(userNewPwdConfirm)) {
            Toast.makeText(ChangePassword.this, "Please confirm your new password", Toast.LENGTH_SHORT).show();
            editTextConfirmPwd.setError("Please re-enter your new password");
            editTextConfirmPwd.requestFocus();
        }  else if (userNewPwd.matches(userNewPwdConfirm)) {
            Toast.makeText(ChangePassword.this, "Password didn't match", Toast.LENGTH_SHORT).show();
            editTextConfirmPwd.setError("Please re-enter same password");
            editTextConfirmPwd.requestFocus();
        }else if (!userCurrentPwd.matches(userNewPwd)) {
            Toast.makeText(ChangePassword.this, "New password can't be same as old password", Toast.LENGTH_SHORT).show();
            editTextConfirmPwd.setError("Please enter new password");
            editTextConfirmPwd.requestFocus();
        } else {
            firebaseUser.updatePassword(userNewPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChangePassword.this, "Password has been changed", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePassword.this, Profile.class);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
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
            Intent intent = new Intent(ChangePassword.this, UpdateProfile.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(ChangePassword.this, DeleteProfile.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(ChangePassword.this, UpdateEmail.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_change_pasword){
            Intent intent = new Intent(ChangePassword.this, ChangePassword.class);
            startActivity(intent);
            finish();
        }  else if (id == R.id.menu_create_compliance) {
            Intent intent = new Intent(ChangePassword.this, Compliance.class);
            startActivity(intent);
        }else if (id == R.id.menu_setting) {
            Toast.makeText(ChangePassword.this, "menu_setting", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePassword.this, Setting.class);
            startActivity(intent);
        }else if (id == R.id.menu_home) {
            Intent intent = new Intent(ChangePassword.this, MainActivity.class);
            startActivity(intent);
        }else if (id == R.id.menu_log_out) {
            auth.signOut();
            Toast.makeText(ChangePassword.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ChangePassword.this, Login2.class);

            //Clear stack to prevent user coming back to MainActivity on pressing back button after login out
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(ChangePassword.this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}