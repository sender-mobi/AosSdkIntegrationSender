package mobi.sender.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONObject;

import java.util.Locale;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.SenderHelper;
import mobi.sender.event.DeleteMyAvatarReq;
import mobi.sender.event.EnableGpsEvent;
import mobi.sender.event.P24onBitcoinClickEvent;
import mobi.sender.event.P24onRegEvent;
import mobi.sender.event.SendFormReq;
import mobi.sender.event.SetMyInfoReq;
import mobi.sender.event.SetlocaleReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.tool.CircleTransform;
import mobi.sender.tool.utils.AnimationUtils;
import mobi.sender.tool.utils.DialogUtils;
import mobi.sender.tool.utils.UiUtils;

public class SettingsActivity extends BaseActivity {

    private static final int PICK_PHOTO_FOR_AVATAR = 100;
    private ImageView ivAvatar;
    private SwitchCompat switchSendStatusRead;
    private SwitchCompat switchSound;
    private SwitchCompat switchNotifications;
    private String name;
    private String description;
    private String mPhotoUri;
    private Spinner spinner;
    private SwitchCompat switchGps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        UiUtils.initToolbar(this, getString(R.string.tlb_settings), true);

        LinearLayout llLang = (LinearLayout) findViewById(R.id.ll_language);

        if (getStor().isEmptyAuthToken()) {
            llLang.setVisibility(View.VISIBLE);
            spinner = (Spinner) findViewById(R.id.lang);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    getResources().getStringArray(R.array.locales));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String[] locales = getResources().getStringArray(R.array.key_locale);
                    String locale = locales[i];
                    if (!locale.equalsIgnoreCase(getStor().getLocale())) {
                        getStor().saveLocale(locale);
                        setLocale(locale);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            setLocaleSpinner();
        } else {
            llLang.setVisibility(View.GONE);
            findViewById(R.id.clear_files).setVisibility(View.GONE);
            findViewById(R.id.disable_this_device).setVisibility(View.GONE);
        }

        ivAvatar = (ImageView) findViewById(R.id.photo);
        makeDefaultValue();
        makeAvatar(mPhotoUri);
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                CharSequence items[] = new CharSequence[]{getString(R.string.dlg_upload_photo), getString(R.string.dlg_delete_photo)};
                DialogUtils.itemsDialog(SettingsActivity.this, R.string.dlg_select_an_action, items, new DialogUtils.OnChooseListener() {
                    @Override
                    public void onSelect(int position) {
                        switch (position) {
                            case 0:
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
                                AnimationUtils.clickAnimation(view);
                                break;
                            case 1:
                                Bus.getInstance().post(new DeleteMyAvatarReq(new SyncEvent.SRespListener() {
                                    @Override
                                    public void onResponse(JSONObject data) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                String myPhoto = getStor().getMyPhoto();
                                                makeAvatar(myPhoto);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                    }
                                }));
                                break;
                        }
                    }
                });
            }
        });

        setupTextValueEditor(name, R.id.user_name, R.string.stg_enter_new_user_name, getString(R.string.stg_new_user_name_error_msg), new OnTextValueChanged() {
            @Override
            public void onNewValue(String newValue) {
                if (newValue.trim().length() != 0) {
                    name = newValue;
                    description = getStor().getMyDesc();
                    mPhotoUri = getStor().getMyPhoto();
                    saveMyProfile();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.tst_name_cant_be_empty, Toast.LENGTH_SHORT).show();
                }
            }
        });
        setupTextValueEditor(description, R.id.status, R.string.stg_enter_new_status, getString(R.string.stg_new_status_error_msg), new OnTextValueChanged() {
            @Override
            public void onNewValue(String newValue) {
                description = newValue;
                name = getStor().getMyName();
                mPhotoUri = getStor().getMyPhoto();
                saveMyProfile();
            }
        });

        switchSendStatusRead = (SwitchCompat) findViewById(R.id.send_status_read);
        switchSendStatusRead.setChecked(getStor().getSendStatusRead());
        switchSendStatusRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getStor().saveSendStatusRead(b);
            }
        });

        switchSound = (SwitchCompat) findViewById(R.id.sound);
        switchSound.setChecked(getStor().isPlaySoundNotifications());
        switchSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getStor().savePlaySoundNotifications(b);
            }
        });

        switchNotifications = (SwitchCompat) findViewById(R.id.notifications);
        switchNotifications.setChecked(getStor().isShowNotifications());
        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getStor().saveShowNotifications(b);
            }
        });

        switchGps = (SwitchCompat) findViewById(R.id.sv_gps);
        switchGps.setChecked(getStor().isGpsEnable());
        switchGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getStor().saveGpsEnable(b);
                Bus.getInstance().post(new EnableGpsEvent());
            }
        });

        setupTextViewOnClickListener(R.id.disable_this_device, R.string.stg_are_you_sure_logout, new OnClickListener() {
            @Override
            public void onClick() {
                SenderHelper.disableDevice(SettingsActivity.this, getStor().isEmptyAuthToken());
            }
        });

        setupTextViewOnClickListener(R.id.disable_other_devices, R.string.stg_are_you_sure_disable_other, new OnClickListener() {
            @Override
            public void onClick() {
                String ci = "user+sender";
                Bus.getInstance().post(new SendFormReq(".devices.sender", ci, null, null));
                Intent i = new Intent(SettingsActivity.this, ChatActivity.class);
                i.putExtra(ChatActivity.EXTRA_CHAT_ID, ci);
                startActivity(i);
                if (getStor().isEmptyAuthToken()) finish();
            }
        });

        setupTextViewOnClickListener(R.id.clear_files, R.string.stg_are_you_sure_clear_cashe, new OnClickListener() {
            @Override
            public void onClick() {
                // TODO: implement
                Toast.makeText(SettingsActivity.this, R.string.tst_coming_soon, Toast.LENGTH_LONG).show();
            }
        });

        setupTextViewOnClickListener(R.id.clear_history, R.string.stg_are_you_sure_clear_history, new OnClickListener() {
            @Override
            public void onClick() {
                getStor().clearHistory();
                if (getStor().isEmptyAuthToken()) {
                    onBackPressed();
                } else {
                    Bus.getInstance().post(new P24onRegEvent());
                    Toast.makeText(SettingsActivity.this, R.string.tst_success, Toast.LENGTH_LONG).show();
                }
            }
        });

        setupTextViewOnClickListener(R.id.reset, R.string.stg_are_you_sure_reset,
                new OnClickListener() {
                    @Override
                    public void onClick() {
                        reset();
                    }
                });

        findViewById(R.id.bitcoin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getStor().isEmptyAuthToken()) {
                    startActivity(new Intent(SettingsActivity.this, BtcActivity.class));
                } else {
                    Bus.getInstance().post(new P24onBitcoinClickEvent());
                }
            }
        });
    }

    private void makeAvatar(String photo) {
        ivAvatar.setImageResource(R.drawable.ic_acc_bg);
        if ((photo != null) && (photo.length() > 0))
            Picasso.with(this).load(photo).transform(new CircleTransform()).into(ivAvatar);
    }

    private void makeDefaultValue() {
        name = getStor().getMyName();
        description = getStor().getMyDesc();
        mPhotoUri = getStor().getMyPhoto();
    }

    @Override
    protected void fromServer(Bus.Event evt) {
    }

    private void setLocaleSpinner() {
        boolean isLocaleFound = false;
        String locale = getStor().getLocale();
        String[] locales = getResources().getStringArray(R.array.key_locale);
        for (int i = 0; i < locales.length; i++) {
            if (locales[i].equalsIgnoreCase(locale)) {
                spinner.setSelection(i);
                isLocaleFound = true;
                break;
            }
        }
        if (!isLocaleFound) {
            spinner.setSelection(0);
        }
    }

    private void setLocale(String locale) {
        getResources().getConfiguration().locale = new Locale(locale);
        getResources().updateConfiguration(getResources().getConfiguration(), getResources().getDisplayMetrics());
        Bus.getInstance().post(new SetlocaleReq(locale));
        recreate();
    }

    private void reset() {
        switchSendStatusRead.setChecked(true);
        switchSound.setChecked(true);
        switchNotifications.setChecked(true);
        switchGps.setChecked(true);
        getStor().saveLocale("");
        if (getStor().isEmptyAuthToken()) {
            setLocaleSpinner();
            setLocale(getStor().getLocale());
        }
    }

    private void setupTextViewOnClickListener(int id, final int message, final OnClickListener listener) {
        TextView textView = (TextView) findViewById(id);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimationUtils.clickAnimation(view);

                DialogUtils.confirmDilog(SettingsActivity.this, 0, message, new DialogUtils.OnConfirmListener() {
                    @Override
                    public void onYesClick() {
                        listener.onClick();
                    }
                });
            }
        });
    }

    private void setupTextValueEditor(final String value,
                                      int textViewId,
                                      final int message,
                                      final String errorMessage,
                                      final OnTextValueChanged listener) {
        final TextView textView = (TextView) findViewById(textViewId);
        textView.setText(value);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimationUtils.clickAnimation(view);
                DialogUtils.inputDialog(SettingsActivity.this, value, message, 0, new DialogUtils.OnDialogListener() {
                    @Override
                    public void onYesClick(String etText) {
                        if ((etText != null)) {
                            textView.setText(etText);
                            listener.onNewValue(etText);
                        } else {
                            Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onNoClick(String etText) {
                    }
                });
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

            mPhotoUri = data.getData().toString();

            CropImage.activity(Uri.parse(mPhotoUri))
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

                name = getStor().getMyName();
                description = getStor().getMyDesc();
                mPhotoUri = resultUri.toString();
                saveMyProfile();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void saveMyProfile() {
        Bus.getInstance().post(new SetMyInfoReq(name, description, mPhotoUri, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                makeDefaultValue();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Picasso.with(SettingsActivity.this).load(mPhotoUri).transform(new CircleTransform()).into(ivAvatar);
                    }
                });
            }

            @Override
            public void onError(Exception e) {

            }
        }));
    }

    private interface OnClickListener {
        void onClick();
    }

    private interface OnTextValueChanged {
        void onNewValue(String newValue);
    }
}
