package mobi.sender.ui.sendbar;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import mobi.sender.ui.ChatActivity;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by vd on 8/11/16.
 */
public abstract class BasePanel {

    protected ChatActivity parent;
    protected LayoutInflater inflater;

    public BasePanel(ChatActivity parent) {
        this.parent = parent;
        inflater = (LayoutInflater) parent.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    abstract public void attachTo(ViewGroup parent);

    public interface OnAction {
    }
}
