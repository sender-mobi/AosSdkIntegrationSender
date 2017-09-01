package mobi.sender.ui.window;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import mobi.sender.R;
import mobi.sender.tool.SaveFileRunnable;
import mobi.sender.tool.Tool;
import mobi.sender.tool.TouchImageView;
import mobi.sender.tool.gif.GifImageView;

public class PicturePreviewWindow {

    private static final float SCREEN_SIZE_PERCENTAGE = 0.66f;
    private String pictureUrl = "";
    private ImageView ivPicture;
    private RelativeLayout root;
    private String name;
    private String type;
    private Activity act;
    private AlertDialog dialog;
    private String preview;

    public PicturePreviewWindow(Activity ctx, String preview, String url, String name, String type) {
        this.act = ctx;
        this.preview = preview;
        this.pictureUrl = url;
        this.name = name;
        this.type = type;
    }

    public void show() {
        dialog = new AlertDialog.Builder(act).setView(getView()).create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                clear();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                if (pictureUrl.startsWith("http")) {
                    new Thread(new SaveFileRunnable(pictureUrl, name.endsWith(type) ? name : (Tool.md5(pictureUrl) + "." + type), Environment.DIRECTORY_PICTURES, new SaveFileRunnable.OnFinishDownloadListener() {
                        @Override
                        public void onSuccess(final String path) {
                            postByType(path);
                        }

                        @Override
                        public void onMemoryError() {
                            Toast.makeText(act, R.string.tst_no_free_space, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError() {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    root.findViewById(R.id.load).setVisibility(View.GONE);
                                }
                            });
                        }
                    })).start();
                } else {
                    ivPicture = new TouchImageView(act);
                    String str = pictureUrl;
                    if (pictureUrl.startsWith("file://")) {
                        str = pictureUrl.substring(7);  //delete "file://"
                    } else if (pictureUrl.startsWith("content://")) {
                        str = Tool.getRealPathFromUri(act, Uri.parse(pictureUrl));
                    }
                    Bitmap bitmap = Tool.extractThumbnail1(act, str, 1024, 768);
                    ivPicture.setImageBitmap(bitmap);
//                    Tool.loadImageLocal(act, str, ivPicture, android.R.drawable.ic_menu_gallery);
                    showPicture();
                }
            }
        });
        dialog.show();
    }

    private View getView() {
        LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View currentView = inflater.inflate(R.layout.ac_preview_picture, null);
        root = (RelativeLayout) currentView.findViewById(R.id.rlRoot);
        return currentView;
    }

    private void postByType(String path) {
        Tool.log("+++path = "+path);
        if ("gif".equalsIgnoreCase(path.substring(path.lastIndexOf(".") + 1, path.length()))) {
            Tool.log("+++gifff");
            ivPicture = new GifImageView(act);
            ((GifImageView) ivPicture).setUrlImage(path, new GifImageView.EndLoadImageListener() {
                @Override
                public void end() {
                    showPicture();
                }
            });
        } else {
            ivPicture = new TouchImageView(act);
            Bitmap bitmap = Tool.extractThumbnail1(act, path, 1024, 768);
            ivPicture.setImageBitmap(bitmap);
            showPicture();
        }
    }

    private void showPicture() {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                root.addView(ivPicture, 0);
                root.findViewById(R.id.load).setVisibility(View.GONE);
                ivPicture.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (dialog.getWindow().getWindowManager().getDefaultDisplay().getHeight() * SCREEN_SIZE_PERCENTAGE)));
                resume();
            }
        });
    }

    private void clear() {
        if (ivPicture != null && ivPicture instanceof GifImageView) {
            ((GifImageView) ivPicture).clear();
        }
    }

    public void resume() {
        if (ivPicture != null && ivPicture instanceof GifImageView) {
            try {
                if (!((GifImageView) ivPicture).isAnimating())
                    ((GifImageView) ivPicture).startAnimation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
