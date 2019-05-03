package harrisonwall.myshuffle;

/*
 * Help with media service set up from
 * https://bit.ly/2kDz7Rg
 * https://bit.ly/2vj0OGe
 */

import android.app.Service;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener, AudioManager.OnAudioFocusChangeListener
{
    private ArrayList<Song> songsList;
    private int songPosition;
    private MediaPlayer mediaPlayer;

    // What gets passed to MediaBrowser clients
    private final IBinder musicBind = new MusicBinder();

    //----- Binding Functions -----\\
    @Override
    public IBinder onBind(Intent intent)
    {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return false;
    }

    public class MusicBinder extends Binder
    {
        MusicService getService() { return MusicService.this; }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        songPosition = 0;
        songsList = new ArrayList<Song>();
        mediaPlayer = new MediaPlayer();

        setUpPlayer();
    }

    @Override
    public void onDestroy()
    {
        if (mediaPlayer != null)
        {
            stopMusic();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onDestroy();
    }

    public void setUpPlayer()
    {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnInfoListener(this);

        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void runPlayer()
    {
        mediaPlayer.reset();

        // Get song Uri
        Uri songUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songsList.get(songPosition).getID() );

        try
        {
            mediaPlayer.setDataSource(getApplicationContext(), songUri);
        }
        catch(Exception e)
        {
            stopSelf();
        }

        try
        {
            mediaPlayer.prepare();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setSongList(ArrayList<Song> pSongs)
    {
        songsList = pSongs;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        mp.reset();
        String errorMessage = "";

        switch (what)
        {
            case MediaPlayer.MEDIA_ERROR_IO:
                errorMessage = "IO ERROR ";
                break;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                errorMessage = "NOT VALID ERROR";
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                errorMessage = "TIMED OUT ERROR ";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                errorMessage = "MALFORMED ERROR ";
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                errorMessage = "UNSUPPORTED ERROR ";
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                errorMessage = "UNKNOWN MEDIA ERROR ";
                break;
            default:
                errorMessage = "DEFAULT ERROR ";
                break;
        }

        Log.e("MusicService - ", errorMessage + extra);

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        playMusic();
    }

    public void setSong(int songIndex)
    {
        songPosition = songIndex;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra)
    {
        return false;
    }

    //-------- AUDIO MANAGER ---------\\
    @Override
    public void onAudioFocusChange(int focusChange)
    {
        switch (focusChange)
        {
            case AudioManager.AUDIOFOCUS_GAIN: // Start playing
                if( mediaPlayer == null )
                    setUpPlayer();
                playMusic();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS: // Release player
                stopMusic();
                mediaPlayer.release();
                mediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // Pause player
                pauseMusic();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: // Lower volume
                if( mediaPlayer.isPlaying() )
                    mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    //-------- MEDIA PLAYER METHODS -------\\
    public void playMusic()
    {
        if( mediaPlayer != null && !mediaPlayer.isPlaying() )
            mediaPlayer.start();
    }

    public void pauseMusic()
    {
        if( mediaPlayer != null && mediaPlayer.isPlaying())
        {
            mediaPlayer.pause();
        }
    }

    public void stopMusic()
    {
        if( mediaPlayer != null && mediaPlayer.isPlaying() )
            mediaPlayer.stop();
    }

    public int getDuration()
    {
        if( mediaPlayer != null )
            return  mediaPlayer.getDuration();

        return 0;
    }

    public boolean isPlaying()
    {
        if(mediaPlayer != null)
          return  mediaPlayer.isPlaying();

        return false;
    }

    public int getSeek()
    {
        if(mediaPlayer != null)
            return  mediaPlayer.getCurrentPosition();

        return 0;
    }

    public void setSeek(int seekLocation)
    {
        if( mediaPlayer != null )
            mediaPlayer.seekTo(seekLocation);
    }

    public void playPrev()
    {
        if( songPosition > 0 )
            songPosition--;

        runPlayer();
    }

    public void playNext()
    {
        if( songPosition < ( songsList.size() - 1) )
            songPosition++;

        runPlayer();
    }

    public int getSongPosition()
    {
        return songPosition;
    }

    public String getCurrentSong ()
    {
        return songsList.get(songPosition).getTitle();
    }

}
