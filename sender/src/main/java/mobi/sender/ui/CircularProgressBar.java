package mobi.sender.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import mobi.sender.R;
import mobi.sender.tool.Tool;

/**
 * Created by Ptitsyn A.
 */
public class CircularProgressBar extends View {
    public static final int MODE_DOUBLE_CLICK = 0;
    public static final int MODE_HOLD = 1;
    public static final int MODE_MANUAL = 2;
    private static final String TAG = ">>> CircularProgressBar";
    private RectF mCircleBounds;

    private Paint paint;

    private float progress;

    private int mStrokeWidth;

    private int mInnerPadding;

    private Drawable src;

    private int mStrokeColor;

    private ObjectAnimator oaProgress;

    private int mDuration;

    private int modeClick;

    private Rect rLayout;

    private Rect rBitmap;
    private ValueAnimator.AnimatorUpdateListener updateProgressListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float progress = (Float) animation.getAnimatedValue();
            if (progress != CircularProgressBar.this.progress) {
                setProgress(progress);
            }
        }
    };
    private OnProgressListener onProgressListener;
    private Animator.AnimatorListener progressListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (onProgressListener != null) {
                onProgressListener.onStartProgress();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            setProgress(0);
            if (onProgressListener != null) {
                onProgressListener.onStopProgress();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            setProgress(0);
            if (onProgressListener != null) {
                onProgressListener.onStopProgress();
            }
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    public CircularProgressBar(Context context) {
        this(context, null);
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBar, defStyle, 0);
        progress = a.getInt(R.styleable.CircularProgressBar_progress, 0);
        mInnerPadding = a.getDimensionPixelOffset(R.styleable.CircularProgressBar_innerPadding, 0);
        mStrokeWidth = a.getDimensionPixelOffset(R.styleable.CircularProgressBar_width_stroke, 0);
        mStrokeColor = a.getColor(R.styleable.CircularProgressBar_stroke_color, Color.GRAY);
        mDuration = a.getInt(R.styleable.CircularProgressBar_duration, 60000);
        modeClick = a.getInt(R.styleable.CircularProgressBar_mode_click, MODE_HOLD);

        paint = new Paint();
        paint.setColor(mStrokeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mStrokeWidth);
        paint.setAntiAlias(true);

        src = a.getDrawable(R.styleable.CircularProgressBar_src);

        a.recycle();

        init();
    }

    public CircularProgressBar(Context context, int innerPadding, int strokeWidth, int colorId, int duration, int mode, int drawableId) {
        super(context);

        progress = 0;
        mInnerPadding = (int) Tool.convertDpToPixel(innerPadding, context);
        mStrokeWidth = (int) Tool.convertDpToPixel(strokeWidth, context);
        mStrokeColor = context.getResources().getColor(colorId);
        mDuration = duration;
        modeClick = mode;
        src = getResources().getDrawable(drawableId);

        init();
    }

    private void init() {
        mCircleBounds = new RectF();
        paint = new Paint();
        paint.setColor(mStrokeColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mStrokeWidth);
        paint.setAntiAlias(true);

        setClickable(true);
        rLayout = new Rect();
        rBitmap = new Rect();

        if (src != null) {
            rBitmap = new Rect(0, 0, ((BitmapDrawable) src.getCurrent()).getBitmap().getWidth(), ((BitmapDrawable) src.getCurrent()).getBitmap().getHeight());
        }
        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                src.setState(PRESSED_ENABLED_STATE_SET);
                if ((oaProgress == null || !oaProgress.isRunning())) {
                    animateProgressTo(0, mDuration);
                }
                return false;

            }
        });
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (modeClick) {
                    case MODE_DOUBLE_CLICK: {
                        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            src.setState(ENABLED_STATE_SET);
                            if (oaProgress != null && oaProgress.isRunning()) {
                                oaProgress.cancel();
                            } else {
                                animateProgressTo(0, mDuration);
                            }
                        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            src.setState(PRESSED_ENABLED_STATE_SET);
                        }
                    }
                    break;
                    case MODE_HOLD: {
                        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                            src.setState(ENABLED_STATE_SET);
                            if (oaProgress != null && oaProgress.isRunning()) {
                                oaProgress.cancel();
                            }
                        }
                        invalidate();
                    }
                    break;
                    case MODE_MANUAL: {

                    }
                    break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float angle = 360 * (progress / mDuration);
        if (src != null) {
            canvas.drawBitmap(((BitmapDrawable) src.getCurrent()).getBitmap(), rBitmap,
                    rLayout, null);
        }

        canvas.drawArc(mCircleBounds, -90, angle, false, paint);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (src != null && (src.getIntrinsicWidth() < widthSize || src.getIntrinsicHeight() < heightSize)) {
            setMeasuredDimension(src.getIntrinsicWidth(), src.getIntrinsicHeight());
        } else {
            setMeasuredDimension(widthSize, heightSize);
        }

        rLayout.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

        mCircleBounds.set(mInnerPadding, mInnerPadding, getMeasuredWidth() - mInnerPadding, getMeasuredHeight() - mInnerPadding);
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public OnProgressListener getiProgress() {
        return onProgressListener;
    }

    public void setProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    public void startProgress() {
        animateProgressTo(0, mDuration);
    }

    public void resume() {
        if (oaProgress != null && !oaProgress.isRunning()) {
            animateProgressTo((int) progress, mDuration);
        }
    }

    public void pause() {
        if (oaProgress != null && oaProgress.isRunning()) {
            oaProgress.cancel();
        }
    }

    public void animateProgressTo(final int start, final int end) {
        if (start != 0) {
            setProgress(start);
        }

        oaProgress = ObjectAnimator.ofFloat(this, "progress", start, end);
        oaProgress.setDuration(end - start);
        oaProgress.setInterpolator(new LinearInterpolator());
        oaProgress.addListener(progressListener);

        oaProgress.addUpdateListener(updateProgressListener);

        oaProgress.start();
    }

    public void setProgressListener(Animator.AnimatorListener progressListener) {
        this.progressListener = progressListener;
    }

    public void setUpdateProgressListener(ValueAnimator.AnimatorUpdateListener updateProgressListener) {
        this.updateProgressListener = updateProgressListener;
    }

    public static interface OnProgressListener {
        void onStartProgress();

        void onStopProgress();
    }
}