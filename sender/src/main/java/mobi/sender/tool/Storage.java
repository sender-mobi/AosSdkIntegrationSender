package mobi.sender.tool;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.sender.library.ChatFacade;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import mobi.sender.model.ChatBased;
import mobi.sender.model.Dialog;
import mobi.sender.model.OperatorModel;
import mobi.sender.model.StateHolder;
import mobi.sender.model.User;
import mobi.sender.model.msg.MsgBased;
import mobi.sender.model.msg.TextMessage;
import mobi.sender.tool.bar.SendBar;
import mobi.sender.tool.utils.MediaUtils;

/**
 * Created by vp on 15.07.16.
 */
public class Storage extends SQLiteOpenHelper {

    private static final int VERSION = 4;
    public static final String PROP_ONLINE = "online";
    public static final String PROP_FULL_VER = "full_ver";
    public static final String PROP_PHONE_PREF = "phone_pref";
    public static final String PROP_LOC_LAT = "l_lat";
    public static final String PROP_LOC_LON = "l_lon";
    public static final String PROP_MY_NAME = "my_name";
    public static final String PROP_MY_PHONE = "my_phone";
    public static final String PROP_MY_PHOTO = "my_photo";
    public static final String PROP_MY_DESC = "my_desc";
    public static final String PROP_USERID = "my_userId";
    public static final String PROP_SEND_STATUS_READ = "send_status_read";
    public static final String PROP_NOTIFICATIONS_SOUND = "notifications_sound";
    public static final String PROP_NOTIFICATIONS = "notifications";
    public static final String PROP_GPS = "set_gps";
    public static final String PROP_LOCALE = "my_locale";
    public static final String PROP_DEF_BAR = "def_bar";
    public static final String PROP_BAR = "bar_";
    public static final String PROP_BAR_O = "bar_oper_";
    public static final String PROP_ENCR_CHATS = "encr_chats";
    public static final String PROP_CHAT_HOLE_PREFIX = "chat_hole_";
    public static final String PROP_CHAT_MEMBERS_PREFIX = "chat_members_";
    public static final String PROP_COMPANIES_ID = "companies_id";
    public static final String PROP_DEF_COMP_FORM = "def_comp_form";
    public static final String PROP_COMP_FORM_PREFIX_ = "comp_form_prefix";
    public static final String PROP_TAB_POSITION = "tab_position";
    public static final String PROP_FAVORITE = "prop_favorite";
    public static final String PROP_USERS = "prop_users";
    public static final String PROP_GROUP = "prop_group";
    public static final String PROP_COMP = "prop_comp";
    public static final String PROP_KEYBOARD_HEIGHT = "keyboard_height";
    public static final String PROP_CHAT_PAUSE = "chat_pause_";
    public static final String PROP_DIALOGS_THAT_I_LEFT = "dialogs_that_i_left";
    public static final String PROP_OPER_USERS = "oper_users";
    public static final String PROP_DEV_ID = "dev_id";
    public static final String PROP_DEV_KEY = "dev_key";
    public static final String PROP_AUTH_TOKEN = "auth_token";
    public static final String PROP_COMPANY_ID = "company_id";
    public static final String PROP_UDID = "udid_";
    private static final String PROP_SID = "sid";
    private static final String PROP_LONGITUDE = "longitude";
    private static final String PROP_LATITUDE = "latitude";
    private static final String DB_NAME = "db.sender";
    private static final String TABLE_NAME_DIALOG = "dialogs";
    private static final String TABLE_NAME_USER = "users";
    private static final String TABLE_NAME_MSG = "messages";
    private static final String IDX_CHAT_ID = "idx_chat_id";
    private static final String IDX_MSG_CHAT_ID = "idx_msg_chat_id";
    private static final String IDX_SERVER_ID = "idx_msg_server_id";
    private static final String IDX_LOCAL_ID = "idx_local_id";
    private static final String IDX_COMPANY_ID = "idx_company_id";
    private static final String IDX_USER_ID = "idx_user_id";
    private static final String KEY_ID = "_id";
    private static final String KEY_LOCAL_ID = "localId";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_CHAT_ID = "chatId";
    private static final String KEY_ICON_URL = "iconUrl";
    private static final String KEY_LAST_TIME = "lastTime";
    private static final String KEY_LAST_MSG = "lastMsg";
    private static final String KEY_COUNT_UNREAD = "countUnread";
    private static final String KEY_IS_OPER = "isOper";
    private static final String KEY_IS_MUTE = "isMute";
    private static final String KEY_COMPANY_ID = "companyId";
    private static final String KEY_IS_FAVORITE = "isFavorite";
    private static final String KEY_IS_BLOCK = "isBlock";
    private static final String KEY_STATUS = "status";
    private static final String KEY_CLASS = "class";
    private static final String KEY_MODEL = "model";
    private static final String KEY_VIEW = "view";
    private static final String KEY_PACKET_ID = "packetId";
    private static final String KEY_FROM = "fromId";
    private static final String KEY_CREATED = "created";
    private static final String KEY_ENCR_KEY = "encr_key";
    private static final String KEY_MSG_KEY = "msg_key";
    private static final String KEY_BTC_ADDR = "btc_addr";
    private static final String KEY_IS_COMPANY = "isCompany";
    private static final String KEY_PROC_ID = "procId";
    private static final String KEY_TO_OPER = "toOper";
    private static final String KEY_BYTE_ARRAY = "mess_byte_array";
    private static final String KEY_TIME_VERSION = "time_version";
    //    private static final String KEY_IS_OWN = "isOwn";
    private static Storage instance;
    private SharedPreferences pref;
    private Context ctx;

    private Storage(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.ctx = context;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Storage getInstance(Context ctx) {
        if (instance == null) instance = new Storage(ctx);
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME_DIALOG + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_NAME + " TEXT, "
                + KEY_CHAT_ID + " TEXT NOT NULL, "
                + KEY_ICON_URL + " TEXT, "
                + KEY_LAST_TIME + " LONG, "
                + KEY_LAST_MSG + " TEXT, "
                + KEY_ENCR_KEY + " TEXT, "
                + KEY_COMPANY_ID + " TEXT, "
                + KEY_IS_OPER + " INTEGER DEFAULT 0, "
                + KEY_IS_FAVORITE + " INTEGER DEFAULT 0, "
                + KEY_IS_BLOCK + " INTEGER DEFAULT 0, "
                + KEY_IS_MUTE + " INTEGER DEFAULT 0, "
                + KEY_STATUS + " INTEGER DEFAULT 1, "
                + KEY_COUNT_UNREAD + " INTEGER DEFAULT 0"
                + ");");
        db.execSQL("CREATE TABLE " + TABLE_NAME_USER + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_LOCAL_ID + " TEXT, "
                + KEY_NAME + " TEXT, "
                + KEY_PHONE + " TEXT, "
                + KEY_CHAT_ID + " TEXT NOT NULL, "
                + KEY_ICON_URL + " TEXT, "
                + KEY_LAST_TIME + " LONG, "
                + KEY_LAST_MSG + " TEXT, "
                + KEY_MSG_KEY + " TEXT, "
                + KEY_BTC_ADDR + " TEXT, "
                + KEY_IS_FAVORITE + " INTEGER DEFAULT 0, "
                + KEY_IS_COMPANY + " INTEGER DEFAULT 0, "
                + KEY_IS_BLOCK + " INTEGER DEFAULT 0, "
                + KEY_IS_MUTE + " INTEGER DEFAULT 0, "
                + KEY_STATUS + " INTEGER DEFAULT 1, "
                + KEY_COUNT_UNREAD + " INTEGER DEFAULT 0"
//                + KEY_IS_OWN + " INTEGER DEFAULT 1"
                + ");");
        db.execSQL("CREATE TABLE " + TABLE_NAME_MSG + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_CLASS + " TEXT, "
                + KEY_MODEL + " TEXT, "
                + KEY_VIEW + " TEXT, "
                + KEY_PROC_ID + " TEXT, "
                + KEY_CHAT_ID + " TEXT NOT NULL, "
                + KEY_PACKET_ID + " LONG NOT NULL, "
                + KEY_FROM + " TEXT, "
                + KEY_CREATED + " LONG, "
                + KEY_STATUS + " INTEGER DEFAULT 0, "
                + KEY_TO_OPER + " INTEGER DEFAULT 0, "
                + KEY_BYTE_ARRAY + " BLOB, "
                + KEY_TIME_VERSION + " LONG"
                + ");");
        db.execSQL("CREATE UNIQUE INDEX '" + IDX_CHAT_ID + "' ON '" + TABLE_NAME_DIALOG + "' ('" + KEY_CHAT_ID + "' ASC);");
        db.execSQL("CREATE INDEX '" + IDX_COMPANY_ID + "' ON '" + TABLE_NAME_DIALOG + "' ('" + KEY_COMPANY_ID + "' ASC);");
        db.execSQL("CREATE INDEX '" + IDX_LOCAL_ID + "' ON '" + TABLE_NAME_USER + "' ('" + KEY_LOCAL_ID + "' ASC);");
        db.execSQL("CREATE UNIQUE INDEX '" + IDX_USER_ID + "' ON '" + TABLE_NAME_USER + "' ('" + KEY_CHAT_ID + "' ASC);");
        db.execSQL("CREATE INDEX '" + IDX_MSG_CHAT_ID + "' ON '" + TABLE_NAME_MSG + "' ('" + KEY_CHAT_ID + "' ASC);");
        db.execSQL("CREATE UNIQUE INDEX '" + IDX_SERVER_ID + "' ON '" + TABLE_NAME_MSG + "' ('" + KEY_PACKET_ID + "' , '" + KEY_CHAT_ID + "' ASC);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME_MSG + " ADD COLUMN " + KEY_TO_OPER + " INTEGER DEFAULT 0");
        } else if (oldVersion == 2 && newVersion == 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME_MSG + " ADD COLUMN " + KEY_BYTE_ARRAY + " BLOB");
        } else if (oldVersion == 3 && newVersion == 4) {
            db.execSQL("ALTER TABLE " + TABLE_NAME_MSG + " ADD COLUMN " + KEY_TIME_VERSION + " LONG");
        }
    }

    ////////////////////// dialogs //////////////////////

    public void saveDialog(Dialog d) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME, d.getName());
        cv.put(KEY_CHAT_ID, d.getChatId());
        cv.put(KEY_ICON_URL, d.getChatPhoto());
        cv.put(KEY_LAST_TIME, d.getMessageTime());
        cv.put(KEY_LAST_MSG, d.getMessageText());
        cv.put(KEY_ENCR_KEY, d.getEncrKey());
        cv.put(KEY_COMPANY_ID, d.getCompanyId());
        cv.put(KEY_IS_OPER, d.isOperator() ? 1 : 0);
        cv.put(KEY_IS_FAVORITE, d.isFavorite() ? 1 : 0);
        cv.put(KEY_IS_MUTE, d.isMute() ? 1 : 0);
        cv.put(KEY_IS_BLOCK, d.isBlocked() ? 1 : 0);
        cv.put(KEY_COUNT_UNREAD, d.getCountUnread());
        cv.put(KEY_STATUS, d.getStatus());
        try {
            Tool.log("*** save dialog");
            db.insertOrThrow(TABLE_NAME_DIALOG, null, cv);
        } catch (SQLiteConstraintException e) {
            Tool.log("*** update dialog");
            cv.remove(KEY_COMPANY_ID);
            cv.remove(KEY_LAST_TIME);
            cv.remove(KEY_LAST_MSG);
            cv.remove(KEY_ENCR_KEY);
            cv.remove(KEY_COUNT_UNREAD);
            db.update(TABLE_NAME_DIALOG, cv, KEY_CHAT_ID + "=?", new String[]{d.getChatId()});
        }
    }


    public void deleteDialog(String chatId) {
        if (Tool.isP2PChat(chatId)) {
            Tool.log("can't delete p2p chats");
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME_DIALOG, KEY_CHAT_ID + "=?", new String[]{chatId});
        db.delete(TABLE_NAME_MSG, KEY_CHAT_ID + "=?", new String[]{chatId});
        pref.edit().remove(PROP_CHAT_MEMBERS_PREFIX + chatId).apply();
    }

    public int getKeyId(long packetId) {
        int keyId = -1;
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_MSG, new String[]{KEY_ID}, KEY_PACKET_ID + " = " + packetId, null, null, null, KEY_LAST_TIME + " DESC");
        while (cursor.moveToNext()) {
            keyId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
        }
        cursor.close();
        return keyId;
    }

    public void setDialogName(String name, String chatId, boolean forse) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME_DIALOG, new String[]{KEY_NAME}, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null);
        String oldName = "";
        if (cursor.moveToNext()) {
            cursor.moveToFirst();
            oldName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
        }
        cursor.close();
        if (oldName.trim().length() > 0 && !forse) return;
        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME, name);
        db.update(TABLE_NAME_DIALOG, cv, KEY_CHAT_ID + "=?", new String[]{chatId});
    }

    public ArrayList<ChatBased> getDialogs() {
        ArrayList<ChatBased> chats = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_DIALOG, null, KEY_IS_OPER + " = 0", null, null, null, KEY_LAST_TIME + " DESC");
        while (cursor.moveToNext()) {
            chats.add(dialogFromCursor(cursor));
        }
        cursor.close();
        return chats;
    }

    public ArrayList<ChatBased> getGroupChats() {
        ArrayList<ChatBased> chats = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_DIALOG, null, KEY_CHAT_ID + " NOT LIKE '%" + User.P2P_CHAT_PREFIX + "%' AND " + KEY_IS_OPER + " = '0'", null, null, null, KEY_LAST_TIME + " DESC");
        while (cursor.moveToNext()) {
            chats.add(dialogFromCursor(cursor));
        }
        cursor.close();
        return chats;
    }

    public int getGroupChatsSize() {
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_DIALOG, null, KEY_CHAT_ID + " NOT LIKE '%" + User.P2P_CHAT_PREFIX + "%' AND " + KEY_IS_OPER + " = '0'", null, null, null, KEY_LAST_TIME + " DESC");
        int size = 0;
        while (cursor.moveToNext()) {
            size = cursor.getCount();
        }
        cursor.close();
        return size;
    }

    public boolean isOperChat(String chatId) {
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_DIALOG, null, KEY_IS_OPER + " = 1 AND " + KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, KEY_LAST_TIME + " DESC");
        boolean flag = false;
        while (cursor.moveToNext()) {
            flag = true;
        }
        cursor.close();
        return flag;
    }

    public ArrayList<ChatBased> getCompChats(String compId) {
        ArrayList<ChatBased> chats = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_DIALOG, null, KEY_COMPANY_ID + "=?", new String[]{compId}, null, null, KEY_LAST_TIME + " DESC");
        while (cursor.moveToNext()) {
            chats.add(dialogFromCursor(cursor));
        }
        cursor.close();
        return chats;
    }

    public ChatBased getChat(String chatId) {
        ChatBased rez = null;
        Cursor cursor = getReadableDatabase().query(Tool.isP2PChat(chatId) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, null, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null);
        if (cursor.moveToFirst()) {
            rez = Tool.isP2PChat(chatId) ? userFromCursor(cursor) : dialogFromCursor(cursor);
        }
        cursor.close();
        return rez;
    }

    public boolean isChatEmpty(String chatId) {
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_MSG, null, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null);
        boolean rez = cursor.getCount() > 1;
        cursor.close();
        return !rez;
    }

    public boolean isCompany(String chatId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER, null, KEY_CHAT_ID + "=? AND " + KEY_IS_COMPANY + " = 1", new String[]{chatId}, null, null, null);
        int i = cursor.getCount();
        cursor.close();
        return i > 0;
    }

    public void setChatUnreadCount(String chatId, int unread) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_COUNT_UNREAD, unread);
        db.update(Tool.isP2PChat(chatId) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, cv, KEY_CHAT_ID + "=?", new String[]{chatId});
    }

    public int getFavoriteCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_DIALOG, null, KEY_IS_FAVORITE + " = 1", null, null, null, null, null);
        int i = cursor.getCount();
        cursor.close();
        cursor = db.query(TABLE_NAME_USER, null, KEY_IS_FAVORITE + " = 1", null, null, null, null, null);
        i = i + cursor.getCount();
        cursor.close();
        return i;
    }

    public int getOperChatCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_DIALOG, null, KEY_IS_OPER + " = 1", null, null, null, null, null);
        int i = cursor.getCount();
        cursor.close();
        return i;
    }

    public int getUnreadFavoriteCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_DIALOG, null, KEY_COUNT_UNREAD + " > 0 AND " + KEY_IS_FAVORITE + " = 1 AND " + KEY_IS_MUTE + " = 0", null, null, null, null, null);
        int i = cursor.getCount();
        cursor.close();
        cursor = db.query(TABLE_NAME_USER, null, KEY_COUNT_UNREAD + " > 0 AND " + KEY_IS_FAVORITE + " = 1 AND " + KEY_IS_MUTE + " = 0", null, null, null, null, null);
        i = i + cursor.getCount();
        cursor.close();
        return i;
    }

    public int getUnreadP2PCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER, null, KEY_COUNT_UNREAD + " > 0 AND " + KEY_IS_COMPANY + " = 0 AND " + KEY_IS_MUTE + " = 0 AND " + KEY_IS_FAVORITE + " = 0", null, null, null, null, null);
        int i = cursor.getCount();
        cursor.close();
        return i;
    }

    public int getUnreadCompCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER, null, KEY_COUNT_UNREAD + " > 0 AND " + KEY_IS_COMPANY + " = 1 AND " + KEY_IS_MUTE + " = 0 AND " + KEY_IS_FAVORITE + " = 0", null, null, null, null, null);
        int i = cursor.getCount();
        cursor.close();
        return i;
    }

    public int getUnreadGroupCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_DIALOG, null, KEY_COUNT_UNREAD + " > 0  AND " + KEY_IS_MUTE + " = 0" + " AND " + KEY_IS_OPER + " != 1 AND " + KEY_IS_FAVORITE + " = 0", null, null, null, null, null);
        int i = cursor.getCount();
        cursor.close();
        return i;
    }

    public int getUnreadOperCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_DIALOG, null, KEY_COUNT_UNREAD + " > 0 AND " + KEY_IS_OPER + " = 1 AND " + KEY_IS_MUTE + " = 0 AND " + KEY_IS_FAVORITE + " = 0", null, null, null, null, null);
        int i = cursor.getCount();
        cursor.close();
        return i;
    }

    public void setChatOptions(boolean block, boolean favorite, boolean mute, String id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_IS_BLOCK, block);
        cv.put(KEY_IS_FAVORITE, favorite);
        cv.put(KEY_IS_MUTE, mute);
        db.update(Tool.isP2PChat(id) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, cv, KEY_CHAT_ID + "=?", new String[]{id});
    }

    public void updateLastMessage(String chatId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME_MSG, new String[]{KEY_MODEL, KEY_CLASS, KEY_CREATED, KEY_VIEW}, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, KEY_CREATED + " DESC", "1");
        if (cursor.moveToFirst()) {
            String className = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS));
            String model = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MODEL));
            long time = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CREATED));
            String view = cursor.getString(cursor.getColumnIndexOrThrow(KEY_VIEW));
            String text = MsgBased.getTextFromModel(ctx, className, model, view != null && view.length() > 0);
            ContentValues cv = new ContentValues();
            cv.put(KEY_LAST_MSG, text);
            cv.put(KEY_LAST_TIME, time);
            db.update(Tool.isP2PChat(chatId) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, cv, KEY_CHAT_ID + "=?", new String[]{chatId});
        }
        cursor.close();
    }

    public String getChatName(String chatId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Tool.isP2PChat(chatId) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, new String[]{KEY_NAME}, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null, null);
        String name = "Anonymous";
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
        }
        cursor.close();
        return name;
    }

    ////////////////////// users //////////////////////

    public void saveUser(User u) {
        Tool.log("KKK try to save user: " + u.getName() + " with key " + u.getMsgKey());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_LOCAL_ID, u.getLocalId());
        cv.put(KEY_NAME, u.getName());
        cv.put(KEY_CHAT_ID, u.getChatId());
        cv.put(KEY_ICON_URL, u.getChatPhoto());
        cv.put(KEY_LAST_TIME, u.getMessageTime());
        cv.put(KEY_LAST_MSG, u.getMessageText());
        cv.put(KEY_BTC_ADDR, u.getBtcAddr());
        cv.put(KEY_MSG_KEY, u.getMsgKey());
        cv.put(KEY_PHONE, u.getPhone());
        cv.put(KEY_IS_FAVORITE, u.isFavorite() ? 1 : 0);
        cv.put(KEY_IS_MUTE, u.isMute() ? 1 : 0);
        cv.put(KEY_IS_BLOCK, u.isBlocked() ? 1 : 0);
        cv.put(KEY_IS_COMPANY, u.isCompany() ? 1 : 0);
        cv.put(KEY_COUNT_UNREAD, u.getCountUnread());
//        cv.put(KEY_IS_OWN, u.isOwn() ? 1 : 0);
        try {
            db.insertOrThrow(TABLE_NAME_USER, null, cv);
        } catch (SQLiteConstraintException e) {
            cv.remove(KEY_LAST_TIME);
            cv.remove(KEY_LAST_MSG);
            db.update(TABLE_NAME_USER, cv, KEY_CHAT_ID + "=?", new String[]{u.getChatId()});
        }
    }


    public void setChatStatus(String chatId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_STATUS, ChatBased.parseStatus(status));
        db.update(Tool.isP2PChat(chatId) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, cv, KEY_CHAT_ID + "=?", new String[]{chatId});

        if (status.equals(ChatBased.STATUS_STR_DELIV) || status.equals(ChatBased.STATUS_STR_READ)) {
            setVersionMess(chatId);
        }
    }

    //set version message, when chat status is changed
    public void setVersionMess(String chatId) {
        long packetId = getLastPacketId(chatId);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        db.update(TABLE_NAME_MSG, cv, KEY_PACKET_ID + "=?", new String[]{String.valueOf(packetId)});
    }

    public int getChatStatus(String chatId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Tool.isP2PChat(chatId) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, new String[]{KEY_STATUS}, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null, null);
        int name = -2;
        if (cursor.moveToFirst()) {
            name = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATUS));
        }
        cursor.close();
        return name;
    }

    public void setUserPhone(String chatId, String phone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_PHONE, phone);
        db.update(TABLE_NAME_USER, cv, KEY_CHAT_ID + "=?", new String[]{chatId});
    }

    public void setChatPhoto(String chatId, String photo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_ICON_URL, photo);
        db.update(Tool.isP2PChat(chatId) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, cv, KEY_CHAT_ID + "=?", new String[]{chatId});
    }

    public void setUserFavorite(String userId, boolean favorite) {
        String chatId = userId.startsWith(User.P2P_CHAT_PREFIX) ? userId : User.P2P_CHAT_PREFIX + userId;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_IS_FAVORITE, favorite ? 1 : 0);
        db.update(TABLE_NAME_USER, cv, KEY_CHAT_ID + "=?", new String[]{chatId});
    }

    public void addDialogKey(String chatId, String key) {
        if (key != null) {
            if (Tool.isP2PChat(chatId)) {
                Tool.log("not allowed for p2p");
                return;
            }
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.query(TABLE_NAME_DIALOG, new String[]{KEY_ENCR_KEY}, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null);
            String keys = "[]";
            if (cursor.moveToFirst()) {
                String keys2 = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ENCR_KEY));
                if (keys2 != null) keys = keys2;
            }
            cursor.close();
            if (!keys.contains(key)) {
                JSONArray keysArr = new JSONArray();
                try {
                    keysArr = new JSONArray(keys);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                keysArr.put(key);
                ContentValues cv = new ContentValues();
                cv.put(KEY_ENCR_KEY, keysArr.toString());
                db.update(TABLE_NAME_DIALOG, cv, KEY_CHAT_ID + "=?", new String[]{chatId});
            }
        }
    }

    public JSONArray getDialogKeys(String chatId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_DIALOG, new String[]{KEY_ENCR_KEY}, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null, null);
        String key = "[]";
        if (cursor.moveToFirst()) {
            String s = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ENCR_KEY));
            if (s != null) key = s;
        }
        cursor.close();
        JSONArray arr = new JSONArray();
        if (!key.startsWith("[")) arr.put(key);
        else {
            try {
                arr = new JSONArray(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return arr;
    }

    public String getUserName(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER, new String[]{KEY_NAME}, KEY_CHAT_ID + "=?", new String[]{User.P2P_CHAT_PREFIX + userId}, null, null, null, null);
        String name = "Anonymous";
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
        }
        cursor.close();
        return name;
    }

    public boolean isUserExists(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER, null, KEY_CHAT_ID + "=?", new String[]{User.P2P_CHAT_PREFIX + userId}, null, null, null, null);
        boolean rez = cursor.getCount() != 0;
        cursor.close();
        return rez;
    }

    public User getUser(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER, null, KEY_CHAT_ID + "=?", new String[]{User.P2P_CHAT_PREFIX + userId}, null, null, null, null);
        User u = null;
        while (cursor.moveToNext()) {
            u = userFromCursor(cursor);
        }
        cursor.close();
        return u;
    }

    public boolean isUserInDialog(String chatId, String userId) {
        List<User> listUsers = getChatMembers(chatId);
        for (int i = 0; i < listUsers.size(); i++) {
            if (listUsers.get(i).getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDialogExists(String chatId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_DIALOG, null, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null, null);
        boolean rez = cursor.getCount() != 0;
        cursor.close();
        return rez;
    }

    public boolean isFavorite(String chatId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Tool.isP2PChat(chatId) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, null, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null, null);
        boolean IsFavorite = false;
        if (cursor.moveToFirst()) {
            IsFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_FAVORITE)) != 0;
        }
        cursor.close();
        return IsFavorite;
    }

    public boolean isBlock(String chatId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Tool.isP2PChat(chatId) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, null, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null, null);
        boolean IsFavorite = false;
        if (cursor.moveToFirst()) {
            IsFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_BLOCK)) != 0;
        }
        cursor.close();
        return IsFavorite;
    }

    public boolean isMute(String chatId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(Tool.isP2PChat(chatId) ? TABLE_NAME_USER : TABLE_NAME_DIALOG, null, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null, null);
        boolean IsFavorite = false;
        if (cursor.moveToFirst()) {
            IsFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_MUTE)) != 0;
        }
        cursor.close();
        return IsFavorite;
    }

    public String getUserIcon(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER, new String[]{KEY_ICON_URL}, KEY_CHAT_ID + "=?", new String[]{User.P2P_CHAT_PREFIX + userId}, null, null, null, null);
        String icon = null;
        if (cursor.moveToFirst()) {
            icon = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ICON_URL));
        }
        cursor.close();
        return icon;
    }

    public String getUserMsgKey(String chatId) {
        if (!chatId.startsWith(User.P2P_CHAT_PREFIX)) chatId = User.P2P_CHAT_PREFIX + chatId;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER, new String[]{KEY_MSG_KEY}, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, null, null);
        String key = null;
        if (cursor.moveToFirst()) {
            key = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MSG_KEY));
        }
        cursor.close();
        return key;
    }

    public int p24getCountUnreadMessages() {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER, new String[]{KEY_COUNT_UNREAD}, KEY_COUNT_UNREAD +
                "!=? AND " + KEY_IS_MUTE + " = 0", new String[]{"0"}, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                count += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COUNT_UNREAD));
                cursor.moveToNext();
            }
        }

        cursor = db.query(TABLE_NAME_DIALOG, new String[]{KEY_COUNT_UNREAD}, KEY_COUNT_UNREAD +
                "!=? AND " + KEY_IS_MUTE + " = 0", new String[]{"0"}, null, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                count += cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COUNT_UNREAD));
                cursor.moveToNext();
            }
        }

        cursor.close();

        return count;
    }

    //TODO: unread != 0 временно, пока сервер не запилит 3 состояния для isOwn
    public List<ChatBased> getUsers(boolean withCompanies) {
        ArrayList<ChatBased> chats = new ArrayList<>();
//        Cursor cursor = getReadableDatabase().query(TABLE_NAME_USER, null, withCompanies ? null : "("+KEY_IS_COMPANY + "=0 AND "+ KEY_IS_OWN +"=1)"+"OR "+KEY_COUNT_UNREAD+ " !=0", null, null, null, KEY_LAST_TIME + " DESC");
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_USER, null, withCompanies ? null : KEY_IS_COMPANY + "=0", null, null, null, KEY_LAST_TIME + " DESC");
        while (cursor.moveToNext()) {
            chats.add(userFromCursor(cursor));
        }
        cursor.close();
        return chats;
    }

    public int getUsersCount(boolean withCompanies) {
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_USER, null, withCompanies ? null : KEY_IS_COMPANY + "=0", null, null, null, KEY_LAST_TIME + " DESC");
        int size = cursor.getCount();
        cursor.close();
        return size;
    }

    public ArrayList<ChatBased> getComps() {
        ArrayList<ChatBased> chats = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_USER, null, KEY_IS_COMPANY + "=1", null, null, null, KEY_LAST_TIME + " DESC");
        while (cursor.moveToNext()) {
            chats.add(userFromCursor(cursor));
        }
        cursor.close();
        return chats;
    }

    public int getCompsSize() {
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_USER, null, KEY_IS_COMPANY + "=1", null, null, null, KEY_LAST_TIME + " DESC");
        int size = cursor.getCount();
        cursor.close();
        return size;
    }

    public List<ChatBased> getFavoriteChats() {
        ArrayList<ChatBased> rez = new ArrayList<>();
        for (ChatBased c : getDialogs()) {
            if (!c.isFavorite()) continue;
            rez.add(c);
        }
        for (ChatBased c : getUsers(true)) {
            if (!c.isFavorite()) continue;
            rez.add(c);
        }
        ChatBased.sort(rez);
        return rez;
    }
    ////////////////////// messages //////////////////////

    public void clearHistory() {
        Tool.log("*** clearHIs");
        new Thread(new Runnable() {
            @Override
            public void run() {
                getWritableDatabase().delete(TABLE_NAME_MSG, null, null);
                getWritableDatabase().delete(TABLE_NAME_DIALOG, null, null);
                getWritableDatabase().delete(TABLE_NAME_USER, null, null);
                Map<String, ?> keys = pref.getAll();
                for (Map.Entry<String, ?> entry : keys.entrySet()) {
                    if (entry.getKey().startsWith(PROP_CHAT_HOLE_PREFIX)) {
                        pref.edit().remove(entry.getKey()).apply();
                    }
                }
                StateHolder.getInstance(ctx).setDialogUnSynced();
            }
        }).start();
    }

    public void saveServerMessage(String chatId, JSONObject jo) {
        Tool.log("*** save mess storage, isExist = "+isUserExists(jo.optString("from"))+", from = "+jo.optString("from"));
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_CLASS, jo.optString("class"));
        cv.put(KEY_MODEL, jo.optString("model"));
        cv.put(KEY_CHAT_ID, chatId);
        cv.put(KEY_PACKET_ID, jo.optLong("packetId"));
        cv.put(KEY_PROC_ID, jo.optString("procId"));
        cv.put(KEY_FROM, jo.optString("from"));
        cv.put(KEY_VIEW, jo.optString("view"));
        cv.put(KEY_CREATED, jo.optLong("created"));
        cv.put(KEY_STATUS, MsgBased.STATUS_SENT);
        cv.put(KEY_TO_OPER, jo.optInt("toOper"));
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        cv.put(KEY_BYTE_ARRAY, getByteArr(jo));
        try {
            db.insertOrThrow(TABLE_NAME_MSG, null, cv);
        } catch (SQLiteConstraintException e) {
            Tool.log("msg id " + jo.optLong("packetId") + " in chat " + chatId + " already saved");
        }
    }

    private byte[] getByteArr(JSONObject jo) {
        if ("text".equals(jo.optString("class"))) {
            try {
                JSONObject model = new JSONObject(jo.optString("model"));
                if (model.optBoolean("encrypted") || 1 == model.optInt("encrypted")) {
                    return LWallet.decodeM(model.optString("text"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public void updateServerMessage(String chatId, JSONObject jo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_MODEL, jo.optString("model"));
        cv.put(KEY_FROM, jo.optString("from"));
        cv.put(KEY_VIEW, jo.optString("view"));
        cv.put(KEY_CLASS, jo.optString("class"));
        cv.put(KEY_STATUS, MsgBased.STATUS_SENT);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        cv.put(KEY_BYTE_ARRAY, getByteArr(jo));
        try {
            db.update(TABLE_NAME_MSG, cv, KEY_PACKET_ID + "=? AND " + KEY_CHAT_ID + "=?", new String[]{jo.optString("linkId"), chatId});
        } catch (SQLiteConstraintException e) {
            Tool.log("msg id " + jo.optLong("packetId") + " in chat " + chatId + " already saved");
        }
    }

    public TextMessage editTextMessage(String text, long packetId, String chatId) {
        TextMessage tm = new TextMessage(text, chatId, packetId);
        if (isChatEncrypted(chatId)) {
            tm = new MsgCryptFacade(ctx).tryEncrypt(tm);
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_CLASS, ChatFacade.CLASS_TEXT_ROUTE);
        cv.put(KEY_MODEL, tm.getModel());
        cv.put(KEY_STATUS, MsgBased.STATUS_NEW);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        if (tm.isEncrypted()) {
            cv.put(KEY_BYTE_ARRAY, LWallet.decodeM(tm.getText()));
        }
        try {
            db.update(TABLE_NAME_MSG, cv, KEY_PACKET_ID + "=?", new String[]{String.valueOf(packetId)});
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            Tool.log("msg id " + packetId + " in chat " + chatId + " already saved");
        }
        updateLastMessage(chatId);
        return tm;
    }

    public TextMessage saveNewTextMessage(String text, String chatId, boolean toOper) {
        TextMessage tm = new TextMessage(text, chatId);
        if (isChatEncrypted(chatId) && !isCompany(chatId)) { //  TODO: check this String "!isCompany(chatId)"
            tm = new MsgCryptFacade(ctx).tryEncrypt(tm);
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        long time = System.currentTimeMillis();
        cv.put(KEY_CLASS, ChatFacade.CLASS_TEXT_ROUTE);
        cv.put(KEY_MODEL, tm.getModel());
        cv.put(KEY_CHAT_ID, chatId);
        cv.put(KEY_PACKET_ID, tm.getPacketId());
        cv.put(KEY_FROM, getMyUserId());
        cv.put(KEY_CREATED, tm.getCreated());
        cv.put(KEY_STATUS, MsgBased.STATUS_NEW);
        cv.put(KEY_TO_OPER, toOper);
        cv.put(KEY_TIME_VERSION, time);
        if (tm.isEncrypted()) {
            cv.put(KEY_BYTE_ARRAY, LWallet.decodeM(tm.getText()));
        }
        long id = db.insert(TABLE_NAME_MSG, null, cv);
        tm.setPacketId(id);
        tm.setFrom(getMyUserId());
        tm.setTimeVersion(time);

        updateLastMessage(chatId);
        setChatStatus(chatId, ChatBased.STATUS_STR_NONE);

//        TextMessage tm2 = (TextMessage) getMessage(chatId, tm.getPacketId() + "");
//        tm = new MsgCryptFacade(ctx).tryEncrypt(tm2);

        return tm;
    }

    public boolean isFormExist(String linkId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_MSG, null, KEY_PACKET_ID + "=?", new String[]{linkId}, null, null, null, null);
        boolean rez = cursor.getCount() != 0;
        cursor.close();
        return rez;
    }

    public long saveNewStickerMessage(String id, String chatId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_CLASS, ChatFacade.CLASS_STICKER);
        try {
            cv.put(KEY_MODEL, new JSONObject().put("id", id).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cv.put(KEY_CHAT_ID, chatId);
        cv.put(KEY_PACKET_ID, System.currentTimeMillis());
        cv.put(KEY_FROM, getMyUserId());
        cv.put(KEY_CREATED, System.currentTimeMillis());
        cv.put(KEY_STATUS, MsgBased.STATUS_SENT);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        long i = db.insert(TABLE_NAME_MSG, null, cv);
        updateLastMessage(chatId);
        return i;
    }

    public long saveNewLocationMessage(String desc, String chatId, String lat, String lon) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_CLASS, ChatFacade.CLASS_SHARE_LOCATION);
        try {
            cv.put(KEY_MODEL, new JSONObject().put("textMsg", desc).put("lat", lat).put("lon", lon).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cv.put(KEY_CHAT_ID, chatId);
        cv.put(KEY_PACKET_ID, System.currentTimeMillis());
        cv.put(KEY_FROM, getMyUserId());
        cv.put(KEY_CREATED, System.currentTimeMillis());
        cv.put(KEY_STATUS, MsgBased.STATUS_SENT);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        long i = db.insert(TABLE_NAME_MSG, null, cv);
        updateLastMessage(chatId);
        return i;
    }

    public long saveNewFileMessage(String url, String name, String size, String type, String chatId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_CLASS, ChatFacade.CLASS_FILE_ROUTE);
        try {
            cv.put(KEY_MODEL, new JSONObject().put("url", url).put("name", name).put("size", size).put("type", type).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cv.put(KEY_CHAT_ID, chatId);
        cv.put(KEY_PACKET_ID, System.currentTimeMillis());
        cv.put(KEY_FROM, getMyUserId());
        cv.put(KEY_CREATED, System.currentTimeMillis());
        cv.put(KEY_STATUS, MsgBased.STATUS_NEW);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        long id = db.insert(TABLE_NAME_MSG, null, cv);
        updateLastMessage(chatId);
        return id;
    }

    public long saveNewMp3Message(String url, String name, String size, String type, String chatId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_CLASS, ChatFacade.CLASS_AUDIO_ROUTE);
        try {
            cv.put(KEY_MODEL, new JSONObject().put("url", url).put("name", name).put("length", size).put("type", type).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cv.put(KEY_CHAT_ID, chatId);
        cv.put(KEY_PACKET_ID, System.currentTimeMillis());
        cv.put(KEY_FROM, getMyUserId());
        cv.put(KEY_CREATED, System.currentTimeMillis());
        cv.put(KEY_STATUS, MsgBased.STATUS_NEW);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        long id = db.insert(TABLE_NAME_MSG, null, cv);
        updateLastMessage(chatId);
        return id;
    }

    public void setFileMessageSent(long cid, String url, String name, String size, String type, String serverId, long time) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        try {
            cv.put(KEY_MODEL, new JSONObject().put("url", url).put("name", name).put("size", size).put("type", type).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cv.put(KEY_PACKET_ID, Long.parseLong(serverId));
        cv.put(KEY_CREATED, time);
        cv.put(KEY_STATUS, MsgBased.STATUS_SENT);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        db.update(TABLE_NAME_MSG, cv, KEY_ID + "=?", new String[]{String.valueOf(cid)});
    }

    public long saveNewImgMessage(String url, byte[] preview, String chatId) {
        String previewFile = Tool.saveBytesAsTempFile(ctx, preview, MediaUtils.getTypeFile(url)/*"png"*/);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_CLASS, ChatFacade.CLASS_IMAGE_ROUTE);
        try {
            cv.put(KEY_MODEL, new JSONObject().put("url", url).put("preview", previewFile).put("type", MediaUtils.getTypeFile(url)).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cv.put(KEY_CHAT_ID, chatId);
        cv.put(KEY_PACKET_ID, System.currentTimeMillis());
        cv.put(KEY_FROM, getMyUserId());
        cv.put(KEY_CREATED, System.currentTimeMillis());
        cv.put(KEY_STATUS, MsgBased.STATUS_NEW);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        Tool.log("*** save new img = "+System.currentTimeMillis());
        long id = db.insert(TABLE_NAME_MSG, null, cv);
        updateLastMessage(chatId);
        return id;
    }

    public void setImgMessageSent(long cid, String url, byte[] preview, String serverId, long time, String type) {
        if (cid <= 0) {
            Tool.log("No previously saved image");
            return;
        }
        String previewFile = Tool.saveBytesAsTempFile(ctx, preview, "png");
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        try {
            cv.put(KEY_MODEL, new JSONObject().put("url", url).put("preview", previewFile).put("type", type).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cv.put(KEY_PACKET_ID, Long.parseLong(serverId));
        cv.put(KEY_CREATED, time);
        cv.put(KEY_STATUS, MsgBased.STATUS_SENT);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        db.update(TABLE_NAME_MSG, cv, KEY_ID + "=?", new String[]{String.valueOf(cid)});
    }

    public void setAudioMessageSent(long cid, String url, String name, String size, String type, String serverId, String created) {
//        Tool.log("**setAudioMessageSent cid = "+cid+", url = "+url+", name = "+name+", size = "+size+", type = "+type+", serverId = "+serverId+", created = "+created);
        if (cid <= 0) {
            Tool.log("No previously saved image");
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        try {
            cv.put(KEY_MODEL, new JSONObject().put("url", url).put("name", name).put("length", size).put("type", type).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cv.put(KEY_PACKET_ID, Long.parseLong(serverId));
        cv.put(KEY_CREATED, created);
        cv.put(KEY_STATUS, MsgBased.STATUS_SENT);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        db.update(TABLE_NAME_MSG, cv, KEY_ID + "=?", new String[]{String.valueOf(cid)});
    }


    public void setTextMessageSent(long cid, String serverId, long time, String chatId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_PACKET_ID, Long.parseLong(serverId));
        cv.put(KEY_CREATED, time);
        cv.put(KEY_STATUS, MsgBased.STATUS_SENT);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        db.update(TABLE_NAME_MSG, cv, KEY_ID + "=? AND " + KEY_CHAT_ID + "=?", new String[]{String.valueOf(cid), chatId});
    }

    public void setTextMessageSent(long packetId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_STATUS, MsgBased.STATUS_SENT);
        cv.put(KEY_TIME_VERSION, System.currentTimeMillis());
        db.update(TABLE_NAME_MSG, cv, KEY_PACKET_ID + "=?", new String[]{String.valueOf(packetId)});
    }

    public ArrayList<MsgBased> getMessages(String chatId) {
        ArrayList<MsgBased> msgs = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_MSG, null, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, KEY_CREATED + " DESC");
        while (cursor.moveToNext()) {
            MsgBased m = createMessPacket(cursor);
            if (m != null) msgs.add(m);
        }
        cursor.close();
        return msgs;
    }

    public ArrayList<MsgBased> getUpdatedMessages(String chatId, long time) {
        ArrayList<MsgBased> msgs = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_MSG, null, KEY_CHAT_ID + "=? AND " + KEY_TIME_VERSION + ">" + time, new String[]{chatId}, null, null, KEY_CREATED + " DESC"/*, "10"*/);
        while (cursor.moveToNext()) {
            MsgBased m = createMessPacket(cursor);
            if (m != null) {
                msgs.add(m);
            }
        }
        cursor.close();

        return msgs;
    }

    public MsgBased getMessage(String chatId, String packetId) {
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_MSG, null, KEY_CHAT_ID + "=? AND " + KEY_PACKET_ID + "=?", new String[]{chatId, packetId}, null, null, KEY_CREATED + " DESC");
        cursor.moveToFirst();
        MsgBased m = createMessPacket(cursor);
        cursor.close();
        return m;
    }

    private MsgBased createMessPacket(Cursor cursor) {
        return MsgBased.create(
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHAT_ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_PACKET_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_VIEW)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROC_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_MODEL)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_FROM)),
                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CREATED)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TO_OPER)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TIME_VERSION)),
                cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_BYTE_ARRAY))
        );
    }

    private MsgBased createMessId(Cursor cursor) {
        return MsgBased.create(
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHAT_ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_CLASS)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_VIEW)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROC_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_MODEL)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_FROM)),
                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_CREATED)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_TO_OPER)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TIME_VERSION)),
                cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_BYTE_ARRAY))
        );
    }

    public ArrayList<MsgBased> getUnsentTextMessages() {
        ArrayList<MsgBased> msgs = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_MSG, null, KEY_CLASS + "=? AND " + KEY_STATUS + "=?", new String[]{ChatFacade.CLASS_TEXT_ROUTE, String.valueOf(MsgBased.STATUS_NEW)}, null, null, KEY_CREATED + " ASC");
        while (cursor.moveToNext()) {
            MsgBased m = createMessId(cursor);
            if (m != null) msgs.add(m);
        }
        cursor.close();
        return msgs;
    }

    public ArrayList<MsgBased> getUnsentMediaMessages() {
        ArrayList<MsgBased> msgs = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_MSG, null, KEY_CLASS + "=? AND " + KEY_STATUS + "=?", new String[]{ChatFacade.CLASS_IMAGE_ROUTE, String.valueOf(MsgBased.STATUS_NEW)}, null, null, KEY_CREATED + " ASC");
        while (cursor.moveToNext()) {
            MsgBased m = createMessId(cursor);
            if (m != null) msgs.add(m);
        }
        cursor.close();
        return msgs;
    }

    public boolean isLastMsgFromMe(String chatId) {
        boolean fromMe = false;
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_MSG, null, KEY_CHAT_ID + "=?", new String[]{chatId}, null, null, KEY_CREATED + " DESC");
        if (cursor.moveToFirst()) {
            fromMe = getMyUserId().equals(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FROM)));
        }
        cursor.close();
        return fromMe;
    }

    public long getLastPacketId(String chatId) {
        // TODO: исключить локальные сообщения у которых packetId = timestamp
        Cursor cursor = getReadableDatabase().rawQuery("SELECT MAX(" + KEY_PACKET_ID + ") AS 'M' FROM " + TABLE_NAME_MSG + " WHERE " + KEY_CHAT_ID + "=?", new String[]{chatId});
        long rez = -1;
        if (cursor.moveToFirst()) {
            rez = cursor.getLong(cursor.getColumnIndexOrThrow("M"));
            if (String.valueOf(rez).length() > System.currentTimeMillis()) {
                Tool.log("---!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
        cursor.close();
        return rez;
    }

    public boolean isFormMessage(String chatId, String packetId) {
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_MSG, new String[]{KEY_VIEW}, KEY_CHAT_ID + "=? AND " + KEY_PACKET_ID + "=?", new String[]{chatId, packetId}, null, null, null);
        cursor.moveToFirst();
        String str = cursor.getString(cursor.getColumnIndexOrThrow(KEY_VIEW));
        cursor.close();
        return str != null && str.length() > 0;
    }


    public long getFirstPacketId(String chatId) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT MIN(" + KEY_PACKET_ID + ") AS 'M' FROM " + TABLE_NAME_MSG + " WHERE " + KEY_CHAT_ID + "=?", new String[]{chatId});
        long rez = -1;
        if (cursor.moveToFirst()) {
            rez = cursor.getLong(cursor.getColumnIndexOrThrow("M"));
        }
        cursor.close();
        return rez;
    }

    public List<User> getAddCandidatesP2P(String chatId) {
        ArrayList<User> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_USER, null, null, null, null, null, KEY_LAST_TIME + " DESC");
        while (cursor.moveToNext()) {
            User user = userFromCursor(cursor);
            if (chatId.contains(user.getUserId())) continue;
            list.add(user);
        }
        cursor.close();
        return list;
    }

    public List<User> getAddCandidates(String chatId) {
        ArrayList<User> list = new ArrayList<>();
        String strJson = pref.getString(PROP_CHAT_MEMBERS_PREFIX + chatId, "no");
        if (!"no".equals(strJson)) {
            try {
                JSONArray ja = new JSONArray(strJson);
                List<String> userIdList = new ArrayList<>();
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    userIdList.add(jo.getString("userId"));
                }

                Cursor cursor = getReadableDatabase().query(TABLE_NAME_USER, null, null, null, null, null, KEY_LAST_TIME + " DESC");
                while (cursor.moveToNext()) {
                    User user = userFromCursor(cursor);
                    if (Tool.isP2PChat(chatId)) {
                        if (chatId.contains(user.getUserId())) continue;
                    } else {
                        if (userIdList.contains(user.getUserId())) continue;
                    }
                    list.add(user);
                }
                cursor.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public ArrayList<ChatBased> searchUsers(String s) {
        ArrayList<ChatBased> chats = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME_USER, null, KEY_NAME + " LIKE '%" + s + "%'", null, null, null, KEY_LAST_TIME + " DESC");
        while (cursor.moveToNext()) {
            chats.add(userFromCursor(cursor));
        }
        cursor.close();
        cursor = getReadableDatabase().query(TABLE_NAME_DIALOG, null, KEY_NAME + " LIKE '%" + s + "%'", null, null, null, KEY_LAST_TIME + " DESC");
        while (cursor.moveToNext()) {
            chats.add(dialogFromCursor(cursor));
        }
        cursor.close();
        return chats;
    }

    public boolean isLocalIdExist(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME_USER, new String[]{KEY_LOCAL_ID}, KEY_CHAT_ID + "=?", new String[]{User.P2P_CHAT_PREFIX + userId}, null, null, null);
        String localId = "";
        while (cursor.moveToNext()) {
            localId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCAL_ID));
        }
        cursor.close();
        return !"".equals(localId);
    }


    ////////////////////// prefs //////////////////////

    public boolean getOperOnlineStatus() {
        return pref.getBoolean(PROP_ONLINE, false);
    }

    public void setOperOnlineStatus(boolean online) {
        pref.edit().putBoolean(PROP_ONLINE, online).apply();
    }

    public boolean isFullVer() {
        return pref.getBoolean(PROP_FULL_VER, false);
    }

    public void setFullVer(boolean fullVer) {
        pref.edit().putBoolean(PROP_FULL_VER, fullVer).apply();
    }

    public void setChatEncrypted(String chatId, boolean encrypted) {
        Set<String> set = pref.getStringSet(PROP_ENCR_CHATS, new HashSet<String>());
        if (encrypted) {
            set.add(chatId);
        } else {
            set.remove(chatId);
        }
        pref.edit().putStringSet(PROP_ENCR_CHATS, set).apply();
    }

    public boolean isChatEncrypted(String chatId) {
        Set<String> set = pref.getStringSet(PROP_ENCR_CHATS, new HashSet<String>());
        return set.contains(chatId);
    }

    public void saveMyName(String name) {
        pref.edit().putString(PROP_MY_NAME, name).apply();
    }

    public void saveMyPhone(String phone) {
        pref.edit().putString(PROP_MY_PHONE, phone).apply();
    }

    public void saveMyPhoto(String photo) {
        pref.edit().putString(PROP_MY_PHOTO, photo).apply();
    }

    public void saveMyDesc(String desc) {
        pref.edit().putString(PROP_MY_DESC, desc).apply();
    }

    public String getMyName() {
        return pref.getString(PROP_MY_NAME, "Anonymous");
    }

    public String getMyDesc() {
        return pref.getString(PROP_MY_DESC, "I like Sender ;)");
    }

    public String getMyPhoto() {
        return pref.getString(PROP_MY_PHOTO, "");
    }

    public String getMyPhone() {
        return pref.getString(PROP_MY_PHONE, "");
    }

    public void saveUserId(String userId) {
        pref.edit().putString(PROP_USERID, userId).apply();
    }

    public String getMyUserId() {
        return pref.getString(PROP_USERID, "");
    }

    public void saveCompaniesId(Set<String> setId) {
        pref.edit().putStringSet(PROP_COMPANIES_ID, setId).apply();
    }

    public Set<String> getCompaniesId() {
        return pref.getStringSet(PROP_COMPANIES_ID, new HashSet<String>());
    }

    public void saveTabPosition(int position) {
        pref.edit().putInt(PROP_TAB_POSITION, position).apply();
    }

    public int getTabPosition() {
        return pref.getInt(PROP_TAB_POSITION, 0);
    }

    public void saveKeyboardHeight(int value) {
        pref.edit().putInt(PROP_KEYBOARD_HEIGHT, value).apply();
    }

    public int getKeyboardHeight() {
        return pref.getInt(PROP_KEYBOARD_HEIGHT, 0);
    }

    public void saveSendStatusRead(boolean value) {
        pref.edit().putBoolean(PROP_SEND_STATUS_READ, value).apply();
    }

    public boolean getSendStatusRead() {
        return pref.getBoolean(PROP_SEND_STATUS_READ, true);
    }

    public void saveOperUsers(String chatId, JSONArray ja) {
        pref.edit().putString(PROP_OPER_USERS + chatId, ja.toString()).apply();
    }

    public OperatorModel getOperUsers(String chatId, String userId) {
        String strJson = pref.getString(PROP_OPER_USERS + chatId, "no");
        if (!"no".equals(strJson)) {
            try {
                JSONArray ja = new JSONArray(strJson);
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    if (userId.equals(jo.optString("from"))) {
                        return new OperatorModel(jo.optString("fromName"), jo.optString("fromPhoto"), jo.optString("from"));
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setDialogsThatIleft(String chatId, boolean action) {
        Set<String> set = pref.getStringSet(PROP_DIALOGS_THAT_I_LEFT, new HashSet<String>());
        if (action) {
            set.add(chatId);
        } else {
            set.remove(chatId);
        }
        pref.edit().putStringSet(PROP_DIALOGS_THAT_I_LEFT, set).apply();
    }

    public boolean isImInDialog(String chatId) {
        Set<String> set = pref.getStringSet(PROP_DIALOGS_THAT_I_LEFT, new HashSet<String>());
        return !set.contains(chatId);
    }

    public void clearChatsThatILeft() {
        pref.edit().remove(PROP_DIALOGS_THAT_I_LEFT).apply();
    }


    public void savePlaySoundNotifications(boolean value) {
        pref.edit().putBoolean(PROP_NOTIFICATIONS_SOUND, value).apply();
    }

    public boolean isPlaySoundNotifications() {
        return pref.getBoolean(PROP_NOTIFICATIONS_SOUND, true);
    }

    public void saveShowNotifications(boolean value) {
        pref.edit().putBoolean(PROP_NOTIFICATIONS, value).apply();
    }

    public boolean isShowNotifications() {
        return pref.getBoolean(PROP_NOTIFICATIONS, true);
    }

    public void saveGpsEnable(boolean value) {
        pref.edit().putBoolean(PROP_GPS, value).apply();
    }

    public boolean isGpsEnable() {
        return pref.getBoolean(PROP_GPS, true);
    }

    public void saveLocale(String value) {
        if (Locale.getDefault().getLanguage().equalsIgnoreCase(value)) value = "";
        if (value.isEmpty()) {
            pref.edit().remove(PROP_LOCALE).apply();
        } else {
            pref.edit().putString(PROP_LOCALE, value).apply();
        }
    }

    public void saveSID(String sid) {
        pref.edit().putString(PROP_SID, sid).apply();
    }

    public String getSID() {
        return pref.getString(PROP_SID, "undef");
    }

    public void savePhonePref(String phonePref) {
        pref.edit().putString(PROP_PHONE_PREF, phonePref).apply();
    }

    public void saveTabFavoriteExist(boolean b) {
        pref.edit().putBoolean(PROP_FAVORITE, b).apply();
    }

    public void saveTabUsersExist(boolean b) {
        pref.edit().putBoolean(PROP_USERS, b).apply();
    }

    public void saveTabGroupExist(boolean b) {
        pref.edit().putBoolean(PROP_GROUP, b).apply();
    }

    public void saveTabCompExist(boolean b) {
        pref.edit().putBoolean(PROP_COMP, b).apply();
    }

    public boolean getTabFavoriteExist() {
        return pref.getBoolean(PROP_FAVORITE, getFavoriteCount() != 0);
    }

    public boolean getTabUsersExist() {
        return pref.getBoolean(PROP_USERS, getFavoriteCount() != 0);
    }

    public boolean getTabGroupExist() {
        return pref.getBoolean(PROP_GROUP, getGroupChatsSize() != 0);
    }

    public boolean getTabCompExist() {
        return pref.getBoolean(PROP_COMP, getCompaniesId().size() != 0);
    }

    public String getPhonePref() {
        return pref.getString(PROP_PHONE_PREF, "380");
    }

    public void unavtorize() {
        clearHistory();
        StateHolder.getInstance(ctx).setUnRegistered();
        pref.edit().clear().apply();
    }

    public String getLocale() {
        return pref.getString(PROP_LOCALE, Locale.getDefault().getLanguage());
    }

    public void setLocation(float lat, float lon) {
        pref.edit().putFloat(PROP_LOC_LAT, lat).putFloat(PROP_LOC_LON, lon).apply();
    }

    public LatLng getLocation() {
        return new LatLng(pref.getFloat(PROP_LOC_LAT, 0), pref.getFloat(PROP_LOC_LON, 0));
    }

    public void saveDefCompForm(String compForm) {
        pref.edit().putString(PROP_DEF_COMP_FORM, compForm).apply();
    }

    public void saveCompForm(String chatId, String compForm) {
        pref.edit().putString(PROP_COMP_FORM_PREFIX_ + chatId, compForm).apply();
    }

    public JSONObject getCompForm(String chatId) {
        try {
            String s = pref.getString(PROP_COMP_FORM_PREFIX_ + chatId, null);
            if (s == null) {
                s = pref.getString(PROP_DEF_COMP_FORM, null);
            }
            return s == null ? null : new JSONObject(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDefBar() {
        return pref.getString(PROP_DEF_BAR, SendBar.DEFAULT_SB);
    }

    public void saveDefBar(String bar) {
        pref.edit().putString(PROP_DEF_BAR, bar).apply();
    }

    public void saveBar(String userId, String bar) {
        pref.edit().putString(PROP_BAR + userId, bar).apply();
    }

    public String getBar(String userId) {
        return pref.getString(PROP_BAR + userId, "{}"/*SendBar.DEFAULT_SB*/);
    }

    public void saveBarO(String userId, String bar) {
        pref.edit().putString(PROP_BAR_O + userId, bar).apply();
    }

    public String getBarO(String userId) {
        return pref.getString(PROP_BAR_O + userId, "{}");
    }

    public void saveMessageOnPause(String s, String chatId) {
        pref.edit().putString(PROP_CHAT_PAUSE + chatId, s).apply();
    }

    public String getMessageOnPause(String chatId) {
        return pref.getString(PROP_CHAT_PAUSE + chatId, "");
    }

    public void setChatMembers(String chatId, JSONArray ja) {
        pref.edit().putString(PROP_CHAT_MEMBERS_PREFIX + chatId, ja.toString()).apply();
    }

    public List<User> getChatMembers(String chatId) {
        List<User> rez = new ArrayList<>();
        try {
            String strJson = pref.getString(PROP_CHAT_MEMBERS_PREFIX + chatId, "no");
            if (!"no".equals(strJson)) {
                try {
                    JSONArray ja = new JSONArray(strJson);
                    List<String> userIdList = new ArrayList<>();
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = ja.getJSONObject(i);
                        userIdList.add(jo.getString("userId"));
                    }

                    for (ChatBased cb : getUsers(true)) {
                        User u = (User) cb;
                        if (userIdList.contains(u.getUserId())) {
                            rez.add(u);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            pref.edit().remove(PROP_CHAT_MEMBERS_PREFIX + chatId).apply();
        }
        return rez;
    }

    public Map<String, String> getChatMembersRole(String chatId) {
        Map<String, String> rez = new HashMap<>();
        String strJson = pref.getString(PROP_CHAT_MEMBERS_PREFIX + chatId, "no");
        if (!"no".equals(strJson)) {
            try {
                JSONArray ja = new JSONArray(strJson);
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    rez.put(jo.getString("userId"), jo.getString("role"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return rez;
    }

    public void saveMyCoordinates(double latitude, double longitude) {
        pref.edit().putFloat(PROP_LATITUDE, (float) latitude).putFloat(PROP_LONGITUDE, (float) longitude).apply();
    }

    public float[] getMyCoordinates() {
        float lon = pref.getFloat(PROP_LONGITUDE, 0);
        float lat = pref.getFloat(PROP_LATITUDE, 0);
        return new float[]{lon, lat};
    }

    public void setChatHole(String chatId, String top, String both) {
        Tool.log("--- setChatHole top = " + top + ", both = " + both);
        try {
            JSONArray ja = new JSONArray();

            if (pref.contains(PROP_CHAT_HOLE_PREFIX + chatId)) {
                String s = pref.getString(PROP_CHAT_HOLE_PREFIX + chatId, null);
                ja = new JSONArray(s);
            }
            JSONObject jo = new JSONObject().put("top", top).put("bottom", both);
            ja.put(jo);

            pref.edit().putString(PROP_CHAT_HOLE_PREFIX + chatId, ja.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updChatHole(String chatId, long top, long both) {
        try {
            String s = pref.getString(PROP_CHAT_HOLE_PREFIX + chatId, null);
            if (s != null) {
                JSONArray ja = new JSONArray(s);

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    if (jo.optLong("bottom") == both + 1) {
                        if (jo.optLong("top") != top - 1) {
                            jo.put("bottom", top);
                        } else {
                            ja = removeJo(ja, i);
                        }
                    }
                }

                pref.edit().putString(PROP_CHAT_HOLE_PREFIX + chatId, ja.toString()).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONArray removeJo(JSONArray ja, int pos) {
        List<String> list = new ArrayList<>();
        try {
            for (int i = 0; i < ja.length(); i++) {
                if (i != pos) {
                    list.add(ja.get(i).toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray(list);
    }

    public void delChatHole(String chatId) {
        pref.edit().remove(PROP_CHAT_HOLE_PREFIX + chatId).apply();
    }

    public List<String[]> getChatHole(String chatId) {
        try {
            String s = pref.getString(PROP_CHAT_HOLE_PREFIX + chatId, null);
            if (s != null) {
                List<String[]> listHoles = new ArrayList<>();
                JSONArray ja = new JSONArray(s);

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    listHoles.add(new String[]{jo.getString("top"), jo.getString("bottom")});
                }
                return listHoles;
            } else {
                return null;
            }

        } catch (Exception e) {
            Tool.log("chatHole err: " + e.getMessage());
            e.printStackTrace();
            delChatHole(chatId);
        }
        return null;
    }

    public String getBarByChatId(String chatId) {
        String bar = getDefBar(); //P2P or group chat
        if (Tool.isP2PChat(chatId)) {
            if (isCompany(chatId) && !"{}".equals(getBar(Tool.getUserId(chatId)))) {
                bar = getBar(Tool.getUserId(chatId)); //Comp chat
            }
        } else {
            if (isOperChat(chatId)) {
                List<User> listMem = getChatMembers(chatId);
                for (User u : listMem) {
                    if (u.isCompany()) {
                        if (!"{}".equals(getBarO(Tool.getUserId(u.getUserId())))) {
                            bar = getBarO(Tool.getUserId(u.getUserId())); //Oper chat
                        } else {
                            bar = getDefBar();
                        }
                    }
                }
            }
        }
        return bar;
    }

    /////////////////////////////////////////////////////////////////////////////////

    private Dialog dialogFromCursor(Cursor cursor) {
        Dialog d = new Dialog();
        d.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
        d.setBlocked(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_BLOCK)) == 1);
        d.setChatId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHAT_ID)));
        d.setChatPhoto(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ICON_URL)));
        d.setCountUnread(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COUNT_UNREAD)));
        d.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_FAVORITE)) == 1);
        d.setMute(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_MUTE)) == 1);
        d.setCompanyId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMPANY_ID)));
        d.setOperator(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_OPER)) == 1);
        d.setMessageText(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_MSG)));
        d.setEncrKey(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ENCR_KEY)));
        d.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATUS)));
        d.setMessageTime(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_LAST_TIME)));
        return d;
    }

    private User userFromCursor(Cursor cursor) {
        User u = new User();
        u.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
        u.setBlocked(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_BLOCK)) == 1);
        u.setCompany(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_COMPANY)) == 1);
        u.setChatId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CHAT_ID)));
        u.setRawPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE)));
        u.setChatPhoto(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ICON_URL)));
        u.setCountUnread(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COUNT_UNREAD)));
        u.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_FAVORITE)) == 1);
        u.setMute(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_MUTE)) == 1);
        u.setMessageText(cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_MSG)));
        u.setBtcAddr(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BTC_ADDR)));
        u.setMsgKey(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MSG_KEY)));
        u.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATUS)));
        u.setMessageTime(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_LAST_TIME)));
//        u.setOwn(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_OWN)) == 1);
        return u;
    }

    public void saveAuthValues(String devId, String devKey, String authToken, String companyId) {
        pref.edit().putString(PROP_DEV_ID, devId).apply();
        pref.edit().putString(PROP_DEV_KEY, devKey).apply();
        pref.edit().putString(PROP_AUTH_TOKEN, authToken).apply();
        pref.edit().putString(PROP_COMPANY_ID, companyId).apply();
    }

    public void saveAuthToken(String authToken) {
        pref.edit().putString(PROP_AUTH_TOKEN, authToken).apply();
    }

    public String getDevId() {
        return pref.getString(PROP_DEV_ID, "");
    }

    public String getDevKey() {
        return pref.getString(PROP_DEV_KEY, "");
    }

    public String getAuthToken() {
        return pref.getString(PROP_AUTH_TOKEN, "");
    }

    public boolean isEmptyAuthToken() {
        return "".equals(getAuthToken());
    }

    public String getCompanyId() {
        return pref.getString(PROP_COMPANY_ID, "");
    }

    public void saveUdid(String udid) {
        pref.edit().putString(PROP_UDID, udid).apply();
    }

    public String getUdid() {
        return pref.getString(PROP_UDID, "");
    }
}
