package utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.easemob.redpacketsdk.bean.RedPacketInfo;
import com.easemob.redpacketsdk.constant.RPConstant;
import com.easemob.redpacketui.R;
import com.easemob.redpacketui.ui.activity.RPRedPacketActivity;
import com.easemob.redpacketui.utils.RPOpenPacketUtil;

/**
 * Created by ustc on 2016/5/31.
 */
public class RedPacketUtil {

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






    /**
     * 进入发红包页面
     *
     * @param activity
     * @param  jsonObject
     * @param requestCode
     */
    public static void startRedPacketActivityForResult(Activity activity, JSONObject jsonObject, int requestCode) {

        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromAvatarUrl =jsonObject.getString("fromAvatarUrl") ;
        redPacketInfo.fromNickName = jsonObject.getString("fromNickName") ;
        //接收者Id或者接收的群Id
        int chatType=jsonObject.getInteger("chatType");
        if (chatType == 1) {
            redPacketInfo.toUserId = jsonObject.getString("userId");
            redPacketInfo.chatType = 1;
        } else if (chatType ==2) {
             redPacketInfo.toGroupId = jsonObject.getString("groupId");
            redPacketInfo.groupMemberCount =jsonObject.getInteger("groupMembersCount");
            redPacketInfo.chatType = 2;
        }
        Intent intent = new Intent(activity, RPRedPacketActivity.class);
        intent.putExtra(RPConstant.EXTRA_MONEY_INFO, redPacketInfo);
        activity.startActivityForResult(intent, requestCode);
    }





    /**
     * 拆红包的方法
     *
     * @param activity       FragmentActivity
     * @param jsonObject
     */
    public static void openRedPacket(final FragmentActivity activity, JSONObject jsonObject, final OpenRedPacketSuccess openRedPacketSuccess) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setCanceledOnTouchOutside(false);
        String messageDirect;
        //接收者头像url 默认值为none
        String toAvatarUrl = jsonObject.getString("toAvatarUrl");
        //接收者昵称 默认值为当前用户ID
        final String toNickname = jsonObject.getString("toNickName");
        //接受者id
        final String toUserId=jsonObject.getString("toUserId");
        String  moneyId = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_ID);


        messageDirect = jsonObject.getString("messageDirect");
        final int chatType=jsonObject.getInteger("chatType");

        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.moneyID = moneyId;
        redPacketInfo.toAvatarUrl = toAvatarUrl;
        redPacketInfo.toNickName = toNickname;
        redPacketInfo.moneyMsgDirect = messageDirect;
        redPacketInfo.chatType = chatType;
        RPOpenPacketUtil.getInstance().openRedPacket(redPacketInfo, activity, new RPOpenPacketUtil.RPOpenPacketCallBack() {
            @Override
            public void onSuccess(String senderId, String senderNickname) {
                openRedPacketSuccess.onSuccess(senderId,senderNickname);
            }

            @Override
            public void showLoading() {
                progressDialog.show();
            }

            @Override
            public void hideLoading() {
                progressDialog.dismiss();
            }

            @Override
            public void onError(String code, String message) {
            }
        });
    }



    public  interface OpenRedPacketSuccess{

        void onSuccess(String senderId, String senderNickname);
     }





}
