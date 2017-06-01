package show.server.mcv.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import motiontracker.mcv.com.R;
import show.server.mcv.com.Globals;
import show.server.mcv.com.utils.ImageDownloader;

/*Adapter class for creating table-- columns*/
public class PhotoAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public PhotoAdapter(Context context, String[] values) {
        super(context, R.layout.listview_item_table_data, values);
        this.context = context;
        this.values = values;
    }

    /*Add columns to the table here.*/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String label = values[position];

        String[] separated = label.split("/");
        String title = separated[separated.length - 1];
        View rowView = inflater.inflate(R.layout.listview_item_photo_activity, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        textView.setText(title);

        if (label.contains(".jpg") || label.contains(".png")) {
            ImageView imageView = (ImageView) rowView.findViewById(R.id.listImageView);
            if (Globals.bitmapCache.containsKey(label)) {
                imageView.setImageBitmap(Globals.bitmapCache.get(label));
            } else {
                new ImageDownloader(imageView).execute(label);
            }
        }

        return rowView;
    }
}
