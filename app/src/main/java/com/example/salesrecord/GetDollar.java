package com.example.salesrecord;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.example.salesrecord.utls.Basic;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetDollar {

    private static Context mContext;
    private static FragmentActivity mActivity;
    private static CurrencyEditText mInput1;
    private static int mSelec;

    static List<String> mUrl = Arrays.asList("https://ve.dolarapi.com/v1/dolares/oficial", "https://pydolarve.org/api/v1/dollar?page=criptodolar", "https://ve.dolarapi.com/v1/dolares/paralelo");
    static List<String> mkey = Arrays.asList("usd", "enparalelovzla");

    public GetDollar(Context applicationContext, FragmentActivity mActivity, int mSelec, CurrencyEditText mInput1) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mSelec = mSelec;
        this.mInput1 = mInput1;
    }

    public static void urlRun() throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(mUrl.get(mSelec))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mActivity.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        Basic.msg("Error de CONEXION!");
                    }
                });
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();

                mActivity.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        try {
                            JSONObject json = new JSONObject(myResponse);
                            Iterator<String> mKeysA = json.keys();

                            for (; mKeysA.hasNext(); ) {
                                String mObjA = mKeysA.next();
                                //String price = json.getJSONObject(mObjA).get("price").toString();

                                String sKey = "promedio";
                                if (mObjA.equals(sKey)) {
                                    String price = json.get(sKey).toString();
                                    double mValue = Double.parseDouble(price);
                                    StartVar startVar = new StartVar();

                                    if(mValue > 0) {
                                        startVar.setDollar(mValue);
                                        mInput1.setText(Basic.setFormatterEs(price));
                                    }
                                    //Basic.msg("Precio del dolar Actualizado: " + price);

                                }
//                                if (mObjA.equals("fechaActualizacion")){
//                                    String date = json.get(i.get(2).get(1)).toString();
//                                    Log.d("PhotoPicker", " --------------Aqui Hay URL?------------------------: " + mObjA+" - date");
//                                    GetDollar.mDate.set(idx, date);
//                                }
                            }
//                            //JSONObject json = new JSONObject(myResponse);
//                            JSONObject json = new JSONObject(myResponse);
//                            Iterator<String> mKeysA = json.keys();
//                            for (; mKeysA.hasNext(); ) {
//                                String mObjA = mKeysA.next();
//                                JSONObject newJson = json.getJSONObject(mObjA);
//                                Iterator<String> mKeysB = newJson.keys();
//
//                                for (; mKeysB.hasNext(); ) {
//                                    String mObjB = mKeysB.next();
//                                    if (mObjB.equals(mkey.get(mSelec))) {
//                                        String value = newJson.getJSONObject(mObjB).get("price").toString();
//                                        StartVar startVar = new StartVar(mContext);
//                                        if(Basic.floatFormat(value) > 0) {
//                                            startVar.setDollar(value);
//                                            mInput1.setText(Basic.setFormatter(value));
//                                        }
//                                        Basic.msg("Precio del dolar Actualizado: " + value);
//                                    }
//                                }
//                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
    }
}
