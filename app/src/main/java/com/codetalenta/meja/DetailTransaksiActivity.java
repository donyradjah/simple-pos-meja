package com.codetalenta.meja;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.codetalenta.meja.adapter.DetailTransaksiAdapter;
import com.codetalenta.meja.helper.BaseApiService;
import com.codetalenta.meja.helper.Session;
import com.codetalenta.meja.helper.UrlApi;
import com.codetalenta.meja.model.DetailTransaksi;
import com.codetalenta.meja.model.ItemMenu;
import com.codetalenta.meja.model.Kategori;
import com.codetalenta.meja.model.Produk;
import com.codetalenta.meja.model.Transaksi;
import com.codetalenta.meja.model.TransaksiKirim;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kinda.alert.KAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

public class DetailTransaksiActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    BaseApiService apiService;
    KAlertDialog pDialog;
    Session session;

    Transaksi transaksi;
    ImageView btnMenu;
    TextView txtMenu, namaPemesan, totalBeli, totalBayar, waktuPesan, waktuBayar;
    Button btnStatus, btnBatal;
    RecyclerView rvListProduk;

    ArrayList<DetailTransaksi> detailTransaksis = new ArrayList<>();
    DetailTransaksiAdapter detailTransaksiAdapter;

    int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_transaksi);

        session = new Session(this);
        myRef = database.getReference();
        apiService = UrlApi.getAPIService();

        pDialog = new KAlertDialog(this, KAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Sedang Mengambil Data ...");
        pDialog.setCancelable(false);

        if (!session.getBoolean("login")) {
            startActivity(new Intent(getApplicationContext(), SettingMejaActivity.class));
            finish();
        }

        btnMenu = findViewById(R.id.btnMenu);
        txtMenu = findViewById(R.id.txtMenu);

        namaPemesan = findViewById(R.id.namaPemesan);
        totalBeli = findViewById(R.id.totalBeli);
        totalBayar = findViewById(R.id.totalBayar);
        waktuPesan = findViewById(R.id.waktuPesan);
        waktuBayar = findViewById(R.id.waktuBayar);
        btnStatus = findViewById(R.id.btnStatus);
        btnBatal = findViewById(R.id.btnBatal);

        btnMenu.setImageResource(R.drawable.ic_arrow_back);
        txtMenu.setText("Detail Transaksi");

        detailTransaksiAdapter = new DetailTransaksiAdapter(detailTransaksis, getApplicationContext(), DetailTransaksiActivity.this, new DetailTransaksiAdapter.OnClickButtonListener() {
        });

        rvListProduk = findViewById(R.id.rvListProduk);
        rvListProduk.setLayoutManager(new LinearLayoutManager(this));
        rvListProduk.setHasFixedSize(true);
        rvListProduk.setItemAnimator(new DefaultItemAnimator());
        rvListProduk.setAdapter(detailTransaksiAdapter);

        Intent intent = getIntent();

        id = intent.getIntExtra("idTransaksi", 0);

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new KAlertDialog(DetailTransaksiActivity.this, KAlertDialog.WARNING_TYPE)
                        .setTitleText("Membatalakn Pesanan ?")
                        .setConfirmText("Ya")
                        .setCancelText("Tidak")
                        .setConfirmClickListener(new KAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(KAlertDialog kAlertDialog) {
                                kAlertDialog.dismissWithAnimation();
                                kirimBatal();
                            }
                        })
                        .show();
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        init();
    }

    void kirimBatal() {
        if (!pDialog.isShowing()) {
            pDialog = new KAlertDialog(DetailTransaksiActivity.this, KAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Sedang Mengambil Data ...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        apiService.batal(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    if (response.isSuccessful()) {
                        try {
                            String data = response.body().string();
                            JSONObject result = new JSONObject(data);

                            if (result.getBoolean("success")) {
                                pDialog.dismissWithAnimation();

                                pDialog.dismissWithAnimation();
                                new KAlertDialog(DetailTransaksiActivity.this, KAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Transaksi Berhasil Di Batalkan")
                                        .show();


                            } else {
                                pDialog.dismissWithAnimation();

                                new KAlertDialog(DetailTransaksiActivity.this, KAlertDialog.ERROR_TYPE)
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
                new KAlertDialog(DetailTransaksiActivity.this, KAlertDialog.ERROR_TYPE)
                        .setTitleText("Terjadi Kesalahan")
                        .show();
            }
        });
    }

    void init() {
        if (!pDialog.isShowing()) {
            pDialog = new KAlertDialog(DetailTransaksiActivity.this, KAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Sedang Mengambil Data ...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        myRef = database.getReference("transaksi/" + id);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {


                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                Transaksi transaksi = new Transaksi();

                transaksi.setIdTransaksi(Integer.parseInt(data.getKey()));
                transaksi.setPerangkatId(Integer.parseInt(data.child("perangkatId").getValue().toString()));
                transaksi.setTotalPembelian(Integer.parseInt(data.child("totalPembelian").getValue().toString()));
                transaksi.setTotalBayar(Integer.parseInt(data.child("totalBayar").getValue().toString()));
                transaksi.setNamaPemesan(data.child("namaPemesan").getValue().toString());
                transaksi.setWaktuPesan(data.child("waktuPesan").getValue().toString());
                transaksi.setStatus(data.child("status").getValue().toString());
                transaksi.setWaktuBayar(data.child("waktuBayar").getValue() == null ? "" : data.child("waktuBayar").getValue().toString());
                transaksi.setWaktuSelesai(data.child("waktuSelesai").getValue() == null ? "" : data.child("waktuSelesai").getValue().toString());
                transaksi.setNamaPerangkat(data.child("namaPerangkat").getValue().toString());
                transaksi.setNomorMeja(data.child("nomorMeja").getValue().toString());

                detailTransaksis.clear();

                for (DataSnapshot dataSnapshot : data.child("detailTransaksi").getChildren()) {
                    DetailTransaksi detailTransaksi = new DetailTransaksi();

                    detailTransaksi.setHarga(Integer.parseInt(dataSnapshot.child("harga").getValue().toString()));
                    detailTransaksi.setJumlah(Integer.parseInt(dataSnapshot.child("jumlah").getValue().toString()));
                    detailTransaksi.setKodeProduk(dataSnapshot.child("kodeProduk").getValue().toString());
                    detailTransaksi.setNamaProduk(dataSnapshot.child("namaProduk").getValue().toString());

                    detailTransaksis.add(detailTransaksi);
                }

                detailTransaksiAdapter.notifyDataSetChanged();

                transaksi.setDetailTransaksis(detailTransaksis);

                namaPemesan.setText("Nama Pemesan : " + transaksi.getNamaPemesan());
                totalBeli.setText("Total Beli : \n" + formatRupiah((double) transaksi.getTotalPembelian()));
                totalBayar.setText("Total Bayar : \n" + formatRupiah((double) transaksi.getTotalBayar()));
                waktuPesan.setText("Waktu Pesan : \n" + transaksi.getWaktuPesan());
                waktuBayar.setText("Waktu Bayar : \n" + transaksi.getWaktuBayar());

                switch (transaksi.getStatus()) {
                    case "proses":
                        btnStatus.setBackgroundColor(getResources().getColor(R.color.proses));
                        btnStatus.setText("Proses");
                        btnBatal.setVisibility(View.GONE);
                        break;
                    case "selesai":
                        btnStatus.setBackgroundColor(getResources().getColor(R.color.selesai));
                        btnStatus.setText("Selesai");
                        btnBatal.setVisibility(View.GONE);
                        break;
                    case "batal":
                        btnStatus.setBackgroundColor(getResources().getColor(R.color.batal));
                        btnStatus.setText("Batal");
                        btnBatal.setVisibility(View.GONE);
                        break;
                    case "belum-bayar":
                    default:
                        btnStatus.setBackgroundColor(getResources().getColor(R.color.belum_bayar));
                        btnStatus.setText("Belum Bayar");
                        btnBatal.setVisibility(View.VISIBLE);
                        break;
                }

                pDialog.dismissWithAnimation();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                pDialog.dismissWithAnimation();
            }
        });
    }

    private String formatRupiah(Double number) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        return formatRupiah.format(number);
    }
}