package com.example.salesrecord.drive;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.salesrecord.AppContextProvider;
import com.example.salesrecord.DBListCreator;
import com.example.salesrecord.GlobalData;
import com.example.salesrecord.db.GenericQueue;
import com.example.salesrecord.utls.FilesManager;
import com.example.salesrecord.StartVar;
import com.example.salesrecord.ex.Logs;
import com.example.salesrecord.ex.PreferenceHelper;
import com.example.salesrecord.utls.Msg;


import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;

import org.json.JSONException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class DriveManager {
    private static DriveManager instance;
    private static final Logger LOG = Logs.of(DriveManager.class);
    private final PreferenceHelper preferenceHelper;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    private java.io.File file;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getContext());

    public static synchronized DriveManager getInstance() {
        if (instance == null) {
            instance = new DriveManager(PreferenceHelper.getInstance());
        }
        return instance;
    }

    public DriveManager(PreferenceHelper preferenceHelper) {
        this.preferenceHelper = preferenceHelper;
        DriveManager.mContext = AppContextProvider.getContext();
    }

    public static String getGoogleDriveApplicationClientID() {
        //OAuth Client for F-Droid release key
        return "889382808911-scco623dhspjbf5guflmg68f61jl1na3.apps.googleusercontent.com";
        // The Client ID doesn't matter too much, it needs to exist, but for verification what Android
        // does is match by SHA1 signing key + package name.
    }

    public static String getGoogleDriveApplicationOauth2Redirect() {
        //Needs to match in androidmanifest.xml
        return "com.mendhak.gpslogger:/oauth2googledrive";
    }

    public static String[] getGoogleDriveApplicationScopes() {
        return new String[]{"https://www.googleapis.com/auth/drive.file"};
    }

    public static AuthorizationService getAuthorizationService(Context context) {
        return new AuthorizationService(context, new AppAuthConfiguration.Builder().build());
    }

    public static AuthorizationServiceConfiguration getAuthorizationServiceConfiguration() {
        return new AuthorizationServiceConfiguration(
                Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
                Uri.parse("https://www.googleapis.com/oauth2/v4/token"),
                null,
                Uri.parse("https://accounts.google.com/o/oauth2/revoke?token=")
        );
    }

    public static AuthState getAuthState() {
        AuthState authState = new AuthState();

        //Esto guarda la autentificacion ==========================================================
        String google_drive_auth_state = PreferenceHelper.getInstance().getGoogleDriveAuthState();

        //copyToClipboard(mContext, google_drive_auth_state, "tago");

        if (!DriveUtils.isNullOrEmpty(google_drive_auth_state)) {
            try {
                authState = AuthState.jsonDeserialize(google_drive_auth_state);

            } catch (JSONException e) {
                LOG.debug(e.getMessage(), e);
            }
        }
        //==============================================================================================

        return authState;
    }

    public void ImportDataToDrive(List<File> files, boolean img) {
        InternalImportDataToDrive(files, img);
    }

    public void ImportDataToDrive(File file, boolean img) {
        if (file == null) {
            android.util.Log.e("ImportData", "El archivo es null");
            return;
        }
        List<File> files = Collections.singletonList(file);   // Forma más corta y eficiente
        InternalImportDataToDrive(files, img);
    }

    public void InternalImportDataToDrive(List<File> files, boolean img) {

        String tag = img ? StartVar.WORK_TAG_UPLOAD_IMG : StartVar.WORK_TAG_UPLOAD;

        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("filePaths", files.stream().map(File::getAbsolutePath).toArray(String[]::new));
        dataMap.put("filePath", "");
        dataMap.put("img", img);
        dataMap.put("list", true);

        SetWorkResult.startWorkManagerRequest(DriveUpWorker.class, dataMap, tag);
    }

    // Metodo para sincronizar desde el preloder
    public void dataSynchronizeStarting(){
        internalDataSynchronize(false,true, false, false, null);
    }

    // Metodo para sincronizar y enviar objetosnull
    public void dataSynchronizeObj(){
        internalDataSynchronize(false,false, false, false, null);
    }

    // Metodo para sincronizar con un Id especifico
    public void dataSynchronizeSelect(String id){
        internalDataSynchronize(false,false, false, false, id);
    }

    // Metodo para sincronizar y enviar imagenes
    public void dataSynchronizeImg(){
        internalDataSynchronize(true,false, false, false, null);
    }

    // Metodo para chequear estado sincronizacio
    public void dataSynchronizeCheck(){
        internalDataSynchronize(false, false, false, true, null);
    }

    // Metodo para sincronizar
    public void dataSynchronize(){
        GenericQueue q = GlobalData.getInstance(mContext).getGenericQueue();
        if (q.hasPendingQueueItems()) {
            Log.w("DriveSync", "Cola pendiente: se omite dataSynchronize()");
            Msg.m("Sincronizando...");
            return;
        }
        internalDataSynchronize( false,false, false, false, null);
    }

    public void internalDataSynchronize(boolean img, boolean preLoader, boolean newObj, boolean check, String selectId){
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+"/"+StartVar.dirAppName+"/"+StartVar.exportName);

        String tag = img? StartVar.WORK_TAG_DOWNLOAD_IMG : StartVar.WORK_TAG_DOWNLOAD;

        // Preparar datos de entrada
        HashMap<String, Object> dataMap = new HashMap<>();

        if(img){
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+"/"+StartVar.dirAppName+"/");
            dataMap.put("img", true);
        }
        else {
            dataMap.put("img", false);
        }

        dataMap.put("type", "?alt=media");

        if (path != null) {
            dataMap.put("path", path.getAbsolutePath());
        }
        dataMap.put("name", StartVar.exportName);
        dataMap.put("fileId", selectId);

        dataMap.put("preloader", preLoader);
        dataMap.put("newobj", newObj);
        dataMap.put("check", check);

        // Encolar el GoogleDriveDownloadWorker
        SetWorkResult.startWorkManagerRequest(DriveDowWorker.class, dataMap, tag);
    }

    public void uploadDataBase() {
        Context context = AppContextProvider.getContext();

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                // Asegurar que las listas CSV existan antes de exportar
                if (StartVar.csvList == null || StartVar.csvList.isEmpty()) {
                    Log.w("Drive", "csvList vacía → generando con DBListCreator");
                    DBListCreator.createDbLists();
                }

                if (StartVar.csvList == null || StartVar.csvList.isEmpty()) {
                    Log.e("Drive", "csvList sigue vacía: se cancela la subida");
                    Msg.m("Error: no hay datos para exportar");
                    return;
                }

                List<File> mFileList = new ArrayList<>();
                FilesManager fMang = new FilesManager();
                String name = StartVar.exportName;

                File file;
                try {
                    file = fMang.csvExport(StartVar.csvList, name);
                } catch (IOException e) {
                    Log.e("Drive", "Error creando CSV: " + e.getMessage());
                    Msg.m("Error Archivo no creado: " + e.getMessage());
                    return; // NO lanzar RuntimeException
                }

                if (file == null) {
                    Log.e("Drive", "csvExport devolvió null");
                    return;
                }

                if (!FilesManager.isCsvSafeToUpload(file)) {
                    Log.e("Drive", "CSV inválido: subida cancelada");
                    Msg.m("Error: CSV inválido, no se subió a Drive");
                    return;
                }

                mFileList.add(file);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    try {
                        LocalDate currDate = LocalDate.now();
                        File newFile = FilesManager.getNewFile(
                                file.getAbsolutePath(),
                                currDate.toString().replaceAll("\\D", "-") + ".bin",
                                context
                        );
                        if (newFile != null) {
                            mFileList.add(newFile);
                        }
                    } catch (IOException e) {
                        Log.e("Drive", "Error creando respaldo .bin: " + e.getMessage());
                        // no tumbar la app; se puede subir solo el CSV principal
                    }
                }

                if (!mFileList.isEmpty()) {
                    ImportDataToDrive(mFileList, false);
                } else {
                    Log.w("Drive", "No hay archivos para subir");
                }

            } catch (Exception e) {
                Log.e("Drive", "Error en uploadDataBase", e);
                Msg.m("Error al subir: " + e.getMessage());
                // sin throw
            }
        });
    }
    public void uploadDataImg() {
        // 1. Obtener una referencia segura al contexto (evita fugas de memoria)
        final Context appContext =  AppContextProvider.getContext();

        // 2. Ejecutar la búsqueda de archivos en un hilo de fondo
        // NUNCA procesar listas de archivos en el MainLooper/Handler
        new Thread(() -> {
            try {
                List<File> mFileList = new ArrayList<>();

                // Procesamiento de la lista (Operación pesada de I/O)
                for (String s : StartVar.getImgList()) {
                    File mFile = new File(s);
                    if (mFile.exists()) {
                        mFileList.add(mFile);
                    }
                }
                // 3. Encolar el trabajo solo si hay archivos
                if (!mFileList.isEmpty()) {
                   // Basic.msg("Siz img: "+mFileList.size());
                    // Llamamos a ImportDataToDrive directamente desde este hilo
                    ImportDataToDrive( mFileList, true);
                    android.util.Log.i("DriveSync", "✅ Lista preparada: " + mFileList.size() + " imágenes.");
                }
                else {
                    Msg.m("Descargando imagenes...");
                    //Si la lista esta vacia se procede a descargar las imagenes
                    dataSynchronizeImg();
                    android.util.Log.w("DriveSync", "⚠️ No se encontraron imágenes para subir.");
                }

            } catch (Exception e) {
                android.util.Log.e("DriveSync", "❌ Error en el hilo de búsqueda de imágenes", e);
                // Si necesitas mostrar un mensaje al usuario, usa el MainLooper solo para el Toast
                new Handler(Looper.getMainLooper()).post(() ->
                        Msg.m("Error al procesar imágenes: " + e.getMessage())
                );
            }
        }).start();
    }

    public boolean isAvailable() {
        return getAuthState().isAuthorized();
    }

    public boolean hasUserAllowedAutoSending() {
        return preferenceHelper.isGoogleDriveAutoSendEnabled();
    }
//
//    public String getName() {
//        return SenderNames.GOOGLEDRIVE;
//    }

    public boolean accept(File file, String s) {
        return true;
    }
}
