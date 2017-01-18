package com.eardatek.special.player.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 16-4-29.
 */
public class StringUtil {

    public static boolean isNumber(String text){
        Pattern p = Pattern.compile("[0-9]*");
        Matcher matcher = p.matcher(text);
        return matcher.matches();
    }
}
