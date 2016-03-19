package com.pd.a2.talkbot;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

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
    private static String MESSAGE_FOLDER;
    MediaPlayer mediaPlayer;
    ImageView ivPicMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, DELAY);
        //If device run's Android 4.0+ then hide navigation bar
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);
        }
        AUDIO_FOLDER = Environment.getExternalStorageDirectory().toString() + File.separator +
                getResources().getString(R.string.audio_folder);
        MESSAGE_FOLDER = Environment.getExternalStorageDirectory().toString() + File.separator +
                getResources().getString(R.string.message_folder);
        createDir(AUDIO_FOLDER);
        createDir(MESSAGE_FOLDER);
        setBright(0f);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        defTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, DELAY);
        createWakeLocks();
        ivPicMessage = (ImageView)findViewById(R.id.ivPictureMessage);
        ivPicMessage.setScaleType(ImageView.ScaleType.FIT_XY);
    }
    @Override
    protected void onPause(){
        super.onPause();
        Log.i(getClass().getSimpleName(), "onPause");
        if(partialWakeLock != null && partialWakeLock.isHeld() == false) {
            partialWakeLock.acquire();
        }
        if(checkAndstopSound()) {
            mediaPlayer.release();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(getClass().getSimpleName(), "onStop");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(getClass().getSimpleName(), "onResume");
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
        Log.i(getClass().getSimpleName(), "onDestroy");
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, defTimeOut);
        if (checkAndstopSound()) {
            mediaPlayer.release();
        }
        if(fullWakeLock.isHeld()){
            fullWakeLock.release();
        }
        if(partialWakeLock.isHeld()){
            partialWakeLock.release();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //String fileName = KeyBinder.giveFileName(keyCode, event, getApplicationContext());
        String fileName = KeyBinder.getName(event);
        if (fileName != null && !fileName.isEmpty() && fileName.compareTo("-1") != 0) {
            Log.i(getClass().getSimpleName() + "onKeyUp", "Path to file: " + fileName);
            playSound("audio-" + fileName + ".mp3");
            showPicture(fileName + ".jpg");
            return true;
        }else {
            Log.i(getClass().getSimpleName() + "onKeyUp", "No path to file or no suitable key " +
                    "was pressed ");
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
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
        if (!checkAndstopSound()) {
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
                    mediaPlayer.reset();
                    fadeOutImageView(ivPicMessage, null, false);
                }
            });

        } catch (IOException  | IllegalStateException | IllegalArgumentException |SecurityException
                e ) {
            Log.e(getClass().getSimpleName(), "playSound() error: " + e.toString());
        }
    }

    /**
     * Stop audio-playback. Does nothing if there's no playback.
     * Resets mediaplayer, but doesn' releases it.
     * @return TRUE if sound was stopped or nothing was playing. FALSE if there's no instance of
     * mediaPlayer or it's not initialized
     */
    public boolean checkAndstopSound(){
        if (mediaPlayer == null) {
            return false;
        } else {
            try {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
            } catch (IllegalStateException e) {
                Log.e(getClass().getSimpleName(), "checkAndStopSound: " + e.toString());
                return false;
            }
            return true;
        }
    }

    public void showPicture(String fileName) {
        final Uri uri = Uri.parse("file://" + MESSAGE_FOLDER + File.separator + fileName);
        File message = new File(uri.getPath());
        if(!message.exists()) {
            Log.i(getClass().getSimpleName(), "showPicture(). file " + uri.toString() + " doesn't " +
                    "exist");
            return;
        }
        Log.i(getClass().getSimpleName(), "showPicture(). File uri: " + uri.toString());
        if (!isExternalStorageReadable()) {
            Log.i(getClass().getSimpleName(), "showPicture(): external storage isn't readable");
            return;
        }
        //Everything is ok, so we can show picture:
        //let's make screen bright and active while picture's on the display
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);
        }
        setBright(0.7f);
        makeKeepScreenOn();
        if(ivPicMessage.getVisibility() == View.VISIBLE) {
            //in this case we must fade out currently visible image and set new one
            fadeOutImageView(ivPicMessage, uri, true);
        } else{
            ivPicMessage.setVisibility(View.VISIBLE);
            //in this case we must fade in picture and then fade it out
            fadeInImageView(ivPicMessage, uri);
        }
    }

//region additional
    public void setBright(float value) {
        Window mywindow = getWindow();
        WindowManager.LayoutParams lp = mywindow.getAttributes();
        lp.screenBrightness = value;
        mywindow.setAttributes(lp);
    }
    public void showMessage(String message){
        Toast.makeText(this, message,Toast.LENGTH_SHORT).show();
    }

    // Called from onCreate
    protected void createWakeLocks(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        fullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Talkbot - FULL WAKE LOCK");
        partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Talkbot - PARTIAL WAKE LOCK");
    }


    // Called whenever we need to wake up the device
    public void wakeDevice() {
        if(fullWakeLock != null && fullWakeLock.isHeld() == false) {
            fullWakeLock.acquire();
        }

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
//endregion

    /**
     * Creates a dir with the given path. Creates missing parent directories if necessary.
     * @param path Folder path.
     * @return Returns TRUE if dir was created. FALSE if there was an error or folder already exists.
     */
    public boolean createDir(String path) {
        File dir = new File(path);
        if(!isExternalStorageWritable()) {
            Log.i(getClass().getSimpleName(), "createDir: External storage isn't writable");
            return false;
        }
        try{
            if (!dir.exists()) {
                dir.mkdirs();
                Log.i(getClass().getSimpleName(), dir.toString() + " folder was created");
                return true;
            } else{
                Log.i(getClass().getSimpleName(), dir.toString() + " folder already exists");
                return false;
            }
        } catch (Exception e) {
            Log.i(getClass().getSimpleName(), "An error occurred creating the folder "
                    + dir.toString());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fades ImageView in and then fades it out
     * @param imageView
     * @param imageUri
     */
    private void fadeInImageView(final ImageView imageView, final Uri imageUri) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(500);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageView.setImageURI(imageUri);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(fadeIn);
    }

    /**
     * Fades ImageView out and if needed fades it in.
     * @param imageView
     * @param imageUri
     * @param fadeInAfter If after fadeout animation should be called fadein
     */
    private void fadeOutImageView(final ImageView imageView, final Uri imageUri, boolean fadeInAfter) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);
        if(fadeInAfter && imageUri != null) {
            //if we need to fade in after this animation (fade out -> change image -> start fade in)
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageView.setImageURI(imageUri);
                    fadeInImageView(imageView, imageUri);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else{
            //we need to fade out and set brightness to 0
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setBright(0);
                    imageView.setVisibility(View.INVISIBLE);
                    dismissKeepScreenOn();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        imageView.startAnimation(fadeOut);

    }

    public void makeKeepScreenOn(){
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    public void dismissKeepScreenOn(){
        this.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }
}
