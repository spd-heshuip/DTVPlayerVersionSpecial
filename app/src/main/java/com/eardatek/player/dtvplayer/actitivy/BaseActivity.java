package com.eardatek.player.dtvplayer.actitivy;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import java.util.List;

/**
 * Created by Administrator on 16-3-31.
 */
public class BaseActivity extends AppCompatActivity{

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        List<Fragment> list = fragmentManager.getFragments();
//        for (Fragment fragment : list){
//            fragment.onActivityResult(requestCode,resultCode,data);
//        }
    }

}
