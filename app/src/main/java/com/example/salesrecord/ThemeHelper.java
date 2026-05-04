package com.example.salesrecord;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.content.pm.ApplicationInfo;

public class ThemeHelper {

    /**
     * Aplica un tema dinámico por nombre.
     * Si el nombre falla, usa el tema definido en el Manifiesto automáticamente.
     */
    public static void applyDynamicTheme(Activity activity, String themeName) {
        int themeId = 0;

        if (themeName != null && !themeName.isEmpty()) {
            themeId = activity.getResources().getIdentifier(themeName, "style", activity.getPackageName());
        }

        // Si el nombre no es válido, obtenemos el del manifiesto de forma pura
        if (themeId == 0) {
            themeId = getManifestThemeId(activity);
        }

        activity.setTheme(themeId);
    }

    /**
     * Obtiene el ID del tema del Manifiesto sin conocer su nombre.
     */
    public static int getManifestThemeId(Activity activity) {
        try {
            // 1. Intentar obtener el tema específico de la Activity
            ActivityInfo activityInfo = activity.getPackageManager()
                    .getActivityInfo(activity.getComponentName(), 0);
            if (activityInfo.theme != 0) return activityInfo.theme;

            // 2. Si la Activity no tiene, obtener el tema global de la <application>
            ApplicationInfo appInfo = activity.getPackageManager()
                    .getApplicationInfo(activity.getPackageName(), 0);
            return appInfo.theme;

        } catch (PackageManager.NameNotFoundException e) {
            // Fallback final: El tema por defecto de Android si todo lo demás falla
            // (Evita que la app se detenga)
            return android.R.style.Theme_DeviceDefault;
        }
    }
}
