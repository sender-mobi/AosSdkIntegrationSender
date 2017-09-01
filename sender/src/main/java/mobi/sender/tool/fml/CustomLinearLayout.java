package mobi.sender.tool.fml;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Ptitsyn A.
 */
public class CustomLinearLayout extends LinearLayout {

    public CustomLinearLayout(Context context) {
        this(context, null);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getOrientation() == HORIZONTAL) {
            boolean isCustom = false;
            for (int i = 0; i < getChildCount(); i++) {
                LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
                if ((lp.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.LEFT || (lp.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT || (lp.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
                    isCustom = true;
                    break;
                }
            }
            if (isCustom) {
                onLayoutHorizontal(l, t, r, b);
            } else {
                super.onLayout(changed, l, t, r, b);
            }
        } else {
            boolean isCustom = false;
            for (int i = 0; i < getChildCount(); i++) {
                LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
                if ((lp.gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP || (lp.gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM || (lp.gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.CENTER_VERTICAL) {
                    isCustom = true;
                }
            }
            if (isCustom) {
                onLayoutVertical(l, t, r, b);
            } else {
                super.onLayout(changed, l, t, r, b);
            }
        }
    }

    private void onLayoutVertical(int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int right = r - l;
        int top = getPaddingTop();
        int bottom = b - t;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams lpChild = (LayoutParams) child.getLayoutParams();

            int leftChild = left;
            int topChild;

            switch (lpChild.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.LEFT: {
                    leftChild = left + lpChild.leftMargin;
                }
                break;
                case Gravity.RIGHT: {
                    leftChild = right - child.getMeasuredWidth() - lpChild.rightMargin;
                }
                break;
                case Gravity.CENTER_HORIZONTAL: {
                    leftChild = (r - l) / 2 - child.getMeasuredWidth() / 2 + lpChild.leftMargin - lpChild.rightMargin;
                }
                break;
            }

            switch (lpChild.gravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.TOP: {
                    topChild = top + lpChild.topMargin;
                    top += child.getMeasuredHeight();
                }
                break;
                case Gravity.BOTTOM: {
                    topChild = bottom - child.getMeasuredHeight() + lpChild.topMargin;
                    bottom -= child.getMeasuredHeight();
                }
                break;
                case Gravity.CENTER_VERTICAL: {
                    topChild = ((b - t) - child.getMeasuredHeight()) / 2;
                }
                break;
                default: {
                    topChild = top + lpChild.topMargin;
                    top += child.getMeasuredHeight();
                }
                break;
            }
            child.layout(leftChild, topChild, leftChild + child.getMeasuredWidth(), topChild + child.getMeasuredHeight());
        }
    }

    private void onLayoutHorizontal(int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int right = r - l;
        int top = getPaddingTop();
        int bottom = b - t;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams lpChild = (LayoutParams) child.getLayoutParams();

            int leftChild;
            int topChild = top;

            switch (lpChild.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.LEFT: {
                    leftChild = left + lpChild.leftMargin;
                    left += child.getMeasuredWidth();
                }
                break;
                case Gravity.RIGHT: {
                    leftChild = right - child.getMeasuredWidth() - lpChild.rightMargin;
                    right -= child.getMeasuredWidth();
                }
                break;
                case Gravity.CENTER_HORIZONTAL: {
                    leftChild = (r - l) / 2 - child.getMeasuredWidth() / 2 + lpChild.leftMargin - lpChild.rightMargin;
                }
                break;
                default: {
                    leftChild = left + lpChild.leftMargin;
                    left += child.getMeasuredWidth();
                }
                break;
            }

            switch (lpChild.gravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.TOP: {
                    topChild = top + lpChild.topMargin;
                }
                break;
                case Gravity.BOTTOM: {
                    topChild = bottom - child.getMeasuredHeight() + lpChild.topMargin;
                }
                break;
                case Gravity.CENTER_VERTICAL: {
                    topChild = (bottom - child.getMeasuredHeight()) / 2;
                }
                break;
            }
            child.layout(leftChild, topChild, leftChild + child.getMeasuredWidth(), topChild + child.getMeasuredHeight());
        }
    }}
