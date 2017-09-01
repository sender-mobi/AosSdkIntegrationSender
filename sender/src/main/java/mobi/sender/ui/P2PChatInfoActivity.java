package mobi.sender.ui;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.Add2ChatReq;
import mobi.sender.event.ChatUpdatedEvent;
import mobi.sender.event.SetChatOptionsReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.event.UpdateContacеReq;
import mobi.sender.model.User;
import mobi.sender.tool.CircleTransform;
import mobi.sender.tool.utils.ConvertUtils;
import mobi.sender.tool.utils.DialogUtils;
import mobi.sender.ui.window.AddSeveralUserWindow;

public class P2PChatInfoActivity extends BaseActivity implements
        SwitchCompat.OnCheckedChangeListener,
        View.OnClickListener,
        AddSeveralUserWindow.DoneListener {

    private String chatId;
    private User user;
    private SwitchCompat svFavorite;
    private SwitchCompat svBlock;
    private SwitchCompat svMute;
    private TextView tvAddParticipant;
    private TextView tvPellete;
    private TextView tvCompany;
    private TextView tvPhone;
    private LinearLayout llPhone;
    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1002;
    private ImageView ivAvatar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p_chat_info);
        getExtraFromIntent();
        initAppbar();
        initViews();
        setValuesFromStorage();
        setListeners();
    }

    private void getExtraFromIntent() {
        Intent intent = getIntent();
        chatId = intent.getStringExtra(ChatActivity.EXTRA_CHAT_ID);
        user = (User) getStor().getChat(chatId);
    }

    public void initAppbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("");
        }

        ivAvatar = (ImageView) findViewById(R.id.iv_avatar);
        String photoURI = user.getChatPhoto();
        if ((photoURI != null) && (photoURI.length() > 0) || (photoURI != null) && !photoURI.isEmpty()) {
            Picasso.with(this).load(photoURI).transform(new CircleTransform()).into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_acc_bg);
        }

        final TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(user.getName());

        // Create collapsing effect
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll_linear);

        final float SCALE_MIN = 0.35f;
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int offSet) {
                    float collapsedRatio = (float) offSet / appBarLayout.getTotalScrollRange();

                    ivAvatar.setScaleX(1 + (collapsedRatio * SCALE_MIN));
                    ivAvatar.setScaleY(1 + (collapsedRatio * SCALE_MIN));

                    FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    frameLayoutParams.setMargins(-Math.round(ConvertUtils.convertDpToPixel(32, P2PChatInfoActivity.this) * (collapsedRatio)), 0, 0, Math.round(ConvertUtils.convertDpToPixel(12, P2PChatInfoActivity.this) * (1 + collapsedRatio)));
                    linearLayout.setLayoutParams(frameLayoutParams);

                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    llp.gravity = Gravity.CENTER_VERTICAL;
                    llp.setMargins(Math.round(ConvertUtils.convertDpToPixel(10, P2PChatInfoActivity.this) * (1 + collapsedRatio)), 0, 0, 0);
                    tvTitle.setGravity(Gravity.CENTER_VERTICAL);
                    tvTitle.setLayoutParams(llp);
                }
            });
        } else {

            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int offSet) {
                    float collapsedRatio = (float) offSet / appBarLayout.getTotalScrollRange();

                    ivAvatar.setScaleX(1 + (collapsedRatio * SCALE_MIN));
                    ivAvatar.setScaleY(1 + (collapsedRatio * SCALE_MIN));

                    FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    frameLayoutParams.setMargins(60, 0, 0, 0);
                    linearLayout.setLayoutParams(frameLayoutParams);
                    linearLayout.requestLayout();

                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    llp.gravity = Gravity.CENTER_VERTICAL;
                    llp.setMargins(10, 0, 0, 0);
                    tvTitle.setGravity(Gravity.CENTER_VERTICAL);
                    tvTitle.setLayoutParams(llp);
                }
            });

            appBarLayout.setExpanded(false);
        }
    }

    private void initViews() {
        svFavorite = (SwitchCompat) findViewById(R.id.sv_favorite);
        svBlock = (SwitchCompat) findViewById(R.id.sv_block);
        svMute = (SwitchCompat) findViewById(R.id.sv_mute);
        tvAddParticipant = (TextView) findViewById(R.id.tv_add_participant);
        tvPellete = (TextView) findViewById(R.id.tv_palette);
        tvCompany = (TextView) findViewById(R.id.tv_company);
        llPhone = (LinearLayout) findViewById(R.id.ll_phone);
        findViewById(R.id.iv_company).setVisibility(user.isCompany() ? View.VISIBLE : View.GONE);
        tvCompany.setVisibility(user.isCompany() ? View.VISIBLE : View.GONE);
        findViewById(R.id.iv_phone).setVisibility(user.getPhone() != null ? View.VISIBLE : View.GONE);
        tvPhone = (TextView) findViewById(R.id.tv_phone);
        tvPhone.setVisibility(user.getPhone() != null ? View.VISIBLE : View.GONE);

        if (!getStor().isEmptyAuthToken() && !getStor().isFullVer()) {
            tvAddParticipant.setVisibility(View.GONE);
            findViewById(R.id.iv_add_participant).setVisibility(View.GONE);
            findViewById(R.id.linee).setVisibility(View.GONE);
        }
    }

    private void setValuesFromStorage() {
        svFavorite.setChecked(user.isFavorite());
        svBlock.setChecked(user.isBlocked());
        svMute.setChecked(user.isMute());

        if (user.getPhone() != null && user.getPhone().length() > 4) {
            tvPhone.setText(user.getPhone());
        } else {
            findViewById(R.id.v_line).setVisibility(View.GONE);
            llPhone.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        svFavorite.setOnCheckedChangeListener(this);
        svBlock.setOnCheckedChangeListener(this);
        svMute.setOnCheckedChangeListener(this);
        tvAddParticipant.setOnClickListener(this);
        tvPellete.setOnClickListener(this);
        tvCompany.setOnClickListener(this);
        llPhone.setOnClickListener(this);

        llPhone.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", user.getPhone());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(P2PChatInfoActivity.this, R.string.tst_phone_copied, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Bus.getInstance().post(new SetChatOptionsReq(svBlock.isChecked(), svFavorite.isChecked(), svMute.isChecked(), chatId));
    }

    /**
     * Close current activity when back pressed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();

        } else if (i == R.id.action_name) {
            DialogUtils.inputDialog(P2PChatInfoActivity.this, user.getName(), R.string.dlg_change_name, R.string.dlg_enter_the_new_name, new DialogUtils.OnDialogListener() {
                @Override
                public void onYesClick(String etText) {
                    Bus.getInstance().post(new UpdateContacеReq(user.getUserId(), etText, user.getChatId()));
                }

                @Override
                public void onNoClick(String etText) {
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.tv_add_participant) {
            new AddSeveralUserWindow(this, getStor().getAddCandidatesP2P(chatId), this).show();

        } else if (i == R.id.tv_palette) {
            Toast.makeText(this, R.string.tst_coming_soon, Toast.LENGTH_LONG).show();

        } else if (i == R.id.tv_company) {
            Intent intent = new Intent(P2PChatInfoActivity.this, CardCompanyActivity.class);
            intent.putExtra(CardCompanyActivity.CHAT_ID, chatId);
            startActivity(intent);
            finish();

        } else if (i == R.id.ll_phone) {
            ChatActivity.doPhoneCall(user.getPhone(), P2PChatInfoActivity.this);
        }
    }

    @Override
    protected void fromServer(final Bus.Event evt) {
        if (evt instanceof ChatUpdatedEvent) {
            final ChatUpdatedEvent event = (ChatUpdatedEvent) evt;
            if (chatId.equals(event.getChatId())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        svFavorite.setChecked(getStor().isFavorite(event.getChatId()));
                        svBlock.setChecked(getStor().isBlock(event.getChatId()));
                        svMute.setChecked(getStor().isMute(event.getChatId()));
                        user = (User) getStor().getChat(chatId);
                        initAppbar();
                    }
                });
            }
        }
    }

    protected String getCurrChatId() {
        return chatId;
    }

    @Override
    public void onDonePressed(List<String> checkedList, final Dialog dialog) {
        JSONArray ja = new JSONArray();
        for (String userId : checkedList) {
            ja.put(userId);
        }

        Bus.getInstance().post(new Add2ChatReq(chatId, ja, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                dialog.dismiss();
                startActivity(new Intent(P2PChatInfoActivity.this, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, data.optString("chatId")));
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!getStor().isCompany(chatId))
            getMenuInflater().inflate(R.menu.chat_info_p2p, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
