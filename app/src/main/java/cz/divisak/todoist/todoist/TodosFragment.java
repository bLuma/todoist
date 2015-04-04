package cz.divisak.todoist.todoist;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.divisak.todoist.todoist.data.TodosContract;

/**
 * Todo entry listing fragment.
 */
public class TodosFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARGUMENT_LIST_ID = TodosActivity.EXTRA_LIST_ID;

    private CursorAdapter adapter;
    private ActionMode actionMode;
    private ListView list;
    private ShareActionProvider shareActionProvider;

    private ProgressBar loadingBar;

    private static final int CURSOR_LOADER = 0;

    private static final String[] projection = {
            TodosContract.TodoEntry._ID,
            TodosContract.TodoEntry.COLUMN_TITLE,
            TodosContract.TodoEntry.COLUMN_END_DATE
    };

    private static final int IDX_ID = 0;
    private static final int IDX_TITLE = 1;
    private static final int IDX_END_DATE = 2;

    /**
     * Callbacks for activities that reacts to TodosFragment.
     */
    public interface TodosFragmentCallbacks {
        public void onNewTodos(long id);
    }

    public TodosFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_todos_fragment, menu);

        MenuItem shareItem = menu.findItem(R.id.share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if (shareActionProvider != null) {
            setShareIntent();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.notification) {
            launchNotification();
            return true;
        } else if (item.getItemId() == R.id.delete) {
            showDeleteListDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setShareIntent() {
        if (shareActionProvider == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        Cursor cursor = adapter.getCursor();
        for (int i = 0; i < adapter.getCount(); i++) {
            cursor.moveToPosition(i);

            if (cursor.getLong(IDX_END_DATE) != 0)
                builder.append("[X]");
            else
                builder.append("[ ]");

            builder.append(' ').append(cursor.getString(IDX_TITLE));
            builder.append("\n");
        }
        String string = builder.toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, string);

        shareActionProvider.setShareIntent(intent);
    }

    private void launchNotification() {
        //Log.d("TF", "Start notification");
        Intent i = new Intent(getActivity(), NotificationService.class);
        i.setAction(NotificationService.ACTION_LAUNCH_NOTIFICATION);
        i.putExtra(NotificationService.EXTRA_LIST_ID, getArguments().getLong(ARGUMENT_LIST_ID));

        getActivity().startService(i);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(CURSOR_LOADER, getArguments(), this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }

        super.onDestroyView();
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (bundle == null || !bundle.containsKey(ARGUMENT_LIST_ID))
            return null;

        //Log.d("Todos", "Loading list " + bundle.getLong(ARGUMENT_LIST_ID));
        CursorLoader loader = new CursorLoader(getActivity(),
                TodosContract.TodoEntry.buildTodoByListUri(bundle.getLong(ARGUMENT_LIST_ID)),
                projection,
                null,
                null,
                TodosContract.TodoEntry._ID + " ASC");
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        //Log.d("TodosFragment", "Cursor loaded!");
        adapter.swapCursor(cursor);

        if (shareActionProvider != null) {
            setShareIntent();
        }

        if (loadingBar != null) {
            loadingBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_todos, container, false);
        list = (ListView) rootView.findViewById(R.id.list);
        adapter = new CursorAdapter(getActivity(), null, 0) {

            class ViewHolder {
                TextView todo;
                CheckBox checkbox;

                ViewHolder(View view) {
                    this.todo = (TextView) view.findViewById(R.id.text);
                    this.checkbox = (CheckBox) view.findViewById(R.id.checkbox);
                }
            }

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View view = LayoutInflater.from(context).inflate(R.layout.todos_list_item, parent, false);
                view.setTag(new ViewHolder(view));

                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                ((ViewHolder) view.getTag()).todo.setText(cursor.getString(IDX_TITLE));
                ((ViewHolder) view.getTag()).checkbox.setChecked(cursor.getLong(IDX_END_DATE) != 0);
            }
        };

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = adapter.getCursor();
                c.moveToPosition(position);
                long todoId = c.getLong(IDX_ID);
                boolean state = ((CheckBox) view.findViewById(R.id.checkbox)).isChecked();

                toggleTodo(state, todoId);
            }
        });


        list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = list.getCheckedItemCount();

                switch (checkedCount) {
                    case 0:
                        mode.setSubtitle(null);
                        break;
                    case 1:
                        mode.setSubtitle(R.string.am_one_item_selected);
                        break;
                    default:
                        mode.setSubtitle("" + checkedCount + " " + getString(R.string.am_n_items_selected));
                        break;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getActivity().getMenuInflater().inflate(R.menu.list_action_mode_menu, menu);
                mode.setTitle(R.string.app_name);
                actionMode = mode;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.delete) {
                    showDeleteTodosDialog();
                    return true;
                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
            }
        });

        rootView.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TodosFragmentCallbacks) getActivity()).onNewTodos(getArguments().getLong(ARGUMENT_LIST_ID));
            }
        });

        loadingBar = (ProgressBar)rootView.findViewById(R.id.progressbar);

        return rootView;
    }

    private void toggleTodo(boolean isDone, long id) {
        //Log.d("toogleTodo", "isdone " + isDone + " id " + id);

        ContentValues cv = new ContentValues();
        cv.put(TodosContract.TodoEntry.COLUMN_END_DATE, !isDone ? Calendar.getInstance().getTimeInMillis() : 0);

        getActivity().getContentResolver().update(
                TodosContract.TodoEntry.buildTodoByListUri(getArguments().getLong(ARGUMENT_LIST_ID)),
                cv,
                TodosContract.TodoEntry._ID + " = ?",
                new String[]{Long.toString(id)}
        );
    }

    private void deleteTodo(long id) {
        ContentResolver cr = getActivity().getContentResolver();

        cr.delete(
                TodosContract.TodoEntry.CONTENT_URI,
                TodosContract.TodoEntry._ID + " = ?",
                new String[]{Long.toString(id)}
        );
    }

    private void showDeleteListDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        long listId = getArguments().getLong(ARGUMENT_LIST_ID);
                        deleteList(listId);
                        ((TodosListFragment.TodosListFragmentCallbacks) getActivity()).onSelectedList(-listId);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setMessage(R.string.confirm_delete_todo_list)
                .setPositiveButton(R.string.yes, listener)
                .setNegativeButton(R.string.no, listener)
                .show();
    }

    private void showDeleteTodosDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: {
                        Cursor c = adapter.getCursor();
                        List<Long> ids = new ArrayList<>();

                        for (int i = 0; i < list.getCount(); i++) {
                            if (list.isItemChecked(i)) {
                                c.moveToPosition(i);
                                ids.add(c.getLong(IDX_ID));
                            }
                        }

                        for (Long id : ids) {
                            //Log.d("AM", "Delete " + id);
                            deleteTodo(id);
                        }

                        //Log.d("AM", "Delete fin!");
                        actionMode.finish();
                        actionMode = null;
                        break;
                    }

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setMessage(R.string.confirm_delete_todo_entries)
                .setPositiveButton(R.string.yes, listener)
                .setNegativeButton(R.string.no, listener)
                .show();
    }

    private void deleteList(long id) {
        ContentResolver cr = getActivity().getContentResolver();
        cr.delete(
                TodosContract.TodoEntry.CONTENT_URI,
                TodosContract.TodoEntry.COLUMN_LIST + " = ?",
                new String[]{Long.toString(id)}
        );

        cr.delete(
                TodosContract.ListEntry.CONTENT_URI,
                TodosContract.ListEntry._ID + " = ?",
                new String[]{Long.toString(id)}
        );
    }
}
