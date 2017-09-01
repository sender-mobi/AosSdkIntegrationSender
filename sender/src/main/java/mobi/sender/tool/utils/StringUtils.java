package mobi.sender.tool.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Created by Zver on 04.11.2016.
 */

public class StringUtils {

     public static void setStringToBuffer(Context ctx, String str){
         ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
         ClipData clip = ClipData.newPlainText("label", str);
         clipboard.setPrimaryClip(clip);
     }
}
