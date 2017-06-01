package show.server.mcv.com.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URLConnection;

import show.server.mcv.com.Globals;

/**
 * Created by Matthew on 12/22/2016.
 */

public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

    public static String TAG = ImageDownloader.class.getSimpleName();
    ImageView mBitmapImage;

    public interface AsyncResponse {
        void processFinish(Bitmap output);
    }

    public AsyncResponse delegate = null;

    public ImageDownloader(ImageView bmImage) {
        this.mBitmapImage = bmImage;
    }

    public ImageDownloader(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    public ImageDownloader(ImageView bmImage, AsyncResponse delegate) {
        this.mBitmapImage = bmImage;
        this.delegate = delegate;
    }

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap bitmap = null;
        try {
            URLConnection connection = new java.net.URL(url).openConnection();
            InputStream in = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(in);
            if (delegate == null && Globals.bitmapCache != null) {
                Globals.bitmapCache.put(url, bitmap);
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        if(mBitmapImage !=null) {
            mBitmapImage.setImageBitmap(result);
        }

        if (delegate != null) {
            delegate.processFinish(result);
        }
    }
}