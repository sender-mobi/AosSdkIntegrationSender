package mobi.sender.tool.fml;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.P24enableFullVerReq;
import mobi.sender.model.ChatBased;
import mobi.sender.model.User;
import mobi.sender.tool.BtcFasade;
import mobi.sender.tool.LWallet;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.ui.adapter.MsgRecAdapter;
import mobi.sender.ui.window.map.MapWindow;

/**
 * Created
 * by vp
 * on 15.10.14.
 */
public class FMLRenderer {

    private static final String TYPE_ROW = "row";
    private static final String TYPE_COL = "col";
    private static final String TYPE_TEXT = "text";
    private static final String TYPE_EDIT = "edit";
    private static final String TYPE_TEXTAREA = "tarea";
    private static final String TYPE_IMG = "img";
    private static final String TYPE_CHECK = "check";
    private static final String TYPE_RADIO = "radio";
    private static final String TYPE_SELECT = "select";
    private static final String TYPE_MAP = "map";
    private static final String TYPE_VIDEO = "video";
    private static final String TYPE_BUTTON = "button";
    private static final String TYPE_ACOMPLETE = "completeselect";
    private static final String STATE_DISABLE = "disable";
    private static final String STATE_INVISIBLE = "invisible";
    private static final String STATE_GONE = "gone";
    private static final String NAME_ROOT = "root";
    private ExecutorService pool = Executors.newCachedThreadPool();
    private String mChatId;
    private MsgRecAdapter.GetActivity mLis;

    public FMLRenderer(String chatId, MsgRecAdapter.GetActivity listener) {
        mChatId = chatId;
        mLis = listener;
    }

    public static void enable(View v) {
        if (v instanceof RadioGroup) {
            for (int i = 0; i < ((RadioGroup) v).getChildCount(); i++) {
                RadioButton rb = (RadioButton) ((RadioGroup) v).getChildAt(i);
                enable(rb);
            }
        } else if (v instanceof RadioButton) {
            v.setEnabled(true);
        } else if (v instanceof Button) {
            v.setEnabled(true);
            v.setAlpha(1f);
        } else if (v instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
                View child = ((ViewGroup) v).getChildAt(i);
                enable(child);
            }
        } else {
            v.setEnabled(true);
        }
    }

    public static JSONObject disableView(JSONObject view, JSONObject values) {
        try {
            if (!"gone".equalsIgnoreCase(view.optString("state"))) {
                view.put("state", "disable");
            }
            if (view.has("action")) view.remove("action");
            if (view.has("actions")) view.remove("actions");
            if (values != null && view.has("name") && values.has(view.optString("name")))
                view.put("val", values.optString(view.optString("name")));
            if (view.has("items")) {
                JSONArray arr = view.optJSONArray("items");
                JSONArray arr2 = new JSONArray();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.optJSONObject(i);
                    arr2.put(disableView(item, values));
                }
                view.put("items", arr2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public static JSONObject refreshView(JSONObject view, JSONObject values) {
        try {
            if (values != null && view.has("name") && values.has(view.optString("name"))) {
                String val = values.optString(view.optString("name"));
                if (view.optString("type").equals(TYPE_IMG) && val.startsWith("http")) {
                    view.put("src", val);
                } else {
                    view.put("val", val);
                }
            }
            if (view.has("items")) {
                JSONArray arr = view.optJSONArray("items");
                JSONArray arr2 = new JSONArray();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.optJSONObject(i);
                    arr2.put(refreshView(item, values));
                }
                view.put("items", arr2);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public void disable(final View v) {
        mLis.getAct().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (v instanceof RadioGroup) {
                    for (int i = 0; i < ((RadioGroup) v).getChildCount(); i++) {
                        RadioButton rb = (RadioButton) ((RadioGroup) v).getChildAt(i);
                        disable(rb);
                    }
//        } else if (v instanceof RadioButton) {
//            v.setEnabled(false);
//        } else if (v instanceof Button) {
//            v.setEnabled(false);
//            v.setAlpha(0.5f);
                } else if (v instanceof ViewGroup) {
                    for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
                        View child = ((ViewGroup) v).getChildAt(i);
                        disable(child);
                    }
                    if (NAME_ROOT.equals(v.getTag())) {
                        //TODO: setAlpha не корректно работает на CustomLinearLayout
                        v.setAlpha(0.5f);
                    }
                } else {
                    v.setEnabled(false);
                }
                v.setClickable(false);
            }
        });
    }

    public void changeFormState(View v, boolean isEnabled) {
        while (!NAME_ROOT.equals(v.getTag())) {
            v = (ViewGroup) v.getParent();
        }
        if (isEnabled) enable(v);
        else disable(v);
    }

    public View makeView(JSONObject jo, SendListener sl) throws Exception {
        jo.put("name", NAME_ROOT);
        try {
            return render(jo, TYPE_ROW.equalsIgnoreCase(jo.optString("type")), sl);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("NewApi")
    private synchronized View render(final JSONObject jo, boolean horizontal, SendListener sl) throws Exception {
        final View v;
        if (TYPE_ROW.equals(jo.optString("type"))) {
            v = renderContainer(jo, true, sl);
        } else if (TYPE_COL.equals(jo.optString("type"))) {
            v = renderContainer(jo, false, sl);
        } else if (TYPE_TEXT.equals(jo.optString("type"))) {
            v = renderText(jo, false, sl);
        } else if (TYPE_EDIT.equals(jo.optString("type"))) {
            v = renderText(jo, true, sl);
        } else if (TYPE_TEXTAREA.equals(jo.optString("type"))) {
            v = renderTArea(jo);
        } else if (TYPE_IMG.equals(jo.optString("type"))) {
            v = renderImage(jo, sl);
        } else if (TYPE_CHECK.equals(jo.optString("type"))) {
            v = renderCheckBox(jo, sl);
        } else if (TYPE_RADIO.equals(jo.optString("type"))) {
            v = renderRadio(jo, sl);
        } else if (TYPE_SELECT.equals(jo.optString("type"))) {
            v = renderSpinner(jo, sl);
        } else if (TYPE_BUTTON.equals(jo.optString("type"))) {
            v = renderButton(jo, sl);
        } else if (TYPE_MAP.equals(jo.optString("type"))) {
            v = renderMap(jo);
        } else if (TYPE_ACOMPLETE.equals(jo.optString("type"))) {
            v = renderAcomplete(jo, sl);
        } else if (TYPE_VIDEO.equals(jo.optString("type"))) {
            v = renderVideo(jo);
        } else {
            throw new Exception("unknown element " + jo.optString("type"));
        }

        if (jo.has("name")) {
            v.setTag(jo.optString("name"));
        }

        setLayoutParams(v, horizontal, jo);

        setGravity(v, jo);

        if (!(v instanceof CheckBox)) {
            if (jo.has("pd")) {
                int[] pp = new int[4];
                JSONArray arr = jo.optJSONArray("pd");
                for (int i = 0; i < arr.length(); i++) {
                    pp[i] = (int) Tool.convertDpToPixel(arr.optInt(i), mLis.getAct());
                }
                v.setPadding(pp[3], pp[0], pp[1], pp[2]);
            }
        }

        if (jo.has("mg")) {
            int[] pp = new int[4];
            JSONArray arr = jo.optJSONArray("mg");
            for (int i = 0; i < arr.length(); i++) {
                pp[i] = (int) Tool.convertDpToPixel(arr.optInt(i), mLis.getAct());
            }
//            if (v instanceof TextView) v.setPadding(0, 0, 0, 0);
            ((LinearLayout.LayoutParams) v.getLayoutParams()).setMargins(pp[3], pp[0], pp[1], pp[2]);
        }

        drawBg(v, jo);

        if (jo.has("state")) {
            String s = jo.optString("state");
            if (STATE_DISABLE.equals(s)) {
                disable(v);
            }
            if (STATE_INVISIBLE.equals(s)) {
                v.setVisibility(View.INVISIBLE);
            }
            if (STATE_GONE.equals(s)) {
                v.setVisibility(View.GONE);
            }
        }
        return v;
    }

    private View renderVideo(JSONObject jo) {
        VideoView v = new VideoView(mLis.getAct());
        if (jo.has("src")) v.setVideoURI(Uri.parse(jo.optString("src")));
        MediaController controller = new MediaController(mLis.getAct());
        controller.setAnchorView(v);
        v.setMediaController(controller);
        return v;
    }

    private void drawBg(final View v, final JSONObject jo) {
        if (v instanceof Spinner) return;
        if (STATE_GONE.equals(jo.optString("state")) || STATE_INVISIBLE.equals(jo.optString("state")))
            return;
        if (jo.has("bg") || jo.has("b_size") || jo.has("b_radius") || jo.has("b_color") || TYPE_IMG.equalsIgnoreCase(jo.optString("type"))) {
            v.setBackgroundColor(Color.TRANSPARENT);
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (v.getBackground().getBounds().right <= 0) {
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (v.getMeasuredWidth() == 0 || v.getMeasuredHeight() == 0) {
                            Tool.log("strange element's bg: " + jo);
                            return;
                        }
                        if (TYPE_IMG.equalsIgnoreCase(jo.optString("type"))) {
                            String src = jo.optString("src");
                            if ("{{!meta.me.photo}}".equalsIgnoreCase(jo.optString("src"))) {
                                src = Storage.getInstance(mLis.getAct()).getMyPhoto();
                            }
                            Tool.log("src=" + src);
                            displayImage(src, v, jo);
                        } else if (jo.has("bg")) {
                            if (jo.optString("bg").startsWith("#")) {
                                try {
                                    SoftReference<Bitmap> bitmapReference = new SoftReference<>(Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.RGB_565));
                                    Canvas bgCanvas = new Canvas(bitmapReference.get());
                                    bgCanvas.drawColor(Color.parseColor(jo.optString("bg")));
//                                    proceedBG(bitmapReference, v, jo);
                                    customView(v, jo, Color.parseColor(jo.optString("bg")));
                                    bgCanvas.setBitmap(null);
                                } catch (Exception e) {
                                    onFailSetBackground(v, jo);
                                    e.printStackTrace();
                                }
                            } else {
                                displayImage(jo.optString("bg"), v, jo);
                            }
                        } else {
//                            proceedBG(null, v, jo);
                            customView(v, jo, 0);
                        }

                    } catch (Throwable e2) {
                        e2.printStackTrace();
                    }
                }
            });
        }
    }

    private void displayImage(final String src, final View v, final JSONObject jo) {
        if (!src.startsWith("http")) return;
        mLis.getAct().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Picasso.with(mLis.getAct())
                        .load(src)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                Tool.log("***onBitmapLoaded");
                                proceedBG(new SoftReference<>(bitmap), v, jo);
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                Tool.log("***onBitmapFailed");
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                Tool.log("***onPrepareLoaded");
                            }
                        });

            }
        });

//        pool.execute(new Runnable() {
//            @Override
//            public void run() {
//                InputStream stream = Tool.http2Stream(src);
//                BufferedInputStream bis = new BufferedInputStream(stream);
//                final Bitmap bmp = BitmapFactory.decodeStream(bis);
//                mLis.getAct().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        proceedBG(new SoftReference<>(bmp), v, jo);
//                    }
//                });
//            }
//        });
    }

    public void customView(final View v, JSONObject jo, int backgroundColor) {
        int stroke = Color.BLACK, radius = 0, strokeW = -1;

        if (jo.has("b_radius")) {
            radius = (int) Tool.convertDpToPixel(jo.optInt("b_radius"), mLis.getAct());
        }
        if (jo.has("b_size")) {
            strokeW = (int) Tool.convertDpToPixel(jo.optInt("b_size"), mLis.getAct());
        }
        if (jo.has("b_color")) {
            stroke = Color.parseColor(jo.optString("b_color"));
        }

        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
        if(backgroundColor != 0)shape.setColor(backgroundColor);
        shape.setStroke(strokeW, stroke);
        mLis.getAct().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.setBackgroundDrawable(shape);
            }
        });
    }

    private void proceedBG(SoftReference<Bitmap> reference, final View v, final JSONObject jo) {
        try {
            if (reference == null || reference.get() == null) {
                reference = new SoftReference<>(Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888));
            }
            final Bitmap bmp = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bmp);
            int stroke = Color.BLACK, radius = 0, strokeW = -1;

            if (jo.has("b_radius")) {
                radius = (int) Tool.convertDpToPixel(jo.optInt("b_radius"), mLis.getAct());
            }
            if (jo.has("b_size")) {
                strokeW = (int) Tool.convertDpToPixel(jo.optInt("b_size"), mLis.getAct());
            }
            if (jo.has("b_color")) {
                stroke = Color.parseColor(jo.optString("b_color"));
            }

            RectF rect = new RectF();
            int s = strokeW > 0 ? strokeW : 0;
            rect.set(s, s, v.getMeasuredWidth() - s, v.getMeasuredHeight() - s);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            canvas.drawRoundRect(rect, radius, radius, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            if (reference.get() != null) {
                Rect rr = new Rect(0, 0, reference.get().getWidth(), reference.get().getHeight());
                canvas.drawBitmap(reference.get(), rr, rect, paint);
            }
            if (strokeW > 0) {
                paint = new Paint();
                paint.setColor(stroke);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(strokeW);
                canvas.drawRoundRect(rect, radius, radius, paint);
            }
            canvas.setBitmap(null);
            final Drawable drawable = new BitmapDrawable(mLis.getAct().getResources(), bmp);
            reference.clear();
            mLis.getAct().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new MyRunnable(drawable, v).run();
                }
            });

        } catch (Exception e) {
            onFailSetBackground(v, jo);
            e.printStackTrace();
        }
//        System.gc();
    }


    private static class MyRunnable {
        private Drawable bm;
        private View v;

        MyRunnable(Drawable b, View v) {
            this.bm = b;
            this.v = v;
        }

        void run() {
            v.setBackgroundDrawable(bm);
            v = null;
            bm = null;
        }
    }

    private void onFailSetBackground(final View v, final JSONObject jo) {
        try {
            mLis.getAct().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
                    Drawable drawable = mLis.getAct().getResources().getDrawable(R.drawable.shape_backround_default);
                    if (jo.has("bg")) {
                        try {
                            drawable.setColorFilter(Color.parseColor(jo.optString("bg")), mMode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    v.setBackgroundDrawable(drawable);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setLayoutParams(View v, boolean horizontal, JSONObject jo) {
        int w = v instanceof LinearLayout ? LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT;
        int h = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (jo.has("w")) w = (int) Tool.convertDpToPixel(getClearedInt(jo, "w"), mLis.getAct());
        if (jo.has("h")) h = (int) Tool.convertDpToPixel(getClearedInt(jo, "h"), mLis.getAct());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
        if (v instanceof ImageView && !jo.has("w") && !jo.has("h") && !jo.has("weight")) {
            params.height = (int) Tool.convertDpToPixel(200, mLis.getAct());
            params.width = (int) Tool.convertDpToPixel(200, mLis.getAct());
        }
        if (!NAME_ROOT.equalsIgnoreCase(jo.optString("name"))) {
            if (!jo.has("weight")) {
                if (horizontal && !jo.has("w")) {
                    params.weight = 1;
                    params.width = 0;
                }
                if (!horizontal && !jo.has("h")) {
//                    params.weight = 1;
//                    params.height = 0;
                }
            } else {
                if (v instanceof ImageView && (jo.has("w") || jo.has("h"))) {
                    // none
                } else {
                    params.weight = getClearedInt(jo, "weight");
                    if (horizontal) params.width = 0;
                    else params.height = 0;
                }
            }
        }
        if (v instanceof ImageView) {
            if (!jo.has("w") && jo.has("h"))
                params.width = params.height;
            else if (!jo.has("h") && jo.has("w"))
                params.height = params.width;
        }
        if (v instanceof TextView && jo.has("h")) {
            params.height = (int) Tool.convertDpToPixel(jo.optInt("h"), mLis.getAct());
        }
        v.setLayoutParams(params);
    }

    private void setGravity(View v, JSONObject jo) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        // TODO: костыль для совместимости
        if (jo.has("gravity")) {
            JSONArray arr = jo.optJSONArray("gravity");
            try {
                jo.put("halign", arr.optString(0));
                jo.put("valign", arr.optString(1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jo.has("valign")) {
            if (jo.has("halign")) {
                params.gravity = decodeGravity(jo.optString("valign"), false) | decodeGravity(jo.optString("halign"), true);
                if (jo.has("talign")) {
                    if (v instanceof TextView)
                        ((TextView) v).setGravity(decodeGravity(jo.optString("valign"), false) | decodeGravity(jo.optString("talign"), true));
                } else {
                    if (v instanceof TextView)
                        ((TextView) v).setGravity(decodeGravity(jo.optString("valign"), false) | decodeGravity(jo.optString("halign"), true));
                }
            } else {
                params.gravity = decodeGravity(jo.optString("valign"), false);
                if (jo.has("talign")) {
                    if (v instanceof TextView)
                        ((TextView) v).setGravity(decodeGravity(jo.optString("valign"), false) | decodeGravity(jo.optString("talign"), true));
                } else {
                    if (v instanceof TextView)
                        ((TextView) v).setGravity(decodeGravity(jo.optString("valign"), false));
                }
            }
        } else {
            if (jo.has("halign")) {
                params.gravity = decodeGravity(jo.optString("halign"), true);
                if (jo.has("talign")) {
                    if (v instanceof TextView)
                        ((TextView) v).setGravity(decodeGravity(jo.optString("talign"), true));
                } else {
                    if (v instanceof TextView)
                        ((TextView) v).setGravity(decodeGravity(jo.optString("halign"), true));
                }
            } else {
                if (jo.has("talign")) {
                    if (v instanceof TextView)
                        ((TextView) v).setGravity(decodeGravity(jo.optString("talign"), true));
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private JSONObject collectFormData(JSONObject jo, View view) {
        JSONObject resp = new JSONObject();
        try {
            resp.put(jo.optString("name"), jo.optString("val"));
            View v = view;
            while (!NAME_ROOT.equals(v.getTag())) {
                v = (ViewGroup) v.getParent();
            }
            getViewValue(v, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    private void processAction(final View view, final JSONObject resp, final JSONObject action, final SendListener sl) {
        mLis.getAct().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (action.has("data")) {
                        Map<String, Object> appData = Tool.jsonToMap(action.optJSONObject("data"));
                        for (String key : appData.keySet()) {
                            resp.put(key, appData.get(key));
                        }
                    }
                    sl.doAction(action.getString("oper"), resp, Tool.jsonToMap(action), new ActionProcessListener() {
                        @Override
                        public void onProcess(boolean disableView) {
                            if (disableView) changeFormState(view, false);
                        }
                    });

                    if ("fullVersion".equals(action.getString("oper"))) {
                        Bus.getInstance().post(new P24enableFullVerReq());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean onClickAction(final View view, final JSONObject jo, final SendListener sl) {
        JSONObject resp = collectFormData(jo, view);
        if (jo.has("action")) {
            JSONObject action = jo.optJSONObject("action");
            processAction(view, resp, action, sl);
            return true;
        }
        if (jo.has("actions")) {
            JSONArray acts = jo.optJSONArray("actions");
            for (int i = 0; i < acts.length(); i++) {
                JSONObject action = acts.optJSONObject(i);
                processAction(view, resp, action, sl);
            }
            return true;
        }
        return false;
    }

    private String getRadioValue(RadioGroup rg) {
        String value = "";
        for (int i = 0; i < rg.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rg.getChildAt(i);
            if (rb.isChecked()) {
                value = (String) rb.getTag();
                break;
            }
        }
        return value;
    }

    private JSONObject getViewValue(final View v, JSONObject resp) throws JSONException {
        Object name = v.getTag();
        if (name != null) {
            if (v instanceof EditText) {
                resp.put((String) name, ((EditText) v).getText().toString());
            } else if (v instanceof RadioButton) {
            } else if (v instanceof CheckBox) {
                resp.put((String) name, ((CheckBox) v).isChecked());
            } else if (v instanceof MapButton) {
                resp.put((String) name, ((MapButton) v).getLatlon());
            } else if (v instanceof Button) {
            } else if (v instanceof RadioGroup) {
                resp.put((String) name, getRadioValue((RadioGroup) v));
            } else if (v instanceof Spinner) {
                resp.put((String) name, ((FormSpinnerAdapter) ((Spinner) v).getAdapter()).sel_value);
            } else if (v instanceof TextView) {
                resp.put((String) name, ((TextView) v).getText().toString());

            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                resp = getViewValue(vg.getChildAt(i), resp);
            }
        }
        return resp;
    }

    private AutoCompleteTextView renderAcomplete(final JSONObject jo, final SendListener sl) {
        final AutoCompleteTextView atv = new AutoCompleteTextView(mLis.getAct().getApplicationContext());
        // TODO:
        return atv;
    }

    private Button buildButon(final JSONObject jo) {
        final Button v = TYPE_MAP.equals(jo.optString("type")) ? new MapButton(mLis.getAct().getApplicationContext()) : new Button(mLis.getAct().getApplicationContext());
        v.setPadding(2, 2, 2, 2);
        if (jo.has("title")) {
            v.setText(jo.optString("title"));
        } else if (jo.has("val")) {
            v.setText(jo.optString("val"));
        }
        v.setSingleLine();
        v.setEllipsize(TextUtils.TruncateAt.END);
        if (jo.has("size")) {
            v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, jo.optLong("size"));
        }
        if (jo.has("tstyle")) {
            JSONArray arr = jo.optJSONArray("tstyle");
            List<String> params = new ArrayList<String>();
            for (int i = 0; i < arr.length(); i++) {
                params.add(arr.optString(i));
            }
            if (params.contains("bold") && params.contains("italic"))
                v.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            else if (params.contains("bold")) v.setTypeface(Typeface.DEFAULT_BOLD);
            else if (params.contains("italic")) v.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        }
        if (jo.has("color")) {
            if (jo.optString("color").length() > 4) {
                v.setTextColor(Color.parseColor(jo.optString("color").trim()));
            }
        }
        return v;
    }

    private Button renderButton(final JSONObject jo, final SendListener sl) {
        Button v = buildButon(jo);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!onClickAction(view, jo, sl)) {
                    sl.doSend(collectFormData(jo, view));
                    changeFormState(view, false);
                }
            }
        });
        return v;
    }

    private MapButton renderMap(final JSONObject jo) {
        final MapButton v = (MapButton) buildButon(jo);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MapWindow(mLis.getAct(), jo, new MapWindow.OnSelectListener() {
                    @Override
                    public void onSelect(String address, double lat, double lon) {
                        v.setText(address);
                        v.setLatlon(lat + "," + lon);
                    }

                    @Override
                    public void onCancel() {
                    }
                }, MapWindow.MAP_WITH_SELECT).show();
            }
        });
        return v;
    }

    private CheckBox renderCheckBox(final JSONObject jo, final SendListener sl) {
        final AppCompatCheckBox v = new AppCompatCheckBox(mLis.getAct());
        if (jo.has("val")) {
            v.setChecked(jo.optBoolean("val"));
        }
        if (jo.has("title")) {
            v.setText(jo.optString("title"));
        }
        if (jo.has("size")) {
            v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, jo.optLong("size"));
        }
        if (jo.has("color")) {
            v.setTextColor(Color.parseColor(jo.optString("color").trim()));
        }

        v.setSupportButtonTintList(ContextCompat.getColorStateList(mLis.getAct(), R.color.selector_check_box_color));

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, MotionEvent motionEvent) {
                if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
                    onClickAction(v, jo, sl);
                }
                return false;
            }
        });
        return v;
    }

    private RadioGroup renderRadio(final JSONObject jo, final SendListener sl) {
        final RadioGroup v = new RadioGroup(mLis.getAct());
        int chId = -1;
        if (jo.has("vars")) {
            final JSONArray vars = jo.optJSONArray("vars");

            for (int i = 0; i < vars.length(); i++) {
                final AppCompatRadioButton r = new AppCompatRadioButton(mLis.getAct());
                r.setId(Tool.generateViewId());
                final JSONObject item = vars.optJSONObject(i);
                r.setText(item.optString("t"));
//                r.setText(Html.fromHtml(item.optString("t")));
                r.setTag(item.optString("v"));
                if (jo.has("size")) {
                    r.setTextSize(TypedValue.COMPLEX_UNIT_DIP, jo.optLong("size"));
                }
                if (jo.has("color")) {
                    r.setTextColor(Color.parseColor(jo.optString("color").trim()));
                } else {
                    r.setTextColor(Color.parseColor("#000000"));
                }

                r.setSupportButtonTintList(ContextCompat.getColorStateList(mLis.getAct(), R.color.selector_check_box_color));

                r.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(final View view, MotionEvent motionEvent) {
                        if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
                            pool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    onClickAction(view, item, sl);
                                    onClickAction(v, jo, sl);
                                }
                            });
                        }
                        return false;
                    }
                });
                if (jo.has("val") && jo.optString("val").equals(item.optString("v")))
                    chId = r.getId();
                v.addView(r);
            }
        }
        if (chId > 0) v.check(chId);
        return v;
    }

    private Spinner renderSpinner(final JSONObject jo, final SendListener sl) {
        final Spinner v = new Spinner(mLis.getAct(), Spinner.MODE_DROPDOWN);
        float textSize = getClearedInt(jo, "size");
        final FormSpinnerAdapter fsa = new FormSpinnerAdapter(mLis.getAct(), null, textSize);
        int selId = 0;
        final List<String> vals = new ArrayList<String>();
        if (jo.has("vars")) {
            JSONArray vars = jo.optJSONArray("vars");
            for (int i = 0; i < vars.length(); i++) {
                JSONObject item = vars.optJSONObject(i);
                fsa.addItem(item.optString("t"), item.optString("v"));
                vals.add(item.optString("v"));
                if (jo.has("val") && jo.optString("val").equals(item.optString("v"))) {
                    selId = i;
                }
            }
        }
        if (vals.size() > 0)
            fsa.sel_value = vals.get(0);
        v.setAdapter(fsa);
        v.setSelection(selId, false);
        mLis.getAct().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String s = vals.get(i);
                        ((FormSpinnerAdapter) v.getAdapter()).sel_value = s;
                        if (!s.equals(jo.optString("val")))
                            onClickAction(v, jo, sl);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
        });
        return v;
    }

    private ImageView renderImage(final JSONObject jo, final SendListener sl) {

//        if ("{{!meta.me.photo}}".equalsIgnoreCase(jo.optString("src"))) {
//            final CircularImageView v = new CircularImageView(act.getApplicationContext());
//            App.tm.exec(new Runnable() {
//                @Override
//                public void run() {
//                    int w = -1, h = -1, r = -1;
////                    if (jo.has("w")) w = (int) Tool.convertDpToPixel(jo.optInt("w"), act);
////                    if (jo.has("h")) h = (int) Tool.convertDpToPixel(jo.optInt("h"), act);
//                    if (jo.has("b_radius"))
//                        r = (int) Tool.convertDpToPixel(jo.optInt("b_radius"), act);
//                    final int radius = r;
//                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(act);
//                    final String src = preferences.getString(App.PROP_URL_PHOTO, null);
//                    App.log("src=" + src);
//                    String name = preferences.getString(App.PROP_MY_NAME, "Anonymous");
//                    App.log("name=" + name);
//                    v.setBorderColor(jo.has("b_color") ? Color.parseColor(jo.optString("b_color")) : act.getResources().getColor(R.color.gray_6c));
//                    v.setBorderWidth(1);
//                    act.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Tool.loadImage(act, src, null, v, radius > 0 ? radius : 44, R.drawable.contact_placeholder);
//                        }
//                    });
//                }
//            });
//            iv = v;
//        } else {
        ImageView iv = new ImageView(mLis.getAct().getApplicationContext());
        if (jo.has("src")) {
            if ("{{!meta.!user.photo}}".equalsIgnoreCase(jo.optString("src"))) {
                ChatBased chatBased = Storage.getInstance(mLis.getAct()).getChat(mChatId);
                Tool.loadImage(mLis.getAct(), chatBased.getChatPhoto(), iv, R.drawable.ic_acc_bg, false);
            }
        }
//        }
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAction(view, jo, sl);
            }
        });
        return iv;
    }

    private int getClearedInt(JSONObject jo, String field) {
        try {
            return jo.getInt(field);
        } catch (Exception e) {
            e.printStackTrace();
            String s = jo.optString(field);
            if (s == null) return 0;
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                if (!Character.isDigit(c)) continue;
                sb.append(c);
            }
            try {
                return Integer.parseInt(sb.toString());
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return 0;
    }

    private LinearLayout renderContainer(final JSONObject jo, boolean horizontal, final SendListener sl) throws Exception {
        CustomLinearLayout v = new CustomLinearLayout(mLis.getAct().getApplicationContext());
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAction(view, jo, sl);
            }
        });
        v.setOrientation(horizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        if (jo.has("items")) {
            JSONArray items = jo.optJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject ijo = items.optJSONObject(i);
                if (jo.has("size") && !ijo.has("size")) ijo.put("size", jo.optInt("size"));
                if (jo.has("color") && !ijo.has("color")) ijo.put("color", jo.optString("color"));
                if (jo.has("tstyle") && !ijo.has("tstyle"))
                    ijo.put("tstyle", jo.optJSONArray("tstyle"));
                if (jo.has("talign") && !ijo.has("talign"))
                    ijo.put("talign", jo.optString("talign"));
                if (jo.has("it") && !ijo.has("it")) ijo.put("it", jo.optJSONArray("it"));
                View vv = render(ijo, horizontal, sl);
                if (!horizontal || items.length() == 1) {
                    if (!ijo.has("w")) {
                        vv.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
                    } else {
                        v.setGravity(Gravity.CENTER_HORIZONTAL);
                        vv.getLayoutParams().width = (int) Tool.convertDpToPixel(getClearedInt(ijo, "w"), mLis.getAct());
                    }
                }
                v.addView(vv, vv.getLayoutParams());
            }
        }
        return v;
    }

    private EditText renderTArea(JSONObject jo) {
        EditText v = new EditText(mLis.getAct());
        v.setSingleLine(false);
        v.setMinLines(3);
        v.setMaxLines(3);
        v.setHorizontallyScrolling(false);
        v.setGravity(Gravity.TOP | Gravity.LEFT);
        v.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return true;
            }
        });

        if (jo.has("val")) {
            v.setText(jo.optString("val"));
        }
        if (jo.has("color") && jo.optString("color").startsWith("#") && jo.optString("color").length() == 7) {
            int color = Color.parseColor(jo.optString("color"));
            v.setTextColor(color);
            if (!Tool.isColorDark(color)) v.setHintTextColor(color);
        } else {
            v.setTextColor(Color.parseColor("#000000"));
        }
        if (jo.has("hint")) {
            v.setHint(jo.optString("hint"));
            v.setHintTextColor(ContextCompat.getColor(mLis.getAct(), R.color.hintText));
        }
        return v;
    }

    private TextView renderText(final JSONObject jo, boolean edit, final SendListener sl) {
        final TextView v = edit ? new EditText(mLis.getAct()) : new TextView(mLis.getAct());
        if (!edit) {
            v.setTextIsSelectable(true);
//            v.setAutoLinkMask(Linkify.WEB_URLS);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickAction(view, jo, sl);
                }
            });
        }
        int defPadding = edit ? 12 : 0;
        v.setPadding(defPadding, defPadding, defPadding, defPadding);
        if (jo.has("val")) {
            if ("{{!meta.me.btc_balance}}".equalsIgnoreCase(jo.optString("val"))) {
                try {
                    BtcFasade.getBalance(mLis.getAct(), new BtcFasade.BalListener() {
                        @Override
                        public void onSuccess(final String bal) {
                            mLis.getAct().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    v.setText(bal);
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            mLis.getAct().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    v.setText("0.00");
                                }
                            });

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("{{!meta.me.btc_addr}}".equalsIgnoreCase(jo.optString("val"))) {
                LWallet w = LWallet.getInstance(mLis.getAct());
                if (w != null) {
                    v.setText(w.currentReceiveAddress().toString());
                }
            } else if ("{{!meta.!user.name}}".equalsIgnoreCase(jo.optString("val"))) {
                ChatBased chatBased = Storage.getInstance(mLis.getAct()).getChat(mChatId);
                v.setText(chatBased.getName());

            } else if ("{{!meta.!user.phone}}".equalsIgnoreCase(jo.optString("val"))) {
                ChatBased chatBased = Storage.getInstance(mLis.getAct()).getChat(mChatId);
                String phone = ((User) chatBased).getPhone();
                v.setText(phone.length() > 4 ? phone : "");

            } else if ("{{!meta.!user.desc}}".equalsIgnoreCase(jo.optString("val"))) {
                v.setText("");

            } else {
                v.setText(jo.optString("val"));
            }
//            v.setText(Html.fromHtml(jo.optString("val").replaceAll("\n", "<br/>")));
        }
        if (edit) {
            Tool.log("edit");
            v.addTextChangedListener(new OnTextWatcherListener(jo));
            v.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            v.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    v.requestFocus();
                    return false;
                }
            });
        }
        if (jo.has("tstyle")) {
            JSONArray arr = jo.optJSONArray("tstyle");
            List<String> params = new ArrayList<String>();
            for (int i = 0; i < arr.length(); i++) {
                params.add(arr.optString(i));
            }
            if (params.contains("bold") && params.contains("italic"))
                v.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            else if (params.contains("bold")) v.setTypeface(Typeface.DEFAULT_BOLD);
            else if (params.contains("italic")) v.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        }
        if (jo.has("size")) {
            v.setTextSize(TypedValue.COMPLEX_UNIT_DIP, jo.optLong("size"));
        }
        if (jo.has("it")) {
            if ("cardnumber".equals(jo.optString("it")) || "number".equals(jo.optString("it"))) {
                v.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            if ("float".equals(jo.optString("it"))) {
                v.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
            if ("phone".equals(jo.optString("it"))) {
                v.setInputType(InputType.TYPE_CLASS_PHONE);
            }
            if ("datetime".equals(jo.optString("it"))) {
                v.setInputType(InputType.TYPE_CLASS_DATETIME);
            }
            if ("mail".equals(jo.optString("it"))) {
                v.setInputType(InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
            }
            if ("uri".equals(jo.optString("it"))) {
                v.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
            }
            if ("pass_text".equals(jo.optString("it"))) {
                v.setTransformationMethod(new PasswordTransformationMethod());
            }
            if ("pass_num".equals(jo.optString("it"))) {
                v.setInputType(InputType.TYPE_CLASS_NUMBER);
                v.setTransformationMethod(new PasswordTransformationMethod());
            }
        }
        if (jo.has("color") && jo.optString("color").startsWith("#") && jo.optString("color").length() == 7) {
            int color = Color.parseColor(jo.optString("color"));
            v.setTextColor(color);
            if (!Tool.isColorDark(color)) v.setHintTextColor(color);
        } else {
            v.setTextColor(Color.parseColor("#000000"));
        }
        if (jo.has("hint")) {
            v.setHint(jo.optString("hint"));
            v.setHintTextColor(ContextCompat.getColor(mLis.getAct(), R.color.hintText));
        }
        if (edit && v.getText().length() < 5 && jo.has("name") && jo.optString("name").equals("phone")) {
            String number = Tool.getPhoneNumber(mLis.getAct());
            if (number != null && number.trim().length() > 3) {
                v.setText(number);
            }
        }

        return v;
    }

    private int decodeGravity(String gravity, boolean horizontal) {
        if (horizontal) {
            if ("left".equalsIgnoreCase(gravity)) return Gravity.LEFT;
            else if ("middle".equalsIgnoreCase(gravity) || "center".equalsIgnoreCase(gravity))
                return Gravity.CENTER_HORIZONTAL;
            else if ("right".equalsIgnoreCase(gravity)) return Gravity.RIGHT;
        } else {
            if ("top".equalsIgnoreCase(gravity)) return Gravity.TOP;
            else if ("middle".equalsIgnoreCase(gravity) || "center".equalsIgnoreCase(gravity))
                return Gravity.CENTER_VERTICAL;
            else if ("bottom".equalsIgnoreCase(gravity)) return Gravity.BOTTOM;
        }
        return -1;
    }

//    public void setActivity(Activity activity) {
//        act = activity;
//    }

    public interface ActionProcessListener {

        void onProcess(boolean result);
    }

    public interface SendListener {
        void doSend(JSONObject data);

        void doAction(String oper, JSONObject data, Map<String, Object> params, ActionProcessListener apl);
    }

    public interface GetActivityInterface {
        Activity getAct();
    }

    private class MapButton extends Button {
        private String latlon;
        private String address;

        public MapButton(Context context) {
            super(context);
        }

        public String getLatlon() {
            return latlon;
        }

        public void setLatlon(String latlon) {
            this.latlon = latlon;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public class OnTextWatcherListener implements TextWatcher {

        JSONObject param;

        public OnTextWatcherListener(JSONObject param) {
            this.param = param;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {/**/ }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                param.put("val", s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {/**/ }
    }
}