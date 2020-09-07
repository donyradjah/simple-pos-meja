package com.codetalenta.meja;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codetalenta.meja.helper.Session;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class HomeActivity extends AppCompatActivity {

    TextView nomorMeja;
    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        nomorMeja = findViewById(R.id.nomorMeja);
        session = new Session(this);

        nomorMeja.setText(session.getString("nomorMeja"));

        nomorMeja.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingMejaActivity.class);
                startActivity(intent);

                return true;
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FIREBASE TOKEN", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        Log.d("FIREBASE TOKEN", token);
                        Toast.makeText(HomeActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void openDaftarMenu(View view) {
        Intent daftarMenu = new Intent(getApplicationContext(), DaftarMenuActivity.class);
        startActivity(daftarMenu);
    }

    public void openDaftarTransksi(View view) {
        Intent daftarTransksi = new Intent(getApplicationContext(), DaftarTransaksiActivity.class);
        startActivity(daftarTransksi);
    }
}