package mobi.sender.tool.bar;

import android.media.AudioManager;
import android.media.MediaPlayer;
import mobi.sender.App;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ptitsyn A.
 */
public class SenderMediaController implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private List<String> streamSongs;

    private MediaPlayer player;

    private static SenderMediaController instance;

    private boolean isPrepared;

    private int currentSong;

    private boolean isLoopingAllTracks;

    private IFinishSound finishSound;

    private IChangePosition changePosition;

    private Timer timer;

    private int delay;

    private SenderMediaController() {
        streamSongs = new ArrayList<String>();
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setLooping(false);
        isPrepared = false;
        currentSong = -1;
        isLoopingAllTracks = false;
    }

    public static SenderMediaController getInstance() {
        if (instance == null) {
            synchronized (SenderMediaController.class) {
                instance = new SenderMediaController();
            }
        }
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public void setSource(String src) {
        if (player != null) {
            stop();
            if (finishSound != null) {
                finishSound.finish();
            }
        }
        player = new MediaPlayer();
        timer = new Timer();
        timer.schedule(new UpdateTimePlaying(), 100, 10);

        try {

            player.setDataSource(src);
            player.setOnPreparedListener(this);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (player != null && isPrepared) {
            if (player.isPlaying()) return;

            player.start();
            player.setOnCompletionListener(this);
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    public void stop() {
        if (player != null) {
            synchronized (this) {
                isPrepared = false;
            }
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
            player.stop();
            player.release();
            if (finishSound != null) {
                finishSound.finish();
            }
        }
        player = null;
        timer = null;
    }

    public void seekTo(int dec) {
        if (player != null && isPrepared && player.isPlaying()) {
            player.seekTo((int) (getDuration() * (dec / 100.)));
        }
    }

    public int getDuration() {
        if (player != null && isPrepared) {
            return player.getDuration();
        }
        return 0;
    }

    public void setLooping(boolean isLoop) {
        if (player != null) {
            player.setLooping(isLoop);
        }
    }

    public boolean isPlaying() {
        if (player != null && isPrepared) {
            return player.isPlaying();
        }
        return false;
    }

    public void setFinishSound(IFinishSound finishSound) {
        this.finishSound = finishSound;
    }

    public void setChangePosition(IChangePosition changePosition) {
        this.changePosition = changePosition;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        synchronized (this) {
            isPrepared = true;
        }
        start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (finishSound != null) {
            finishSound.finish();
        }
        synchronized (this) {
            isPrepared = false;
            if (streamSongs != null && streamSongs.size() > 0 && currentSong != -1) {
                try {
                    if (currentSong >= streamSongs.size() - 1 && isLoopingAllTracks) {
                        currentSong = 0;
                        player.setDataSource(streamSongs.get(currentSong));
                    } else {
                        ++currentSong;
                        player.setDataSource(streamSongs.get(currentSong));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void cleanup() {
        if (instance != null)
            instance.stop();
    }

    public interface IFinishSound {
        public void finish();
    }

    public interface IChangePosition {
        public void changePosition(int millis);
    }

    class UpdateTimePlaying extends TimerTask {

        @Override
        public void run() {
            if (player != null && isPrepared && player.isPlaying()) {
                if (changePosition != null) {
                    changePosition.changePosition(player.getCurrentPosition());
                }
            }
        }
    }
}
