/*
 * Copyright (C) Google 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package show.server.mcv.com.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import show.server.mcv.com.utils.HttpConnectionUtil;

/**
 * Creates a MapDirectionsDownLoader that loads returns a drawable array.
 */
public class ListLoader extends AsyncTaskLoader<String[]> {

    private final String mUrl;

    public ListLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    /**
     * Returns array that contains json strings for each mode of transportation.
     */
    @Override
    public String[] loadInBackground() {
        return getModel(getData(mUrl));
    }

    /**
     * Returns json string
     */
    private String getData(String url) {
        String data = null;
        try {
            HttpConnectionUtil http = new HttpConnectionUtil();
            data = http.readUrl(url);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * Returns current marker model list with matrix data added.
     */
    private String[] getModel(String... data) {
        String[] list = null;
        try {
            //InputStream stream = new ByteArrayInputStream(data[0].getBytes());
            //System.out.println("getModel: " + data[0]);

            list = data[0].split("\\|");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}