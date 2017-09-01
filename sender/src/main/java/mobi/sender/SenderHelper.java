package mobi.sender;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobi.sender.event.ChatUpdatedEvent;
import mobi.sender.event.GetMyInfoReq;
import mobi.sender.event.MsgUpdatedEvent;
import mobi.sender.event.P24DisableDeviceReq;
import mobi.sender.event.P24qrEkEvent;
import mobi.sender.event.SendQrReq;
import mobi.sender.event.SetFullVerReq;
import mobi.sender.event.SetlocaleReq;
import mobi.sender.event.StopReq;
import mobi.sender.event.SyncDlgReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.event.TotalUnreadMessagesEvent;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.ui.StartActivity;
import mobi.sender.ui.adapter.MainPagerAdapter;
import mobi.sender.ui.page.CompChatPage;
import mobi.sender.ui.page.FavChatPage;
import mobi.sender.ui.page.GroupChatPage;
import mobi.sender.ui.page.OperChatPage;
import mobi.sender.ui.page.P2PChatPage;
import mobi.sender.ui.page.ProfilePage;

/**
 * Created by Smmarat on 10.01.17.
 */

public class SenderHelper implements Bus.Subscriber {

    private TabLayout tabLayout;
    private Activity act;
    private FloatingActionButton fab;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;
    private FavChatPage fcp;
    private P2PChatPage pcp;
    private GroupChatPage gcp;
    private CompChatPage ccp;
    private OperChatPage ocp;
    private ProfilePage pp;
    private Storage stor;
    private View view;
    private MainPagerAdapter mpa;
    private List<String> tabName = new ArrayList<>();
    private OnEventListener mListener;
    private static boolean mIsSender;

    /**
     * @param act          - instance activity
     * @param isSender     - is it Sender
     * @param listener     - listener for all event
     * @param developerId  - developer id
     * @param developerKey - developer key
     * @param authToken    - auth token
     * @param companyId    - company id
     */
    public SenderHelper(Activity act,
                        boolean isSender,
                        OnEventListener listener,
                        String developerId,
                        String developerKey,
                        String authToken,
                        String companyId) {
        this.act = act;
        mIsSender = isSender;
        mListener = listener;
        Storage.getInstance(act).saveAuthValues(developerId, developerKey, authToken, companyId);
        stor = Storage.getInstance(act);
    }

    /**
     * @return - return view with main screen
     */
    public View create() {
        if ((!mIsSender && Storage.getInstance(act).isFullVer()) || mIsSender) {
            view = LayoutInflater.from(act).inflate(R.layout.activity_main, null);
        } else {
            view = LayoutInflater.from(act).inflate(R.layout.view_comp, null);
        }
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab1 = (FloatingActionButton) view.findViewById(R.id.fab_1);
        fab2 = (FloatingActionButton) view.findViewById(R.id.fab_2);
        fab3 = (FloatingActionButton) view.findViewById(R.id.fab_3);
        reload();

        return view;
    }

    /**
     * @param ctx - context
     */
    public static void getTotalUnreadMessages(final Context ctx) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int totalCountUnread = 0;
                Storage stor = Storage.getInstance(ctx);

                if (!mIsSender && !stor.isFullVer()) {
                    totalCountUnread = stor.getUnreadCompCount();
                }

                if (stor.getTabFavoriteExist()) {
                    totalCountUnread += stor.getUnreadFavoriteCount();
                }
                if (stor.getTabUsersExist()) {
                    totalCountUnread += stor.getUnreadP2PCount();
                }
                if (stor.getTabGroupExist()) {
                    totalCountUnread += stor.getUnreadGroupCount();
                }
                if (stor.getTabCompExist()) {
                    totalCountUnread += stor.getUnreadCompCount();
                }
                if (stor.getOperChatCount() != 0) {
                    totalCountUnread += stor.getUnreadOperCount();
                }

                Bus.getInstance().post(new TotalUnreadMessagesEvent(totalCountUnread));
            }
        }).start();
    }

    /**
     * Call when need update main screen
     */
    public void update() {

        Storage stor = Storage.getInstance(act);

        if (!mIsSender && !stor.isFullVer()) {
            refresh();
            return;
        }

        boolean tabFavoriteCurrent = stor.getFavoriteCount() != 0;
        boolean tabUsersCurrent = stor.getUsersCount(false) != 0;
        boolean tabCompCurrent = stor.getCompaniesId().size() != 0;
        boolean tabGroupCurrent = stor.getGroupChatsSize() != 0;

        if (stor.getTabFavoriteExist() == tabFavoriteCurrent &&
                stor.getTabCompExist() == tabCompCurrent &&
                stor.getTabGroupExist() == tabGroupCurrent &&
                stor.getTabUsersExist() == tabUsersCurrent) {
            refresh();
        } else {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    reload();
                }
            });
        }
    }

    /**
     * Call when need start service
     */
    public static void startService(Context ctx) {
        if (!Bus.getInstance().isRegistered(Sender.class.getSimpleName())) {
            ctx.startService(new Intent(ctx, Sender.class));
        }
    }

    /**
     * @param ctx       - context
     * @param authToken - auth token
     */
    public static void changeAuthToken(Context ctx, String authToken) {
        Storage.getInstance(ctx).saveAuthToken(authToken);
    }


    /**
     * Call when need disable account
     *
     * @param ctx               - context
     * @param withStartActivity - is open start activity
     */
    public static void disableDevice(Context ctx, boolean withStartActivity) {
        Storage.getInstance(ctx).unavtorize();
        Bus.getInstance().post(new StopReq());
        if (withStartActivity) {
            Intent i = new Intent(ctx, StartActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ctx.startActivity(i);
        }
    }

    public static void disableDeviceP24(final Context ctx, final SyncEvent.SRespListener listener) {
        Bus.getInstance().post(new P24DisableDeviceReq(new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                Storage.getInstance(ctx).unavtorize();
                Bus.getInstance().post(new StopReq());
                listener.onResponse(new JSONObject());
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        }));
    }

    /**
     * @param ctx - context
     * @return is full version
     */
    public static boolean isFullVer(Context ctx) {
        return Storage.getInstance(ctx).isFullVer();
    }

    /**
     * @param ctx      - context
     * @param fullVer  - is full version
     * @param listener - listener for the method
     */
    public static void setFullVer(final Context ctx, final boolean fullVer, final FullVerListener listener) {
        Bus.getInstance().post(new SetFullVerReq(fullVer, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                Storage.getInstance(ctx).setFullVer(fullVer);
                listener.onSuccess();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                listener.onError();
            }
        }));
    }




    public interface FullVerListener {
        void onSuccess();

        void onError();
    }

    /**
     * Called when needed stop service
     */
    public static void stopServiceSender() {
        Bus.getInstance().post(new StopReq());
    }

    /**
     * @param ctx - context
     * @return - is user authorized
     */
    public static boolean isUserAuth(Context ctx) {
        return !"".equals(Storage.getInstance(ctx).getMyPhone());
    }

    /**
     * @param ctx    - context
     * @param locale - en, ru or uk
     */
    public static void changeLanguage(Context ctx, String locale) {
        Storage.getInstance(ctx).saveLocale(locale);
        ctx.getResources().getConfiguration().locale = new Locale(locale);
        ctx.getResources().updateConfiguration(ctx.getResources().getConfiguration(), ctx.getResources().getDisplayMetrics());
        Bus.getInstance().post(new SetlocaleReq(locale));
    }

    /**
     * @param ctx      - context
     * @param listener - the listener of the method
     */
    public static void syncDatabase(final Context ctx, final boolean isFullVersion, final SyncEvent.SRespListener listener) {
        Storage.getInstance(ctx).clearHistory();
        Bus.getInstance().post(new SyncDlgReq(isFullVersion, new SyncEvent.SRespListener() {
            @Override
            public void onResponse(JSONObject data) {
                listener.onResponse(data);
                Storage.getInstance(ctx).setFullVer(isFullVersion);

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                listener.onError(e);
            }
        }));
    }


    /**
     * @param s - param for QR event
     */
    public static void sendQR(String s, SyncEvent.SRespListener listener) {
        if(s.startsWith("EK_")){
            Bus.getInstance().post(new P24qrEkEvent(s));
        }else{
            Bus.getInstance().post(new SendQrReq(s, listener));
        }
    }

    //For Bus...
    @Override
    public void onEvent(Bus.Event evt) {
        mListener.shOnEvent(evt);
        if (evt instanceof MsgUpdatedEvent || evt instanceof ChatUpdatedEvent) {
            update();
        }
    }

    public interface OnEventListener {
        void shOnEvent(Bus.Event evt);
    }
    //...end


    private boolean equalMod(int tabPos) {
        if (!mIsSender) {
            if (Storage.getInstance(act).isFullVer()) {
                return tabPos != 5;
            } else {
                return !(tabPos == 1 || tabPos == 2 || tabPos == 3 || tabPos == 5);
            }
        } else {
            return true;
        }
    }

    private void reload() {
        if (!mIsSender && !Storage.getInstance(act).isFullVer()) {
            ccp = new CompChatPage(act);
            RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.rrr);
            rl.addView(ccp.getView(true));

        } else {
            fcp = new FavChatPage(act);
            pcp = new P2PChatPage(act);
            gcp = new GroupChatPage(act);
            ccp = new CompChatPage(act);
            ocp = new OperChatPage(act);
            pp = new ProfilePage(act);
            Bus.getInstance().post(new GetMyInfoReq(new SyncEvent.SRespListener() {
                @Override
                public void onResponse(JSONObject data) {
                    pp.reloadMyInfo();
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            }));

            if (stor.getFavoriteCount() != 0 && equalMod(0)) {
                fcp.reload();
            }
            if (stor.getUsersCount(false) != 0 && equalMod(1)) {
                pcp.reload();
            }
            if (stor.getGroupChatsSize() != 0 && equalMod(2)) {
                gcp.reload();
            }
            ccp.reload();

            //get data for pager...
            List<Integer> resList = new ArrayList<>();
            TypedArray type = act.getResources().obtainTypedArray(R.array.icons);

            if (stor.getFavoriteCount() != 0 && equalMod(0)) {
                stor.saveTabFavoriteExist(true);
                resList.add(type.getResourceId(0, 1));
            } else {
                stor.saveTabFavoriteExist(false);
            }
            if (stor.getUsersCount(false) != 0 && equalMod(1)) {
                stor.saveTabUsersExist(true);
                resList.add(type.getResourceId(1, 1));
            } else {
                stor.saveTabUsersExist(false);
            }
            if (stor.getGroupChatsSize() != 0 && equalMod(2)) {
                stor.saveTabGroupExist(true);
                resList.add(type.getResourceId(2, 1));
            } else {
                stor.saveTabGroupExist(false);
            }
            resList.add(type.getResourceId(3, 1));
            if (stor.getCompaniesId().size() != 0 && equalMod(4)) {
                stor.saveTabCompExist(true);
                resList.add(type.getResourceId(4, 1));
            } else {
                stor.saveTabCompExist(false);
            }
            if (equalMod(5)) {
                resList.add(type.getResourceId(5, 1));
            }
            type.recycle();
            //...end

            mpa = new MainPagerAdapter(act, resList);
            ViewPager viewPager = (ViewPager) view.findViewById(R.id.vPager);
            if (viewPager != null) {
                viewPager.setAdapter(mpa);
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {
                        fabLogic(position);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
                mpa.clear();
            }
            tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);

            //create page...
            tabName.clear();
            if (stor.getFavoriteCount() != 0 && equalMod(0)) {
                mpa.addPage(fcp.getView(false));
                tabName.add(fcp.getName());
            }
            if (stor.getUsersCount(false) != 0 && equalMod(1)) {
                mpa.addPage(pcp.getView(false));
                tabName.add(pcp.getName());
            }
            if (stor.getGroupChatsSize() != 0 && equalMod(2)) {
                mpa.addPage(gcp.getView(false));
                tabName.add(gcp.getName());
            }
            mpa.addPage(ccp.getView(false));
            tabName.add(ccp.getName());
            if (stor.getCompaniesId().size() > 0 && equalMod(4)) {
                mpa.addPage(ocp.getView());
                tabName.add(ocp.getName());
            }
            if (equalMod(5)) {
                mpa.addPage(pp.getView());
                tabName.add(pp.getName());
            }
            //...end

            if (viewPager != null) {
                viewPager.setCurrentItem(stor.getTabPosition());
                tabLayout.setupWithViewPager(viewPager);
            }
            makeUnreadBadges();
        }

        fabLogic(stor.getTabPosition());
    }

    private void makeUnreadBadges() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (tabLayout != null) {
                    for (int i = 0; i < tabLayout.getTabCount(); i++) {
                        final TabLayout.Tab tab = tabLayout.getTabAt(i);
                        if (tab == null) return;

                        int count = 0;
                        String name = tabName.get(i);
                        if (fcp.getName().equals(name)) {
                            count = stor.getUnreadFavoriteCount();
                        } else if (pcp.getName().equals(name)) {
                            count = stor.getUnreadP2PCount();
                        } else if (gcp.getName().equals(name)) {
                            count = stor.getUnreadGroupCount();
                        } else if (ccp.getName().equals(name)) {
                            count = stor.getUnreadCompCount();
                        } else if (ocp.getName().equals(name)) {
                            count = stor.getUnreadOperCount();
                        } else if (pp.getName().equals(name)) {
                            count = 0;
                        }

                        final int finalI = i;
                        final int finalCount = count;
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tab.setCustomView(null);
                                tab.setCustomView(mpa.getTabView(finalI, finalCount));
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public static void p24getCountUnreadMessages(final Context ctx) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = Storage.getInstance(ctx).p24getCountUnreadMessages();
                Bus.getInstance().post(new TotalUnreadMessagesEvent(count));
            }
        }).start();
    }

    private void fabLogic(int position) {
        if (mIsSender | (!mIsSender && Storage.getInstance(act).isFullVer())) {
            if (tabLayout != null) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                stor.saveTabPosition(position);
                final Animation scale_up = AnimationUtils.loadAnimation(act, R.anim.scale_up);
                final Animation scale_down = AnimationUtils.loadAnimation(act, R.anim.scale_down);
                if (tab != null) {
                    tab.select();
                    fab1.setVisibility(View.INVISIBLE);
                    fab2.setVisibility(View.INVISIBLE);
                    fab3.setVisibility(View.INVISIBLE);

                    fab.startAnimation(scale_down);
                    fab.startAnimation(scale_up);

                    if (position == tabLayout.getTabCount() - 1 && equalMod(5)) {
                        fab.setImageResource(R.drawable.ic_qr);
                    } else {
                        fab.setImageResource(R.drawable.ic_plus_white);
                    }

                    //Fab logic
                    String name = tabName.get(position);
                    if (fcp.getName().equals(name)) {
                        fcp.getFab(fab, fab1, fab2, fab3);
                    } else if (pcp.getName().equals(name)) {
                        pcp.getFab(fab, fab1, fab2, fab3);
                    } else if (gcp.getName().equals(name)) {
                        gcp.getFab(fab, fab1, fab2, fab3);
                    } else if (ccp.getName().equals(name)) {
                        ccp.getFab(fab, fab1, fab2, fab3);
                    } else if (ocp.getName().equals(name)) {
                        ocp.getFab(fab, fab1, fab2, fab3);
                    } else if (pp.getName().equals(name)) {
                        pp.getFab(fab, fab1, fab2, fab3);
                    }
                }
            }
        } else {
            fab.setImageResource(R.drawable.ic_plus_white);
            ccp.getFab(fab, fab1, fab2, fab3);
        }
    }

    private void refresh() {
        Storage stor = Storage.getInstance(act);
        if (mIsSender | (!mIsSender && stor.isFullVer())) {
            if (stor.getFavoriteCount() != 0 && fcp != null) {
                fcp.reload();
            }
            if (stor.getUsersCount(false) != 0 && pcp != null) {
                pcp.reload();
            }
            if (stor.getGroupChatsSize() != 0 && gcp != null) {
                gcp.reload();
            }
            if (stor.getCompaniesId().size() != 0 && ocp != null) {
                ocp.reload();
            }
            makeUnreadBadges();
        }
        ccp.reload();
    }
}
