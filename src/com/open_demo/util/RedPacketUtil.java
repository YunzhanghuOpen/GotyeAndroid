package com.open_demo.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.easemob.redpacketsdk.bean.AuthData;
import com.easemob.redpacketsdk.bean.RPUserBean;
import com.easemob.redpacketsdk.bean.RedPacketInfo;
import com.easemob.redpacketsdk.constant.RPConstant;
import com.easemob.redpacketui.callback.GroupMemberCallback;
import com.easemob.redpacketui.callback.NotifyGroupMemberCallback;
import com.easemob.redpacketui.ui.activity.RPChangeActivity;
import com.easemob.redpacketui.ui.activity.RPRedPacketActivity;
import com.easemob.redpacketui.utils.RPGroupMemberUtil;
import com.easemob.redpacketui.utils.RPOpenPacketUtil;
import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeMessageType;
import com.gotye.api.GotyeUser;
import com.open_demo.activity.ChatPage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import utils.AuthDataUtils;
import utils.RedPacketConstant;

/**
 * Created by ustc on 2016/5/31.
 */
public class RedPacketUtil {
    /**
     *  打开发送红包界面
     * @param activity
     * @param chatType //单聊或者群聊
     * @param toId  //单聊接受红包者ID,群聊ID
     * @param mCurrentUser //当前用户
     * @param requestCode //请求code
     * @param mAllMembers //所有的群成员
     */
    public static void startRedPacketActivityForResult(Activity activity, int chatType, String toId, final GotyeUser mCurrentUser, int requestCode, final List<GotyeUser> mAllMembers) {
        RedPacketInfo mRedPacketInfo = new RedPacketInfo();
        //传递参数到红包sdk：发送者头像url，昵称（缺失则传id）
        String fromAvatarUrl = mCurrentUser.getIcon().getPath();
        String fromNickName = mCurrentUser.getNickname();

        mRedPacketInfo.fromAvatarUrl = TextUtils.isEmpty(fromAvatarUrl) ? "none" : fromAvatarUrl;
        mRedPacketInfo.fromNickName = TextUtils.isEmpty(fromNickName) ? mCurrentUser.getName() : fromNickName;

        if (chatType == 0) {//单聊
            mRedPacketInfo.chatType = RPConstant.CHATTYPE_SINGLE;
            mRedPacketInfo.toUserId = toId;//接收人id(这里没有id传name)
        } else {//群聊
            //如果是群聊传递群id和群人数
            mRedPacketInfo.chatType = RPConstant.CHATTYPE_GROUP;//群聊
            mRedPacketInfo.toGroupId = toId;//群ID
            mRedPacketInfo.groupMemberCount = mAllMembers.size();//群成员人数
            //实现群成员接口
            RPGroupMemberUtil.getInstance().setGroupMemberListener(new NotifyGroupMemberCallback() {
                @Override
                public void getGroupMember(final String groupID, final GroupMemberCallback mCallBack) {

                    List<RPUserBean> userBeanList = new ArrayList<RPUserBean>();
                    if (mAllMembers != null && mAllMembers.size() != 0) {
                        for (int i = 0; i < mAllMembers.size(); i++) {
                            RPUserBean rpUserBean = new RPUserBean();
                            GotyeUser gotyeUser = mAllMembers.get(i);
                            if (gotyeUser.getName().equals(mCurrentUser.getName())) {
                                continue;
                            }
                            rpUserBean.userId = gotyeUser.getName();
                            if (gotyeUser != null) {
                                rpUserBean.userAvatar = TextUtils.isEmpty(gotyeUser.getIcon().getPath()) ? "none" : gotyeUser.getIcon().getPath();
                                rpUserBean.userNickname = TextUtils.isEmpty(gotyeUser.getNickname()) ? gotyeUser.getName() : gotyeUser.getNickname();
                            } else {
                                rpUserBean.userNickname = rpUserBean.userId;
                                rpUserBean.userAvatar = "none";
                            }
                            userBeanList.add(rpUserBean);
                        }
                        mCallBack.setGroupMember(userBeanList);
                    } else {
                        mCallBack.setGroupMember(null);
                    }

                }
            });
        }

        Intent intent = new Intent(activity, RPRedPacketActivity.class);
        intent.putExtra(RPConstant.EXTRA_MONEY_INFO, mRedPacketInfo);
        intent.putExtra(RPConstant.EXTRA_AUTH_INFO, AuthDataUtils.getInstance().getAuthData(mCurrentUser.getName()));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     *  打开红包
     * @param mChatPage
     * @param jsonRedPacket
     * @param mDirect
     * @param api
     */
    public static void openRedPacket(final ChatPage mChatPage, JSONObject jsonRedPacket, String mDirect, GotyeAPI api) {

        try {
            final ProgressDialog progressDialog = new ProgressDialog(mChatPage);
            progressDialog.setCanceledOnTouchOutside(false);
            RedPacketInfo mRedPacketInfo = new RedPacketInfo();
            GotyeUser mCurrentUser = mChatPage.currentLoginUser;
            //拆红包者的昵称和头像
            String toAvatarUrl = mCurrentUser.getIcon().getPath();
            String toNickName = mCurrentUser.getNickname();
            mRedPacketInfo.toAvatarUrl = TextUtils.isEmpty(toAvatarUrl) ? "none" : toAvatarUrl;
            mRedPacketInfo.toNickName = TextUtils.isEmpty(toNickName) ? mCurrentUser.getName() : toNickName;
            mRedPacketInfo.moneyMsgDirect = mDirect;//红包方向
            String moneyID = jsonRedPacket.getString(RPConstant.EXTRA_RED_PACKET_ID);
            mRedPacketInfo.moneyID = moneyID;//红包ID
            if (mChatPage.chatType == 0) {//单聊
                mRedPacketInfo.chatType = RPConstant.CHATTYPE_SINGLE;
            } else if (mChatPage.chatType == 2) {//群聊
                mRedPacketInfo.chatType = RPConstant.CHATTYPE_GROUP;
            }
            String packetType = jsonRedPacket.getString(RedPacketConstant.MESSAGE_ATTR_RED_PACKET_TYPE);
            String specialReceiveId = jsonRedPacket.getString(RedPacketConstant.MESSAGE_ATTR_SPECIAL_RECEIVER_ID);
            if (!TextUtils.isEmpty(packetType) && packetType.equals(RedPacketConstant.GROUP_RED_PACKET_TYPE_EXCLUSIVE)) {
                GotyeUser userTemp = new GotyeUser();
                userTemp.setName(specialReceiveId);
                GotyeUser specialUser = api.getUserDetail(userTemp, false);
                String specialAvatarUrl = "none";
                String specialNickname = specialReceiveId;
                if (specialUser != null) {
                    specialAvatarUrl = TextUtils.isEmpty(specialUser.getIcon().getPath()) ? "none" : specialUser.getIcon().getPath();
                    specialNickname = TextUtils.isEmpty(specialUser.getNickname()) ? specialUser.getName() : specialUser.getNickname();
                }
                mRedPacketInfo.specialAvatarUrl = specialAvatarUrl;
                mRedPacketInfo.specialNickname = specialNickname;
                mRedPacketInfo.toUserId = mChatPage.currentLoginUser.getName();
            }

            AuthData authData = AuthDataUtils.getInstance().getAuthData(mChatPage.currentLoginUser.getName());
            RPOpenPacketUtil.getInstance().openRedPacket(mRedPacketInfo, authData, mChatPage, new RPOpenPacketUtil.RPOpenPacketCallBack() {
                @Override
                public void onSuccess(String senderId, String senderNickname) {
                    mChatPage.sendRedPacketAckMessage(senderId, senderNickname);
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
                    Toast.makeText(mChatPage, message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 拆红包回调接口
     */
    public interface OpenRedPacketSuccess {

        void onSuccess(String senderId, String senderNickname);
    }

    /**
     * 打开零钱页
     * @param fragmentActivity
     * @param fromNickname
     * @param fromAvatarUrl
     * @param userId
     */
    public static void startChangeActivity(FragmentActivity fragmentActivity, String fromNickname, String fromAvatarUrl, String userId) {

        Intent intent = new Intent(fragmentActivity, RPChangeActivity.class);
        RedPacketInfo redPacketInfo = new RedPacketInfo();
        redPacketInfo.fromNickName = fromNickname;
        redPacketInfo.fromAvatarUrl = fromAvatarUrl;
        intent.putExtra(RPConstant.EXTRA_MONEY_INFO, redPacketInfo);
        intent.putExtra(RPConstant.EXTRA_AUTH_INFO, AuthDataUtils.getInstance().getAuthData(userId));
        fragmentActivity.startActivity(intent);
    }

    /**
     *  是否是红包消息
     * @param message
     * @return
     */
    public static JSONObject isRedPacketMsg(GotyeMessage message) {
        //如果没有扩展字段直接返回null
        if (message.getExtraData() == null) {
            return null;
        }
        if (message.getType() == GotyeMessageType.GotyeMessageTypeText) {
            String extraData = new String(message.getExtraData());
            if (!TextUtils.isEmpty(extraData)) {
                try {
                    JSONObject rpJsonObject = new JSONObject(extraData);
                    if (rpJsonObject.has(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE)
                            && rpJsonObject.getBoolean(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE)) {

                        return rpJsonObject;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * 是否是回执消息
     * @param message
     * @return
     */
    public static JSONObject isRedPacketAckMsg(GotyeMessage message) {
        //如果没有扩展字段直接返回null
        if (message.getExtraData() == null) {
            return null;
        }
        if (message.getType() == GotyeMessageType.GotyeMessageTypeText) {
            String extraData = new String(message.getExtraData());
            if (!TextUtils.isEmpty(extraData)) {
                try {
                    JSONObject ackJsonObject = new JSONObject(extraData);
                    if (ackJsonObject.has(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE)
                            && ackJsonObject.getBoolean(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE)) {

                        return ackJsonObject;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 是否是发送红包者的回执消息
     * @param message
     * @return
     */
    public static boolean isMyAckMessage(GotyeMessage message) {
        JSONObject jsonObject = isRedPacketAckMsg(message);
        if (jsonObject != null) {
            try {
                String receiveUserId = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_RECEIVER_ID);//红包接受者id
                String sendUserId = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_SENDER_ID);//红包发送者id
                String currentUserId = AuthDataUtils.getInstance().getLoginUserId();
                if (TextUtils.isEmpty(currentUserId)) {
                    return false;
                }
                //发送者和领取者都不是自己-
                if (!currentUserId.equals(receiveUserId) && !currentUserId.equals(sendUserId)) {
                    return false;
                } else {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
        return false;
    }


}
