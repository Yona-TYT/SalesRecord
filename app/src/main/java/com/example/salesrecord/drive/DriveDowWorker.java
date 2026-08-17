package com.example.salesrecord.drive;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.salesrecord.ex.DownloadEvents;
import com.example.salesrecord.ex.Logs;
import com.example.salesrecord.ex.PreferenceHelper;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;

import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.greenrobot.event.EventBus;

public class DriveDowWorker extends Worker {
    private static final Logger LOG = Logs.of(DriveDowWorker.class);

    private String googleDriveAccessToken;
    private final Context mContext;

    private static final String KEY_RESULT_MESSAGE = "result_message";
    private static final String KEY_FILES_DOWNLOADED = "files_downloaded";
    private static final String KEY_IS_PRELOADER = "preloader";
    private static final String KEY_IS_NEW_OBJ = "newobj";
    private static final String KEY_IS_FILE_OK = "file";
    private static final String KEY_IS_CHECK = "check";
    private static final String KEY_IS_IMG = "img";
    private static final String KEY_IS_ID = "isId";

    private int count = 0;


    public DriveDowWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String failureMessage = "";
        boolean isFileOk = true;
        count = 0;

        String filePath = getInputData().getString("path");
        String fileName = getInputData().getString("name");
        String fileType = getInputData().getString("type"); // ej. "?alt=media"
        String fileId = getInputData().getString("fileId");

        boolean isPreloader = getInputData().getBoolean("preloader", false);
        boolean isNewObj = getInputData().getBoolean("newobj", false);
        boolean isCheck = getInputData().getBoolean("check", false);
        boolean isImg = getInputData().getBoolean("img", false);
        boolean isId = fileId != null && !fileId.isEmpty();

        File fileToDownload = new File(filePath);
        boolean success = true;
        Throwable failureThrowable = null;

        AuthState authState = DriveManager.getAuthState();
        if (authState == null || !authState.isAuthorized()) {
            failureMessage = "Could not download. Not Authorized.";
            return Result.failure(new Data.Builder()
                    .putString(KEY_RESULT_MESSAGE, failureMessage)
                    .putBoolean(KEY_IS_PRELOADER, isPreloader)
                    .putBoolean(KEY_IS_FILE_OK, false)
                    .build());
        }

        try {
            AuthorizationService authorizationService = DriveManager.getAuthorizationService(mContext);
            googleDriveAccessToken = DriveUtils.getFreshAccessToken(authState, authorizationService);

            if (DriveUtils.isNullOrEmpty(googleDriveAccessToken)) {
                failureMessage = "Failed to fetch Access Token.";
                return Result.failure(new Data.Builder()
                        .putString(KEY_RESULT_MESSAGE, failureMessage)
                        .putBoolean(KEY_IS_PRELOADER, isPreloader)
                        .putBoolean(KEY_IS_FILE_OK, false)
                        .build());
            }

            // ===== Carpetas del path (Sales-Save, etc.) =====
            String folderPath = PreferenceHelper.getInstance().getGoogleDriveFolderPath();
            String[] pathParts = folderPath.split("/");
            String parentFolderId = "root";

            for (String part : pathParts) {
                if (part == null || part.trim().isEmpty()) continue;
                // getOrCreateFolder ya usa mimeType = application/vnd.google-apps.folder
                parentFolderId = DriveUtils.getOrCreateFolder(
                        googleDriveAccessToken,
                        part.trim(),
                        parentFolderId
                );
            }

            String mFolderId = parentFolderId;

            if (DriveUtils.isNullOrEmpty(mFolderId)) {
                failureMessage = "Could not resolve folder";
                success = false;
            } else if (isImg) {
                // ===== Carpeta de imágenes =====
                String imgFolderName = PreferenceHelper.getInstance().getGoogleDriveImgPath();
                String imgFolderId = DriveUtils.getOrCreateFolder(
                        googleDriveAccessToken,
                        imgFolderName,
                        mFolderId
                );

                if (DriveUtils.isNullOrEmpty(imgFolderId)) {
                    failureMessage = "Could not create img folder";
                    success = false;
                } else {
                    List<String[]> mList = DriveUtils.getDriveIdAndNameList(googleDriveAccessToken, imgFolderId);
                    count = mList.size();

                    for (String[] dataFile : mList) {
                        String fId = dataFile[0];
                        String fName = dataFile[1];
                        if (DriveUtils.isNullOrEmpty(fId)) {
                            isFileOk = false;
                            failureMessage = "Error no se encontraron DATOS.";
                            return Result.failure(new Data.Builder()
                                    .putString(KEY_RESULT_MESSAGE, failureMessage)
                                    .putBoolean(KEY_IS_PRELOADER, isPreloader)
                                    .putBoolean(KEY_IS_FILE_OK, isFileOk)
                                    .putBoolean(KEY_IS_IMG, true)
                                    .build());
                        }
                        File currFile = new File(filePath + "/" + fName);
                        downloadFileContents(googleDriveAccessToken, imgFolderId, fId, currFile, fileType, false);
                    }
                }
            } else {
                // ===== Archivo DB / CSV / bin =====
                String driveFileId;
                if (isId) {
                    driveFileId = fileId;
                } else {
                    // Buscar por nombre + mime del archivo (no folder)
                    String fileMime = DriveUtils.getMimeTypeFromFileName(fileName);
                    // Si getMimeTypeFromFileName devuelve spreadsheet para .csv y en Drive
                    // lo guardas como octet-stream, prueba así:
                    driveFileId = DriveUtils.getFileIdFromFileName(
                            googleDriveAccessToken,
                            fileName,
                            mFolderId,
                            fileMime
                    );
                    // Fallback: sin filtrar mime (por si el tipo en Drive no coincide)
                    if (DriveUtils.isNullOrEmpty(driveFileId)) {
                        driveFileId = DriveUtils.getFileIdFromFileName(
                                googleDriveAccessToken,
                                fileName,
                                mFolderId,
                                null  // o un método findFileIdByName sin mime
                        );
                    }
                    // Mejor aún si tienes getFileMetaFromDrive:
                    // DriveFileMeta meta = DriveUtils.getFileMetaFromDrive(...);
                    // driveFileId = meta != null ? meta.id : "";
                }

                if (DriveUtils.isNullOrEmpty(driveFileId)) {
                    isFileOk = false;
                    failureMessage = "Error no se encontraron DATOS.";
                    return Result.failure(new Data.Builder()
                            .putString(KEY_RESULT_MESSAGE, failureMessage)
                            .putBoolean(KEY_IS_PRELOADER, isPreloader)
                            .putBoolean(KEY_IS_FILE_OK, isFileOk)
                            .putBoolean(KEY_IS_CHECK, isCheck)
                            .putBoolean(KEY_IS_NEW_OBJ, isNewObj)
                            .build());
                }

                downloadFileContents(
                        googleDriveAccessToken,
                        mFolderId,
                        driveFileId,
                        fileToDownload,
                        fileType,
                        isId || isCheck || isPreloader  // forzar descarga en check/preloader si aplica
                );
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            success = false;
            failureMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            failureThrowable = e;
        }

        if (success) {
            if (isImg) {
                EventBus.getDefault().post(
                        new DownloadEvents.GoogleDrive().succeeded(" Archivos Descargados: ", count));
            }
            return Result.success(new Data.Builder()
                    .putString(KEY_RESULT_MESSAGE, "")
                    .putBoolean(KEY_IS_PRELOADER, isPreloader)
                    .putBoolean(KEY_IS_NEW_OBJ, isNewObj)
                    .putBoolean(KEY_IS_CHECK, isCheck)
                    .putBoolean(KEY_IS_IMG, isImg)
                    .putBoolean(KEY_IS_ID, isId)
                    .putBoolean(KEY_IS_FILE_OK, true)
                    .putStringArray(KEY_FILES_DOWNLOADED, new String[]{fileToDownload.getAbsolutePath()})
                    .build());
        }

        if (getRunAttemptCount() < getRetryLimit()) {
            return Result.retry();
        }

        return Result.failure(new Data.Builder()
                .putString(KEY_RESULT_MESSAGE, failureMessage)
                .putBoolean(KEY_IS_PRELOADER, isPreloader)
                .putBoolean(KEY_IS_FILE_OK, isFileOk)
                .putBoolean(KEY_IS_CHECK, isCheck)
                .build());
    }

    protected int getRetryLimit() {
        return 3;
    }

    // Método para descargar un archivo desde Google Drive
    public int downloadFileContents(String accessToken, String folderId, String mFileId, File mFile, String mType, boolean isId) throws Exception {


        if(!isId) {
            // 1. Obtener metadatos del archivo en Drive (incluye modifiedTime)
            DriveFileMeta driveFile = DriveUtils.getFileMetaFromDrive(
                    accessToken,
                    mFile.getName(),   // Usamos el nombre del archivo
                    folderId
            );
            if (driveFile == null) {
                LOG.warn("No se pudieron obtener metadatos del archivo en Drive. Se procederá a descargar.");
            } else {
                LOG.debug("Fecha en Drive: {}", driveFile.modifiedTime);

                //DriveUtils.copyToClipboard(mContext, mFile.getName()+driveFile.md5Checksum+" ?"+ driveFile.md5Checksum +" "+DriveUtils.getLocalFileMd5(mFile), mFile.getName());
                //copyToClipboard(mContext, localFile.getName()+" "+ driveFile.md5Checksum +" "+GoogleDriveFileHelper.getLocalFileMd5(localFile), localFile.getName());

                // 2. Comparar fechas si el archivo local existe
                if (mFile.exists() && driveFile.hasModifiedTime()) {
                    long localLastModified = mFile.lastModified();           // milisegundos
                    long driveLastModified = DriveUtils.parseGoogleDriveTime(driveFile.modifiedTime); // milisegundos

                    LOG.debug("Fecha local : {}", new java.util.Date(localLastModified));

                    if (driveLastModified <= localLastModified) {
                        LOG.info("✅ Archivo local está actualizado. No se descargará.");
                        count--;
                        return 0;   // No necesita descargar
                    } else {
                        if (driveFile.md5Checksum.equals(DriveUtils.getLocalFileMd5(mFile))) {
                            count--;
                            return 0;   // No necesita descargar
                        }

                        LOG.info("🔄 Archivo en Drive es más reciente. Procediendo a descargar...");
                    }
                }
            }
        }

        return DriveUtils.downloadFileFromDrive(accessToken, mFileId, mFile, mType);
    }
}
