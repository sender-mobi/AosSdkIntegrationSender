package mobi.sender.ui.window;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mobi.sender.R;
import mobi.sender.tool.Tool;
import mobi.sender.ui.adapter.FileListAdapter;

public class FileChooseWindow {

    private Activity mAct;
    private OnFileSelectedListener mListener;
    private AlertDialog dialog;

    private TextView tvPatch;
    private List<File> files = new ArrayList<>();
    private String currPath;
    private FileListAdapter adapter;

    public FileChooseWindow(Activity mAct, OnFileSelectedListener mListener) {
        this.mAct = mAct;
        this.mListener = mListener;
    }

    public void show() {
        dialog = new AlertDialog.Builder(mAct).setView(getCtListView()).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mListener.onCancel();
            }
        }).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                scan();
            }
        });
        dialog.show();
    }

    private boolean refresh() {
        if (currPath.trim().equalsIgnoreCase("/") || currPath.equals("")) return true;
        currPath = currPath.substring(0, currPath.lastIndexOf("/"));
        if (currPath.equals("")) return true;
        scan();
        return false;
    }

    private void scan() {
        if (!Tool.isMediaMounted()) {
            Toast.makeText(mAct, "Storage is not mounted!", Toast.LENGTH_LONG).show();
            dialog.dismiss();
            return;
        }
        if (currPath == null)
            currPath = Environment.getExternalStorageDirectory().getPath();

        File tf = new File(currPath);
        if (tf.exists() && !tf.getPath().contains("sdcard0/storage")) currPath = tf.getPath();
        if (currPath == null || !currPath.startsWith("/")) {
            currPath = "/";
        }

        files = new ArrayList<>();
        File[] ff = new File(currPath).listFiles();
        if (ff != null)

        {
            for (File file : ff) {
                if (file.getName().startsWith(".")) continue;
                files.add(file);
            }
        }

        tvPatch.setText(currPath);
        adapter.setFiles(files);
    }


    private View getCtListView() {
        LayoutInflater inflater = (LayoutInflater) mAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View currentView = inflater.inflate(R.layout.ac_fmanager, null);

        tvPatch = (TextView) currentView.findViewById(R.id.tvPatch);
        ListView flv = (ListView) currentView.findViewById(R.id.lvFiles);
        adapter = new FileListAdapter(mAct);
        flv.setAdapter(adapter);
        flv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File f = files.get(position);
                if (f.isDirectory()) {
                    currPath = f.getPath();
                    scan();
                } else {
                    mListener.onFileSelected(f);
                    dialog.dismiss();
                }
            }
        });
        currentView.findViewById(R.id.tvUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        return currentView;
    }

    public interface OnFileSelectedListener {
        void onFileSelected(File file);
        void onCancel();
    }
}
