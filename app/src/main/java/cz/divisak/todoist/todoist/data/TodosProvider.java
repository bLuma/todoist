package cz.divisak.todoist.todoist.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class TodosProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = buidlUriMatcher();

    private static final int LIST = 100;
    private static final int TODO = 200;
    private static final int TODO_BY_LIST = 201;
    private static final int TODO_BY_ID = 202;

    private TodosDbHelper dbHelper;

    public TodosProvider() {
    }

    private static UriMatcher buidlUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(TodosContract.CONTENT_AUTHORITY, TodosContract.PATH_LIST, LIST);
        matcher.addURI(TodosContract.CONTENT_AUTHORITY, TodosContract.PATH_TODO, TODO);
        matcher.addURI(TodosContract.CONTENT_AUTHORITY, TodosContract.PATH_TODO + "/list/#", TODO_BY_LIST);
        matcher.addURI(TodosContract.CONTENT_AUTHORITY, TodosContract.PATH_TODO + "/id/#", TODO_BY_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new TodosDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);

        switch(match) {
            case LIST:
                return TodosContract.ListEntry.CONTENT_TYPE;
            case TODO:
                return TodosContract.TodoEntry.CONTENT_TYPE;
            case TODO_BY_LIST:
                return TodosContract.TodoEntry.CONTENT_TYPE;
            case TODO_BY_ID:
                return TodosContract.TodoEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int match = uriMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            case LIST: {
                cursor = db.query(
                        TodosContract.ListEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case TODO: {
                cursor = db.query(
                        TodosContract.TodoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case TODO_BY_LIST: {
                cursor = db.query(
                        TodosContract.TodoEntry.TABLE_NAME,
                        projection,
                        TodosContract.TodoEntry.COLUMN_LIST + " = ?",
                        new String[] { Long.toString(TodosContract.TodoEntry.getListIdByUri(uri)) },
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case TODO_BY_ID: {
                cursor = db.query(
                        TodosContract.TodoEntry.TABLE_NAME,
                        projection,
                        TodosContract.TodoEntry._ID + " = ?",
                        new String[] { Long.toString(TodosContract.TodoEntry.getTodoIdByUri(uri)) },
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri " + uri + " / " + match);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case LIST: {
                long id = db.insert(TodosContract.ListEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = TodosContract.ListEntry.buildListUri(id);
                else
                    throw new SQLException("Insert fail " + uri);
                break;
            }

            case TODO: {
                long id = db.insert(TodosContract.TodoEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = TodosContract.TodoEntry.buildTodoByIdUri(id);
                else
                    throw new SQLException("Insert fail " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri " + uri + " / " + match);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int updatedRows;

        switch (match) {
            case LIST: {
                updatedRows = db.update(TodosContract.ListEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            case TODO:
            case TODO_BY_ID:
            case TODO_BY_LIST: {
                updatedRows = db.update(TodosContract.TodoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri " + uri + " / " + match);
        }

        if (updatedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updatedRows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int deletedRows;

        switch (match) {
            case LIST: {
                deletedRows = db.delete(TodosContract.ListEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case TODO: {
                deletedRows = db.delete(TodosContract.TodoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri " + uri + " / " + match);
        }

        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedRows;
    }
}
