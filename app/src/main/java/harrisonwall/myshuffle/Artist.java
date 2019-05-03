package harrisonwall.myshuffle;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Artist contains the Name and Genre of an artist
 * as well as a List of the albums they have released.
 */
public class Artist
{
    private String mArtistName, mGenre;
    private ArrayList<Album> mAlbums;

    public Artist()
    {
        mArtistName = "Unknown Artist";
        mGenre = "Unknown Genre";
        mAlbums = new ArrayList<Album>();
    }

    public Artist(ArrayList<Album> pAlbums)
    {
        mArtistName = "Unknown Artist";
        mGenre = "Unknown Genre";
        mAlbums = pAlbums;
    }

    public Artist(String name, ArrayList<Album> pAlbums)
    {
        mArtistName = name;
        mGenre = "Unknown Genre";
        mAlbums = pAlbums;
    }

    public Artist(String name, String genre, ArrayList<Album> pAlbums)
    {
        mArtistName = name;
        mGenre = genre;
        mAlbums = pAlbums;
    }

    // Getters
    public String getName() {return mArtistName;}

    public String getGenre() {return mGenre;}

    public ArrayList<Album> getAlbums() {return mAlbums;}

    // Return all songs from all albums
    public ArrayList<Song> getSongs()
    {
        ArrayList<Song> retArray = new ArrayList<Song>();
        ArrayList<Song> tempArray;

        for( int i = 0; i < mAlbums.size(); i++ )
        {
            tempArray = new ArrayList<Song>();
            tempArray.addAll( mAlbums.get(i).getSongs() );
            Collections.sort( tempArray, new SortUtil.SongTrackNum());

            retArray.addAll( tempArray );
        }

        return retArray;
    }

    // Setters
    public void setName(String name) {mArtistName = name;}

    public void setGenre(String genre) {mGenre = genre;}

    public void setAlbums(ArrayList<Album> pAlbums) {mAlbums = pAlbums;}

    public void addAlbum(Album pAlbum) { mAlbums.add(pAlbum); }

    // Search for matching album in list
    public Album findAlbum( String albumName )
    {
        Album retVal = null;
        int i = 0;

        while( i < mAlbums.size() )
        {
            if( albumName.compareTo( mAlbums.get(i).getTitle() ) == 0  )
            {
                retVal = mAlbums.get(i);
                break;
            }

            i++;
        }

        return retVal;
    }
}
