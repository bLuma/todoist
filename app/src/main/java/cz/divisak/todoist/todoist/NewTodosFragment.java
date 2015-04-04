package cz.divisak.todoist.todoist;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import cz.divisak.todoist.todoist.data.TodosContract;

/**
 * New todo entry fragment
 */
public class NewTodosFragment extends Fragment implements View.OnClickListener{

    private EditText todo;

    public static final String ARGUMENT_LIST_ID = NewTodosActivity.EXTRA_LIST_ID;

    public NewTodosFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_todos, container, false);

        todo = (EditText)rootView.findViewById(R.id.todos_title);
        todo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onClick(v);
                    return true;
                }
                return false;
            }
        });
        todo.requestFocus();

        todo.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;

                InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(todo, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 50);

        rootView.findViewById(R.id.submit_todos).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getView().getApplicationWindowToken(), 0);
        } catch (NullPointerException ex) { }

        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        String text = todo.getText().toString();
        if (text == null || text.length() == 0) {
            Toast.makeText(getActivity(), R.string.please_fill_todo, Toast.LENGTH_SHORT).show();
            return;
        }

        long listId = getArguments().getLong(ARGUMENT_LIST_ID);

        ContentValues cv = new ContentValues();
        cv.put(TodosContract.TodoEntry.COLUMN_TITLE, text);
        cv.put(TodosContract.TodoEntry.COLUMN_LIST, listId);
        cv.put(TodosContract.TodoEntry.COLUMN_END_DATE, 0);

        /*Uri todoUri =*/ getActivity().getContentResolver().insert(TodosContract.TodoEntry.CONTENT_URI, cv);
        //long todoId = TodosContract.ListEntry.getIdFromUri(todoUri);

        InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

        ((TodosListFragment.TodosListFragmentCallbacks) getActivity()).onSelectedList(listId);
    }
}
