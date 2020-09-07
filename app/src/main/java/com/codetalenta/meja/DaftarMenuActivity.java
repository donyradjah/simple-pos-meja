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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codetalenta.meja.adapter.MenuAdapter;
import com.codetalenta.meja.helper.BaseApiService;
import com.codetalenta.meja.helper.ClickListener;
import com.codetalenta.meja.helper.RecyclerTouchListener;
import com.codetalenta.meja.helper.Session;
import com.codetalenta.meja.helper.UrlApi;
import com.codetalenta.meja.model.DetailTransaksi;
import com.codetalenta.meja.model.ItemMenu;
import com.codetalenta.meja.model.Kategori;
import com.codetalenta.meja.model.Produk;
import com.codetalenta.meja.model.TransaksiKirim;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.kinda.alert.KAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

public class DaftarMenuActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    BaseApiService apiService;
    KAlertDialog pDialog;
    Session session;
    MenuAdapter menuAdapter;

    RecyclerView rvListMenu;
    ImageView btnMenu;
    TextView txtMenu, tvTotalItem, tvTotalBayar;
    Button btnBayar;
    EditText edNamaPemesan;

    ArrayList<ItemMenu> menus = new ArrayList<>();
    ArrayList<Kategori> kategoris = new ArrayList<>();
    ArrayList<Produk> keranjang = new ArrayList<>();
    int totalItem = 0;
    int totalHarga = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_menu);

        session = new Session(this);
        myRef = database.getReference();
        apiService = UrlApi.getAPIService();
        menuAdapter = new MenuAdapter(menus, getApplicationContext(), DaftarMenuActivity.this, new MenuAdapter.OnClickButtonListener() {
            @Override
            public void onPlusClick(View view, int position) {
                tambahKeranjang(position);
            }

            @Override
            public void onMinusClick(View view, int position) {
                kurangKeranjang(position);
            }

            @Override
            public void onTambahClick(View view, int position) {
                tambahKeranjang(position);
            }
        });

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
        rvListMenu = findViewById(R.id.listMenu);
        tvTotalItem = findViewById(R.id.tvTotalItem);
        tvTotalBayar = findViewById(R.id.tvTotalBayar);
        btnBayar = findViewById(R.id.btnBayar);
        edNamaPemesan = findViewById(R.id.edNamaPemesan);

        btnMenu.setImageResource(R.drawable.ic_arrow_back);
        txtMenu.setText("Daftar Menu");
        rvListMenu.setLayoutManager(new LinearLayoutManager(this));
        rvListMenu.setHasFixedSize(true);
        rvListMenu.setItemAnimator(new DefaultItemAnimator());
        rvListMenu.setAdapter(menuAdapter);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnBayar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edNamaPemesan.getText().toString().equals("") || edNamaPemesan.getText().toString().isEmpty()) {

                    if (pDialog.isShowing()) {
                        pDialog.dismissWithAnimation();
                    }

                    new KAlertDialog(DaftarMenuActivity.this, KAlertDialog.ERROR_TYPE)
                            .setTitleText("Isi Nama Pemesan")
                            .show();
                } else {
                    if (pDialog.isShowing()) {
                        pDialog.dismissWithAnimation();
                    }

                    new KAlertDialog(DaftarMenuActivity.this, KAlertDialog.WARNING_TYPE)
                            .setTitleText("Melanjutkan Ke Pembayaran ?")
                            .setConfirmText("Ya")
                            .setCancelText("Pilih Lagi")
                            .setConfirmClickListener(new KAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(KAlertDialog kAlertDialog) {
                                    kAlertDialog.dismissWithAnimation();
                                    bayar();
                                }
                            })
                            .show();
                }
            }
        });

        init();

    }

    void init() {

        if (!pDialog.isShowing()) {
            pDialog = new KAlertDialog(DaftarMenuActivity.this, KAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Sedang Mengambil Data ...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        menus.clear();
        kategoris.clear();
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                kategoris.clear();

                for (DataSnapshot data : dataSnapshot.child("kategori").getChildren()) {

                    Kategori kategori = new Kategori();
                    kategori.setId(Integer.parseInt(data.getKey()));
                    kategori.setKategori(data.child("kategori").getValue().toString());
                    kategori.setProduks(new ArrayList<Produk>());
                    kategoris.add(kategori);
                }

                for (DataSnapshot data : dataSnapshot.child("produk").getChildren()) {

                    Produk produk = new Produk();

                    produk.setId(Integer.parseInt(data.getKey()));
                    produk.setNamaProduk(data.child("namaProduk").getValue().toString());
                    produk.setHarga(Integer.parseInt(data.child("harga").getValue().toString()));
                    produk.setKodeProduk(data.child("kodeProduk").getValue().toString());
                    produk.setGambarProduk(data.child("gambarProduk").getValue().toString());
                    produk.setKategoriId(data.child("kategoriId").getValue().toString());
                    produk.setStatus(data.child("status").getValue().toString());
                    produk.setQty(0);

                    String[] kategoriId = data.child("kategoriId").getValue().toString().split(",");

                    for (String s : kategoriId) {
                        int idKategori = Integer.parseInt(s);
                        int keyKategori = cariKategoriById(idKategori);
                        if (keyKategori >= 0) {
                            ArrayList<Produk> produks = kategoris.get(keyKategori).getProduks();

                            produks.add(produk);
                            kategoris.get(keyKategori).setProduks(produks);
                        }
                    }
                }

                menus.clear();
                for (Kategori kategori : kategoris) {
                    menus.add((ItemMenu) kategori);

                    for (Produk produk : kategori.getProduks()) {
                        menus.add((ItemMenu) produk);
                    }
                }

                menuAdapter.notifyDataSetChanged();
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

    void tambahKeranjang(int i) {
        Produk produk = (Produk) menus.get(i);
        int qtyProduk = produk.getQty() + 1;

        produk.setQty(qtyProduk);

        if (qtyProduk == 1) {
            keranjang.add(produk);
        } else {
            int keranjangIndex = cariKeranjangByProdukId(produk.getId());

            if (keranjangIndex > -1) {
                keranjang.set(keranjangIndex, produk);
            } else {
                keranjang.add(produk);
            }
        }

        menuAdapter.notifyDataSetChanged();
        hitungTotal();
    }

    void kurangKeranjang(int i) {
        Produk produk = (Produk) menus.get(i);
        int qtyProduk = produk.getQty() - 1;

        produk.setQty(qtyProduk);

        if (qtyProduk == 0) {
            int keranjangIndex = cariKeranjangByProdukId(produk.getId());

            if (keranjangIndex > -1) {
                keranjang.remove(keranjangIndex);
            }
        } else {
            int keranjangIndex = cariKeranjangByProdukId(produk.getId());

            if (keranjangIndex > -1) {
                keranjang.set(keranjangIndex, produk);
            }
        }

        menuAdapter.notifyDataSetChanged();
        hitungTotal();
    }

    void hitungTotal() {
        totalItem = 0;
        totalHarga = 0;

        for (Produk produk : keranjang) {
            totalItem += produk.getQty();
            totalHarga += produk.getQty() * produk.getHarga();
        }

        tvTotalItem.setText(totalItem + " Item");
        tvTotalBayar.setText("Total Bayar : " + formatRupiah((double) totalHarga));

    }

    int cariKategoriById(int id) {
        int i = 0;
        for (Kategori kategori : kategoris) {
            if (kategori.getId() == id) {
                return i;
            }

            i++;
        }

        return -1;
    }

    int cariKeranjangByProdukId(int id) {
        int i = 0;
        for (Produk detailTransaksi : keranjang) {
            if (detailTransaksi.getId() == id) {
                return i;
            }

            i++;
        }

        return -1;
    }


    void bayar() {
        if (!pDialog.isShowing()) {
            pDialog = new KAlertDialog(DaftarMenuActivity.this, KAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Sedang Mengambil Data ...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        Gson gson = new Gson();

        apiService.beli(gson.toJson(new TransaksiKirim(session.getInt("idPerangkat"), session.getString("uniqueId"), edNamaPemesan.getText().toString(), totalHarga, keranjang))).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    if (response.isSuccessful()) {
                        try {
                            String data = response.body().string();
                            JSONObject result = new JSONObject(data);

                            if (result.getBoolean("success")) {
                                pDialog.dismissWithAnimation();

                                menus.clear();
                                keranjang.clear();
                                for (Kategori kategori : kategoris) {
                                    menus.add((ItemMenu) kategori);

                                    for (Produk produk : kategori.getProduks()) {
                                        menus.add((ItemMenu) produk);
                                    }
                                }
                                menuAdapter.notifyDataSetChanged();

                                hitungTotal();
                                edNamaPemesan.setText("");

                                Intent intent = new Intent(DaftarMenuActivity.this, KonfirmasiPembelianActivity.class);
                                intent.putExtra("totalHarga", totalHarga);
                                intent.putExtra("totalItem", totalItem);
                                intent.putExtra("namaPemesan", edNamaPemesan.getText().toString());

                                startActivity(intent);


                            } else {
                                pDialog.dismissWithAnimation();

                                new KAlertDialog(DaftarMenuActivity.this, KAlertDialog.ERROR_TYPE)
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
                new KAlertDialog(DaftarMenuActivity.this, KAlertDialog.ERROR_TYPE)
                        .setTitleText("Terjadi Kesalahan")
                        .show();
            }
        });
    }

    private String formatRupiah(Double number) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        return formatRupiah.format(number);
    }


}