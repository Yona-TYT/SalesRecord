package com.example.salesrecord.activitys;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.databinding.ActivityQrBinding;
import com.example.salesrecord.R;
import com.example.salesrecord.db.Article;
import com.example.salesrecord.db.Cliente;
import com.example.salesrecord.db.DatabaseUtils;
import com.example.salesrecord.db.GenericQueue;
import com.example.salesrecord.db.Sale;
import com.example.salesrecord.db.dao.DaoArt;
import com.example.salesrecord.db.dao.DaoClt;
import com.example.salesrecord.db.dao.DaoSal;
import com.example.salesrecord.drive.DriveManager;
import com.example.salesrecord.ex.Dialogs;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.LayoutShareHelper;
import com.example.salesrecord.utls.MathUtls;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.Msg;
import com.example.salesrecord.utls.QrPagoMovilCodec;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QrActivity extends AppCompatActivity {

    private ActivityQrBinding binding;

    private CheckBox check1;
    private Cliente mClt;
    private DaoSal daoSal;
    private DaoClt daoClt;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el archivo XML 'cltlist.xml' en la barra superior
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Obtiene el ID del elemento del menú que el usuario presionó
        int id = item.getItemId();

        // Verifica si presionó el botón de compartir
        if (id == R.id.share) {
            View viewToCapture = findViewById(R.id.share_content_container);

            // 2. Pass the Context (this), the View, and your custom Title
            LayoutShareHelper.shareLayoutAsImage(this, viewToCapture, "Share via:");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Escanear Codigo QR");
        }

        binding = ActivityQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        check1 = binding.chkIsPaid;

        boolean isList = getIntent().getBooleanExtra("islist", false);
        boolean process = getIntent().getBooleanExtra("process", false);
        String saleId = getIntent().getStringExtra("saleid");


        // 1. Recuperar y mostrar el monto total
        double amount = getIntent().getDoubleExtra("amount", 0);
        binding.qrAmount.setText(MoneyUtls.setFormatterEs(amount) + " Bs");



        // 2. Renderizar el código QR
        displayGeneratedQr(MoneyUtls.formatPlainDecimal(amount));


        daoSal = StartVar.appDBall.daoSal();
        daoClt = StartVar.appDBall.daoClt();

        mClt = StartVar.appDBall.daoClt().getUsers(glData.getCurrCltId());

        // 3. Botón para cerrar la Activity
        binding.dummyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(check1.isChecked()){

                    if(process){
                        glData.clearChecksState();
                        GlobalData.shouldProcess = true;
                        finish();
                        return;
                    }

                    List<Sale> mSalList = new ArrayList<>();

                    if(isList) {
                        HashMap<String, Boolean> checkStates = glData.getChecksState();
                        if (checkStates != null) {
                            // Recorremos las llaves del mapa de forma directa
                            for (String s : checkStates.keySet()) {

                                // Usamos Boolean.TRUE.equals para evitar NullPointerException si alguna llave no tiene valor
                                if (Boolean.TRUE.equals(checkStates.get(s))) {

                                    Sale mS = daoSal.getUsers(s);
                                    if (mS != null) {
                                        mSalList.add(mS);
                                    }
                                }
                            }
                        }
                    }
                    else {
                        Sale mS = daoSal.getUsers(saleId);
                        if (mS != null) {
                            mSalList.add(mS);
                        }
                    }
                    List<Object> mList = new ArrayList<>();

                    double mPrice = 0.0;

                    for (Sale mS : mSalList) {
                        if (mS != null && mS.status != 0 && mS.status != 3) {
                            mPrice += mS.monto;

                            mList.addAll(updatePointsStatus( mS.status, 0,  mS, mPrice));
                        }
                    }
                    glData.clearChecksState();

                    finishActivity(mList);
                }
                else {
                    finish();
                }
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        // Cierra esta actividad y regresa de inmediato a la anterior
        finish();
        return true;
    }


    private void displayGeneratedQr(String amount) {
        QrPagoMovilCodec.QrData qrData = new QrPagoMovilCodec.QrData();
        qrData.phone = GlobalData.glPhone;
        qrData.amount = amount;
        qrData.name = GlobalData.glName;
        qrData.dni = GlobalData.glCedula;
        qrData.bank = GlobalData.glCodeBank;

        new Thread(() -> {
            try {
                String payload = QrPagoMovilCodec.encodeWeb(qrData);
                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.suiche);

                // MEJORA 1: Subimos el tamaño a 1024 píxeles para definición interna impecable
                Bitmap qrBitmap = generateQrCodeBitmap(payload, 1024, logo);

                runOnUiThread(() -> {
                    if (binding.imgQrCode != null && qrBitmap != null) {
                        binding.imgQrCode.setImageBitmap(qrBitmap);
                    }
                });
            } catch (Exception e) {
                Log.e("QR_GEN", "Error al generar el código QR: " + e.getMessage());
            }
        }).start();
    }

    public static Bitmap generateQrCodeBitmap(String payload, int size, @Nullable Bitmap logo) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0); // Forzar a usar todo el espacio del bitmap

        // MEJORA 2: Bajamos a nivel Q (25%). Al hacer el logo más pequeño, no requerimos el nivel H (30%).
        // Esto reduce drásticamente la densidad de puntos negros del QR.
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        qrBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        if (logo != null) {
            Bitmap combinado = Bitmap.createBitmap(width, height, qrBitmap.getConfig());
            Canvas canvas = new Canvas(combinado);
            canvas.drawBitmap(qrBitmap, new Matrix(), null);

            // MEJORA 3: El logo ahora ocupa solo la octava parte (12.5%) en vez de la quinta parte (20%)
            int logoSize = size / 8;
            Bitmap logoEscalado = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true);
            int centro = (size - logoSize) / 2;

            canvas.drawBitmap(logoEscalado, centro, centro, null);
            logoEscalado.recycle();

            return combinado;
        }

        return qrBitmap;
    }


    private List<Object> updatePointsStatus(int oldOpt, int opt, Sale mSale, double price){
        List<Object> mList = new ArrayList<>();

        if (oldOpt != opt){

            if (mClt != null) {
                float oldPoints = 0f;
                float newPoints = 0f;

                // 1. Puntos que se habían otorgado con el estatus anterior
                switch (oldOpt) {
                    case 0: // Pagado
                        oldPoints = (float) (price * GlobalData.pointPay);
                        break;
                    case 1: // No pagado
                        oldPoints = (float) (price * GlobalData.pointNoPay);
                        break;
                    case 2: // Perdido
                        oldPoints = (float) (price * GlobalData.pointLost);
                        break;
                    case 3: // Exonerar → no se habían sumado puntos
                        oldPoints = 0f;
                        break;
                }

                // 2. Puntos que corresponden con el nuevo estatus
                switch (opt) {
                    case 0: // Pagado
                        newPoints = (float) (price * GlobalData.pointPay);
                        break;
                    case 1: // No pagado
                        newPoints = (float) (price * GlobalData.pointNoPay);
                        break;
                    case 2: // Perdido
                        newPoints = (float) (price * GlobalData.pointLost);
                        break;
                    case 3: // Exonerar → se restan los puntos anteriores y no se agrega nada
                        newPoints = 0f;
                        break;
                }

                // 3. Matemática segura
                float points = mClt.level - oldPoints + newPoints;
                mClt.level = points > 0f ? points : newPoints;

                daoClt.insertUser(mClt);
                mList.add(mClt);
            }
            mSale.status = opt;
            daoSal.insertUser(mSale);
            mList.add(mSale);
        }
        return mList;
    }

    private void finishActivity(List<Object> mList){
        if( !DriveManager.getAuthState().isAuthorized()){
            finish();
            return;
        }

        if(GlobalData.shouldReload && (mList == null || mList.isEmpty())){
            finish(); // Cierre instantáneo y seguro
            return;
        }

        GlobalData.shouldReload = false;

        // 1. Mostrar el diálogo de carga (Sin tiempo fijo)
        // 1. MOSTRAR EL DIÁLOGO CENTRALIZADO: Usando tu propia clase Dialogs
        Dialogs.progress(this, "Sincronizando con Google Drive");

        // 2. Obtener la cola de sincronización
        GenericQueue queue = GlobalData.getInstance(AppContextProvider.getContext()).getGenericQueue();

        // 3. CONDICIÓN DE PARADA: Configuramos el Callback dinámico
        queue.setOnSyncCompleteListener(new GenericQueue.OnSyncCompleteListener() {
            @Override
            public void onSyncComplete() {
                // ¡ÉXITO! Drive confirmó la subida. Cancelamos la espera de inmediato.
                Dialogs.hideProgress();
                finish(); // Cierre instantáneo y seguro
            }
        });

        // 4. Enviar los datos a la cola para que empiece a procesar
        queue.enqueueList(mList, 3);

        // 5. RESPALDO DE SEGURIDAD (Timeout): Si la red falla, liberamos la interfaz tras 8 segundos
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Verificamos de forma segura y cerramos si se alcanzó el tiempo límite
                // Nota: Como simpleProgress es privado en tu clase Dialogs, llamamos directamente a hideProgress()
                Dialogs.hideProgress();

                // Si el callback sigue activo significa que no terminó a tiempo, lo limpiamos para evitar cierres duplicados
                queue.setOnSyncCompleteListener(null);

                android.util.Log.w("QR", "Timeout de red alcanzado. Liberando pantalla.");
                finish(); // Cerramos la actividad; la cola Room reintentará en background de todas formas
            }
        }, 8000); // 8 segundos máximos de tolerancia

    }
}