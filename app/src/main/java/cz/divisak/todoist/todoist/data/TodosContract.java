package cz.divisak.todoist.todoist.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.List;

/**
 * Todos database contract.
 */
public class TodosContract {
    public static final String CONTENT_AUTHORITY = "cz.divisak.todoist.todoist";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_LIST = "list";
    public static final String PATH_TODO = "todo";

    public static final class ListEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LIST).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"+ PATH_LIST;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+ "/" + CONTENT_AUTHORITY + "/"+ PATH_LIST;

        public static final String TABLE_NAME = "list";

        public static final String COLUMN_TITLE = "title";
        //public static final String COLUMN_START_DATE = "start_date";

        public static Uri buildListUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static long getIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

    }

    public static final class TodoEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TODO).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"+ PATH_TODO;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE+ "/" + CONTENT_AUTHORITY + "/"+ PATH_TODO;

        public static final String TABLE_NAME = "todo";

        public static final String COLUMN_LIST = "list";
        //public static final String COLUMN_ORDER = "orderer";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_END_DATE = "end_date";
        //public static final String COLUMN_COMMENT = "comment";

        public static Uri buildTodoByListUri(long listId) {
            return CONTENT_URI.buildUpon().appendPath("list").appendPath(Long.toString(listId)).build();
        }

        public static long getListIdByUri(Uri uri) {
            List<String> segments = uri.getPathSegments();
            if (segments.get(1).equals("list"))
                return Long.parseLong(segments.get(2));

            throw new UnsupportedOperationException("Unknown uri " + uri);
        }

        public static Uri buildTodoByIdUri(long todoId) {
            return CONTENT_URI.buildUpon().appendPath("id").appendPath(Long.toString(todoId)).build();
        }

        public static long getTodoIdByUri(Uri uri) {
            List<String> segments = uri.getPathSegments();
            if (segments.get(1).equals("id"))
                return Long.parseLong(segments.get(2));

            throw new UnsupportedOperationException("Unknown uri " + uri);
        }
    }
}
