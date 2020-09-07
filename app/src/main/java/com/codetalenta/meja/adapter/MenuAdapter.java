package com.codetalenta.meja.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codetalenta.meja.R;
import com.codetalenta.meja.helper.UrlApi;
import com.codetalenta.meja.model.ItemMenu;
import com.codetalenta.meja.model.Kategori;
import com.codetalenta.meja.model.Produk;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_KATEGORI = 0;
    public static final int TYPE_PRODUK = 1;

    private ArrayList<ItemMenu> menus;
    private Context context;
    private Activity activity;
    public OnClickButtonListener buttonListener;

    public MenuAdapter(ArrayList<ItemMenu> menus, Context context, Activity activity, OnClickButtonListener buttonListener) {
        this.menus = menus;
        this.context = context;
        this.activity = activity;
        this.buttonListener = buttonListener;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_KATEGORI) {
            // Here Inflating your recyclerview item layout
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
            return new KategoriViewHolder(itemView);
        } else if (viewType == TYPE_PRODUK) {
            // Here Inflating your header view
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_produk, parent, false);
            return new ProdukViewHolder(itemView);
        } else return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        if (holder instanceof KategoriViewHolder) {
            KategoriViewHolder headerViewHolder = (KategoriViewHolder) holder;
            Kategori kategori = (Kategori) menus.get(position);

            headerViewHolder.txtMenu.setText(kategori.getKategori());
        } else if (holder instanceof ProdukViewHolder) {
            final ProdukViewHolder produkViewHolder = (ProdukViewHolder) holder;
            Produk produk = (Produk) menus.get(position);
            String urlGambar = UrlApi.BASE_URL_API + "upload/produk/" + produk.getGambarProduk();

            produkViewHolder.txtNamaProduk.setText(produk.getNamaProduk());
            if (produk.getQty() > 0) {
                produkViewHolder.txtQty.setText(produk.getQty() + " x");
                produkViewHolder.txtQty.setVisibility(View.VISIBLE);
                produkViewHolder.btnTambah.setVisibility(View.VISIBLE);
                produkViewHolder.btnKurang.setVisibility(View.VISIBLE);
                produkViewHolder.btnAddKeranjang.setVisibility(View.GONE);
            } else {
                produkViewHolder.txtQty.setText("");
                produkViewHolder.txtQty.setVisibility(View.GONE);
                produkViewHolder.btnTambah.setVisibility(View.GONE);
                produkViewHolder.btnKurang.setVisibility(View.GONE);
                produkViewHolder.btnAddKeranjang.setVisibility(View.VISIBLE);
            }

            if (produk.getStatus().equals("kosong")) {
                produkViewHolder.txtQty.setText("Tidak Tersedia");
                produkViewHolder.txtQty.setVisibility(View.VISIBLE);
                produkViewHolder.btnAddKeranjang.setVisibility(View.GONE);
            }

            produkViewHolder.txtHargaProduk.setText(formatRupiah((double) produk.getHarga()));
            Picasso.with(context).load(urlGambar).error(R.drawable.produk).into(produkViewHolder.imgProduk);


        }
    }

    @Override
    public int getItemViewType(int position) {
        return menus.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }

    public class KategoriViewHolder extends RecyclerView.ViewHolder {
        TextView txtMenu;

        public KategoriViewHolder(View view) {
            super(view);

            txtMenu = view.findViewById(R.id.txtMenu);
        }
    }

    public class ProdukViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduk;
        TextView txtNamaProduk, txtHargaProduk, txtQty;
        ImageButton btnTambah, btnKurang;
        Button btnAddKeranjang;

        public ProdukViewHolder(View view) {
            super(view);

            imgProduk = view.findViewById(R.id.imgProduk);
            txtNamaProduk = view.findViewById(R.id.txtNamaProduk);
            txtHargaProduk = view.findViewById(R.id.txtHargaProduk);
            txtQty = view.findViewById(R.id.txtQty);
            btnTambah = view.findViewById(R.id.btnTambah);
            btnKurang = view.findViewById(R.id.btnKurang);
            btnAddKeranjang = view.findViewById(R.id.btnAddKeranjang);

            btnTambah.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buttonListener.onPlusClick(view, getAdapterPosition());
                }
            });

            btnKurang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buttonListener.onMinusClick(view, getAdapterPosition());
                }
            });

            btnAddKeranjang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buttonListener.onTambahClick(view, getAdapterPosition());
                }
            });
        }
    }

    private String formatRupiah(Double number) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        return formatRupiah.format(number);
    }

    public interface OnClickButtonListener {
        void onPlusClick(View view, int position);

        void onMinusClick(View view, int position);

        void onTambahClick(View view, int position);
    }
}
