package cz.divisak.todoist.todoist;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cz.divisak.todoist.todoist.data.TodosContract;

/**
 * Todo list fragment.
 */
public class TodosListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LAST_SELECTED_LIST = "lastSelectedList";

    private CursorAdapter adapter;
    private ListView list;
    //private ActionMode actionMode;
    private long selectedList;

    private ProgressBar loadingBar;

    private static final int CURSOR_LOADER = 0;

    private static final String[] projection = {
            TodosContract.ListEntry._ID,
            TodosContract.ListEntry.COLUMN_TITLE
            //TodosContract.ListEntry.COLUMN_START_DATE
    };

    private static final int IDX_ID = 0;
    private static final int IDX_TITLE = 1;
    private static final int IDX_START_DATE = 2;

    /**
     * Callbacks for activities that reacts to TodosListFragment.
     */
    public interface TodosListFragmentCallbacks {
        public boolean isTabletLayout();

        public void onNewList();

        public boolean onSelectedList(long id);
    }

    public TodosListFragment() {
        //setHasOptionsMenu(true);
    }

    public void onNewListCallback() {
        if (selectedList > 0) {
            int position = getPositionFromId(selectedList);
            list.setItemChecked(position, false);
        }
        selectedList = -2;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(LAST_SELECTED_LIST, selectedList);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(CURSOR_LOADER, new Bundle(), this);

        if (savedInstanceState != null && savedInstanceState.containsKey(LAST_SELECTED_LIST)) {
            selectedList = savedInstanceState.getLong(LAST_SELECTED_LIST);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader loader = new CursorLoader(getActivity(), TodosContract.ListEntry.CONTENT_URI,
                projection, null, null, TodosContract.ListEntry._ID + " DESC");

        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);

        if (loadingBar != null) {
            loadingBar.setVisibility(View.GONE);
        }

        if (selectedList > 0) {
            list.smoothScrollToPosition(getPositionFromId(selectedList));
        }

        if (((TodosListFragmentCallbacks) getActivity()).isTabletLayout()) {
            if (selectedList == -2) {
                return;
            }

            if (!cursor.moveToFirst()) {
                if (selectedList == 0) {
                    selectedList = -2;
                    getActivity().findViewById(R.id.list_fragment).post(new Runnable() {
                        @Override
                        public void run() {
                            ((TodosListFragmentCallbacks)getActivity()).onNewList();
                        }
                    });
                }
                return;
            }

            final long listId = cursor.getLong(IDX_ID);

            getActivity().findViewById(R.id.list_fragment).post(new Runnable() {
                @Override
                public void run() {
                    // got selected list from before (device rotation,...)?
                    if (selectedList != 0) {
                        setSelectedList(selectedList);
                        return;
                    }

                    // or new fragment - check first one and publish to activity
                    if (((TodosListFragmentCallbacks) getActivity()).onSelectedList(listId)) {
                        selectedList = listId;

                        //Log.d("TodosList", "Checking 0");
                        list.setItemChecked(0, true);
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_todos_list, container, false);
        list = (ListView) rootView.findViewById(R.id.list);
        adapter = new CursorAdapter(getActivity(), null, 0) {

            class ViewHolder {
                TextView title;

                ViewHolder(View view) {
                    this.title = (TextView) view.findViewById(R.id.text);
                }
            }

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View view = LayoutInflater.from(context).inflate(R.layout.simple_list_item, parent, false);
                view.setTag(new ViewHolder(view));

                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                ((ViewHolder)view.getTag()).title.setText(cursor.getString(IDX_TITLE));
            }
        };

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = adapter.getCursor();
                c.moveToPosition(position);
                long listId = c.getLong(IDX_ID);

                if (((TodosListFragmentCallbacks) getActivity()).isTabletLayout()) {
                    //Log.d("TodosList", "Checking position (onclick) " + position);
                    list.setItemChecked(position, true);
                    selectedList = listId;
                }

                ((TodosListFragmentCallbacks) getActivity()).onSelectedList(listId);
            }
        });

        loadingBar = (ProgressBar)rootView.findViewById(R.id.progressbar);

        return rootView;
    }

    /**
     * Set selected (checked) list in ListView by listId
     *
     * @param listId
     */
    public void setSelectedList(long listId) {
        if (adapter == null)
            return;

        selectedList = listId;

        int position = getPositionFromId(listId);
        //Log.d("TodosList", "Checking position (setselectedlist) " + position);
        list.setItemChecked(position, true);
    }

    /**
     * Get list position from listId
     *
     * @param listId
     * @return
     */
    private int getPositionFromId(long listId) {
        if (adapter == null)
            return 0;

        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItemId(i) == listId) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Get first available listId
     *
     * @param ignoreList
     * @return
     */
    public long getFirstListId(long ignoreList) {
        if (adapter == null || adapter.getCount() == 0)
            return -1;

        for (int i = 0; i < adapter.getCount(); i++) {
            long id = adapter.getItemId(i);
            if (id != ignoreList) {
                return id;
            }
        }

        return -1;
    }
}
