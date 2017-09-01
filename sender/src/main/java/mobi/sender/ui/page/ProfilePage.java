package mobi.sender.ui.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.SendFormReq;
import mobi.sender.model.ChatBased;
import mobi.sender.tool.utils.AnimationUtils;
import mobi.sender.tool.CircleTransform;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.QrActivity;
import mobi.sender.ui.SettingsActivity;

/**
 * Created by vd on 7/28/16.
 */
public class ProfilePage extends ChatsPageBased{

    private View view;

    public ProfilePage(final Context ctx) {
        super(ctx);
        final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.profile, null);
        reloadMyInfo();

        Tool.setItemInFragment(ctx, view, R.id.tell_friends, R.drawable.ic_share, R.string.prf_tell_friends);
        Tool.setItemInFragment(ctx, view, R.id.wallet, R.drawable.ic_wallet, R.string.prf_wallet);
        Tool.setItemInFragment(ctx, view, R.id.shop, R.drawable.ic_shopping, R.string.prf_shop);
        Tool.setItemInFragment(ctx, view, R.id.add_company, R.drawable.ic_robot, R.string.prf_add_company);
        Tool.setItemInFragment(ctx, view, R.id.settings, R.drawable.ic_settings, R.string.tlb_settings);

        view.findViewById(R.id.tell_friends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String playStoreLink = "https://play.google.com/store/apps/details?id=" + ProfilePage.this.ctx.getPackageName();
                String shareText = ctx.getString(R.string.prf_install_this_app) + playStoreLink;
                ShareCompat.IntentBuilder.from((Activity) ProfilePage.this.ctx)
                        .setType("text/plain")
                        .setText(shareText)
                        .startChooser();
                AnimationUtils.clickAnimation(view);
            }
        });

        view.findViewById(R.id.wallet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ci = "user+sender";
                Bus.getInstance().post(new SendFormReq(".wallet.sender", ci, null, null));
                Intent i = new Intent(ProfilePage.this.ctx, ChatActivity.class);
                i.putExtra(ChatActivity.EXTRA_CHAT_ID, ci);
                ProfilePage.this.ctx.startActivity(i);
                AnimationUtils.clickAnimation(view);
            }
        });

        view.findViewById(R.id.shop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProfilePage.this.ctx, R.string.tst_coming_soon, Toast.LENGTH_LONG).show();
                AnimationUtils.clickAnimation(view);
            }
        });

        view.findViewById(R.id.add_company).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ci = "user+sender";
                Bus.getInstance().post(new SendFormReq(".createBusiness.sender", ci, null, null));
                Intent i = new Intent(ProfilePage.this.ctx, ChatActivity.class);
                i.putExtra(ChatActivity.EXTRA_CHAT_ID, ci);
                ProfilePage.this.ctx.startActivity(i);
                AnimationUtils.clickAnimation(view);
            }
        });

        view.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfilePage.this.ctx.startActivity(new Intent(ProfilePage.this.ctx, SettingsActivity.class));
                AnimationUtils.clickAnimation(view);
            }
        });
    }

    @Override
    protected List<ChatBased> getChats() {
        return null;
    }

    public View getView() {
        return view;
    }

    public void reloadMyInfo() {
        final Storage storage = Storage.getInstance(ctx);
        ((Activity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView photo = (ImageView) view.findViewById(R.id.imageView);
                photo.setImageResource(R.drawable.ic_acc_bg);
                String photoURI = storage.getMyPhoto();
                if ((photoURI != null) && (photoURI.length() > 0)) {
                    Picasso.with(ctx).load(photoURI).transform(new CircleTransform()).into(photo);
                }
                ((TextView) view.findViewById(R.id.tvUserName)).setText(storage.getMyName());
            }
        });
    }

    @Override
    public String getName(){
        return getClass().getName();
    }

    @Override
    public void getFab(final FloatingActionButton fab,
                       final FloatingActionButton fab1,
                       final FloatingActionButton fab2,
                       final FloatingActionButton fab3) {
        
        closeFab(fab, fab1, fab2, fab3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ctx.startActivity(new Intent(ctx, QrActivity.class));
            }
        });
    }
}
