package utils;

/**
 * Created by max on 16/5/24.
 */
public class RedPacketConstant {
    //以下常量值切勿更改
    public static final String EXTRA_RED_PACKET_SENDER_ID = "money_sender_id";
    public static final String EXTRA_RED_PACKET_RECEIVER_ID = "money_receiver_id";
    public static final String MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE = "is_open_money_msg";
    public static final String MESSAGE_ATTR_IS_RED_PACKET_MESSAGE = "is_money_msg";
    public static final String EXTRA_RED_PACKET_SENDER_NAME = "money_sender";
    public static final String EXTRA_RED_PACKET_RECEIVER_NAME = "money_receiver";
    public static final String EXTRA_SPONSOR_NAME = "money_sponsor_name";
    public static final String EXTRA_RED_PACKET_GREETING = "money_greeting";
    public static final String EXTRA_RED_PACKET_ID = "ID";

    //点击红包按钮，需要传给红包SDK的数据 json的键值对的key值常量
    public static final String KEY_FROM_NICK_NAME = "fromNickName";
    public static final String KEY_FROM_AVATAR_URL = "fromAvatarUrl";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_CHAT_TYPE = "chatType";
    public static final String KEY_GROUP_ID = "groupId";
    public static final String KEY_GROUO_MEMBERS_COUNT = "groupMembersCount";

    //点击红包消息，需要传给红包SDK的数据，json的键值对的key值常量
    public static final String KEY_TO_NICK_NAME = "toNickName";
    public static final String KEY_TO_AVATAR_URL = "toAvatarUrl";
    public static final String KEY_MESSAGE_DIRECT= "messageDirect";


}

