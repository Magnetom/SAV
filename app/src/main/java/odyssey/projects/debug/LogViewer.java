package odyssey.projects.debug;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;

import odyssey.projects.adapters.LogAdapter;
import odyssey.projects.callbacks.DebugLogListener;
import odyssey.projects.sav.driver.R;
import odyssey.projects.utils.ZipManager;

import static odyssey.projects.utils.DateTimeUtils.getCurrentTimeStamp;
import static odyssey.projects.utils.DateTimeUtils.getCurrentTimeStampForFileName;

public class LogViewer {

    public static final String TAG = "LOG_VIEWER";

    private Context context;
    private LogAdapter adapter;
    private static DebugLogListener listener;

    public LogViewer(Context context) {
        this.context = context;
        if (context == null) return;

        setupAdapter();
        setupListView();
        setupListeners();
    }

    private void setupListeners() {
        listener = new DebugLogListener() {
            @Override
            public void addToLog(String tag, LogItemType type, String message) {
                addReport(tag,type,message);
            }
        };
    }

    static DebugLogListener getListener(){
        return listener;
    }

    private void setupListView() {
        final ListView listView = (((AppCompatActivity) context).findViewById(R.id.debugLogListView));
        if (listView == null) return;

        // Настраиваем view для случая пустого списка.
        ViewGroup parentGroup = (ViewGroup) listView.getParent();
        View emptyListView = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.empty_list_layout, parentGroup, false);
        parentGroup.addView(emptyListView);
        listView.setEmptyView(emptyListView);

        listView.setAdapter(adapter);
        //listView.setDivider(context.getResources().getDrawable(android.R.color.transparent));
        //listView.setDivider(context.getResources().getDrawable(R.drawable.split_line));

        listView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // По окончанию обновления данных в ListView плавно перемещаемся в конец списка.
                listView.smoothScrollToPosition(listView.getCount());
            }
        });

        /////////////////////////////////////////////////////////////////
        //addToLog(TAG, LogItemType.TYPE_DEBUG, "Test message debug!");
        //addToLog(TAG, LogItemType.TYPE_INFO, "Test message info!");
        //addToLog(TAG, LogItemType.TYPE_WARNING, "Test message warning!");
        //addToLog(TAG, LogItemType.TYPE_ERROR, "Test message error!");
    }

    private void setupAdapter() {
        // создаем адаптер и настраиваем список
        adapter = new LogAdapter(context);
    }

    public void clearAllReports(){
        adapter.clear();
    }

    private void addReport(String tag, LogItemType type, String message){
        if (adapter != null){
            LogItem item = new LogItem();

            item.tag  = tag;
            item.type = type;
            item.timestamp = getCurrentTimeStamp();
            item.message = message;

            adapter.addItem(item);
        }
    }


    public void shareData(){
        // Создаем файл лога событий.
        File zipFile = saveResults(false);

        if (zipFile != null && context != null) {

            //////////////////////////////////////////////////////////////////////////////
            // Без этих двух строк приложение отваливается при попытке передать Intent !!!
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            //////////////////////////////////////////////////////////////////////////////

            // Отправляем полученный файл любым удобным способом (системой будет предложен выбор).
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType(URLConnection.guessContentTypeFromName(zipFile.getName()));
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + zipFile.getAbsolutePath()));
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,"SAV Log File");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Log file for "+getCurrentTimeStamp()+".");
            context.startActivity(Intent.createChooser(sendIntent, "Share File"));

            // Удаляем ненужный более файл.
            //zipFile.delete();
        }
    }

    private String getStoragePlace(boolean useExternalStorage){
        if (useExternalStorage) return Environment.getExternalStorageState();
        else
        return context.getCacheDir().toString();
    }

    private File saveResults(boolean useExternalStorage) {

        if (context == null) return null;

        try {

            // Во директории Cash создается временный файл, который в последствии будет заполнен информацией и сжат.
            File tmpFile = File.createTempFile("log_" + getCurrentTimeStampForFileName(), ".html", context.getCacheDir());

            BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile, false));

            int size = adapter.getCount();
            ArrayList<LogItem> itemsList = adapter.getList();

            bw.write("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>Title</title></head><body><table cellpadding=\"2\" cellspacing=\"1\" border=\"1\"><tr><td>#</td><td>Время</td><td>Модуль</td><td>Тип сообщения</td><td>Сообщение</td></tr>\n");

            for (int ii = 0; ii < size; ii++) {
                LogItem item = itemsList.get(ii);
                String row = "<tr><td>"+ii+"</td><td>"+item.timestamp+"</td><td>"+item.tag+"</td><td>"+item.type+"</td><td>"+item.message+"</td></tr>\n";
                bw.write(row);
            }

            bw.write("</table></body></html>");
            bw.close();

            ////////////////////////////////////////////////////////////////////////////////////////////////
            // Упаковываем файл в zip-архив и помещаем его на внешний носитель для обспечения             //
            // общего доступа к нему.                                                                     //
            ////////////////////////////////////////////////////////////////////////////////////////////////
            // Проверяем возможность записи на вешний носитель.
            if (useExternalStorage)
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                DebugOut.generalPrintError(context, "Can't get access to the external storage.", TAG);
                return null;
            }

            // Создается директория на внешенем носителе для записи туда будущего zip-файла.
            File zipDir = new File(getStoragePlace(useExternalStorage),"logs");
            if (!zipDir.exists()) {
                if (!zipDir.mkdirs()) {
                    if (!zipDir.exists()) {
                        DebugOut.generalPrintError(context, "Can't create directory ["+zipDir.getAbsolutePath()+"].", TAG);
                        return null;
                    }
                }
            }
            // Создается zip-файл.
            File zipFile = new File(zipDir, "log_" + getCurrentTimeStampForFileName() + ".zip");
            ArrayList<File> filesList = new ArrayList<>();
            filesList.add(tmpFile);
            ZipManager.zip(filesList, zipFile);


            // Удаляем неныжный более временный файл.
            if (tmpFile != null) {
                boolean result = tmpFile.delete();
            }

            return zipFile;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}