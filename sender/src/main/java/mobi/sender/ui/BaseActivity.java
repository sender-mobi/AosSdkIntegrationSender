package mobi.sender.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import mobi.sender.App;
import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.Sender;
import mobi.sender.event.AuthEvent;
import mobi.sender.event.ChatUpdatedEvent;
import mobi.sender.event.CompFormUpdatedEvent;
import mobi.sender.event.ForceOpenEvent;
import mobi.sender.event.MsgUpdatedEvent;
import mobi.sender.event.SetActStateReq;
import mobi.sender.event.StatusInternetEvent;
import mobi.sender.event.TypingEvent;
import mobi.sender.event.UpdateBarEvent;
import mobi.sender.tool.Notificator;
import mobi.sender.tool.Storage;

public abstract class BaseActivity extends AppCompatActivity implements Bus.Subscriber {

    private String locale = "";
    private Snackbar mSnackbar;
    private boolean flag = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        App.getInstance().changeTheme(this);
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        String savedLocale = Storage.getInstance(this).getLocale();
        locale = Locale.getDefault().getLanguage();
        if (!locale.equalsIgnoreCase(savedLocale)) {
            locale = savedLocale;
            getResources().getConfiguration().locale = new Locale(locale);
            getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());
        }

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currChat = getCurrChatId();
        if (currChat.equals(Sender.ALL_CHAT_ID)) {
            Notificator.getInstance(this).removeNotifications();
        } else if (currChat.length() > 0) {
            Notificator.getInstance(this).removeNotifications(currChat);
        }
        Bus.getInstance().post(new SetActStateReq(currChat, true));
        String savedLocale = Storage.getInstance(this).getLocale();
        if (!locale.equalsIgnoreCase(savedLocale)) {
            recreate();
        }

        registerBus(this);
        makeSnakBar(isNetworkAvailable());
    }

    protected abstract void fromServer(Bus.Event evt);

    protected String getCurrChatId() {
        return "";
    }

    @Override
    protected void onPause() {
        super.onPause();
        Bus.getInstance().post(new SetActStateReq(getCurrChatId(), false));
        unregisterBus(this);
    }

    public String getLocale() {
        return locale;
    }

    public void makeSnakBar(boolean isVisible) {
        if (!isVisible && flag) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSnackbar = Snackbar.make(findViewById(android.R.id.content), R.string.tst_connecting, Snackbar.LENGTH_INDEFINITE);
                    mSnackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
                    View snackbarView = mSnackbar.getView();
                    TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRedTransparent));
                    mSnackbar.setAction(R.string.tst_close, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            flag = false;
                        }
                    }).show();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSnackbar != null) {
                        mSnackbar.dismiss();
                    }
                }
            });
            makeLastRequest();
        }
    }

    public Storage getStor() {
        return Storage.getInstance(this);
    }

    public void makeLastRequest() {
    }

    protected void registerBus(Bus.Subscriber subscriber) {
        registerBaseEvents(subscriber);
    }

    public static void registerBaseEvents(Bus.Subscriber subscriber) {
        Bus.getInstance().register(subscriber, MsgUpdatedEvent.class.getSimpleName());
        Bus.getInstance().register(subscriber, ChatUpdatedEvent.class.getSimpleName());
        Bus.getInstance().register(subscriber, AuthEvent.class.getSimpleName());
        Bus.getInstance().register(subscriber, StatusInternetEvent.class.getSimpleName());
        Bus.getInstance().register(subscriber, ForceOpenEvent.class.getSimpleName());
        Bus.getInstance().register(subscriber, CompFormUpdatedEvent.class.getSimpleName());
        Bus.getInstance().register(subscriber, TypingEvent.class.getSimpleName());
        Bus.getInstance().register(subscriber, UpdateBarEvent.class.getSimpleName());
    }

    protected void unregisterBus(Bus.Subscriber subscriber) {
        Bus.getInstance().unregister(subscriber);
    }

    @Override
    public void onEvent(Bus.Event evt) {
        fromServer(evt);
    }
}
