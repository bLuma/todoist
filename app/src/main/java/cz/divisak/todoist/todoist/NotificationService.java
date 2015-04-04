package cz.divisak.todoist.todoist;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.widget.Toast;

import java.util.Calendar;

import cz.divisak.todoist.todoist.data.TodosContract;

/**
 * Notification service - for showing, controlling and discarding of todos notification.
 */
public class NotificationService extends IntentService {

    public static final String ACTION_LAUNCH_NOTIFICATION = "launchNotification";
    public static final String ACTION_MARK_AND_CONTINUE = "markAndContinue";
    public static final String ACTION_CANCEL_NOTIFICATION = "cancelNotification";

    public static final String EXTRA_LIST_ID = "listId";
    public static final String EXTRA_TODO_ID = "todoId";
    public static final String EXTRA_FROM_WEAR = "fromWear";


    private static final String[] projection = {
            TodosContract.TodoEntry._ID,
            TodosContract.TodoEntry.COLUMN_TITLE,
            TodosContract.TodoEntry.COLUMN_END_DATE
    };

    private static final int IDX_ID = 0;
    private static final int IDX_TITLE = 1;
    private static final int IDX_END_DATE = 2;

    private static final int NOTIFICATION_ID = 1;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (intent.getAction().equals(ACTION_LAUNCH_NOTIFICATION)) {
                launchNotification(intent);
            } else if (intent.getAction().equals(ACTION_MARK_AND_CONTINUE)) {
                markAndContinue(intent);
            } else if (intent.getAction().equals(ACTION_CANCEL_NOTIFICATION)) {
                cancelNotification(intent);
            }
        }
    }

    private void launchNotification(Intent intent) {
        long listId = intent.getLongExtra(EXTRA_LIST_ID, -1);

        Cursor c = getContentResolver().query(
                TodosContract.TodoEntry.CONTENT_URI,
                projection,
                TodosContract.TodoEntry.COLUMN_LIST + " = ? AND " + TodosContract.TodoEntry.COLUMN_END_DATE + " = 0",
                new String[] { Long.toString(listId) },
                TodosContract.TodoEntry._ID + " ASC"
        );

        if (!c.moveToFirst()) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(NotificationService.this.getApplicationContext(), R.string.notification_todo_complete, Toast.LENGTH_SHORT).show();
                }
            });

            cancelNotification(intent);
            return;
        }

        long todoId = c.getLong(IDX_ID);
        String title = c.getString(IDX_TITLE);
        //Log.d("NS", "List " + listId + " Todo " + todoId);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        b.setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(title)
                .setAutoCancel(true);
        b.setTicker(getString(R.string.todo_in_notification));
        b.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(title));

        Intent ci = new Intent(this, TodosListActivity.class);
        ci.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ci.putExtra(TodosListActivity.EXTRA_SELECTED_LIST, listId);
        PendingIntent pci = PendingIntent.getActivity(this, 0, ci, PendingIntent.FLAG_UPDATE_CURRENT);

        b.setContentIntent(pci);

        // check & mark button
        Intent i = new Intent(this, NotificationService.class);
        i.setAction(ACTION_MARK_AND_CONTINUE);
        i.putExtra(EXTRA_LIST_ID, listId);
        i.putExtra(EXTRA_TODO_ID, todoId);
        PendingIntent pi = PendingIntent.getService(this, 1, i, PendingIntent.FLAG_UPDATE_CURRENT);

        b.addAction(android.R.drawable.ic_media_next, getString(R.string.notification_check_and_next), pi);

        // cancel button
        i = new Intent(this, NotificationService.class);
        i.setAction(ACTION_CANCEL_NOTIFICATION);
        pi = PendingIntent.getService(this, 2, i, PendingIntent.FLAG_UPDATE_CURRENT);

        b.addAction(android.R.drawable.ic_menu_delete, getString(R.string.notification_cancel), pi);

        // android wear support
        addWearFeatures(b, c, listId, todoId);

        // close cursor
        c.close();

        // show notification
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(NOTIFICATION_ID, b.build());
    }

    private void addWearFeatures(NotificationCompat.Builder builder, Cursor cursor, long listId, long todoId) {
        // next todos card
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.next_todos));
        boolean showMore = false;
        while (cursor.moveToNext()) {
            showMore = true;

            inboxStyle.addLine(Html.fromHtml("<strong>&#8226;</strong> ") + cursor.getString(IDX_TITLE));
        }
        NotificationCompat.Builder secondPageBuilder = new NotificationCompat.Builder(this);
        Notification secondPageNotification = secondPageBuilder.setStyle(inboxStyle).build();

        // wear action button
        Intent i = new Intent(this, NotificationService.class);
        i.setAction(ACTION_MARK_AND_CONTINUE);
        i.putExtra(EXTRA_LIST_ID, listId);
        i.putExtra(EXTRA_TODO_ID, todoId);
        i.putExtra(EXTRA_FROM_WEAR, true);
        PendingIntent pi = PendingIntent.getService(this, 3, i, PendingIntent.FLAG_UPDATE_CURRENT);

        // extend with wear features
        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
        if (showMore) {
            extender.addPage(secondPageNotification);
        }
        extender.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, getString(R.string.notification_check_and_next), pi));

        builder.extend(extender);
    }

    private void markAndContinue(Intent intent) {
        //Log.d("NS", "Mark and continue");
        long listId = intent.getLongExtra(EXTRA_LIST_ID, -1);
        long todoId = intent.getLongExtra(EXTRA_TODO_ID, -1);

        //Log.d("NS", "List " + listId + " Todo " + todoId);

        if (intent.hasExtra(EXTRA_FROM_WEAR) && intent.getBooleanExtra(EXTRA_FROM_WEAR, false)) {
            cancelNotification(intent);
        }

        ContentValues cv = new ContentValues();
        cv.put(TodosContract.TodoEntry.COLUMN_END_DATE, Calendar.getInstance().getTimeInMillis());

        getContentResolver().update(
                TodosContract.TodoEntry.buildTodoByListUri(listId),
                cv,
                TodosContract.TodoEntry._ID + " = ?",
                new String[] { Long.toString(todoId) }
        );

        launchNotification(intent);
    }

    private void cancelNotification(Intent intent) {
        //Log.d("NS", "Cancel notification");
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.cancel(NOTIFICATION_ID);
    }

}
