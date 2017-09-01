package mobi.sender.tool;

import android.content.Context;

import com.sender.library.ChatFacade;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import mobi.sender.App;
import mobi.sender.Bus;
import mobi.sender.event.MsgUpdatedEvent;

/**
 * Created by Smmarat on 16.09.16.
 */
public class HistoryLoader {

    private static HistoryLoader instance;
    private BlockingQueue<HisReq> queue = new ArrayBlockingQueue<>(20);
    private boolean running = false;
    private ChatFacade chat;
    private LoadListener loadListener;
//    private Context mCtx;
    public static int COUNT_MESS = 50;

    private HistoryLoader(Context ctx, ChatFacade chat, LoadListener ll) {
        this.chat = chat;
        this.loadListener = ll;
//        mCtx = ctx;
    }

    public static HistoryLoader getInstance(Context ctx, ChatFacade chat, LoadListener ll) {
        if (instance == null) instance = new HistoryLoader(ctx, chat, ll);
        return instance;
    }

    public void getHistory(HisReq req) {
        Tool.log("---HisReq = "+req);
        for (HisReq hr : queue) {
            if (hr.isInclude(req)) {
                Tool.log("his reg ignored: " + req);
                return;
            }
        }
        queue.add(req);
        if (!running) {
            new Worker().start();
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            while (queue.size() > 0) {
                try {
                    final HisReq req = queue.poll();
                    int size = req.getSize();
                    if(req.getTop() != null && req.getBoth() != null) size = -1;
                    if(req.getTop() != null && req.getBoth() == null) size = 50;

                    chat.getHistory(req.getChatId(),  req.getTop(), req.getBoth(), size, new ChatFacade.JsonRespListener() {
                        @Override
                        public void onSuccess(JSONObject jo) {
                            try {
                                boolean more = jo.optBoolean("more");
                                JSONArray ja = sortJsonArr(jo.optJSONArray("msgs"));

                                loadListener.onLoad(req.getChatId(), ja);

                                if (req.getListener() != null) req.getListener().onResponse(new JSONObject().put("more", more));

                                Bus.getInstance().post(new MsgUpdatedEvent(req.getChatId()));

                                if(jo.has("status")){
                                    Storage.getInstance(App.getInstance()).setChatStatus(req.getChatId(), jo.optString("status"));
                                }
                            } catch (Exception e) {
                                if (req.getListener() != null)
                                    req.getListener().onError(e);
                                else e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Exception e, String s) {
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            running = false;
        }
    }

    private JSONArray sortJsonArr(JSONArray array) {
        List asList = new ArrayList(array.length());
        for (int i=0; i<array.length(); i++){
            asList.add(array.opt(i));
        }
        Collections.sort(asList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                String lid = null;
                String rid = null;
                try {
                    lid = lhs.getString("packetId");
                    rid = rhs.getString("packetId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return lid.compareTo(rid);
            }
        });

        return new JSONArray(asList);
    }

    public interface LoadListener {
        long onLoad(String chatId, JSONArray msgs);
    }



    public void getChatHole(HisReq req) {
        Tool.log("---getChatHole = "+req);
        for (HisReq hr : queue) {
            if (hr.isInclude(req)) {
                Tool.log("his reg ignored: " + req);
                return;
            }
        }
        queue.add(req);
        if (!running) {
            new Worker2().start();
        }
    }

    private class Worker2 extends Thread {
        @Override
        public void run() {
            while (queue.size() > 0) {
                try {
                    final HisReq req = queue.poll();
                    int size = req.getSize();
                    if(req.getTop() != null && req.getBoth() != null) size = -1;

                    chat.getHistory(req.getChatId(),  req.getTop(), req.getBoth(), size, new ChatFacade.JsonRespListener() {
                        @Override
                        public void onSuccess(JSONObject jo) {
                            try {
                                boolean more = jo.optBoolean("more");
                                JSONArray ja = sortJsonArr(jo.optJSONArray("msgs"));

                                loadListener.onLoad(req.getChatId(), ja);

                                if (req.getListener() != null)
                                    req.getListener().onResponse(new JSONObject().put("more", more));

                                Bus.getInstance().post(new MsgUpdatedEvent(req.getChatId()));

                                if(jo.has("status")){
                                    Storage.getInstance(App.getInstance()).setChatStatus(req.getChatId(), jo.optString("status"));
                                }

                                JSONObject joFirst = ja.getJSONObject(0);
                                JSONObject joLast = ja.getJSONObject(ja.length()-1);

                                long packetId1 = joFirst.getLong("packetId");
                                long packetId2 = joLast.getLong("packetId");

                                Storage.getInstance(App.getInstance()).updChatHole(req.getChatId(), packetId1, packetId2);

                            } catch (Exception e) {
                                if (req.getListener() != null)
                                    req.getListener().onError(e);
                                else e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Exception e, String s) {
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            running = false;
        }
    }
}
