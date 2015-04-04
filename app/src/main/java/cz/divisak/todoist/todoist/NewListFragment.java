package cz.divisak.todoist.todoist;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
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
 * New list fragment.
 */
public class NewListFragment extends Fragment implements View.OnClickListener {

    private EditText title;

    public NewListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_new_list, container, false);

        title = (EditText)rootView.findViewById(R.id.list_title);
        title.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onClick(v);
                    return true;
                }
                return false;
            }
        });
        title.requestFocus();

        title.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;

                InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(title, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 50);


        rootView.findViewById(R.id.submit_list).setOnClickListener(this);

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
        String text = title.getText().toString();
        if (text == null || text.length() == 0) {
            Toast.makeText(getActivity(), R.string.please_fill_title, Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(TodosContract.ListEntry.COLUMN_TITLE, text);
        //cv.put(TodosContract.ListEntry.COLUMN_START_DATE, 0);

        Uri listUri = getActivity().getContentResolver().insert(TodosContract.ListEntry.CONTENT_URI, cv);
        long listId = TodosContract.ListEntry.getIdFromUri(listUri);

        InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

        ((TodosListFragment.TodosListFragmentCallbacks) getActivity()).onSelectedList(listId);
    }
}
