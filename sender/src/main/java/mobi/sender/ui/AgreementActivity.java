package mobi.sender.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import mobi.sender.R;
import mobi.sender.tool.utils.UiUtils;

public class AgreementActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        UiUtils.initToolbar(this, getResources().getString(R.string.snc_agreement), true);
    }
}
