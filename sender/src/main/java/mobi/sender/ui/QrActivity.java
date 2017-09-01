package mobi.sender.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.SenderHelper;
import mobi.sender.event.ForceOpenEvent;
import mobi.sender.event.SendQrReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.UiUtils;

public class QrActivity extends BaseActivity {

    private TextView tvPhone;
    private ImageView ivQrImage;

    // BEGIN:VCARD\nVERSION:2.1\nN:Pupkin;Vasya\nFN:Vasya Pupkin\nTEL;HOME:+380954182835\nTEL;WORK:+380954182835\nTEL;CELL:+380954182835\nEMAIL:smmarat@gmail.com\nEND:VCARD\n

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        UiUtils.initToolbar(this, getString(R.string.tlb_qr_code), true);
        initViews();
        setValues();
    }

    @Override
    protected void fromServer(final Bus.Event evt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (evt instanceof ForceOpenEvent) {
                    ForceOpenEvent event = (ForceOpenEvent) evt;
                    Intent i = new Intent(QrActivity.this, ChatActivity.class);
                    i.putExtra(ChatActivity.EXTRA_CHAT_ID, event.getChatId());
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    private void initViews() {
        tvPhone = (TextView) findViewById(R.id.tv_phone);
        ivQrImage = (ImageView) findViewById(R.id.iv_qr);
        Button btnScan = (Button) findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new IntentIntegrator(QrActivity.this).initiateScan();
            }
        });
    }

    private void setValues() {
        String phone = getStor().getMyPhone();
        tvPhone.setText(phone);
        try {
            String urlQr = String.format("https://chart.googleapis.com/chart?cht=qr&chs=%sx%s&chl=%s&chld=%s", 400, 400, URLEncoder.encode(phone, "UTF-8"), URLEncoder.encode("L|0", "UTF-8"));
            Tool.loadImage(this, urlQr, ivQrImage, 0, false);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String s = result.getContents();
                SenderHelper.sendQR(s, new SyncEvent.SRespListener() {
                    @Override
                    public void onResponse(JSONObject data) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
            Toast.makeText(this, R.string.tst_qr_sent, Toast.LENGTH_LONG).show();
        }
    }
}
