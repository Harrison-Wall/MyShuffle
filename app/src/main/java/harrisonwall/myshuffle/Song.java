package harrisonwall.myshuffle;

public class Song {

    // Metadata about a song
    private String mTitle;
    private int mTrackNumber, mDiscNumber, mDuration;
    private long mID;
    private boolean mPlaying;

    public Song() {
        mTitle = "Unknown Title";

        mTrackNumber = 1;
        mDiscNumber = 1;
        mDuration = 1000;
        mPlaying = false;
    }

    public Song(long ID, String title, int trackNum, int discNum, int length) {
        mID = ID;
        mTitle = title;

        mTrackNumber = trackNum;
        mDiscNumber = discNum;
        mDuration = length;
        mPlaying = false;
    }

    public Song(long ID, String title, String artist) {
        mID = ID;
        mTitle = title;

        mTrackNumber = 0;
        mDiscNumber = 0;
        mDuration = 0;
        mPlaying = false;
    }

    // Getters

    public long getID() {
        return mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getTrackNumber() {
        return mTrackNumber;
    }

    public int getDiscNumber() {
        return mDiscNumber;
    }

    public int getDuration() {
        return mDuration;
    }

    public boolean isPlaying() {
        return mPlaying;
    }


    // Setters
    public void setID(long ID) {
        mID = ID;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setTrackNumber(int trackNum) {
        mTrackNumber = trackNum;
    }

    public void setDiscNumber(int discNum) {
        mDiscNumber = discNum;
    }

    public void setDuration(int length) {
        mDuration = length;
    }

    public void togglePLaying() {
        mPlaying = !mPlaying;
    }

    public void setIsPlaying() {
        mPlaying = true;
    }

    public void setNotPlaying() {
        mPlaying = false;
    }

}
