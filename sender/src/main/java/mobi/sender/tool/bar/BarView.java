package mobi.sender.tool.bar;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONObject;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.EditMsgReq;
import mobi.sender.event.SendTextReq;
import mobi.sender.event.SendTypingReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.model.msg.TextMessage;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.KeyboardUtils;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.sendbar.ActionPanel;
import mobi.sender.ui.sendbar.AttachPanel;
import mobi.sender.ui.sendbar.BasePanel;
import mobi.sender.ui.sendbar.SmilePanel;
import mobi.sender.ui.sendbar.SoundPanel;


public class BarView extends BasePanel implements View.OnClickListener {

    private EditText etInput;
    private ImageView ivPlus;
    private ImageView ivAttach;
    private ImageView ivSmile;
    public ImageView ivSend;
    private RelativeLayout rl;
    public PopupWindow popupWindow;

    private static final String KEYBOARD = "keyboard";
    private static final String ATTACH = "attach";
    private static final String SMILE = "smile";
    public static final String SEND = "send";
    public static final String RECORD = "record";
    //panels
    private static final String LAY_ACTION_PANEL = "action_panel";
    private static final String LAY_ATTACH_PANEL = "attach_panel";
    private static final String LAY_SMILE_PANEL = "smile_panel";
    private static final String LAY_SOUND_PANEL = "sound_panel";

    private int lenTypingSent = 0;
    private int previousHeightDiffrence = 0;
    private long mPackedId = -1;
    private String mChatId;
    private int keyboardHeight;
    public boolean keyBoardVisible;
    private LinearLayout emoticonsCover;

    private ActionPanel actionPanel;
    private SmilePanel smilePanel;
    private AttachPanel attachPanel;
    private SoundPanel soundPanel;
    private View parentLayout;
    private BarListener mListener;

    public BarView(ChatActivity parent, String chatId, BarListener listener) {
        super(parent);
        mChatId = chatId;
        mListener = listener;
    }

    @Override
    public void attachTo(ViewGroup viewGroup) {
        View v = inflater.inflate(R.layout.new_bar, viewGroup);
        //init views
        etInput = (EditText) v.findViewById(R.id.chat_bar_edit);
        Tool.checkEmojiSupport(parent, etInput);
        ivPlus = (ImageView) v.findViewById(R.id.chat_bar_plus);
        ivAttach = (ImageView) v.findViewById(R.id.chat_bar_attach);
        ivSmile = (ImageView) v.findViewById(R.id.chat_bar_smile);
        ivSend = (ImageView) v.findViewById(R.id.chat_bar_last);

        View popUpView = parent.getLayoutInflater().inflate(R.layout.chat_plus, null);
        rl = (RelativeLayout) popUpView.findViewById(R.id.lay_root);

        emoticonsCover = (LinearLayout) v.findViewById(R.id.footer_for_emoticons);
        parentLayout = parent.findViewById(android.R.id.content);

        ivAttach.setTag(ATTACH);
        ivSmile.setTag(SMILE);
        ivSend.setTag(RECORD);

        //init listeners
        ivPlus.setOnClickListener(this);
        ivAttach.setOnClickListener(this);
        ivSmile.setOnClickListener(this);
        ivSend.setOnClickListener(this);
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
                if (text.length() > 0 && Math.abs(text.length() - lenTypingSent) > 3) {
                    lenTypingSent = text.length();
                    Bus.getInstance().post(new SendTypingReq(mChatId));
                }
                if (text.toString().trim().length() > 0) {
                    setTagAndView(ivSend, SEND, R.drawable.ic_send);
                } else if (text.toString().trim().length() == 0) {
                    setTagAndView(ivSend, RECORD, R.drawable.ic_mic);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Defining all components of emoticons keyboard
        popupWindow = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight, false);
        checkKeyboardHeight(parentLayout);

        if (Storage.getInstance(parent).getKeyboardHeight() == 0) {
            etInput.requestFocus();

            // Defining default height of keyboard which is equal to 230 dip
            float defaultHeigthPanel = parent.getResources().getDimension(R.dimen.keyboard_height);
            changeKeyboardHeight((int) defaultHeigthPanel);
        } else {
            changeKeyboardHeight(Storage.getInstance(parent).getKeyboardHeight());
        }

        etInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    etInput.requestFocus();
                    KeyboardUtils.visibleKeyboard(true, etInput, parent);
                    if (popupWindow.isShowing()) {
                        closePanel();
                    }
                }
                return false;
            }
        });
    }

    public void closePanel() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
            ivPlus.setRotation(0);
            if (etInput.getText().toString().trim().equals("")) {
                setTagAndView(ivSend, RECORD, R.drawable.ic_mic);
            } else {
                setTagAndView(ivSend, SEND, R.drawable.ic_send);
            }
            setTagAndView(ivAttach, ATTACH, R.drawable.ic_attachment);
            setTagAndView(ivSmile, SMILE, R.drawable.ic_smile);
            rl.setTag("");
            recyclerScrollToPosition();
        }
    }

    private void recyclerScrollToPosition() {
        mListener.OnRecyclerScrollToPosition(0);
    }

    public void setTagAndView(ImageView v, String t, int r) {
        v.setTag(t);
        v.setImageResource(r);
    }

    private ActionPanel getActionPanel() {
        if (actionPanel == null) actionPanel = new ActionPanel(parent);
        return actionPanel;
    }

    private SmilePanel getSmilePanel() {
        if (smilePanel == null) smilePanel = new SmilePanel(parent);
        return smilePanel;
    }

    public AttachPanel getAttachPanel() {
        if (attachPanel == null) attachPanel = new AttachPanel(parent, this);
        return attachPanel;
    }

    private SoundPanel getSoundPanel() {
        if (soundPanel == null) soundPanel = new SoundPanel(parent);
        return soundPanel;
    }

    public void setImageToSend(boolean canSend) {
        setTagAndView(ivSend, canSend ? SEND : RECORD, canSend ? R.drawable.ic_send : R.drawable.ic_mic);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.chat_bar_plus) {
            if (v.getRotation() == 45) {
                KeyboardUtils.visibleKeyboard(false, etInput, parent);
                closeOnlyMedia();
                if (rl.getTag() == LAY_SOUND_PANEL) ivSend.setImageResource(R.drawable.ic_mic);

            } else if (!popupWindow.isShowing()) {
                popupWindow.setHeight(keyboardHeight);
                emoticonsCover.setVisibility(keyBoardVisible ? LinearLayout.GONE : LinearLayout.VISIBLE);
                popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);

                rl.removeAllViews();
                getActionPanel().attachTo(rl);
                rl.setTag(LAY_ACTION_PANEL);
                v.setRotation(45);
            }

        } else if (i == R.id.chat_bar_attach) {
            if (ivAttach.getTag().equals(KEYBOARD)) {
                etInput.requestFocus();
                KeyboardUtils.visibleKeyboard(true, etInput, parent);
                closeOnlyMedia();

            } else {
                if (!popupWindow.isShowing()) {
                    popupWindow.setHeight(keyboardHeight);
                    emoticonsCover.setVisibility(keyBoardVisible ? LinearLayout.GONE : LinearLayout.VISIBLE);
                    popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);

                    rl.removeAllViews();
                    getAttachPanel().attachTo(rl);
                    rl.setTag(LAY_ATTACH_PANEL);

                    ivPlus.setRotation(45);
                    setTagAndView(ivAttach, KEYBOARD, R.drawable.ic_keyboard);
                    setTagAndView(ivSmile, SMILE, R.drawable.ic_smile);

                } else if (popupWindow.isShowing() && rl.getTag() != LAY_ATTACH_PANEL) {
                    rl.removeAllViews();
                    getAttachPanel().attachTo(rl);
                    rl.setTag(LAY_ATTACH_PANEL);

                    ivPlus.setRotation(45);
                    setTagAndView(ivAttach, KEYBOARD, R.drawable.ic_keyboard);
                    setTagAndView(ivSmile, SMILE, R.drawable.ic_smile);
                    setTagAndView(ivSend, RECORD, R.drawable.ic_mic);
                } else {
                    popupWindow.dismiss();
                    ivPlus.setRotation(90);
                    setTagAndView(ivAttach, ATTACH, R.drawable.ic_attachment);
                    setTagAndView(ivSmile, SMILE, R.drawable.ic_smile);
                }
            }

        } else if (i == R.id.chat_bar_smile) {
            if (ivSmile.getTag().equals(KEYBOARD)) {
                etInput.requestFocus();
                KeyboardUtils.visibleKeyboard(true, etInput, parent);
                closeOnlyMedia();

            } else {
                if (!popupWindow.isShowing()) {
                    popupWindow.setHeight(keyboardHeight);
                    emoticonsCover.setVisibility(keyBoardVisible ? LinearLayout.GONE : LinearLayout.VISIBLE);
                    popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);

                    rl.removeAllViews();
                    getSmilePanel().attachTo(rl);
                    rl.setTag(LAY_SMILE_PANEL);

                    ivPlus.setRotation(45);
                    setTagAndView(ivSmile, KEYBOARD, R.drawable.ic_keyboard);
                    setTagAndView(ivAttach, ATTACH, R.drawable.ic_attachment);

                } else if (popupWindow.isShowing() && rl.getTag() != LAY_SMILE_PANEL) {
                    rl.removeAllViews();
                    getSmilePanel().attachTo(rl);
                    rl.setTag(LAY_SMILE_PANEL);

                    ivPlus.setRotation(45);
                    setTagAndView(ivSmile, KEYBOARD, R.drawable.ic_keyboard);
                    setTagAndView(ivAttach, ATTACH, R.drawable.ic_attachment);
                    setTagAndView(ivSend, RECORD, R.drawable.ic_mic);
                } else {
                    popupWindow.dismiss();
                    ivPlus.setRotation(90);
                    setTagAndView(ivSmile, SMILE, R.drawable.ic_smile);
                    setTagAndView(ivAttach, ATTACH, R.drawable.ic_attachment);
                }
            }

        } else if (i == R.id.chat_bar_last) {
            if (ivSend.getTag().equals(KEYBOARD)) {
                etInput.requestFocus();
                KeyboardUtils.visibleKeyboard(true, etInput, parent);
                closeOnlyMedia();

                setTagAndView(ivSend, RECORD, R.drawable.ic_mic);

            } else if (ivSend.getTag().equals(SEND)) {
                if (rl.getTag() == LAY_ATTACH_PANEL) {
                    getAttachPanel().sendSelectedImages();
                } else {
                    mListener.onProgressVisibilityListener(true);
                    String text = etInput.getText().toString().trim();
                    if (mPackedId == -1) {
                        //Посмотри как сделано для другого бара
//                        Bus.getInstance().post(new SendTextReq(mChatId, text, false, new SyncEvent.SRespListener() {
//                            @Override
//                            public void onResponse(JSONObject data) {
//                                mListener.onProgressVisibilityListener(false);
//                            }
//
//                            @Override
//                            public void onError(Exception e) {
//                                e.printStackTrace();
//                                mListener.onProgressVisibilityListener(false);
//                            }
//                        }));

                    } else {
                        Bus.getInstance().post(new EditMsgReq(mChatId, text, mPackedId, new SyncEvent.SRespListener() {
                            @Override
                            public void onResponse(JSONObject data) {
                                mListener.onProgressVisibilityListener(false);
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(parent, "Error", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                                mListener.onProgressVisibilityListener(false);
                            }
                        }));
                        mPackedId = -1;
                    }
                    etInput.setText("");
                }
            } else if (ivSend.getTag().equals(RECORD)) {
                if (!popupWindow.isShowing()) {
                    popupWindow.setHeight(keyboardHeight);
                    emoticonsCover.setVisibility(keyBoardVisible ? LinearLayout.GONE : LinearLayout.VISIBLE);
                    popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);

                    rl.removeAllViews();
                    getSoundPanel().attachTo(rl);
                    rl.setTag(LAY_SOUND_PANEL);

                    ivPlus.setRotation(45);
                    setTagAndView(ivSmile, SMILE, R.drawable.ic_smile);
                    setTagAndView(ivAttach, ATTACH, R.drawable.ic_attachment);
                    setTagAndView(ivSend, KEYBOARD, R.drawable.ic_keyboard);

                } else if (popupWindow.isShowing() && rl.getTag() != LAY_SOUND_PANEL) {
                    rl.removeAllViews();
                    getSoundPanel().attachTo(rl);
                    rl.setTag(LAY_SOUND_PANEL);

                    ivPlus.setRotation(45);
                    setTagAndView(ivSmile, SMILE, R.drawable.ic_smile);
                    setTagAndView(ivAttach, ATTACH, R.drawable.ic_attachment);
                    setTagAndView(ivSend, KEYBOARD, R.drawable.ic_keyboard);
                } else {
                    popupWindow.dismiss();
                    ivPlus.setRotation(90);
                    setTagAndView(ivSmile, SMILE, R.drawable.ic_smile);
                    setTagAndView(ivAttach, ATTACH, R.drawable.ic_attachment);
                    setTagAndView(ivSend, RECORD, R.drawable.ic_mic);
                }
            }
            recyclerScrollToPosition();

        }

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                emoticonsCover.setVisibility(LinearLayout.GONE);
            }
        });
    }

    private void closeOnlyMedia() {
        popupWindow.dismiss();
        ivPlus.setRotation(0);
        setTagAndView(ivAttach, ATTACH, R.drawable.ic_attachment);
        setTagAndView(ivSmile, SMILE, R.drawable.ic_smile);
        rl.setTag("");
    }

    private void checkKeyboardHeight(final View parentLayout) {
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);

                        int screenHeight;
                        if (Build.VERSION.SDK_INT >= 5.0) {
                            screenHeight = calculateScreenHeightForLollipop();
                        } else {
                            screenHeight = parentLayout.getRootView().getHeight();
                        }

                        int heightDifference = screenHeight - (r.bottom - r.top);

                        int resourceId = parent.getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            heightDifference -= parent.getResources().getDimensionPixelSize(resourceId);
                        }

                        if (previousHeightDiffrence - heightDifference > 50) {
                            popupWindow.dismiss();
                        }

                        previousHeightDiffrence = heightDifference;

                        boolean lastVisible = keyBoardVisible;

                        if (heightDifference > 100) {
                            keyBoardVisible = true;
                            changeKeyboardHeight(heightDifference);
                        } else {
                            keyBoardVisible = false;

                            //refresh image in bar when keyboard is closed
                            if (lastVisible) {
                                closePanel();
                            }
                        }
                    }
                });
    }

    public void onEditMessage(final TextMessage tm) {
        etInput.setText(tm.getText());
        etInput.setSelection(etInput.getText().length());
        mPackedId = tm.getPacketId();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private int calculateScreenHeightForLollipop() {
        WindowManager wm = (WindowManager) parent.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    /**
     * change height of emoticons keyboard according to height of actual
     * keyboard
     *
     * @param height minimum height by which we can make sure actual keyboard is
     *               open or not
     */
    private void changeKeyboardHeight(int height) {
        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
            emoticonsCover.setLayoutParams(params);

            if (Storage.getInstance(parent).getKeyboardHeight() < height) {
                Storage.getInstance(parent).saveKeyboardHeight(height);
            }
        }
    }

    public String getInput() {
        return etInput.getText().toString();
    }

    public void addText(String text) {
        etInput.setText(etInput.getText() + text);
        int n = etInput.getText().length();
        etInput.setSelection(n, n);
    }

    public void makeDefaultMessage() {
        etInput.setText(Storage.getInstance(parent).getMessageOnPause(mChatId));
    }

    public boolean isKeyboardVisible(){
        return keyBoardVisible;
    }

    public interface BarListener {
        void onProgressVisibilityListener(boolean visible);
        void OnRecyclerScrollToPosition(int position);
    }
}
