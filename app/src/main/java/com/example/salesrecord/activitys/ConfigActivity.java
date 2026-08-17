package com.example.salesrecord.activitys;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.example.salesrecord.R;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Configuración"); // Opcional: Cambia el título de la barra
        }


        setContentView(R.layout.activity_config);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.app_settings, new ConfigFragment())
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Cierra esta actividad y regresa de inmediato a la anterior
        finish();
        return true;
    }

}