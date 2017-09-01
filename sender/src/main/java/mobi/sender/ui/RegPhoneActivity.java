package mobi.sender.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sender.library.ChatFacade;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.AuthReq;
import mobi.sender.event.StatusInternetEvent;
import mobi.sender.event.SyncEvent;
import mobi.sender.model.Country;
import mobi.sender.tool.RegRouter;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.KeyboardUtils;
import mobi.sender.tool.utils.MediaUtils;
import mobi.sender.tool.utils.ServerUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegPhoneActivity extends BaseActivity implements Bus.Subscriber {

    public static final String GET_COUNTRY_URL = "https://www.senderapi.com/10/country_list";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_DEVICE = "device";
    private Button btnContinue;
    private ArrayList<Country> countries;
    public final static String INTENT_COUNTRY = "country";
    public final static int REQUEST_CODE = 111;
    private TextView etCountry;
    private TextView tvPref;
    private RelativeLayout rlCountry;
    private EditText etPhone;
    private final static int REQUEST_CODE_PHONE = 100;
    private boolean isNeededOnResume = true;

    @Override
    public void onEvent(final Bus.Event evt) {
        if (evt instanceof StatusInternetEvent) {
            boolean isVisible = ((StatusInternetEvent) evt).isVisible();
            makeSnakBar(isVisible);
            if (isVisible) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onResume();
                    }
                });
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        getDataFromServer();
        initToolbar();
        btnContinue = (Button) findViewById(R.id.btn_continue);
        tvPref = (TextView) findViewById(R.id.reg_c_code);
        etCountry = (TextView) findViewById(R.id.tv_country);
        rlCountry = (RelativeLayout) findViewById(R.id.rl_country);
        rlCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegPhoneActivity.this, CountryActivity.class);
                i.putParcelableArrayListExtra(INTENT_COUNTRY, countries);
                startActivityForResult(i, REQUEST_CODE);
            }
        });

        etPhone = (EditText) findViewById(R.id.reg_phone);
        etPhone.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnContinue.performClick();
                    return true;
                }
                return false;
            }
        });

        etPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                findViewById(R.id.tv_license).setVisibility(hasFocus ? View.GONE : View.VISIBLE);
            }
        });

        KeyboardUtils.isKeyboardShownListener2(findViewById(android.R.id.content), new KeyboardUtils.OnKeyboardShownListener() {
            @Override
            public void onShown(boolean isShown) {
                findViewById(R.id.tv_license).setVisibility(isShown ? View.GONE : View.VISIBLE);
            }
        });

    }

    private void getDataFromServer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.reg_progress).setVisibility(View.VISIBLE);
            }
        });
        try {
            String rb = new JSONObject().put("language", Locale.getDefault().getLanguage()).toString();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), rb);
            Tool.log("===> " + GET_COUNTRY_URL + " " + rb);
            new OkHttpClient().newCall(new Request.Builder().url(GET_COUNTRY_URL).post(body).build()).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Tool.log("<=== fail = " + e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.reg_progress).setVisibility(View.INVISIBLE);
                            Bus.getInstance().post(new StatusInternetEvent(false));
                        }
                    });
                    e.printStackTrace();

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.reg_progress).setVisibility(View.INVISIBLE);
                            rlCountry.setVisibility(View.VISIBLE);
                        }
                    });
                    try {
                        String s = response.body().string();
                        Tool.log("<=== " + s);
                        JSONObject jo = new JSONObject(s);
                        JSONArray arr = jo.optJSONArray("list");
                        countries = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject ijo = arr.getJSONObject(i);
                            Country c = new Country(ijo);
                            countries.add(c);
                        }

                        final Country country = new Country(jo.getString("cName"), jo.getString("prefix"), jo.getString("country"));
                        for (int i = 0; i < countries.size(); i++) {
                            if (countries.get(i).getCode().equals(country.getCode()) && countries.get(i).getName().equals(country.getName())) {
                                countries.remove(i);
                            }
                        }
                        countries.add(0, country);

                        refreshUi(country.getName(), country.getCode(), country.getPrefix());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            isNeededOnResume = false;
            if (data != null && data.hasExtra(CountryActivity.NAME)) {
                refreshUi(data.getStringExtra(CountryActivity.NAME), data.getStringExtra(CountryActivity.CODE), data.getStringExtra(CountryActivity.PREFIX));
            }
        }
    }

    public void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setTitle(R.string.tlb_registration);
        }
    }

    @Override
    protected void fromServer(Bus.Event evt) {
    }

    public void showEULA(View view) {
        startActivity(new Intent(RegPhoneActivity.this, AgreementActivity.class));
    }

    public void regNext(View view) {
        String code = ((TextView) findViewById(R.id.reg_c_code)).getText().toString();
        String phone = ((TextView) findViewById(R.id.reg_phone)).getText().toString();

        if (!phone.startsWith("+897")) phone = code + phone;
        if (phone.length() < 6) return;
        final String finalPhone = phone;
        findViewById(R.id.reg_progress).setVisibility(View.VISIBLE);
        Bus.getInstance().post(new AuthReq(ChatFacade.AUTH_ACTION_PHONE, phone, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.reg_progress).setVisibility(View.INVISIBLE);
                    }
                });
                if (data.has("error")) {
                    ServerUtils.makeErrorMessage(RegPhoneActivity.this, data.optString("error"));
                } else {
                    RegRouter.route(RegPhoneActivity.this, finalPhone, data);
                    //Save my phone to storage
                    getStor().saveMyPhone(finalPhone);
                }
            }

            @Override
            public void onError(final Exception e) {
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

    /**
     * Refresh ui elements
     */
    private void refreshUi(final String name, final String code, final String pref) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etCountry.setText(name);
                Picasso.with(RegPhoneActivity.this).load("https://s.sender.mobi/flag/" + code + ".png").into((ImageView) findViewById(R.id.reg_flag));
                tvPref.setText(pref);

                if (ActivityCompat.checkSelfPermission(RegPhoneActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegPhoneActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_PHONE);
                } else {
                    setPhoneToInput();
                }
                etPhone.setSelection(etPhone.getText().length());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PHONE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        setPhoneToInput();
                    }
                }
                break;
            }
        }
    }

    private void setPhoneToInput() {
        String strPhone = MediaUtils.getPhone(RegPhoneActivity.this, tvPref.getText().toString());
        if (!"".equals(strPhone)) etPhone.setText(strPhone);
    }

    /**
     * Make menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Tool.getDiagonal(RegPhoneActivity.this) < 4) {
            getMenuInflater().inflate(R.menu.done_menu, menu);
            btnContinue.setVisibility(View.GONE);
        }
        return true;
    }

    /**
     * Close current activity when back pressed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_done) {
            regNext(menuItem.getActionView());
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void makeLastRequest() {
        super.makeLastRequest();
        if(isNeededOnResume) getDataFromServer();
        isNeededOnResume = true;
    }
}
