package mobi.sender.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sender.library.ChatFacade;

import org.json.JSONException;
import org.json.JSONObject;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.tool.SmsReceiver;
import mobi.sender.event.AuthReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.tool.RegRouter;
import mobi.sender.tool.utils.ServerUtils;

public class RegOtpActivity extends BaseActivity {

    private EditText etCode;
    private String phone;
    public final static String WAIT = "wait";
    private final static int REQUEST_CODE_SMS = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_otp);
        phone = getIntent().getStringExtra(RegPhoneActivity.EXTRA_PHONE);
        makeWaitLogic();
        if (phone == null) finish();
        ((TextView) findViewById(R.id.reg_otp_phone)).setText(phone);
        etCode = (EditText) findViewById(R.id.reg_otp_code);
        etCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendForm();
                }
                return false;
            }
        });

        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
                sendForm();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //paste number from sms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SEND_SMS}, REQUEST_CODE_SMS);
            } else{
                getSmsText();
            }
        } else{
            getSmsText();
        }
    }

    private void getSmsText(){
        SmsReceiver.bindListener(new SmsReceiver.SmsListener() {
            @Override
            public void messageReceived(final String messageText) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        etCode.setText(messageText);
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        getSmsText();
                    }
                } else {
                    Toast.makeText(this, R.string.tst_permission_sms_off, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void makeWaitLogic() {
        if (getIntent().hasExtra(WAIT)) {
            int wait = getIntent().getIntExtra(WAIT, 0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.btn_i_dont_recive).setVisibility(View.VISIBLE);
                        }
                    });
                }
            }, wait * 1000);
        }
    }

    private void sendForm() {
        String s = etCode.getText().toString().trim();
        if (s.length() == 4) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.reg_progress).setVisibility(View.VISIBLE);
                }
            });
            Bus.getInstance().post(new AuthReq(ChatFacade.AUTH_ACTION_OTP, s, new SyncEvent.SRespListener() {
                @Override
                public void onResponse(JSONObject data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.reg_progress).setVisibility(View.INVISIBLE);
                        }
                    });
                    if (data.has("error")) {
                        try {
                            ServerUtils.makeErrorMessage(RegOtpActivity.this, data.getString("error"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    RegRouter.route(RegOtpActivity.this, phone, data);
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.reg_progress).setVisibility(View.INVISIBLE);
                        }
                    });
                    e.printStackTrace();
                }
            }));
        }
    }

    @Override
    protected void fromServer(Bus.Event evt) {
    }

    public void notReceiveSMS(View view) {
        findViewById(R.id.reg_progress).setVisibility(View.VISIBLE);
        Bus.getInstance().post(new AuthReq(ChatFacade.AUTH_ACTION_BREAK, null, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.reg_progress).setVisibility(View.INVISIBLE);
                    }
                });
                if (data.has("error")) {
                    try {
                        ServerUtils.makeErrorMessage(RegOtpActivity.this, data.getString("error"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                RegRouter.route(RegOtpActivity.this, phone, data);
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.reg_progress).setVisibility(View.INVISIBLE);
                    }
                });
                e.printStackTrace();
            }
        }));
    }
}
