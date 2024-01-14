package com.example.wildgurd_pro;

import androidx.appcompat.app.AppCompatActivity;;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class Login extends AppCompatActivity {

    private static int SPLSH_SCREEN = 5000;
    //Variables
    Animation topAnim, bottomAnim;
    TextView slogon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        //Animation
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        //Hooks
        slogon = findViewById(R.id.textView);

        slogon.setAnimation(bottomAnim);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Login.this, Login2.class);
            startActivity(intent);
            finish();
        }, SPLSH_SCREEN);
    }
}