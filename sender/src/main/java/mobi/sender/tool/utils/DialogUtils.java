package mobi.sender.tool.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import mobi.sender.R;

/**
 * Created by Zver on 10.11.2016.
 */

public class DialogUtils {

    /**
     * Make input alert dialog with two buttons (Ok and Cancel)
     *
     * @param act         context
     * @param defaultText default text in input
     * @param message     message of alert
     * @param title       title of alert
     * @param listener    on buttons click listener
     */
    public static void inputDialog(Activity act, String defaultText, int message, int title, final OnDialogListener listener) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(act);
        if (message != 0) alert.setMessage(message);
        if (title != 0) alert.setTitle(title);

        View view = LayoutInflater.from(act).inflate(R.layout.item_edittext, null);
        final EditText etName = (EditText) view.findViewById(R.id.et_name);
        etName.setText(defaultText);
        etName.setSelection(defaultText.length());
        alert.setView(view);

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                listener.onYesClick(etName.getText().toString());
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = alert.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    /**
     * Make confirm alert dialog with two buttons (Ok and Cancel)
     *
     * @param act      context
     * @param message  message of alert
     * @param listener on buttons click listener
     */
    public static void confirmDilog(Activity act, int title, int message, final OnConfirmListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(act);
        if (title != 0) builder.setTitle(title);
        if (message != 0) builder.setMessage(message);
        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onYesClick();
            }
        }).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
    }

    /**
     * Make choose alert dialog with one negative button
     *
     * @param act      context
     * @param title    title of alert
     * @param items    array with name of items
     * @param listener on select listener
     */
    public static void chooseDialog(Activity act, int title, CharSequence[] items, final OnChooseListener listener) {
        AlertDialog.Builder adb = new AlertDialog.Builder(act);
        adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int i) {
                listener.onSelect(i);
                d.dismiss();
            }
        });
        adb.setNegativeButton(android.R.string.cancel, null);
        adb.setTitle(title);
        adb.show();
    }

    /**
     * Make items alert dialog
     *
     * @param ctx      context
     * @param title    title of alert
     * @param items    array with name of items
     * @param listener on select listener
     */
    public static void itemsDialog(Context ctx, int title, CharSequence[] items, final OnChooseListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onSelect(which);
            }
        });
        builder.show();
    }


    // Listeners
    public interface OnChooseListener {
        void onSelect(int position);
    }

    public interface OnConfirmListener {
        void onYesClick();
    }

    public interface OnDialogListener {
        void onYesClick(String etText);

        void onNoClick(String etText);
    }
}
