package mobi.sender.tool;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import mobi.sender.R;
import mobi.sender.model.ChatBased;
import mobi.sender.model.User;
import mobi.sender.tool.utils.ConvertUtils;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 12.02.14
 * Time: 16:47
 */
public class Tool {

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private static final Integer[] EMS = new Integer[]{
            0x263a, 0x1f60a, 0x1f600, 0x1f601, 0x1f602, 0x1f603, 0x1f604, 0x1f605,
            0x1f606, 0x1f607, 0x1f608, 0x1f609, 0x1f62f, 0x1f610, 0x1f611, 0x1f615,
            0x1f620, 0x1f62c, 0x1f621, 0x1f622, 0x1f634, 0x1f62e, 0x1f623, 0x1f624,
            0x1f625, 0x1f626, 0x1f627, 0x1f628, 0x1f629, 0x1f630, 0x1f61f, 0x1f631,
            0x1f632, 0x1f633, 0x1f635, 0x1f636, 0x1f637, 0x1f61e, 0x1f612, 0x1f60d,
            0x1f61b, 0x1f61c, 0x1f61d, 0x1f60b, 0x1f617, 0x1f619, 0x1f618, 0x1f61a,
            0x1f60e, 0x1f62d, 0x1f60c, 0x1f616, 0x1f614, 0x1f62a, 0x1f60f, 0x1f613,
            0x1f62b, 0x1f64b, 0x1f64c, 0x1f64d, 0x1f645, 0x1f646, 0x1f647, 0x1f64e,
            0x1f64f, 0x1f63a, 0x1f63c, 0x1f638, 0x1f639, 0x1f63b, 0x1f63d, 0x1f63f,
            0x1f63e, 0x1f640, 0x1f648, 0x1f649, 0x1f64a, 0x1f4a9, 0x1f476, 0x1f466,
            0x1f467, 0x1f468, 0x1f469, 0x1f474, 0x1f475, 0x1f48f, 0x1f491, 0x1f46a,
            0x1f46b, 0x1f46c, 0x1f46d, 0x1f464, 0x1f465, 0x1f46e, 0x1f477, 0x1f481,
            0x1f482, 0x1f46f, 0x1f470, 0x1f478, 0x1f385, 0x1f47c, 0x1f471, 0x1f472,
            0x1f473, 0x1f483, 0x1f486, 0x1f487, 0x1f485, 0x1f47b, 0x1f479, 0x1f47a,
            0x1f47d, 0x1f47e, 0x1f47f, 0x1f480, 0x1f4aa, 0x1f440, 0x1f442, 0x1f443,
            0x1f463, 0x1f444, 0x1f445, 0x1f48b, 0x2764, 0x1f499, 0x1f49a, 0x1f49b,
            0x1f49c, 0x1f493, 0x1f494, 0x1f495, 0x1f496, 0x1f497, 0x1f498, 0x1f49d,
            0x1f49e, 0x1f49f, 0x1f44d, 0x1f44e, 0x1f44c, 0x270a, 0x270c, 0x270b,
            0x1f44a, 0x261d, 0x1f446, 0x1f447, 0x1f448, 0x1f449, 0x1f44b, 0x1f44f,
            0x1f450
    };
    private static final ArrayList<String> emsList = new ArrayList<>();
    static Random rnd = new Random();

    static {
        for (Integer i : EMS) {
            final String s = new String(Character.toChars(i));
            emsList.add(s);
        }
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static void log(String s) {
        int maxLogSize = 400;
        if (s.length() > maxLogSize) {
            for (int i = 0; i <= s.length() / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i + 1) * maxLogSize;
                end = end > s.length() ? s.length() : end;
                Log.v("Sender", s.substring(start, end));
            }
        } else {
            Log.v("Sender", s);
        }
    }

    public static String md5(String input) {
        String md5 = null;
        try {
            StringBuilder code = new StringBuilder();
            java.security.MessageDigest messageDigest = java.security.MessageDigest.getInstance("MD5");
            byte bytes[] = input.getBytes();
            byte digest[] = messageDigest.digest(bytes);

            for (int i = 0; i < digest.length; ++i) {
                code.append(Integer.toHexString(0x0100 + (digest[i] & 0x00FF)).substring(1));
            }
            md5 = code.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5;
    }

    public static int getExifRotation(String path) {
        ExifInterface exif;
        int orientation = 0;
        try {
            exif = new ExifInterface(path);
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int rotation = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }
        return rotation;
    }


    public static Bitmap extractThumbnail1(Context ctx, String fileName, int w, int h) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inScaled = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            if (w <= 0 && h <= 0) {
                options.inJustDecodeBounds = false;
            } else {
                options.inJustDecodeBounds = true;
                int coefW = 0;
                int coefH = 0;
                while (options.outWidth / (Math.pow(2, coefW)) > w && w != -1) {
                    coefW++;
                }
                while (options.outHeight / (Math.pow(2, coefH)) > h && h != -1) {
                    coefH++;
                }
                options.inSampleSize = (int) Math.pow(2, Math.min(coefW, coefH) - 1);
                options.inJustDecodeBounds = false;
            }

            Bitmap bmp = fileName.startsWith("content") || fileName.startsWith("file") ? BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(Uri.parse(fileName)), null, options) : BitmapFactory.decodeFile(fileName, options);

            final int maxSize = 1024;
            int inWidth = bmp.getWidth();
            int inHeight = bmp.getHeight();
            int outWidth = inWidth;
            int outHeight = inHeight;
            if(inWidth > maxSize || inHeight > maxSize) {
                if (inWidth > inHeight) {
                    outWidth = maxSize;
                    outHeight = (inHeight * maxSize) / inWidth;
                } else {
                    outHeight = maxSize;
                    outWidth = (inWidth * maxSize) / inHeight;
                }
            }

            return Bitmap.createScaledBitmap(bmp, outWidth, outHeight, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] extractThumbnail2(Bitmap bmp) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] extractThumbnail(Context ctx, String fileName, int w, int h) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inScaled = true;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            if (w <= 0 && h <= 0) {
                options.inJustDecodeBounds = false;
            } else {
                options.inJustDecodeBounds = true;
                int coefW = 0;
                int coefH = 0;
                while (options.outWidth / (Math.pow(2, coefW)) > w && w != -1) {
                    coefW++;
                }
                while (options.outHeight / (Math.pow(2, coefH)) > h && h != -1) {
                    coefH++;
                }
                options.inSampleSize = (int) Math.pow(2, Math.min(coefW, coefH) - 1);
                options.inJustDecodeBounds = false;
            }

            Bitmap bmp = fileName.startsWith("content") || fileName.startsWith("file") ? BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(Uri.parse(fileName)), null, options) : BitmapFactory.decodeFile(fileName, options);

            final int maxSize = 1024;
            int outWidth;
            int outHeight;
            int inWidth = bmp.getWidth();
            int inHeight = bmp.getHeight();
            if(inWidth > inHeight){
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bmp, outWidth, outHeight, false);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] stream2Bytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static InputStream http2Stream(String url) {
        try {
            String url2 = ConvertUtils.encodeString(url);

            HttpURLConnection conn = (HttpURLConnection) new URL(url2).openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            return conn.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVersion(Context ctx) {
        String ver = "1.0";
        try {
            ver = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return ver;
    }

    public static String getImei(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String stored = pref.getString("syn_imei", null);
        if (stored == null) {
            final TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            String tmDevice = "";
            String tmSerial = "", androidId = "";

            // IMEI (International Mobile Equipment Identity)
            if (tm.getDeviceId() != null)
                tmDevice = "" + tm.getDeviceId();

            // Серийный номер сим карты
            if (tm.getSimSerialNumber() != null)
                tmSerial = "" + tm.getSimSerialNumber();

            // Уникальный порядковый номер устройства с андроидом на борту у Google
            if (android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID) != null)
                androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            // http://ru.wikipedia.org/wiki/UUID
            // UUID (Universally Unique Identifier)
            // http://www.ietf.org/rfc/rfc4122.txt
            // Спариваем хэшкоды устройства, имея и серийного номера симкарты
            // СДВИГ на 32бита и побитовое "ИЛИ"
            // androidId.hashCode() = (a 32-bit signed integer)
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            String deviceId = deviceUuid.toString();
            pref.edit().putString("syn_imei", deviceId).apply();
            return deviceId;
        } else {
            return stored;
        }
    }

    public static boolean isOnline(Context context) {
        NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static boolean isWifi(Context context) {
        NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String num = mTelephonyMgr.getLine1Number();
        return fillPhoneNumber(num);
    }

    public static String fillPhoneNumber(String num) {
        try {
            if (num != null && num.length() > 0) {
                if (num.length() >= 9) {
                    num = num.replaceAll("\\D", "");
                    if (num.substring(0, 1).equals("8")) {
                        num = "+3" + num;
                    } else if (num.substring(0, 1).equals("0")) {
                        num = "+38" + num;
                    } else if (num.substring(0, 1).equals("3")) {
                        num = "+" + num;
                    }
                }
                return num;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getRealImei(Context ctx) {
        String device_id = "";
        try {
            TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            device_id = tm.getDeviceId();
            return device_id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return device_id;
    }


    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String getCountryCode(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso().toUpperCase();
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static boolean isMediaMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static void disconnectIncomingCall() {
        try {
            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";
            Class<?> telephonyClass;
            Class<?> telephonyStubClass;
            Class<?> serviceManagerClass;
            Class<?> serviceManagerNativeClass;
            Method telephonyEndCall;
            Object telephonyObject;
            Object serviceManagerObject;
            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.invoke(telephonyObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getPlaceholder(ChatBased c) {
        return (c instanceof User) ? (((User) c).isCompany() ? R.drawable.ic_business_bg : R.drawable.ic_acc_bg) : R.drawable.ic_group_bg;
    }

    public static boolean isP2PChat(String chatId) {
        return chatId.startsWith(User.P2P_CHAT_PREFIX);
    }

    public static String getUserId(String chatId) {
        return chatId.replace(User.P2P_CHAT_PREFIX, "");
    }

    public static boolean isEmptyString(String str) {
        return str == null || str.length() == 0;
    }

    public static void setItemInFragment(Context ctx, View view, int itemId, int imageId, int textId) {
        View itemView = view.findViewById(itemId);
        ImageView itemImage = (ImageView) itemView.findViewById(R.id.imageView);
        itemImage.setBackgroundResource(imageId);
        TextView itemText = (TextView) itemView.findViewById(R.id.tv_badge_text);
        itemText.setText(ctx.getString(textId));
    }

    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static String httpGet(final String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String l;
            StringBuilder sb = new StringBuilder();
            do {
                l = reader.readLine();
                sb.append(l);
            } while (l != null);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void http2ByteArray(final String url, LoadListener ll) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            is = new URL(url).openStream();
            byte[] byteChunk = new byte[4096];
            int n;
            while ((n = is.read(byteChunk)) > 0) {
                baos.write(byteChunk, 0, n);
            }
            baos.flush();
            ll.onLoad(baos.toByteArray());
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File createImageFile() throws IOException {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            storageDir = new File(storageDir.getAbsolutePath(), "sender");
            File image = null;
            storageDir.mkdirs();
            if (storageDir.exists()) {
                image = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
                Tool.log("name = " + image.getName());
                Tool.log("path = " + image.getAbsolutePath());
            } else {
                Tool.log("Can't create folders for :" + storageDir.getAbsolutePath());
            }
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String saveBytesAsTempFile(Context context, byte[] data, String ext) {
        try {
            File f = File.createTempFile(
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()),
                    "." + ext,
                    context.getExternalCacheDir()
            );

            Tool.log("name 1 = " + f.getName());
            Tool.log("path 1 = " + f.getAbsolutePath());
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
            stream.write(data);
            stream.flush();
            stream.close();
            return f.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> getEms() {
        return emsList;
    }

    public static Integer[] getEmsInt() {
        return EMS;
    }

    public static boolean isSmile(String s) {
        if (s.length() < 3)
            return emsList.contains(s);
        return false;
    }

    public static String getRandomFileName(String ext) throws Exception {
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            throw new Exception("media not mounted");
        }
        String dirType;
        if ("mp3".equalsIgnoreCase(ext)) {
            dirType = Environment.DIRECTORY_MUSIC;
        } else if ("mp4".equalsIgnoreCase(ext)) {
            dirType = Environment.DIRECTORY_MOVIES;
        } else if ("png".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext) || "jpg".equalsIgnoreCase(ext)) {
            dirType = Environment.DIRECTORY_PICTURES;
        } else {
            throw new Exception("unknown file extension");
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(dirType), "Sender");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                throw new Exception("dir not created");
            }
        }
        return mediaStorageDir.getPath() + File.separator + System.currentTimeMillis() + "." + ext;
    }

    public static void loadImage(Context ctx, String url, ImageView view, int placeholder, boolean isTransform) {
        if (url == null || url.length() == 0) {
            if (placeholder != 0) view.setImageResource(placeholder);
            return;
        }
        if (placeholder != 0) {     //with placeholder
            if (isTransform) {      //circle
                Picasso.with(ctx)
                        .load(url)
                        .placeholder(placeholder)
                        .transform(new CircleTransform())
                        .into(view);
            } else {
                Picasso.with(ctx)
                        .load(url)
                        .placeholder(placeholder)
                        .into(view);
            }

        } else {                    //without placeholder
            if (isTransform) {      //circle
                Picasso.with(ctx)
                        .load(url)
                        .transform(new CircleTransform())
                        .into(view);
            } else {
                Picasso.with(ctx)
                        .load(url)
                        .into(view);
            }
        }
    }

    public static void loadImageLocal(Context ctx, String url, ImageView view, int placeholder) {
        Picasso.with(ctx)
                .load(new File(url))
                .placeholder(placeholder)
                .into(view);
    }

    public static double getDiagonal(Activity act) {
        DisplayMetrics dm = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        return Math.sqrt(x + y);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static String parseLongToDate(Context ctx, long date) {
        return DateFormat.getTimeFormat(ctx).format(new Date(date));
    }

    public static String parseLongToDate2(Context ctx, long date) {
        return DateFormat.getDateFormat(ctx).format(new Date(date));
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void checkEmojiSupport(Context context, TextView textView) {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSansEmoji.ttf");
            textView.setTypeface(tf);
        }
    }

    public static int getFileSize(Context context, Uri uri) {
        int size = -1;
        try {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getInt(sizeIndex);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return size;
    }

    public interface LoadListener {
        void onLoad(byte[] data);
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    @Nullable
    public static Bitmap stringToQrCodeBitmap(String contents, BarcodeFormat format, int dimension) throws WriterException {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;
        if (contents == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(contents, format, dimension, dimension, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static CharSequence parseSmiles(CharSequence text) {
        CharSequence result = "";
        if (text == null) return result;
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            return text;
        for (int i = 0; i < text.length(); i++)
            if (isDoubleChar(text.charAt(i)) || emsList.contains(new Integer(text.charAt(i))))
                result = TextUtils.concat(TextUtils.concat(result, text.subSequence(result.length(), i)), /*getSpannableText(*/isDoubleChar(text.charAt(i)) ? text.subSequence(i++, i + 1) : text.subSequence(i, i + 1)/*)*/);
        return result.equals("") ? text : result;
    }

    public static boolean isColorDark(int color) {
        return 1-(0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 >= 0.5;
    }

    private static boolean isDoubleChar(char ch) {
        return (int) ch == 55357 || (int) ch == 55356;
    }

  /*  private static SpannableString getSpannableText(CharSequence sequence) {
        SpannableString span = new SpannableString(sequence);
        span.setSpan(new CustomTypefaceSpan("sans-serif", TypefaceUtils.get(TypefaceUtils.Type.symbolaemoji)), 0, span.length(), 0);
        return span;
    }*/
}
