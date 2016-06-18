package utils;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.easemob.redpacketsdk.bean.RedPacketInfo;
import com.easemob.redpacketsdk.constant.RPConstant;
import com.easemob.redpacketui.ui.activity.RPRedPacketActivity;

/**
 * Created by ustc on 2016/5/31.
 */
public class RedPacketUtils {

    public static final String EXTRA_RED_PACKET_SENDER_ID = "money_sender_id";
    public static final String MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE = "is_open_money_msg";
    public static final String MESSAGE_ATTR_IS_RED_PACKET_MESSAGE = "is_money_msg";
    public static final String EXTRA_RED_PACKET_SENDER_NAME = "money_sender";
    public static final String EXTRA_RED_PACKET_RECEIVER_NAME = "money_receiver";
    public static final String EXTRA_RED_PACKET_RECEIVER_ID = "money_receiver_id";
    public static final String EXTRA_SPONSOR_NAME = "money_sponsor_name";
    public static final String EXTRA_RED_PACKET_GREETING = "money_greeting";
    public static final String EXTRA_RED_PACKET_ID = "ID";
    public static final String MESSAGE_DIRECT_SEND = "SEND";
    public static final String MESSAGE_DIRECT_RECEIVE = "RECEIVE";
    public static final String KEY_USER_ID = "id";
    public static final String KEY_USER_NAME = "username";
    public static final String KEY_RED_PACKET = "redpacket";
    public static final String KEY_RED_PACKET_USER = "redpacket_user";
    public static final String KEY_TYPE = "type";
    public static final String VALUE_TYPE = "redpacket_taken";


    public static RedPacketInfo initRedPacketInfo_single(String fromNickname, String fromAvatarUrl, String toUserId, int chatType) {
        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromAvatarUrl = fromAvatarUrl;
        redPacketInfo.fromNickName = fromNickname;
        redPacketInfo.toUserId = toUserId;
        redPacketInfo.chatType = chatType;
        return redPacketInfo;
    }

    public static RedPacketInfo initRedPacketInfo_group(String fromNickname, String fromAvatarUrl, String toUserId, int chatType, String toGroupId, int groupMemberCount) {
        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromAvatarUrl = fromAvatarUrl;
        redPacketInfo.fromNickName = fromNickname;
        redPacketInfo.toUserId = toUserId;
        redPacketInfo.chatType = chatType;
        redPacketInfo.toGroupId = toGroupId;
        redPacketInfo.groupMemberCount = groupMemberCount;
        redPacketInfo.chatType = chatType;
        return redPacketInfo;
    }

    public static RedPacketInfo initRedPacketInfo_received(String fromNickname, String fromAvatarUrl, String moneyMsgDirect, int chatType, String moneyId) {
        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.moneyMsgDirect = moneyMsgDirect;
        redPacketInfo.chatType = chatType;
        redPacketInfo.moneyID = moneyId;
        redPacketInfo.toAvatarUrl = fromAvatarUrl;
        redPacketInfo.toNickName = fromNickname;
        return redPacketInfo;
    }

    public static void selectRedPacket(Fragment fragment, String toUserId, String fromNickname, String fromAvatarUrl, int chatType, String tpGroupId, int membersNum, int REQUEST_CODE_SEND_MONEY) {
        Intent intent = new Intent(fragment.getActivity(), RPRedPacketActivity.class); /*接收者Id或者接收的群Id*/
        RedPacketInfo redpacketInfo;
        if (chatType == RPConstant.CHATTYPE_SINGLE)
            redpacketInfo = initRedPacketInfo_single(fromNickname, fromAvatarUrl, toUserId, RPConstant.CHATTYPE_SINGLE);
        else if (chatType == RPConstant.CHATTYPE_GROUP)
            redpacketInfo = initRedPacketInfo_group(fromNickname, fromAvatarUrl, toUserId, RPConstant.CHATTYPE_GROUP, tpGroupId, membersNum);
        else return;
        intent.putExtra(RPConstant.EXTRA_MONEY_INFO, redpacketInfo);
        fragment.startActivityForResult(intent, REQUEST_CODE_SEND_MONEY);
    }
}
