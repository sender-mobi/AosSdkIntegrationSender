package mobi.sender.ui.sendbar;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.SendStickerReq;
import mobi.sender.tool.Tool;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.sendbar.emoji.EmotionsPanel;
import mobi.sender.ui.sendbar.emoji.SimpleEmojiPanel;

/**
 * Created by vd on 8/15/16.
 */
public class SmilePanel extends BasePanel {

    private final SmilesPagerAdapter mpa;
    private final View.OnClickListener listener;
    private TabLayout tabLayout;
    private ViewPager pager;

    public SmilePanel(final ChatActivity parent) {
        super(parent);
        mpa = new SmilesPagerAdapter();

        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bus.getInstance().post(new SendStickerReq(view.getTag().toString(), parent.getChatId()));
                parent.closePanel();
            }
        };
    }

    @Override
    public void attachTo(ViewGroup parent) {
        View view = inflater.inflate(R.layout.smile_panel, parent);
        pager = (ViewPager) view.findViewById(R.id.vPager);
        pager.setAdapter(mpa);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null) {
                    correctTabAlpha(position);
                    tab.select();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        pager.setCurrentItem(0);

        tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#ffffff"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.smile).setTag(0));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.dog).setTag(1));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.plaat).setTag(2));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.signals).setTag(3));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.yo).setTag(4));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.club).setTag(5));
        correctTabAlpha(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Object t = tab.getTag();
                if (t != null) {
                    int it = (int) t;
                    correctTabAlpha(it);
                    pager.setCurrentItem(it);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }


    private void correctTabAlpha(int position) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (i == position) tab.getIcon().setAlpha(255);
            else tab.getIcon().setAlpha(128);
        }
    }

    private class SmilesPagerAdapter extends PagerAdapter {

        int[] dogIds = {R.drawable.dog_happy, R.drawable.dog_furious, R.drawable.dog_love, R.drawable.dog_pleased,
                R.drawable.dog_sad, R.drawable.dog_sick, R.drawable.dog_smile, R.drawable.dog_surprise,
                R.drawable.dog_tongue, R.drawable.dog_wink, R.drawable.dog_active, R.drawable.dog_amorous,
                R.drawable.dog_calm, R.drawable.dog_disappoint, R.drawable.dog_evil, R.drawable.dog_festive,
                R.drawable.dog_greeting, R.drawable.dog_joyful, R.drawable.dog_malicious, R.drawable.dog_sore,
                R.drawable.dog_miss, R.drawable.dog_brain, R.drawable.dog_introduce, R.drawable.dog_sing};

        String[] dogNmes = {"dog_happy", "dog_furious", "dog_love", "dog_pleased",
                "dog_sad", "dog_sick", "dog_smile", "dog_surprise",
                "dog_tongue", "dog_wink", "dog_active", "dog_amorous",
                "dog_calm", "dog_disappoint", "dog_evil", "dog_festive",
                "dog_greeting", "dog_joyful", "dog_malicious", "dog_sore",
                "dog_miss", "dog_brain", "dog_introduce", "dog_sing"};

        int[] plaatIds = {R.drawable.plaat_1, R.drawable.plaat_2, R.drawable.plaat_3, R.drawable.plaat_4,
                R.drawable.plaat_5, R.drawable.plaat_6, R.drawable.plaat_7, R.drawable.plaat_8, R.drawable.plaat_9,
                R.drawable.plaat_10};

        String[] plaatNames = {"plaat_1", "plaat_2", "plaat_3", "plaat_4",
                "plaat_5", "plaat_6", "plaat_7", "plaat_8", "plaat_9",
                "plaat_10"};

        // TODO: add images
        int[] signalIds = {R.drawable.bingo_a, R.drawable.cool_a, R.drawable.fail_a, R.drawable.hurray_a,
                R.drawable.lol_a, R.drawable.omg_a, R.drawable.thanks_a, R.drawable.win_a, R.drawable.wtf_a};

        String[] signalNames = {"bingo_a", "cool_a", "fail_a", "hurray_a", "lol_a", "omg_a", "thanks_a",
                "win_a", "wtf_a"};

        // TODO: add images
        int[] yoIds = {R.drawable.bingo_b, R.drawable.cool_b, R.drawable.fail_b, R.drawable.hurray_b,
                R.drawable.lol_b, R.drawable.omg_b, R.drawable.thanks_b, R.drawable.win_b, R.drawable.wtf_b};

        String[] yoNames = {"bingo_b", "cool_b", "fail_b", "hurray_b", "lol_b", "omg_b", "thanks_b",
                "win_b", "wtf_b"};

        int[] clubIds = {R.drawable.ic_card_a_clubs, R.drawable.ic_card_a_hearts, R.drawable.ic_card_a_spades, R.drawable.ic_card_a_diamonds,
                R.drawable.ic_card_2_clubs, R.drawable.ic_card_2_hearts, R.drawable.ic_card_2_spades, R.drawable.ic_card_2_diamonds,
                R.drawable.ic_card_3_clubs, R.drawable.ic_card_3_hearts, R.drawable.ic_card_3_spades, R.drawable.ic_card_3_diamonds,
                R.drawable.ic_card_4_clubs, R.drawable.ic_card_4_hearts, R.drawable.ic_card_4_spades, R.drawable.ic_card_4_diamonds,
                R.drawable.ic_card_5_clubs, R.drawable.ic_card_5_hearts, R.drawable.ic_card_5_spades, R.drawable.ic_card_5_diamonds,
                R.drawable.ic_card_6_clubs, R.drawable.ic_card_6_hearts, R.drawable.ic_card_6_spades, R.drawable.ic_card_6_diamonds,
                R.drawable.ic_card_7_clubs, R.drawable.ic_card_7_hearts, R.drawable.ic_card_7_spades, R.drawable.ic_card_7_diamonds,
                R.drawable.ic_card_8_clubs, R.drawable.ic_card_8_hearts, R.drawable.ic_card_8_spades, R.drawable.ic_card_8_diamonds,
                R.drawable.ic_card_9_clubs, R.drawable.ic_card_9_hearts, R.drawable.ic_card_9_spades, R.drawable.ic_card_9_diamonds,
                R.drawable.ic_card_10_clubs, R.drawable.ic_card_10_hearts, R.drawable.ic_card_10_spades, R.drawable.ic_card_10_diamonds,
                R.drawable.ic_card_j_clubs, R.drawable.ic_card_j_hearts, R.drawable.ic_card_j_spades, R.drawable.ic_card_j_diamonds,
                R.drawable.ic_card_q_clubs, R.drawable.ic_card_q_hearts, R.drawable.ic_card_q_spades, R.drawable.ic_card_q_diamonds,
                R.drawable.ic_card_k_clubs, R.drawable.ic_card_k_hearts, R.drawable.ic_card_k_spades, R.drawable.ic_card_k_diamonds,
                R.drawable.ic_card_jok_black, R.drawable.ic_card_jok_red};

        String[] clubNames = {"card_ace_of_clubs", "card_ace_of_hearts", "card_ace_of_spades", "card_ace_of_diamonds",
                "card_2_of_clubs", "card_2_of_hearts", "card_2_of_spades", "card_2_of_diamonds",
                "card_3_of_clubs", "card_3_of_hearts", "card_3_of_spades", "card_3_of_diamonds",
                "card_4_of_clubs", "card_4_of_hearts", "card_4_of_spades", "card_4_of_diamonds",
                "card_5_of_clubs", "card_5_of_hearts", "card_5_of_spades", "card_5_of_diamonds",
                "card_6_of_clubs", "card_6_of_hearts", "card_6_of_spades", "card_6_of_diamonds",
                "card_7_of_clubs", "card_7_of_hearts", "card_7_of_spades", "card_7_of_diamonds",
                "card_8_of_clubs", "card_8_of_hearts", "card_8_of_spades", "card_8_of_diamonds",
                "card_9_of_clubs", "card_9_of_hearts", "card_9_of_spades", "card_9_of_diamonds",
                "card_10_of_clubs", "card_10_of_hearts", "card_10_of_spades", "card_10_of_diamonds",
                "card_jack_of_clubs", "card_jack_of_hearts", "card_jack_of_spades", "card_jack_of_diamonds",
                "card_queen_of_clubs", "card_queen_of_hearts", "card_queen_of_spades", "card_queen_of_diamonds",
                "card_king_of_clubs", "card_king_of_hearts", "card_king_of_spades", "card_king_of_diamonds",
                "card_black_joker", "card_red_joker"};

        private ArrayList<RelativeLayout> views = new ArrayList<>();
        private EmotionsPanel emotionsPanel;
        private SimpleEmojiPanel dogsPanel;
        private SimpleEmojiPanel plaatsPanel;
        private SimpleEmojiPanel signalsPanel;
        private SimpleEmojiPanel yoPanel;
        private SimpleEmojiPanel cardsPanel;

        public SmilesPagerAdapter() {
            for (int i = 0; i < 6; i++) {
                RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.emotions, null);
                inflater.inflate(R.layout.empty_panel_with_progress, rootView);
                views.add(rootView);
            }
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final RelativeLayout v = views.get(position);
            switch (position) {
                case 0:
                    //smiles
                    if (emotionsPanel == null) {
                        emotionsPanel = new EmotionsPanel(parent);
                        AsyncTask<Void, Void, Void> smilesLoader = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                for (final String s : Tool.getEms()) {
                                    emotionsPanel.addEmotion(s, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            parent.getBar().addText(s);
                                        }
                                    });
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                v.removeAllViews();
                                v.addView(emotionsPanel.getView());
                            }
                        };
                        smilesLoader.execute((Void[]) null);
                    }
                    break;
                case 1:
                    //dogs
                    if (dogsPanel == null) {
                        dogsPanel = new SimpleEmojiPanel(parent);
                        getPanel(dogsPanel, v, dogIds, dogNmes);
                    }
                    break;
                case 2:
                    //plaats
                    if (plaatsPanel == null) {
                        plaatsPanel = new SimpleEmojiPanel(parent);
                        getPanel(plaatsPanel, v, plaatIds, plaatNames);
                    }
                    break;
                case 3:
                    //signals
                    if (signalsPanel == null) {
                        signalsPanel = new SimpleEmojiPanel(parent);
                        getPanel(signalsPanel, v, signalIds, signalNames);
                    }
                    break;
                case 4:
                    //yo
                    if (yoPanel == null) {
                        yoPanel = new SimpleEmojiPanel(parent);
                        getPanel(yoPanel, v, yoIds, yoNames);
                    }
                    break;
                case 5:
                    //cards
                    if (cardsPanel == null) {
                        cardsPanel = new SimpleEmojiPanel(parent);
                        getPanel(cardsPanel, v, clubIds, clubNames);
                    }
                    break;
//                case 6:
                    //flags
//                    if (flagsPanel == null) {
//                        flagsPanel = new SimpleEmojiPanel(parent);
//                        getPanel(flagsPanel, v, flagIds, flagNames);
//                        getPanel(cardsPanel, v, clubIds, clubNames);
//                    }
//                    break;
                default:
            }
            if (v.getParent() != null && v.getParent() instanceof ViewGroup)
                ((ViewGroup) v.getParent()).removeAllViews();
            container.addView(v, 0);
            return v;
        }

        private void getPanel(final SimpleEmojiPanel panel, final RelativeLayout v, final int[] imageIds, final String[] imageNames) {
            final RelativeLayout rootView = (RelativeLayout) inflater.inflate(R.layout.emotions, null);
            inflater.inflate(R.layout.empty_panel_with_progress, rootView);

            AsyncTask<Void, Void, Void> loader = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    for (int i = 0; i < imageIds.length; i++) {
                        panel.addEmotion(imageIds[i], imageNames[i], SmilePanel.this.listener);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    v.removeAllViews();
                    v.addView(panel.getView());
                }
            };
            loader.execute((Void[]) null);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }
    }
}
