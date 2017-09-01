package mobi.sender.ui.sendbar.emoji;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import mobi.sender.R;
import mobi.sender.tool.Tool;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.flow.FlowLayout;

/**
 * Created by vd on 8/16/16.
 */
public class SimpleEmojiPanel extends BaseEmojiPanel {

    private final View view;
    private FlowLayout grid;

    public SimpleEmojiPanel(Context context) {
        super(context);
        view = inflater.inflate(R.layout.simple_emoji, null);
        grid = (FlowLayout) view.findViewById(R.id.grid);
        grid.removeAllViews();
    }

    @Override
    public View getView() {
        return view;
    }

    public void addEmotion(int id, String name, View.OnClickListener listener) {
        final View cellView = inflater.inflate(R.layout.icon_cell, null);
        ImageView imageView = (ImageView) cellView.findViewById(R.id.imageView);
        imageView.setImageResource(id);
        cellView.setTag(name);
        cellView.setOnClickListener(listener);
        ((ChatActivity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                grid.addView(cellView);
            }
        });
    }


}
