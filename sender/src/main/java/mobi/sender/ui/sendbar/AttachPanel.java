package mobi.sender.ui.sendbar;

import android.content.ContentResolver;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.SendFileReq;
import mobi.sender.event.SendLocationReq;
import mobi.sender.tool.Tool;
import mobi.sender.tool.bar.BarView;
import mobi.sender.tool.utils.MediaUtils;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.control.SelectableImageView;
import mobi.sender.ui.window.map.MapWindow;

public class AttachPanel extends BasePanel {

    private static final int MAX_IMAGES_COUNT = 10;
    private static final String TAG = AttachPanel.class.getSimpleName();
    /**
     * Cursor used to access the results from querying for images on the SD card.
     */
    private Cursor cursor;
    /*
     * Column index for the Thumbnails Image IDs.
     */
    private int columnIndex;
    private LinearLayout photos;
    private PhotosAdapter adapter;
    private List<Uri> selectedImages = new ArrayList<>();
    private LinearLayout photosContainer;
    private BarView mBarView;

    public AttachPanel(ChatActivity parent, BarView barView) {
        super(parent);
        adapter = new PhotosAdapter();
        mBarView = barView;
    }

    private void loadPhotos(final ChatActivity parent) {
        // Set up an array of the Thumbnail Image ID column we want
        String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN};
        // Create the cursor pointing to the SDCard
        CursorLoader loader = new CursorLoader(parent,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, // Which columns to return
                null,       // Return all rows
                null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC") {

            @Override
            public void deliverResult(Cursor cursor) {
                super.deliverResult(cursor);
                AttachPanel.this.cursor = cursor;
                refreshImages(adapter);
            }

        };
        loader.startLoading();
    }

    public void attachTo(final ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.chat_attach, viewGroup);
        photosContainer = (LinearLayout) view.findViewById(R.id.photos_container);
        loadPhotos(this.parent);
        setAttachItem(view, R.id.camera, new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                MediaUtils.openCamera(parent);
            }
        });
        setAttachItem(view, R.id.gallery, new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                MediaUtils.openGallary(parent);
            }
        });
        setAttachItem(view, R.id.file, new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                MediaUtils.openFileChoose(parent, new MediaUtils.OnSelectListener() {
                    @Override
                    public void onSelect(File file) {
                        parent.processFile(Uri.fromFile(file));
                        parent.closePanel();
                    }
                });
            }
        });
        setAttachItem(view, R.id.location, new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                new MapWindow(AttachPanel.this.parent, new MapWindow.OnSelectListener() {
                    @Override
                    public void onSelect(String address, double lat, double lon) {
                        mBarView.closePanel();
                        Bus.getInstance().post(new SendLocationReq(address + "", lat + "", lon + "", parent.getChatId()));
                        mBarView.closePanel();
                    }

                    @Override
                    public void onCancel() {
                    }
                }, MapWindow.MAP_FOR_SEND).show();
            }
        });
    }

    private void refreshImages(PhotosAdapter adapter) {
        Tool.log("refreshImages");
        selectedImages.clear();
        photosContainer.removeAllViews();
        View view = inflater.inflate(R.layout.attach_panel_photos_container, photosContainer);
        photos = (LinearLayout) view.findViewById(R.id.photos);
        // Get the column index of the Thumbnails Image ID
        columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        photos.removeAllViews();
        int n = cursor.getCount() > MAX_IMAGES_COUNT ? MAX_IMAGES_COUNT : cursor.getCount();
        for (int i = 0; i < n; i++) {
            addImage(photos, i);
        }
    }

    private void addImage(final ViewGroup parent, final int i) {
        final AsyncTask<Void, Void, Bitmap> loadBitmap = new AsyncTask<Void, Void, Bitmap>() {

            private int imageHeight;
            private SelectableImageView imageView;
            private View view;
            private Uri uri;

            @Override
            protected void onPreExecute() {
                view = inflater.inflate(R.layout.chat_attach_image_item, null);
                imageView = (SelectableImageView) view.findViewById(R.id.imageView);
                imageHeight = imageView.getLayoutParams().height;
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {

                // Move cursor to current position
                cursor.moveToPosition(i);
                // Get the current value for the requested column
                int imageID = cursor.getInt(columnIndex);
                // Set the content of the image based on the provided URI
                uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imageID);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                try {
                    BitmapFactory.decodeStream(AttachPanel.this.parent.getContentResolver().openInputStream(uri), null, options);

                    final int height = options.outHeight;
                    final int width = options.outWidth;
                    int inSampleSize = 1;

                    if (imageHeight > 0) {
                        if (height > imageHeight || width > imageHeight) {

                            final int halfHeight = height / 2;
                            final int halfWidth = width / 2;

                            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                            // height and width larger than the requested height and width.
                            while ((halfHeight / inSampleSize) >= imageHeight
                                    && (halfWidth / inSampleSize) >= imageHeight) {
                                inSampleSize *= 2;
                            }
                        }
                    }

                    options = new BitmapFactory.Options();
                    options.inSampleSize = inSampleSize;
                    options.inJustDecodeBounds = false;
                    Bitmap bitmap = BitmapFactory.decodeStream(AttachPanel.this.parent.getContentResolver().openInputStream(uri), null, options);

                    return bitmap;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Tool.log("onCLick");
                        SelectableImageView image = (SelectableImageView) view;
                        if (image.isSelected()) {
                            image.setSelected(false);
                            selectedImages.remove(uri);
                            if (selectedImages.size() == 0) {
                                mBarView.setImageToSend(false);
                            }
                        } else {
                            image.setSelected(true);
                            selectedImages.add(uri);
                            if (selectedImages.size() == 1) {
                                mBarView.setImageToSend(true);
                            }
                        }
                    }
                });
                parent.addView(view);
                Log.d(TAG, "image added");
            }
        };
        loadBitmap.execute((Void[]) null);
    }

    private void setAttachItem(View parent, int id, View.OnClickListener l) {
        ImageView imageView = (ImageView) parent.findViewById(id);
        imageView.setOnClickListener(l);
    }

    public void sendFile(Uri uri) {
        ContentResolver cr = parent.getContentResolver();
        String mime = cr.getType(uri);
        if (mime == null || "".equals(mime)) {
            String filename = uri.getLastPathSegment();
            if (filename != null) {
                try {
                    String ext = filename.substring(filename.indexOf(".") + 1);
                    mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                } catch (Exception e) {
                    Tool.log("exep e = " + e);
                }
            }
        }

        Bus.getInstance().post(new SendFileReq(parent.getChatId(), "", "", uri.toString(), mime));
        parent.closePanel();
    }


    public void sendSelectedImages() {
        if (selectedImages.size() > 0) {
            for (Uri uri : selectedImages) {
                //TODO: переделать эту логику. Здесь не uri нужен а путь к файлу. Метод для получения полного пути, есть в Tool.class
                sendFile(uri);
            }
        }
        selectedImages.clear();
        for (int i = 0; i < photos.getChildCount(); i++) {
            View view = photos.getChildAt(i);
            SelectableImageView imageView = (SelectableImageView) view.findViewById(R.id.imageView);
            if ((imageView != null) && (imageView.isSelected())) {
                imageView.setSelected(false);
                imageView.invalidate();
            }
        }
    }

    private class PhotosAdapter {

        public int getCount() {
            if (cursor != null) {
                return cursor.getCount();
            } else {
                return 0;
            }
        }
    }
}
