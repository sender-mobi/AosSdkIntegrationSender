package mobi.sender.ui.adapter;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mobi.sender.Bus;
import mobi.sender.R;
import mobi.sender.event.EditMsgReq;
import mobi.sender.event.SyncEvent;
import mobi.sender.model.OperatorModel;
import mobi.sender.model.User;
import mobi.sender.model.msg.AudioMessage;
import mobi.sender.model.msg.FileMessage;
import mobi.sender.model.msg.FormMessage;
import mobi.sender.model.msg.LocationMessage;
import mobi.sender.model.msg.MediaMessage;
import mobi.sender.model.msg.MsgBased;
import mobi.sender.model.msg.StickerMessage;
import mobi.sender.model.msg.TextMessage;
import mobi.sender.tool.MsgCryptFacade;
import mobi.sender.tool.SendListenerImpl;
import mobi.sender.tool.Storage;
import mobi.sender.tool.Tool;
import mobi.sender.tool.fml.FMLRenderer;
import mobi.sender.tool.utils.AttrUtils;
import mobi.sender.tool.utils.ContentUtils;
import mobi.sender.tool.utils.ConvertUtils;
import mobi.sender.tool.utils.DatesUtils;
import mobi.sender.tool.utils.DialogUtils;
import mobi.sender.tool.utils.StringUtils;
import mobi.sender.ui.ChatActivity;
import mobi.sender.ui.window.PicturePreviewWindow;
import mobi.sender.ui.window.map.MapWindow;

import static mobi.sender.tool.Tool.checkEmojiSupport;
import static mobi.sender.tool.Tool.log;

public class MsgRecAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SendListenerImpl.ActionListener {

    private final static int MY_TEXT = 1;
    private final static int MY_MEDIA = 2;
    private final static int MY_AUDIO = 3;
    private final static int MY_LOCATION = 4;
    private final static int MY_STICKER = 5;
    private final static int MY_FILE = 6;
    private final static int OPPO_TEXT = 7;
    private final static int OPPO_MEDIA = 8;
    private final static int OPPO_AUDIO = 9;
    private final static int OPPO_LOCATION = 10;
    private final static int OPPO_STICKER = 11;
    private final static int OPPO_FILE = 12;
    private final static int FORM = 13;
    private final static int ACTION_DELETE = 0;
    private final static int ACTION_EDIT = 1;
    private final static int ACTION_COPY = 2;
    private Activity ctx;
    private List<MsgBased> history = new ArrayList<>();
    private MediaPlayer mp = new MediaPlayer();
    private ValueAnimator animator = new ValueAnimator();
    private int index = -1;
    private int audioLength = -1;
    private FMLRenderer fmlRenderer;
    private OnAdapterListener mListener;

    public MsgRecAdapter(Activity ctx, GetActivity listener, String chatId, List<MsgBased> history,
                         OnAdapterListener editListener) {
        this.ctx = ctx;
        this.history = history;
        fmlRenderer = new FMLRenderer(chatId, listener);
        mListener = editListener;
    }

    public void setHistory(List<MsgBased> newHistory, ScrollToBotomListener listener, int firstVisibleItemPosition) {

        Tool.log("*** newHistory = " + newHistory.size());

        List<MsgBased> oldHistory = history;
        for (int i = 0; i < newHistory.size(); i++) {
            MsgBased newMess = newHistory.get(i);
            long pacId = newMess.getPacketId();
            long locId = newMess.getLocalId();

            if (isAdd(pacId, locId, oldHistory)) {
                //add new mess (THIS LOGIC DISABLE BLICK IN FROMS)
                history.add(newMess);
                sortMess();
                oldHistory = history;

                final int indexM = getIndexByLocalId(newMess.getLocalId(), oldHistory);
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemInserted(indexM);
                        notifyItemChanged(indexM + 1);
                    }
                });

//                ctx.runOnUiThread(new Runnable() {//add (THIS LOGIC JUST NOTIF ALL VIEWS)
//                    @Override
//                    public void run() {
//                        notifyDataSetChanged();
//                    }
//                });

                if (newMess.getFrom().equals(Storage.getInstance(ctx).getMyUserId()) && firstVisibleItemPosition == 0) {
                    listener.scrollToBotom();
                } else {
                    if (firstVisibleItemPosition == 0) {
                        listener.scrollToBotom();
                    } else {
                        listener.updateShowUnreadMess();
                    }
                }

            } else {
                //update mess
                final int indexM = getIndexByLocalId(newMess.getLocalId(), oldHistory);
                MsgBased oldMess = oldHistory.get(indexM);

                if (oldMess instanceof FormMessage) {
                    FormMessage oldM = (FormMessage) oldMess;
                    FormMessage newM = (FormMessage) getMessByPacketId(oldM.getPacketId(), newHistory);

                    if (newM != null && !newM.getView().equals(oldM.getView())) {
                        history.set(i, newM);
                        notifyItemChanged(i);
                    }
                } else {
//                    if (oldMess.getVersionMess() != newMess.getVersionMess()) {
                        history.set(indexM, newMess);
                        notifyItemChanged(indexM);
//                    }
                }
            }
        }
    }

    private boolean isAdd(long packetId, long localId, List<MsgBased> list) {
        for (MsgBased m : list) {
            if (packetId == m.getPacketId() || m.getPacketId() == localId) {
                return false;
            }
            if (localId == m.getLocalId()) {
                return false;
            }
        }
        return true;
    }

    public long getTimeLastMess() {
        if (history.size() != 0) {
            return history.get(0).getTimeVersion();
        }
        return 0;
    }

    private void sortMess() {
        Collections.sort(history, new Comparator<MsgBased>() {
            @Override
            public int compare(MsgBased lhs, MsgBased rhs) {
                return lhs.getCreated() >= rhs.getCreated() ? -1 : 1;
            }
        });
    }

    private MsgBased getMessByPacketId(long packetId, List<MsgBased> listMsg) {
        for (MsgBased m : listMsg) {
            if (packetId == m.getPacketId()) {
                return m;
            }
        }
        return null;
    }

    private MsgBased getMessByLocalId(long localId, List<MsgBased> listMsg) {
        for (MsgBased m : listMsg) {
            if (localId == m.getLocalId()) {
                return m;
            }
        }
        return null;
    }

    private MsgBased getMessByLocalId(int localId, List<MsgBased> listMsg) {
        for (MsgBased m : listMsg) {
            if (localId == m.getLocalId()) {
                return m;
            }
        }
        return null;
    }

    private int getIndexByLocalId(int localId, List<MsgBased> listMsg) {
        for (int i = 0; i < listMsg.size(); i++) {
            if (localId == listMsg.get(i).getLocalId()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void OnActionListener(final String s, final int pos) {
        MsgBased m = history.get(pos);
        ((FormMessage) m).setView(s);
        history.set(pos, m);
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(pos);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case MY_TEXT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_my_text, parent, false);
                holder = new TextHolder(v);
                checkEmojiSupport(ctx, ((TextHolder) holder).tvText);
                break;
            case OPPO_TEXT:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_oppo_text, parent, false);
                holder = new TextHolder(v);
                checkEmojiSupport(ctx, ((TextHolder) holder).tvText);
                break;
            case MY_MEDIA:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_my_media, parent, false);
                holder = new MediaHolder(v);
                break;
            case OPPO_MEDIA:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_oppo_media, parent, false);
                holder = new MediaHolder(v);
                break;
            case MY_LOCATION:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_my_location, parent, false);
                holder = new LocationHolder(v);
                break;
            case OPPO_LOCATION:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_oppo_location, parent, false);
                holder = new LocationHolder(v);
                break;
            case MY_STICKER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_my_sticker, parent, false);
                holder = new StickerHolder(v);
                break;
            case OPPO_STICKER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_oppo_sticker, parent, false);
                holder = new StickerHolder(v);
                break;
            case MY_FILE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_my_file, parent, false);
                holder = new FileHolder(v);
                break;
            case OPPO_FILE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_oppo_file, parent, false);
                holder = new FileHolder(v);
                break;
            case MY_AUDIO:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_my_audio, parent, false);
                holder = new AudioHolder(v);
                break;
            case OPPO_AUDIO:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_oppo_audio, parent, false);
                holder = new AudioHolder(v);
                break;
            case FORM:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mes_form, parent, false);
                holder = new FormHolder(v);
                break;
        }
        return holder;
    }


    private void decr(final TextMessage tm, final TextView tv, final int position) {

        final TextMessage textMessage = new MsgCryptFacade(ctx).tryDecrypt(tm);

        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (textMessage.getText().equals("")) {
                    tv.setText(R.string.msg_mess_deleted);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.txt_size_medium));
                } else {
                    if (Tool.isSmile(textMessage.getText())) {
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.txt_size_extra_large));
                    } else {
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.txt_size_medium));
                    }
                    tv.setText(textMessage.getText());
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        mListener.onBindStarted(position);

        final int itemType = getItemViewType(position);
        final MsgBased m = history.get(position);
        boolean mine = MsgBased.isMine(m, ctx);

        TextMessage tm = null;
        if (m instanceof TextMessage) {
            tm = (TextMessage) m;
            TextView tv = (TextView) holder.itemView.findViewById(R.id.msg_text);
            decr(tm, tv, position);
        }

        //make oppo avatar, name, long click and bubbles
        if (itemType >= OPPO_TEXT && itemType < OPPO_FILE) {
            ImageView ivAvatar = (ImageView) holder.itemView.findViewById(R.id.msg_icon);

            final Storage storage = Storage.getInstance(ctx);
            String iconUrl = storage.getUserIcon(m.getFrom());

            if (storage.getOperUsers(m.getChatId(), m.getFrom()) != null) { // FOR OPERATORS
                OperatorModel operModel = storage.getOperUsers(m.getChatId(), m.getFrom());
                String url = operModel.getPhoto();
                if (url == null || !url.startsWith("http")) {
                    ivAvatar.setImageResource(R.drawable.ic_acc_bg);
                } else {
                    Tool.loadImage(ctx, url, ivAvatar, 0, true);
                }

            } else {

                if (iconUrl == null || !iconUrl.startsWith("http")) {
                    ivAvatar.setImageResource(R.drawable.ic_acc_bg);
                } else {
                    Tool.loadImage(ctx, iconUrl, ivAvatar, 0, true);
                }
            }

            ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!storage.isOperChat(m.getChatId()) && !storage.isCompany(m.getChatId())) {
                        ctx.startActivity(new Intent(ctx, ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, User.P2P_CHAT_PREFIX + m.getFrom()));
                        ctx.finish();
                    }
                }
            });

            //make long click on text msg
            if (m instanceof TextMessage) {
                final TextMessage finalTm = tm;
                final boolean[] isLongClick = new boolean[1];

                final TextView tv = (TextView) holder.itemView.findViewById(R.id.msg_text);
                tv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        isLongClick[0] = true;
                        makeCopyAlertDialog(finalTm);
                        return false;
                    }
                });

                tv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP && isLongClick[0]) {
                            isLongClick[0] = false;
                            return true;
                        }
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            isLongClick[0] = false;
                        }
                        return v.onTouchEvent(event);
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        makeCopyAlertDialog(finalTm);
                        return false;
                    }
                });
            } else {
                holder.itemView.setOnLongClickListener(null);
            }

            //make bubbles
            TextView tvName = (TextView) holder.itemView.findViewById(R.id.msg_name);
            if (tvName != null) {
                if (!Tool.isP2PChat(m.getChatId())) {
                    String name = Storage.getInstance(ctx).getUserName(m.getFrom());
                    tvName.setVisibility(View.VISIBLE);
                    tvName.setText(name);
                } else {
                    tvName.setVisibility(View.GONE);
                    tvName.setText("");
                }
            }


            if (isSameMessage(holder.getAdapterPosition())) {
                ivAvatar.setVisibility(View.INVISIBLE);
                tvName.setVisibility(View.GONE);
            } else {
                ivAvatar.setVisibility(View.VISIBLE);
                if (tvName.getVisibility() != View.GONE) {
                    tvName.setVisibility(View.VISIBLE);
                }
            }


        }//...end

        //make time and date
        if (itemType != FORM) {
            //time
            ((TextView) holder.itemView.findViewById(R.id.msg_time)).setText(Tool.parseLongToDate(ctx, m.getCreated()));

            //date
            TextView tvDate = (TextView) holder.itemView.findViewById(R.id.msg_date);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);

            Date past = DatesUtils.getDate(m.getCreated());
            Date now = new Date();
            now.setTime(cal.getTimeInMillis());
            int diff = (int) TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

            String str = "";
            if (diff == 0) {
                str = ctx.getString(R.string.msg_today);
            } else if (diff == 1) {
                str = ctx.getString(R.string.msg_yesterday);
            } else {
                str = DateFormat.getLongDateFormat(ctx).format(new Date(m.getCreated()));
            }
            tvDate.setText(str);

            if (position != getItemCount() - 1) {
                long timeSt1 = history.get(position).getCreated();
                long timeSt2 = history.get(position + 1).getCreated();

                if (DatesUtils.isThatOneDay(timeSt1, timeSt2)) {
                    tvDate.setVisibility(View.GONE);
                } else {
                    tvDate.setVisibility(View.VISIBLE);
                }
            }

            //make form babble...
            if (m.isToOper() && !(m instanceof FormMessage)) {//operator
                holder.itemView.findViewById(R.id.ll_root).setBackgroundDrawable(AttrUtils.getDrawableByAttr(ctx, mine ? R.attr.attr_bubbleMyHide : R.attr.attr_bubbleOppoHide));
            } else if (m instanceof TextMessage && tm.isEncrypted()) {//encr
                ((TextHolder) holder).llRoot.setBackgroundDrawable(AttrUtils.getDrawableByAttr(ctx, mine ? R.attr.attr_bubbleMyEncr : R.attr.attr_bubbleOppoEncr));
            } else {//other
                holder.itemView.findViewById(R.id.ll_root).setBackgroundDrawable(AttrUtils.getDrawableByAttr(ctx, mine ? R.attr.attr_bubbleMy : R.attr.attr_bubbleOppo));
            }
            //...end


        }//...end

        //make status msg and long click
        if (itemType >= MY_TEXT && itemType <= MY_FILE) {
            TextView tvStatus = (TextView) holder.itemView.findViewById(R.id.msg_status);
            tvStatus.setText("");
            int status = m.getStatus();
            int chatStatus = Storage.getInstance(ctx).getChatStatus(m.getChatId());

//            Tool.log("*** mesSatatus = "+status+", chatStatus = "+chatStatus+", pi = "+m.getPacketId());

            if (chatStatus > status) {
                status = chatStatus;
            }
            switch (status) {
                case 0:
                    tvStatus.setText(R.string.msg_not_sent);
                    break;
                case 1:
                    tvStatus.setText(R.string.msg_sent);
                    break;
                case 2:
                    tvStatus.setText(R.string.msg_delivered);
                    break;
                case 3:
                    tvStatus.setText(R.string.msg_viewed);
                    break;
                default:
                    tvStatus.setText("");
                    break;
            }

            if (position == 0 || status == 0) {
                tvStatus.setVisibility(View.VISIBLE);
            } else {
                tvStatus.setVisibility(View.GONE);
            }

            final boolean[] isLongClick = new boolean[1];

            if (m instanceof TextMessage) {
                final TextMessage finalTm = tm;
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        MyLongClick(m, finalTm);
                        return false;
                    }
                });

                final TextView tv = (TextView) holder.itemView.findViewById(R.id.msg_text);
                tv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        isLongClick[0] = true;
                        MyLongClick(m, finalTm);
                        return false;
                    }
                });

                tv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP && isLongClick[0]) {
                            isLongClick[0] = false;
                            return true;
                        }
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            isLongClick[0] = false;
                        }
                        return v.onTouchEvent(event);
                    }
                });

            } else {
                holder.itemView.setOnLongClickListener(null);
            }


        }//...end

//        if (m instanceof TextMessage) {
//            if (tm.getText().equals("")) {
//                ((TextHolder) holder).tvText.setText(R.string.msg_mess_deleted);
//                ((TextHolder) holder).tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
//                        ctx.getResources().getDimension(R.dimen.txt_size_medium));
//            } else {
//                if (Tool.isSmile(tm.getText())) {
//                    ((TextHolder) holder).tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.txt_size_extra_large));
//                } else {
//                    ((TextHolder) holder).tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.txt_size_medium));
//                }
//                ((TextHolder) holder).tvText.setText(tm.getText());
//            }
//            holder.itemView.setOnClickListener(null);
/*
        } else */
        if (m instanceof MediaMessage) {
            // Image & video message
            final MediaMessage mm = (MediaMessage) m;

            ImageView ivImage = ((MediaHolder) holder).ivImage;
            final ProgressBar pb = (ProgressBar) holder.itemView.findViewById(R.id.msg_pb);
            final String urlEncoded = ConvertUtils.encodeString(mm.getPreview());


            if (mm.getPreview() != null && mm.getPreview().startsWith("http")) {
                pb.setVisibility(View.VISIBLE);
                Picasso.with(ctx)
                        .load(urlEncoded)
                        .into(ivImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                pb.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                pb.setVisibility(View.GONE);
                                log("Error loading image, msg packetId: " + mm.getPacketId() + " ,msg url: " + mm.getUrl());
                            }
                        });

            } else if (!mm.getPreview().equals("") && mm.getPreview().startsWith("/")) {
                pb.setVisibility(View.VISIBLE);
                Picasso.with(ctx)
                        .load(new File(mm.getPreview()))
                        .into(ivImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                pb.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                pb.setVisibility(View.GONE);
                                log("Error loading image, msg packetId: " + mm.getPacketId() + " ,msg url: " + mm.getUrl());
                            }
                        });

            } else if (mm.getPreview().equals("") && !mm.getUrl().equals("")) {
                pb.setVisibility(View.VISIBLE);
                Picasso.with(ctx)
                        .load(ConvertUtils.encodeString(mm.getUrl()))
                        .into(ivImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                pb.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                pb.setVisibility(View.GONE);
                                log("Error loading image, msg packetId: " + mm.getPacketId()
                                        + " ,msg url: " + mm.getUrl());
                            }
                        });
            }

            ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((MediaMessage) m).getType().equals(MediaMessage.TYPE_VIDEO)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(mm.getUrl()), "video");
                        ctx.startActivity(Intent.createChooser(intent, "Choose video player"));
                    } else {
                        new PicturePreviewWindow(ctx,
                                urlEncoded,
                                ConvertUtils.encodeString(mm.getUrl()),
                                mm.getName(),
                                mm.getType()).show();
                    }
                }
            });
            // if it's video
            if (((MediaMessage) m).getType().equals(MediaMessage.TYPE_VIDEO)) {
                ((MediaHolder) holder).ivPlay.setVisibility(View.VISIBLE);
            } else {
                ((MediaHolder) holder).ivPlay.setVisibility(View.GONE);
            }

        } else if (m instanceof LocationMessage) {
            //audio message
            final LocationMessage lm = (LocationMessage) m;
            TextView tvLocation = ((LocationHolder) holder).tvLocation;

            tvLocation.setPaintFlags(tvLocation.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tvLocation.setText(lm.getTextMsg());

            tvLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MapWindow(ctx, lm.getLat(), lm.getLon(), MapWindow.MAP_JUST_SHOW).show();
                }
            });

        } else if (m instanceof StickerMessage) {
            //sticker message
            final StickerMessage sm = (StickerMessage) m;

            String src = sm.getImage();
            if (!src.startsWith("http")) {
                src = "https://s.sender.mobi/stickers/" + src + ".png";
            }

            ImageView iv = (ImageView) holder.itemView.findViewById(R.id.msg_image);
            Picasso.with(ctx)
                    .load(src)
                    .into(iv, new Callback() {
                        @Override
                        public void onSuccess() {
                            ((StickerHolder) holder).pbProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                        }
                    });

        } else if (m instanceof FileMessage) {
            final FileMessage fm = (FileMessage) m;

            if (fm.getUrl().startsWith("http")) {
                ((FileHolder) holder).tvName.setText(fm.getName());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(fm.getUrl()));
                        ctx.startActivity(i);
                    }
                });
            } else {
                ((FileHolder) holder).tvName.setText(ContentUtils.getFileName(Uri.parse(fm.getUrl()), ctx));
                ((FileHolder) holder).llRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String type = fm.getType();
                        if (type == null)
                            type = "*/*";

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(fm.getUrl()), type);
                        try {
                            ctx.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ctx, R.string.tst_no_found_app, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

//            ((FileHolder) holder).llRoot.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    Tool.log("***fm.getUrl() = "+fm.getUrl()+", name = "+fm.getName());
//                    Toast.makeText(ctx, R.string.tst_link_copied, Toast.LENGTH_SHORT).show();
//                    StringUtils.setStringToBuffer(ctx, fm.getUrl());
//                    return true;
//                }
//            });

            ((FileHolder) holder).ivIcon.setImageResource(R.drawable.ic_file);
        } else if (m instanceof AudioMessage) {
            //audio message
            final AudioMessage am = (AudioMessage) m;

            final TextView tvMsg = ((AudioHolder) holder).tvName;
            final ImageView ivAudio = ((AudioHolder) holder).ivIcon;

            final int length = am.getLength();
            tvMsg.setText(DateUtils.formatElapsedTime(length));

            ivAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (index != holder.getAdapterPosition() && index != -1) {
                        refreshMediaPlayer();
                        mListener.onUpdatePlayer(index, audioLength);
                        animator.cancel();
                        animator = new ValueAnimator();
                    }

                    if (!mp.isPlaying()) {
                        index = holder.getAdapterPosition();
                        audioLength = am.getLength();
                        ivAudio.setImageResource(R.drawable.ic_pause_24dp);

                        try {
                            Uri uri = Uri.parse(am.getUrl());
                            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mp.setDataSource(ctx, uri);
                            mp.prepare();
                            mp.start();

                            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                    animator.setObjectValues(length, 1);
                                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            int num = Integer.valueOf(animation.getAnimatedValue() + "");
                                            String text = DateUtils.formatElapsedTime(num);
                                            tvMsg.setText(String.valueOf(text));
                                        }
                                    });
                                    animator.setEvaluator(new TypeEvaluator<Integer>() {
                                        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
                                            return Math.round(startValue + (endValue - startValue) * fraction);
                                        }
                                    });
                                    animator.setDuration(length * 1000);
                                    animator.start();
                                }
                            });

                            // stop listener media player
                            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                public void onCompletion(MediaPlayer mp) {
                                    refreshMediaPlayer();
                                    ivAudio.setImageResource(R.drawable.ic_play_arrow_black24dp);
                                    int length = am.getLength();
                                    tvMsg.setText(DateUtils.formatElapsedTime(length));
                                }
                            });


                        } catch (Exception e) {
                            Log.d("dag", "exep e = " + e);
                        }

                    } else {
                        index = -1;
                        audioLength = -1;
                        ivAudio.setImageResource(R.drawable.ic_play_arrow_black24dp);
                        int length = am.getLength();
                        tvMsg.setText(DateUtils.formatElapsedTime(length));

                        if (mp.isPlaying()) {
                            animator.cancel();
                            refreshMediaPlayer();
                        }
                    }
                }

            });
        } else if (m instanceof FormMessage) {
            // FML message
            final FormMessage fm = (FormMessage) m;

            try {
                LinearLayout root = ((FormHolder) holder).llForm;
                root.removeAllViews();
                root.addView(fmlRenderer.makeView(new JSONObject(fm.getView()), new SendListenerImpl(fm, fm.getChatId(), ctx, position, this)));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private boolean isSameMessage(int position) {
        return position != getItemCount() - 1 && history.get(position).getFrom().equals(history.get(position + 1).getFrom());
    }

    private void MyLongClick(final MsgBased m, final TextMessage tm) {
        if (!tm.getText().equals("")) {
            long timeDiff = (System.currentTimeMillis() - m.getCreated()) / 60000;

            if (timeDiff < 30) {
                CharSequence[] actions = new CharSequence[]{ctx.getString(R.string.dlg_delete), ctx.getString(R.string.dlg_edit), ctx.getString(R.string.dlg_copy)};
                DialogUtils.itemsDialog(ctx, R.string.dlg_select_action, actions, new DialogUtils.OnChooseListener() {
                    @Override
                    public void onSelect(int position) {
                        switch (position) {
                            case ACTION_DELETE:
                                Bus.getInstance().post(new EditMsgReq(m.getChatId(), "", m.getPacketId(), new SyncEvent.SRespListener() {
                                    @Override
                                    public void onResponse(JSONObject data) {
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(ctx, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }));
                                break;
                            case ACTION_EDIT:
//                                ((ChatActivity) ctx).getBar().onEditMessage(tm);
                                mListener.onEdit(tm);
                                break;
                            case ACTION_COPY:
                                StringUtils.setStringToBuffer(ctx, tm.getText());
                                break;
                        }
                    }
                });
            } else {
                makeCopyAlertDialog(tm);
            }
        }
    }

    private void makeCopyAlertDialog(final TextMessage tm) {
        CharSequence[] actions = new CharSequence[]{ctx.getString(R.string.dlg_copy)};
        DialogUtils.itemsDialog(ctx, R.string.dlg_select_action, actions, new DialogUtils.OnChooseListener() {
            @Override
            public void onSelect(int position) {
                StringUtils.setStringToBuffer(ctx, tm.getText());
            }
        });
    }

    private void refreshMediaPlayer() {
        mp.stop();
        mp.release();
        mp = null;
        mp = new MediaPlayer();
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    @Override
    public int getItemViewType(int position) {
        final MsgBased m = history.get(position);
        boolean mine = MsgBased.isMine(m, ctx);

        if (m instanceof TextMessage) {
            if (mine) {
                return MY_TEXT;
            } else {
                return OPPO_TEXT;
            }
        } else if (m instanceof MediaMessage) {
            if (mine) {
                return MY_MEDIA;
            } else {
                return OPPO_MEDIA;
            }
        } else if (m instanceof AudioMessage) {
            if (mine) {
                return MY_AUDIO;
            } else {
                return OPPO_AUDIO;
            }
        } else if (m instanceof LocationMessage) {
            if (mine) {
                return MY_LOCATION;
            } else {
                return OPPO_LOCATION;
            }
        } else if (m instanceof StickerMessage) {
            if (mine) {
                return MY_STICKER;
            } else {
                return OPPO_STICKER;
            }
        } else if (m instanceof FileMessage) {
            if (mine) {
                return MY_FILE;
            } else {
                return OPPO_FILE;
            }
        } else {
            return FORM;
        }
    }

    public interface GetActivity {
        Activity getAct();
    }

    private static class FormHolder extends RecyclerView.ViewHolder {
        LinearLayout llForm;

        FormHolder(View v) {
            super(v);
            llForm = (LinearLayout) v.findViewById(R.id.msg_form_root);
        }
    }

    private static class ParentHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvTime;
        LinearLayout llRoot;
        TextView tvStatus;
        ImageView ivAvatar;
        TextView tvName;

        ParentHolder(View v) {
            super(v);
            tvDate = (TextView) v.findViewById(R.id.msg_date);
            tvTime = (TextView) v.findViewById(R.id.msg_time);
            llRoot = (LinearLayout) v.findViewById(R.id.ll_root);
            tvStatus = (TextView) v.findViewById(R.id.msg_status);
            tvName = (TextView) v.findViewById(R.id.msg_name);
            ivAvatar = (ImageView) v.findViewById(R.id.msg_icon);
        }
    }

    private static class TextHolder extends ParentHolder {
        TextView tvText;

        TextHolder(View v) {
            super(v);
            tvText = (TextView) v.findViewById(R.id.msg_text);
        }
    }

    private static class MediaHolder extends ParentHolder {
        ImageView ivImage;
        ImageView ivPlay;
        ProgressBar pbProgress;

        MediaHolder(View v) {
            super(v);
            ivImage = (ImageView) v.findViewById(R.id.msg_image);
            ivPlay = (ImageView) v.findViewById(R.id.msg_play);
            pbProgress = (ProgressBar) v.findViewById(R.id.msg_pb);
        }
    }

    private static class LocationHolder extends ParentHolder {
        ImageView ivAudio;
        TextView tvAudioText;
        TextView tvLocation;

        LocationHolder(View v) {
            super(v);
            ivAudio = (ImageView) v.findViewById(R.id.msg_audio_play);
            tvAudioText = (TextView) v.findViewById(R.id.msg_text);
            tvLocation = (TextView) v.findViewById(R.id.msg_location);
        }
    }

    private static class StickerHolder extends ParentHolder {
        ImageView ivSticker;
        ProgressBar pbProgress;

        StickerHolder(View v) {
            super(v);
            ivSticker = (ImageView) v.findViewById(R.id.msg_image);
            pbProgress = (ProgressBar) v.findViewById(R.id.msg_pb);
        }
    }

    private static class FileHolder extends ParentHolder {
        ImageView ivIcon;
        TextView tvName;

        FileHolder(View v) {
            super(v);
            ivIcon = (ImageView) v.findViewById(R.id.msg_audio_play);
            tvName = (TextView) v.findViewById(R.id.msg_text);
        }
    }

    private static class AudioHolder extends ParentHolder {
        ImageView ivIcon;
        TextView tvName;

        AudioHolder(View v) {
            super(v);
            ivIcon = (ImageView) v.findViewById(R.id.msg_audio_play);
            tvName = (TextView) v.findViewById(R.id.msg_text);
        }
    }

    //Listener
    public interface OnAdapterListener {
        void onEdit(TextMessage tm);

        void onUpdatePlayer(int index, int timeLength);

        void onBindStarted(int position);
    }

    public interface ScrollToBotomListener {
        void scrollToBotom();

        void updateShowUnreadMess();
    }
}

