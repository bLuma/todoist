package cz.divisak.todoist.todoist;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;


/**
 * NewTodosActivity - adding new todo entry (mobile layout)
 */
public class NewTodosActivity extends ActionBarActivity implements TodosListFragment.TodosListFragmentCallbacks{

    public static final String EXTRA_LIST_ID = "listId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_todos);
        if (savedInstanceState == null) {
            NewTodosFragment fragment = new NewTodosFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_todos, menu);
        return true;
    }

    @Override
    public void onNewList() {
    }

    @Override
    public boolean onSelectedList(long id) {
        Intent i = new Intent(this, TodosActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(TodosActivity.EXTRA_LIST_ID, id);

        startActivity(i);

        return true;
    }

    @Override
    public boolean isTabletLayout() {
        return false;
    }
}
