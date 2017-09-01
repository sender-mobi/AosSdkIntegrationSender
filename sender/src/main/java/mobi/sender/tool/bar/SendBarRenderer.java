package mobi.sender.tool.bar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.LeaveChatReq;
import mobi.sender.model.msg.TextMessage;
import mobi.sender.tool.ActionExecutor;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.KeyboardUtils;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.CircularProgressBar;
import mobi.sender.ui.flow.FlowLayout;

public class SendBarRenderer {
    private static final String OPERATION_SWITCH_OPERATOR_ONLY = "switchOperOnly";
    private static final String OPERATION_SUPPORT = "support";
    private static final String TYPE_SELECT_THEME = "subjects";
    private static final String TYPE_SELECT_SNIPPETS = "snippets";
    public static boolean ENABLE_CUSTOM_VIEW_IDENTIFICATION = true;
    public static final int BIAS = 1000;
    public static final int SENDBAR_DOC_ID = 0;
    public static final int SENDBAR_DOC_PLUS_ID = 1;
    public static final int SENDBAR_DOC_CANCEL_ID = 2;
    public static final int SENDBAR_DOC_TWITCH_ID = 3;
    public static final int SENDBAR_DOC_GEO2_ID = 4;
    public static final int SENDBAR_DOC_TEXT_ID = 5;
    public static final int SENDBAR_DOC_CAMERA_ID = 6;
    public static final int SENDBAR_DOC_SMILE_ID = 7;
    public static final int SENDBAR_DOC_VOICE_ID = 8;
    public static final int SENDBAR_DOC_ADD_USER_ID = 9;
    public static final int SENDBAR_DOC_STICKERS_ID = 10;
    public static final int SENDBAR_DOC_ATTACH_ID = 11;
    public static final int SENDBAR_DOC_VIDEO_ID = 12;
    public static final int SENDBAR_DOC_CARDS_ID = 13;
    public static final int SENDBAR_DOC_SEND_ID = 14;
    public static final int SENDBAR_DOC_TOP_UP_ID = 15;
    public static final int SENDBAR_DOC_LOCK_OPEN_CLOSE_ID = 16;
    public static final int SENDBAR_DOC_VIBRO_ID = 17;
    public static final int SENDBAR_DOC_AMES_ID = 18;
    public static final int SENDBAR_DOC_TICTAC_ID = 19;
    public static final int SENDBAR_DOC_VINNI_ID = 20;
    public static final int SENDBAR_DOC_CHESS_ID = 21;
    public static final int SENBAR_ID = BIAS + 0;
    public static final int SENBAR_PLUS_ID = BIAS + 1;
    public static final int SENBAR_CANCEL_ID = BIAS + 2;
    public static final int SENBAR_TWITCH_ID = BIAS + 3;
    public static final int SENBAR_GEO2_ID = BIAS + 4;
    public static final int SENBAR_TEXT_ID = BIAS + 5;
    public static final int SENBAR_CAMERA_ID = BIAS + 6;
    public static final int SENBAR_SMILE_ID = BIAS + 7;
    public static final int SENBAR_VOICE_ID = BIAS + 8;
    public static final int SENBAR_ADD_USER_ID = BIAS + 9;
    public static final int SENBAR_STICKERS_ID = BIAS + 10;
    public static final int SENBAR_ATTACH_ID = BIAS + 11;
    public static final int SENBAR_VIDEO_ID = BIAS + 12;
    public static final int SENBAR_CARDS_ID = BIAS + 13;
    public static final int SENBAR_SEND_ID = BIAS + 14;
    public static final int SENBAR_TOP_UP_ID = BIAS + 15;
    public static final int SENBAR_LOCK_OPEN_CLOSE_ID = BIAS + 16;
    public static final int SENBAR_VIBRO_ID = BIAS + 17;
    public static final int SENBAR_GAMES_ID = BIAS + 18;
    public static final int SENBAR_TICTAC_ID = BIAS + 19;
    public static final int SENBAR_VINNI_ID = BIAS + 20;
    public static final int SENBAR_CHESS_ID = BIAS + 21;

    public static final int SENDBAR_LEVEL_0_ID = BIAS * 2 + 1;
    public static final int SENDBAR_LEVEL_1_ID = BIAS * 2 + 2;

    private static final String PARAM_ITEMS = "items";
    private static final String PARAM_OPER = "oper";
    private static final String PARAM_INIT = "init";
    private static final String PARAM_LEVEL_0 = "_0";
    private static final String PARAM_LEVEL_1 = "_1";
    private static final String OPERATION_RELOAD = "reload";
    private static final String TYPE_SMILE = "smile";
    private static final String OPERATION_SEND_MESSAGE = "sendMsg";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_ICON = "icon";
    private static final String PARAM_ICON_2 = "icon2";
    private static final String PARAM_ID = "id";
    private static final String PARAM_ACTIONS = "actions";
    private static final String PNG_SUFFIX = ".png";
    private static final String PNG_SUFFIX_4X = "@3x.png";
    private static final String OPERATION_SEND_MEDIA = "sendMedia";
    private static final String TYPE_STICKER = "sticker";
    private static final String TYPE_VOICE = "voice";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_STICKER_ID = "stickerId";
    private static final String PARAM_FILE_PATH = "filePath";
    private static final String PARAM_LENGTH = "length";
    private static final String OPERATION_SWITCH_CRYPTO = "switchCrypto";
    private static final String OPERATION_CALL_ROBOT = "callRobot";
    private static final int ICON_SIZE = 36;
    private static final long KEYBOARD_HIDE_TIME = 213L;
    private static final String PARAM_TEXT_COLOR = "textColor";

    private static HashMap<String, String> lastMsgMap = new HashMap<>();

    private Activity act;
    private int typingLenSent = 0;
    private Map<String, BarItem> items = new HashMap<>();
    private JSONObject jo;
    private BarListener barListener;
    private LinearLayout level0, level1;
    private View[] prevViews;
    private MonitoringEditText msgEt;
    private StickerFactory sf;
    private ForwardActionListener fal;
    private static final String sendIcon = "https://s.sender.mobi/bars/send.png";
    public static final String TAG = SendBarRenderer.class.getSimpleName();
    private static final String SEND_TAG = "send_btn";
    private String mChatId;
    private Handler mHandler;
    private boolean isOpenKeyboard;
    private ResultReceiver receiver;
    private ImageView siv;
    private boolean biginEdit = false;
    private boolean onlyForOperators = false;
    private String userId;
    private boolean isBarOpen = false;

    public SendBarRenderer(Activity act, JSONObject jo, BarListener barListener, String chatId) {
        this(act, jo, null, barListener, chatId);
    }

    public SendBarRenderer(Activity act, JSONObject jo, ForwardActionListener fal, BarListener barListener) {
        this(act, jo, fal, barListener, null);
    }

    public SendBarRenderer(final Activity act, final JSONObject jo, final ForwardActionListener fal, BarListener barListener, String chatId) {
        this.act = act;
        this.jo = jo;
        this.fal = fal;
        this.mChatId = chatId;
        sf = new StickerFactory();
        this.barListener = barListener;
        mHandler = new Handler();
        receiver = new ResultReceiver(mHandler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                isOpenKeyboard = (resultCode == InputMethodManager.RESULT_UNCHANGED_SHOWN || resultCode == InputMethodManager.RESULT_SHOWN);
                Tool.log("+++receiver = " + isOpenKeyboard);
            }
        };
        level0 = new LinearLayout(act);
        level1 = new LinearLayout(act);
        level0.setOrientation(LinearLayout.HORIZONTAL);
        level1.setOrientation(LinearLayout.VERTICAL);
        level0.setGravity(Gravity.CENTER);
        level1.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        level0.setLayoutParams(params);
        level1.setLayoutParams(params);
        if (ENABLE_CUSTOM_VIEW_IDENTIFICATION) {
            level0.setId(SENDBAR_LEVEL_0_ID);
            level0.setId(SENDBAR_LEVEL_1_ID);
        }

        msgEt = new MonitoringEditText(act);
        msgEt.setCutPasteListener(new MonitoringEditText.OnCutPasteListener() {
            @Override
            public void onCut() {
                lastMsgMap.put(mChatId, "");
            }

            @Override
            public void onPaste() {
                lastMsgMap.put(mChatId, msgEt.getText().toString());
            }
        });
//        msgEt.setTypeface(TypefaceUtils.get(TypefaceUtils.Type.symbolaemoji));
        msgEt.addTextChangedListener(new MsgUI());
        msgEt.setBackgroundDrawable(null);
        msgEt.setTextColor(Color.BLACK);
        msgEt.setMaxLines(5);
        msgEt.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        msgEt.setImeOptions(EditorInfo.IME_ACTION_DONE);
        msgEt.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        msgEt.setOnFocusChangeListener(new DefaultOnFocusChangeListener());
        msgEt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    saveBack();
                    level1.removeAllViews();
//                    showKb();
                }
                return false;
            }
        });

        JSONArray arr = jo.optJSONArray(PARAM_ITEMS);
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                BarItem item = new BarItem(arr.optJSONObject(i));
                items.put(item.getId(), item);
            }
        }

        //hide keyboard
        KeyboardUtils.checkKeyboardHeight(act.findViewById(android.R.id.content), act, new KeyboardUtils.OnKeyboardShownListener() {
            @Override
            public void onShown(boolean isShown) {
                if (!isShown) {
                    if (isOpenTextInput()) {
                        Tool.log("&&& 1");
                        buildLevel(jo.optJSONObject(PARAM_INIT));
                        isOpenKeyboard = false;
                    }
                }
            }
        });
    }


    public static void preloadLinks(JSONObject jo) {
        JSONArray arr = jo.optJSONArray(PARAM_ITEMS);
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                preloadItem(arr.optJSONObject(i));
            }
        }
    }

    private static void preloadItem(JSONObject item) {
        loadImage(item.optString(PARAM_ICON));
        loadImage(item.optString(PARAM_ICON_2));
    }

    private static void loadImage(String img) {
        if (img != null && !img.isEmpty()) {
            if (!img.contains("@")) img = img.replace(PNG_SUFFIX, PNG_SUFFIX_4X);
//            ImageLoader.getInstance().loadImage(img, null);
        }
    }

    private void showKb() {
        ((InputMethodManager) SendBarRenderer.this.act.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(msgEt, InputMethodManager.SHOW_FORCED, receiver);
    }

    private void hideKb() {
        ((InputMethodManager) SendBarRenderer.this.act.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(msgEt.getWindowToken(), 0, receiver);
        msgEt.clearFocus();
    }

    public SendBarRenderer setCryptoEnabled(boolean isEnabled) {
        try {
            for (String k : items.keySet()) {
                BarItem bi = items.get(k);
                if (OPERATION_SWITCH_CRYPTO.equalsIgnoreCase(bi.getActionsSrc().optJSONObject(0).optString(PARAM_OPER))) {
                    bi.setEnabled(isEnabled);
                    setCryptoBg(isEnabled);
                    items.put(k, bi);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public View init() {
        LinearLayout root = new LinearLayout(act);
        root.setBackgroundColor(act.getResources().getColor(android.R.color.white));
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Tool.log("&&& 2");
        buildLevel(jo.optJSONObject("init"));
        clearView(level0);
        root.addView(level0);
        root.addView(sf.getDivider());
        clearView(level1);
        root.addView(level1);
        return root;
    }

    public void minimize() {
        hideKb();
        Tool.log("&&& 3");
        buildLevel(jo.optJSONObject(PARAM_INIT));
    }

    private void buildLevel(JSONObject jo) {
        Tool.log("&&& buildLevel = "+jo);
        if (jo.has(PARAM_LEVEL_0)) {
            buildLevel(jo.optJSONArray(PARAM_LEVEL_0), true);
            isBarOpen = false;
        }
        if (jo.has(PARAM_LEVEL_1)) {
            buildLevel(jo.optJSONArray(PARAM_LEVEL_1), false);
            isBarOpen = true;
        } else {
            level1.removeAllViews();
        }
    }

    public boolean getIsBarOpen() {
        return isBarOpen;
    }

    private void buildLevel(JSONArray ids, boolean isLinear) {
        if (isLinear) {
            level0.removeAllViews();
            level0 = buildRow(level0, ids, false);
        } else {
            int oldCount = level1.getChildCount();
            saveBack();
            level1.removeAllViews();
            int k = 0;
            while (k < ids.length()) {
                JSONArray sl = new JSONArray();
                for (int i = 0; i < 4; i++, k++) {
                    if (i == ids.length()) break;
                    try {
                        sl.put(ids.get(k));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                level1.addView(buildRow(null, sl, true));
            }
            if (oldCount > 0 && level1.getChildCount() < oldCount) {
                addBackBtn();
            }
        }
    }

    private void addBackBtn() {
        LinearLayout row = new LinearLayout(act);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ImageView iv = new ImageView(act);
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams((int) Tool.convertDpToPixel(40, act), (int) Tool.convertDpToPixel(40, act));
        vlp.topMargin = 12;
        iv.setLayoutParams(vlp);
        iv.setImageResource(R.drawable._arrow_back);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        row.addView(iv);
        level1.addView(row);
    }

    private void clearView(View v) {
        ViewParent parent = v.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeAllViews();
        }
    }

    private void saveBack() {
        prevViews = new View[level1.getChildCount()];
        for (int i = 0; i < level1.getChildCount(); i++) prevViews[i] = level1.getChildAt(i);
    }

    private void back() {
        if (prevViews == null) return;
        level1.removeAllViews();
        for (View pv : prevViews) level1.addView(pv);
    }

    private LinearLayout buildRow(LinearLayout row, JSONArray jo, boolean withBg) {
        if (row == null) {
            row = new LinearLayout(act);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        for (int i = 0; i < jo.length(); i++) {
            String id = jo.optString(i);
            BarItem bi = items.get(id);
            row.addView(bi.toView(withBg, level0 == row));
        }
        return row;
    }

    private Action[] parseActions(JSONArray arr) {
        Action[] actions = new Action[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            actions[i] = new Action(arr.optJSONObject(i));
        }
        Arrays.sort(actions, new Comparator<Action>() {
            @Override
            public int compare(Action lhs, Action rhs) {
                if (lhs.oper.equalsIgnoreCase(OPERATION_RELOAD) && !rhs.oper.equalsIgnoreCase(OPERATION_RELOAD))
                    return -1;
                if (!lhs.oper.equalsIgnoreCase(OPERATION_RELOAD) && rhs.oper.equalsIgnoreCase(OPERATION_RELOAD))
                    return 1;
                return 0;
            }
        });
        return actions;
    }

    private String getName(JSONObject name) {
//        String loc = PreferenceManager.getDefaultSharedPreferences(act).getString(App.PREF_LOCALE_T, App.PREF_LOCALE_DEFAULT_T);
        String loc = Storage.getInstance(act).getLocale();
        if (name.has(loc)) {
            return name.optString(loc);
        } else {
            return name.optString("ru");
        }
    }

    private String getTextId() {
        for (String id : items.keySet()) {
            BarItem bi = items.get(id);
            Action[] actions = parseActions(bi.getActionsSrc());
            for (Action a : actions) {
                if (OPERATION_SEND_MESSAGE.equalsIgnoreCase(a.getOper())) {
                    return id;
                }
            }
        }
        return null;
    }

    public void setText(String t) {
        proceedActions(findSendMessageId(), true);
        msgEt.setText(Tool.parseSmiles(t));
    }

    private void proceedActions(String id, boolean isFocused) {
        if (Tool.isEmptyString(id)) return;
//        if (TextUtils.isEmpty(id)) return;
        for (Action a : parseActions(items.get(id).getActionsSrc())) {
            if (OPERATION_RELOAD.equalsIgnoreCase(a.getOper())) {
                Tool.log("&&& 4");
                buildLevel(a.getSrc());
                break;
            }
        }
        Tool.log("*** id = " + id + ", isFocused = " + isFocused);
        openEdit(id, isFocused);
    }

    private String findSendMessageId() {
        String result = "";
        for (String id : items.keySet()) {
            Action[] actions = parseActions(items.get(id).getActionsSrc());
            for (Action a : actions) {
                if (OPERATION_SEND_MESSAGE.equalsIgnoreCase(a.getOper())) {
                    result = id;
                    break;
                }
            }
        }
        return result;
    }


    public void execActions(String id, JSONArray acts) {
        Tool.log("&&& id = " + id + ", acts = " + acts);
        BarItem bi = items.get(id);
        if (bi == null) return;
        for (final Action a : parseActions(acts)) {
            if (fal != null) {
                fal.onForward(id, acts);
                return;
            }
            bi.setEnabled(!bi.isEnabled());
            if (OPERATION_RELOAD.equalsIgnoreCase(a.getOper())) {
                if (isOpenKeyboard) {
                    hideKb();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Tool.log("&&& 5");
                            buildLevel(a.getSrc());
                        }
                    }, KEYBOARD_HIDE_TIME);
                } else {
                    Tool.log("&&& 6");
                    buildLevel(a.getSrc());
                }
            } else if (OPERATION_SWITCH_CRYPTO.equalsIgnoreCase(a.getOper())) {
                setCryptoBg(bi.isEnabled());
                barListener.doAction(a.getOper(), a.getParams());
            } else if (OPERATION_SEND_MEDIA.equalsIgnoreCase(a.getOper()) && TYPE_SMILE.equalsIgnoreCase(a.getParams().get(PARAM_TYPE).toString())) {
                level1.removeAllViews();
                if (isOpenKeyboard) {
                    hideKb();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showSmiles();
                            Tool.log("&&& W");
                        }
                    }, KEYBOARD_HIDE_TIME);
                } else {
                    showSmiles();
                }

            } else if (OPERATION_SEND_MEDIA.equalsIgnoreCase(a.getOper()) && TYPE_STICKER.equalsIgnoreCase(a.getParams().get(PARAM_TYPE).toString())) {
                saveBack();
                level1.removeAllViews();
                if (isOpenKeyboard) {
                    hideKb();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Tool.log("&&& E");
                            showStickers(a);
                        }
                    }, KEYBOARD_HIDE_TIME);
                } else {
                    showStickers(a);
                }
            } else if (OPERATION_SEND_MEDIA.equalsIgnoreCase(a.getOper()) && TYPE_VOICE.equalsIgnoreCase(a.getParams().get(PARAM_TYPE).toString())) {
                if (ActivityCompat.checkSelfPermission(act, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    saveBack();
                    level1.removeAllViews();
                    if (isOpenKeyboard) {
                        hideKb();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Tool.log("&&& R");
                                showVoice(a);
                            }
                        }, KEYBOARD_HIDE_TIME);
                    } else {
                        showVoice(a);
                    }
                } else {
                    ActivityCompat.requestPermissions(act,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            ChatActivity.MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                }

            } else if (OPERATION_SEND_MESSAGE.equalsIgnoreCase(a.getOper())) {
                openEdit(id, true);
            } else if (OPERATION_CALL_ROBOT.equalsIgnoreCase(a.getOper())) {
                minimize();
                barListener.doAction(a.getOper(), a.getParams());
            } else if (OPERATION_SWITCH_OPERATOR_ONLY.equalsIgnoreCase(a.getOper())) {
                switchMsgOnlyForOperators();
            } else if (OPERATION_SUPPORT.equalsIgnoreCase(a.getOper())) {
                if (TYPE_SELECT_THEME.equalsIgnoreCase(a.getType())) {
//                    new SelectThemeDialog(act, userId, mChatId).show();
                } else if (ActionExecutor.TYPE_LEAVE_CHAT.equalsIgnoreCase(a.getType())) {
                    Bus.getInstance().post(new LeaveChatReq(mChatId));
                }

//                else if (TYPE_SELECT_SNIPPETS.equalsIgnoreCase(a.getType()))
//                    new SelectSnippetDialog(act, userId, new SelectSnippetDialog.OnSnippetSelectListener() {
//                        @Override
//                        public void onStart() {
//                            proceedActions(findSendMessageId(), true);
//                        }
//
//                        @Override
//                        public void onSnippetSelect(final CompanySnippet companySnippet) {
//                            msgEt.setText(companySnippet.getText());
//                        }
//                    }).show();

            } else {
                barListener.doAction(a.getOper(), a.getParams());
            }
        }
    }

    private void switchMsgOnlyForOperators() {
        onlyForOperators = !onlyForOperators;
    }

//    private void leaveChatAction() {
////        Bus.getInstance().post(new LeaveChatRequest(mChatId));
//    }

    public boolean isOnlyForOperators() {
        return onlyForOperators;
    }

    private void showVoice(final Action a) {
        level1.addView(sf.getVoiceRecorder(act, new OnVoiceSendListener() {
            @Override
            public void onSendVoice(String fileName, float length) {
                back();
                Map<String, Object> params = a.getParams();
                params.put(PARAM_FILE_PATH, fileName);
                params.put(PARAM_LENGTH, String.valueOf(length));
                barListener.doAction(a.getOper(), params);
            }

            @Override
            public void onMediaNotFound() {
//                App.getInstance().showToast(R.string.ac_camera_sd_unmounted);
                Toast.makeText(act, "ac_camera_sd_unmounted", Toast.LENGTH_SHORT).show();
                minimize();
            }

        }));
    }

    private void showStickers(final Action a) {
        level1.addView(sf.getStickers(act, new OnStickerClickListener() {
            @Override
            public void onStickerClick(String s) {
                Map<String, Object> params = a.getParams();
                params.put(PARAM_STICKER_ID, s);
                barListener.doAction(a.getOper(), params);
            }

            @Override
            public void onBackClick() {
                back();
            }
        }));
    }

    private void showSmiles() {
        proceedActions(findSendMessageId(), false);
        level1.addView(sf.getEmoFlow(act, new OnEmoSelectListener() {
            @Override
            public void onSelect(String s) {
                String prev = msgEt.getText().toString();
                prev = prev + s;
                lastMsgMap.put(mChatId, prev);
                msgEt.setText(prev);
                msgEt.setSelection(prev.length());
                Tool.checkEmojiSupport(act, msgEt);
            }
        }));
    }

    private void openEdit(String id, boolean isFocused) {
        clearView(msgEt);
        LinearLayout cnt = (LinearLayout) level0.findViewWithTag(id);

        if (cnt == null) cnt = (LinearLayout) level0.getChildAt(0);

        resetWeights((LinearLayout) cnt.getParent());
        cnt.removeAllViews();


        msgEt.setText(Tool.parseSmiles(lastMsgMap.get(mChatId)));
        msgEt.setSelection(msgEt.getText().length());
        cnt.addView(msgEt);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cnt.getLayoutParams();
        lp.weight = 10;
        cnt.setLayoutParams(lp);
        if (isFocused) msgEt.requestFocus();
    }

//    private void openEdit(String id, boolean isFocused) {
//        clearView(msgEt);
//        LinearLayout cnt = (LinearLayout) level0.findViewWithTag(id);
//            resetWeights((LinearLayout) cnt.getParent());
//            cnt.removeAllViews();
//
//
//            msgEt.setText(Tool.parseSmiles(lastMsgMap.get(mChatId)));
//            msgEt.setSelection(msgEt.getText().length());
//            cnt.addView(msgEt);
//            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cnt.getLayoutParams();
//            lp.weight = 10;
//            cnt.setLayoutParams(lp);
//            if (isFocused) msgEt.requestFocus();
//    }


    private void resetWeights(LinearLayout parent) {
        for (int i = 0; i < parent.getChildCount(); i++)
            ((LinearLayout.LayoutParams) parent.getChildAt(i).getLayoutParams()).weight = 0;
    }

    private void setCryptoBg(boolean isEnabled) {
        if (isEnabled) msgEt.setBackgroundColor(act.getResources().getColor(R.color.colorAccent));
        else msgEt.setBackgroundColor(act.getResources().getColor(android.R.color.transparent));
    }

    public void beginMsgEdit(TextMessage message, final MsgEditListener listener) {
        proceedActions(findSendMessageId(), true);
        msgEt.setText(Tool.parseSmiles(message.getText()));
        msgEt.setSelection(msgEt.getText().toString().length());
        biginEdit = true;
        showKb();
        if (siv != null)
            siv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (msgEt.getText() != null && msgEt.getText().length() > 0) {
                        biginEdit = false;
                        listener.onEdited(msgEt.getText().toString());
                        lastMsgMap.put(mChatId, "");
                        msgEt.setText("");
                        siv.setOnClickListener(new MsgSendListener());
                        minimize();
                    }
                }
            });
        msgEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                biginEdit = false;
                if (!hasFocus) {
                    msgEt.getText().clear();
                    if (siv != null)
                        siv.setOnClickListener(new MsgSendListener());
                    msgEt.setOnFocusChangeListener(new DefaultOnFocusChangeListener());
                }
            }
        });
    }

  /*  public void beginMsgEdit(FMessage message, final MsgEditListener listener) {
        proceedActions(findSendMessageId(), true);
        msgEt.setText(Tool.parseSmiles(message.getStringMessage()));
        msgEt.setSelection(msgEt.getText().toString().length());
        biginEdit = true;
        showKb();
        if (siv != null)
            siv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (msgEt.getText() != null && msgEt.getText().length() > 0) {
                        biginEdit = false;
                        listener.onEdited(msgEt.getText().toString());
                        lastMsgMap.put(mChatId, "");
                        msgEt.setText("");
                        siv.setOnClickListener(new MsgSendListener());
                        minimize();
                    }
                }
            });
        msgEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                biginEdit = false;
                if (!hasFocus) {
                    msgEt.getText().clear();
                    if (siv != null)
                        siv.setOnClickListener(new MsgSendListener());
                    msgEt.setOnFocusChangeListener(new DefaultOnFocusChangeListener());
                }
            }
        });
    }*/

    public boolean isOpenTextInput() {
        return msgEt.hasFocus();
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public interface MsgEditListener {
        void onEdited(String text);
    }

    private class BarItem {
        private String img1, img2, id;
        private JSONObject name;
        private boolean enabled = false;
        private JSONArray actionsSrc;
        private ImageView iv;

        public BarItem(JSONObject jo) {
            name = jo.optJSONObject(PARAM_NAME);
            img1 = jo.optString(PARAM_ICON);
            img2 = jo.optString(PARAM_ICON_2);
            id = jo.optString(PARAM_ID);
            iv = new ImageView(act);
            LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams((int) Tool.convertDpToPixel(ICON_SIZE, act), (int) Tool.convertDpToPixel(ICON_SIZE, act));
            iv.setLayoutParams(vlp);
            if (jo.has(PARAM_ACTIONS)) {
                JSONArray arr = jo.optJSONArray(PARAM_ACTIONS);
                actionsSrc = arr;
            }
        }

        private void drawIcon() {
            if (isEnabled() && img2 != null && img2.length() > 0) {
                loadImg(img2, iv);
            } else if (img1 != null) {
                loadImg(img1, iv);
            } else {
                iv.setImageResource(R.drawable._logo_app);
            }
        }

        public LinearLayout toView(boolean withBg, boolean withoutText) {
            final LinearLayout root = new LinearLayout(act);
            root.setOrientation(LinearLayout.VERTICAL);
//            if (withBg) root.setBackgroundDrawable(act.getResources().getDrawable(R.drawable.border));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.weight = 1;
            int pPx = (int) Tool.convertDpToPixel(3, act);
            lp.topMargin = pPx;
            lp.bottomMargin = pPx;
            lp.leftMargin = pPx;
            lp.rightMargin = pPx;
            root.setPadding(pPx, pPx, pPx, pPx);
            root.setLayoutParams(lp);
            root.setGravity(Gravity.CENTER);
            drawIcon();
            clearView(iv);
            iv.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            ImageView view = (ImageView) v;
                            Drawable drawable = view.getDrawable();
                            if (drawable != null)
                                drawable.setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                            view.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            ImageView view = (ImageView) v;
                            Drawable drawable = view.getDrawable();
                            if (drawable != null) drawable.clearColorFilter();
                            view.invalidate();
                            break;
                        }
                    }
                    return false;
                }
            });
            root.addView(iv);
            if (!withoutText && name != null && name.length() > 0) {
                TextView tv = new TextView(act);
                tv.setGravity(Gravity.CENTER);
                tv.setText(getName(name));
                tv.setTextColor(Color.GRAY);
                tv.setSingleLine();
                tv.setEllipsize(TextUtils.TruncateAt.END);
                tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                root.addView(tv);
            }
            root.setTag(getId());
            if (ENABLE_CUSTOM_VIEW_IDENTIFICATION)
                root.setId(getCustomViewId(getId()));

            if (getActionsSrc() != null) {
                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        execActions(getId(), getActionsSrc());
                    }
                });
            }
            return root;
        }

        public int getCustomViewId(String strId) {
            if (strId.equals("" + SENDBAR_DOC_ID)) {
                return SENBAR_ID;
            } else if (strId.equals("" + SENDBAR_DOC_PLUS_ID)) {
                return SENBAR_PLUS_ID;
            } else if (strId.equals("" + SENDBAR_DOC_CANCEL_ID)) {
                return SENBAR_CANCEL_ID;
            } else if (strId.equals("" + SENDBAR_DOC_TWITCH_ID)) {
                return SENBAR_TWITCH_ID;
            } else if (strId.equals("" + SENDBAR_DOC_GEO2_ID)) {
                return SENBAR_GEO2_ID;
            } else if (strId.equals("" + SENDBAR_DOC_TEXT_ID)) {
                return SENBAR_TEXT_ID;
            } else if (strId.equals("" + SENDBAR_DOC_CAMERA_ID)) {
                return SENBAR_CAMERA_ID;
            } else if (strId.equals("" + SENDBAR_DOC_SMILE_ID)) {
                return SENBAR_SMILE_ID;
            } else if (strId.equals("" + SENDBAR_DOC_VOICE_ID)) {
                return SENBAR_VOICE_ID;
            } else if (strId.equals("" + SENDBAR_DOC_ADD_USER_ID)) {
                return SENBAR_ADD_USER_ID;
            } else if (strId.equals("" + SENDBAR_DOC_STICKERS_ID)) {
                return SENBAR_STICKERS_ID;
            } else if (strId.equals("" + SENDBAR_DOC_ATTACH_ID)) {
                return SENBAR_ATTACH_ID;
            } else if (strId.equals("" + SENDBAR_DOC_VIDEO_ID)) {
                return SENBAR_VIDEO_ID;
            } else if (strId.equals("" + SENDBAR_DOC_CARDS_ID)) {
                return SENBAR_CARDS_ID;
            } else if (strId.equals("" + SENDBAR_DOC_SEND_ID)) {
                return SENBAR_SEND_ID;
            } else if (strId.equals("" + SENDBAR_DOC_TOP_UP_ID)) {
                return SENBAR_TOP_UP_ID;
            } else if (strId.equals("" + SENDBAR_DOC_LOCK_OPEN_CLOSE_ID)) {
                return SENBAR_LOCK_OPEN_CLOSE_ID;
            } else if (strId.equals("" + SENDBAR_DOC_VIBRO_ID)) {
                return SENBAR_VIBRO_ID;
            } else if (strId.equals("" + SENDBAR_DOC_AMES_ID)) {
                return SENBAR_GAMES_ID;
            } else if (strId.equals("" + SENDBAR_DOC_TICTAC_ID)) {
                return SENBAR_TICTAC_ID;
            } else if (strId.equals("" + SENDBAR_DOC_VINNI_ID)) {
                return SENBAR_VINNI_ID;
            } else if (strId.equals("" + SENDBAR_DOC_CHESS_ID)) {
                return SENBAR_CHESS_ID;
            } else
                return BIAS;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            drawIcon();
        }

        public String getId() {
            return id;
        }

        public JSONArray getActionsSrc() {
            return actionsSrc;
        }
    }

    protected class Action {
        private String type;
        protected String oper;
        private JSONObject src;
        private Map<String, Object> params;

        public Action(JSONObject jo) {
            this.src = jo;
            oper = jo.optString(PARAM_OPER);
            type = jo.optString(PARAM_TYPE);
            Iterator keys = jo.keys();
            params = new HashMap<>();
            while (keys.hasNext()) {
                String k = (String) keys.next();
                if (PARAM_OPER.equalsIgnoreCase(k)) continue;
                try {
                    params.put(k, jo.getString(k));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public String getType() {
            return type;
        }

        public String getOper() {
            return oper;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public JSONObject getSrc() {
            return src;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }
    }

    public interface BarListener {
        void doAction(String oper, Map<String, Object> params);

        void doTyping(String text);

        void doSend(String msg, boolean onlyForOperators);
    }

    private class MsgUI implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(final Editable s) {
            if (biginEdit)
                return;
            lastMsgMap.put(mChatId, s.toString());
            if (s.length() > 0) {
                if (s.length() > 2) {
                    if (s.length() - typingLenSent > 4) {
                        typingLenSent = s.length();
                        barListener.doTyping(s.toString());
                    }
                    if (typingLenSent - s.length() > 4) {
                        typingLenSent = s.length();
                    }
                }
                LinearLayout root = new LinearLayout(act);
                root.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int pPx = (int) Tool.convertDpToPixel(3, act);
                lp.topMargin = pPx;
                lp.bottomMargin = pPx;
                lp.leftMargin = pPx;
                lp.rightMargin = pPx;
                root.setPadding(pPx, pPx, pPx, pPx);
                root.setLayoutParams(lp);
                root.setGravity(Gravity.CENTER);
                root.setTag(SEND_TAG);
                siv = new ImageView(act);
                LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams((int) Tool.convertDpToPixel(ICON_SIZE, act), (int) Tool.convertDpToPixel(ICON_SIZE, act));
                siv.setLayoutParams(vlp);
                if (jo.has(PARAM_TEXT_COLOR))
                    loadImg(sendIcon, siv, Color.parseColor(jo.optString(PARAM_TEXT_COLOR)));
                else
                    loadImg(sendIcon, siv);
                siv.setOnClickListener(new MsgSendListener());
                root.addView(siv);
                if (level0.findViewWithTag(SEND_TAG) == null) level0.addView(root);
            } else {
                View view = level0.findViewWithTag(SEND_TAG);
                if (view != null) level0.removeView(view);
            }
        }
    }

    private void loadImg(String img, ImageView iv) {
        if (!img.contains("@")) img = img.replace(PNG_SUFFIX, PNG_SUFFIX_4X);
//        ImageLoader.getInstance().displayImage(img, iv);
        Picasso.with(act).load(img).into(iv);
    }

    private void loadImg(String img, final ImageView iv, final int color) {
        if (!img.contains("@")) img = img.replace(PNG_SUFFIX, PNG_SUFFIX_4X);
//        ImageLoader.getInstance().displayImage(img, iv, new ImageLoadingListener() {
//            @Override
//            public void onLoadingStarted(String s, View view) {
//
//            }
//
//            @Override
//            public void onLoadingFailed(String s, View view, FailReason failReason) {
//
//            }
//
//            @Override
//            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
//                Drawable drawable = ((ImageView) view).getDrawable();
//                if (drawable != null) drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
//                view.invalidate();
//            }
//
//            @Override
//            public void onLoadingCancelled(String s, View view) {
//
//            }
//        });

        Picasso.with(act)
                .load(img)
                .into(iv, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Drawable drawable = iv.getDrawable();
                        if (drawable != null) {
                            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        }
                        iv.invalidate();
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    public interface OnEmoSelectListener {
        void onSelect(String emoText);
    }


    public interface OnStickerClickListener {
        public void onStickerClick(String stickerId);

        void onBackClick();
    }

    public interface OnVoiceSendListener {
        public void onSendVoice(String fileName, float length);

        public void onMediaNotFound();
    }

    public interface ForwardActionListener {
        void onForward(String id, JSONArray arr);
    }
//     how to use
//    addView( factory.getEmoFlow(act, new OnEmoSelectListener() {
//    @Override
//    public void onSelect(String s) { }
//    }));

    private class StickerFactory {
        private static final int DURATION = 30000;
        private static final int PADDINGS = 4;
        private static final int DEFAULT_PADDINGS = 4;
        private static final int STROKE_WIDTH = 4;
        private final int RECORDER_COLOR_ID = R.color.colorGray;
        private static final int MODE_HOLD = 1;
        private static final int MODE_MANUAL = 2;
        private final int RECORDER_DRAWABLE_ID = R.drawable.draw_hold_talk;
        private final int PLAYER_DRAWABLE_ID = R.drawable.record;
        private static final float TEXT_SIZE = 18;
        private static final int INNER_STICKER_HEIGHT = 80;
        private static final int MAX_LAYOUT_HEIGHT = 200;
        private static final float NAVIGATION_HEIGHT = (INNER_STICKER_HEIGHT / 2) + DEFAULT_PADDINGS * 3;
        private static final float CONTAINER_HEIGHT = MAX_LAYOUT_HEIGHT - NAVIGATION_HEIGHT;

        ScrollView smilesSL, dogsSL, cardsSL, plaatsSL = null;
        LinearLayout navigationLL, stickersLL, containerLL, voiceLL = null;

        private MediaRecorder recorder;
        private String fileName;
        private long startRecord;
        private float length;

        private String[] dogs = new String[]{"dog_active", "dog_amorous", "dog_brain", "dog_calm", "dog_disappoint",
                "dog_dismal", "dog_evil", "dog_festive", "dog_furious", "dog_greeting", "dog_happy", "dog_introduce", "dog_joyful",
                "dog_love", "dog_malicious", "dog_miss", "dog_pleased", "dog_sad", "dog_sick", "dog_sing", "dog_smile", "dog_sore",
                "dog_surprise", "dog_tongue", "dog_wink"
        };

        private String[] cards = new String[]{"card_2_of_clubs", "card_2_of_diamonds", "card_2_of_hearts", "card_2_of_spades",
                "card_3_of_clubs", "card_3_of_diamonds", "card_3_of_hearts", "card_3_of_spades", "card_4_of_clubs",
                "card_4_of_diamonds", "card_4_of_hearts", "card_4_of_spades", "card_5_of_clubs", "card_5_of_diamonds",
                "card_5_of_hearts", "card_5_of_spades", "card_6_of_clubs", "card_6_of_diamonds", "card_6_of_hearts",
                "card_6_of_spades", "card_7_of_clubs", "card_7_of_diamonds", "card_7_of_hearts", "card_7_of_spades",
                "card_8_of_clubs", "card_8_of_diamonds", "card_8_of_hearts", "card_8_of_spades", "card_9_of_clubs",
                "card_9_of_diamonds", "card_9_of_hearts", "card_9_of_spades", "card_10_of_clubs", "card_10_of_diamonds",
                "card_10_of_hearts", "card_10_of_spades", "card_ace_of_clubs", "card_ace_of_diamonds", "card_ace_of_hearts",
                "card_ace_of_spades", "card_black_joker", "card_jack_of_clubs", "card_jack_of_diamonds", "card_jack_of_hearts",
                "card_jack_of_spades", "card_king_of_clubs", "card_king_of_diamonds", "card_king_of_hearts", "card_king_of_spades",
                "card_queen_of_clubs", "card_queen_of_diamonds", "card_queen_of_hearts", "card_queen_of_spades", "card_red_joker"
        };

        private String[] plaats = new String[]{"plaat_1", "plaat_2", "plaat_3", "plaat_4", "plaat_5", "plaat_6",
                "plaat_7", "plaat_8", "plaat_9", "plaat_10"
        };

        public LinearLayout getStickers(final Context ctx, final OnStickerClickListener listener) {
            if (stickersLL == null) {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dogsSL = getFlow(dogsSL, dogs, ctx, listener);
                        setStickerView(dogsSL);
                    }
                });
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cardsSL = getFlow(cardsSL, cards, ctx, listener);
                    }
                });
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        plaatsSL = getFlow(plaatsSL, plaats, ctx, listener);
                    }
                });
                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.getTag().equals("_arrow_back")) {
                            listener.onBackClick();
                            return;
                        }
                        ScrollView sl = null;
                        if (v.getTag().equals("dog_sample"))
                            sl = getFlow(dogsSL, dogs, ctx, listener);
                        else if (v.getTag().equals("card_sample"))
                            sl = getFlow(cardsSL, cards, ctx, listener);
                        else if (v.getTag().equals("plaat_sample"))
                            sl = getFlow(plaatsSL, plaats, ctx, listener);
                        setStickerView(sl);
                        resetSelection(v);
                    }
                };
                int side = (int) Tool.convertDpToPixel((INNER_STICKER_HEIGHT / 2) + DEFAULT_PADDINGS, ctx);

                containerLL = getContainerLL(ctx);

                stickersLL = new LinearLayout(ctx);
                stickersLL.setOrientation(LinearLayout.VERTICAL);
                stickersLL.setGravity(Gravity.CENTER);
                stickersLL.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) Tool.convertDpToPixel(MAX_LAYOUT_HEIGHT, ctx)));
                stickersLL.setPadding(DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS);

                navigationLL = new LinearLayout(ctx);
                navigationLL.setOrientation(LinearLayout.HORIZONTAL);
                navigationLL.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                navigationLL.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) Tool.convertDpToPixel(NAVIGATION_HEIGHT, ctx)));
                navigationLL.setPadding(DEFAULT_PADDINGS * 2, DEFAULT_PADDINGS * 2, DEFAULT_PADDINGS * 2, DEFAULT_PADDINGS * 2);

                navigationLL.addView(createIv("_arrow_back", side, ctx, listener, clickListener));
                ImageView selected = createIv("dog_sample", side, ctx, listener, clickListener);
                navigationLL.addView(selected);
                navigationLL.addView(createIv("card_sample", side, ctx, listener, clickListener));
                navigationLL.addView(createIv("plaat_sample", side, ctx, listener, clickListener));
                resetSelection(selected);

                containerLL.addView(new ProgressBar(act));
                stickersLL.addView(containerLL);
                stickersLL.addView(getDivider());
                stickersLL.addView(navigationLL);
            }
            return stickersLL;
        }

        private LinearLayout getContainerLL(Context ctx) {
            if (containerLL == null) {
                containerLL = new LinearLayout(ctx);
                containerLL.setOrientation(LinearLayout.VERTICAL);
                containerLL.setGravity(Gravity.CENTER);
                containerLL.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) Tool.convertDpToPixel(CONTAINER_HEIGHT, ctx)));
                containerLL.setPadding(DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS);
            }
            return containerLL;
        }

        private void resetSelection(View v) {
            for (int i = 1; i < navigationLL.getChildCount(); i++)
                navigationLL.getChildAt(i).setAlpha(0.4f);
            v.setAlpha(1);
        }

        private ImageView createIv(final String id, int side, Context ctx, final OnStickerClickListener listener, View.OnClickListener clickListener) {
            ImageView iv = new ImageView(ctx);
            try {
                iv.setLayoutParams(new LinearLayout.LayoutParams(side, side));
                iv.setImageResource(getResId(id));
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                iv.setTag(id);
                iv.setPadding(DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS);
                if (clickListener != null) {
                    iv.setOnClickListener(clickListener);
                } else {
                    iv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onStickerClick(id);
                        }
                    });
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
            return iv;
        }

        public ScrollView getEmoFlow(Context ctx, final OnEmoSelectListener oEsl) {
            if (smilesSL == null) {
                smilesSL = new ScrollView(ctx);
                smilesSL.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, (int) Tool.convertDpToPixel(MAX_LAYOUT_HEIGHT, ctx)));
                smilesSL.setPadding(DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS);
                FlowLayout fl = new FlowLayout(ctx);
                fl.setGravity(Gravity.CENTER);
                smilesSL.addView(fl);
                for (final int i : Tool.getEmsInt()) {
                    final String s = getUnicodeChar(i);
                    TextView tv = new TextView(ctx);
                    tv.setText(Tool.parseSmiles(s));
                    Tool.checkEmojiSupport(act, tv);
                    tv.setTextSize(40);
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            oEsl.onSelect(s);
                        }
                    });
                    fl.addView(tv);
                }
            }
            return smilesSL;
        }

        public LinearLayout getVoiceRecorder(final Context ctx, final OnVoiceSendListener listener) {
            return new SoundView().getVoiceRecorder(ctx, listener);
        }

        private ScrollView getFlow(ScrollView layout, String[] src, Context ctx, final OnStickerClickListener listener) {
            if (layout == null) {
                layout = new ScrollView(ctx);
                layout.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, (int) Tool.convertDpToPixel(CONTAINER_HEIGHT, ctx)));
                layout.setPadding(DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS);
                FlowLayout fl = new FlowLayout(ctx);
                fl.setGravity(Gravity.CENTER);
                layout.addView(fl);
                int side = (int) Tool.convertDpToPixel(INNER_STICKER_HEIGHT, ctx);
                for (final String str : src) {
                    fl.addView(createIv(str, side, ctx, listener, null));
                }
            }
            return layout;
        }

        private int getResId(String resName) {
            int resId = 0;
            try {
                Class res = R.drawable.class;
                Field field = res.getField(resName);
                resId = field.getInt(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resId;
        }

        private String getUnicodeChar(int codepoint) {
            return new String(Character.toChars(codepoint));
        }

        private void setStickerView(final View view) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getContainerLL(act).removeAllViews();
                    getContainerLL(act).addView(view);
                }
            });
        }

        public View getDivider() {
            View del = new LinearLayout(act);
            del.setBackgroundColor(Color.GRAY);
            del.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            return del;
        }
    }

    private class MsgSendListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            barListener.doSend(msgEt.getText().toString(), onlyForOperators);
            typingLenSent = 0;
            lastMsgMap.put(mChatId, "");
            msgEt.setText("");
        }
    }

    private class DefaultOnFocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
                showKb();
            else
                hideKb();
        }
    }
}
