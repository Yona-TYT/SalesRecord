package com.example.salesrecord.activitys;

import android.os.Bundle;
import android.view.View;

import com.example.salesrecord.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.salesrecord.databinding.ActivityMainBinding;

import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_add, R.id.navigation_edit)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 2. CAMBIA TU LISTENER POR ESTE REFACTORIZADO:
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {

            // 1. CONTROL DEL TECLADO
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (isKeyboardVisible) {
                binding.navView.setVisibility(View.GONE);
            } else {
                binding.navView.setVisibility(View.VISIBLE);
            }

            // 2. CALCULAR EL ESPACIO SUPERIOR TOTAL (Barra de estado + Barra de título)
            View navHostFragment = findViewById(R.id.nav_host_fragment_activity_main);
            if (navHostFragment != null) {
                // A. Obtener la altura de la barra de estado (reloj, batería)
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                // B. Obtener el tamaño de la Barra de Título (Action Bar) del tema actual de la app
                int actionBarHeight = 0;
                android.util.TypedValue tv = new android.util.TypedValue();
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight = android.util.TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }

                // C. La suma de ambas es el espacio real que debemos respetar
                int totalTopPadding = statusBarHeight + actionBarHeight;

                // D. Controlar el espacio inferior (barra de navegación por gestos de Android)
                int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                int bottomPadding = isKeyboardVisible ? 0 : navigationBarHeight;

                // Aplicamos el padding al contenedor de fragmentos
                navHostFragment.setPadding(0, totalTopPadding, 0, bottomPadding);
            }

            return insets;
        });
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        // Detectamos si el usuario acaba de levantar el dedo de la pantalla (un tap completo)
        if (ev.getAction() == android.view.MotionEvent.ACTION_UP) {
            View v = getCurrentFocus();

            // Si el elemento enfocado actualmente es un campo de texto (o parte del SearchView)
            if (v instanceof android.widget.EditText || (v != null && v.getClass().getName().contains("SearchView"))) {
                android.graphics.Rect outRect = new android.graphics.Rect();
                v.getGlobalVisibleRect(outRect);

                // Si el toque ocurrió FUERA de los límites geométricos de ese input
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus(); // Quitamos el foco

                    // Ocultamos manualmente el teclado por seguridad adicional
                    android.view.inputmethod.InputMethodManager imm =
                            (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}