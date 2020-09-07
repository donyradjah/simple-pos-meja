package com.codetalenta.meja;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codetalenta.meja.helper.BaseApiService;
import com.codetalenta.meja.helper.Session;
import com.codetalenta.meja.helper.UrlApi;
import com.kinda.alert.KAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingMejaActivity extends AppCompatActivity {

    TextView titleBrand, titleModel, titlePage;
    EditText nomorMeja;
    ImageView backArrow;
    Button btnSimpan;

    Session session;
    KAlertDialog pDialog;
    BaseApiService apiService;


    String model, brand, uniqueIdDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_meja);

        session = new Session(this);
        apiService = UrlApi.getAPIService();

        titleBrand = findViewById(R.id.titleBrand);
        titleModel = findViewById(R.id.titleModel);
        titlePage = findViewById(R.id.titlePage);
        backArrow = findViewById(R.id.backArrow);
        btnSimpan = findViewById(R.id.btnSimpan);
        nomorMeja = findViewById(R.id.nomorMeja);

        model = Build.MODEL;
        brand = Build.BRAND;
        uniqueIdDevice = session.getString("uniqueId");

        pDialog = new KAlertDialog(SettingMejaActivity.this, KAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Sedang memproses ..");
        pDialog.setCancelable(false);

        titleBrand.setText("Brand Perangkat : " + brand);
        titleModel.setText("Model Perangkat : " + model);
        nomorMeja.setText(session.getString("nomorMeja"));

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nomorMeja.getText().toString().equals("") || nomorMeja.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Nomor Meja Harus Di Isi", Toast.LENGTH_LONG).show();
                } else {
                    simpanMeja();
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (!session.getBoolean("login")) {
            backArrow.setVisibility(View.GONE);
        }

    }

    void simpanMeja() {
        if (!pDialog.isShowing()) {
            pDialog.show();
        }

        apiService.gantiMeja(session.getInt("idPerangkat"), (brand + " " + model), nomorMeja.getText().toString(), uniqueIdDevice).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    if (response.isSuccessful()) {
                        try {
                            String data = response.body().string();
                            JSONObject result = new JSONObject(data);

                            if (result.getBoolean("success")) {
                                pDialog.dismissWithAnimation();
                                session.add("login", true);
                                session.add("idPerangkat", result.getInt("id"));
                                session.add("nomorMeja", nomorMeja.getText().toString());

                                new KAlertDialog(SettingMejaActivity.this, KAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Data Berhasil Di Simpan")
                                        .setConfirmClickListener(new KAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(KAlertDialog kAlertDialog) {
                                                kAlertDialog.dismissWithAnimation();
                                                startActivity(new Intent(SettingMejaActivity.this, HomeActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();


                            } else {
                                Toast.makeText(SettingMejaActivity.this, result.getString("pesan"), Toast.LENGTH_LONG).show();
                                pDialog.dismissWithAnimation();

                                new KAlertDialog(SettingMejaActivity.this, KAlertDialog.ERROR_TYPE)
                                        .setTitleText(result.getString("pesan"))
                                        .show();
                            }

                        } catch (JSONException | IOException | NullPointerException e) {
                            e.printStackTrace();
                            pDialog.dismissWithAnimation();
                        }
                    } else {
                        pDialog.dismissWithAnimation();

                    }
                } else {
                    pDialog.dismissWithAnimation();

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                pDialog.dismissWithAnimation();
                new KAlertDialog(SettingMejaActivity.this, KAlertDialog.ERROR_TYPE)
                        .setTitleText("Terjadi Kesalahan")
                        .show();
            }
        });
    }
}