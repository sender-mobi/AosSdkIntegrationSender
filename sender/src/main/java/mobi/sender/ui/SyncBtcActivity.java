package mobi.sender.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import org.bitcoinj.crypto.MnemonicCode;
import org.json.JSONObject;

import java.io.IOException;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.GetStorageReq;
import mobi.sender.event.SetStorageReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.model.StateHolder;
import mobi.sender.tool.LWallet;
import mobi.sender.tool.utils.DialogUtils;
import mobi.sender.tool.utils.UiUtils;

public class SyncBtcActivity extends BaseActivity {

    private String storage;
    protected ScrollView blockVerify, blockCreate;
    private ProgressBar pb;
    private Button btnCont;
    private Button btnCreateNew;
    public final static String NO_INTERNET = "no_internet";

    public static final String OLD_P24_IOS_DEFAULT_BTC_PASS_PREFIX = "+++P24+++";
    public static final String DEFAULT_BTC_PASS_PREFIX = "P24_DEF";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MnemonicCode.INSTANCE == null) try {
//            MnemonicCode.INSTANCE = new MnemonicCode(getResources().openRawResource(R.raw.english), MnemonicCode.BIP39_ENGLISH_SHA256);
            MnemonicCode.INSTANCE = new MnemonicCode(getResources().openRawResource(R.raw.english), MnemonicCode.BIP39_STANDARDISATION_TIME_SECS+"");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //init views
        setContentView(R.layout.activity_sync_btc);
        pb = (ProgressBar) findViewById(R.id.sync_pb);
        pb.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        btnCreateNew = (Button) findViewById(R.id.sync_btc_create_new);
        btnCont = (Button) findViewById(R.id.sync_btc_cont);
        btnCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doRestore();
            }
        });
        btnCreateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doReset();
            }
        });
        blockCreate = (ScrollView) findViewById(R.id.sync_btc_block_create);
        blockVerify = (ScrollView) findViewById(R.id.sync_btc_block_verify);
        //...end

        UiUtils.initToolbar(this, getString(R.string.tlb_password_encryption), false);

        sendStorageReq();
    }

    protected void sendStorageReq() {
        if (blockVerify.getVisibility() == View.GONE && blockCreate.getVisibility() == View.GONE) {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            Bus.getInstance().post(new GetStorageReq(new SyncEvent.SRespListener() {
                @Override
                public void onResponse(JSONObject data) {
                    storage = data.optString("storage");
                    if (NO_INTERNET.equals(storage)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                        return;
                    }
                    if ("unreg".equals(storage)) {
                        onUnregAction();
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                            if (storage == null || storage.length() == 0 || storage.startsWith(OLD_P24_IOS_DEFAULT_BTC_PASS_PREFIX)
                                    || storage.startsWith(DEFAULT_BTC_PASS_PREFIX)) {
                                blockCreate.setVisibility(View.VISIBLE);
                                blockVerify.setVisibility(View.GONE);
                            } else {
                                blockCreate.setVisibility(View.GONE);
                                blockVerify.setVisibility(View.VISIBLE);
                            }

                            //for active edit text
                            if (blockCreate.getVisibility() == View.VISIBLE) {
                                findViewById(R.id.sync_btc_pass_1).requestFocus();
                            }
                            if (blockVerify.getVisibility() == View.VISIBLE) {
                                EditText etPass = (EditText) findViewById(R.id.sync_btc_pass_restore);
                                etPass.requestFocus();
                            }
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                        }
                    });
                    e.printStackTrace();
                }
            }));
        }
    }

    protected void onUnregAction() {
        startActivity(new Intent(SyncBtcActivity.this, RegPhoneActivity.class));
        finish();
    }

    @Override
    protected void fromServer(Bus.Event evt) {
    }

    public void doCreatePass(View view) {
        String p1 = ((EditText) findViewById(R.id.sync_btc_pass_1)).getText().toString();
        String p2 = ((EditText) findViewById(R.id.sync_btc_pass_2)).getText().toString();
        if (p1.length() >= 4 && p1.equals(p2)) {
            try {
                LWallet bt = LWallet.getInstance(SyncBtcActivity.this, true);
                String s = bt.exportSeed();
                bt.regenerate(s, SyncBtcActivity.this);
                String encrypted = LWallet.encryptPass(s, p1);
                findViewById(R.id.progress).setVisibility(View.VISIBLE);
                Bus.getInstance().post(new SetStorageReq(encrypted, new SyncEvent.SRespListener() {
                    @Override
                    public void onResponse(JSONObject data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                            }
                        });
                        setWalletSynced();
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, R.string.tst_pass_incorrect, Toast.LENGTH_LONG).show();
        }
    }

    public void doRestore() {
        pb.setVisibility(View.VISIBLE);

        btnCont.setEnabled(false);
        btnCreateNew.setEnabled(false);
        final String p = ((EditText) findViewById(R.id.sync_btc_pass_restore)).getText().toString();

        new Thread(new Runnable() {
            public void run() {
                try {
                    if (!p.isEmpty()) {
                        String mnemonic = LWallet.decryptPass(storage, p);
                        LWallet bt = LWallet.getInstance(SyncBtcActivity.this, true);
                        bt.regenerate(mnemonic, SyncBtcActivity.this);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setWalletSynced();
                                pb.setVisibility(View.GONE);
                                btnCont.setEnabled(true);
                                btnCreateNew.setEnabled(true);
                                finish();
                            }
                        });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SyncBtcActivity.this, getString(R.string.tst_pass_empty), Toast.LENGTH_LONG).show();
                                pb.setVisibility(View.GONE);
                                btnCont.setEnabled(true);
                                btnCreateNew.setEnabled(true);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            btnCont.setEnabled(true);
                            btnCreateNew.setEnabled(true);
                            Toast.makeText(SyncBtcActivity.this, getString(R.string.tst_pass_incorrect), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();

    }

    protected void setWalletSynced() {
        StateHolder.getInstance(SyncBtcActivity.this).setWalletSynced();
    }

    public void doReset() {
        DialogUtils.confirmDilog(this, R.string.dlg_are_you_sure, R.string.dlg_key_lost, new DialogUtils.OnConfirmListener() {
            @Override
            public void onYesClick() {
                blockCreate.setVisibility(View.VISIBLE);
                blockVerify.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
