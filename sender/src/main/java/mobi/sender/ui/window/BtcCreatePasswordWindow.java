package mobi.sender.ui.window;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import mobi.sender.R;

public class BtcCreatePasswordWindow {
    private final AlertDialog dialog;

    public BtcCreatePasswordWindow(final Activity activity, final WindowCallback callback) {
        View vv = LayoutInflater.from(activity).inflate(R.layout.btc_change_pass, null);
        final EditText pass1 = (EditText) vv.findViewById(R.id.pass1);
        final EditText pass2 = (EditText) vv.findViewById(R.id.pass2);
        vv.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(activity);
                callback.onCancel();
            }
        });
        vv.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pass1.getText() != null && pass2.getText() != null && !pass2.getText().toString().isEmpty() && pass1.getText().toString().equals(pass2.getText().toString())) {
                    dismiss(activity);
                    callback.onCreatePassword(pass1.getText().toString());
                } else Toast.makeText(activity, R.string.tst_pass_incorrect, Toast.LENGTH_LONG).show();
            }
        });
        this.dialog = new AlertDialog.Builder(activity).setView(vv).create();
    }

    private void dismiss(Activity activity) {
        dialog.dismiss();
    }

    public void show() {
        dialog.show();
    }

    public interface WindowCallback {
        void onCreatePassword(String password);

        void onCancel();
    }

}
