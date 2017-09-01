package mobi.sender.tool.bar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class MonitoringEditText extends EditText {

    private OnCutPasteListener listener;

    public MonitoringEditText(Context context) {
        super(context);
    }

    public MonitoringEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MonitoringEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean consumed = super.onTextContextMenuItem(id);
        if (listener != null)
            switch (id) {
                case android.R.id.cut:
                    listener.onCut();
                    break;
                case android.R.id.paste:
                    listener.onPaste();
                    break;
            }
        return consumed;
    }

    public void setCutPasteListener(OnCutPasteListener listener) {
        this.listener = listener;
    }

    public interface OnCutPasteListener {

        void onCut();

        void onPaste();
    }
}
