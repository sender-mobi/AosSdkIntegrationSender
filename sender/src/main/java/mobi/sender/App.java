package mobi.sender;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;
import android.support.v7.app.AppCompatDelegate;

import java.util.List;
import java.util.Locale;

import mobi.sender.event.RegEvent;
import mobi.sender.tool.Notificator;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.ui.RegPhoneActivity;
import mobi.sender.ui.StartActivity;

/**
 * Created by vd on 8/8/16.
 */
public class App extends Application implements Bus.Subscriber {

    private static App instance;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onCreate() {
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        this.instance = this;
        Bus.getInstance().register(this, Bus.NotDeliveredEvent.class.getSimpleName());
        Bus.getInstance().register(this, RegEvent.class.getSimpleName());
        super.onCreate();

        String locale = Storage.getInstance(this).getLocale();
        if (!locale.equalsIgnoreCase(Locale.getDefault().getLanguage())) {
            getResources().getConfiguration().locale = new Locale(locale);
            getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onTerminate() {
        Bus.getInstance().unregister(this);
        super.onTerminate();
    }

    @Override
    public void onEvent(Bus.Event evt) {
        if (evt instanceof Bus.NotDeliveredEvent) {
            Tool.log("Event " + ((Bus.NotDeliveredEvent) evt).getEvent().getClass() + " not delivered!");
        }
        else if (evt instanceof RegEvent) {
            if (Bus.getInstance().isRegistered(RegPhoneActivity.class.getSimpleName())) return;
            Notificator.getInstance(getApplicationContext()).removeNotifications();
            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
            if(!isActivityAlreadyOpen()){
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            getApplicationContext().startActivity(intent);
        }
    }

    private boolean isActivityAlreadyOpen(){
        ActivityManager mngr = (ActivityManager) getSystemService( ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);
        return taskList.get(0).numActivities == 1 && taskList.get(0).topActivity.getClassName().equals(RegPhoneActivity.class.getName());
    }

    public static App getInstance() {
        return instance;
    }

    public void changeTheme(Activity activity) {
    }
}
