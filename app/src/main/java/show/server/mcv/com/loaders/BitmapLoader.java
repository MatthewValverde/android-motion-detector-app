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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.InputStream;
import java.net.URL;

import show.server.mcv.com.utils.ExifUtil;

/**
 * Creates a BitmapLoader that loads returns a drawable array.
 */

public class BitmapLoader extends AsyncTaskLoader<Drawable> {

    private final String mUrl;

    public BitmapLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    /**
     * Returns drawable native array of loaded bitmaps.
     */
    @Override
    public Drawable loadInBackground() {
        Drawable drawable = null;
        try {
            //System.out.println(mUrl);
            //Bitmap mIcon11 = null;
            InputStream in = new URL(mUrl).openStream();
            // mIcon11 = BitmapFactory.decodeStream(in);
            // ExifUtil.rotateBitmap(mUrl, mIcon11);
          /*
            System.out.println(mUrl);
            drawable = new BitmapDrawable(BitmapFactory.decodeStream(in));*/
            drawable = Drawable.createFromStream(in, null);

            /*System.out.println("drawable: " + drawable.getIntrinsicWidth() + " - " +
                    drawable.getIntrinsicHeight());*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}