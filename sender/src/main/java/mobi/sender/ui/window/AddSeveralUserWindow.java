package mobi.sender.ui.window;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.R;
import mobi.sender.model.User;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.KeyboardUtils;
import mobi.sender.ui.adapter.AddSeveralContactAdapter;

public class AddSeveralUserWindow implements AddSeveralContactAdapter.CheckBoxListener {

    private Activity mAct;
    private List<User> userList;
    private Dialog dialog;
    private List<String> checkedList = new ArrayList<>();
    private FrameLayout flDone;
    private CheckBox cbSearch;
    private EditText etSearch;
    private DoneListener listener;
    private AddSeveralContactAdapter adapter;

    public AddSeveralUserWindow(Activity mAct, List<User> userList, DoneListener listener) {
        this.mAct = mAct;
        this.userList = userList;
        this.listener = listener;
    }

    public void show() {
        dialog = new Dialog(mAct, R.style.AppTheme);
        dialog.setContentView(getCtListView());
        dialog.show();
    }

    private View getCtListView() {
        LayoutInflater inflater = (LayoutInflater) mAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View currentView = inflater.inflate(R.layout.dialog_select_user, null);

        if(userList.size() == 0){
            currentView.findViewById(R.id.tv_empty_list).setVisibility(View.VISIBLE);
        }

        //make adapter
        adapter = new AddSeveralContactAdapter(mAct, userList, this);
        final ListView listView = (ListView) currentView.findViewById(R.id.lv_contacts);
        listView.setAdapter(adapter);

        //back press
        currentView.findViewById(R.id.fl_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        flDone = (FrameLayout) currentView.findViewById(R.id.fl_done);
        flDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDonePressed(checkedList, dialog);
            }
        });

        cbSearch = (CheckBox) currentView.findViewById(R.id.cb_search);
        final TextView tvTitle = (TextView) currentView.findViewById(R.id.tv_title);
        etSearch = (EditText) currentView.findViewById(R.id.et_search);
        FrameLayout flSearch = (FrameLayout) currentView.findViewById(R.id.fl_search);
        flSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbSearch.isChecked()) {
                    cbSearch.setChecked(false);
                    tvTitle.setVisibility(View.VISIBLE);
                    etSearch.setVisibility(View.GONE);
                    KeyboardUtils.visibleKeyboard(false, etSearch, mAct);
                    etSearch.setText("");
                } else {
                    cbSearch.setChecked(true);
                    tvTitle.setVisibility(View.GONE);
                    etSearch.setVisibility(View.VISIBLE);
                    etSearch.requestFocus();
                    KeyboardUtils.visibleKeyboard(true, etSearch, mAct);
                }
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
                filter(text.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.visibleKeyboard(false, etSearch, mAct);
                    return true;
                }
                return false;
            }
        });

        return currentView;
    }

    private void filter(String str) {
        List<User> nList = new ArrayList<>();
        for (User u : userList) {
            if (u.getName().toLowerCase().contains(str)) nList.add(u);
        }

        adapter.setNewUsers(nList);
    }

    @Override
    public void checkClick(User user, boolean status) {
        if (status) {
            checkedList.add(user.getUserId());
        } else {
            checkedList.remove(user.getUserId());
        }

        if (checkedList.size() > 0) {
            flDone.setVisibility(View.VISIBLE);
        } else {
            flDone.setVisibility(View.GONE);
        }
    }

    public interface DoneListener{
        void onDonePressed(List<String> chekedList, Dialog dialog);
    }
}
