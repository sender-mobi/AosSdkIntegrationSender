package mobi.sender.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import mobi.sender.R;
import mobi.sender.model.Country;
import mobi.sender.tool.ItemOffsetDecoration;
import mobi.sender.tool.utils.UiUtils;
import mobi.sender.ui.adapter.CountryAdapter;
import mobi.sender.ui.adapter.CountryAdapter2;

/**
 * Created by Zver on 02.11.2016.
 */

public class CountryActivity extends AppCompatActivity implements CountryAdapter2.CountryClickListener{

    private ArrayList<Country> countries;
    private CountryAdapter2 adapter;
    public final static String NAME = "name";
    public final static String CODE = "code";
    public final static String PREFIX = "prefix";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country);
        UiUtils.initToolbar(this, getString(R.string.src_search), true);

        //get intent from extra
        countries = this.getIntent().getParcelableArrayListExtra(RegPhoneActivity.INTENT_COUNTRY);

        //init views
        RecyclerView rvCountry = (RecyclerView) findViewById(R.id.rv_country);

        //init llmanager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvCountry.setLayoutManager(mLayoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(this, R.dimen.country_padding);
        rvCountry.addItemDecoration(itemDecoration);

        //make adapter
        adapter = new CountryAdapter2(this, countries, this);
        rvCountry.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<Country> newCountries = new ArrayList<>();
                for(int i=0; i<countries.size(); i++){
                    if(countries.get(i).getName().toLowerCase().contains(s)){
                        newCountries.add(countries.get(i));
                    }
                }
                adapter.setModels(newCountries);
                return false;
            }
        });

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCountryClickListener(Country country) {
        Intent intent = new Intent();
        intent.putExtra(NAME, country.getName());
        intent.putExtra(CODE, country.getCode());
        intent.putExtra(PREFIX, country.getPrefix());
        setResult(RegPhoneActivity.REQUEST_CODE, intent);
        finish();
    }
}
