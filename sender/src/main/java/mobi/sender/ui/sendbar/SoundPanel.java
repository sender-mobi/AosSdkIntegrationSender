package mobi.sender.ui.sendbar;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.SendFileReq;
import mobi.sender.tool.Tool;
import mobi.sender.tool.bar.SendBarRenderer;
import mobi.sender.tool.bar.SenderMediaController;
import mobi.sender.tool.bar.SoundView;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.CircularProgressBar;

/**
 * Created by Zver on 11/01/17.
 */
public class SoundPanel extends BasePanel {

    public SoundPanel(ChatActivity parent) {
        super(parent);
    }

    @Override
    public void attachTo(final ViewGroup viewGroup) {
        viewGroup.addView(
                new SoundView().getVoiceRecorder(parent, new SendBarRenderer.OnVoiceSendListener() {
                    @Override
                    public void onSendVoice(String fileName, float length) {
                        parent.closePanel();
                        Bus.getInstance().post(new SendFileReq(SoundPanel.this.parent.getChatId(), "", length + "", fileName, null));
                    }

                    @Override
                    public void onMediaNotFound() {
                        parent.closePanel();
                        Toast.makeText(parent, "Media not found", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

}
