package com.eardatek.special.player.actitivy;


import android.os.Bundle;
import android.support.annotation.Nullable;

import com.anthony.ultimateswipetool.activity.AbsSwipeBackActivity;
import com.eardatek.special.player.util.LanguageUtil;
import com.umeng.message.PushAgent;

import rx.Subscription;

/**
 * Created by Luke He on 16-9-23 下午2:08.
 * Email:spd_heshuip@163.com
 * Company:Eardatek
 */

public class SwipeBackBaseActicity extends AbsSwipeBackActivity{

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
