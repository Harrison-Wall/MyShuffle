package harrisonwall.myshuffle;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AlbumAdapter extends ArrayAdapter<Album> {

    public AlbumAdapter(Context context, ArrayList<Album> albums) {
        super(context, 0, albums);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View albumView = convertView;
        if (albumView == null)
            albumView = LayoutInflater.from(getContext()).inflate(R.layout.album, parent, false);

        // Fill Data
        Album currentAlbum = getItem(position);

        TextView albumName = (TextView) albumView.findViewById(R.id.album_name);
        albumName.setText(currentAlbum.getTitle());

        TextView artistName = (TextView) albumView.findViewById(R.id.album_year);
        artistName.setText("Released: " + currentAlbum.getYear());

        // Check for album art
        if (currentAlbum.getSongs().size() > 0) {
            Uri songUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentAlbum.getSongs().get(0).getID());

            MediaMetadataRetriever mmdRet = new MediaMetadataRetriever();
            mmdRet.setDataSource(getContext(), songUri);

            // If there is art, change the default image
            if (mmdRet.getEmbeddedPicture() != null) {
                byte[] byteCover = mmdRet.getEmbeddedPicture();

                ImageView art = (ImageView) albumView.findViewById(R.id.album_art);
                art.setImageBitmap(BitmapFactory.decodeByteArray(byteCover, 0, byteCover.length));
            }
        }

        return albumView;
    }
}
