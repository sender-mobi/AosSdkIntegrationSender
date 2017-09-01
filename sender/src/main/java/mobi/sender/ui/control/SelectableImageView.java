package mobi.sender.ui.control;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import mobi.sender.R;

/**
 * Created by vd on 8/15/16.
 */
public class SelectableImageView extends ImageView {

    private boolean isSelected;
    private Bitmap check;
    private Bitmap uncheck;
    private Paint paint = new Paint(0);

    public SelectableImageView(Context context) {
        super(context);
        init();
    }

    public SelectableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SelectableImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        check = BitmapFactory.decodeResource(getResources(), R.drawable.check, null);
        uncheck = BitmapFactory.decodeResource(getResources(), R.drawable.uncheck, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Bitmap bitmap;
        if (isSelected) {
            bitmap = check;
        } else {
            bitmap = uncheck;
        }
        int s = bitmap.getWidth();
        int h = getLayoutParams().height / 12;
        int w = getLayoutParams().width - h - s;
        canvas.drawBitmap(bitmap, w, h, paint);
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void setSelected(boolean selected) {
        isSelected = selected;
        invalidate();
    }
}
