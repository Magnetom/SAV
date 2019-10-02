package odyssey.projects.sav.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Phone> phones;

    DataAdapter(Context context, List<Phone> phones) {
        this.phones = phones;
        this.inflater = LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {
        Phone phone = phones.get(position);
        holder.imageView.setImageResource(phone.getImage());
        holder.nameView.setText(phone.getName());
        holder.companyView.setText(phone.getCompany());
    }

    @Override
    public int getItemCount() {
        return phones.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView imageView;
        final TextView nameView, companyView;

        ViewHolder(View view){
            super(view);
            imageView   = view.findViewById(R.id.image);
            nameView    = view.findViewById(R.id.name);
            companyView = view.findViewById(R.id.company);
        }
    }
}