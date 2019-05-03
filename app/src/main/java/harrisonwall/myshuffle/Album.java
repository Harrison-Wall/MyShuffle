package harrisonwall.myshuffle;

import java.util.ArrayList;
import java.util.Base64;

public class Album
{
    private String mTitle, mArtist, sYear;
    private int iYear;
    private ArrayList<Song> mSongs;

    public Album()
    {
        mTitle = "Unkown Title";
        mArtist = "Unknown Artist";
        sYear = "Unkown Year";
        iYear = 1900;
        mSongs = new ArrayList<Song>();
    }

    public Album(String title, String artist, String year)
    {
        mTitle = title;
        mArtist = artist;
        sYear = year;
        iYear = Integer.parseInt(year);
        mSongs = new ArrayList<Song>();
    }

    public Album(String title, String artist, String year, byte[] cover)
    {
        mTitle = title;
        mArtist = artist;
        sYear = year;
        iYear = Integer.parseInt(year);
        mSongs = new ArrayList<Song>();
    }

    // Getters
    public String getTitle() {return mTitle;}

    public String getArtist() { return mArtist; }

    public String getYear() {return sYear;}

    public int getYearAsInt() {return iYear;}

    public ArrayList<Song> getSongs() {return mSongs;}

    // Setters
    public void addSong (Song pSong) { mSongs.add(pSong); }

    public void setTitle(String title) {mTitle = title;}

    public void setArtist(String artist) { mArtist = artist; }

    public void setSongs(ArrayList<Song> tracks) {mSongs = tracks;}

    public void setYear(String year)
    {
        if( year != null )
        {
            sYear = year;
            iYear = Integer.parseInt(year);
        }
    }
}
