package mobi.sender.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.json.JSONObject;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.SyncDlgReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.tool.Storage;

public class SyncDlgActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_dlg);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.READ_CONTACTS
//                            , Manifest.permission.WRITE_CONTACTS
                    },
                    PERMISSION_REQUEST_CODE);
        } else {
            doSync();
        }
    }

    @Override
    protected void fromServer(Bus.Event evt) {}

    private void doSync() {
        Bus.getInstance().post(new SyncDlgReq(getStor().isFullVer(), new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                finish();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean ok = true;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    ok = false;
                    break;
                }
            }
            if (!ok) {
                Toast.makeText(this, getString(R.string.tst_permission_not_granted), Toast.LENGTH_LONG).show();
                finish();
            } else {
                doSync();
            }
        }
    }


}
