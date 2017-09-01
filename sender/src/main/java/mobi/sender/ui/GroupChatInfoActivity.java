package mobi.sender.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.Add2ChatReq;
import mobi.sender.event.ChatUpdatedEvent;
import mobi.sender.event.GetCompanyOperatorsReq;
import mobi.sender.event.LeaveChatReq;
import mobi.sender.event.SetChatOptionsReq;
import mobi.sender.event.SetChatProfileReq;
import mobi.sender.event.SetDialogCryptStateReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.model.Dialog;
import mobi.sender.model.User;
import mobi.sender.tool.CircleTransform;
import mobi.sender.tool.ItemOffsetDecoration;
import mobi.sender.tool.utils.ConvertUtils;
import mobi.sender.tool.utils.DialogUtils;
import mobi.sender.ui.adapter.MembersRecAdapter;
import mobi.sender.ui.window.AddSeveralUserWindow;

public class GroupChatInfoActivity extends BaseActivity implements
        SwitchCompat.OnCheckedChangeListener,
        View.OnClickListener,
        AddSeveralUserWindow.DoneListener {

    private String chatId;
    private Dialog dialog;
    private TextView tvExit;
    private SwitchCompat svFavorite;
    private SwitchCompat svEncrypt;
    private SwitchCompat svBlock;
    private SwitchCompat svMute;
    private TextView tvPellete;
    private List<User> listMembers;
    private TextView tvAddParticipant;
    private static final int PICK_PHOTO_FOR_AVATAR = 1111;
    private ImageView ivAvatar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_info);
        getExtraFromIntent();
        initViews();
        setValuesFromStorage();
        makeRecyclerView();
        setListeners();
        initAppbar();
        ((NestedScrollView) findViewById(R.id.nsv_group)).smoothScrollTo(0,0);
    }

    private void makeRecyclerView() {
        listMembers = getStor().getChatMembers(chatId);
        Map<String, String> listRole = getStor().getChatMembersRole(chatId);

        RecyclerView rvMembers = (RecyclerView) findViewById(R.id.lv_members);

        //init llmanager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvMembers.setLayoutManager(mLayoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.country_padding);
        rvMembers.addItemDecoration(itemDecoration);

        //make adapter
        MembersRecAdapter adapter = new MembersRecAdapter(this, listMembers, chatId, listRole);
        rvMembers.setAdapter(adapter);
    }

    private void getExtraFromIntent() {
        Intent intent = getIntent();
        chatId = intent.getStringExtra(ChatActivity.EXTRA_CHAT_ID);
        dialog = (Dialog) getStor().getChat(chatId);
    }

    private void initViews() {
        svFavorite = (SwitchCompat) findViewById(R.id.sv_favorite);
        svEncrypt = (SwitchCompat) findViewById(R.id.sv_encrypt);
        svBlock = (SwitchCompat) findViewById(R.id.sv_block);
        svMute = (SwitchCompat) findViewById(R.id.sv_mute);
        tvAddParticipant = (TextView) findViewById(R.id.tv_add_participant);
        tvExit = (TextView) findViewById(R.id.tv_leave_chat);
        tvPellete = (TextView) findViewById(R.id.tv_palette);
    }

    private void setValuesFromStorage() {
        svFavorite.setChecked(dialog.isFavorite());
        svEncrypt.setChecked(getStor().isChatEncrypted(chatId));
        svBlock.setChecked(dialog.isBlocked());
        svMute.setChecked(dialog.isMute());

        if (getStor().isOperChat(chatId)) {
            findViewById(R.id.iv_fav).setVisibility(View.GONE);
            svFavorite.setVisibility(View.GONE);
            findViewById(R.id.iv_block).setVisibility(View.GONE);
            svBlock.setVisibility(View.GONE);
            findViewById(R.id.iv_encr).setVisibility(View.GONE);
            svEncrypt.setVisibility(View.GONE);
            findViewById(R.id.iv_mute).setVisibility(View.GONE);
            svMute.setVisibility(View.GONE);
            findViewById(R.id.iv_palette).setVisibility(View.GONE);
            tvPellete.setVisibility(View.GONE);
        }
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
        String url = dialog.getChatPhoto();
        if ((url != null) && (url.length() > 0) || (url != null) && !url.isEmpty()) {
            Picasso.with(this).load(url).transform(new CircleTransform()).into(ivAvatar);
        }

        //title
        final TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(dialog.getName());

        //subtitle
        final TextView tvSubtitle = (TextView) findViewById(R.id.tv_subtitle);
        tvSubtitle.setText(listMembers.size() + getString(R.string.tlb_members));

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
                    frameLayoutParams.setMargins(-Math.round(ConvertUtils.convertDpToPixel(32, GroupChatInfoActivity.this) * (collapsedRatio)), 0, 0, Math.round(ConvertUtils.convertDpToPixel(12, GroupChatInfoActivity.this) * (1 + collapsedRatio)));
                    linearLayout.setLayoutParams(frameLayoutParams);

                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp.gravity = Gravity.CENTER_VERTICAL;
                    llp.setMargins(Math.round(ConvertUtils.convertDpToPixel(10, GroupChatInfoActivity.this) * (1 + collapsedRatio)), 0, 0, 0);
                    tvTitle.setGravity(Gravity.CENTER_VERTICAL);
                    tvTitle.setLayoutParams(llp);

                    tvSubtitle.setGravity(Gravity.CENTER_VERTICAL);
                    tvSubtitle.setLayoutParams(llp);
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Bus.getInstance().post(new SetChatOptionsReq(svBlock.isChecked(), svFavorite.isChecked(), svMute.isChecked(), chatId));
    }

    private void setListeners() {
        svFavorite.setOnCheckedChangeListener(this);
        svBlock.setOnCheckedChangeListener(this);
        svMute.setOnCheckedChangeListener(this);
        tvExit.setOnClickListener(this);

        svEncrypt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Bus.getInstance().post(new SetDialogCryptStateReq(chatId, b));
            }
        });
        tvAddParticipant.setOnClickListener(this);
        tvPellete.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.tv_leave_chat) {
            DialogUtils.confirmDilog(GroupChatInfoActivity.this, 0, R.string.dlg_leave_chat, new DialogUtils.OnConfirmListener() {
                @Override
                public void onYesClick() {
                    Bus.getInstance().post(new LeaveChatReq(chatId));
                    startActivity(new Intent(GroupChatInfoActivity.this, MainActivity.class));
                    finish();
                }
            });

        } else if (i == R.id.tv_add_participant) {
            if (dialog.isOperator()) {
                Bus.getInstance().post(new GetCompanyOperatorsReq(dialog.getCompanyId(), new SyncEvent.SRespListener() {
                    @Override
                    public void onResponse(JSONObject data) {
                        final List<User> list = new ArrayList<>();
                        try {
                            JSONArray arr = data.getJSONArray("operators");
                            List<String> idsMembers = new ArrayList<>();
                            for (User u : listMembers) {
                                idsMembers.add(u.getUserId());
                            }
                            for (int i = 0; i < arr.length(); i++) {
                                User user = new User(arr.getJSONObject(i));
                                if (getStor().getMyUserId().equals(user.getUserId())) continue;
                                if (idsMembers.contains(user.getUserId())) continue;
                                list.add(user);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AddSeveralUserWindow(GroupChatInfoActivity.this, list, GroupChatInfoActivity.this).show();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }));
            } else {
                List<User> list = getStor().getAddCandidates(chatId);
                new AddSeveralUserWindow(this, list, GroupChatInfoActivity.this).show();
            }

        } else if (i == R.id.tv_palette) {
            Toast.makeText(this, R.string.tst_coming_soon, Toast.LENGTH_LONG).show();

        }
    }

    protected String getCurrChatId() {
        return chatId;
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
                        svEncrypt.setChecked(getStor().isChatEncrypted(event.getChatId()));
                        svMute.setChecked(getStor().isMute(event.getChatId()));
                        dialog = (Dialog) getStor().getChat(chatId);
                        initAppbar();
                    }
                });
            }
        }
    }

    @Override
    public void onDonePressed(List<String> checkedList, final android.app.Dialog dialog) {
        JSONArray ja = new JSONArray();
        for (String userId : checkedList) {
            ja.put(userId);
        }

        Bus.getInstance().post(new Add2ChatReq(chatId, ja, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                dialog.dismiss();
                Intent intent = new Intent(GroupChatInfoActivity.this, ChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ChatActivity.EXTRA_CHAT_ID, data.optString("chatId"));
                startActivity(intent);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }));
    }

    /**
     * Close current activity when back pressed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();

        } else if (i == R.id.action_photo) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);

        } else if (i == R.id.action_name) {
            DialogUtils.inputDialog(GroupChatInfoActivity.this, dialog.getName(), R.string.dlg_change_name, R.string.dlg_enter_the_new_name, new DialogUtils.OnDialogListener() {
                @Override
                public void onYesClick(String etText) {
                    if (etText.trim().length() > 0) {
                        Bus.getInstance().post(new SetChatProfileReq(etText, dialog.getChatPhoto(), chatId, null));
                    } else {
                        Toast.makeText(GroupChatInfoActivity.this, getString(R.string.tst_name_cant_be_empty), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNoClick(String etText) {
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.chat_info_group, menu);
//        return super.onCreateOptionsMenu(menu);

        if (!getStor().isOperChat(chatId))
            getMenuInflater().inflate(R.menu.chat_info_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }

            String photoUri = data.getData().toString();

            CropImage.activity(Uri.parse(photoUri))
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .setFixAspectRatio(true)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                Bus.getInstance().post(new SetChatProfileReq(null, resultUri.toString(), chatId, null));

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}
