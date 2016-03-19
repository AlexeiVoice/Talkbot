package com.pd.a2.talkbot;

import android.content.Context;
import android.view.KeyEvent;

/* Helps bind keyboard keys to audio-files */
public final class KeyBinder {
    private KeyBinder(){
    }

    public static String giveAudioFileName(int keycode, KeyEvent event, Context context) {
        String fileName = "";
        /*switch (keycode) {
            //Numbers
            case KeyEvent.KEYCODE_0:
                fileName = context.getString(R.string._0);
                break;
            case KeyEvent.KEYCODE_1:
                fileName = context.getString(R.string._1);
                break;
            case KeyEvent.KEYCODE_2:
                fileName = context.getString(R.string._2);
                break;
            case KeyEvent.KEYCODE_3:
                fileName = context.getString(R.string._3);
                break;
            case KeyEvent.KEYCODE_4:
                fileName = context.getString(R.string._4);
                break;
            case KeyEvent.KEYCODE_5:
                fileName = context.getString(R.string._5);
                break;
            case KeyEvent.KEYCODE_6:
                fileName = context.getString(R.string._6);
                break;
            case KeyEvent.KEYCODE_7:
                fileName = context.getString(R.string._7);
                break;
            case KeyEvent.KEYCODE_8:
                fileName = context.getString(R.string._8);
                break;
            case KeyEvent.KEYCODE_9:
                fileName = context.getString(R.string._9);
                break;
            //Letters
            case KeyEvent.KEYCODE_A:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.A);
                }else {
                    fileName = context.getString(R.string.a);
                }
                break;
            case KeyEvent.KEYCODE_B:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.B);
                }else {
                    fileName = context.getString(R.string.b);
                }
                break;
            case KeyEvent.KEYCODE_C:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.C);
                }else {
                    fileName = context.getString(R.string.c);
                }
                break;
            case KeyEvent.KEYCODE_D:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.D);
                }else {
                    fileName = context.getString(R.string.d);
                }
                break;
            case KeyEvent.KEYCODE_E:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.E);
                }else {
                    fileName = context.getString(R.string.e);
                }
                break;
            case KeyEvent.KEYCODE_F:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.F);
                }else {
                    fileName = context.getString(R.string.f);
                }
                break;
            case KeyEvent.KEYCODE_G:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.G);
                }else {
                    fileName = context.getString(R.string.g);
                }
                break;
            case KeyEvent.KEYCODE_H:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.H);
                }else {
                    fileName = context.getString(R.string.h);
                }
                break;
            case KeyEvent.KEYCODE_I:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.I);
                }else {
                    fileName = context.getString(R.string.i);
                }
                break;
            case KeyEvent.KEYCODE_J:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.J);
                }else {
                    fileName = context.getString(R.string.j);
                }
                break;
            case KeyEvent.KEYCODE_K:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.K);
                }else {
                    fileName = context.getString(R.string.k);
                }
                break;
            case KeyEvent.KEYCODE_L:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.L);
                }else {
                    fileName = context.getString(R.string.l);
                }
                break;
            case KeyEvent.KEYCODE_M:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.M);
                }else {
                    fileName = context.getString(R.string.m);
                }
                break;
            case KeyEvent.KEYCODE_N:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.N);
                }else {
                    fileName = context.getString(R.string.n);
                }
                break;
            case KeyEvent.KEYCODE_O:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.O);
                }else {
                    fileName = context.getString(R.string.o);
                }
                break;
            case KeyEvent.KEYCODE_P:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.P);
                }else {
                    fileName = context.getString(R.string.p);
                }
                break;
            case KeyEvent.KEYCODE_Q:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.Q);
                }else {
                    fileName = context.getString(R.string.q);
                }
                break;
            case KeyEvent.KEYCODE_R:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.R);
                }else {
                    fileName = context.getString(R.string.r);
                }
                break;
            case KeyEvent.KEYCODE_S:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.S);
                }else {
                    fileName = context.getString(R.string.s);
                }
                break;
            case KeyEvent.KEYCODE_T:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.T);
                }else {
                    fileName = context.getString(R.string.t);
                }
                break;
            case KeyEvent.KEYCODE_U:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.U);
                }else {
                    fileName = context.getString(R.string.u);
                }
                break;
            case KeyEvent.KEYCODE_V:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.V);
                }else {
                    fileName = context.getString(R.string.v);
                }
                break;
            case KeyEvent.KEYCODE_W:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.W);
                }else {
                    fileName = context.getString(R.string.w);
                }
                break;
            case KeyEvent.KEYCODE_X:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.X);
                }else {
                    fileName = context.getString(R.string.x);
                }
                break;
            case KeyEvent.KEYCODE_Y:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.Y);
                }else {
                    fileName = context.getString(R.string.y);
                }
                break;
            case KeyEvent.KEYCODE_Z:
                if(isCapsOrShiftOn(event)) {
                    fileName = context.getString(R.string.Z);
                }else {
                    fileName = context.getString(R.string.z);
                }
                break;
            //We should indicate that there were no suitable key-kodes:
            default: fileName = "-1";
                break;
        }*/

        return getName(event).concat(".mp3");
    }
    public static String giveMessageFileName(int keycode, KeyEvent event, Context context) {
        String fileName = "";
        return getName(event).concat(".jpg");
    }
    public static String getName(KeyEvent event) {
        StringBuffer sb = new StringBuffer();
        //TODO check character if it belong to needes set (numbers and letters)
        if(Character.isLetterOrDigit((char)event.getUnicodeChar())) {
            if(isCapsOrShiftOn(event) && !Character.isDigit((char)event.getUnicodeChar())) {
                sb.append("b");
            }
            char unicodeChar = (char)event.getUnicodeChar();
            sb.append(unicodeChar);
        }else {
            sb.append("-1");
        }
        return sb.toString();
    }
    public static boolean isCapsOrShiftOn(KeyEvent keyEvent) {
        return keyEvent.isShiftPressed() || keyEvent.isCapsLockOn();
    }
}
