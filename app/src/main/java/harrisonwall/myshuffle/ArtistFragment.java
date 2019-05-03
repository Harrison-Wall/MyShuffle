package harrisonwall.myshuffle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ArtistFragment extends Fragment
{
    private ArrayList<Artist> artists = new ArrayList<Artist>();
    private MusicViewModel model;
    private ArtistAdapter artistAdapter;
    private int currArtistPos;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get artists from ViewModel
        model = ViewModelProviders.of(getActivity()).get(MusicViewModel.class);
        model.getMusic().observe(this, allArtists -> {
            artists = allArtists;
            Collections.sort(artists, new SortUtil.ArtistName());

            // If adapter is set, update with artists
            if( artistAdapter != null )
            {
                artistAdapter.clear();
                artistAdapter.addAll(artists);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.artist_list, container ,false);
        artistAdapter = new ArtistAdapter(getActivity(), artists);
        final ListView listView = (ListView) root.findViewById(R.id.artist_list);
        listView.setAdapter(artistAdapter);

        // Handle artist is clicked on
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Get songs from the clicked on artist
                ArrayList<Song> temp = new ArrayList<Song>();
                temp.addAll( artists.get(position).getSongs() );

                // Build the now playing list and go to the now playing fragment
                model.setNowPlayingList( temp );
                model.setPosition(0);

                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                viewPager.setCurrentItem(0);
            }
        });

        // Long click brings up playback menu
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                currArtistPos = position; // Remember which artist was clicked on
                shuffleAlert().show();
                return true;
            }
        });

        return root;
    }

    // Builds the playback options menu dialog
    public AlertDialog shuffleAlert()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        LayoutInflater layoutInflater = requireActivity().getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.shuffle_artist, null );

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
                        // Get radiobutton and checkbox input
                        RadioButton thisArtistBtn = (RadioButton) dialogView.findViewById(R.id.shuffle_artists_this_artist);
                        RadioButton allArtistsNameBtn = (RadioButton) dialogView.findViewById(R.id.shuffle_artists_all_by_name);

                        CheckBox songsCheck = (CheckBox) dialogView.findViewById(R.id.shuffle_artist_songs);
                        CheckBox albumsCheck = (CheckBox) dialogView.findViewById(R.id.shuffle_artist_albums);

                        ArrayList<Song> shuffledSongs = new ArrayList<Song>();

                        if( thisArtistBtn.isChecked() ) // Only the artist clicked on
                        {
                            Artist thisArtist = new Artist();

                            //Want a copy, not to change the artist list
                            thisArtist.setName( artists.get(currArtistPos).getName() );
                            thisArtist.setGenre( artists.get(currArtistPos).getGenre() );
                            thisArtist.getAlbums().addAll( artists.get(currArtistPos).getAlbums() );

                            if( albumsCheck.isChecked() ) // Shuffle albums
                                Collections.shuffle( thisArtist.getAlbums(), new Random( System.currentTimeMillis() ));

                            shuffledSongs.addAll( thisArtist.getSongs() );

                            if( songsCheck.isChecked() ) // shuffle songs
                                Collections.shuffle(shuffledSongs, new Random( System.currentTimeMillis() ));
                        }
                        else // All artists
                        {
                            ArrayList<Artist> sortedArtists = new ArrayList<Artist>();
                            sortedArtists.addAll(artists);

                            if( allArtistsNameBtn.isChecked() ) // Sorted by Name
                                Collections.sort(sortedArtists, new SortUtil.ArtistName() );
                            else // Sorted by Genre
                                Collections.sort(sortedArtists, new SortUtil.ArtistGenre() );

                            // Get songs from all artists
                            for( int i = 0; i < sortedArtists.size(); i++ )
                            {
                                if( albumsCheck.isChecked() ) // Shuffle albums
                                    Collections.shuffle( sortedArtists.get(i).getAlbums(), new Random( System.currentTimeMillis()) );

                                shuffledSongs.addAll( sortedArtists.get(i).getSongs() );

                                if( songsCheck.isChecked() ) // Shuffle songs
                                    Collections.shuffle( sortedArtists.get(i).getSongs() );
                            }
                        }

                        // Set Now Playing Lists
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
