package mobi.sender.tool;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import mobi.sender.model.User;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 24.03.14
 * Time: 18:02
 */
public class CtConnector {

    private static final String[] PROJECTION_CONTACT = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.PHOTO_ID
    };
    private static final String[] PROJECTION_PHONE = {
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
    };
    private static final String[] PROJECTION_EMAIL = {
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.DATA,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID
    };
    private static final String SELECTION_PHONE =
            ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
    private static SparseArray<User> hM1;
    private static SparseArray<String> hM2;

    public static String getNameById(Context ctx, long id) {
        final long t = System.currentTimeMillis();
        String name = null;
        Cursor cursor = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                ContactsContract.Contacts._ID + " = " + id, null, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        cursor.close();
        return name;
    }

//    public static void getAllContacts(final Context context, final ContactsCallback callback) {
//        Log.v(App.TAG, "begin contacts reading...");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                long t = System.currentTimeMillis();
//                List<ChatUser> chatUsers = getAllLocalContacts(context);
//                Log.v(App.TAG, "getAllContacts time " + (System.currentTimeMillis() - t));
//                callback.onContactsRead(chatUsers);
//            }
//        }).start();
//    }

    public static Bitmap getPhotoById(Context context, int contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    Bitmap photoBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    return photoBitmap;
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }


//    public static Bitmap getPhotoById(Context context, int photoId) {
//        String selection = ContactsContract.Data._ID + " = " + photoId;
//        Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.Contacts.Photo.PHOTO}, selection, null, null);
//        if (cursor.moveToFirst()) {
//            byte[] photo = cursor.getBlob(0);
//            Bitmap photoBitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
//            cursor.close();
//            return ImageHelper.getRoundedCornerBitmap(photoBitmap, 80);
//        }
//        cursor.close();
//        return null;
//    }

    public static String getMailById(Context ctx, long id) {
        long t = System.currentTimeMillis();
        String mail = null;
        Cursor cursor = ctx.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                new String[]{String.valueOf(id)}, null);

        int emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

        if (cursor.moveToFirst()) {
            mail = cursor.getString(emailIdx);
        }
        cursor.close();
//        Log.v(App.TAG, "getMailById time "+(System.currentTimeMillis()-t));
        return mail;
    }

    public static String getMyName(Context ctx) {
        Cursor c = ctx.getContentResolver().query(ContactsContract.Profile.CONTENT_URI,
                null, null, null, null);
        String name = "Anonymous";
        if (c != null && c.moveToFirst()) {
            String dbName = c.getString(c.getColumnIndexOrThrow("display_name"));
            if (dbName != null && dbName.length() > 0) name = dbName;
            //photo_thumb_uri = photo uri
        }
        if (c != null) {
            c.close();
        }
        return name;
    }

//    public static List<User> getAllLocalContacts(final Context context) {
//        Thread t1 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                hM1 = getAllPhoneNumbers(context);
//            }
//        });
//        Thread t2 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                hM2 = getAllEmails(context);
//            }
//        });
//        long t = System.currentTimeMillis();
//        t1.start();
//        t2.start();
//        try {
//            t1.join();
//            t2.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        List<User> chatUsers = new ArrayList<User>();
//        for (int i = 0; i < hM1.size(); i++) {
//            int key = hM1.keyAt(i);
//            User cu = hM1.get(key);
//            String mail = hM2.get(key);
//            if (mail != null) cu.setMail(mail);
//            chatUsers.add(cu);
//        }
//        return chatUsers;
//    }

//    public static SparseArray<User> getAllPhoneNumbers(Context context) {
//
//        SparseArray<User> array = new SparseArray<>();
//
//        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                PROJECTION_PHONE, null, null,
//                ContactsContract.CommonDataKinds.Phone._ID + " ASC");
//
//        if (cursor.moveToFirst()) {
//            do {
//                User cu = new User();
//                String number = cursor.getString(0);
//                String name = cursor.getString(1);
//                int id = cursor.getInt(2);
//                if (number == null || number.length() < 7) continue;
//                cu.setName(name);
//                cu.setLocalId(String.valueOf(id));
//                cu.setPhone(number);
//                array.put(id, cu);
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        return array;
//    }

    public static List<User> getLocalUsers(Context context) {

        List<User> array = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION_PHONE, null, null,
                ContactsContract.CommonDataKinds.Phone._ID + " ASC");

        if (cursor.moveToFirst()) {
            do {
                User cu = new User();
                String number = cursor.getString(0);
                String name = cursor.getString(1);
                int id = cursor.getInt(2);
                if (number == null || number.length() < 7) continue;
                cu.setName(name);
                cu.setLocalId(String.valueOf(id));
                cu.setRawPhone(number);
                array.add(cu);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return array;
    }

    private static SparseArray<String> getAllEmails(final Context context) {

        SparseArray<String> hashMap = new SparseArray<String>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                PROJECTION_EMAIL, null, null,
                ContactsContract.CommonDataKinds.Email._ID + " ASC");
        while (cursor.moveToNext()) {
//                String address = cursor.getString(0);
            String data = cursor.getString(1);
            int id = cursor.getInt(2);
            if (data == null || !data.contains("@")) continue;
            hashMap.put(id, data);
        }
        cursor.close();
        return hashMap;
    }

    public static void addUserToContactBook(final Context ctx, User u, byte[] icon) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        Tool.log("+=====+" + u.getName() + " " + u.getPhone() + " " + icon.length);
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME,null )
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, u.getPhone())
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, u.getName())
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, icon)
                .build());
        try {
            ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isPhoneExists(Context context, String phone) {
        boolean result;
        String[] selectionArgs = {phone};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION_PHONE, SELECTION_PHONE, selectionArgs, null);
        result = cursor.moveToFirst();
        cursor.close();
        return result;
    }

    private static ConcurrentHashMap<Integer, Bitmap> getAllPhotos(final Context context) {

        final ConcurrentHashMap<Integer, Bitmap> cHashMap = new ConcurrentHashMap<Integer, Bitmap>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                PROJECTION_CONTACT, null, null,
                ContactsContract.Contacts._ID + " ASC");
        ExecutorService executor = Executors.newFixedThreadPool(50);
        if (cursor.moveToFirst()) {
            do {
                final int id = cursor.getInt(1);
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap photo = getPhotoById(context, id);
                        cHashMap.put(id, photo);
                    }
                });
            } while (cursor.moveToNext());
        }
        executor.shutdown();
        cursor.close();
        try {
            executor.awaitTermination(200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return cHashMap;
    }

    public interface IconLoadListener {
        void onLoad(Bitmap bmp);
    }

}
