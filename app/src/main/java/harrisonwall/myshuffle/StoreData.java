package harrisonwall.myshuffle;



import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Handles the storage and retrieval of music
 */
public class StoreData
{
    private String mLocation = "harrionwall.myshuffle.STORAGE";
    private SharedPreferences mPreferences;

    private FileOutputStream outputStream;
    private FileInputStream inputStream;
    private String fileName = "storage.DATA";

    private Context mContext;

    public StoreData(Context context)
    {
        mContext = context;
    }

    // Store an Artist List
    public void storeData(ArrayList<Artist> pArtistList)
    {
        Gson gson = new Gson();
        String json = gson.toJson(pArtistList);

        try
        {
            outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write( json.getBytes() );
            outputStream.close();
        }
        catch( FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Load the stores Artist List
    public ArrayList<Artist> loadData()
    {
        Gson gson = new Gson();
        String json = null;
        StringBuffer stringBuffer = new StringBuffer();
        byte[] byteBuffer = new byte[1024];
        int bytesRead;

        try
        {
            // Open data file and read to the buffer
            inputStream = mContext.openFileInput(fileName);

            while( (bytesRead = inputStream.read(byteBuffer)) != -1)
                stringBuffer.append( new String( byteBuffer, 0, bytesRead));

            inputStream.close();

            json = stringBuffer.toString();
        }
        catch( FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // If no list, make a new list
        if( json == null )
            return new ArrayList<Artist>();
        else
        {
            // Rebuild list from jason
            Type type = new TypeToken<ArrayList<Artist>>(){}.getType();
            return gson.fromJson(json, type);
        }
    }

    // Store the number of songs found when last checked
    public void storeLastCount(long pDate)
    {
        mPreferences = mContext.getSharedPreferences(mLocation, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = mPreferences.edit();

        prefEditor.putLong("lastCount", pDate);
        prefEditor.apply();
        prefEditor.commit();
    }

    // Load the number of songs found when last checked
    public long loadLastCount()
    {
        mPreferences = mContext.getSharedPreferences(mLocation, Context.MODE_PRIVATE);
        return mPreferences.getLong("lastCount", -1); // -1 if not data found
    }

    public void clearData()
    {
        // Clear date and index
        mPreferences = mContext.getSharedPreferences(mLocation, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = mPreferences.edit();
        prefEditor.clear();
        prefEditor.apply();
        prefEditor.commit();

        // Delete file
        mContext.deleteFile(fileName);
    }
}
