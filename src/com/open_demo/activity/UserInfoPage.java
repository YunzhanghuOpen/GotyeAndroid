package com.open_demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeDelegate;
import com.gotye.api.GotyeMedia;
import com.gotye.api.GotyeRoom;
import com.gotye.api.GotyeUser;
import com.open_demo.R;
import com.open_demo.main.MainActivity;
import com.open_demo.util.BitmapUtil;
import com.open_demo.util.ImageCache;
import com.open_demo.util.ProgressDialogUtil;
import com.open_demo.util.ToastUtil;

public class UserInfoPage extends Activity implements OnClickListener {
    private GotyeUser user;
    private ImageView userIconView;
    private int from;
    private GotyeRoom room;
    TextView userInfoView;
    public GotyeAPI api = GotyeAPI.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_user_info);
        user = (GotyeUser) getIntent().getSerializableExtra("user");
        api.addListener(mdelegate);
        from = getIntent().getIntExtra("from", -1);
        room = (GotyeRoom) getIntent().getSerializableExtra("room");
        GotyeUser tempUser = api.getUserDetail(user, true);
        if (tempUser != null) {
            user = tempUser;
        }
        initView();
        setValue();
    }

    private void initView() {

        findViewById(R.id.back).setOnClickListener(this);
        userInfoView = (TextView) findViewById(R.id.user_info);
        if (user.getName().equals(api.getLoginUser().getName())) {
            findViewById(R.id.add_friend).setEnabled(false);
            findViewById(R.id.del_frieng).setEnabled(false);
            findViewById(R.id.to_bleak).setEnabled(false);
            findViewById(R.id.remove_from_black).setEnabled(false);
        } else {
            if (user.isFriend()) {
                findViewById(R.id.add_friend).setEnabled(false);
                findViewById(R.id.del_frieng).setEnabled(true);
                findViewById(R.id.del_frieng).setOnClickListener(this);
            } else {
                findViewById(R.id.add_friend).setEnabled(true);
                findViewById(R.id.add_friend).setOnClickListener(this);
                findViewById(R.id.del_frieng).setEnabled(false);
            }

            if (user.isBlocked()) {
                findViewById(R.id.to_bleak).setEnabled(false);
                findViewById(R.id.remove_from_black).setEnabled(true);
                findViewById(R.id.remove_from_black).setOnClickListener(this);
            } else {
                findViewById(R.id.remove_from_black).setEnabled(false);
                findViewById(R.id.to_bleak).setEnabled(true);
                findViewById(R.id.to_bleak).setOnClickListener(this);
            }
        }


    }

    private void setValue() {
        ((TextView) findViewById(R.id.name)).setText(user.getName());
        ((TextView) findViewById(R.id.id)).setText("昵称:" + user.getNickname());
        userInfoView.setText("用户信息" + user.getInfo());
        userIconView = (ImageView) findViewById(R.id.user_icon);
        Bitmap bmp = ImageCache.getInstance().get(user.getName());
        if (bmp != null) {
            userIconView.setImageBitmap(bmp);
        } else {
            if (user.getIcon() != null) {
                Bitmap bm = BitmapUtil.getBitmap(user.getIcon().getPath());
                if (bm != null) {
                    userIconView.setImageBitmap(bm);
                    ImageCache.getInstance().put(user.getName(), bm);
                } else {
                    api.downloadMedia(user.getIcon());
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
//		api.removeListener(this);
        api.removeListener(mdelegate);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.add_friend:
                GotyeUser userLogin = GotyeAPI.getInstance().getLoginUser();
                if (user.getName().equals(userLogin.getName())) {
                    Toast.makeText(this, "不能添加自己", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    ProgressDialogUtil.showProgress(this, "正在加为好友...");
                    api.reqAddFriend(user);
                }
                break;
            case R.id.del_frieng:
                ProgressDialogUtil.showProgress(this, "正在删除好友");
                api.reqRemoveFriend(user);
                break;
            case R.id.to_bleak:
                ProgressDialogUtil.showProgress(this, "正在把" + user.getName()
                        + "加入到黑名单");
                api.reqAddBlocked(user);
                break;
            case R.id.remove_from_black:
                ProgressDialogUtil.showProgress(this, "正在把" + user.getName()
                        + "移除黑名单");
                api.reqRemoveBlocked(user);
                break;
            default:
                break;
        }
    }

    private EditText report;

    public void hideKeyboard() {
        // 隐藏输入法
        InputMethodManager imm = (InputMethodManager) getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        // 显示或者隐藏输入法
        imm.hideSoftInputFromWindow(report.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    private GotyeDelegate mdelegate = new GotyeDelegate() {

        @Override
        public void onGetUserDetail(int code, GotyeUser user) {
            // TODO Auto-generated method stub
            if (code == 0) {
                if (user.getName().equals(UserInfoPage.this.user.getName())) {
                    UserInfoPage.this.user = user;
                    ImageCache.getInstance().removeKey(user.getName());
                    setValue();
                }

            }
        }

        @Override
        public void onAddFriend(int code, GotyeUser user) {

            ProgressDialogUtil.dismiss();
            if (code == 0) {
                UserInfoPage.this.user = user;
                ToastUtil.show(UserInfoPage.this, "添加好友成功!");
                initView();
            } else {
                ToastUtil.show(UserInfoPage.this, "添加好友失败!");
            }
        }

        @Override
        public void onAddBlocked(int code, GotyeUser user) {
            if (code == 0) {
                UserInfoPage.this.user = user;
                ToastUtil.show(getBaseContext(), "成功把" + user.getName() + "加入黑名单");
                ProgressDialogUtil.dismiss();
                initView();
            } else {
                ToastUtil.show(getBaseContext(), "抱歉，没能把" + user.getName()
                        + "加入黑名单");
                ProgressDialogUtil.dismiss();
            }
        }

        @Override
        public void onRemoveFriend(int code, GotyeUser user) {

            if (from == 100) {
                ProgressDialogUtil.dismiss();
                Intent i = new Intent(UserInfoPage.this, RoomInfoPage.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("room", room);
                startActivity(i);
                finish();
                ToastUtil.show(UserInfoPage.this, "成功删除好友：" + user.getName());
            } else if (from == 1) {
                finish();
            } else {
                ProgressDialogUtil.dismiss();
                Intent i = new Intent(UserInfoPage.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("tab", 1);
                startActivity(i);
                ToastUtil.show(UserInfoPage.this, "成功删除好友：" + user.getName());
            }

        }

        @Override
        public void onRemoveBlocked(int code, GotyeUser user) {
            // TODO Auto-generated method stub
            ProgressDialogUtil.dismiss();
            if (code == 0) {
                UserInfoPage.this.user = user;
                ToastUtil.show(getBaseContext(), "成功把" + user.getName() + "移除黑名单");
                initView();
            } else {
                ToastUtil.show(getBaseContext(), "抱歉，没能把" + user.getName()
                        + "移除黑名单");
            }
        }

        @Override
        public void onDownloadMedia(int code, GotyeMedia media) {
            if (!TextUtils.isEmpty(media.getUrl())) {
                if (user.getIcon() != null && media.getUrl().equals(user.getIcon().getUrl())) {
                    Bitmap bm = BitmapUtil.getBitmap(media.getPath());
                    if (bm != null) {
                        userIconView.setImageBitmap(bm);
                        ImageCache.getInstance().put(user.getName(), bm);
                    }

                }
            }
        }
    };
}
