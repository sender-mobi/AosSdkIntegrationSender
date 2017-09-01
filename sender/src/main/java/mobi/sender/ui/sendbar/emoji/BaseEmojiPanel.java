package mobi.sender.ui.sendbar.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by vd on 8/15/16.
 */
public abstract class BaseEmojiPanel {

    protected LayoutInflater inflater;
    protected Context context;

    public BaseEmojiPanel(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    abstract public View getView();
}
