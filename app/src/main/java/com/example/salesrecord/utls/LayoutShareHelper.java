package com.example.salesrecord.utls;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.view.View;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class LayoutShareHelper {

    /**
     * Converts a target View/Layout into a Bitmap image, saves it to the app cache,
     * and triggers the Android Share Sheet to send it to external apps.
     *
     * @param context    The hosting Activity context.
     * @param targetView The View or Layout container to capture.
     * @param title      The title displayed at the top of the Android share dialog.
     */
    public static void shareLayoutAsImage(Context context, View targetView, String title) {
        if (targetView == null) {
            Msg.m("Target view cannot be null");
            return;
        }

        Bitmap layoutBitmap = convertLayoutToBitmap(targetView);
        Uri imageUri = saveBitmapToCache(context, layoutBitmap);

        if (imageUri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

            // Crucial flag to grant temporary read permissions to third-party apps (WhatsApp, Telegram)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Opens the system share chooser sheet
            context.startActivity(Intent.createChooser(shareIntent, title));
        }
    }

    private static Bitmap convertLayoutToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private static Uri saveBitmapToCache(Context context, Bitmap bitmap) {
        Uri fileUri = null;

        try {
            // Guardamos directamente en la raíz de la caché (getCacheDir()) para que coincida perfectamente con tu path="."
            File cacheFile = new File(context.getCacheDir(), "captured_layout.png");
            FileOutputStream outputStream = new FileOutputStream(cacheFile);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Formats the secure URI using your FileProvider definition
            String authority = context.getPackageName() + ".fileprovider";
            fileUri = FileProvider.getUriForFile(context, authority, cacheFile);

        } catch (Exception e) {
            Msg.m("Failed to share image: " + e.getMessage(),true);
        }

        return fileUri;
    }

    /**
     * Shares plain text content to external applications.
     *
     * @param context      The hosting Activity context.
     * @param shareText    The formatted text message to share.
     * @param dialogTitle  The title displayed at the top of the Android share dialog.
     */
    public static void sharePlainText(Context context, String shareText, String dialogTitle) {
        if (shareText == null || shareText.trim().isEmpty()) {
            Msg.m("Text content is empty");
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // Opens the system share chooser sheet for text
        context.startActivity(Intent.createChooser(shareIntent, dialogTitle));
    }
}