package com.open_demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.easemob.redpacketsdk.RPCallback;
import com.easemob.redpacketsdk.RedPacket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class RequestTask extends AsyncTask<String, String, String> {
    private final String TAG = "RedPakcet";
    private String userID;
    private Context context;
    private final int HANDLER_LOGIN_SUCCESS = 1;
    private final int HANDLER_LOGIN_FAILURE = 0;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_LOGIN_SUCCESS:

                    break;
                case HANDLER_LOGIN_FAILURE:

                    break;
            }
        }


    };

    public RequestTask(Context context, String userID) {
        this.context = context;
        this.userID = userID;
    }

    @Override
    protected String doInBackground(String... uri) {
        String mockUrl = "http://rpv2.yunzhanghu.com/api/sign?duid=" + userID;
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(mockUrl);
        HttpResponse response;
        try {
            response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                Log.d("Response of GET request", response.toString());
                String responseBody = EntityUtils.toString(response.getEntity());
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    entity.consumeContent();
                }
                client.getConnectionManager().shutdown();
                return responseBody;
            }

            return null;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;

    }

    @Override
    protected void onPostExecute(String result) {
        try {
            if (result != null) {
                JSONObject jsonObj = new JSONObject(result);
                String partner = jsonObj.getString("partner");
                String userId = jsonObj.getString("user_id");
                String timestamp = jsonObj.getString("timestamp");
                String sign = jsonObj.getString("sign");
                RedPacket.getInstance().initRPAuthToken(partner, userId, timestamp, sign,
                        new RPCallback() {
                            @Override
                            public void onSuccess() {

                                mHandler.obtainMessage(HANDLER_LOGIN_SUCCESS).sendToTarget();
                                Log.e(TAG, "init Red Packet success token: " + RedPacket.getInstance().sToken);
                            }

                            @Override
                            public void onError(String code, String message) {
                                //错误处理
                                mHandler.obtainMessage(HANDLER_LOGIN_FAILURE).sendToTarget();
                                Log.e(TAG, "init Red Packet fail token:" + message);
                            }
                        });
            } else {
                mHandler.obtainMessage(HANDLER_LOGIN_FAILURE).sendToTarget();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
