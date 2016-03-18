package com.pd.a2.talkbot;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.app.KeyguardManager;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    WindowManager.LayoutParams params;
    PowerManager.WakeLock fullWakeLock, partialWakeLock;
    /*Delay before screen goes off*/
    private static final int DELAY = 0;
    /*Default delay before screen goes off. The state before app was launched*/
    int defTimeOut = 0;

    private static String AUDIO_FOLDER;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AUDIO_FOLDER = Environment.getExternalStorageDirectory().toString() + File.separator +
                getResources().getString(R.string.audio_folder);
        createAudioDir(AUDIO_FOLDER);
        setBright(0f);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        defTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, DELAY);
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, DELAY);
        createWakeLocks();
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
                playSound(getString(R.string._0));
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
    @Override
    protected void onPause(){
        super.onPause();
        partialWakeLock.acquire();
        stopSound();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(fullWakeLock.isHeld()){
            fullWakeLock.release();
        }
        if(partialWakeLock.isHeld()){
            partialWakeLock.release();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, defTimeOut);
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
                wakeDevice();
                playSound(getString(R.string._0));
                showMessage("0");
                return true;
            case KeyEvent.KEYCODE_1:
                showMessage("1");
                return true;
            case KeyEvent.KEYCODE_2:
                showMessage("2");
                return true;
            case KeyEvent.KEYCODE_D:
                showMessage("D");
                return true;
            case KeyEvent.KEYCODE_F:
                showMessage("F");
                return true;
            case KeyEvent.KEYCODE_J:
                showMessage("J");
                return true;
            case KeyEvent.KEYCODE_K:
                showMessage("K");
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }


    public void playSound(String fileName) {
        Uri uri = Uri.parse("file://" + AUDIO_FOLDER + File.separator + fileName);
        File audio = new File(uri.getPath());
        if(!audio.exists()) {
            Log.i(getClass().getSimpleName(), "playSound(). file " + uri.toString() + " doesn't " +
                    "exist");
            return;
        }
        Log.i(getClass().getSimpleName(), "playSound(). File uri: " + uri.toString());

        if (!isExternalStorageReadable()) {
            Log.i(getClass().getSimpleName(), "playSound(): external storage isn't readable");
            return;
        }
        //we should stop playing audio-file if it's playing at the moment
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        } else{
            mediaPlayer = new MediaPlayer();
        }
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.release();
                }
            });

        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "playSound() error: " + e.toString());
        }
    }

    public void stopSound(){
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
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

    // Called from onCreate
    protected void createWakeLocks(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        fullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Loneworker - FULL WAKE LOCK");
        partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Talkbot - PARTIAL WAKE LOCK");
    }


    // Called whenever we need to wake up the device
    public void wakeDevice() {
        fullWakeLock.acquire();
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Creates a dir with the given path. Creates missing parent directories if necessary.
     * @param path Folder path.
     * @return Returns TRUE if dir was created. FALSE if there was an error or folder already exists.
     */
    public boolean createAudioDir(String path) {
        File audioDir = new File(path);
        if(!isExternalStorageWritable()) {
            Log.i(getClass().getSimpleName(), "createAudioDir: External storage isn't writable");
            return false;
        }
        try{
            if (!audioDir.exists()) {
                audioDir.mkdirs();
                Log.i(getClass().getSimpleName(), audioDir.toString() + " folder was created");
                return true;
            } else{
                Log.i(getClass().getSimpleName(), audioDir.toString() + " folder already exists");
                return false;
            }
        } catch (Exception e) {
            Log.i(getClass().getSimpleName(), "An error occurred creating the folder "
                    + audioDir.toString());
            e.printStackTrace();
            return false;
        }
    }


}
