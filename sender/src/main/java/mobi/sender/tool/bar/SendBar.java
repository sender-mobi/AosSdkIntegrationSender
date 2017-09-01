package mobi.sender.tool.bar;

import org.json.JSONException;
import org.json.JSONObject;

import mobi.sender.model.ChatBased;

public class SendBar {

    private static final String PRIVAT_SB = "{\"textColor\":\"#00ff00\",\"items\":[{\"id\":\"1\",\"icon\":\"https://s.sender.mobi/bars/plus_g.png\",\"actions\":[{\"oper\":\"reload\",\"_0\":[2,3,4],\"_1\":[7,8]}]},{\"id\":\"2\",\"icon\":\"https://s.sender.mobi/bars/cancel_g.png\",\"actions\":[{\"oper\":\"reload\",\"_0\":[1,3,4]}]},{\"id\":\"3\",\"icon\":\"https://s.sender.mobi/bars/qr_g.png\",\"actions\":[{\"oper\":\"qrScan\",\"class\":\".qr.sender\",\"chatId\":\"user+sender\"}]},{\"id\":\"4\",\"icon\":\"https://s.sender.mobi/bars/text_g.png\",\"actions\":[{\"oper\":\"sendMsg\",\"expand\":false},{\"oper\":\"reload\",\"_0\":[1,5,4,6]}]},{\"id\":\"5\",\"icon\":\"https://s.sender.mobi/bars/smile_g.png\",\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"smile\"}]},{\"id\":\"6\",\"icon\":\"https://s.sender.mobi/bars/voice_g.png\",\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"voice\"}]},{\"id\":\"7\",\"icon\":\"https://s.sender.mobi/bars/send_g.png\",\"name\":{\"ru\":\"Отправить\",\"en\":\"Send\"},\"actions\":[{\"oper\":\"callRobot\",\"class\":\".sendMoney.sender\"}]},{\"id\":\"8\",\"icon\":\"https://s.sender.mobi/bars/top_up_g.png\",\"name\":{\"ru\":\"Пополнить\",\"en\":\"Fill phone\"},\"actions\":[{\"oper\":\"callRobot\",\"class\":\".14543.privat24\"}]}],\"init\":{\"_0\":[1,3,4]}}";
    public static final String DEFAULT_SB = "{\"textColor\":\"#9c9c9c\",\"items\":[{\"id\":\"1\",\"icon\":\"https://s.sender.mobi/bars/plus.png\",\"name\":{\"ru\":\"Больше...\",\"en\":\"More...\"},\"actions\":[{\"oper\":\"reload\",\"_0\":[2,3,4,5,8,6,7],\"_1\":[9,10,11,12,13,16,17,18]}]},{\"id\":\"2\",\"icon\":\"https://s.sender.mobi/bars/cancel.png\",\"name\":{\"ru\":\"Закрыть\",\"en\":\"Close\"},\"actions\":[{\"oper\":\"reload\",\"_0\":[1,3,4,5,8,6,7]}]},{\"id\":\"3\",\"icon\":\"https://s.sender.mobi/bars/twitch.png\",\"name\":{\"ru\":\"Взбодрить\",\"en\":\"Twitch\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"twitch\"}]},{\"id\":\"4\",\"icon\":\"https://s.sender.mobi/bars/geo2.png\",\"name\":{\"ru\":\"Где Я\",\"en\":\"Location\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"location\"}]},{\"id\":\"5\",\"icon\":\"https://s.sender.mobi/bar/sender/text_A9jE.png\",\"name\":{\"ru\":\"Послать сообщение\",\"en\":\"Send message\"},\"actions\":[{\"oper\":\"sendMsg\",\"expand\":false},{\"oper\":\"reload\",\"_0\":[1,7,5]}]},{\"id\":\"6\",\"icon\":\"https://s.sender.mobi/bars/camera.png\",\"name\":{\"ru\":\"Фото\",\"en\":\"Photo\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"photo\"}]},{\"id\":\"7\",\"icon\":\"https://s.sender.mobi/bars/smile.png\",\"name\":{\"ru\":\"Смайлы\",\"en\":\"Smiles\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"smile\"}]},{\"id\":\"8\",\"icon\":\"https://s.sender.mobi/bars/voice.png\",\"name\":{\"ru\":\"Голос\",\"en\":\"Voice\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"voice\"}]},{\"id\":\"9\",\"icon\":\"https://s.sender.mobi/bars/add_user.png\",\"name\":{\"ru\":\"Добавить\",\"en\":\"Add user\"},\"actions\":[{\"oper\":\"addUser\"}]},{\"id\":\"10\",\"icon\":\"https://s.sender.mobi/bars/stickers.png\",\"name\":{\"ru\":\"Стикер\",\"en\":\"Sticker\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"sticker\"}]},{\"id\":\"11\",\"icon\":\"https://s.sender.mobi/bars/attach.png\",\"name\":{\"ru\":\"Файл\",\"en\":\"Send file\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"file\"}]},{\"id\":\"12\",\"icon\":\"https://s.sender.mobi/bars/video.png\",\"name\":{\"ru\":\"Видео\",\"en\":\"Send video\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"video\"}]},{\"id\":\"13\",\"icon\":\"https://s.sender.mobi/bars/cards.png\",\"name\":{\"ru\":\"Деньги\",\"en\":\"Money\"},\"actions\":[{\"oper\":\"reload\",\"_1\":[14,15]}]},{\"id\":\"14\",\"icon\":\"https://s.sender.mobi/bars/send.png\",\"name\":{\"ru\":\"Отправить\",\"en\":\"Send\"},\"actions\":[{\"oper\":\"callRobot\",\"class\":\".sendMoney.sender\"}]},{\"id\":\"15\",\"icon\":\"https://s.sender.mobi/bars/top_up.png\",\"name\":{\"ru\":\"Пополнить\",\"en\":\"Fill phone\"},\"actions\":[{\"oper\":\"callRobot\",\"class\":\".payMobile.sender\"}]},{\"id\":\"16\",\"icon\":\"https://s.sender.mobi/bars/lock_open.png\",\"icon2\":\"https://s.sender.mobi/bars/lock_closed.png\",\"name\":{\"ru\":\"Шифрование\",\"en\":\"Encryption\"},\"actions\":[{\"oper\":\"switchCrypto\"}]},{\"id\":\"17\",\"icon\":\"https://s.sender.mobi/bars/vibro.png\",\"name\":{\"ru\":\"Вибро\",\"en\":\"Vibro\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"vibro\"}]},{\"id\":\"18\",\"icon\":\"https://s.sender.mobi/bars/games.png\",\"name\":{\"ru\":\"Игры\",\"en\":\"Play\"},\"actions\":[{\"oper\":\"reload\",\"_1\":[19,20,21]}]},{\"id\":\"19\",\"icon\":\"https://s.sender.mobi/bars/tic-tac.png\",\"name\":{\"ru\":\"Крестики-нолики\",\"en\":\"Tic Tac Toe\"},\"actions\":[{\"oper\":\"callRobot\",\"class\":\".ticTacToe.sender\"}]},{\"id\":\"20\",\"icon\":\"https://s.sender.mobi/bars/vinni.png\",\"name\":{\"ru\":\"Винни Пух\",\"en\":\"Winny Puh\"},\"actions\":[{\"oper\":\"callRobot\",\"class\":\".winnieThePoohHoney.sender\"}]},{\"id\":\"21\",\"icon\":\"https://s.sender.mobi/bars/chess.png\",\"name\":{\"ru\":\"Шахматы\",\"en\":\"Chess\"},\"actions\":[{\"oper\":\"callRobot\",\"class\":\".chess.sender\"}]}],\"init\":{\"_0\":[1,3,4,5,8,6,7]}}";
    //    private static final String DEFAULT_SB = "{\"textColor\":\"#000000\",\"items\":[{\"id\":\"1\",\"icon\":\"https://s.sender.mobi/bars/plus.png\",\"actions\":[{\"oper\":\"reload\",\"_0\":[2,5,7],\"_1\":[6,9,11]}]},{\"id\":\"2\",\"icon\":\"https://s.sender.mobi/bars/cancel.png\",\"actions\":[{\"oper\":\"reload\",\"_0\":[1,5,7]}]},{\"id\":\"5\",\"icon\":\"https://s.sender.mobi/bars/text.png\",\"actions\":[{\"oper\":\"sendMsg\",\"expand\":false},{\"oper\":\"reload\",\"_0\":[1,5,7]}]},{\"id\":\"6\",\"icon\":\"https://s.sender.mobi/bars/camera.png\",\"name\":{\"ru\":\"Фото\",\"en\":\"Photo\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"photo\"}]},{\"id\":\"7\",\"icon\":\"https://s.sender.mobi/bars/smile.png\",\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"smile\"}]},{\"id\":\"9\",\"icon\":\"https://s.sender.mobi/bars/add_user.png\",\"name\":{\"ru\":\"Добавить\",\"en\":\"Add user\"},\"actions\":[{\"oper\":\"addUser\"}]},{\"id\":\"11\",\"icon\":\"https://s.sender.mobi/bars/attach.png\",\"name\":{\"ru\":\"Файл\",\"en\":\"Send file\"},\"actions\":[{\"oper\":\"sendMedia\",\"type\":\"file\"}]}],\"init\":{\"_0\":[1,5,7]}}";
    private String src;


    public SendBar(JSONObject jo) {
        src = jo.toString();
    }

    public SendBar(String src) {
        this.src = src;
    }

    public String getSrc() {
        return src;
    }

    public static JSONObject toJson(ChatBased cu) {
        JSONObject jo = getDefault(cu);
        return jo;
    }

    public static JSONObject getDefault(ChatBased cu) {
        JSONObject jo = null;
        // try parse self bar
//        if (cu == null) {
//            try {
//                jo = new JSONObject(DEFAULT_SB);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        } else {
//            try {
//                if (cu.containsBar()) {
//                    jo = new JSONObject(cu.getBar());
//                    if (!jo.has("items")) throw new JSONException("bar without items!");
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            if (jo == null) {
//                // try parse Sender's bar
//                ChatUser sender = DbHelper.getInstance().findChatUserById("sender", true, DbHelper.SEARCH_BY_USER_ID);
//                try {
//                    if (sender != null && sender.getBar() != null && sender.getBar().startsWith("{")) {
//                        jo = new JSONObject(sender.getBar());
//                        if (!jo.has("items")) throw new JSONException("bar without items!");
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (jo == null || jo.length() == 0) {
//                // try parse default bar
//                try {
//                    jo = new JSONObject(cu.isCompany() && (cu.getUserId().equals(App.PRIVAT24_ID) || cu.getUserId().equals(App.PRIVATBANK_ID)) ? PRIVAT_SB : DEFAULT_SB);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        return jo;
    }

}
