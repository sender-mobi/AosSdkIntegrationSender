package mobi.sender.tool.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import mobi.sender.R;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by Zver on 24.09.2016.
 */
public class UiUtils {

    public static void initToolbar(AppCompatActivity act, String title, boolean visibleHomeButton) {
        Toolbar toolbar = (Toolbar) act.findViewById(R.id.toolbar);
        act.setSupportActionBar(toolbar);
        ActionBar actionBar = act.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(visibleHomeButton);
            actionBar.setDisplayShowHomeEnabled(visibleHomeButton);
            actionBar.setTitle(title);
        }
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }
}
