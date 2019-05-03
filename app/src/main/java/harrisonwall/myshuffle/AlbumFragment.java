package harrisonwall.myshuffle;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class AlbumFragment extends Fragment
{
    private ArrayList<Album> albums = new ArrayList<Album>();;
    private MusicViewModel model;
    private AlbumAdapter albumAdapter;
    private int currentAlbumPos;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get albums from ViewModel
        model = ViewModelProviders.of(getActivity()).get(MusicViewModel.class);
        model.getMusic().observe(this, allArtists -> {
            albums = model.getAllAlbums();

            // Put albums in alphabetical order
            Collections.sort(albums, new SortUtil.AlbumName());

            // If adapter is already set update with new albums
            if( albumAdapter != null )
            {
                albumAdapter.clear();
                albumAdapter.addAll(albums);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.album_list, container ,false);
        albumAdapter = new AlbumAdapter(getActivity(), albums);
        final ListView listView = (ListView) root.findViewById(R.id.album_list);
        listView.setAdapter(albumAdapter);

        // Slow how fast the user can scroll
        listView.setFriction(ViewConfiguration.getScrollFriction() * 5000);

        // Handle album is clicked on
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ArrayList<Song> temp = new ArrayList<Song>();
                temp.addAll( albums.get(position).getSongs() );

                // Build up now playing list
                model.setNowPlayingList( temp );
                model.setPosition(0);

                // Go to NowPlaying Fragment
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                viewPager.setCurrentItem(0);
            }
        });

        // Long click brings up playback menu
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                currentAlbumPos = position; // Remember which album was clicked on
                shuffleAlert().show();
                return true;
            }
        });

        return root;
    }

    // Builds dialog menu with playback options
    public AlertDialog shuffleAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.shuffle_album, null );

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
                        // Get radio and check button input
                        RadioButton thisAlbumBtn = (RadioButton) dialogView.findViewById(R.id.shuffle_albums_this_album);
                        RadioButton allAlbumNameBtn = (RadioButton) dialogView.findViewById(R.id.shuffle_albums_all_by_name);

                        CheckBox songsCheck = (CheckBox) dialogView.findViewById(R.id.shuffle_album_songs);

                        ArrayList<Song> shuffledSongs = new ArrayList<Song>();

                        if( thisAlbumBtn.isChecked() ) // Only this album
                        {
                            shuffledSongs.addAll( albums.get( currentAlbumPos ).getSongs() );

                            if( songsCheck.isChecked() ) // Shuffle songs in the album
                                Collections.shuffle(shuffledSongs, new Random( System.currentTimeMillis() ));
                        }
                        else // All albums
                        {
                            ArrayList<Album> sortedAlbums = new ArrayList<Album>();
                            sortedAlbums.addAll( albums );

                            if( allAlbumNameBtn.isChecked() ) // Sort By Name
                                Collections.sort(sortedAlbums, new SortUtil.AlbumName() );
                            else // Sort By Year
                                Collections.sort(sortedAlbums, new SortUtil.AlbumYear() );

                            // Add each albums songs to list
                            for( int i = 0; i < sortedAlbums.size(); i++ )
                            {
                                if( songsCheck.isChecked() ) // Shuffle each album's songs
                                    Collections.shuffle( sortedAlbums.get(i).getSongs(), new Random( System.currentTimeMillis() ));

                                shuffledSongs.addAll( sortedAlbums.get(i).getSongs() );
                            }
                        }

                        // Set now playing
                        model.setPosition(0);
                        model.setNowPlayingList( shuffledSongs );

                        dialog.dismiss();

                        // Go to NowPlaying Fragment
                        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                        viewPager.setCurrentItem(0);
                    }
                });

        return builder.create();
    }
}
