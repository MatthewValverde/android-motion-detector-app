package show.server.mcv.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import motiontracker.mcv.com.R;


/*Adapter class for creating table-- columns*/
public class ListAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public ListAdapter(Context context, String[] values) {
        super(context, R.layout.listview_item_table_data, values);
        this.context = context;
        this.values = values;
    }

    /*Add columns to the table here.*/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listview_item_table_data, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        textView.setText(String.valueOf(values[position]));

        return rowView;
    }
}
