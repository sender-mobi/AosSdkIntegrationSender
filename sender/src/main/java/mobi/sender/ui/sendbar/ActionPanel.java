package mobi.sender.ui.sendbar;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.SendFormReq;
import mobi.sender.ui.ChatActivity;

/**
 * Created by vd on 8/11/16.
 */
public class ActionPanel extends BasePanel {

    public ActionPanel(ChatActivity parent) {
        super(parent);
    }

    @Override
    public void attachTo(final ViewGroup vg) {
        View view = inflater.inflate(R.layout.chat_sendbar, vg);
        setSendbarItem(view, R.id.transfer, R.drawable.ic_cash, R.string.cht_sendbar_transfer, new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                Bus.getInstance().post(new SendFormReq(".sendMoney.sender", parent.getChatId(), null, null));
                parent.closePanel();
            }
        });
//        setSendbarItem(view, R.id.ask, R.drawable.invoice, R.string.chat_sendbar_ask, new View.OnClickListener() {
//            @Override
//            public void onClick(View view1) {
//                // TODO: implement
//            }
//        });
        setSendbarItem(view, R.id.recharge, R.drawable.ic_smartphone, R.string.cht_sendbar_recharge, new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                Bus.getInstance().post(new SendFormReq(".payMobile.sender", parent.getChatId(), null, null));
                parent.closePanel();
            }
        });
//        setSendbarItem(view, R.id.games, R.drawable.gamepad_variant, R.string.chat_sendbar_games, new View.OnClickListener() {
//            @Override
//            public void onClick(View view1) {
//                // TODO: implement
//            }
//        });
    }

    private void setSendbarItem(View parent, int viewId, int imageId, int textId, View.OnClickListener onClickListener) {
        View view = parent.findViewById(viewId);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(imageId);
        TextView textView = (TextView) view.findViewById(R.id.tv_badge_text);
        textView.setText(textId);
        view.setOnClickListener(onClickListener);
    }

}
