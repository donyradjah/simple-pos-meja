package com.codetalenta.meja.helper;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface BaseApiService {

    @FormUrlEncoded
    @POST("perangkat/ganti-meja")
    Call<ResponseBody> gantiMeja(@Field("idPerangkat") int idPerangkat,
                                 @Field("namaPerangkat") String namaPerangkat,
                                 @Field("nomorMeja") String nomorMeja,
                                 @Field("uniqueId") String uniqueId);

    @FormUrlEncoded
    @POST("transaksi/beli")
    Call<ResponseBody> beli(@Field("transaksi") String transaksiKirim);

    @FormUrlEncoded
    @POST("transaksi/batal")
    Call<ResponseBody> batal(@Field("idTransaksi") int idTransaksi);
}
