package mobi.sender.ui.sendbar.emoji;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import mobi.sender.R;
import mobi.sender.ui.flow.FlowLayout;

import static mobi.sender.tool.Tool.checkEmojiSupport;

/**
 * Created by vd on 8/15/16.
 */
public class EmotionsPanel extends BaseEmojiPanel {

    private final View view;
    private FlowLayout grid;
    //    private TabLayout tabLayout;

    public EmotionsPanel(Context context) {
        super(context);
        view = inflater.inflate(R.layout.emotions_panel, null);
        grid = (FlowLayout) view.findViewById(R.id.grid);
        grid.removeAllViews();

//        tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.clock).setTag(0));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.emoticon_smile).setTag(1));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.flower).setTag(2));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.food).setTag(3));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.soccer).setTag(4));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.pine_tree).setTag(5));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.pound).setTag(6));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.backspace).setTag(7));
//        correctTabAlpha(0);
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                Object t = tab.getTag();
//                if (t != null) {
//                    int it = (int) t;
//                    correctTabAlpha(it);
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//            }
//        });
    }

    public View getView() {
        return view;
    }

    public void addEmotion(String emotion, View.OnClickListener listener) {
        TextView textView = new TextView(context);
        checkEmojiSupport(context, textView);
        grid.addView(textView);
        textView.setOnClickListener(listener);
        textView.setText(emotion);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        textView.setAlpha(1);
    }

    // TODO: duplicate from MainActivity
//    private void correctTabAlpha(int position) {
//        for (int i = 0; i < tabLayout.getTabCount(); i++) {
//            TabLayout.Tab tab = tabLayout.getTabAt(i);
//            if (i == position) tab.getIcon().setAlpha(255);
//            else tab.getIcon().setAlpha(128);
//        }
//    }
}
