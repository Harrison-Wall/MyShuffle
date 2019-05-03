package harrisonwall.myshuffle;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class TabAdapter extends FragmentPagerAdapter
{
    private Context mContext;

    public TabAdapter(Context context, FragmentManager fm)
    {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int pos)
    {
        // Now playing is 0 so the media controller shows when first started
        switch (pos)
        {
            case 1:
                return new SongsFragment();
            case 2:
                return new AlbumFragment();
            case 3:
                return new ArtistFragment();
            default: // 0
                return new NowPlayingFragment();
        }
    }

    @Override
    public int getCount()
    {
        return 4;
    }

    @Override
    public int getItemPosition(Object object)
    {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int pos)
    {
        switch (pos)
        {
            case 1:
                return mContext.getString(R.string.tab_songs);
            case 2:
                return mContext.getString(R.string.tab_albums);
            case 3:
                return mContext.getString(R.string.tab_artists);
            default: // 0
                return mContext.getString(R.string.tab_now);
        }
    }
}