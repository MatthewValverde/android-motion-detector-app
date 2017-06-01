package show.server.mcv.com;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import motiontracker.mcv.com.R;
import show.server.mcv.com.adapter.PhotoAdapter;
import show.server.mcv.com.loaders.ListLoader;

public class SlideShowActivity extends AppCompatActivity implements LoaderCallbacks<String[]> {
    private static final int sDistanceMatrixLoaderId = 0x060606;

    private static final String TAG = "MainShowActivity";
    private static final String DIRECTORY_TAG = "DIRECTORY_TAG";
    private String mDirectoryName;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setHomeButtonEnabled(true);
        mDirectoryName = getIntent().getStringExtra(DIRECTORY_TAG);
        if (mDirectoryName == null) {
            mDirectoryName = "";
        }

        mUrl = MainUrl.string + "images/" + mDirectoryName;
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
            data[i] = mUrl + "/" + data[i];
            //   Log.d(TAG, data[i]);
        }

        getSupportActionBar().setTitle(String.valueOf(data.length) + " Photos Detected");

        PhotoAdapter adapter = new PhotoAdapter(this, data);

        ListView lv = (ListView) findViewById(R.id.listView);

        lv.setAdapter(adapter);

       /* lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = null;
                if (!mDirectoryName.isEmpty()) {
                    in = new Intent(getApplicationContext(), SlideShowActivity.class);
                } else {
                    in = new Intent(getApplicationContext(), MainShowActivity.class);
                }
                in.putExtra(DIRECTORY_TAG, data[position]);
                startActivity(in);
            }
        });*/

        // Destroy loader by Id.
        getLoaderManager().destroyLoader(sDistanceMatrixLoaderId);
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {

    }
}
