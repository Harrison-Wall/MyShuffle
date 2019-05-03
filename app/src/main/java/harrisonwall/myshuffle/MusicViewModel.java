package harrisonwall.myshuffle;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class MusicViewModel extends AndroidViewModel
{
    private MutableLiveData<ArrayList<Artist>> allArtists;
    private MutableLiveData<ArrayList<Song>> nowPlayingList;
    private MutableLiveData<Integer> mPosition;
    private StoreData storage;

    public MusicViewModel(@NonNull Application application)
    {
        super(application);
        storage = new StoreData(application);
    }

    // Return the position of the song to play in the now playing list
    public LiveData<Integer> getPosition()
    {
        if( mPosition == null) // If it has not been set
        {
            mPosition = new MutableLiveData<Integer>();
            mPosition.setValue(new Integer(0));
        }

        return mPosition;
    }

    // Set position of the song to play
    public void setPosition(int position)
    {
        mPosition.setValue( position );
    }

    public LiveData<ArrayList<Song>> getNowPlayingList()
    {
        if( nowPlayingList == null )
        {
            nowPlayingList = new MutableLiveData<ArrayList<Song>>();
            nowPlayingList.setValue(new ArrayList<Song>());
        }
        return nowPlayingList;
    }

    public void setNowPlayingList(ArrayList<Song> songList)
    {
        if( nowPlayingList == null )
        {
            nowPlayingList = new MutableLiveData<ArrayList<Song>>();
            nowPlayingList.setValue(new ArrayList<Song>());
        }

        // Make sure the playing icon does not show up at first
        for( int i  = 0; i < songList.size(); i++ )
            songList.get(i).setNotPlaying();

        nowPlayingList.setValue(songList);
    }

    // Return the stored list of artists
    public LiveData<ArrayList<Artist>> getMusic(){
        if(allArtists == null)
        {
            allArtists = new MutableLiveData<ArrayList<Artist>>();
            allArtists.setValue(new ArrayList<Artist>());

            loadArtists();
        }

        return allArtists;
    }

    // Return all artists albums
    public ArrayList<Album> getAllAlbums(){
        ArrayList<Album> retArrayList = new ArrayList<Album>();

        for( int i = 0; i < allArtists.getValue().size(); i++  )
            retArrayList.addAll( allArtists.getValue().get(i).getAlbums() ) ;

        return retArrayList;
    }

    // Return all artists songs
    public ArrayList<Song> getAllSongs(){
        ArrayList<Song> retArrayList = new ArrayList<Song>();

        for( int i = 0; i < allArtists.getValue().size(); i++  )
            for( int j = 0; j < allArtists.getValue().get(i).getAlbums().size(); j++ )
                retArrayList.addAll( allArtists.getValue().get(i).getAlbums().get(j).getSongs() ) ;

        return retArrayList;
    }

    // Load the artists from storage
    @SuppressLint("StaticFieldLeak")
    public void  loadArtists() {
        new AsyncTask<Void, Void, ArrayList<Artist>>() {
            @Override
            protected ArrayList<Artist> doInBackground(Void...voids)
            {
                ArrayList<Artist> retArray = null;
                long currCount = 0;
                long lastCount;

                // Load Data from JSON
                retArray = storage.loadData();
                lastCount = storage.loadLastCount();

                ContentResolver musicResolver = getApplication().getContentResolver();
                Uri musicURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                // Query for music
                String selectMusic = MediaStore.Audio.Media.IS_MUSIC + " != 0 ";
                Cursor musicCursor = musicResolver.query(musicURI, null, selectMusic, null, null);

                // Count the number of songs found
                if( musicCursor != null && musicCursor.moveToFirst() )
                    currCount = musicCursor.getCount();

                // If there has been a change remake the array list
                if( currCount != lastCount )
                {
                    retArray = new ArrayList<Artist>();
                    storage.storeLastCount(currCount);

                    Song tempSong = null;
                    Album tempAlbum = null;
                    boolean newArtist = false;

                    String genre = "Unknown Genre";

                    //get columns
                    int titleColumn = musicCursor.getColumnIndex (MediaStore.Audio.Media.TITLE);
                    int idColumn = musicCursor.getColumnIndex (MediaStore.Audio.Media._ID);
                    int artistColumn = musicCursor.getColumnIndex (MediaStore.Audio.Media.ARTIST);
                    int albumCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                    int yearCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
                    int durationCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                    int trackCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);

                    //add songs to list
                    do
                    {
                        Artist tempArtist = null;

                        // Get column info
                        long ID = musicCursor.getLong(idColumn);
                        String title = musicCursor.getString(titleColumn);
                        String artistName = musicCursor.getString(artistColumn);
                        String albumName = musicCursor.getString(albumCol);
                        String year = musicCursor.getString(yearCol);
                        int duration = musicCursor.getInt(durationCol);
                        int trackNumber = musicCursor.getInt(trackCol);

                        // Add info to new song
                        tempSong = new Song();
                        tempSong.setID(ID);
                        tempSong.setTitle(title);
                        tempSong.setDuration(duration);
                        tempSong.setTrackNumber(trackNumber);

                        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ID);

                        Log.d("Loading: ", title);

                        // Get song METADATA
                        MediaMetadataRetriever mmdRet = new MediaMetadataRetriever();;
                        if( songUri != null )
                        {
                            mmdRet.setDataSource(getApplication(), songUri);

                            // Get genre if it exists
                            if (mmdRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) != null)
                                genre = mmdRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

                            // Get disc number if it exists
                            if (mmdRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER) != null)
                            {
                                int discNumber = Integer.parseInt(mmdRet.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER));
                                tempSong.setDiscNumber(discNumber);
                            }
                        }

                        // Check for matching Artist
                        int i = 0;
                        while( i < retArray.size() )
                        {
                            // match found
                            if( artistName.compareTo( retArray.get(i).getName() ) == 0 )
                            {
                                newArtist = false;
                                tempArtist = retArray.get(i);
                                break;
                            }

                            i++;
                        }

                        // If no match found make a new artist
                        if( tempArtist == null )
                        {
                            tempArtist = new Artist();
                            tempArtist.setName( artistName);
                            tempArtist.setGenre( genre );
                            newArtist = true;
                        }

                        // Check for matching album
                        tempAlbum = tempArtist.findAlbum( albumName );

                        // If not match found make a new album
                        if( tempAlbum == null )
                        {
                            tempAlbum = new Album();
                            tempAlbum.setYear(year);
                            tempAlbum.setArtist(artistName);
                            tempAlbum.setTitle(albumName);

                            tempAlbum.addSong(tempSong);
                            tempArtist.addAlbum(tempAlbum);
                        }
                        else // otherwise add the song to the existing album
                            tempAlbum.addSong(tempSong);

                        Collections.sort(tempAlbum.getSongs(), new SortUtil.SongTrackNum());

                        // If it was a new artist add it to the list
                        if( newArtist )
                            retArray.add( tempArtist );

                    }
                    while (musicCursor.moveToNext());

                    // Save the updated data
                    musicCursor.close();
                    storage.storeData(retArray);
                }

                return retArray;
            }

            @Override
            protected void onPostExecute(ArrayList<Artist> data){
                allArtists.setValue(data);
            }

        }.execute();
    }
}