package com.example.wildgurd_pro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.Person;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wildgurd_pro.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;

    ActivityMainBinding binding;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        textView = findViewById(R.id.home_page);
        user = auth.getCurrentUser();

        if (user == null){
            Intent intent = new Intent(getApplicationContext(), Login2.class);
            startActivity(intent);
            finish();
        }
        else {
            textView.setText(user.getEmail());
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
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
             Intent intent = new Intent(MainActivity.this, Profile.class);
             startActivity(intent);
         }  else if (id == R.id.menu_create_compliance) {
             Intent intent = new Intent(MainActivity.this, Compliance.class);
             startActivity(intent);
         }else if (id == R.id.menu_setting) {
             Toast.makeText(MainActivity.this, "menu_setting", Toast.LENGTH_SHORT).show();
             Intent intent = new Intent(MainActivity.this, Setting.class);
             startActivity(intent);
         }else if (id == R.id.menu_home) {
             Intent intent = new Intent(MainActivity.this, MainActivity.class);
             startActivity(intent);
         }else if (id == R.id.menu_log_out) {
             auth.signOut();
             Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_LONG).show();
             Intent intent = new Intent(MainActivity.this, Login2.class);

             //Clear stack to prevent user coming back to MainActivity on pressing back button after login out
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
             startActivity(intent);
             finish();
         } else {
             Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
         }
        return super.onOptionsItemSelected(item);
    }
}