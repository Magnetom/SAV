package odyssey.projects.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import odyssey.projects.sav.driver.R;

public class VehiclesCursorAdapter extends BaseAdapter {

    protected final Context context;
    private ArrayList<String> list = null;

    public VehiclesCursorAdapter(Context context) {
        this.context = context;
        list = new ArrayList<String>();
    }

    public VehiclesCursorAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
        notifyDataSetChanged();
    }

    public void setDataSet(ArrayList<String> list){
        this.list = list;
        notifyDataSetChanged();
    }

    public void clear(){
        list = new ArrayList<String>();
        notifyDataSetChanged();
    }

    public boolean isEmpty(){
        return (list.size() == 0);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        if ( list != null && (position < getCount()) ){
            return list.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(this.context).inflate(R.layout.vehicles_list_item, null);

        TextView vehicleIdView = (TextView) convertView.findViewById(R.id.vehicleIdView);
        vehicleIdView.setText((String)getItem(position));

        //TextView currDateView = (TextView) convertView.findViewById(R.id.currDateView);
        //if (currDateView != null) currDateView.setText("888");

        return convertView;
    }
}
