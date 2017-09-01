package mobi.sender.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.model.Country;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.AnimationUtils;
import mobi.sender.tool.utils.UiUtils;
import mobi.sender.ui.adapter.CountryAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddContactActivity extends BaseActivity {

    public static final String GET_COUNTRY_URL = "https://www.senderapi.com/10/country_list";
    private CountryAdapter ca;
    private Button btnContinue;
    private TextView tvRegCode;
    private EditText etRegPhone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        UiUtils.initToolbar(this, getString(R.string.adc_add_user), true);
        btnContinue = (Button) findViewById(R.id.btn_continue);
        tvRegCode = (TextView) findViewById(R.id.reg_c_code);
        etRegPhone = (EditText) findViewById(R.id.reg_phone);

        etRegPhone.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnContinue.performClick();
                    return true;
                }
                return false;
            }
        });

        ca = new CountryAdapter(this);
        Spinner spinner = (Spinner) findViewById(R.id.reg_countries);
        spinner.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorGray), PorterDuff.Mode.SRC_ATOP);
        spinner.setAdapter(ca);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (ca.getCount() > 0) {
                    Country c = (Country) ca.getItem(position);
                    tvRegCode.setText(c.getPrefix());
                    Picasso.with(AddContactActivity.this).load("https://s.sender.mobi/flag/" + c.getCode() + ".png").into((ImageView) findViewById(R.id.reg_flag));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        findViewById(R.id.btn_tell).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String playStoreLink = "https://play.google.com/store/apps/details?id=" + getPackageName();
                String shareText = getString(R.string.prf_install_this_app) + playStoreLink;
                ShareCompat.IntentBuilder.from(AddContactActivity.this)
                        .setType("text/plain")
                        .setText(shareText)
                        .startChooser();
                AnimationUtils.clickAnimation(view);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.reg_progress).setVisibility(View.VISIBLE);
        try {
            String rb = new JSONObject().put("language", Locale.getDefault().getCountry()).toString();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), rb);
            Tool.log("===> " + GET_COUNTRY_URL + " " + rb);
            new OkHttpClient().newCall(new Request.Builder().url(GET_COUNTRY_URL).post(body).build()).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Tool.log("<=== fail");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.reg_progress).setVisibility(View.INVISIBLE);
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
                        }
                    });
                    try {
                        String s = response.body().string();

                        JSONObject jo = new JSONObject(s);
                        JSONArray arr = jo.optJSONArray("list");
                        ArrayList<Country> countries = new ArrayList<Country>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject ijo = arr.getJSONObject(i);
                            Country c = new Country(ijo);
                            countries.add(c);
                        }
                        Country country = new Country(jo.getString("cName"), jo.getString("prefix"), jo.getString("country"));
                        for(int i=0; i<countries.size(); i++){
                            if(countries.get(i).getCode().equals(country.getCode()) && countries.get(i).getName().equals(country.getName())){
                                countries.remove(i);
                            }
                        }
                        countries.add(0, country);
                        ca.setCountries(countries);
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
    protected void fromServer(Bus.Event evt) {
    }

    public void pressContinue(View view) {
        Intent intent = new Intent(AddContactActivity.this, AddContact2Activity.class);
        intent.putExtra(AddContact2Activity.PHONE, tvRegCode.getText() + etRegPhone.getText().toString());
        startActivity(intent);
        finish();
    }

    /**
     * Make menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Tool.getDiagonal(AddContactActivity.this) < 4) {
            getMenuInflater().inflate(R.menu.done_menu, menu);
            btnContinue.setVisibility(View.GONE);
        }
        return true;
    }

    /**
     * Done press logic
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_done) {
            pressContinue(menuItem.getActionView());
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
