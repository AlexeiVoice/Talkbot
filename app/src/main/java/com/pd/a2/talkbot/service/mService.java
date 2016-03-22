package com.pd.a2.talkbot.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.pd.a2.talkbot.util.State;
import com.pd.a2.talkbot.util.StorageUtil;
import com.pd.a2.talkbot.util.PlaybackEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

public class mService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private static float AUDIO_VOLUME = 1f;
    private MediaPlayer mediaPlayer;

    private String currentKey; //currently showed and playback'ed key-event
    private String nextKey; //next key-event to show and playback
    private State currentState;

    public mService() {
    }

//region Mandatory methods
    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public mService getService() {
            // Return this instance of LocalService so clients can call public methods
            return mService.this;
        }
    }
//endregion

    /**
     * Plays sound using given file name (just name in format 'name.mp3', path is generated
     * automatically)
     * @param uri Path to the file.
     * @return Returns TRUE if file exists and will be played, FALSE - if there's no such file or
     * storage is unreadable.
     */
    public boolean playSound(Uri uri) {
        //Uri uri = Uri.parse("file://" + AUDIO_FOLDER + File.separator + fileName);
        File audio = new File(uri.getPath());
        if(!audio.exists()) {
            Log.i(getClass().getSimpleName(), "playSound(). file " + uri.toString() + " doesn't " +
                    "exist");
            return false;
        }
        Log.i(getClass().getSimpleName(), "playSound(). File uri: " + uri.toString());

        if (!StorageUtil.isExternalStorageReadable()) {
            Log.i(getClass().getSimpleName(), "playSound(): external storage isn't readable");
            return false;
        }
        //we should stop playing audio-file if it's playing at the moment
        if (!checkAndstopSound()) {
            mediaPlayer = new MediaPlayer();
        }
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(AUDIO_VOLUME, AUDIO_VOLUME);
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            /*mediaPlayer.prepare();
            mediaPlayer.start();*/
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    currentState = State.START_PLAYING;
                    mediaPlayer.start();
                    notifyStateChanged();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.reset();
                    currentState = State.STOP_PLAYING;
                    notifyStateChanged();
                    currentKey = "-1";
                }
            });
            mediaPlayer.prepareAsync();
            return true;

        } catch (IOException | IllegalStateException | IllegalArgumentException |SecurityException
                e ) {
            Log.e(getClass().getSimpleName(), "playSound() error: " + e.toString());
            return false;
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
                    //also we should hide current picture
                    currentState = State.START_PLAYING;
                    notifyStateChanged();
                }
                mediaPlayer.reset();
            } catch (IllegalStateException e) {
                Log.e(getClass().getSimpleName(), "checkAndStopSound: " + e.toString());
                return false;
            }
            return true;
        }
    }

    public String getCurrentKeyProcessed() {
        return currentKey;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentKey(String keyPressed) {
        currentKey = keyPressed;
    }

    private void notifyStateChanged() {
        EventBus.getDefault().post(new PlaybackEvent(currentState));
    }

}
