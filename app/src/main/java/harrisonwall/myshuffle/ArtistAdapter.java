package harrisonwall.myshuffle;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ArtistAdapter extends ArrayAdapter<Artist>
{
    public ArtistAdapter(Context context, ArrayList<Artist> artists)
    {
        super(context, 0, artists);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View artistView = convertView;
        if( artistView == null )
            artistView = LayoutInflater.from(getContext()).inflate(R.layout.artist, parent, false );

        // Fill artist name
        Artist currentArtist = getItem(position);
        TextView name = (TextView) artistView.findViewById(R.id.artist_name);
        name.setText(currentArtist.getName() );

        //Fill artist genre
        TextView genre = (TextView) artistView.findViewById(R.id.artist_genre);
        genre.setText( currentArtist.getGenre() );

        return artistView;

    }
}
