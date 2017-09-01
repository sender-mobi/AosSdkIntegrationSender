package mobi.sender.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.bitcoinj.core.Coin;
import org.json.JSONObject;

import java.io.IOException;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.SetStorageReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.tool.LWallet;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.UiUtils;
import mobi.sender.ui.window.BtcCreatePasswordWindow;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BtcActivity extends BaseActivity {

    private LWallet wallet;
    private Coin balance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btc);
        UiUtils.initToolbar(this, "Bitcoin", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        wallet = LWallet.getInstance(this);
        ((TextView) findViewById(R.id.btc_addr)).setText(wallet.currentReceiveAddress().toString());
        findViewById(R.id.btc_progress).setVisibility(View.VISIBLE);
        new OkHttpClient().newCall(new Request.Builder().url("https://blockchain.info/ru/q/addressbalance/" + wallet.getXPub()).get().build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Tool.log("can't get balance for BTC address");
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.btc_progress).setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    balance = Coin.valueOf(Long.parseLong(response.body().string()));
                    new OkHttpClient().newCall(new Request.Builder().url("https://blockchain.info/ru/frombtc?currency=USD&value=" + balance.getValue()).get().build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Tool.log("can't convert balance");
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    findViewById(R.id.btc_progress).setVisibility(View.INVISIBLE);
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final String eqv = response.body().string();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) findViewById(R.id.btc_eqv_amt)).setText(eqv);
                                    findViewById(R.id.btc_progress).setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.btc_amt)).setText(balance == null ? "error" : balance.toPlainString());
                        findViewById(R.id.btc_progress).setVisibility(View.INVISIBLE);
                    }
                });

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    protected void fromServer(Bus.Event evt) {
    }

    public void doExport(View view) {
        View v = LayoutInflater.from(this).inflate(R.layout.btc_export_key_window, null);
        TextView tv = (TextView) v.findViewById(R.id.btc_ex_text);
        ImageView iv = (ImageView) v.findViewById(R.id.btc_ex_qr);
        try {
            LWallet wallet = LWallet.getInstance(this);
            if (wallet == null) return;
            String val = wallet.exportSeed();
            tv.setText(val);
            Bitmap qrBitmap = Tool.stringToQrCodeBitmap(val, BarcodeFormat.QR_CODE, 400);
            if (qrBitmap != null)
                iv.setImageBitmap(qrBitmap);
            new AlertDialog.Builder(this).setView(v).create().show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.tst_error, Toast.LENGTH_LONG).show();
        }
    }

    public void doImport(View view) {
        new IntentIntegrator(BtcActivity.this).initiateScan();
    }

    public void doCreateNew(View view) {
        Toast.makeText(this, R.string.tst_coming_soon, Toast.LENGTH_LONG).show();
    }

    public void doChangePass(View view) {
        new BtcCreatePasswordWindow(BtcActivity.this, new BtcCreatePasswordWindow.WindowCallback() {
            @Override
            public void onCreatePassword(String password) {
                try {
                    String encrypted = LWallet.encryptPass(wallet.getMnemonic(), password);
                    Bus.getInstance().post(new SetStorageReq(encrypted, new SyncEvent.SRespListener() {
                        @Override
                        public void onResponse(JSONObject data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BtcActivity.this, R.string.tst_success, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BtcActivity.this, R.string.tst_error, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }));
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BtcActivity.this, R.string.tst_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancel() {
                Tool.log("cancelled!");
            }
        }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            new BtcCreatePasswordWindow(BtcActivity.this, new BtcCreatePasswordWindow.WindowCallback() {
                @Override
                public void onCreatePassword(String password) {
                    try {
                        wallet.regenerate(result.getContents(), BtcActivity.this);
                        String encrypted = LWallet.encryptPass(wallet.getMnemonic(), password);
                        Bus.getInstance().post(new SetStorageReq(encrypted, new SyncEvent.SRespListener() {
                            @Override
                            public void onResponse(JSONObject data) {
                                // TODO:
                            }

                            @Override
                            public void onError(Exception e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BtcActivity.this, R.string.tst_error, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(BtcActivity.this, R.string.tst_error, Toast.LENGTH_LONG).show();
                    } finally {
                        refresh();
                    }
                }

                @Override
                public void onCancel() {
                    Tool.log("cancelled!");
                }
            }).show();
        }
    }
}
