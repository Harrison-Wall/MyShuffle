package harrisonwall.myshuffle;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class SongsFragment extends Fragment
{
    private ArrayList<Song> songList = new ArrayList<Song>();
    private MusicViewModel model;
    private SongAdapter sAdapter;
    private int currSongPos;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get song list from ViewModel
        model = ViewModelProviders.of(getActivity()).get(MusicViewModel.class);
        model.getMusic().observe(this, allArtists -> {
            songList = model.getAllSongs();

            // Put in name order
            Collections.sort(songList, new SortUtil.SongName());

            // If the adapter has been set, add the songs
            if( sAdapter != null )
            {
                sAdapter.clear();
                sAdapter.addAll(songList);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.song_list, container ,false);
        sAdapter = new SongAdapter(getActivity(), songList);
        final ListView listView = (ListView) root.findViewById(R.id.song_list);
        listView.setAdapter(sAdapter);

        // Handle song is clicked on
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Add all songs to the now playing list
                ArrayList<Song> temp = new ArrayList<Song>();
                temp.addAll( songList );

                // The clicked on song should be played first
                model.setPosition(position);
                model.setNowPlayingList( temp );

                // Go to NowPlaying Fragment
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                viewPager.setCurrentItem(0);
            }
        });

        // Handle song long click
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Remember which song was clicked and show options menu
                currSongPos = position;
                shuffleAlert().show();
                return true;
            }
        });

        return root;
    }

    // Builds a dialog menu with song playback options
    public AlertDialog shuffleAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.shuffle_song, null );

        builder.setView(dialogView)
                .setTitle(R.string.play_options)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.play, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // Get radio button input
                        RadioButton thisSongBtn = (RadioButton) dialogView.findViewById(R.id.shuffle_song_this_song);
                        RadioButton shuffleSongsBtn = (RadioButton) dialogView.findViewById(R.id.shuffle_song_shuffle_all);

                        ArrayList<Song> shuffledList = new ArrayList<Song>();

                        if( thisSongBtn.isChecked() ) // Only play this song
                            shuffledList.add( songList.get( currSongPos ) );
                        else
                        {
                            // Play all songs
                            shuffledList.addAll( songList );

                            if( shuffleSongsBtn.isChecked() ) // Shuffle all songs
                                Collections.shuffle(shuffledList, new Random( System.currentTimeMillis() ));
                        }

                        // Set Now Playing List
                        model.setPosition(0);
                        model.setNowPlayingList(shuffledList);

                        dialog.dismiss();

                        // Go to NowPlaying Fragment
                        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                        viewPager.setCurrentItem(0);
                    }
                });

        return builder.create();
    }
}
