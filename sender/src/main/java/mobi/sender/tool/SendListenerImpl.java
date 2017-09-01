package mobi.sender.tool;

import android.app.Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import mobi.sender.Bus;
import mobi.sender.event.SendFormReq;
import mobi.sender.model.msg.FormMessage;
import mobi.sender.tool.fml.FMLRenderer;
import mobi.sender.ui.BaseActivity;

/**
 * Created by Smmarat on 29.09.16.
 */

public class SendListenerImpl implements FMLRenderer.SendListener {

    private FormMessage fm;
    private ActionExecutor actionExecutor;
    private ActionListener listener;
    private int position;

    public SendListenerImpl(FormMessage fm, String chatId, Activity ctx, int position, ActionListener listener) {
        this.fm = fm;
        this.actionExecutor = new ActionExecutor((BaseActivity) ctx, chatId);
        this.listener = listener;
        this.position = position;
    }

    @Override
    public void doSend(JSONObject data) {
        Bus.getInstance().post(new SendFormReq(fm.getClassName(), fm.getChatId(), fm.getProcId(), data));
    }

    @Override
    public void doAction(String oper, final JSONObject data, Map<String, Object> params, final FMLRenderer.ActionProcessListener apl) {
        params.put(ActionExecutor.PARAM_MESSAGE, fm);
        if (!params.keySet().contains(ActionExecutor.PARAM_CLASS)) {
            params.put(ActionExecutor.PARAM_CLASS, fm.getClassName());
        }
        actionExecutor.setOnActionListener(new ActionExecutor.OnActionFinished() {
            @Override
            public void onActionFinished(boolean disableForm) {
                if (disableForm) {
                    try {
                        fm.setView(FMLRenderer.disableView(new JSONObject(fm.getView()), data).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                apl.onProcess(disableForm);
                try {
                    JSONObject obj = new JSONObject(fm.getView());
                    listener.OnActionListener(FMLRenderer.disableView(obj, data).toString(), position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void refreshForm() {
                try {
                    JSONObject jo = new JSONObject(fm.getView());
                    fm.setView(FMLRenderer.refreshView(jo, data).toString());
                    listener.OnActionListener(FMLRenderer.refreshView(jo, data).toString(), position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        actionExecutor.exec(data, oper, params);
    }

    public interface ActionListener {
        void OnActionListener(String s, int positon);
    }
}
