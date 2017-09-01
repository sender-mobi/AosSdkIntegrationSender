package taras.com.ua.testsender;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import mobi.sender.Bus;
import mobi.sender.SenderHelper;
import mobi.sender.model.StateHolder;
import mobi.sender.tool.Storage;

public class MainActivity extends AppCompatActivity {

    public static final String developerId = "YOUR_DEV_ID";
    public static final String developerKey = "YOUR_DEV_KEY";
    public static String companyId = "YOUR_COMPANY_ID";
    public static String authToken = "YOUR_AUTH_TOKEN";

    private int REQUEST_CODE_PHONE = 111;
    private Button btn;
    private TextView tvUnreadMess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        btn = (Button) findViewById(R.id.btn_start);
        tvUnreadMess = (TextView) findViewById(R.id.tv_unread);

        Storage.getInstance(this).saveAuthToken(authToken);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (StateHolder.getInstance(this).isRegistered()) {
            btn.setText("Go to chat");
        } else {
            btn.setText("Make sync");
        }
    }

    public void goToSender(View v) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_PHONE);
        } else {
            logic();
        }
    }

    private void logic() {
        if (StateHolder.getInstance(this).isRegistered()) {
            startActivity(new Intent(this, mobi.sender.ui.MainActivity.class));
        } else {
            new SenderHelper(this, false, new SenderHelper.OnEventListener() {
                @Override
                public void shOnEvent(Bus.Event evt) {
                }
            }, developerId, developerKey, authToken, companyId);
        }
        SenderHelper.startService(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PHONE) {
            logic();
        }
    }
}
