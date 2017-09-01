package mobi.sender.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import mobi.sender.R;
import mobi.sender.SenderHelper;
import mobi.sender.tool.Tool;
import mobi.sender.tool.utils.UiUtils;
import mobi.sender.ui.fragment.SharedFragment;

/**
 * Created by mw on 17.03.17.
 */

public class SharedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared);
        SenderHelper.startService(this);
        UiUtils.initToolbar(this, getString(R.string.tlb_new_forward_mess), true);

        PagerAdapter mAdapter = new PagerAdapter(getSupportFragmentManager());

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    mAdapter.setSharedText(sharedText);
                }
            } else if (type.startsWith("image/")) {
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    mAdapter.setSharedUri(imageUri.toString());
                }
            }
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(mAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        private String sharedText = "";
        private String sharedUri = "";

        public String getSharedText() {
            return sharedText;
        }

        public void setSharedText(String sharedText) {
            this.sharedText = sharedText;
        }

        public String getSharedUri() {
            return sharedUri;
        }

        public void setSharedUri(String sharedUri) {
            this.sharedUri = sharedUri;
        }

        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new SharedFragment();
            Bundle args = new Bundle();
            args.putInt(SharedFragment.ARG_OBJECT, i);
            args.putString(ChatActivity.EXTRA_TEXT, getSharedText());
            args.putString(ChatActivity.EXTRA_IMAGE, getSharedUri());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getResources().getString(R.string.shr_contacts);
            } else {
                return getResources().getString(R.string.shr_groups);
            }
        }
    }
}
