package com.example.salesrecord;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.WorkManager;

public class AppContextProvider extends Application {
    private static AppContextProvider sInstance;
    private Activity currentActivity; // Guardamos la actividad actual

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        // Registramos un listener que detecta cuando una Activity se crea o resume
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                // MUY IMPORTANTE: Para que ThemeHelper funcione desde el inicio
                currentActivity = activity;
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                // Asegura que la referencia esté actualizada al hacerse visible
                currentActivity = activity;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                // Solo limpiar si la actividad que se pausa es la que tenemos guardada
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                // Limpieza final para evitar fugas de memoria
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }
        });

    }

    public static AppContextProvider getInstance() {
        return sInstance;
    }

    public static Context getContext() {
        if (sInstance == null) {
            throw new IllegalStateException("AppContextProvider no ha sido inicializado. " +
                    "¿Olvidaste registrarlo en el AndroidManifest.xml?");
        }
        return sInstance.getApplicationContext();
    }

    public static Activity getCurrentActivity() {
        return sInstance.currentActivity;
    }

    /**
     * Ejecuta una acción de interfaz de forma 100% segura.
     * Si la aplicación está en la transición (el Preloader murió pero la MainActivity aún es null),
     * este método esperará de forma asíncrona en el hilo principal hasta que la pantalla esté lista.
     *
     * @param runnable Bloque de código que requiere la Activity viva.
     */
    public static void runWithSafeActivity(final SafeActivityRunnable runnable) {
        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                Activity activity = sInstance.currentActivity;

                // 1. Verificamos que la actividad no sea null, no esté muriendo y no sea el Preloader cerrándose
                if (activity != null
                        && !activity.isFinishing()
                        && !activity.isDestroyed()
                        && !activity.getClass().getSimpleName().equals("Preloader")) {

                    // ¡Éxito absoluto! Encontramos la MainActivity (o la pantalla activa válida)
                    runnable.onActivityReady(activity);

                } else {
                    // 2. Zona muerta detectada: Volvemos a consultar en 80 milisegundos sin congelar la app
                    android.util.Log.w("AppContextProvider", "Limbo de cambio de pantalla detectado. Esperando a MainActivity...");
                    handler.postDelayed(this, 80);
                }
            }
        });
    }

    // Interfaz de soporte para pasar la Activity de forma segura como parámetro
    public interface SafeActivityRunnable {
        void onActivityReady(Activity activity);
    }
}
