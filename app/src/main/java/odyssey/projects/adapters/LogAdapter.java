package odyssey.projects.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import odyssey.projects.debug.LogItem;
import odyssey.projects.pref.SettingsCache;
import odyssey.projects.sav.driver.R;

public class LogAdapter extends BaseAdapter {

    private final Context context;
    private static ArrayList<LogItem> list = new ArrayList<>();

    public LogAdapter(Context context) {
        super();
        this.context = context;
    }

    public ArrayList<LogItem> getList(){
        return list;
    }

    public void addItem (LogItem item){
        list.add(item);

        // Проверяем общее количество строк в логе событий. Если оно превышает установленный предел - удаляем самое старое сообщение.
        if (list.size() > SettingsCache.DEBUG_LOG_MAX_LINES){
            if (!list.isEmpty()) list.remove(0);
        }

        // Получаем указатель на экземпляр активити, содержащую графические компоненты, доступ к которым
        // нам необходим.
        Activity activity = (Activity) context;
        // Проверяем, является ли текущий поток основным UI-потоком. Это необходимо, так как текущая функция
        // может быть вызвана асинхронно из любого потока приложения.
        boolean isOnUiThread = Thread.currentThread() == Looper.getMainLooper().getThread();
        // Если текущий поток, вызвавший эту функцию, не является UI-потоком, то запускаем обновление
        // (функция notifyDataSetChanged()) графических элементов из UI-потока. В противном случае приложение вызовет исключение
        // и будет закрыто.
        if (!isOnUiThread){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        } else {
            // Если вызов текущей функции (addItem()) вызван из основного UI-потока, то просто вызываем функцию обновления
            // визуальных компонентов (в данном случае - ListView).
            notifyDataSetChanged();
        }
    }

    public void clear(){
        list.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) convertView = LayoutInflater.from(this.context).inflate(R.layout.debug_log_item, null);

        TextView tag  = convertView.findViewById(R.id.tagTextView);
        TextView type = convertView.findViewById(R.id.typeTextView);
        TextView time = convertView.findViewById(R.id.timeTextView);
        TextView mess = convertView.findViewById(R.id.messageTextView);

        LogItem item = (LogItem)getItem(position);

        tag.setText((item.tag!=null)?item.tag:"DEFAULT_TAG");
        type.setText((item.type!=null)?item.type.toString():"UNKNOWN_TYPE");
        time.setText((item.timestamp!=null)?item.timestamp:"1900-01-01 00:00:00");
        mess.setText((item.message!=null)?item.message:"No message.");

        int color = -1;

        if (item.type != null)
        switch (item.type){
            case TYPE_DEBUG:
                color = R.color.colorLogItemTypeDEBUG;
                break;
            case TYPE_INFO:
                color = R.color.colorLogItemTypeINFO;
                break;
            case TYPE_WARNING:
                color = R.color.colorLogItemTypeWARNING;
                break;
            case TYPE_ERROR:
                color = R.color.colorLogItemTypeERROR;
                break;
                default:
                    color = R.color.colorLogItemTypeERROR;
                    break;
        }

        type.setTextColor(context.getResources().getColor(color));

        return convertView;
    }
}
