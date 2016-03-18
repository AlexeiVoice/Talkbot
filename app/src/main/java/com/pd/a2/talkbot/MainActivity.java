package com.pd.a2.talkbot;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    WindowManager.LayoutParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnMakeBrighter = (Button)findViewById(R.id.raiseBrightness);
        btnMakeBrighter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                params = getWindow().getAttributes();
                //float brightness = params.screenBrightness;
                /*if (brightness <= 0.9f) {
                    params.screenBrightness = brightness + 0.1f;
                }*/
                //getWindow().setAttributes(params);
                setBright(1f);
            }
        });
        Button btnDim = (Button)findViewById(R.id.lowerBrightness);
        btnDim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*params = getWindow().getAttributes();
                float brightness = params.screenBrightness;
                if (brightness >= 0.1f){
                    params.screenBrightness = brightness - 0.1f;
                }
                getWindow().setAttributes(params);*/
                setBright(0f);
            }
        });
        Button btnPlaySound = (Button)findViewById(R.id.play);
        btnPlaySound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //playSound();
            }
        });
        Button btnStopSound = (Button)findViewById(R.id.stop);
        btnStopSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSound();
            }
        });
    }

    public void playSound(Uri uri) {

    }

    public void stopSound(){

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                showMessage("D");
                return true;
            case KeyEvent.KEYCODE_F:
                showMessage("F");
                return true;
            case KeyEvent.KEYCODE_J:
                showMessage("J");
                return true;
            case KeyEvent.KEYCODE_0:
                showMessage("0");
                return true;
            case KeyEvent.KEYCODE_1:
                showMessage("1");
                return true;
            case KeyEvent.KEYCODE_2:
                showMessage("2");
                return true;
            case KeyEvent.KEYCODE_K:
                showMessage("K");
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void showMessage(String message){
        Toast.makeText(this, message,Toast.LENGTH_SHORT).show();
    }

    public void setBright(float value) {
        Window mywindow = getWindow();
        WindowManager.LayoutParams lp = mywindow.getAttributes();
        lp.screenBrightness = value;
        mywindow.setAttributes(lp);
    }
}
