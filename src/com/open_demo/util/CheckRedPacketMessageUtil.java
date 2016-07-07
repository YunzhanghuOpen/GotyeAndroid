package com.open_demo.util;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeMessageType;

import utils.AuthDataUtils;
import utils.RedPacketConstant;

/**
 * Created by ustc on 2016/6/20.
 */
public class CheckRedPacketMessageUtil {


    public static JSONObject isRedPacketMessage(GotyeMessage message){
        JSONObject rpJSON=null;

        if(message.getType()== GotyeMessageType.GotyeMessageTypeText){

            // 设置内容
            String extraData = message.getExtraData() == null ? null : new String(
                    message.getExtraData());
            if(extraData!=null){

                try {
                    JSONObject jsonObject=JSONObject.parseObject(extraData);
                    if(jsonObject!=null&&jsonObject.containsKey(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE)&&jsonObject.getBoolean(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE)){

                        rpJSON=jsonObject;
                    }
                }catch (JSONException e){

                    Log.e("JSONExceptionr",e.toString());
                }
            }
        }


        return rpJSON;
    }

    public static JSONObject isRedPacketAckedMessage(GotyeMessage message){
        JSONObject jsonRedPacketAcked=null;
        if(message.getType()==GotyeMessageType.GotyeMessageTypeText){

            // 设置内容
            String extraData = message.getExtraData() == null ? null : new String(
                    message.getExtraData());
            if(extraData!=null){
                try {
                    JSONObject jsonObject=JSONObject.parseObject(extraData);
                    if(jsonObject!=null&&jsonObject.containsKey(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE)&&jsonObject.getBoolean(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE)){

                        jsonRedPacketAcked=jsonObject;
                    }
                }catch (JSONException e){

                    Log.e("JSONExceptionr",e.toString());
                }
            }
        }
        return jsonRedPacketAcked;
    }

    public  static  boolean   isMyAckMessage(GotyeMessage message) {
        boolean IS_MY_MESSAGE = true;
        JSONObject jsonObject = isRedPacketAckedMessage(message);
        if (jsonObject != null) {
            String recieveUserId = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_RECEIVER_ID);//红包接收者id
            String sendUserId = jsonObject.getString(RedPacketConstant.EXTRA_RED_PACKET_SENDER_ID);//红包发送者id
            String   currentUserId= AuthDataUtils.getInstance().getLoginUserId();
            //发送者和领取者都不是自己-
            if (!TextUtils.isEmpty(currentUserId)&&(!currentUserId.equals(recieveUserId)) && (!currentUserId.equals(sendUserId))) {
                IS_MY_MESSAGE=false;
            }

        }
        return IS_MY_MESSAGE;
    }



}
