package com.open_demo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import utils.TokenUtils;


public class RequestTask extends AsyncTask<String, String, String> {
    private final String TAG = "RedPacket";
    private String userID;
    private Context context;

    public RequestTask(Context context, String userID  ) {
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
                JSONObject jsonObj =   JSONObject.parseObject(result);
                TokenUtils.getInstance().setAuthData(jsonObj,userID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
