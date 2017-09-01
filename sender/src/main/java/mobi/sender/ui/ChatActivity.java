package mobi.sender.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.SenderHelper;
import mobi.sender.event.ChatHoleReq;
import mobi.sender.event.ChatInfoReq;
import mobi.sender.event.ChatUpdatedEvent;
import mobi.sender.event.EditMsgReq;
import mobi.sender.event.ForceOpenEvent;
import mobi.sender.event.GetHistoryReq;
import mobi.sender.event.MsgUpdatedEvent;
import mobi.sender.event.SendFileReq;
import mobi.sender.event.SendReadReq;
import mobi.sender.event.SendTextReq;
import mobi.sender.event.SendTypingReq;
import mobi.sender.event.SetChatOptionsReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.event.TypingEvent;
import mobi.sender.event.UpdateBarEvent;
import mobi.sender.model.ChatBased;
import mobi.sender.model.Dialog;
import mobi.sender.model.OperatorModel;
import mobi.sender.model.User;
import mobi.sender.model.msg.MsgBased;
import mobi.sender.model.msg.TextMessage;
import mobi.sender.tool.ActionExecutor;
import mobi.sender.tool.ItemOffsetDecoration;
import mobi.sender.tool.Tool;
import mobi.sender.tool.bar.BarView;
import mobi.sender.tool.bar.SendBarRenderer;
import mobi.sender.tool.utils.KeyboardUtils;
import mobi.sender.tool.utils.MediaUtils;
import mobi.sender.tool.utils.UiUtils;
import mobi.sender.ui.adapter.MsgRecAdapter;

public class ChatActivity extends BaseActivity implements
        View.OnClickListener, MsgRecAdapter.GetActivity, MsgRecAdapter.ScrollToBotomListener {

    public static final int MAX_FILE_SIZE = 1024 * 1024 * 100;

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_TEXT = "extra_text";
    public static final String EXTRA_IMAGE = "extra_image";

    public static final int CAPTURE_IMAGE = 100;
    public static final int REQUEST_IMAGE = 101;
    public static final int REQUEST_FILE = 102;
    public static final int REQUEST_CAMERA = 1;

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1000;
    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1001;
    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1002;
    private static final int COUNT_MESS = 50;
    /**
     * Checking keyboard height and keyboard visibility
     */
    private MsgRecAdapter mAdapter;
    public RecyclerView mRecView;
    private ChatBased c;
    public ProgressBar progress;
    private TextView tvTyping;

    private String mChatId = "";
    private boolean mMore = true;
    private boolean mIsLoading = true;
    private boolean mIsGetPhone;
    private Button btnUnblock;
    private View tvNewMsg;
    private int bottomViewPosition = 0;
    private long lastShownMsgTime;
    private ArrayList<MsgBased> mListModel;
    private Button btnRemoved;
    private BarView mBar;

    private final static int NEW_BAR = 1;
    private final static int OLD_BAR = 0;
    private final static int WHAT_BAR_IS_SHOWN = OLD_BAR;

    private int lenTypingSent = 0;
    private SendBarRenderer sendBar;
    public String photoAbsolutePath = "";
    private LinearLayoutManager mLayoutManager;
    private List<String[]> listHole;
    private long mPacketId;
    private long mLastVersion;

    @Override
    protected void onResume() {
        super.onResume();
        if (WHAT_BAR_IS_SHOWN == NEW_BAR) {
            if (getBar().getInput() != null && !getBar().getInput().equals("")) {
                getBar().setTagAndView(getBar().ivSend, BarView.SEND, R.drawable.ic_send);
            }
            //make default message
            getBar().makeDefaultMessage();
        }

        //make block
        if (btnUnblock != null && c != null) {
            ChatBased chat = getStor().getChat(mChatId);
            btnUnblock.setVisibility(chat != null && chat.isBlocked() ? View.VISIBLE : View.GONE);
        }

        //if user delete from dialog
        if (btnRemoved != null) {
            if (!mChatId.equals("") && !Tool.isP2PChat(mChatId) && !getStor().isImInDialog(mChatId)) {
                btnRemoved.setVisibility(View.VISIBLE);
            } else {
                btnRemoved.setVisibility(View.GONE);
            }
        }

        //update toolbar
        if (mChatId != null) {
            UiUtils.initToolbar(this, getStor().getChatName(mChatId), true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init views
        tvTyping = (TextView) findViewById(R.id.chat_typing);
        progress = (ProgressBar) findViewById(R.id.progress);
        tvNewMsg = findViewById(R.id.chat_new_msg);
        tvNewMsg.setVisibility(View.INVISIBLE);
        tvNewMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecView.scrollToPosition(0);
                tvNewMsg.setVisibility(View.INVISIBLE);
                updateBottomViewPosition();
            }
        });

        btnUnblock = (Button) findViewById(R.id.btn_unblock);
        btnRemoved = (Button) findViewById(R.id.btn_you_removed);
        mRecView = (RecyclerView) findViewById(R.id.chat_messages);
        final RecyclerView.ItemAnimator animator = mRecView.getItemAnimator();
        //remove blinking after notifyDataChanged()
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        //..end

        //init listeners...
        mRecView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    if (WHAT_BAR_IS_SHOWN == NEW_BAR) {
                        //if keyboard or panel is open close it when user taps message list
                        if (getBar().isKeyboardVisible()) {
                            KeyboardUtils.visibleKeyboard(false, mRecView, ChatActivity.this);
                        }
                        closePanel();
                    } else {
                        if (sendBar.isOpenTextInput()) {
                            KeyboardUtils.visibleKeyboard(false, mRecView, ChatActivity.this);
                        }
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
        //...end


        //get intent from extra...
        mChatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
        if (mChatId != null) {
            c = getStor().getChat(mChatId);
        }
        if (c == null) {
            Bus.getInstance().post(new ChatInfoReq(mChatId, new SyncEvent.SRespListener() {
                @Override
                public void onResponse(JSONObject data) {
                    c = getStor().getChat(mChatId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onCreateLogic();
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ChatActivity.this, R.string.tst_chat_not_found, Toast.LENGTH_LONG).show();
                    finish();
                }
            }));
        } else {
            onCreateLogic();
        }

        if (getIntent().hasExtra(EXTRA_TEXT)) {
            sendTextMess(getIntent().getStringExtra(EXTRA_TEXT), false);
        } else if (getIntent().hasExtra(EXTRA_IMAGE)) {
            sendFile(Uri.parse(getIntent().getStringExtra(EXTRA_IMAGE)));
        }
        //...end
    }


    private void onCreateLogic() {
        UiUtils.initToolbar(this, c.getName(), true);
        makeBar();

        //get model for adapter
        mListModel = getStor().getMessages(mChatId);

        updateLastShownMsgTime();
        mAdapter = new MsgRecAdapter(this, this, mChatId, mListModel, new MsgRecAdapter.OnAdapterListener() {
            @Override
            public void onEdit(final TextMessage tm) {
                if (WHAT_BAR_IS_SHOWN == NEW_BAR) {
                    getBar().onEditMessage(tm);
                } else {
                    sendBar.beginMsgEdit(tm, new SendBarRenderer.MsgEditListener() {
                        @Override
                        public void onEdited(String text) {
                            Bus.getInstance().post(new EditMsgReq(mChatId, text, tm.getPacketId(), new SyncEvent.SRespListener() {
                                @Override
                                public void onResponse(JSONObject data) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress.setVisibility(View.GONE);
                                        }
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }));
                        }
                    });
                }
            }

            @Override
            public void onUpdatePlayer(int index, int timeLength) {
                mRecView.invalidate();
                View v = mRecView.getLayoutManager().findViewByPosition(index);
                if (v != null) {
                    ImageView imageView = (ImageView) v.findViewById(R.id.msg_audio_play);
                    imageView.setImageResource(R.drawable.ic_play_arrow_black24dp);
                    TextView tvTime = (TextView) v.findViewById(R.id.msg_text);
                    tvTime.setText(DateUtils.formatElapsedTime(timeLength));
                }
            }

            @Override
            public void onBindStarted(int position) {
                listHole = getStor().getChatHole(mChatId);
                long packetId = mListModel.get(position).getPacketId();
                if (mPacketId != packetId) {
                    if (listHole != null) {
                        for (int i = 0; i < listHole.size(); i++) {
                            String[] holeArray = listHole.get(i);
                            if (packetId == Long.parseLong(holeArray[1])) {
//                                Tool.log("--- if packetId = " + packetId + ", Long.parseLong(holeArray[0]) = " + Long.parseLong(holeArray[0]) + ", Long.parseLong(holeArray[1]) = " + Long.parseLong(holeArray[1]));
                                Bus.getInstance().post(new ChatHoleReq(mChatId, packetId, Long.parseLong(holeArray[0]), new SyncEvent.SRespListener() {
                                    @Override
                                    public void onResponse(JSONObject data) {
                                        Tool.log("--- onResponse data = " + data);
                                    }

                                    @Override
                                    public void onError(Exception e) {

                                    }
                                }));
                            }
                        }
                    }
                    mPacketId = packetId;
                }
            }
        });

        if (mListModel.size() != 0) {
//            mLastVersion = mListModel.get(0).getTimeVersion();
            mLastVersion = mAdapter.getTimeLastMess();
        }

        // init llManager for recycler view
        mLayoutManager = new LinearLayoutManager(this) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                if (bottomViewPosition != 0) {
                    if (state.didStructureChange()) {
                        return;
                    }
                    if (tvTyping.getText() != null && tvTyping.getText().length() > 0) {
                        return;
                    }
                    if (tvNewMsg.getVisibility() == View.VISIBLE) {
                        return;
                    }
                }
                super.onLayoutChildren(recycler, state);
            }
        };
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setReverseLayout(true);

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getApplicationContext(), R.dimen.chat_padding);
        mRecView.addItemDecoration(itemDecoration);
        mRecView.setAdapter(mAdapter);
        mRecView.setLayoutManager(mLayoutManager);

        mRecView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                updateBottomViewPosition();
                if (bottomViewPosition == 0) {
                    tvNewMsg.setVisibility(View.INVISIBLE);
                    updateLastShownMsgTime();
                }

                if (dy < 0) {
                    if (mLayoutManager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1) {
                        if (mIsLoading && mMore) {
                            String top = getStor().getFirstPacketId(mChatId) + "";
                            getModel(top, null);
                            mIsLoading = false;
                        }
                    }
                }
            }
        });

        int numItems = mRecView.getAdapter().getItemCount();
        if (numItems <= COUNT_MESS) {
            if (mIsLoading && mMore) {
                String top = getStor().getFirstPacketId(mChatId) + "";
                getModel(top, null);
                mIsLoading = false;
            }
        }

        checkHoles();

        if (mListModel.size() == 0) {
            getModel(null, null);
        }

        btnUnblock.setOnClickListener(this);

        //send read
        long top = getStor().getLastPacketId(mChatId);
        if (!getStor().isLastMsgFromMe(mChatId) && top > 0) {
            Bus.getInstance().post(new SendReadReq(mChatId, top));
        }

        //make phone
        if (c instanceof User) {
            String phone = ((User) c).getPhone();
            mIsGetPhone = phone != null && phone.length() > 3;
        } else {
            mIsGetPhone = false;
        }
        invalidateOptionsMenu();

        //enable encryption
        if (Tool.isP2PChat(mChatId) && !getStor().isChatEncrypted(mChatId)) {
            getStor().setChatEncrypted(mChatId, true);
        }
    }


    private void makeBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout llBar = (LinearLayout) findViewById(R.id.ll_chat_panel);
                llBar.removeAllViews();

                if (WHAT_BAR_IS_SHOWN == NEW_BAR) {
                    getBar().attachTo(llBar);
                } else {
                    try {
                        JSONObject jo = new JSONObject(getStor().getBarByChatId(mChatId));

                        final ActionExecutor actionExecutor = new ActionExecutor(ChatActivity.this, mChatId);
                        sendBar = new SendBarRenderer(ChatActivity.this, jo, new SendBarRenderer.BarListener() {
                            @Override
                            public void doAction(String oper, Map<String, Object> params) {
                                Tool.log("***doAction = " + oper + " , params = " + params);
                                actionExecutor.exec(oper, params, null);
                            }

                            @Override
                            public void doSend(final String msg, final boolean onlyForOperators) {
                                sendTextMess(msg, onlyForOperators);
                            }

                            @Override
                            public void doTyping(String text) {
                                if (text.length() > 0 && Math.abs(text.length() - lenTypingSent) > 3) {
                                    lenTypingSent = text.length();
                                    Bus.getInstance().post(new SendTypingReq(mChatId));
                                }
                            }
                        }, mChatId);
                        llBar.addView(sendBar.init());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendTextMess(final String msg, final boolean onlyForOperators) {
        setProgress(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                TextMessage tm = getStor().saveNewTextMessage(msg, mChatId, onlyForOperators);
                Bus.getInstance().post(new MsgUpdatedEvent(getChatId()));

                Bus.getInstance().post(new SendTextReq(tm.getText(), onlyForOperators,
                        tm.isEncrypted(), tm.getPkey(), tm.getPacketId(), tm.getChatId(), new SyncEvent.SRespListener() {
                    @Override
                    public void onResponse(JSONObject data) {
                        setProgress(false);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                        setProgress(false);
                    }
                }));

            }
        }).start();
    }

    private void setProgress(final boolean visible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });
    }

    private boolean updateLastShownMsgTime() {
        if (mListModel.size() > 0) {
            MsgBased m = mListModel.get(0);
            lastShownMsgTime = m.getCreated();
            return true;
        } else {
            return false;
        }
    }

    private long getLastMsgTime() {
        if (mListModel.size() > 0) {
            MsgBased m = mListModel.get(0);
            return m.getCreated();
        } else {
            return 0;
        }
    }

    private void checkHoles() {
        listHole = getStor().getChatHole(mChatId);
    }

    private void getModel(final String top, final String both) {
        setProgress(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bus.getInstance().post(new GetHistoryReq(mChatId, top, both, new SyncEvent.SRespListener() {
                    @Override
                    public void onResponse(final JSONObject data) {
                        setProgress(false);
                        mMore = data.optBoolean("more");
                    }

                    @Override
                    public void onError(Exception e) {
                        setProgress(false);
                    }
                }));
            }
        }).start();
    }

    @Override
    protected String getCurrChatId() {
        return mChatId;
    }

    public static void doPhoneCall(String phone, Context ctx) {
        if (phone != null && phone.length() > 3) {
            if (!phone.startsWith("+") && !phone.startsWith("0")) phone = "+" + phone;
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phone));
            ctx.startActivity(callIntent);
        } else {
            Toast.makeText(ctx, R.string.tst_call_avalible, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(mIsGetPhone ? R.menu.chat : R.menu.chat_no_phone, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (c instanceof Dialog) {
            menu.removeItem(R.id.action_call);
        }
        if (btnRemoved.getVisibility() == View.VISIBLE) {
            menu.removeItem(R.id.action_info);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_call) {
            doPhoneCall(((User) c).getPhone(), ChatActivity.this);
            return true;
        } else if (i == R.id.action_info) {
            if (c.getChatId() != null && !"".equals(c.getChatId())) {
                if (Tool.isP2PChat(c.getChatId())) {
                    Intent intent = new Intent(this, P2PChatInfoActivity.class);
                    intent.putExtra(EXTRA_CHAT_ID, c.getChatId());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, GroupChatInfoActivity.class);
                    intent.putExtra(EXTRA_CHAT_ID, c.getChatId());
                    startActivity(intent);
                }
            }
            return true;
        } else if (i == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (WHAT_BAR_IS_SHOWN == OLD_BAR) {
            if (sendBar != null && sendBar.getIsBarOpen()) {
                sendBar.minimize();
            }
        }

        if (isTaskRoot()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE && resultCode == RESULT_OK) {
            processCamera();
            closePanel();
            return;
        }
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            sendFile(fullPhotoUri);
            return;
        }
        if (requestCode == REQUEST_FILE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            processFile(uri);
            closePanel();
        }


        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String s = result.getContents();
                // TODO: костыль для vCard
                if (s.startsWith("BEGIN:VCARD")) {
                    for (String l : s.split("\\r?\\n")) {
                        if (l == null || l.trim().length() == 0) continue;
//                        if (l.startsWith("FN:")) name = l.substring("FN:".length());
                        if (l.startsWith("TEL") && l.contains("+")) {
                            s = l.substring(l.indexOf("+"));
                            break;
                        }
                    }
                }
                // ...
                SenderHelper.sendQR(s, new SyncEvent.SRespListener() {
                    @Override
                    public void onResponse(JSONObject data) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
                Toast.makeText(this, R.string.tst_qr_sent, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void processCamera() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(photoAbsolutePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        sendFile(contentUri);
    }

    public void sendFile(Uri uri) {
        ContentResolver cr = getContentResolver();
        String mime = null;
        if (uri != null) {
            mime = cr.getType(uri);
        }
        if (mime == null || "".equals(mime)) {
            String filename = uri.getLastPathSegment();
            if (filename != null) {
                try {
                    String ext = filename.substring(filename.indexOf(".") + 1);
                    mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                } catch (Exception e) {
                    Tool.log("exep e = " + e);
                }
            }
        }

        Bus.getInstance().post(new SendFileReq(getChatId(), "", "", uri.toString(), mime));
        closePanel();
    }

    public void processFile(Uri uri) {
        int size = Tool.getFileSize(this, uri);
        if (size > MAX_FILE_SIZE) {
            Toast.makeText(this, R.string.cht_file_too_large, Toast.LENGTH_LONG).show();
            return;
        }
        sendFile(uri);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, R.string.tst_acces_location_enabled, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, R.string.tst_you_disabled_acces_location, Toast.LENGTH_LONG).show();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, R.string.tst_acces_mic_enabled, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, R.string.tst_disabled_mic, Toast.LENGTH_LONG).show();
                }
                break;
            }
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                doPhoneCall(((User) c).getPhone(), ChatActivity.this);
                break;
            }
            case REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MediaUtils.openCamera(this);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), R.string.tst_permisson_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void closePanel() {
        if (WHAT_BAR_IS_SHOWN == NEW_BAR) getBar().closePanel();
    }

    public String getChatId() {
        return c.getChatId();
    }

    public BarView getBar() {
        if (mBar == null) mBar = new BarView(this, mChatId, new BarView.BarListener() {
            @Override
            public void onProgressVisibilityListener(final boolean visible) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(visible ? View.VISIBLE : View.GONE);
                    }
                });
            }

            @Override
            public void OnRecyclerScrollToPosition(int position) {
                mRecView.scrollToPosition(0);
            }
        });
        return mBar;
    }

    @Override
    protected void fromServer(final Bus.Event event) {
        Tool.log("*** fromServer evt = " + event);
        if (event instanceof ChatUpdatedEvent) {
            ChatUpdatedEvent ev = (ChatUpdatedEvent) event;
            if (mChatId.equals((ev.getChatId()))) {
                c = getStor().getChat(mChatId);
                if (getStor().isImInDialog(mChatId)) {//check if user in dialog
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnRemoved.setVisibility(View.GONE);
                            if (c != null) {
                                UiUtils.initToolbar(ChatActivity.this, c.getName(), true);
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnRemoved.setVisibility(View.VISIBLE);
                        }
                    });
                }

                //refresh toolbar
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (c != null)
                            UiUtils.initToolbar(ChatActivity.this, c.getName(), true);
                    }
                });
            }

        } else if (event instanceof MsgUpdatedEvent) {
            final MsgUpdatedEvent evt = (MsgUpdatedEvent) event;
            if (mChatId.equals(evt.getChatId())) {
                final List<MsgBased> newHistory = getStor().getUpdatedMessages(mChatId, mLastVersion);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progress != null) progress.setVisibility(View.GONE);
                        if (mAdapter != null) {
                            mAdapter.setHistory(newHistory, ChatActivity.this, mLayoutManager.findFirstVisibleItemPosition());
                            if (mAdapter.getTimeLastMess() != 0)
                                mLastVersion = mAdapter.getTimeLastMess();
                        }
                    }
                });
                mIsLoading = true;
            }
        } else if (event instanceof TypingEvent) {
            final TypingEvent te = (TypingEvent) event;
            if (c.getChatId().equals(te.getChatId())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String userName = getStor().getUserName(te.getFrom());
                        tvTyping.setVisibility(View.VISIBLE);

                        if (getStor().getOperUsers(mChatId, te.getFrom()) != null) { // FOR OPERATORS
                            OperatorModel operModel = getStor().getOperUsers(mChatId, te.getFrom());
                            if (tvTyping.getText().toString().equals("")) {
                                tvTyping.setText(operModel.getName() + getString(R.string.msg_typing));
                            }
                        } else {
                            tvTyping.setText(userName + getString(R.string.msg_typing));
                        }

                        /*else {
                            String lastName = tvTyping.getText().toString();
                            tvTyping.setText(userName+", "+lastName);
                        }*/
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((TextView) findViewById(R.id.chat_typing)).setText("");
                                        tvTyping.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }, 2000);
                    }
                });
            }
        } else if (event instanceof ForceOpenEvent) {
            ForceOpenEvent evt = (ForceOpenEvent) event;
            if (!c.getChatId().equals(evt.getChatId())) {
                Intent i = new Intent(this, ChatActivity.class);
                i.putExtra(ChatActivity.EXTRA_CHAT_ID, evt.getChatId());
                startActivity(i);
                finish();
            }
        } else if (event instanceof UpdateBarEvent) {
            UpdateBarEvent ev = (UpdateBarEvent) event;
            if (mChatId.equals((ev.getChatId()))) {
                //refresh bar
                makeBar();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_unblock) {
            ChatBased chat = getStor().getChat(mChatId);
            Bus.getInstance().post(new SetChatOptionsReq(false, chat.isFavorite(), chat.isMute(), mChatId));
            btnUnblock.setVisibility(View.GONE);

        }
    }

    private void updateBottomViewPosition() {
        int cc = mRecView.getLayoutManager().getChildCount();
        if (cc > 0) {
            View v = mRecView.getLayoutManager().getChildAt(0);
            if (v != null) {
                RecyclerView.ViewHolder vl = mRecView.findContainingViewHolder(v);
                if (vl != null) {
                    int p = vl.getAdapterPosition();
                    if (p > -1) {
                        bottomViewPosition = p;
                    }
                }
            }
        }
    }

    /**
     * Overriding onKeyDown for dismissing keyboard on key down
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (WHAT_BAR_IS_SHOWN == NEW_BAR && getBar().popupWindow.isShowing()) {
            closePanel();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (WHAT_BAR_IS_SHOWN == NEW_BAR) {
            getStor().saveMessageOnPause(getBar().getInput(), mChatId);
            closePanel();
        } else {
            if (sendBar != null)
                sendBar.minimize();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) mAdapter = null;
    }

    @Override
    public Activity getAct() {
        return this;
    }

    @Override
    public void scrollToBotom() {
        mRecView.smoothScrollToPosition(0);
    }

    @Override
    public void updateShowUnreadMess() {
        if (WHAT_BAR_IS_SHOWN == NEW_BAR) {
            if ((bottomViewPosition > 0) && !getBar().isKeyboardVisible()) {
                if (lastShownMsgTime < getLastMsgTime())
                    tvNewMsg.setVisibility(View.VISIBLE);
            } else {
                updateLastShownMsgTime();
            }
        } else {
            if ((bottomViewPosition > 0) && !sendBar.isOpenTextInput()) {
                if (lastShownMsgTime < getLastMsgTime()) {
                    tvNewMsg.setVisibility(View.VISIBLE);
                }
            } else {
                updateLastShownMsgTime();
            }
        }
    }
}
