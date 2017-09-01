package mobi.sender.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import mobi.sender.tool.Tool;

/**
 * Created by vp
 * on 08.07.16.
 */
public class StateHolder {

    private static final String PREF_STATE = "state";
    private static final int STATE_NEW = 0;
    private static final int STATE_REGISTERED = 1;
    private static final int STATE_WALLET_SYNCED = 2;
    private static final int STATE_DIALOGS_SYNCED = 3;
    private static StateHolder instance;
    private SharedPreferences pref;

    private StateHolder(Context ctx) {
        this.pref = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static StateHolder getInstance(Context ctx) {
        if (instance == null) instance = new StateHolder(ctx);
        return instance;
    }

    public int getCurrState() {
        return pref.getInt(PREF_STATE, -1);
    }

    private int getState() {
        return pref.getInt(PREF_STATE, STATE_NEW);
    }

    public boolean isRegistered() {
        return getState() >= STATE_REGISTERED;
    }

    public boolean isWalletSynced() {
        return getState() >= STATE_WALLET_SYNCED;
    }

    public boolean isDialogsSynced() {
        return getState() >= STATE_DIALOGS_SYNCED;
    }

    public void setRegistered() {
        pref.edit().putInt(PREF_STATE, STATE_REGISTERED).apply();
    }

    public void setUnRegistered() {
        pref.edit().putInt(PREF_STATE, STATE_NEW).apply();
    }

    public void setWalletSynced() {
        pref.edit().putInt(PREF_STATE, STATE_WALLET_SYNCED).apply();
    }

    public void setWalletUnSynced() {
        pref.edit().putInt(PREF_STATE, STATE_REGISTERED).apply();
    }

    public void setDialogSynced() {
        pref.edit().putInt(PREF_STATE, STATE_DIALOGS_SYNCED).apply();
    }

    public void setDialogUnSynced() {
        pref.edit().putInt(PREF_STATE, STATE_WALLET_SYNCED).apply();
    }
}
