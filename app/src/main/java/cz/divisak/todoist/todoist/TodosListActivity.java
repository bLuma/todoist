package cz.divisak.todoist.todoist;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;

/**
 * TodosListActivity - for showing of todo lists (mobile/tablet layout).
 */
public class TodosListActivity extends ActionBarActivity implements TodosListFragment.TodosListFragmentCallbacks, TodosFragment.TodosFragmentCallbacks {

    public static final String EXTRA_SELECTED_LIST = "selectedList";

    private static final String DETAIL_FRAGMENT_TAG = "detailFragmentTag";

    private boolean isTabletLayout;
    private long selectedList;

    private com.getbase.floatingactionbutton.FloatingActionButton fabNewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todos_list);

        fabNewList = (FloatingActionButton)findViewById(R.id.fab_new_list);
        fabNewList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TodosListFragment)getSupportFragmentManager()
                        .findFragmentById(R.id.list_fragment))
                        .onNewListCallback();
                onNewList();

                if (isTabletLayout) {
                    fabNewList.setVisibility(View.GONE);
                }
            }
        });

        if (findViewById(R.id.detail_fragment) != null) {
            //if (savedInstanceState == null) {
                //getSupportFragmentManager().beginTransaction()
                //       .add(R.id.container, new TodosListFragment(), DETAIL_FRAGMENT_TAG)
                //        .commit();
            //}

            isTabletLayout = true;
        } else {
            isTabletLayout = false;
        }

        if (getIntent() != null && getIntent().hasExtra(EXTRA_SELECTED_LIST) &&
                (savedInstanceState == null || !savedInstanceState.containsKey(EXTRA_SELECTED_LIST))) {
            //Log.d("Act", "Restored selected list (intent): " + getIntent().getLongExtra(EXTRA_SELECTED_LIST, -1));
            onSelectedList(getIntent().getLongExtra(EXTRA_SELECTED_LIST, -1));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(EXTRA_SELECTED_LIST, selectedList);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if (isTabletLayout && savedInstanceState.containsKey(EXTRA_SELECTED_LIST)) {
            selectedList = savedInstanceState.getLong(EXTRA_SELECTED_LIST, -1);
            //Log.d("Act", "Restored selected list: " + selectedList);
            if (fabNewList != null && selectedList < 0) {
                fabNewList.setVisibility(View.GONE);
            }
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_todos_list, menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(EXTRA_SELECTED_LIST)) {
            onSelectedList(intent.getLongExtra(EXTRA_SELECTED_LIST, -1));
        }
    }

    @Override
    public void onNewList() {
        //Log.d("Act", "onNewList()");
        selectedList = -1;

        if (isTabletLayout) {
            fabNewList.setVisibility(View.GONE);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_fragment, new NewListFragment(), DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent i = new Intent(this, NewListActivity.class);
            startActivity(i);
        }
    }

    @Override
    public boolean onSelectedList(long id) {
        //Log.d("Act", "Selected: " + id);
        if (isTabletLayout) {
            fabNewList.setVisibility(View.VISIBLE);
        }

        // if selecting list after list deletion
        if (id < 0) {
            // find first nondeleted list
            id = ((TodosListFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.list_fragment))
                    .getFirstListId(-id);
            //Log.d("Act", "Selected forced: " + id);

            // none found, then open new list fragment
            if (id < 0) {
                //Log.d("Act", "Going to new list!");
                onNewList();
                return true;
            }
        }

        // same list and tablet? do nothing...
        if (selectedList == id && isTabletLayout) {
            //Log.d("Act", "List already active! Do nothing");
            return true;
        }

        selectedList = id;

        if (isTabletLayout) {
            //Log.d("Act", "Launching new fragment!");
            Bundle bundle = new Bundle();
            bundle.putLong(TodosFragment.ARGUMENT_LIST_ID, id);

            TodosFragment fragment = new TodosFragment();
            fragment.setArguments(bundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_fragment, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();

            ((TodosListFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.list_fragment))
                    .setSelectedList(id);
        }else {
            //Log.d("Act", "Launching activity with list");
            Intent i = new Intent(this, TodosActivity.class);
            i.putExtra(TodosActivity.EXTRA_LIST_ID, id);
            startActivity(i);
        }

        return true;
    }

    @Override
    public void onNewTodos(long id) {
        if (isTabletLayout) {
            fabNewList.setVisibility(View.GONE);
        }

        selectedList = -1;

        Bundle bundle = new Bundle();
        bundle.putLong(NewTodosFragment.ARGUMENT_LIST_ID, id);

        NewTodosFragment fragment = new NewTodosFragment();
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_fragment, fragment, DETAIL_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public boolean isTabletLayout() {
        return isTabletLayout;
    }
}
