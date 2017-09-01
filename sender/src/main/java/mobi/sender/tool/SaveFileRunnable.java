package mobi.sender.tool;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tsynPti on 16.03.15.
 */
public class SaveFileRunnable implements Runnable {

    private String url;

    private String fileName;

    private String directory;

    private OnFinishDownloadListener listener;

    private Handler handler;

    public SaveFileRunnable(String url, String fileName, String directory, OnFinishDownloadListener listener) {
        this.url = url;
        this.fileName = fileName;
        this.directory = directory;
        this.listener = listener;
        handler = new Handler(Looper.getMainLooper());

        Tool.log("+++url = "+url+", name = "+fileName+", directory = "+directory);
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(directory) + File.separator + "Sender");
            if (!file.exists()) file.mkdirs();

            Long tsLong = System.currentTimeMillis();
            String ts = tsLong.toString();

            file = new File(file.getAbsolutePath() + File.separator + ts +fileName);
            if (file.isFile()) {
                if (listener != null) {
                    final File finalFile = file;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSuccess(finalFile.getAbsolutePath());
                        }
                    });


                }
                return;
            } else file.createNewFile();

            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            FileOutputStream fos = new FileOutputStream(file);
            connection.connect();
            InputStream is = connection.getInputStream();
            byte[] bytes = new byte[1024];
            int count;
            while ((count = is.read(bytes)) != -1) {
                fos.write(bytes, 0, count);
            }
            fos.flush();
            fos.close();
            is.close();

            if (listener != null) {
                final File finalFile1 = file;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalFile1.length() > 0)
                            listener.onSuccess(finalFile1.getAbsolutePath());
                        else
                            listener.onMemoryError();
                    }
                });


            }
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onError();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public interface OnFinishDownloadListener {
        void onSuccess(String path);

        void onError();

        void onMemoryError();
    }
}
