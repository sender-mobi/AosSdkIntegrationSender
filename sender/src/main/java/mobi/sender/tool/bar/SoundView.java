package mobi.sender.tool.bar;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import mobi.sender.R;
import mobi.sender.tool.Tool;
import mobi.sender.ui.CircularProgressBar;

/**
 * Created by Zver on 11.01.2017.
 */

public class SoundView {

    private LinearLayout voiceLL = null;
    private static final int DURATION = 30000;
    private static final int PADDINGS = 4;
    private static final int DEFAULT_PADDINGS = 4;
    private static final int STROKE_WIDTH = 4;
    private static final int RECORDER_COLOR_ID = R.color.colorGray;
    private static final int MODE_HOLD = 1;
    private static final int MODE_MANUAL = 2;
    private static final int RECORDER_DRAWABLE_ID = R.drawable.draw_hold_talk;
    private static final int PLAYER_DRAWABLE_ID = R.drawable.record;
    private static final float TEXT_SIZE = 18;
    private static final int MAX_LAYOUT_HEIGHT = 200;
    private float length;

    private long startRecord;
    private String fileName = "";

    private MediaRecorder recorder;

    public LinearLayout getVoiceRecorder(final Context ctx, final SendBarRenderer.OnVoiceSendListener listener) {
        if (voiceLL == null) {
            voiceLL = new LinearLayout(ctx);
            voiceLL.setGravity(Gravity.CENTER);
            voiceLL.setOrientation(LinearLayout.VERTICAL);
            voiceLL.setPadding(DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS, DEFAULT_PADDINGS);
            voiceLL.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) Tool.convertDpToPixel(MAX_LAYOUT_HEIGHT, ctx)));

            final CircularProgressBar recordProgress = new CircularProgressBar(ctx, PADDINGS, STROKE_WIDTH, RECORDER_COLOR_ID, DURATION, MODE_HOLD, RECORDER_DRAWABLE_ID);
            final CircularProgressBar playerProgress = new CircularProgressBar(ctx, PADDINGS, STROKE_WIDTH, RECORDER_COLOR_ID, DURATION, MODE_MANUAL, PLAYER_DRAWABLE_ID);
            final LinearLayout wrapper = new LinearLayout(ctx);
            final TextView timer = new TextView(ctx);
            final ImageView statusPlaying = new ImageView(ctx);
            final SenderMediaController controller = SenderMediaController.getInstance();


            LinearLayout.LayoutParams recorderParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            recorderParams.setMargins(0, 0, 0, (int) Tool.convertDpToPixel(26, ctx));
            recordProgress.setLayoutParams(recorderParams);
            recordProgress.setProgressListener(new CircularProgressBar.OnProgressListener() {
                @Override
                public void onStartProgress() {
                    try {
                        fileName = Tool.getRandomFileName("mp3");
                    } catch (Exception e) {
                        if (listener != null)
                            listener.onMediaNotFound();
                        e.printStackTrace();
                        return;
                    }

                    try {
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        recorder.setOutputFile(fileName);
                        recorder.prepare();
                        recorder.start();

                        startRecord = System.currentTimeMillis();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onStopProgress() {
                    try {
                        recorder.stop();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }

                    try {
                        length = (System.currentTimeMillis() - startRecord) / 1000f;
                        if (length < 1) return;
                        recordProgress.setVisibility(View.GONE);
                        wrapper.setVisibility(View.VISIBLE);
                        timer.setText(String.format("%d:%02d", (int) length / 60, (int) length % 60));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            voiceLL.addView(recordProgress);

            wrapper.setGravity(Gravity.CENTER);
            wrapper.setOrientation(LinearLayout.VERTICAL);
            wrapper.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            wrapper.setVisibility(View.GONE);
            voiceLL.addView(wrapper);

            LinearLayout container = new LinearLayout(ctx);
            container.setGravity(Gravity.CENTER);
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            wrapper.addView(container);


            LinearLayout.LayoutParams timerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            timerParams.setMargins(0, (int) Tool.convertDpToPixel(4, ctx), 0, 0);
            timer.setLayoutParams(timerParams);
            timer.setGravity(Gravity.CENTER_HORIZONTAL);
            timer.setTextColor(ctx.getResources().getColor(R.color.primaryText));
            timer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
            wrapper.addView(timer);

            ImageView deleteIv = new ImageView(ctx);
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            deleteParams.setMargins(0, 0, (int) Tool.convertDpToPixel(20, ctx), 0);
            deleteIv.setLayoutParams(deleteParams);
            deleteIv.setImageDrawable(ctx.getResources().getDrawable(R.drawable.cancel_red));
            deleteIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recordProgress.setVisibility(View.VISIBLE);
                    wrapper.setVisibility(View.GONE);
                    controller.stop();
                    playerProgress.setProgress(0);
                    if (fileName != null) {
                        File file = new File(fileName);
                        if (file.exists())
                            file.delete();
                    }
                }
            });
            container.addView(deleteIv);

            RelativeLayout rl = new RelativeLayout(ctx);
            rl.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            container.addView(rl);

            LinearLayout.LayoutParams playerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            playerProgress.setLayoutParams(playerParams);
            rl.addView(playerProgress);


            RelativeLayout.LayoutParams statusParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            statusParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            statusPlaying.setLayoutParams(statusParams);
            statusPlaying.setImageDrawable(ctx.getResources().getDrawable(R.drawable.play));
            statusPlaying.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (controller.isPlaying()) {
                        controller.stop();
                        controller.setChangePosition(null);
                        controller.setFinishSound(null);
                    } else {
                        controller.setChangePosition(new SenderMediaController.IChangePosition() {
                            @Override
                            public void changePosition(final int millis) {
                                playerProgress.setDuration(controller.getDuration());
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (millis >= playerProgress.getProgress() || (millis > playerProgress.getDuration() - 100 && playerProgress.getProgress() == playerProgress.getDuration() && controller.isPlaying())
                                                || (millis < playerProgress.getDuration() - 100 && playerProgress.getProgress() != playerProgress.getDuration() && controller.isPlaying())) {
                                            playerProgress.setProgress(millis);
                                            timer.setText(String.format("%d:%02d", millis / 1000 / 60, (millis / 1000) % 60));
                                        } else {
                                            playerProgress.setProgress(playerProgress.getDuration());
                                            timer.setText(String.format("%d:%02d", playerProgress.getDuration() / 1000 / 60, (playerProgress.getDuration() / 1000) % 60));
                                        }
                                        if (controller.isPlaying()) {
                                            statusPlaying.setImageResource(R.drawable.pause);
                                        }
                                    }
                                });
                            }
                        });
                        controller.setFinishSound(new SenderMediaController.IFinishSound() {
                            @Override
                            public void finish() {
                                statusPlaying.setImageResource(R.drawable.play);
                                playerProgress.setProgress(0);
                                timer.setText("0:00");
                            }
                        });
                        controller.setSource(fileName);
                    }
                }
            });
            rl.addView(statusPlaying);


            ImageView sendIv = new ImageView(ctx);
            LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            sendParams.setMargins((int) Tool.convertDpToPixel(20, ctx), 0, 0, 0);
            sendIv.setLayoutParams(sendParams);
            sendIv.setImageDrawable(ctx.getResources().getDrawable(R.drawable.send_message));
            sendIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String filename = new String(fileName.toString());
                    fileName = null;
                    listener.onSendVoice(filename, length);
                    controller.stop();
                    playerProgress.setProgress(0);
                }
            });
            container.addView(sendIv);
            voiceLL.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    recordProgress.setVisibility(View.VISIBLE);
                    wrapper.setVisibility(View.GONE);
                    controller.stop();
                    playerProgress.setProgress(0);
                    if (fileName != null) {
                        File file = new File(fileName);
                        if (file.exists())
                            file.delete();
                    }
                }
            });
        }
        return voiceLL;
    }
}
