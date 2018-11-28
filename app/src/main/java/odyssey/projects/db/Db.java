package odyssey.projects.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public final class Db {

    public static final String TAG = "DB";

    // Основная информация о базе данных.
    private static final String DB_NAME         = "savDb_v8";
    private static final int    DB_VERSION      = 1;
    private static final String TABLE_MARKS     = "marks";
    private static final String TABLE_VEHICLES  = "vehicles";

    // Таблица TABLE_MARKS_COLUMNS содержит информацию о всех отметках.
    public static final class TABLE_MARKS_COLUMNS {
        public static final String COLUMN_ID         = "_id";
        public static final String COLUMN_VEHICLE    = "vehicle";
        public static final String COLUMN_TIMESTAMP  = "timestamp";

        public static final int ID_COLUMN_ID         = 0;
        public static final int ID_COLUMN_VEHICLE    = 1;
        public static final int ID_COLUMN_TIMESTAMP  = 2;

        private static final String columns[]={
                COLUMN_ID,
                COLUMN_VEHICLE,
                COLUMN_TIMESTAMP};
    }

    // Таблица содержит список введенных гос. номеров и их популярность.
    public static final class TABLE_VEHICLES_COLUMNS {
        public static final String COLUMN_ID         = "_id";
        public static final String COLUMN_VEHICLE    = "vehicle";
        public static final String COLUMN_POPULARITY = "popularity";

        public static final int ID_COLUMN_ID          = 0;
        public static final int ID_COLUMN_VEHICLE     = 1;
        public static final int ID_COLUMN_POPULARITY  = 2;

        private static final String columns[]={
                COLUMN_ID,
                COLUMN_VEHICLE,
                COLUMN_POPULARITY};
    }

    private static final String CREATE_TABLE_MARKS = "create table if not exists "  +
            TABLE_MARKS + "(" +
            TABLE_MARKS_COLUMNS.COLUMN_ID                       + " integer primary key autoincrement, " +
            TABLE_MARKS_COLUMNS.COLUMN_VEHICLE                  + " TEXT NOT NULL," +
            TABLE_MARKS_COLUMNS.COLUMN_TIMESTAMP                + " DATETIME NOT NULL," +
            "CONSTRAINT timestamp_unique UNIQUE ("+TABLE_MARKS_COLUMNS.COLUMN_TIMESTAMP+"));";

    private static final String CREATE_TABLE_VEHICLES = "create table if not exists "  +
            TABLE_VEHICLES + "(" +
            TABLE_VEHICLES_COLUMNS.COLUMN_ID                       + " integer primary key autoincrement, " +
            TABLE_VEHICLES_COLUMNS.COLUMN_VEHICLE                  + " NOT NULL UNIQUE ON CONFLICT REPLACE," +
            TABLE_VEHICLES_COLUMNS.COLUMN_POPULARITY               + " integer DEFAULT 0);";

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    // Конструктор класса Db.
    public Db() {
        /*  */
    }

    // Открыть подключение к локальной БД.
    public void open(final Context context) throws SQLiteException {
        mDBHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // Закрыть подключение.
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // Вложенный класс по созданию и управлению БД.
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            // Создаем таблицу отметок.
            db.execSQL(CREATE_TABLE_MARKS);
            // Создаем таблицу с перечнем транспортных средств.
            db.execSQL(CREATE_TABLE_VEHICLES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    // Очистить таблицу от всех колонок.
    public void clearTable(String tableName) throws SQLiteException{
        mDB.delete("\""+tableName+"\"",null,null);
        Log.d(TAG, "Cleared table: "+tableName);
    }

    // Удалить таблицу, если таковая существует
    public void deleteTable(String tableName) throws SQLiteException{
        mDB.execSQL("drop table if exists \""+tableName+"\"");
        Log.d(TAG, "Deleted table: "+tableName);
    }

    // Очистить таблицу @marks от всех колонок.
    public void clearTableMarks() throws SQLiteException{
        clearTable(TABLE_MARKS);
        Log.d(TAG, "Cleared table: "+TABLE_MARKS);
    }

    // Получить все отметки за все время за все ТС.
    public Cursor getAllMarks() throws SQLiteException {
        return mDB.query(TABLE_MARKS, null, null, null, null, null, null);
    }

    // Получить все отметки за все время по конкретному ТС.
    public Cursor getAllMarks(String vehicle) throws SQLiteException {
        return mDB.query(
                TABLE_MARKS,
                TABLE_MARKS_COLUMNS.columns,
                TABLE_MARKS_COLUMNS.COLUMN_VEHICLE+"=\""+vehicle+"\"",
                null,
                null, null, null);
    }

    // Получить все отметки по конкретному ТС за указанную дату.
    public Cursor getAllMarks(String vehicle, String timestamp) throws SQLiteException {
        return mDB.query(
                TABLE_MARKS,
                TABLE_MARKS_COLUMNS.columns,
                TABLE_MARKS_COLUMNS.COLUMN_VEHICLE+"=\""+vehicle+"\" AND" + TABLE_MARKS_COLUMNS.COLUMN_TIMESTAMP+"=\""+timestamp+"\"",
                null,
                null, null, null);
    }



    // Добавить запись в TABLE_MARKS
    public void addMark(String vehicle, String timestamp) throws SQLiteException {
        // Создаем контейнер типа ключ-значение.
        ContentValues cv = new ContentValues();
        cv.put(TABLE_MARKS_COLUMNS.COLUMN_VEHICLE, vehicle);
        cv.put(TABLE_MARKS_COLUMNS.COLUMN_TIMESTAMP, timestamp);
        // Записываем данные в базу данных.
        long result = mDB.insert( TABLE_MARKS, null, cv );
        Log.d(TAG, "Added mark: "+timestamp);
    }

    public Boolean deleteVehicle(String vehicle){
        mDB.delete("\""+TABLE_VEHICLES+"\"",TABLE_VEHICLES_COLUMNS.COLUMN_VEHICLE+"=\""+vehicle+"\"",null);
        return true;
    }

    // Возвращает курсор на ТС с указанным номером.
    public Cursor getVehicle(String vehicle){
        return mDB.query(
                TABLE_VEHICLES,
                TABLE_VEHICLES_COLUMNS.columns,
                TABLE_VEHICLES_COLUMNS.COLUMN_VEHICLE+"=\""+vehicle+"\"",
                null, null,null,null);
    }

    // Возвращает значение рейтинга указанного ТС.
    public int getVehiclePopularity(String vehicle){
        Cursor cursor = getVehicle(vehicle);
        // Если в локальной БД уже присутствует ТС с таким номером, то считываем его рейтинг.
        if ( (cursor != null) && (cursor.getCount() != 0)){
            cursor.moveToFirst();
            return cursor.getInt(TABLE_VEHICLES_COLUMNS.ID_COLUMN_POPULARITY);
        }
        return -1;
    }

    // Добавить транспортное средство в БД. Популярность будет прсчитана автоматически.
    public Boolean insertVehicle(String vehicle){
        int popularity = getVehiclePopularity(vehicle);

        if (popularity < 0) popularity = 0; else popularity++;
        //String query = "INSERT INTO "+TABLE_VEHICLES+" (vehicle, popularity) VALUES ('"+vehicle+"','"+popularity+"');";
        //mDB.execSQL(query,null);

        // Создаем контейнер типа ключ-значение.
        ContentValues cv = new ContentValues();
        cv.put(TABLE_VEHICLES_COLUMNS.COLUMN_POPULARITY, popularity);
        cv.put(TABLE_VEHICLES_COLUMNS.COLUMN_VEHICLE, vehicle);
        // Записываем данные в базу данных.
        return (mDB.insert( TABLE_VEHICLES, null, cv ) != -1);
    }

    // Получить все занесенные в БД транспортные средства в порядке убывания популярности.
    public Cursor getAllVehicles() throws SQLiteException {
        return mDB.query(TABLE_VEHICLES, null, null, null, null, null, "popularity DESC");
    }

    // Удалить все номера из локальной БД.
    public void removeAllVehicles() throws SQLiteException {
        clearTable(TABLE_VEHICLES);
    }
}
