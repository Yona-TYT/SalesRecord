package com.example.salesrecord.activitys;

import static androidx.fragment.app.FragmentManager.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.DBListCreator;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.R;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.db.Conf;
import com.example.salesrecord.db.dao.DaoCfg;
import com.example.salesrecord.drive.DriveManager;
import com.example.salesrecord.drive.SetWorkResult;
import com.example.salesrecord.ex.PreferenceHelper;
import com.example.salesrecord.utls.Basic;
import com.example.salesrecord.utls.CalendUtls;
import com.example.salesrecord.utls.FilesManager;
import com.example.salesrecord.utls.Msg;
import com.example.salesrecord.utls.SharedViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

import com.example.salesrecord.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationBarView;

import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private DriveManager driveManager;
    private SetWorkResult mWorkResult;
    private ExecutorService driveExecutor;

    private DaoCfg mDaoCfg;
    private Conf mCfg;

    private static final int STORAGE_PERMISSION_CODE = 23;

    private FilesManager mFile;

    private int mTry = 30;
    private final int mTime = 100;

    private boolean reTry = true;

    private SharedViewModel sharedViewModel;
    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    // 1. Variables globales para controlar el tiempo entre clics
    private boolean doubleBackToExitPressedOnce = false;
    private final Handler backPressHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Msg.init(this);

        if (DriveManager.getAuthState().isAuthorized()) {
            driveManager = new DriveManager(PreferenceHelper.getInstance());
            driveExecutor = Executors.newSingleThreadExecutor();

            if(StartVar.mLifecycle == null) {
                StartVar.mLifecycle = ProcessLifecycleOwner.get();
                Msg.m("Sincronizando Datos...");
                driveManager.dataSynchronize();
            }

            mWorkResult = new SetWorkResult(StartVar.mLifecycle, driveExecutor, driveManager);

            // 🎧 Dejas el LiveData escuchando permanentemente de forma segura
            mWorkResult.observeWorkResult();
        }

        // 2. Registrar el callback moderno para controlar el botón de atrás
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                // Si ya se presionó una vez y está dentro del rango de 2 segundos, cierra la app
                if (doubleBackToExitPressedOnce) {
                    finishAffinity(); // Cierra todas las actividades y el proceso de forma limpia
                    return;
                }

                // Primer clic: Activamos la bandera y mostramos el aviso
                doubleBackToExitPressedOnce = true;
                Msg.m("Presiona atrás dos veces para salir");

                // Iniciamos el temporizador: Si pasan 2 segundos, la bandera vuelve a false
                backPressHandler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        });

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_add, R.id.navigation_edit, R.id.navigation_pays)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        //Satrted variables
        StartVar startVar = new StartVar();
        Basic mBasic = new Basic(getApplicationContext());

        StartVar.setmActivity(this);


        mDaoCfg = StartVar.appDBall.daoCfg();
        mCfg = mDaoCfg.getUsers(StartVar.mConfID);

        String[] mDat = mCfg.datos.split("\\|", -1); // -1 conserva vacíos
        if (mDat.length >= 5) {
            GlobalData.glName = mDat[0];;
            GlobalData.glTelef = mDat[1];;
            GlobalData.glPhone= mDat[1].replaceFirst("^0", "58");

            GlobalData.glCedula = mDat[2];;
            GlobalData.glCodeBank = mDat[3];;
            GlobalData.glNameBank = mDat[4];;
        }

        startMainDelayTry(mTime);

        //Start File manager class
        mFile = new FilesManager();

        CalendUtls calen = new CalendUtls();

        if(!StartVar.mPermiss) {
            if (checkStoragePermissions()) {
                startVar.setmPermiss(true);
            }
            else {
                requestForStoragePermissions();
                startVar.setmPermiss(checkStoragePermissions());
            }
        }


        // 2. CAMBIA TU LISTENER POR ESTE REFACTORIZADO:
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {

            // 1. CONTROL DEL TECLADO
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (isKeyboardVisible) {
                binding.navView.setVisibility(View.GONE);
            } else {
                binding.navView.setVisibility(View.VISIBLE);
            }

            // 2. CALCULAR EL ESPACIO SUPERIOR TOTAL
            View navHostFragment = findViewById(R.id.nav_host_fragment_activity_main);
            if (navHostFragment != null) {

                // A. Altura de la barra de estado
                int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

                int actionBarHeight = getResources().getDimensionPixelSize(R.dimen.action_bar_size);

                // C. Suma total
                int totalTopPadding = statusBarHeight + actionBarHeight;

                // D. Espacio inferior
                int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                int bottomPadding = isKeyboardVisible ? 0 : navigationBarHeight;

                navHostFragment.setPadding(0, totalTopPadding, 0, bottomPadding);
            }

            return insets;
        });

        // ==========================================
        // 🔥 CONTROL GLOBAL Y DINÁMICO DE LOS MENÚS SUPERIORES
        // ==========================================
        addMenuProvider(new androidx.core.view.MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull android.view.Menu menu, @NonNull android.view.MenuInflater menuInflater) {
                // Obtenemos el fragmento actual mediante el NavController
                int currentDestinationId = navController.getCurrentDestination() != null
                        ? navController.getCurrentDestination().getId() : -1;

                // 💡 CASO 1: Si estás en la pestaña de INICIO (Home)
                if (currentDestinationId == R.id.navigation_home) {
                    menuInflater.inflate(R.menu.srch, menu);
                    menuInflater.inflate(R.menu.calc, menu);
                    menuInflater.inflate(R.menu.chg, menu);

                }
//[                // 💡 CASO 2: Si estás en la pestaña de AGREGAR (Add)
//                else if (currentDestinationId == R.id.navigation_add) {
//                    menuInflater.inflate(R.menu.save, menu);
//                }]
//                // 💡 CASO 3: Si estás en la pestaña de EDITAR (Edit)
                else if (currentDestinationId == R.id.navigation_edit) {
                    menuInflater.inflate(R.menu.margen, menu);
                }
                // 💡 CASO 4: Si estás en la pestaña de PAGOS (Pays)
                else if (currentDestinationId == R.id.navigation_pays) {
                    // Puedes inflar otros o dejarlo vacío si no lleva íconos
                    menuInflater.inflate(R.menu.summary, menu);
                }

                menuInflater.inflate(R.menu.impor, menu);
                menuInflater.inflate(R.menu.save, menu);
                menuInflater.inflate(R.menu.sync, menu);
                menuInflater.inflate(R.menu.confg, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull android.view.MenuItem menuItem) {
                // Capturamos las acciones de TODOS tus menús usando sus IDs internos
                int id = menuItem.getItemId();

                // IDs de ejemplo (Reemplázalos por los IDs reales de tus archivos XML)
                if (id == R.id.calc) {
                    sharedViewModel.toggleCalc();
                    return true;
                } else if (id == R.id.srch) {
                    sharedViewModel.toggleSrch();
                    return true;
                }
                else if (id == R.id.margen) {
                    sharedViewModel.toggleMarg();
                    return true;
                }

                else if (id == R.id.chg) {
                    return true;
                }
                else if (id == R.id.action_list1) {
                    sharedViewModel.selectListValue(0);
                    return true;
                }
                if (id == R.id.action_list2) {
                    sharedViewModel.selectListValue(1);
                    return true;
                }
                else if (id == R.id.action_list3) {
                    sharedViewModel.selectListValue(2);
                    return true;
                }
//                else if (id == R.id.action_list4) {
//                    sharedViewModel.selectListValue(3);
//                    return true;
//                }

                else if (id == R.id.summary) {
                    Intent intent = new Intent(MainActivity.this, ClientListActivity.class);
                    startActivity(intent);
                    return true;
                }
                //Para Exportar archivo CSV
                else if (id == R.id.save) {
                    DBListCreator mListCreator = new DBListCreator(getApplicationContext());
                    mListCreator.createDbLists(); //Actualiza la lista para exportar csv

                    try {
                        File file = mFile.csvExport(StartVar.csvList);
                        Log.d("PhotoPicker", " Aquiiiiiiiiii Hayyyyyy ------------------------: "+ StartVar.mPermiss);
                        if(file != null) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setType("text/comma-separated-values");
                            // Se obtine la Uri , se debe modificar manidest con: android:authorities="com.example.cow_data.provider"
                            Uri fileUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".fileprovider", file);
                            // Log.d("PhotoPicker", " Aquiiiiiiiiii Hayyyyyy ------------------------: "+ fileUri.toString());

                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // this will not work
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION); // this will not work
                            intent.putExtra(Intent.EXTRA_STREAM, fileUri);

                            startActivity(Intent.createChooser(intent, "Enviar datos para GUARDAR"));
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;

                }
                //Para Importar archivo CSV
                else if (id == R.id.impor) {
                    Msg.m("Importando datos...");
                    if (StartVar.mPermiss) {
                        try {
                            String[] mimetype = {"text/csv", "text/comma-separated-values"};
                            mCsvRequest.launch(mimetype);
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    return true;
                } else //Basic.msg("Sincronizando con el servidor...");
                    if (id == R.id.marge) {
                    //Basic.msg("Combinando registros...");
                    return true;
                } else if(id == R.id.sync){
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                else {
                    Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                    startActivity(intent);
                    return id == R.id.confg;
                }
            }
        });

        // 🔄 OBLIGATORIO: Escuchamos el cambio de fragmentos para redibujar la barra
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Le ordena a la MainActivity destruir los íconos viejos y llamar a onCreateMenu de nuevo
            invalidateOptionsMenu();
        });

        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean navego = NavigationUI.onNavDestinationSelected(item, navController);

                // Solo disparamos la tarea de sincronización, el observador del onCreate ya está listo esperando
                if (DriveManager.getAuthState().isAuthorized() && driveManager != null) {
                    boolean hasQueue = GlobalData.getInstance(getApplicationContext()).getGenericQueue().hasPendingQueueItems();
                    if(!hasQueue) {
                        //Msg.m("Sincronizando Datos...nav");
                        driveManager.dataSynchronize();
                    }
                }

                // 3. Retornas el resultado de la navegación
                return navego;
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(GlobalData.shouldReload && StartVar.mLifecycle != null) {
            if (DriveManager.getAuthState().isAuthorized() && driveManager != null) {
                Msg.m("Sincronizando Datos...");
                driveManager.dataSynchronize();
            }
        }

        GlobalData.shouldReload = true;
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

    private boolean checkStoragePermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            //Android is 11 (R) or above
            else if (Environment.isExternalStorageManager()){
                Log.d("PhotoPicker", " Permiso Aquiiiiiiiiii Hayyyyyy 11100------------------------: " );
                return true;
            }
            else {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startActivityIfNeeded(intent, 101);
                    return true;
                }
                catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    startActivityIfNeeded(intent, 101);
                    return true;
                }
            }
        }
        else {
            Log.d("PhotoPicker", " -----Permiso Aquiiiiiiiiii Hayyyyyy 11100------------------------: " );

            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    //Para importar archivos CSV
    private final ActivityResultLauncher<String[]> mCsvRequest = registerForActivityResult(
        new ActivityResultContracts.OpenDocument(),
        uri -> {
            if (uri != null) {
                StartVar mImpVar = new StartVar();
                // call this to persist permission across decice reboots
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                DBListCreator mListCreator = new DBListCreator(this);
                mListCreator.cvsToDB(StartVar.reloadActivity, uri, 1, "");
            }
            else {
                Msg.m("Solicitud Denegada!");
            }
        }
    );

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>(){
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                //Android is 11 (R) or above
                                if(Environment.isExternalStorageManager()) {
                                    //Manage External Storage Permissions Granted
                                    Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

    void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }
            catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }
        else{
            //Below android 11
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    private void startMainDelayTry(int s){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTry--;
                if(reTry) {
                    StartVar startVar = new StartVar();
                    //Basic.msg("Delay... " + StartVar.mDollar);
                    if (StartVar.mDollar == 0.0) {
                        if (mTry > 0) {
                            startMainDelayTry(mTime);
                        } else {
                            Msg.m("Ultima tasa disponible: " + mCfg.dolar);
                            startVar.setDollar(mCfg.dolar);
                            startVar.setShortDate(mCfg.datetasa);

                            reTry = false;
                        }
                    } else {

                        mCfg.dolar = StartVar.mDollar;

                        long date = System.currentTimeMillis();
                        mCfg.datetasa = CalendUtls.getShortDate(date);
                        mDaoCfg.insertUser(mCfg);
                        startVar.setShortDate(mCfg.datetasa);
                        //Basic.msg("Precio Dolar Guardado "+mCfg.datetasa);
                        reTry = false;
                    }
                }
            }
        }, s);
    }
}