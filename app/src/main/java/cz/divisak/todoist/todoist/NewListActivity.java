package cz.divisak.todoist.todoist;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;


public class NewListActivity extends ActionBarActivity implements TodosListFragment.TodosListFragmentCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_list);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new NewListFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_list, menu);
        return true;
    }

    @Override
    public void onNewList() {
    }

    @Override
    public boolean onSelectedList(long id) {
        Intent i = new Intent(this, TodosListActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(TodosListActivity.EXTRA_SELECTED_LIST, id);
        startActivity(i);
        return true;
    }

    @Override
    public boolean isTabletLayout() {
        return false;
    }
}
