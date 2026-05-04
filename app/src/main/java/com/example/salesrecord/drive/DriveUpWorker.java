package com.example.salesrecord.drive;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.salesrecord.utls.FilesManager;
import com.example.salesrecord.ex.Logs;
import com.example.salesrecord.ex.PreferenceHelper;
import com.example.salesrecord.ex.UploadEvents;


import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;

import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.greenrobot.event.EventBus;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DriveUpWorker extends Worker {
    private static final Logger LOG = Logs.of(DriveUpWorker.class);

    private String googleDriveAccessToken;

    private final Context mContext;
    private int count = 0;

    public DriveUpWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        //String filePath = getInputData().getString("filePath");
        boolean isList = getInputData().getBoolean("list", false);
        boolean isImg = getInputData().getBoolean("img", false);


        String[] filePaths = getInputData().getStringArray("filePaths");

        count = filePaths.length;

        //File fileToUpload = new File(filePath);
        boolean success = true;
        String failureMessage = "";
        Throwable failureThrowable = null;


        AuthState authState = DriveManager.getAuthState();
        if (!authState.isAuthorized()) {
            EventBus.getDefault().post(new UploadEvents.GoogleDrive().failed("Could not upload to Google Drive. Not Authorized."));
        }

        final AtomicBoolean taskDone = new AtomicBoolean(false);
        //PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();

        DriveUtils.copyToClipboard(mContext, filePaths.length+" ?", "tag");


        try {
            AuthorizationService authorizationService = DriveManager.getAuthorizationService(mContext);
            googleDriveAccessToken = DriveUtils.getFreshAccessToken(authState, authorizationService);

            if (DriveUtils.isNullOrEmpty(googleDriveAccessToken)) {
                LOG.error("Failed to fetch Access Token for Google Drive. Stopping this job.");
                return Result.failure();
            }
            // Figure out the Folder ID to upload to, from the path; recursively create if it doesn't exist.
            String folderPath = PreferenceHelper.getInstance().getGoogleDriveFolderPath();
            String[] pathParts = folderPath.split("/");
            String parentFolderId = PreferenceHelper.getInstance().getGoogleDriveFolderId();
            String latestFolderId = null;

            for (String part : pathParts) {

                latestFolderId = DriveUtils.getFileIdFromFileName(googleDriveAccessToken, part, parentFolderId);

                if (!DriveUtils.isNullOrEmpty(latestFolderId)) {

                    LOG.debug("Folder " + part + " found, folder ID is " + latestFolderId);
                } else {
                    LOG.debug("Folder " + part + " not found, creating.");
                    latestFolderId = DriveUtils.createEmptyFile(googleDriveAccessToken, part,
                            "application/vnd.google-apps.folder", DriveUtils.isNullOrEmpty(parentFolderId) ? "root" : parentFolderId);
                }
                parentFolderId = latestFolderId;
            }

            String diverFolderId = latestFolderId;

            if (DriveUtils.isNullOrEmpty(diverFolderId)) {
                failureMessage = "Could not create folder";
                success = false;
            }
            else{
                 if (isList){
                    String imgFolderName = PreferenceHelper.getInstance().getGoogleDriveImgPath();
                    String imgFolderId = DriveUtils.getFileIdFromFileName(googleDriveAccessToken, imgFolderName, diverFolderId, "application/vnd.google-apps.folder");
                    if (!DriveUtils.isNullOrEmpty(imgFolderId)) {
                        LOG.debug("Folder " + imgFolderName + " found, folder ID is " + diverFolderId);
                    } else {
                        LOG.debug("Folder " + imgFolderName + " not found, creating.");
                        imgFolderId = DriveUtils.createEmptyFile(googleDriveAccessToken, imgFolderName,
                                "application/vnd.google-apps.folder", diverFolderId);
                    }
                    if (DriveUtils.isNullOrEmpty(imgFolderId)) {
                        failureMessage = "Could not create folder";
                        success = false;
                    }
                    else {
                        String folderId = (isImg ? imgFolderId : diverFolderId );
                        for(String path : filePaths){
                            File mFile = new File(path);
                            if(mFile.exists()){
                                filesSet(mFile, folderId);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            success = false;
            failureMessage = e.getMessage();
            failureThrowable = e;
        }

        if(success){
            // Notify internal listeners
            if (isList) {
                EventBus.getDefault().post(new UploadEvents.GoogleDrive().succeeded("Archivos Subidos: "+filePaths.length+" ; ", count));
            }
            else {
                EventBus.getDefault().post(new UploadEvents.GoogleDrive().succeeded());
            }
            // Notify external listeners
            //Basic.sendFileUploadedBroadcast(getApplicationContext(), new String[]{fileToUpload.getAbsolutePath()}, "googledrive");
            return Result.success();
        }

        if(getRunAttemptCount() < getRetryLimit()){
            LOG.warn(String.format("Google Drive - attempt %d of %d failed, will retry", getRunAttemptCount(), getRetryLimit()));
            return Result.retry();
        }

        if(failureThrowable == null) {
            failureThrowable = new Exception(failureMessage);
        }

        EventBus.getDefault().post(new UploadEvents.GoogleDrive().failed(failureMessage, failureThrowable));
        return Result.failure();

    }

    private boolean filesSet(File localFile, String folderId) throws Exception {
        String fileName = localFile.getName();

        LOG.info("=== INICIANDO filesSet() - Archivo: " + fileName);

        // 1. Buscar ID existente
        String driveFileId = DriveUtils.getFileIdFromFileName(googleDriveAccessToken, fileName, folderId);
        LOG.info("   → getFileIdFromFileName() → " + (driveFileId != null ? driveFileId : "NULL"));


        if (DriveUtils.isNullOrEmpty(driveFileId)) {
            LOG.info("   → Archivo no existe → Creando archivo vacío...");
            driveFileId = DriveUtils.createEmptyFile(googleDriveAccessToken,
                    fileName,
                    DriveUtils.getMimeTypeFromFileName(fileName),
                    folderId);
            LOG.info("   → createEmptyFile() devolvió ID: " + (driveFileId != null ? driveFileId : "NULL"));
        }

        if (DriveUtils.isNullOrEmpty(driveFileId)) {
            LOG.error("   ❌ ERROR: No se pudo obtener ni crear el archivo en Drive");
            count--;
            return false;
        }

        LOG.info("   → ID listo para usar: " + driveFileId);

        // 2. Obtener metadatos + MD5 + modification date
        DriveFileMeta driveFile = DriveUtils.getFileMetaFromDrive(googleDriveAccessToken, localFile.getName(), folderId);

        if (driveFile != null) {
            LOG.info("✅ MD5 obtenido: " + (driveFile.md5Checksum != null ? driveFile.md5Checksum : "NULL"));
            LOG.info("   → DriveFileMeta completo: " + driveFile.toString());
        } else {
            LOG.warn("⚠️ getFileMetaFromDrive devolvió NULL (posible archivo recién creado)");
        }

        // 3. Subir contenido
        LOG.info("   → Iniciando updateFileContents()...");
        //copyToClipboard(mContext, localFile.getName()+" "  +getLocalFileMd5(localFile), localFile.getName());

        String mMime = FilesManager.getMimeType(localFile);
        if(fileName.endsWith("bin")){
            mMime = "application/octet-stream";
        }
        if(driveFile == null) {
            //DriveUtils.copyToClipboard(mContext, localFile.getName()+" "+ driveFile.md5Checksum +" "+DriveUtils.getLocalFileMd5(localFile), localFile.getName());
            uploadFileContents(googleDriveAccessToken, driveFileId, localFile);
            LOG.info("   → updateFileContents() finalizado correctamente");
            LOG.info("=== FIN DE filesSet() para " + fileName);
            return true;
        }

        else {
//            if(fileName.endsWith("csv")){
//                DriveUtils.copyToClipboard(mContext, fileName+" --"+ driveFile.md5Checksum +" --"+ DriveUtils.getLocalFileMd5(localFile), fileName);
//
//            }
//
//            if(fileName.endsWith("csv")){
//                uploadFileContents(googleDriveAccessToken, driveFileId, localFile);
//
//                LOG.info("   → updateFileContents() finalizado correctamente");
//                LOG.info("=== FIN DE filesSet() para " + fileName);
//                return true;
//            }
            if(!driveFile.md5Checksum.equals(DriveUtils.getLocalFileMd5(localFile))){
                //copyToClipboard(mContext, driveFile.modifiedTime+driveFile.md5Checksum.isEmpty()+" --"+ driveFile.md5Checksum +" --"+ DriveUtils.getLocalFileMd5(localFile), fileName);

                uploadFileContents(googleDriveAccessToken, driveFileId, localFile);

                LOG.info("   → updateFileContents() finalizado correctamente");
                LOG.info("=== FIN DE filesSet() para " + fileName);
                return true;
            }
        }
        count--;
        return false;
    }

    private String uploadFileContents2(String accessToken, String driveFileId, File fileToUpload, String mType) throws Exception {

        if (fileToUpload == null || !fileToUpload.exists()) {
            throw new IllegalArgumentException("El archivo a subir no existe");
        }

        String mimeType = DriveUtils.getMimeTypeFromFileName(fileToUpload.getName());

        // Para CSV forzamos UTF-8 explícitamente (muy importante)
        String contentType = mType; //"application/octet-stream";


//        String contentType = fileToUpload.getName().toLowerCase().endsWith(".csv")
//                ? "text/csv; charset=utf-8"
//                : mimeType;

        String updateUrl = "https://www.googleapis.com/upload/drive/v3/files/" +
                driveFileId + "?uploadType=media";

        LOG.debug("Subiendo archivo a Drive → ID: {} | Nombre: {} | Tipo: {}",
                driveFileId, fileToUpload.getName(), contentType);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        // Usamos RequestBody.create con el archivo directamente (más eficiente)
        RequestBody body = RequestBody.create(fileToUpload, MediaType.parse(contentType));

        Request.Builder builder = new Request.Builder()
                .url(updateUrl)
                .addHeader("Authorization", "Bearer " + accessToken);

        // Soporte para Android antiguo
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            builder.addHeader("X-HTTP-Method-Override", "PATCH");
            builder.method("POST", body);
        } else {
            builder.method("PATCH", body);
        }

        Request request = builder.build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                LOG.error("Error al subir archivo. Código: {} - {}", response.code(), errorBody);
                throw new Exception("Error al subir archivo: Código " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            LOG.debug("Respuesta de Drive al subir: {}", responseBody);

            JSONObject json = new JSONObject(responseBody);
            String fileId = json.getString("id");

            LOG.info("✅ Archivo subido correctamente a Drive. ID: {}", fileId);
            return fileId;

        } catch (Exception e) {
            LOG.error("Error en updateFileContents al subir: {}", fileToUpload.getName(), e);
            throw e;
        }
    }

    private String uploadFileContents(String accessToken, String driveFileId, File fileToUpload) throws Exception {
        FileInputStream fis = new FileInputStream(fileToUpload);
        String fileId = null;

        String fileUpdateUrl = "https://www.googleapis.com/upload/drive/v3/files/" + driveFileId + "?uploadType=media";

        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url(fileUpdateUrl);

        requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        RequestBody body = RequestBody.create(MediaType.parse(DriveUtils.getMimeTypeFromFileName(fileToUpload.getName())), getByteArrayFromInputStream(fis));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            requestBuilder.addHeader("X-HTTP-Method-Override", "PATCH");
        }
        requestBuilder = requestBuilder.method("PATCH", body);

        Request request = requestBuilder.build();
        Response response = client.newCall(request).execute();
        String fileMetadata = response.body().string();
        LOG.debug(fileMetadata);
        response.body().close();

        JSONObject fileMetadataJson = new JSONObject(fileMetadata);
        fileId = fileMetadataJson.getString("id");

        return fileId;
    }

    private String uploadFileContents3(String accessToken, String driveFileId, File fileToUpload, String mType) throws Exception {
        FileInputStream fis = new FileInputStream(fileToUpload);
        String fileId = null;

        String fileUpdateUrl = "https://www.googleapis.com/upload/drive/v3/files/" + driveFileId + "?uploadType=media";

        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url(fileUpdateUrl);

        requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
        RequestBody body = RequestBody.create(MediaType.parse(DriveUtils.getMimeTypeFromFileName(fileToUpload.getName())), getByteArrayFromInputStream(fis));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            requestBuilder.addHeader("X-HTTP-Method-Override", "PATCH");
        }
        requestBuilder = requestBuilder.method("PATCH", body);

        Request request = requestBuilder.build();
        Response response = client.newCall(request).execute();
        String fileMetadata = response.body().string();
        LOG.debug(fileMetadata);
        response.body().close();

        JSONObject fileMetadataJson = new JSONObject(fileMetadata);
        fileId = fileMetadataJson.getString("id");

        return fileId;
    }

    protected int getRetryLimit() {
        return 3;
    }

    public static byte[] getByteArrayFromInputStream(InputStream is) {

        try {
            int length;
            int size = 1024;
            byte[] buffer;

            if (is instanceof ByteArrayInputStream) {
                size = is.available();
                buffer = new byte[size];
                is.read(buffer, 0, size);
            } else {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                buffer = new byte[size];
                while ((length = is.read(buffer, 0, size)) != -1) {
                    outputStream.write(buffer, 0, length);
                }

                buffer = outputStream.toByteArray();
            }
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                LOG.warn("f", "getStringFromInputStream - could not close stream");
            }
        }

        return null;
    }
}

