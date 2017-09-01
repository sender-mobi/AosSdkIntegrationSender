package mobi.sender.tool.gif;

import android.content.Context;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import mobi.sender.tool.Tool;

public class GifDataDownloader extends AsyncTask<String, Void, byte[]> {

    private Context ctx;

    public GifDataDownloader(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected byte[] doInBackground(final String... params) {
        final String gifUrl = params[0];

        if (gifUrl == null)
            return null;

        byte[] gif = null;
        InputStream content = null;
        ByteArrayOutputStream bos = null;
        FileOutputStream cStream = null;
        try {
            File cashe = new File(ctx.getExternalCacheDir().getAbsolutePath() + "/" + Tool.md5(gifUrl));
            if (gifUrl.startsWith("http")) {
                if (cashe.exists()) {
                    content = new FileInputStream(cashe);
                    Tool.log("from cashe...");
                } else {
                    content = Tool.http2Stream(gifUrl);
                }
            } else {
                content = new FileInputStream(gifUrl);
            }
            byte[] buf = new byte[1024];
            bos = new ByteArrayOutputStream();
            int readed;
            while ((readed = content.read(buf)) > 0) {
                bos.write(buf, 0, readed);
            }
            bos.flush();
            gif = bos.toByteArray();
            if (!cashe.exists()) {
                cStream = new FileOutputStream(cashe);
                cStream.write(gif);
                cStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (content != null) content.close();
                if (bos != null) bos.close();
                if (cStream != null) cStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return gif;
    }
}
