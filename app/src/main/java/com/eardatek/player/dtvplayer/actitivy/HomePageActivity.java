package com.eardatek.player.dtvplayer.actitivy;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.util.NetworkUtil;
import com.eardatek.player.dtvplayer.widget.ProgressWheel;

/**
 * Created by Administrator on 16-7-31.
 */
public class HomePageActivity extends AppCompatActivity{

    private static final String TAG = HomePageActivity.class.getSimpleName();
    private ProgressWheel mProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        init();
    }

    private void init(){
        Button next = (Button) findViewById(R.id.next);
        mProgress = (ProgressWheel) findViewById(R.id.progres);

        mProgress.spin();
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String wifiName = NetworkUtil.getWifiName(getApplicationContext());
                if (wifiName != null && wifiName.contains("MobileTV_")){
                    Intent intent = new Intent(HomePageActivity.this, EardatekVersion2Activity.class);
                    startActivity(intent);
                    finish();
                }else {
                    showDialog();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String wifiName = NetworkUtil.getWifiName(getApplicationContext());
        if (wifiName != null && wifiName.contains("MobileTV_")){
            mProgress.stopSpinning();
            Intent intent = new Intent(HomePageActivity.this, EardatekVersion2Activity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showDialog() {
        View view = getLayoutInflater().inflate(R.layout.wifi_dialog,
                null);
        final Dialog dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        Button connectWifi = (Button) view.findViewById(R.id.yes);
        Button no = (Button) view.findViewById(R.id.no);

        connectWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing())
                    dialog.dismiss();

                NetworkUtil.openSetting(HomePageActivity.this);

            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing())
                    dialog.dismiss();
            }
        });

        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        //设置窗口显示位置
        window.setGravity(Gravity.CENTER);
        // 设置窗口动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //设置窗口显示位置偏移量
        wl.x = 0;
        wl.y = 0;
        // 使窗口宽度铺满手机屏幕
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        // 窗口属性改变时，显示窗口
        dialog.onWindowAttributesChanged(wl);
        // 点击外围dimiss dialog
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

}
