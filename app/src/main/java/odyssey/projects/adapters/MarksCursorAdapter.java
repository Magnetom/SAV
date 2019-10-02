package odyssey.projects.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import odyssey.projects.db.Db;
import odyssey.projects.sav.driver.R;

import static odyssey.projects.utils.DateTimeUtils.getHHMMFromStringTimestamp;

public class MarksCursorAdapter extends SimpleCursorAdapter {

    public MarksCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView timestamp = view.findViewById(R.id.tvTimestamp);
        timestamp.setText(getHHMMFromStringTimestamp(cursor.getString(Db.TABLE_MARKS_COLUMNS.ID_COLUMN_TIMESTAMP)));

        TextView _id = view.findViewById(R.id.tvSeqNum);
        String pos = Integer.valueOf(cursor.getPosition()+1).toString();
        _id.setText(pos);

        ConstraintLayout container = view.findViewById(R.id.sequenceContainer);

        // Последнюю позицию в списке выделяем другим цетом, чтобы визуально лучше воспринимался конец списка.
        if (cursor.getCount() == (cursor.getPosition()+1)){
            // Получаем ссылку на графический элемент (квадратная рамка со скругленными краями, которая обрамляет
            // последовательный номер элемента в списке).
            container.setBackgroundResource(R.drawable.square_marked);
        } else {
            container.setBackgroundResource(R.drawable.square);
        }

    }
}
