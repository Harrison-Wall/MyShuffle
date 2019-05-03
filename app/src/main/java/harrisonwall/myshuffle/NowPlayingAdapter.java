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

public class NowPlayingAdapter extends ArrayAdapter<Song>
{
    public NowPlayingAdapter(Context context, ArrayList<Song> songs)
    {
        super(context, 0, songs);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View songView = convertView;
        if( songView == null )
            songView = LayoutInflater.from(getContext()).inflate(R.layout.song, parent, false );

        Song currentSong = getItem(position);

        // Fill song name
        TextView title = (TextView) songView.findViewById(R.id.title);
        title.setText(currentSong.getTitle());

        // Only show playing icon if the song is currently playing
        if( currentSong.isPlaying() )
            songView.findViewById(R.id.play_button).setVisibility(View.VISIBLE);
        else
            songView.findViewById(R.id.play_button).setVisibility(View.GONE);

        return songView;
    }
}
