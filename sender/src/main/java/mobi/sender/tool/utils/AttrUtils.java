package mobi.sender.tool.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

/**
 * Created by Zver on 08.02.2017.
 */

public class AttrUtils {

    public static int getColor(Context ctx, int attr){
        TypedArray typedArray = ctx.obtainStyledAttributes(new int[]{attr});
        int backgroundResource = typedArray.getResourceId(0, 0);
        typedArray.recycle();
        return backgroundResource;
    }

    public static Drawable getDrawableByAttr(Context context, int idAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{idAttr});
        int attributeResourceId = a.getResourceId(0, 0);
        Drawable drawable = context.getResources().getDrawable(attributeResourceId);
        a.recycle();
        return drawable;
    }

    public static int getDrawableIdByAttr(Context context, int idAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{idAttr});
        int resourceId = a.getResourceId(0, 0);
        a.recycle();
        return resourceId;
    }
}
