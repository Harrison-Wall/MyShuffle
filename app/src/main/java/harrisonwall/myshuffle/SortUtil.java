package harrisonwall.myshuffle;

import java.util.Comparator;

// Container class for various comparators
public class SortUtil
{
    public SortUtil() {}

    // Compare Albums by name
    public static class AlbumName implements Comparator<Album>
    {
        @Override
        public int compare(Album album1, Album album2)
        {
            return album1.getTitle().compareTo( album2.getTitle() );
        }
    }

    // Compare albums by year
    public static class AlbumYear implements Comparator<Album>
    {
        @Override
        public int compare(Album album1, Album album2)
        {
            int retVal = 1;

            if( album1.getYearAsInt() < album2.getYearAsInt() )
                retVal =  -1;
            else if ( album1.getYearAsInt() == album2.getYearAsInt() )
                retVal =  0;

            return retVal;
        }
    }

    // Compare artists by genre
    public static class ArtistGenre implements Comparator<Artist>
    {
        @Override
        public int compare(Artist art1, Artist art2)
        {
            return art1.getGenre().compareTo( art2.getGenre() );
        }
    }

    // Compare artists by name
    public static class ArtistName implements Comparator<Artist>
    {
        @Override
        public int compare(Artist art1, Artist art2)
        {
            return art1.getName().compareTo( art2.getName() );
        }
    }

    // Compare songs by name
    public static class SongName implements Comparator<Song>
    {
        @Override
        public int compare(Song song1, Song song2)
        {
            return song1.getTitle().compareTo( song2.getTitle() );
        }
    }

    // Compare songs by number order
    public static class SongTrackNum implements Comparator<Song>
    {
        @Override
        public int compare(Song song1, Song song2)
        {
            int retVal = 1;

            if( song1.getTrackNumber() < song2.getTrackNumber() )
                retVal = -1;
            else if ( song1.getTrackNumber() == song2.getTrackNumber() )
                retVal =  0;

            return retVal;
        }

    }

}
