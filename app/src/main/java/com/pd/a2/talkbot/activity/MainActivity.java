package com.pd.a2.talkbot.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.pd.a2.talkbot.util.KeyBinder;
import com.pd.a2.talkbot.R;
import com.pd.a2.talkbot.util.PlaybackEvent;
import com.pd.a2.talkbot.util.State;
import com.pd.a2.talkbot.util.StorageUtil;
import com.pd.a2.talkbot.service.mService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static String MESSAGE_FOLDER;
    private static float MESSAGE_BRIGHTNESS = 0.7f;
    private static String MESSAGE_PIC_FORMAT = ".jpg";
    private static String AUDIO_FOLDER;
    private static String AUDIO_FOLDER_PREFIX = "audio-";
    private static String AUDIO_FILE_FORMAT = ".mp3";
    /*Delay before screen goes off*/
    private static final int DELAY = 0;
    /*Default delay before screen goes off. The state before app was launched*/
    private int defTimeOut = 0;

    WindowManager.LayoutParams params;
    PowerManager.WakeLock fullWakeLock, partialWakeLock;
    mService mAudioService;
    boolean mBound = false;
    ImageView ivPicMessage;
    /** Defines callbacks for service binding, passed to bindService() */
    private AudioServiceConnection mConnection;

//region State Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Log.i(getClass().getSimpleName(), "onCreate");
        setContentView(R.layout.activity_main);
        ivPicMessage = (ImageView)findViewById(R.id.ivPictureMessage);
        try {
            mConnection = (AudioServiceConnection)getLastCustomNonConfigurationInstance();
        } catch (ClassCastException e) {
            Log.i(getClass().getSimpleName(), "onCreate, trying to get retain audioService: "
                    + e.toString());
        }
        connectToService();
        if(mAudioService != null) {
            if(mAudioService.getCurrentState() == State.START_PLAYING) {
                String keyName = mAudioService.getCurrentKeyProcessed();
                Uri imageUri = Uri.parse("file://" + MESSAGE_FOLDER + File.separator +
                        keyName  + MESSAGE_PIC_FORMAT);
                ivPicMessage.setImageURI(imageUri);
                ivPicMessage.setVisibility(View.VISIBLE);
            }
        }
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
        //Set minimum brightness:
        setBright(0f);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        defTimeOut = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, DELAY);
        createWakeLocks();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(getClass().getSimpleName(), "onPause");
        /*if(partialWakeLock != null && partialWakeLock.isHeld() == false) {
            partialWakeLock.acquire();
        }*/
        /*if(checkAndstopSound()) {
            mediaPlayer.release();
        }*/
    }

    @Override
    protected void onStop() {
        Log.i(getClass().getSimpleName(), "onStop");

        super.onStop();
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

        if(fullWakeLock.isHeld()){
            fullWakeLock.release();
        }
        if(partialWakeLock.isHeld()){
            partialWakeLock.release();
        }
        EventBus.getDefault().unregister(this);
        if (mBound) {
            getApplicationContext().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        Log.i(getClass().getSimpleName(), "onRetainCustom....");
        if(mBound) {
            return mConnection;
        } else{
            return super.onRetainCustomNonConfigurationInstance();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
//endregion

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //String eventName = KeyBinder.giveFileName(keyCode, event, getApplicationContext());
        String eventName = KeyBinder.getName(event);
        if (eventName != null && !eventName.isEmpty() && eventName.compareTo("-1") != 0) {
            Log.i(getClass().getSimpleName() + " onKeyUp", "Event name: " + eventName);
            //Now we should try play audio. If we are unable to do it then we shouldn't show picture
            //for now:
            mAudioService.setCurrentKey(eventName);
            Uri audioFileUri = Uri.parse("file://" + AUDIO_FOLDER + File.separator + AUDIO_FOLDER_PREFIX
                    + eventName + AUDIO_FILE_FORMAT);
            mAudioService.playSound(audioFileUri);
            return true;
        }else {
            Log.i(getClass().getSimpleName() + "onKeyUp", "No path to file or no suitable key " +
                    "was pressed ");
            return super.onKeyUp(keyCode, event);
        }
    }

    public void showPicture(String fileName) {
        final Uri uri = Uri.parse("file://" + MESSAGE_FOLDER + File.separator + fileName);
        File message = new File(uri.getPath());
        if(!message.exists()) {
            Log.i(getClass().getSimpleName(), "showPicture(). file " + uri.toString() + " doesn't " +
                    "exist");
            //we should fadeout previous picture if there was one
            if(ivPicMessage.getVisibility() == View.VISIBLE) {
                fadeOutImageView(ivPicMessage, null, false);
            }
            return;
        }
        Log.i(getClass().getSimpleName(), "showPicture(). File uri: " + uri.toString());
        if (!StorageUtil.isExternalStorageReadable()) {
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
        setBright(MESSAGE_BRIGHTNESS);
        makeKeepScreenOn();
        if(ivPicMessage.getVisibility() == View.VISIBLE) {
            //in this case we must fade out currently visible image, set new one and fade it in
            fadeOutImageView(ivPicMessage, uri, true);
        } else{
            //in this case we must fade in picture and then fade it out
            fadeInImageView(ivPicMessage, uri);
        }
    }

    /**
     * Creates a dir with the given path. Creates missing parent directories if necessary.
     * @param path Folder path.
     * @return Returns TRUE if dir was created. FALSE if there was an error or folder already exists.
     */
    public boolean createDir(String path) {
        File dir = new File(path);
        if(!StorageUtil.isExternalStorageWritable()) {
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

    public void hidePicture(ImageView imageView){
        fadeOutImageView(imageView, null, false);
    }

    /**
     * Fades ImageView in.
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
                imageView.setVisibility(View.VISIBLE);
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
        if(imageView.getVisibility() == View.VISIBLE) {
            if(fadeInAfter && imageUri != null) {
                //if we need to fade in after this animation (fade out -> change image -> start fade in)
                fadeOut.setDuration(200);
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
                fadeOut.setDuration(1000);
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
    }

    public void makeKeepScreenOn(){
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    public void dismissKeepScreenOn(){
        this.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

//region additional
    public void setBright(float value) {
        Window mywindow = getWindow();
        WindowManager.LayoutParams lp = mywindow.getAttributes();
        lp.screenBrightness = value;
        mywindow.setAttributes(lp);
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
//endregion

    @Subscribe
    public void onPlaybackEvent(PlaybackEvent event){
        switch (event.state) {
            case START_PLAYING:
                showPicture(mAudioService.getCurrentKeyProcessed() + MESSAGE_PIC_FORMAT);
                break;
            case STOP_PLAYING:
                hidePicture(ivPicMessage);
                break;
        }
    }

    private void connectToService() {
        // Start and bind to service if it wasn't done already:
        if(mConnection == null) {
            mConnection = new AudioServiceConnection();
            Intent intent = new Intent(this, mService.class);
            getApplicationContext().startService(intent);
            getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else{
            mAudioService = mConnection.mservice;
        }
    }
    private class AudioServiceConnection implements ServiceConnection {
        mService mservice;
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
            Log.i(getClass().getSimpleName(), "disconnected from service successfully");
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mService.LocalBinder binder = (mService.LocalBinder) iBinder;
            mservice = binder.getService();
            mAudioService = mservice;
            mBound = true;
            Log.i(getClass().getSimpleName(), "connected to service successfully "
                    + mAudioService.toString());
        }
    }
}
