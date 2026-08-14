package com.example.salesrecord;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.Msg;

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

    private static GetDollar instance;

    private static Context mContext;
    private static Activity mActivity;
    private static CurrencyEditText mInput1;
    private static int mSelec;

    static List<String> mUrl = Arrays.asList("https://ve.dolarapi.com/v1/dolares/oficial", "https://pydolarve.org/api/v1/dollar?page=criptodolar", "https://ve.dolarapi.com/v1/dolares/paralelo");
    static List<String> mkey = Arrays.asList("usd", "enparalelovzla");

    public GetDollar(Activity mActivity, int mSelec, CurrencyEditText mInput1) {
        this.mContext = AppContextProvider.getContext();
        this.mActivity = mActivity;
        this.mSelec = mSelec;
        this.mInput1 = mInput1;
    }

    public GetDollar(Activity mActivity) {
        this.mContext = AppContextProvider.getContext();
        this.mActivity = mActivity;
        this.mSelec = 0;
        this.mInput1 = null;
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
                        Msg.m("Error de CONEXION!1");

                        if(mInput1 != null ) {
 //                           mInput1.setFocusable(true);
                            mInput1.setFocusableInTouchMode(true);
                            mInput1.setClickable(true);
                            mInput1.setEnabled(true);
//                            mInput1.setCursorVisible(true);

                            mInput1.setError("Error de CONEXION");
                            mInput1.setCurrencySymbol("Bs");

                            if(StartVar.mDollar > 0) {
                               // mInput1.setCurrencySymbol("Bs (" + StartVar.mShortDate + ")");
                                mInput1.setText(Basic.setFormatterEs(StartVar.mDollar));

                                mInput1.setError("Error de CONEXION, ultima tasa disponible de: "+StartVar.mShortDate);

                            }
                            mInput1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if(!hasFocus) {
                                        if (StartVar.mDollar > 0) {

                                            mInput1.setText(Basic.setFormatterEs(StartVar.mDollar));

                                            mInput1.setError("Error de CONEXION, ultima tasa disponible de: " + StartVar.mShortDate);
                                        }
                                    }
                                }
                            });

//                            Editable editable = mInput1.getText();
//                            if (editable != null) {
//                                TextWatcher[] watchers = editable.getSpans(0, editable.length(), TextWatcher.class);
//                                for (TextWatcher watcher : watchers) {
//                                    mInput1.removeTextChangedListener(watcher);
//                                }
//                            }
//                            mInput1.addTextChangedListener(new TextWatcher() {
//                                @Override
//                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                                }
//
//                                @Override
//                                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                                    double value = mInput1.getNumericValue();
//                                    if (value > 0){
//                                        StartVar startVar = new StartVar();
//                                        startVar.setDollar(value);
//                                    }
//                                }
//
//                                @Override
//                                public void afterTextChanged(Editable s) {
//
//                                }
//                            });
                        }
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
                                        if(mInput1 != null) {

                                            Editable editable = mInput1.getText();
                                            if (editable != null) {
                                                TextWatcher[] watchers = editable.getSpans(0, editable.length(), TextWatcher.class);
                                                for (TextWatcher watcher : watchers) {
                                                    mInput1.removeTextChangedListener(watcher);
                                                }
                                            }

                                            mInput1.setError(null);
                                            mInput1.setFocusable(false);
                                            mInput1.setFocusableInTouchMode(false);
                                            mInput1.setClickable(false);
                                            mInput1.setEnabled(false);
                                            mInput1.setCursorVisible(false);

                                            mInput1.setCurrencySymbol("Bs  Dolar BCV");

                                            mInput1.setText(Basic.setFormatterEs(price));
                                        }
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
