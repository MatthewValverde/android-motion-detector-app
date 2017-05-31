package show.server.mcv.com;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import motiontracker.mcv.com.R;
import show.server.mcv.com.loaders.BitmapLoader;
import show.server.mcv.com.loaders.ListLoader;

public class SlideShowActivity extends AppCompatActivity {

    private static String SAVED_POSITION_TAG = "SlideShowActivity.SAVED_POSITION_TAG";
    public static final String SHARED_PREFERENCES = "SlideShowActivity.SHARED_PREFERENCES";
    private static final long SLIDE_SHOW_TIME_AMOUNT = 9000;
    private static final int sDistanceMatrixLoaderId = 0x060606;
    private static final int sBitmapLoaderId = 0x090909;
    private LoaderCallbacks<Drawable> imageResultLoaderListener;

    private String[] mList;
    private String mMainDirectoryName = "";
    private SlideShowTimerTask mSlideShowTimerTask;
    private int mSlideShowCounter;
    private Timer mSlideShowTimer;

    private List<String> mSortingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show);

        /*if (savedInstanceState != null) {
            mSlideShowCounter = savedInstanceState.getInt(SAVED_POSITION_TAG);
        }*/

        mMainDirectoryName = getIntent().getStringExtra("DIR_NAME");
        SAVED_POSITION_TAG = mMainDirectoryName;

        mSlideShowCounter = getPositionSharedPrefs();

       // System.out.println("mSlideShowCounter: " + mSlideShowCounter);

        LoaderCallbacks<String[]> listResultLoaderListener = new LoaderCallbacks<String[]>() {

            @Override
            public Loader<String[]> onCreateLoader(int id, Bundle args) {
                return new ListLoader(getApplicationContext(),
                        MainUrl.string + "list.php?dir=" + mMainDirectoryName);
            }

            @Override
            public void onLoadFinished(Loader<String[]> loader, String[] data) {
                /*for (int i = 0; i < data.length; i++) {
                   // System.out.println(data[i]);
                }*/

                mList = data;

                mSortingList = new ArrayList<String>(Arrays.asList(mList));

                Collections.sort(mSortingList, String.CASE_INSENSITIVE_ORDER);

                //getLoaderManager().initLoader(sBitmapLoaderId, null, imageResultLoaderListener);

                // Destroy loader by Id.
                getLoaderManager().destroyLoader(sDistanceMatrixLoaderId);
            }

            @Override
            public void onLoaderReset(Loader<String[]> loader) {

            }
        };

        imageResultLoaderListener = new LoaderCallbacks<Drawable>() {

            @Override
            public Loader<Drawable> onCreateLoader(int id, Bundle args) {
                return new BitmapLoader(getApplicationContext(),
                        "http://www.americancuervo.com/fotos/" + mMainDirectoryName + "/" +
                                mSortingList.get(mSlideShowCounter));

                                /* return new BitmapLoader(getApplicationContext(),
                        "http://www.americancuervo.com/fotos/" + mMainDirectoryName + "/" +
                                mList[mSlideShowCounter]);*/
            }

            @Override
            public void onLoadFinished(Loader<Drawable> loader, Drawable data) {

                ImageView im = (ImageView) findViewById(R.id.imageView);
                im.setImageDrawable(data);
                mSlideShowCounter++;
                startTimer();
                // Destroy loader by Id.
                getLoaderManager().destroyLoader(sBitmapLoaderId);
            }

            @Override
            public void onLoaderReset(Loader<Drawable> loader) {

            }
        };

        // Start loader dedicated to the Distance Matrix API
        getLoaderManager().initLoader(sDistanceMatrixLoaderId, null, listResultLoaderListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSharedPrefs();
        cancelTimer();
        System.out.println("PAUSE");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mList != null) {
            startTimer();
        }
    }

    private int getPositionSharedPrefs() {
        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(SHARED_PREFERENCES, Activity.MODE_PRIVATE);

       // System.out.println(SAVED_POSITION_TAG);
        return sharedPreferences.getInt(SAVED_POSITION_TAG, 0);
    }

    private void saveSharedPrefs() {
        SharedPreferences.Editor editor = getApplicationContext()
                .getSharedPreferences(SHARED_PREFERENCES, Activity.MODE_PRIVATE).edit();

        editor.putInt(SAVED_POSITION_TAG, mSlideShowCounter);

        //System.out.println(SAVED_POSITION_TAG + " - " + mSlideShowCounter);

        editor.apply();
    }

    /**
     * Starting new polyline task timer for animation.
     */

    private void startTimer() {
        if (mSlideShowTimer == null) mSlideShowTimer = new Timer();
        mSlideShowTimerTask = new SlideShowTimerTask();
        mSlideShowTimer.schedule(mSlideShowTimerTask, SLIDE_SHOW_TIME_AMOUNT);
    }

    private void cancelTimer() {
        if (mSlideShowTimer != null) {
            mSlideShowTimerTask.cancel();
        }
    }

    /**
     * Timer task for polyline animation.
     */
    private class SlideShowTimerTask extends TimerTask {
        public void run() {
            // If the polyline counter = the polyline decoded list size, cancel the timer and return.
            if (mSlideShowCounter >= mList.length) {
                //cancelTimer();
                mSlideShowCounter = 0;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cancelTimer();
                    getLoaderManager().initLoader(sBitmapLoaderId, null, imageResultLoaderListener);
                }
            });

        }
    }
}
