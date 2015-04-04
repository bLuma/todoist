package cz.divisak.todoist.todoist;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;

/**
 * TodosActivity - for showing of todo entries (mobile layout)
 */
public class TodosActivity extends ActionBarActivity implements TodosFragment.TodosFragmentCallbacks, TodosListFragment.TodosListFragmentCallbacks {

    public static final String EXTRA_LIST_ID = "listId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todos);

        if (savedInstanceState == null) {
            TodosFragment fragment = new TodosFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_todos, menu);
        return true;
    }

    @Override
    public void onNewTodos(long id) {
        //Log.d("TodosActivity", "onNewTodos("  + id + ")");
        Intent i = new Intent(this, NewTodosActivity.class);
        i.putExtra(NewTodosActivity.EXTRA_LIST_ID, id);
        startActivity(i);
    }

    @Override
    public boolean isTabletLayout() {
        return false;
    }

    @Override
    public void onNewList() {
    }

    @Override
    public boolean onSelectedList(long id) {
        Intent i = new Intent(this, TodosListActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (id >= 0) {
            i.putExtra(TodosListActivity.EXTRA_SELECTED_LIST, id);
        }

        startActivity(i);
        return false;
    }
}
