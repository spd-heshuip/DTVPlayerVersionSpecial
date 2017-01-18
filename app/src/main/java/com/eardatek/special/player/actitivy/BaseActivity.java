package com.eardatek.special.player.actitivy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.eardatek.special.player.util.LanguageUtil;
import com.umeng.message.PushAgent;

import rx.Subscription;

/**
 * Created by Administrator on 16-3-31.
 */
public class BaseActivity extends AppCompatActivity {

    protected Subscription subscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PushAgent.getInstance(this).onAppStart();
        swtichLanguage();
    }

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

    protected void swtichLanguage(){
        LanguageUtil.swtichLanguage();
    }
}
