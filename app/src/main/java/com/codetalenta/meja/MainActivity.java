package com.codetalenta.meja;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.codetalenta.meja.helper.Session;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new Session(this);

        checkPerangkat();
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPerangkat();
    }

    void checkPerangkat() {
        if (session.getBoolean("login")) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        } else {
            if (session.getString("token").equals("")) {
                session.add("uniqueId", UUID.randomUUID().toString());
            }

            Intent intent = new Intent(getApplicationContext(), SettingMejaActivity.class);
            startActivity(intent);
            finish();
        }
    }


}