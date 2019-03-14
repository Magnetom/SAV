package odyssey.projects.sav.db;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.Objects;

import odyssey.projects.sav.SwipeListView.SwipeMenu;
import odyssey.projects.sav.SwipeListView.SwipeMenuCreator;
import odyssey.projects.sav.SwipeListView.SwipeMenuItem;
import odyssey.projects.sav.SwipeListView.SwipeMenuListView;
import odyssey.projects.sav.adapters.TrackAdapter;
import odyssey.projects.sav.remote.UploadManager;
import odyssey.projects.sav.tracker.PointsActivity;
import odyssey.projects.sav.tracker.R;

import static odyssey.projects.sav.utils.Helper.dp2px;


public class TracksView extends DbProc {

    private final String TAG = "TRACKS_VIEW";

    private TrackAdapter adapter;

    public TracksView(Context context) {super(context);}

    @Override
    SimpleCursorAdapter getAdapter() {return adapter;}

    @Override
    void setupAdapter(Context context) {
        // Формируем столбцы сопоставления
        String[] from = new String[] {
                Db.TABLE_TRACKS_COLUMNS.COLUMN_ID,
                Db.TABLE_TRACKS_COLUMNS.COLUMN_TRACK,
                Db.TABLE_TRACKS_COLUMNS.COLUMN_POINTS
        };

        int[] to = new int[] {  0,                   // [00] Db.COLUMN_ID
                                R.id.trackNameView,  // [01] Db.COLUMN_TRACK
                                R.id.pointsCountView // [02]
        };

        // создаем адаптер и настраиваем список
        adapter = new TrackAdapter(this.context, R.layout.tracks_list_item, null, from, to, 0);
    }

    @Override
    void setupListView() {

        final SwipeMenuListView listView = (((AppCompatActivity) context).findViewById(R.id.trackListView));
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
        View list_header = LayoutInflater.from(this.context).inflate(R.layout.tracks_list_header, null);
        listView.addHeaderView(list_header);

        // Устанавливаем подвал списка
        View list_footer = LayoutInflater.from(this.context).inflate(R.layout.tracks_list_footer, null);
        listView.addFooterView(list_footer);

        listView.setClickable(true);
        listView.setLongClickable(true);

        // Регистрируем ListView для контекстного меню.
        //((AppCompatActivity) context).unregisterForContextMenu(listView);

        // Swipe Menu Creator
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {

                // create "upload" item
                SwipeMenuItem uploadItem = new SwipeMenuItem(context);
                uploadItem.setBackground(context.getDrawable(R.color.colorSwipeMenuItemUpload));
                uploadItem.setWidth(dp2px(context,90));
                uploadItem.setIcon(R.drawable.ic_upload, dp2px(context,32), dp2px(context,32));
                menu.addMenuItem(uploadItem);

                // create "edit" item
                SwipeMenuItem editItem = new SwipeMenuItem(context);
                // set item background
                //editItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,0xCE)));
                editItem.setBackground(context.getDrawable(R.color.colorSwipeMenuItemEdit));
                // set item width
                editItem.setWidth(dp2px(context,70));
                // set item title
                //editItem.setTitle("Изменить");
                // set item title font size
                //editItem.setTitleSize(18);
                // set item title font color
                //editItem.setTitleColor(Color.WHITE);
                editItem.setIcon(R.drawable.ic_edit, dp2px(context,32), dp2px(context,32));
                // add to menu
                menu.addMenuItem(editItem);

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

        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);

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

                /*
                // Получаем позицию элемента в списке с учетом невидимых элементов списка и заголовков,
                // если таоквые имются.
                int pos = position-listViewFinal.getFirstVisiblePosition();
                pos += listViewFinal.getHeaderViewsCount();
                // Получаем указатель на лэйаут выбранного элемента списка.
                View listItem = listViewFinal.getChildAt(pos);
                // Проверяем на валидность.
                if (listItem == null) return false;
                // Получаем текстовое поле, содержащее имя трека.
                TextView track_red = listItem.findViewById(R.id.trackNameView);
                // Проверяем на валидность.
                if (track_red == null || track_red.getText().toString().equals("")) return false;
                */


                Cursor cursor = (Cursor) listView.getAdapter().getItem(position+ listView.getHeaderViewsCount());
                if (cursor != null) cursor.moveToPosition(position);

                final String track_id = Objects.requireNonNull(cursor).getString(Db.TABLE_TRACKS_COLUMNS.ID_COLUMN_ID);
                final String track_name = Objects.requireNonNull(cursor).getString(Db.TABLE_TRACKS_COLUMNS.ID_COLUMN_TRACK);

                switch (index) {
                    case 0:
                        ///////////////////////////////////
                        // Выгрузка маршрута на сервер.  //
                        ///////////////////////////////////
                        Track track = getTrack(Long.valueOf(track_id));
                        UploadManager.doUpload(context, track);
                        break;
                    case 1:
                        ///////////////////////////////////
                        // Редактирование имени маршрута //
                        ///////////////////////////////////
                        View view = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.rename_track_dialog, null);
                        if (view == null) return false;

                        final TextView trkName = view.findViewById(R.id.renameTrackNameText);
                        if (trkName != null) trkName.setText(track_name);

                        new AlertDialog.Builder(context,R.style.AlertDialogTheme)
                                .setView(view)
                                .setIcon(R.drawable.alert_rename)
                                .setTitle("Редактирование маршрута")
                                .setPositiveButton("Ок", new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Переименовываем маршурт только в случае, если пользователь изменил имя.
                                        if (trkName != null && !trkName.getText().toString().equalsIgnoreCase(track_name)) {
                                            renameTrack(Integer.valueOf(track_id), trkName.getText().toString());
                                        }
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
                    case 2:
                        /////////////////////////////////
                        // Удаление текущего маршрута. //
                        /////////////////////////////////

                        view = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.remove_track_dialog, null);
                        if (view == null) return false;

                        final TextView trkName_del = view.findViewById(R.id.trackForDeleteView);
                        if (trkName_del != null) trkName_del.setText(track_name);

                        new AlertDialog.Builder(context,R.style.AlertDialogTheme)
                                .setView(view)
                                .setIcon(R.drawable.alert_attention)
                                .setTitle("Удаление маршрута")
                                .setPositiveButton("Ок", new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteTrack(Integer.valueOf(track_id));
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
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Если произошло нажатие на заголовок или подвал - пропускаем такое нажатие.
                if (id == -1) return;
                // Запуск новой активити - "Список точек маршрута".
                Intent intent = new Intent(context, PointsActivity.class);
                // Ложим в намеринени дополнительную информацию - номер маршрута, для которого вызывается эта активити.
                intent.putExtra("track", id);
                context.startActivity(intent);
            }
        });

        // Присваиваем адаптер для виджета ListView
        listView.setAdapter(adapter);
    }

    @Override
    CursorLoader initCursorLoader() {
        return new MyCursorLoader(this.context, this.db);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        this.adapter.swapCursor(data);
    }

    @Override
    void OnDestroy() {}

    private static class MyCursorLoader extends CursorLoader {
        Db db;
        MyCursorLoader(@NonNull Context context, Db db) {
            super(context);
            this.db = db;
        }
        @Override
        public Cursor loadInBackground() {
            return  db.getAllTracks();
        }
    }
}
