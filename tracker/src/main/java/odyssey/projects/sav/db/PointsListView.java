package odyssey.projects.sav.db;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import odyssey.projects.sav.SwipeListView.SwipeMenu;
import odyssey.projects.sav.SwipeListView.SwipeMenuCreator;
import odyssey.projects.sav.SwipeListView.SwipeMenuItem;
import odyssey.projects.sav.SwipeListView.SwipeMenuListView;
import odyssey.projects.sav.activity.R;
import odyssey.projects.sav.adapters.PointsAdapter;

import static odyssey.projects.sav.utils.Helper.dp2px;

public class PointsListView extends DbProc {

    private final String TAG = "POINTS_LIST_VIEW";

    private PointsAdapter adapter;

    private static long track = -1;

    public PointsListView(Context context, long track) { super(context); setTrack(track);}

    @Override
    SimpleCursorAdapter getAdapter() { return adapter; }

    @Override
    void setupAdapter(Context context) {
        // Формируем столбцы сопоставления
        String[] from = new String[] {
                Db.TABLE_POINTS_COLUMNS.COLUMN_ID,
                Db.TABLE_POINTS_COLUMNS.COLUMN_TRACK_ID,
                Db.TABLE_POINTS_COLUMNS.COLUMN_SEQUENCE,
                Db.TABLE_POINTS_COLUMNS.COLUMN_POINT,
                Db.TABLE_POINTS_COLUMNS.COLUMN_GPS_LATITUDE,
                Db.TABLE_POINTS_COLUMNS.COLUMN_GPS_LONGITUDE,
                Db.TABLE_POINTS_COLUMNS.COLUMN_GPS_TOLERANCE,
                Db.TABLE_POINTS_COLUMNS.COLUMN_ACTIVE
        };

        int[] to = new int[] {
                0,                      // [00] COLUMN_ID
                0,                      // [01] COLUMN_TRACK_ID
                R.id.itemSequenceView,  // [02] COLUMN_SEQUENCE
                R.id.pointNameView,     // [03] COLUMN_POINT
                R.id.itemLatitudeView,  // [04] COLUMN_GPS_LATITUDE
                R.id.itemLongitudeView, // [05] COLUMN_GPS_LONGITUDE
                R.id.toleranceView,     // [06] COLUMN_GPS_TOLERANCE
                0                       // [07] COLUMN_ACTIVE
        };

        // создаем адаптер и настраиваем список
        adapter = new PointsAdapter(this.context, R.layout.points_list_item, null, from, to, 0);
    }

    @Override
    void setupListView() {

        final SwipeMenuListView listView = (((AppCompatActivity) context).findViewById(R.id.pointsListView));
        if (listView == null) return;

        // Настраиваем view для случая пустого списка.
        ViewGroup parentGroup = (ViewGroup) listView.getParent();
        View emptyListView = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.empty_list_layout, parentGroup, false);
        parentGroup.addView(emptyListView);
        listView.setEmptyView(emptyListView);

        //listView.setDivider(context.getResources().getDrawable(android.R.color.transparent));
        listView.setDivider(context.getResources().getDrawable(R.drawable.list_divider));
        listView.setDividerHeight(1);
        listView.setFooterDividersEnabled(false);
        listView.setHeaderDividersEnabled(false);

        // Устанавливаем шапку списка.
        View list_header = LayoutInflater.from(this.context).inflate(R.layout.points_list_header, null);
        listView.addHeaderView(list_header);

        // Устанавливаем подвал списка
        View list_footer = LayoutInflater.from(this.context).inflate(R.layout.points_list_footer, null);
        listView.addFooterView(list_footer);

        listView.setClickable(true);
        listView.setLongClickable(true);

        // Регистрируем ListView для контекстного меню.
        //((AppCompatActivity) context).unregisterForContextMenu(listView);

        // Swipe Menu Creator
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(context);
                // set item background
                //openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,0xCE)));
                openItem.setBackground(context.getDrawable(R.color.colorSwipeMenuItemEdit));
                // set item width
                openItem.setWidth(dp2px(context,90));
                // set item title
                //openItem.setTitle("Изменить");
                // set item title font size
                //openItem.setTitleSize(18);
                // set item title font color
                //openItem.setTitleColor(Color.WHITE);
                openItem.setIcon(R.drawable.ic_edit, dp2px(context,32), dp2px(context,32));
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(context);
                // set item background
                //deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,0x3F, 0x25)));
                deleteItem.setBackground(context.getDrawable(R.color.colorSwipeMenuItemDelete));
                // set item width
                deleteItem.setWidth(dp2px(context,50));
                // set a icon
                //deleteItem.setIcon(R.drawable.ic_delete);
                deleteItem.setIcon(R.drawable.ic_delete, dp2px(context,32), dp2px(context,32));
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // Right
        //listView.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        // set creator
        listView.setMenuCreator(creator);

        listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {

                Cursor cursor = (Cursor) listView.getAdapter().getItem(position+ listView.getHeaderViewsCount());
                if (cursor != null) cursor.moveToPosition(position);

                final LocationPointItem point = getPoint(cursor);

                if (point == null) return false;

                switch (index){
                    case 0:
                        /////////////////////////////////
                        // Редактирование имени точки. //
                        /////////////////////////////////
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                        View view = inflater.inflate(R.layout.rename_point_dialog, null);

                        final TextView pointName        = view.findViewById(R.id.renamePointNameText);
                        final TextView pointLatitude    = view.findViewById(R.id.renamePointLatitudeText);
                        final TextView pointLongitude   = view.findViewById(R.id.renamePointLongitudeText);
                        final TextView pointTolerance   = view.findViewById(R.id.renamePointToleranceText);

                        pointName.setText(point.name);
                        pointLatitude.setText(point.latitude);
                        pointLongitude.setText(point.longitude);
                        pointTolerance.setText(String.valueOf(point.tolerance));

                        // Настраиваем диалоговое овно присвоения имени новой точке.
                        new AlertDialog.Builder(context,R.style.AlertDialogTheme)
                                .setView(view)
                                .setIcon(R.drawable.alert_rename)
                                .setTitle("Редактирование точки №"+point.sequence)
                                //.setMessage("Введите имя для новой точки маршрута.")
                                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Ок", new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {

                                        if (pointName.getText().toString().equals(""))       pointName.setText("новая точка");
                                        if (pointLatitude.getText().toString().equals(""))   pointLatitude.setText("0.000000");
                                        if (pointLongitude.getText().toString().equals(""))  pointLongitude.setText("0.000000");
                                        if (pointTolerance.getText().toString().equals(""))  pointTolerance.setText("50");

                                        LocationPointItem newPoint = new LocationPointItem();

                                        newPoint.id         = point.id;
                                        newPoint.track_id   = point.track_id;
                                        newPoint.name       = pointName.getText().toString();
                                        newPoint.latitude   = pointLatitude.getText().toString();
                                        newPoint.longitude  = pointLongitude.getText().toString();
                                        newPoint.tolerance  = Integer.valueOf(pointTolerance.getText().toString());
                                        newPoint.active     = point.active;

                                        updatePoint(newPoint);

                                        dialog.cancel();
                                    }
                                })
                                .create()
                                .show();

                        break;
                    case 1:
                        /////////////////////////////////
                        // Удаление текущей точки.     //
                        /////////////////////////////////
                        view = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.remove_point_dialog, null);
                        if (view == null) return false;

                        final TextView pointName_del = view.findViewById(R.id.pointForDeleteView);
                        if (pointName_del != null) pointName_del.setText(point.name);

                        new android.app.AlertDialog.Builder(context,R.style.AlertDialogTheme)
                                .setView(view)
                                .setIcon(R.drawable.alert_attention)
                                .setTitle("Удаление точки")
                                .setPositiveButton("Ок", new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                        deletePoint(point.id);
                                        // Закрываем текущее диалоговое окно.
                                        dialog.cancel();
                                    }
                                })
                                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Закрываем текущее диалоговое окно.
                                        dialog.cancel();
                                    }
                                })
                                .create()
                                .show();
                        break;
                }
                return false;
            }
        });

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(context, PointsActivity.class);
//                context.startActivity(intent);
//            }
//        });

        // Присваиваем адаптер для виджета ListView
        listView.setAdapter(adapter);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        this.adapter.swapCursor(data);
    }

    @Override
    CursorLoader initCursorLoader() {
        return new PointsListView.MyCursorLoader(this.context, this.db);
    }

    @Override
    void OnDestroy() { }

    private static class MyCursorLoader extends CursorLoader {
        Db db;
        MyCursorLoader(@NonNull Context context, Db db) {
            super(context);
            this.db = db;
        }
        @Override
        public Cursor loadInBackground() {
            return  db.getTrackPoints(track);
        }
    }

    public long  getTrack() {return track;}
    private void setTrack(long track_id) { track = track_id; }

    public String getTrackName(Long track_id){
        return db.getTrackName(track_id);
    }
}
