package com.eardatek.player.dtvplayer.actitivy;

import android.support.v7.app.AppCompatActivity;

import rx.Subscription;

/**
 * Created by Administrator on 16-3-31.
 */
public class BaseActivity extends AppCompatActivity{

    protected Subscription subscription;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }

    protected void unsubscribe(){
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
    }
}
