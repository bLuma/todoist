package cz.divisak.todoist.todoist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cz.divisak.todoist.todoist.data.TodosContract;

/**
 * Todos DB helper.
 */
public class TodosDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "todos.db";

    public TodosDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LIST_TABLE = "CREATE TABLE " + TodosContract.ListEntry.TABLE_NAME + " (" +
                TodosContract.ListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TodosContract.ListEntry.COLUMN_TITLE + " TEXT NOT NULL" +
                //TodosContract.ListEntry.COLUMN_START_DATE + " INTEGER NOT NULL" +
                ");";

        final String SQL_CREATE_TODO_TABLE = "CREATE TABLE " + TodosContract.TodoEntry.TABLE_NAME + " (" +
                TodosContract.TodoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TodosContract.TodoEntry.COLUMN_LIST + " INTEGER NOT NULL," +
                //TodosContract.TodoEntry.COLUMN_ORDER + " INTEGER NOT NULL," +
                TodosContract.TodoEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                //TodosContract.TodoEntry.COLUMN_COMMENT + " TEXT," +
                TodosContract.TodoEntry.COLUMN_END_DATE + " INTEGER NOT NULL," +
                " FOREIGN KEY (" + TodosContract.TodoEntry.COLUMN_LIST + ") REFERENCES "
                + TodosContract.ListEntry.TABLE_NAME + " (" + TodosContract.ListEntry._ID + ")" +
                ");";

        db.execSQL(SQL_CREATE_LIST_TABLE);
        db.execSQL(SQL_CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TodosContract.TodoEntry.TABLE_NAME + ";");
        db.execSQL("DROP TABLE " + TodosContract.ListEntry.TABLE_NAME + ";");

        onCreate(db);
    }
}
