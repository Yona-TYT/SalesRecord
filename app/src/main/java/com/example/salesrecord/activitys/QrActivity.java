package com.example.salesrecord.activitys;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;

import android.util.Log;

import com.example.salesrecord.GlobalData;
import com.example.salesrecord.databinding.ActivityQrBinding;
import com.example.salesrecord.R;
import com.example.salesrecord.utls.MoneyUtls;
import com.example.salesrecord.utls.QrPagoMovilCodec;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class QrActivity extends AppCompatActivity {

    private ActivityQrBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Recuperar y mostrar el monto total
        double amount = getIntent().getDoubleExtra("amount", 0);
        binding.qrAmount.setText(MoneyUtls.setFormatterEs(amount) + " Bs");

        // 2. Renderizar el código QR
        displayGeneratedQr(MoneyUtls.formatPlainDecimal(amount));

        // 3. Botón para cerrar la Activity
        binding.dummyButton.setOnClickListener(v -> finish());
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
}