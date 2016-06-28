package utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.easemob.redpacketsdk.bean.AuthData;
import com.easemob.redpacketsdk.bean.RedPacketInfo;
import com.easemob.redpacketsdk.constant.RPConstant;
import com.easemob.redpacketui.ui.activity.RPChangeActivity;
import com.easemob.redpacketui.ui.activity.RPRedPacketActivity;
import com.easemob.redpacketui.utils.RPOpenPacketUtil;

/**
 * Created by ustc on 2016/5/31.
 */
public class RedPacketUtil {
    /**
     * 进入发红包页面
     *
     * @param activity
     * @param jsonObject
     * @param requestCode
     */
    public static void startRedPacketActivityForResult(Activity activity, JSONObject jsonObject, int requestCode) {

        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromAvatarUrl = jsonObject.getString(RedPacketConstant.KEY_FROM_AVATAR_URL);
        redPacketInfo.fromNickName = jsonObject.getString(RedPacketConstant.KEY_FROM_NICK_NAME);
        //接收者Id或者接收的群Id
        int chatType = jsonObject.getInteger(RedPacketConstant.KEY_CHAT_TYPE);
        if (chatType == 1) {
            redPacketInfo.toUserId = jsonObject.getString(RedPacketConstant.KEY_USER_ID);
            redPacketInfo.chatType = 1;
        } else if (chatType == 2) {
            redPacketInfo.toGroupId = jsonObject.getString(RedPacketConstant.KEY_GROUP_ID);
            redPacketInfo.groupMemberCount = jsonObject.getInteger(RedPacketConstant.KEY_GROUO_MEMBERS_COUNT);
            redPacketInfo.chatType = 2;
        }
        String currentId=jsonObject.getString(RedPacketConstant.KEY_CURRENT_ID);
        Intent intent = new Intent(activity, RPRedPacketActivity.class);
        intent.putExtra(RPConstant.EXTRA_MONEY_INFO, redPacketInfo);
        intent.putExtra(RPConstant.EXTRA_AUTH_INFO, AuthDataUtils.getInstance().getAuthData(currentId));
        activity.startActivityForResult(intent, requestCode);
    }


    /**
     * 拆红包的方法
     *
     * @param activity   FragmentActivity
     * @param jsonObject
     */
    public static void openRedPacket(final FragmentActivity activity, JSONObject jsonObject, final OpenRedPacketSuccess openRedPacketSuccess) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setCanceledOnTouchOutside(false);
        String messageDirect;
        //接收者头像url 默认值为none
        String toAvatarUrl = jsonObject.getString(RedPacketConstant.KEY_TO_AVATAR_URL);
        //接收者昵称 默认值为当前用户ID
        final String toNickname = jsonObject.getString(RedPacketConstant.KEY_TO_NICK_NAME);
        String moneyId = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_ID);
        messageDirect = jsonObject.getString(RedPacketConstant.KEY_MESSAGE_DIRECT);
        final int chatType = jsonObject.getInteger(RedPacketConstant.KEY_CHAT_TYPE);
        String specialAvatarUrl =jsonObject.getString(RedPacketConstant.KEY_SPECIAL_AVATAR_URL);
        String specialNickname = jsonObject.getString(RedPacketConstant.KEY_SPECIAL_NICK_NAME);
        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.moneyID = moneyId;
        redPacketInfo.toAvatarUrl = toAvatarUrl;
        redPacketInfo.toNickName = toNickname;
        redPacketInfo.moneyMsgDirect = messageDirect;
        redPacketInfo.chatType = chatType;
        String packetType = jsonObject.getString(RedPacketConstant.MESSAGE_ATTR_RED_PACKET_TYPE);
        if (!TextUtils.isEmpty(packetType)&&packetType.equals(RedPacketConstant.GROUP_RED_PACKET_TYPE_EXCLUSIVE)) {
            redPacketInfo.specialAvatarUrl = specialAvatarUrl;
            redPacketInfo.specialNickname = specialNickname;
        }
        String currentUserId=jsonObject.getString(RedPacketConstant.KEY_CURRENT_ID);
        redPacketInfo.imUserId = currentUserId;
        redPacketInfo.toUserId = currentUserId;

        AuthData authData=AuthDataUtils.getInstance().getAuthData(currentUserId);
        RPOpenPacketUtil.getInstance().openRedPacket(redPacketInfo,authData, activity, new RPOpenPacketUtil.RPOpenPacketCallBack() {
            @Override
            public void onSuccess(String senderId, String senderNickname) {
                openRedPacketSuccess.onSuccess(senderId, senderNickname);
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

    /**
     *
     * 拆红包回调接口
     */
    public interface OpenRedPacketSuccess {

        void onSuccess(String senderId, String senderNickname);
    }


   public static  void  startChangeActivity(FragmentActivity fragmentActivity,String fromNickname ,String fromAvatarUrl,String userId){

        Intent intent = new Intent(fragmentActivity, RPChangeActivity.class);
        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromNickName = fromNickname;
        redPacketInfo.fromAvatarUrl = fromAvatarUrl;
        intent.putExtra(RPConstant.EXTRA_MONEY_INFO, redPacketInfo);
        intent.putExtra(RPConstant.EXTRA_AUTH_INFO, AuthDataUtils.getInstance().getAuthData(userId));
       fragmentActivity.startActivity(intent);
    }

}
