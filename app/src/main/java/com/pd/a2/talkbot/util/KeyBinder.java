package com.pd.a2.talkbot.util;

import android.content.Context;
import android.view.KeyEvent;

/* Helps bind keyboard keys to audio-files */
public final class KeyBinder {
    private KeyBinder(){
    }

   public static String getName(KeyEvent event) {
        StringBuffer sb = new StringBuffer();
        if(isLetterOrDigit((char) event.getUnicodeChar())) {
            if(Character.isUpperCase((char)event.getUnicodeChar())) {
                sb.append("b");
            }
            char unicodeChar = (char)event.getUnicodeChar();
            sb.append(unicodeChar);
        }else {
            sb.append("-1");
        }
        return sb.toString();
    }
    public static boolean isLetterOrDigit(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c >= '0' && c <= '9');
    }

}
