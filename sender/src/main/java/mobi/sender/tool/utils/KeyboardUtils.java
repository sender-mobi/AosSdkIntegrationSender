package mobi.sender.tool.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import mobi.sender.tool.Tool;

/**
 * Created by Zver on 28.11.2016.
 */

public class KeyboardUtils {

    //once triggered  (not work on some device)
    public static void isKeyboardShownListener(final View parentLayout, final OnKeyboardShownListener listener) {
        final boolean[] isOpened = {false};

        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = parentLayout.getRootView().getHeight() - parentLayout.getHeight();
                if (heightDiff > 100) { // 99% of the time the height diff will be due to a keyboard.
                    listener.onShown(true);
                    isOpened[0] = true;
                } else if (isOpened[0]) {
                    listener.onShown(false);
                    isOpened[0] = false;
                }
            }
        });
    }

    //twice triggered
    public static void isKeyboardShownListener2(final View parentLayout, final OnKeyboardShownListener listener) {

        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                parentLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = parentLayout.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
                    listener.onShown(true);

                } else {
                    // keyboard is closed
                    listener.onShown(false);
                }

            }
        });
    }



    public static void checkKeyboardHeight(final View parentLayout, final Activity parent, final OnKeyboardShownListener listener) {
        final int[] previousHeightDiffrence = {0};
        final boolean[] keyBoardVisible = new boolean[1];

        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);

                        int screenHeight;
                        if (Build.VERSION.SDK_INT >= 5.0) {
                            screenHeight = calculateScreenHeightForLollipop(parent);
                        } else {
                            screenHeight = parentLayout.getRootView().getHeight();
                        }

                        int heightDifference = screenHeight - (r.bottom - r.top);

                        int resourceId = parent.getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            heightDifference -= parent.getResources().getDimensionPixelSize(resourceId);
                        }

                        if (previousHeightDiffrence[0] - heightDifference > 50) {
//                            popupWindow.dismiss();
                        }

                        previousHeightDiffrence[0] = heightDifference;

                        boolean lastVisible = keyBoardVisible[0];

                        if (heightDifference > 100) {
                            keyBoardVisible[0] = true;
                            listener.onShown(true);
                        } else {
                            keyBoardVisible[0] = false;

                            if (lastVisible) {
                                listener.onShown(false);
                            }
                        }
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private static int calculateScreenHeightForLollipop(Activity parent) {
        WindowManager wm = (WindowManager) parent.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }




    public static void visibleKeyboard(boolean isVisible, View et, Context ctx) {
        if (isVisible) {
            InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        } else {
            InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        }
    }

    //Interfaces
    public interface OnKeyboardShownListener {
        void onShown(boolean isShown);
    }
}
