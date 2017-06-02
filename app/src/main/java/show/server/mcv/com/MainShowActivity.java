package show.server.mcv.com;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import motiontracker.mcv.com.R;
import show.server.mcv.com.adapter.ListAdapter;
import show.server.mcv.com.loaders.ListLoader;

public class MainShowActivity extends AppCompatActivity implements LoaderCallbacks<String[]> {
    private static final int sDistanceMatrixLoaderId = 0x060606;

    private static final String TAG = "MainShowActivity";
    private static final String DIRECTORY_TAG = "DIRECTORY_TAG";
    private String mDirectoryName;
    private String[] mDevicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDirectoryName = getIntent().getStringExtra(DIRECTORY_TAG);
        if (mDirectoryName == null) {
            mDirectoryName = "";
        } else {
            getSupportActionBar().setTitle("Sessions");
        }

        // Start loader dedicated to the Distance Matrix API
        getLoaderManager().initLoader(sDistanceMatrixLoaderId, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mDirectoryName.isEmpty()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_main, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.triggerBtn:
                for (int i = 0; i < mDevicesList.length; i++) {
                    new updateData()
                            .execute(MainUrl.string + "trigger.php?device=" + mDevicesList[i]);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<String[]> onCreateLoader(int id, Bundle args) {
        return new ListLoader(getApplicationContext(),
                MainUrl.string + "list.php?dir=images/" + mDirectoryName);
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, final String[] data) {
        mDevicesList = data;

        Arrays.sort(data);

        ListAdapter adapter = new ListAdapter(this, data);
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = null;
                if (!mDirectoryName.isEmpty()) {
                    in = new Intent(getApplicationContext(), SlideShowActivity.class);
                    in.putExtra(DIRECTORY_TAG, mDirectoryName + "/" + data[position]);
                } else {
                    in = new Intent(getApplicationContext(), MainShowActivity.class);
                    in.putExtra(DIRECTORY_TAG, data[position]);
                }
                startActivity(in);
            }
        });

        getLoaderManager().destroyLoader(sDistanceMatrixLoaderId);
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {
    }

    public class updateData extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null;

            try {
                URL url;
                url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                } else {
                    InputStream err = conn.getErrorStream();
                }
                return "Done";
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }

    Comparator<Integer> comparator = new Comparator<Integer>() {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }
    };

}
