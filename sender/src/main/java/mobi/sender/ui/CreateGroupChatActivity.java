package mobi.sender.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.doodle.android.chips.ChipsView;
import com.doodle.android.chips.model.Contact;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.CreateGroupChatReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.model.ChatBased;
import mobi.sender.model.User;
import mobi.sender.tool.ItemOffsetDecoration;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.AnimationUtils;
import mobi.sender.tool.utils.UiUtils;
import mobi.sender.ui.adapter.AddSeveralContactRecAdapter;

/**
 * Created by Zver on 15.11.2016.
 */

public class CreateGroupChatActivity extends BaseActivity implements AddSeveralContactRecAdapter.CheckBoxListener {

    private List<ChatBased> mModelList;
    private ChipsView mChipsView;
    private AddSeveralContactRecAdapter mAdapter;
    private static final int PICK_PHOTO_FOR_AVATAR = 100;
    private ImageView mIvAvatar;
    private Uri photoPath;
    private boolean isLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        UiUtils.initToolbar(this, getString(R.string.tlb_new_group_chat), true);

        //Make recycler view...
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_participant);
        mModelList = getStor().getUsers(true);
        mAdapter = new AddSeveralContactRecAdapter(this, mModelList, this);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
        ItemOffsetDecoration decoration = new ItemOffsetDecoration(this, R.dimen.reccler_vertical_padding);
        rv.addItemDecoration(decoration);
        rv.setAdapter(mAdapter);
        //...end

        mChipsView = (ChipsView) findViewById(R.id.et_add_participant);

        // change EditText config
        mChipsView.getEditText().setCursorVisible(true);

        mChipsView.setChipsListener(new ChipsView.ChipsListener() {
            @Override
            public void onChipAdded(ChipsView.Chip chip) {
                for (ChipsView.Chip chipItem : mChipsView.getChips()) {
                    Log.d("ChipList", "chip: " + chipItem.toString());
                }
            }

            @Override
            public void onChipDeleted(ChipsView.Chip chip) {
                String userId = chip.getContact().getLastName();
                mAdapter.setCheckBox(false, userId);
            }

            @Override
            public void onTextChanged(CharSequence text) {
                filterItems(text.toString());
            }
        });

        //make avatar
        mIvAvatar = (ImageView) findViewById(R.id.iv_icon);
        mIvAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
                AnimationUtils.clickAnimation(view);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }

            CropImage.activity(Uri.parse(data.getData().toString()))
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .setFixAspectRatio(true)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                photoPath = result.getUri();

                Tool.loadImage(CreateGroupChatActivity.this, photoPath.toString(), mIvAvatar, R.drawable.ic_create_group_chat, true);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void filterItems(String text) {
        List<ChatBased> newModelList = new ArrayList<>();
        for(ChatBased c : mModelList){
            if (c.getName().toLowerCase().contains(text.toLowerCase())) {
                newModelList.add(c);
            }
        }
        mAdapter.setFilter(newModelList);
    }

    @Override
    protected void fromServer(Bus.Event evt) {
    }

    @Override
    public void checkClick(User user, boolean status) {
        Contact contact = new Contact(user.getName(), user.getUserId(), user.getName(), user.getName(), Uri.EMPTY);
        if (status) {
            mChipsView.addChip(user.getName(), user.getChatPhoto(), contact);
        } else {
            mChipsView.removeChipBy(contact);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_done) {
            if (!isLoading) {
                isLoading = true;
                String name = ((EditText) findViewById(R.id.et_group_name)).getText().toString();
                Bus.getInstance().post(new CreateGroupChatReq(photoPath, name, mAdapter.getListCheck(), new SyncEvent.SRespListener() {
                    @Override
                    public void onResponse(JSONObject data) {
                        startActivity(new Intent(CreateGroupChatActivity.this, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, data.optString("chatId")));
                        finish();
                        isLoading = false;
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                        isLoading = false;
                    }
                }));
            }
            return true;
        } else if (i == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
