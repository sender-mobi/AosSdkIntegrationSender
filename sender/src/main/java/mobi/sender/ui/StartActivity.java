package mobi.sender.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;

import mobi.sender.R;
import mobi.sender.SenderHelper;
import mobi.sender.model.StateHolder;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.UiUtils;

public class StartActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    public static final String devKey = "8feb2b3cecdafe9eb619556feffcb7430df2f3a6f20851e92f3299935db50651";
    public static final String devId = "697a0a1a91c2023d3255cfa2b23f6215a53444245237debd16553f5e7f5b8bf7";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int result = api.isGooglePlayServicesAvailable(this);
        Tool.log("isGooglePlayServicesAvailable: " + result);
        if (result != 0) {
//            android.app.Dialog dialog = api.getErrorDialog(this, result, 0);
            Toast.makeText(this, "Problem with GooglePlayServices. Try to update it.", Toast.LENGTH_LONG).show();
//            dialog.show();
            finish();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.PHONE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    PERMISSION_REQUEST_CODE);
        } else {
            next();
        }
    }

    private void next() {
        Storage.getInstance(this).saveAuthValues(devId, devKey, null, null);
        SenderHelper.startService(this);
        if (StateHolder.getInstance(this).isRegistered()) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, RegPhoneActivity.class));
        }
        finish();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                builder.setTitle(getString(R.string.tst_permission_not_granted))
                        .setMessage(R.string.dlg_go_to_settings)
                        .setCancelable(false)
                        .setNegativeButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                        .setPositiveButton(R.string.btn_go, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                UiUtils.startInstalledAppDetailsActivity(StartActivity.this);
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            } else {
                next();
            }
        }
    }
}
