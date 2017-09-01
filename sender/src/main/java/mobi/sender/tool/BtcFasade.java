package mobi.sender.tool;

import android.content.Context;

import org.bitcoinj.core.Coin;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by vp
 * on 12.04.16.
 */
public class BtcFasade {

    public static void getBalance(final Context ctx, final BalListener bl) {
        try {
            LWallet wallet = LWallet.getInstance(ctx);
            if (wallet == null) return;
//            String pubB58 = wallet.getWatchingKey().serializePubB58(wallet.getParams());
//            Bus.getInstance().post(new BtcBalanceRequest(pubB58, new SyncEvent.SRespListener() {
//                @Override
//                public void onResponse(JSONObject data) {
//                    Coin coin = Coin.valueOf(data.optLong("bal"));
//                    bl.onSuccess(coin.toPlainString());
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    bl.onError(e);
//                }
//            }));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        LWallet wallet = LWallet.getInstance(ctx);
                        if (wallet == null) return;
                        String pubB58 = wallet.getWatchingKey().serializePubB58(wallet.getParams());
                        String req = "https://blockchain.info/ru/q/addressbalance/" + pubB58;
                        Tool.log("~~> " + req);
                        String resp = new OkHttpClient().newCall(new Request.Builder().url(req).build()).execute().body().string();
                        Tool.log("<~~ " + resp);
                        long l = Long.parseLong(resp);
                        Coin coin = Coin.valueOf(l);
                        bl.onSuccess(coin.toPlainString());
                    } catch (Exception e) {
                        bl.onError(e);
                    }
                }
            }).start();
        } catch (Exception e) {
            bl.onError(e);
        }
    }

    public static void fillOuts(final Context ctx, final BalListener bl) {
//        try {
//            final LWallet wallet = LWallet.getInstance(ctx);
//            if (wallet == null) return;
//            if (!wallet.isUnspentsAdded()) {
//                String pubB58 = wallet.getWatchingKey().serializePubB58(wallet.getParams());
//                Bus.getInstance().post(new BtcOutsRequest(pubB58, new SyncEvent.SRespListener() {
//                    @Override
//                    public void onResponse(JSONObject data) {
//                        wallet.addOutputs(data);
//                        bl.onSuccess(wallet.getBalance().toPlainString());
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        bl.onError(e);
//                    }
//                }));
//            } else {
//                bl.onSuccess(wallet.getBalance().toPlainString());
//            }
//        } catch (Exception e) {
//            bl.onError(e);
//        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    LWallet wallet = LWallet.getInstance(ctx);
//                    if (wallet == null) return;
//                    if (!wallet.isUnspentsAdded()) {
//                        String pubB58 = wallet.getWatchingKey().serializePubB58(wallet.getParams());
//                        String req = "https://blockchain.info/ru/unspent?active=" + pubB58;
//                        Log.v("~~>", req);
//                        String resp = EntityUtils.toString(new DefaultHttpClient().execute(new HttpGet(req)).getEntity());
//                        Log.v("<~~", resp);
//                        JSONObject jo = new JSONObject(resp);
//                        wallet.addOutputs(jo);
//                        bl.onSuccess(wallet.getBalance().toPlainString());
//                    }
//                } catch (Exception e) {
//                    bl.onError(e);
//                }
//            }
//        }).start();
    }

    public static void pay(final Context ctx, final String to, final Coin amt, final PayListener pl) {
//        try {
//            LWallet lWallet = LWallet.getInstance(ctx);
//            if (lWallet == null) throw new Exception("wallet is null");
//            final Transaction tx = lWallet.makeTransaction(to, amt, null);
//            byte[] bytes = tx.bitcoinSerialize();
//            String txs = Base58.encode(bytes);
//            Bus.getInstance().post(new BtcPublishTxRequest(txs, new SyncEvent.SRespListener() {
//                @Override
//                public void onResponse(JSONObject data) {
//                    if (!"ok".equalsIgnoreCase(data.optString("st"))) {
//                        pl.onError(new Exception(data.optString("err")));
//                    }
//                    pl.onSuccess(tx.getHashAsString());
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    pl.onError(e);
//                }
//            }));
//        } catch (Exception e) {
//            pl.onError(e);
//        }
    }

    public static void notarize(final Context ctx, final String path, final PayListener pl) {
//        try {
//            byte[] fBytes = Tool.readFile(path);
//            if (fBytes != null) {
//                byte[] hash = Sha256Hash.hash(fBytes);
//                LWallet lWallet = LWallet.getInstance(ctx);
//                if (lWallet == null) throw new Exception("wallet is null");
//                final Transaction tx = lWallet.makeTransaction(lWallet.currentReceiveAddress().toString(), Coin.ZERO, hash);
//                byte[] bytes = tx.bitcoinSerialize();
//                String txs = Base58.encode(bytes);
//                Bus.getInstance().post(new BtcPublishTxRequest(txs, new SyncEvent.SRespListener() {
//                    @Override
//                    public void onResponse(JSONObject data) {
//                        if (!"ok".equalsIgnoreCase(data.optString("st"))) {
//                            pl.onError(new Exception(data.optString("err")));
//                        }
//                        pl.onSuccess(tx.getHashAsString());
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        pl.onError(e);
//                    }
//                }));
//            }
//        } catch (Exception e) {
//            App.track(e);
//        }
    }

    public interface BalListener {
        void onSuccess(String bal);
        void onError(Exception e);
    }

    public interface PayListener {
        void onSuccess(String txHash);
        void onError(Exception e);
    }
}
