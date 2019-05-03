package harrisonwall.myshuffle;

import android.content.Context;
import android.widget.MediaController;

/**
 * A MediaController that does not hide
  */
public class MusicController extends MediaController
{

    public MusicController(Context context)
    {
        super(context);
    }

    @Override
    public void hide()
    {
        // DO NOT HIDE THIS
    }
}
