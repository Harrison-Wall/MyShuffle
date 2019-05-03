package harrisonwall.myshuffle;

/*
 * Help with media controller set up from
 * https://bit.ly/2kDz7Rg
 * https://bit.ly/2vj0OGe
 */

import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;

import java.util.ArrayList;

public class NowPlayingFragment extends Fragment implements MediaController.MediaPlayerControl
{
    private ArrayList<Song> songList = new ArrayList<Song>();
    private MusicViewModel model;
    private NowPlayingAdapter songAdapter = null;

    private MusicService musicService;
    private Intent bindServiceIntent;
    private boolean serviceBinded = false;

    private MusicController musicController;

    private TextView songTitle;
    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setUpController();

        songTitle = (TextView) getActivity().findViewById(R.id.song_title);

        // Get now playing list from view model
        model = ViewModelProviders.of( getActivity() ).get( MusicViewModel.class );
        model.getNowPlayingList().observe(this, nowPlayingList -> {

            // If adapter is set, update
            if( songAdapter != null )
            {
                songAdapter.clear();
                songAdapter.addAll(nowPlayingList);

                // Show playing icon on first song
                if( nowPlayingList.size() > 0 )
                    songAdapter.getItem( model.getPosition().getValue() ).togglePLaying();

                // Bring focus to selected song
                songAdapter.notifyDataSetChanged();
                listView.setSelection( model.getPosition().getValue() );
            }

            // If music service is set, add songs and play
            if( musicService != null )
            {
                musicService.setSong( model.getPosition().getValue() );
                musicService.setSongList(nowPlayingList);
                songTitle.setText( musicService.getCurrentSong() );
                musicService.runPlayer();
                musicController.show(0);
            }

            songList = nowPlayingList;
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.now_playing, container, false);
        songAdapter = new NowPlayingAdapter(getActivity(), songList);
        listView = (ListView) root.findViewById(R.id.now_playing_list_view);
        listView.setAdapter(songAdapter);

        // Handle song is clicked on
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id )
            {
                // Remove playing Icon from previous song, add it to clicked song
                songAdapter.getItem( model.getPosition().getValue() ).togglePLaying();
                songAdapter.getItem(position).togglePLaying();
                songAdapter.notifyDataSetChanged();
                listView.setSelection(position);

                // Play the clicked on song
                musicService.setSong(position);
                model.setPosition(position);
                songTitle.setText( musicService.getCurrentSong() );
                musicService.runPlayer();
                musicController.show(0);
            }
        });

        return root;
    }

    //----------- MEDIA SERVICE BINDING -----------\\
    @Override
    public void onStart()
    {
        super.onStart();
        if(bindServiceIntent == null)
        {
            // Bind and start the music service
            bindServiceIntent = new Intent(getContext(), MusicService.class);

            getActivity().getApplication().bindService(bindServiceIntent, servConn, Context.BIND_AUTO_CREATE);
            getActivity().getApplication().startService(bindServiceIntent);
        }
    }

    @Override
    public void onDestroy()
    {
        musicController = null;

        // Unbind and stop the music service
        if(serviceBinded)
        {
            getActivity().getApplication().unbindService(servConn);
            getActivity().getApplication().stopService(bindServiceIntent);

            musicService.stopSelf();
            musicService = null;
        }

        super.onDestroy();
    }

    // Connect to the music service
    private ServiceConnection servConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder)
        {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)iBinder;

            musicService = binder.getService();
            serviceBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            musicService = null;
            serviceBinded = false;
        }

    };

    //--- MEDIA PLAYER CONTROL ---\\
    private void setUpController()
    {
        musicController = new MusicController( getContext() );

        musicController.setPrevNextListeners(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    playNext();
                }
            }
            , new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    playPrev();
                }
            }
        );

        musicController.setMediaPlayer(this);
        musicController.setAnchorView( getActivity().findViewById(R.id.main_layout) );
        musicController.setEnabled(true);
        musicController.show(0); // 0 so it doesn't auto hide
    }

    @Override
    public void start()
    {
        musicService.playMusic();
        musicController.show(0);
    }

    @Override
    public void pause()
    {
        musicService.pauseMusic();
        musicController.show(0);
    }

    @Override
    public int getDuration()
    {
        if( musicService != null && serviceBinded )
            return musicService.getDuration();

        return 0;
    }

    @Override
    public int getCurrentPosition()
    {
        if( musicService != null && serviceBinded )
            return musicService.getSeek();

        return 0;
    }

    @Override
    public void seekTo(int pos)
    {
        if( musicService != null && serviceBinded )
            musicService.setSeek(pos);
    }

    @Override
    public boolean isPlaying()
    {
        if( musicService != null && serviceBinded && musicService.isPlaying() )
            return true;

        return false;
    }

    @Override
    public int getBufferPercentage()
    {
        return 0;
    }

    @Override
    public boolean canPause()
    {
        return true;
    }

    @Override
    public boolean canSeekBackward()
    {
        return true;
    }

    @Override
    public boolean canSeekForward()
    {
        return true;
    }

    @Override
    public int getAudioSessionId()
    {
        return 0;
    }

    private void playNext()
    {
        // Make sure there is a song to play
        if( songList.size() > 0 )
        {
            // Remove playing icon from current song
            songAdapter.getItem( model.getPosition().getValue() ).togglePLaying();

            musicService.playNext();

            // Update model and adapter with song and icon
            model.setPosition(musicService.getSongPosition());
            songTitle.setText( musicService.getCurrentSong() );
            songAdapter.getItem( model.getPosition().getValue() ).togglePLaying();
            songAdapter.notifyDataSetChanged();

            listView.setSelection( model.getPosition().getValue() );

            musicController.show(0);
        }
    }

    private void playPrev()
    {
        // Make sure there is a song to play
        if( songList.size() > 0 )
        {
            // Remove playing icon from current song
            songAdapter.getItem( model.getPosition().getValue() ).togglePLaying();

            musicService.playPrev();

            // Update model and adapter with song and icon
            model.setPosition(musicService.getSongPosition());
            songTitle.setText( musicService.getCurrentSong() );
            songAdapter.getItem( model.getPosition().getValue() ).togglePLaying();
            songAdapter.notifyDataSetChanged();

            listView.setSelection( model.getPosition().getValue() );

            musicController.show(0);
        }
    }
}
