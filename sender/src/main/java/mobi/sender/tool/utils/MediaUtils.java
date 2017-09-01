package mobi.sender.tool.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mobi.sender.R;
import mobi.sender.tool.Tool;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.window.FileChooseWindow;

/**
 * Created by Zver on 18.11.2016.
 */

public class MediaUtils {

    public static void openCamera(Activity act) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(act.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = Tool.createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                ((ChatActivity) act).photoAbsolutePath = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(act,
                        act.getString(R.string.provider_str),
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                //For Lenovo
                List<ResolveInfo> resInfoList = act.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    act.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                //...end

                if (ActivityCompat.checkSelfPermission(act, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.CAMERA}, ChatActivity.REQUEST_CAMERA);
                } else {
                    act.startActivityForResult(intent, ChatActivity.CAPTURE_IMAGE);
                }
            }
        }
    }

    public static void openGallary(Activity act) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(act.getPackageManager()) != null) {
            act.startActivityForResult(intent, ChatActivity.REQUEST_IMAGE);
        }
    }

    public static void openFileChoose(final Activity act, final OnSelectListener listener) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(act.getPackageManager()) != null) {
            act.startActivityForResult(intent, ChatActivity.REQUEST_FILE);
        } else {
            new FileChooseWindow(act, new FileChooseWindow.OnFileSelectedListener() {
                @Override
                public void onFileSelected(File file) {
                    listener.onSelect(file);
                }

                @Override
                public void onCancel() {
                }
            }).show();
        }
    }

    public static InputStream getInputStreamFromUri(Context ctx, Uri uri) {
        if (uri == null) {
            return null;
        }

        InputStream is = null;
        try {
            is = ctx.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException ex) {
            try {
                is = new FileInputStream(uri.toString());
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        return is;
    }

    //Interfaces
    public interface OnSelectListener {
        void onSelect(File file);
    }

    public static String getPhone(Context ctx, String pref) {
        String phoneNumber = "";
        try {
            TelephonyManager tMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            phoneNumber = tMgr.getLine1Number();

            if (phoneNumber.startsWith(pref)) {
                phoneNumber = phoneNumber.replace(pref, "");
            } else {
                phoneNumber = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return phoneNumber;
    }

    public static String getTypeFile(String url) {
        int index = url.lastIndexOf(".");
        return url.substring(index+1, url.length());
    }
}
