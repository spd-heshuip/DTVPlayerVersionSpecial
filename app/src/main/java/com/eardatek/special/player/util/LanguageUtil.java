package com.eardatek.special.player.util;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.eardatek.special.player.system.DTVApplication;

/**
 * Created by Administrator on 16-9-21.
 */
public class LanguageUtil {

    @SuppressWarnings("deprecation")
    public static void swtichLanguage(){
        Resources resources = DTVApplication.getAppContext().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(DTVApplication.getLocale());
        }else
            configuration.locale = DTVApplication.getLocale();
        resources.updateConfiguration(configuration,displayMetrics);
    }
}
