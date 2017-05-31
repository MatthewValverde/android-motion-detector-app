package show.server.mcv.com;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.InputStream;

import motiontracker.mcv.com.R;
import show.server.mcv.com.adapter.ListAdapter;
import show.server.mcv.com.loaders.ListLoader;

public class MainShowActivity extends AppCompatActivity implements LoaderCallbacks<String[]> {
    private static final int sDistanceMatrixLoaderId = 0x060606;

    private static final String TAG = "MainShowActivity";
    private static final String DIRECTORY_TAG = "DIRECTORY_TAG";
    private String mDirectoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDirectoryName = getIntent().getStringExtra(DIRECTORY_TAG);
        if(mDirectoryName==null){
            mDirectoryName = "";
        }

        // Start loader dedicated to the Distance Matrix API
        getLoaderManager().initLoader(sDistanceMatrixLoaderId, null, this);
    }

    @Override
    public Loader<String[]> onCreateLoader(int id, Bundle args) {
        return new ListLoader(getApplicationContext(),
                MainUrl.string + "list.php?dir=images/" + mDirectoryName);
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, final String[] data) {
        for (int i = 0; i < data.length; i++) {
         //   Log.d(TAG, data[i]);
        }

        ListAdapter adapter = new ListAdapter(this, data);

        ListView lv = (ListView) findViewById(R.id.listView);

        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(getApplicationContext(), MainShowActivity.class);
                String separator = "";
                if(!mDirectoryName.isEmpty()){
                    separator = "/";
                }
                in.putExtra(DIRECTORY_TAG, mDirectoryName + separator + data[position]);
                startActivity(in);
            }
        });

        // Destroy loader by Id.
        getLoaderManager().destroyLoader(sDistanceMatrixLoaderId);
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {

    }

    /**
     * Returns a loaded drawable that is placed into a native array.
     */
    private Drawable getDrawable(String url) {
        Drawable drawable = null;
        try {
            InputStream in = getApplicationContext().getAssets().open(url);
            drawable = Drawable.createFromStream(in, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }


}
