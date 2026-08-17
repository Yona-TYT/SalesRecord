package com.example.salesrecord.activitys;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.salesrecord.AppContextProvider;
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

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class QrActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private View mContentView;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());


    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private ActivityQrBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;

        // Recuperar el monto enviado desde la otra Activity
        double amount = getIntent().getDoubleExtra("amount", 0);

        TextView txAmount = findViewById(R.id.qrAmount);

        txAmount.setText(MoneyUtls.setFormatterEs(amount)+" Bs");

        // Renderizar la matriz de ZXing en el ImageView
        displayGeneratedQr(MoneyUtls.formatPlainDecimal(amount));

        // ¡SOLUCIÓN!: Reemplaza el TouchListener de la plantilla por un ClickListener real
        binding.dummyButton.setOnClickListener(v -> {
            finish(); // Cierra QrActivity y regresa al menú anterior sin problemas
        });
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Ocultar la barra de acciones superior del tema
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // ¡SOLUCIÓN!: Comenta o elimina esta línea para que los botones NUNCA se oculten solos
        // mControlsView.setVisibility(View.GONE);

        mVisible = false;

        // Remove callbacks
        mHideHandler.removeCallbacks(mShowPart2Runnable);
    }

    private void show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            mContentView.getWindowInsetsController().show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    private void displayGeneratedQr(String amount) {
        // 1. Instanciar y rellenar los datos de tu Pago Móvil
        QrPagoMovilCodec.QrData qrData = new QrPagoMovilCodec.QrData();
        qrData.phone = GlobalData.glPhone;
        qrData.amount = amount;
        qrData.name = GlobalData.glName;
        qrData.dni = GlobalData.glCedula;
        qrData.bank = GlobalData.glCodeBank;

        // 2. Ejecutar de forma asíncrona en segundo plano
        new Thread(() -> {
            try {
                // Generar el String encriptado con tu método actual

                String payload = QrPagoMovilCodec.encode(qrData);

                Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.suiche);

                // Convertir el payload a un Bitmap de 512x512 píxeles
                Bitmap qrBitmap = generateQrCodeBitmap(payload, 512, logoBitmap);
                // Regresar al hilo principal (UI Thread) para mostrarlo en pantalla
                runOnUiThread(() -> {
                    ImageView myQrImageView = findViewById(R.id.imgQrCode);
                    if (myQrImageView != null && qrBitmap != null) {
                        myQrImageView.setImageBitmap(qrBitmap);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Notificar al usuario mediante tu clase de mensajes o un Toast si algo falla
                    Log.e("QR_GEN", "Error al encriptar o generar el código QR: " + e.getMessage());
                });
            }
        }).start();
    }

    public static Bitmap generateQrCodeBitmap(String payload, int size) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1); // Margen blanco mínimo alrededor del QR

        // Generar la matriz de bits del código QR
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                payload,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
        );

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];

        // Pintar la matriz: Negro para los módulos activos, Blanco para el fondo
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        // Crear el Bitmap nativo de Android
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap generateQrCodeBitmap(String payload, int size, @Nullable Bitmap logo) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        // ¡CRÍTICO!: Forzar nivel de corrección H (Alto) para que el QR sea legible con un logo en el centro
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        // Generar la matriz de bits del código QR
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                payload,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
        );

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];

        // Pintar la matriz base del QR
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        // Crear el Bitmap base del QR
        Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        qrBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        // Si pasaron un logo válido, lo dibujamos exactamente en el centro geométrico
        if (logo != null) {
            // Clonamos el bitmap para poder dibujar sobre él usando un Canvas nativo
            Bitmap combinado = Bitmap.createBitmap(width, height, qrBitmap.getConfig());
            Canvas canvas = new Canvas(combinado);
            canvas.drawBitmap(qrBitmap, new Matrix(), null);

            // Calcular que el logo ocupe máximo la quinta parte (20%) del tamaño total del QR
            int logoSize = size / 5;
            Bitmap logoEscalado = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true);

            // Posicionar el logo exactamente en el centro restando la mitad de sus dimensiones
            int centroX = (size - logoSize) / 2;
            int centroY = (size - logoSize) / 2;

            // Dibujar el logotipo sobre la matriz de puntos
            canvas.drawBitmap(logoEscalado, centroX, centroY, null);

            // Liberar memoria del escalado intermedio
            logoEscalado.recycle();

            return combinado;
        }

        return qrBitmap;
    }
}