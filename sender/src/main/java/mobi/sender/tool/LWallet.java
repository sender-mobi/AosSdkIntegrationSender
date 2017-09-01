package mobi.sender.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.jce.provider.JCEECPrivateKey;
import org.spongycastle.jce.provider.JCEECPublicKey;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;
import org.spongycastle.jce.spec.ECNamedCurveSpec;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.jce.spec.ECPrivateKeySpec;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.spec.KeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by vp
 * on 21.03.16.
 */
public class LWallet extends Wallet {

    private static final String PROP_MNEMONIC = "mnemonic";
    private static final String PROP_CREATED = "created";
    private static final NetworkParameters params = MainNetParams.get();
    private static LWallet instance;
    private DeterministicKey rootKey;

    private LWallet(NetworkParameters params) {
        super(params);
    }

    private LWallet(NetworkParameters params, KeyChainGroup keyChainGroup) {
        super(params, keyChainGroup);
    }

    public static synchronized LWallet getInstance(android.content.Context ctx, boolean createNewInstance) {
        if (instance == null || createNewInstance) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
            String mnemonic = preferences.getString(PROP_MNEMONIC, null);
            long created = preferences.getLong(PROP_CREATED, -1);
            if (createNewInstance || mnemonic == null || mnemonic.trim().length() == 0 || created < 0) {
                Log.v(LWallet.class.getSimpleName(), "no seed");
                instance = new LWallet(params);
                instance.allowSpendingUnconfirmedTransactions();
                DeterministicSeed seed = instance.getKeyChainSeed();
                mnemonic = Utils.join(seed.getMnemonicCode());
                preferences.edit().putString(PROP_MNEMONIC, mnemonic).putLong(PROP_CREATED, seed.getCreationTimeSeconds()).apply();
                instance.rootKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
            } else {
                Log.v(LWallet.class.getSimpleName(), "seed exists");
                try {
                    instance = new LWallet(params, new KeyChainGroup(params, new DeterministicSeed(mnemonic, null, "", created)));
                    instance.allowSpendingUnconfirmedTransactions();
                    DeterministicSeed seed = instance.getKeyChainSeed();
                    instance.rootKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }

    public static LWallet getInstance(android.content.Context ctx) {
        return getInstance(ctx, false);
    }

    public static byte[] hmacSha256(byte[] msg, byte[] keyB) {
        try {
            SecretKeySpec key = new SecretKeySpec(keyB, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return mac.doFinal(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] slice(byte[] in, int start, int end) {
        int len = end - start;
        byte[] out = new byte[len];
        System.arraycopy(in, start, out, 0, len);
        return out;
    }

    public static byte[] encryptAesCBC(byte[] key, byte[] initV, byte[] value) throws Exception {
        IvParameterSpec iv = new IvParameterSpec(initV);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] rez = cipher.doFinal(value);
        byte[][] out = new byte[][]{initV, rez};
        return concatByteArrays(out);
    }

    public static byte[] decryptAesCBC(byte[] key, byte[] value) throws Exception {
        byte[] initV = slice(value, 0, 16);
        byte[] data = slice(value, 16, value.length);
        IvParameterSpec iv = new IvParameterSpec(initV);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        return cipher.doFinal(data);
    }

    public static byte[] kEkM(ECPublicKey publicKey, ECPrivateKey privateKey) {
        BigInteger d = privateKey.getD();
        ECPoint q = publicKey.getQ();
        ECPoint p = q.multiply(d);
        BigInteger x = p.getX().toBigInteger();
        byte[] s = getUnsignedBytes(x, 32);
        return sha512(s, 64);
    }

    public static byte[] concatByteArrays(byte[][] arrs) {
        int alLen = 0;
        for (byte[] e : arrs) {
            alLen += e.length;
        }
        byte[] d = new byte[alLen];
        int endPos = 0;
        for (byte[] e : arrs) {
            System.arraycopy(e, 0, d, endPos, e.length);
            endPos += e.length;
        }
        return d;
    }

    public static byte[] sha512(byte[] bytes, int digestLength) {
        MessageDigest sha512;
        try {
            sha512 = MessageDigest.getInstance("SHA-512");
            byte[] sum = sha512.digest(bytes);
            return copyOf(sum, digestLength);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException occurred in DigestSHA512.sha512()", e);
        }
    }

    public static byte[] getUnsignedBytes(BigInteger number, int length) {
        byte[] value = number.toByteArray();

        if (value.length > length + 1) {
            throw new IllegalArgumentException
                    ("The given BigInteger does not fit into a byte array with the given length: " + value.length
                            + " > " + length);
        }

        byte[] result = new byte[length];

        int i = value.length == length + 1 ? 1 : 0;
        for (; i < value.length; i++) {
            result[i + length - value.length] = value[i];
        }
        return result;
    }

    public static byte[] copyOf(byte[] from, int length) {
        byte[] result = new byte[length];
        System.arraycopy(from, 0, result, 0, length);
        return result;
    }

    public static String encryptPass(String data, String pass) throws Exception {
        Key key = pass2key(pass);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, key, getIv());
        byte[] bytes = cipher.doFinal(data.getBytes());
        return encodeM(bytes);
    }

    public static String decryptPass(String data, String pass) throws Exception {
        Key key = pass2key(pass);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
        cipher.init(Cipher.DECRYPT_MODE, key, getIv());
        byte[] bytes = cipher.doFinal(decodeM(data));
        return new String(bytes);
    }

    private static IvParameterSpec getIv() {
        byte[] iv = Hex.decode("01f01f01f01f01f01f01f01f01f01f01");
        return new IvParameterSpec(iv);
    }

    public static Key pass2key(String pass) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] salt = Hex.decode("4205c730b7afd0c048a9e9775ac4167e");
        KeySpec keyspec = new PBEKeySpec(pass.toCharArray(), salt, 1000, 128);
        SecretKey secretKey = factory.generateSecret(keyspec);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    public static String getRandomAesKey() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr;
            if (android.os.Build.VERSION.SDK_INT >= 17) {
                sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
            } else {
                sr = SecureRandom.getInstance("SHA1PRNG");
            }
            kgen.init(128, sr);
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();
            return encodeM(raw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encryptAes(String key, String data) throws Exception {
        byte[] keyb = decodeM(key);
        SecretKeySpec skeySpec = new SecretKeySpec(keyb, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, getIv());
        byte[] bytes = cipher.doFinal(data.getBytes());
        return encodeM(bytes);
    }

    public static String decryptAes(String key, String data) throws Exception {
        byte[] keyb = decodeM(key);
        SecretKeySpec skeySpec = new SecretKeySpec(keyb, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, getIv());
        byte[] bytes = cipher.doFinal(decodeM(data));
        return new String(bytes);
    }

    public String getMnemonic() {
        DeterministicSeed seed = instance.getKeyChainSeed();
        return Utils.join(seed.getMnemonicCode());
    }

    public void regenerate(String mnemonic, Context ctx) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        long created = preferences.getLong(PROP_CREATED, System.currentTimeMillis() / 1000);
        preferences.edit().putString(PROP_MNEMONIC, mnemonic).putLong(PROP_CREATED, created).apply();
        try {
            DeterministicSeed seed = new DeterministicSeed(mnemonic, null, "", created);
            instance = new LWallet(params, new KeyChainGroup(params, seed));
            instance.allowSpendingUnconfirmedTransactions();
            seed = instance.getKeyChainSeed();
            instance.rootKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
    }

    public String getMyRootPubKey() {
        if (rootKey != null) {
            byte[] pubKey = rootKey.getPubKey();
            return encodeM(pubKey);
        }
        return null;
    }

    public ECPublicKey pubKeyFromString(String s) {
        try {
            ECDomainParameters parameters = getDParams();
            byte[] f = decodeM(s);
            ECPublicKeyParameters params = new ECPublicKeyParameters(parameters.getCurve().decodePoint(f), parameters);
            ECNamedCurveSpec spec = new ECNamedCurveSpec("secp256k1", parameters.getCurve(), parameters.getG(), parameters.getN(), parameters.getH());
            return new JCEECPublicKey("ECIES", params, spec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String encrypt(ECPublicKey key, String s) throws Exception {
        ECPrivateKey privateKey = getMyPrivateKey();
        byte[] ivbuf0 = hmacSha256(s.getBytes("ASCII"), privateKey.getEncoded());
        byte[] ivbuf = slice(ivbuf0, 0, 16);
        byte[] kKem = kEkM(key, privateKey);
        byte[] ke = slice(kKem, 0, 32);
        byte[] km = slice(kKem, 32, 64);
        byte[] c = encryptAesCBC(ke, ivbuf, s.getBytes());
        byte[] dd = hmacSha256(c, km);
        byte[] tag = slice(dd, 0, 4); // short
        byte[][] encbuf = new byte[][]{c, tag};
        byte[] bytes = concatByteArrays(encbuf);
        return encodeM(bytes);
    }

    private byte[] ke = null;

    public String decrypt(ECPublicKey key, String s) throws Exception {
        byte[] decrypted;
        if (ke != null) {
            TimeMeter t = new TimeMeter("if oper ");
            byte[] encbuf = decodeM(s);
            t.end();
            int tagLength = 4;
            byte[] c = slice(encbuf, 0, encbuf.length - tagLength);

            try {
                decrypted = decryptAesCBC(ke, c);
                return new String(decrypted);
            } catch (Exception e) {
                e.printStackTrace();
                ke = null;
            }
        }

        ECPrivateKey privateKey = getMyPrivateKey();
        byte[] encbuf = decodeM(s);
        int tagLength = 4;
        byte[] c = slice(encbuf, 0, encbuf.length - tagLength);
        byte[] d = slice(encbuf, encbuf.length - tagLength, encbuf.length);
        byte[] kKem = kEkM(key, privateKey);
        ke = slice(kKem, 0, 32);
        byte[] km = slice(kKem, 32, 64);
        byte[] dd = hmacSha256(c, km);
        byte[] d2 = slice(dd, 0, 4); // shortTag
        for (int i = 0; i < d.length; i++) {
            if (d[i] != d2[i])
                throw new Exception("Invalid checksum, key: " + key + ", s:" + s);
        }
        decrypted = decryptAesCBC(ke, c);
        return new String(decrypted);
    }

    public String decrypt(ECPublicKey key, byte [] encbuf) throws Exception {
        byte[] decrypted;
        if (ke != null) {
            int tagLength = 4;
            byte[] c = slice(encbuf, 0, encbuf.length - tagLength);

            try {
                decrypted = decryptAesCBC(ke, c);
                return new String(decrypted);
            } catch (Exception e) {
                e.printStackTrace();
                ke = null;
            }
        }

        ECPrivateKey privateKey = getMyPrivateKey();
        int tagLength = 4;
        byte[] c = slice(encbuf, 0, encbuf.length - tagLength);
        byte[] d = slice(encbuf, encbuf.length - tagLength, encbuf.length);
        byte[] kKem = kEkM(key, privateKey);
        ke = slice(kKem, 0, 32);
        byte[] km = slice(kKem, 32, 64);
        byte[] dd = hmacSha256(c, km);
        byte[] d2 = slice(dd, 0, 4); // shortTag
        for (int i = 0; i < d.length; i++) {
            if (d[i] != d2[i])
                throw new Exception("Invalid checksum, key: " + key + ", s:" + encbuf);
        }
        decrypted = decryptAesCBC(ke, c);
        return new String(decrypted);
    }

    public ECPrivateKey getMyPrivateKey() throws Exception {
        if (rootKey == null) {
            return null;
        }
        BigInteger privKey = rootKey.getPrivKey();
        ECDomainParameters parameters = getDParams();
        ECPrivateKeySpec spec = new ECPrivateKeySpec(privKey, new ECParameterSpec(parameters.getCurve(), parameters.getG(), parameters.getN()));
        return new JCEECPrivateKey("ECIES", spec);
    }

    private ECDomainParameters getDParams() {
        ECNamedCurveParameterSpec ecParams = ECNamedCurveTable.getParameterSpec("secp256k1");
        return new ECDomainParameters(ecParams.getCurve(), ecParams.getG(), ecParams.getN(), ecParams.getH());
    }

    public String getXPub() {
        return getWatchingKey().serializePubB58(getParams());
    }

    public boolean isUnspentsAdded() {
        return myUnspents.size() > 0;
    }

    public void clear() {
        myUnspents.clear();
        clearTransactions(0);
    }

    public void addOutputs(JSONObject jo) {
        HashMap<Sha256Hash, LTransaction> map = new HashMap<>();
        if (jo.has("outs")) {
            JSONArray arr = jo.optJSONArray("outs");
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject ujo = arr.getJSONObject(i);
                    byte[] sb = decodeM(ujo.optString("script"));
                    Sha256Hash hash = Sha256Hash.wrap(decodeM(ujo.optString("hash")));
                    long index = ujo.optLong("index");
                    Coin value = Coin.valueOf(ujo.optLong("sat"));
                    LTransaction unspent = map.get(hash);
                    if (unspent == null) {
                        unspent = new LTransaction(getParams(), hash);
                        unspent.getConfidence().setConfidenceType(TransactionConfidence.ConfidenceType.BUILDING);
                    }
                    while (unspent.getOutputs().size() < index) {
                        unspent.addOutput(new TransactionOutput(getParams(), unspent, Coin.ZERO, new byte[]{}));
                    }
                    unspent.addOutput(new TransactionOutput(getParams(), unspent, value, sb));
                    map.put(unspent.getHash(), unspent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (jo.has("unspent_outputs")) {
            JSONArray arr = jo.optJSONArray("unspent_outputs");
            for (int i = 0; i < arr.length(); i++) {
                try {
                    JSONObject ujo = arr.getJSONObject(i);
                    byte[] sb = Hex.decode(ujo.optString("script"));
                    Sha256Hash hash = Sha256Hash.wrap(Hex.decode(ujo.optString("tx_hash")));
                    long index = ujo.optLong("tx_output_n");
                    Coin value = Coin.valueOf(ujo.optLong("value"));
                    LTransaction unspent = map.get(hash);
                    if (unspent == null) {
                        unspent = new LTransaction(getParams(), hash);
                        unspent.getConfidence().setConfidenceType(TransactionConfidence.ConfidenceType.BUILDING);
                    }
                    while (unspent.getOutputs().size() < index) {
                        unspent.addOutput(new TransactionOutput(getParams(), unspent, Coin.ZERO, new byte[]{}));
                    }
                    unspent.addOutput(new TransactionOutput(getParams(), unspent, value, sb));
                    map.put(unspent.getHash(), unspent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (LTransaction tx : map.values()) {
            for (TransactionOutput out : tx.getOutputs()) {
                if (out.getValue().getValue() == 0) continue;
                myUnspents.add(out);
            }
        }
    }

//    public Transaction makeTransaction(String to, Coin amt, byte[] info) throws AddressFormatException, InsufficientMoneyException {
//        Wallet.SendRequest request = Wallet.SendRequest.to(new Address(getParams(), to), amt);
//        Transaction tx = request.tx;
//        if (info != null)
//            tx.addOutput(Transaction.MIN_NONDUST_OUTPUT, new ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data(info).build());
//        completeTx(request);
//        commitTx(tx);
//        for (TransactionInput ti : tx.getInputs()) {
//            for (TransactionOutput tu : myUnspents) {
//                if (ti.getValue().getValue() == tu.getValue().getValue()) {
//                    myUnspents.remove(tu);
//                    break;
//                }
//            }
//        }
//        return tx;
//    }

    public String exportSeed() throws Exception {
        DeterministicSeed seed = getKeyChainSeed();
        if (seed == null || seed.getMnemonicCode() == null)
            throw new Exception("seed is not found");
        return Utils.join(seed.getMnemonicCode());
    }

    private ECKey getCurrKey() {
        Address address = currentReceiveAddress();
        for (ECKey key : getIssuedReceiveKeys()) {
            Address addr = key.toAddress(getParams());
            if (addr.equals(address)) {
                return key;
            }
        }
        return null;
    }

    public String sign(String msg) {
        ECKey key = getCurrKey();
        if (key == null) return null;
        return key.signMessage(msg);
    }

    public boolean verify(String msg, String sign) {
        ECKey key = getCurrKey();
        if (key == null) return false;
        try {
            key.verifyMessage(msg, sign);
            return true;
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    public class LTransaction extends Transaction {
        private final Sha256Hash hash;

        public LTransaction(final NetworkParameters params, final Sha256Hash hash) {
            super(params);
            this.hash = hash;
        }

        @Override
        public Sha256Hash getHash() {
            return hash;
        }
    }

    private static String encodeM(byte[] arr) {
//        return Base58.encode(arr);
        return Base58m.encode(arr);
    }

    public static byte[] decodeM(String s) {
//        return Base58.decode(s);
        return Base58m.decode(s);
    }
}
