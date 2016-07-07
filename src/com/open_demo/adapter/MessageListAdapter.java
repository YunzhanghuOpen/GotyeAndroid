package com.open_demo.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeChatTarget;
import com.gotye.api.GotyeChatTargetType;
import com.gotye.api.GotyeCustomerService;
import com.gotye.api.GotyeGroup;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeMessageType;
import com.gotye.api.GotyeRoom;
import com.gotye.api.GotyeUser;
import com.open_demo.R;
import com.open_demo.main.MessageFragment;
import com.open_demo.util.BitmapUtil;
import com.open_demo.util.CheckRedPacketMessageUtil;
import com.open_demo.util.ImageCache;
import com.open_demo.util.TimeUtil;

import java.util.List;

import utils.RedPacketConstant;

public class MessageListAdapter extends BaseAdapter {
    private MessageFragment messageFragment;
    private List<GotyeChatTarget> sessions;
    private GotyeAPI api;
    private GotyeUser currentLoginUser;

    public MessageListAdapter(MessageFragment messageFragment,
                              List<GotyeChatTarget> sessions) {
        this.messageFragment = messageFragment;
        this.sessions = sessions;
        api = GotyeAPI.getInstance();
        currentLoginUser = api.getLoginUser();
    }

    static class ViewHolder {
        ImageView icon;
        TextView title, content, time, count;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return sessions.size();
    }

    @Override
    public GotyeChatTarget getItem(int arg0) {
        // TODO Auto-generated method stub
        return sessions.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        // TODO Auto-generated method stub
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        GotyeChatTarget t = sessions.get(position);
        if (t.getName().equals(MessageFragment.fixName)) {
            return 0;
        } else {
            return 1;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint({"NewApi", "InflateParams"})
    @Override
    public View getView(int arg0, View view, ViewGroup arg2) {
        // TODO Auto-generated method stub
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(messageFragment.getActivity()).inflate(
                    R.layout.item_delete, null);
            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
            viewHolder.title = (TextView) view.findViewById(R.id.title_tx);
            viewHolder.content = (TextView) view.findViewById(R.id.content_tx);
            viewHolder.time = (TextView) view.findViewById(R.id.time_tx);
            viewHolder.count = (TextView) view.findViewById(R.id.count);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final GotyeChatTarget session = getItem(arg0);
        Log.d("offLine", "session" + session);
        if (getItemViewType(arg0) == 0) {
            viewHolder.title.setText(session.getName());
            viewHolder.content.setVisibility(View.GONE);
            viewHolder.icon.setImageResource(R.drawable.contact_group);
            viewHolder.time.setVisibility(View.GONE);
            int count = api.getUnreadNotifyCount();
            if (count > 0) {
                viewHolder.count.setVisibility(View.VISIBLE);
                viewHolder.count.setText(String.valueOf(count));
            } else {
                viewHolder.count.setVisibility(View.GONE);
            }

        } else {
            String title = "", content = "";
            viewHolder.content.setVisibility(View.VISIBLE);
            // 获取该session最后一条消息记录
            GotyeMessage lastMsg = api.getLastMessage(session);
            if (lastMsg == null) {
                return view;
            }

            // time请*1000还原成正常时间
            String lastMsgTime = TimeUtil
                    .dateToMessageTime(lastMsg.getDate() * 1000);
            viewHolder.time.setText(lastMsgTime);

            if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeText) {

                JSONObject redpacketJSON = CheckRedPacketMessageUtil.isRedPacketMessage(lastMsg);
                JSONObject redpacketAckJSON = CheckRedPacketMessageUtil.isRedPacketAckedMessage(lastMsg);
                if (redpacketJSON != null) {
                    String greetings = redpacketJSON.getString(RedPacketConstant.EXTRA_RED_PACKET_GREETING);
                    content = "[" + messageFragment.getActivity().getResources().getString(R.string.gotye_luckymoney) + "]"+greetings;
                } else if (redpacketAckJSON != null) {
                    String currentUserId = currentLoginUser.getName();   //当前登陆用户id
                    String recieveUserId = redpacketAckJSON.getString(RedPacketConstant.EXTRA_RED_PACKET_RECEIVER_ID);//红包接收者id
                    String recieveUserNick = redpacketAckJSON.getString(RedPacketConstant.EXTRA_RED_PACKET_RECEIVER_NAME);//红包接收者昵称
                    String sendUserId = redpacketAckJSON.getString(RedPacketConstant.EXTRA_RED_PACKET_SENDER_ID);//红包发送者id
                    String sendUserNick = redpacketAckJSON.getString(RedPacketConstant.EXTRA_RED_PACKET_SENDER_NAME);//红包发送者昵称
                    //发送者和领取者都是自己-
                    if (currentUserId.equals(recieveUserId) && currentUserId.equals(sendUserId)) {
                        content = messageFragment.getActivity().getResources().getString(R.string.money_msg_take_money);

                    } else if (currentUserId.equals(sendUserId)) {
                        //我仅仅是发送者
                        content = String.format(messageFragment.getActivity().getResources().getString(R.string.money_msg_someone_take_money), recieveUserNick);
                    } else if (currentUserId.equals(recieveUserId)) {
                        //我仅仅是接收者
                        content = String.format(messageFragment.getActivity().getResources().getString(R.string.money_msg_take_someone_money), sendUserNick);
                    }

                } else {
                    content = "文本消息：" + ":" + lastMsg.getText();

                }

            } else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeImage) {
                content = "图片消息";
            } else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
                content = "语音消息";
            } else if (lastMsg.getType() == GotyeMessageType.GotyeMessageTypeUserData) {
                content = "自定义消息";
            }

            if (session.getType() == GotyeChatTargetType.GotyeChatTargetTypeUser) {
                setIcon(viewHolder.icon, session);
                GotyeUser user = api.getUserDetail(session, false);
                if (user != null) {
                    if (TextUtils.isEmpty(user.getNickname())) {
                        title = "好友：" + user.getName();
                    } else {
                        title = "好友：" + user.getNickname();
                    }
                } else {
                    title = "好友：" + session.getName();
                }
            } else if (session.getType() == GotyeChatTargetType.GotyeChatTargetTypeRoom) {
                setIcon(viewHolder.icon, session);
                GotyeRoom room = (GotyeRoom) session;
                room = api.getRoomDetail(room);
                if (room != null) {
                    if (TextUtils.isEmpty(room.getRoomName())) {
                        title = "聊天室：" + room.getId();
                    } else {
                        title = "聊天室：" + room.getRoomName();
                    }
                } else {
                    title = "聊天室：" + session.getId();
                }

            } else if (session.getType() == GotyeChatTargetType.GotyeChatTargetTypeGroup) {
//				GotyeGroup group = api.getGroupDetail(session.getId(), false);
                GotyeGroup group = api.getGroupDetail(session, false);
                setIcon(viewHolder.icon, session);
                if (group != null) {
                    if (TextUtils.isEmpty(group.getGroupName())) {
                        title = "群：" + group.getId();
                    } else {
                        title = "群：" + group.getGroupName();
                    }
                } else {
                    title = "群：" + session.getId();
                }

            } else if (session.getType() == GotyeChatTargetType.GotyeChatTargetTypeCustomerService) {
                viewHolder.icon.setImageResource(R.drawable.contact_group);
                GotyeCustomerService service = (GotyeCustomerService) session;
                if (service != null) {
                    title = "客服：" + String.valueOf(service.getGroupId());
                }
            }
            viewHolder.title.setText(title);
            viewHolder.content.setText(content);
            int count = api.getUnreadMessageCount(session);
            if (count > 0) {
                viewHolder.count.setVisibility(View.VISIBLE);
                viewHolder.count.setText(String.valueOf(count));
            } else {
                viewHolder.count.setVisibility(View.GONE);
            }
        }
        return view;
    }

    private void setIcon(ImageView iconView, GotyeChatTarget target) {
        String name;
        String path;
        if (target.getType() == GotyeChatTargetType.GotyeChatTargetTypeUser) {
            name = target.getName();
            path = api.getUserDetail(target, false).getIcon().getPath();
        } else {
            name = target.getId() + "";
            if (target.getType() == GotyeChatTargetType.GotyeChatTargetTypeGroup) {
                path = api.getGroupDetail(target, false).getIcon().getPath();
            } else {
                path = api.getRoomDetail(target).getIcon().getPath();
            }
        }

        Bitmap bmp = ImageCache.getInstance().get(name);
        if (bmp != null) {
            iconView.setImageBitmap(bmp);
        } else {
            bmp = BitmapUtil.getBitmap(path);
            if (bmp != null) {
                iconView.setImageBitmap(bmp);
                ImageCache.getInstance().put(name, bmp);
            } else {
                iconView.setImageResource(R.drawable.mini_avatar_shadow);
            }
        }
    }

    public void setData(List<GotyeChatTarget> sessions) {
        // TODO Auto-generated method stub
        this.sessions = sessions;
        notifyDataSetChanged();
    }
}
