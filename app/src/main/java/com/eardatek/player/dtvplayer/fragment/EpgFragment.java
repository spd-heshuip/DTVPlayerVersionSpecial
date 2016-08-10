package com.eardatek.player.dtvplayer.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.actitivy.EardatekVersion2Activity;
import com.eardatek.player.dtvplayer.adapter.EpgListAdapter;
import com.eardatek.player.dtvplayer.bean.EpgItem;
import com.eardatek.player.dtvplayer.callback.MyEvents;
import com.eardatek.player.dtvplayer.layoutmanager.DividerItemDecoration;
import com.eardatek.player.dtvplayer.util.EpgUtil;
import com.eardatek.player.dtvplayer.util.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 16-3-30.
 */
public class EpgFragment extends StatedFragment implements View.OnClickListener{

    private static final String TAG = EpgFragment.class.getSimpleName();

    private static final int EMPTY_EPG = 0;
    private static final int EPG_VALIABLE = 1;


    private View mRootView;
    private RecyclerView mEpgRecycleView;
    private TextView mProgramNameText;
    private LinearLayout mTipsLayout;

    private String mServiceName;
    private int mServiceId;

    private EpgHandler mHandler;
    private List<EpgItem> mListEpg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_epg,container,false);

        initView();

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new EpgHandler(EpgFragment.this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        EardatekVersion2Activity mActivity = (EardatekVersion2Activity) getActivity();
        if (mActivity != null){
            mServiceName = mActivity.getServiceName();
            mServiceId = mActivity.getServiceID();
        }
        if (mServiceName != null)
            initEpgInfo(mServiceName,mServiceId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView");
        EventBus.getDefault().unregister(this);
    }

    private void initView(){
        mEpgRecycleView = (RecyclerView) mRootView.findViewById(R.id.epg_adapter);
        mProgramNameText = (TextView) mRootView.findViewById(R.id.epg_guide_text);
        ImageView mRefresh = (ImageView) mRootView.findViewById(R.id.epg_refresh);
        mTipsLayout = (LinearLayout) mRootView.findViewById(R.id.epg_tips_layout);
        mRefresh.setClickable(true);

        mEpgRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEpgRecycleView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mEpgRecycleView.setItemAnimator(new DefaultItemAnimator());

        mRefresh.setOnClickListener(this);
    }

    private void initEpgInfo(String serviceName,int serviceId){
        mProgramNameText.setText(String.format(Locale.ENGLISH,"%s" + " Program Guide",serviceName));
        Thread epgThread = new Thread(new EpgRunable(EpgFragment.this,serviceName,serviceId));
        epgThread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.epg_refresh:
                if (mServiceName != null){
                    LogUtil.i(TAG,"get epg");
                    LogUtil.i(TAG,mServiceName);
                    LogUtil.i(TAG,mServiceId + "");
                    initEpgInfo(mServiceName,mServiceId);
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEventBus(MyEvents events) {
        if (events.getEventType() == MyEvents.UPDATE_EPG_FRAGMENT){
            mServiceName = ((Bundle)events.getData()).getString("serviceName");
            mServiceId = ((Bundle)events.getData()).getInt("serviceId");
            if (mServiceName != null)
                initEpgInfo(mServiceName,mServiceId);
        }
    }

    private static class EpgHandler extends Handler{

        private WeakReference<EpgFragment> mFragment;

        public EpgHandler(EpgFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            EpgFragment fragment = mFragment.get();
            if (fragment == null)
                return;
            switch (msg.what){
                case EMPTY_EPG:
                    fragment.mTipsLayout.setVisibility(View.VISIBLE);
                    break;
                case EPG_VALIABLE:
                    fragment.mTipsLayout.setVisibility(View.GONE);
                    EpgListAdapter mAdapter = new EpgListAdapter(fragment.getActivity(), fragment.mListEpg);
                    fragment.mEpgRecycleView.setAdapter(mAdapter);
                    break;
            }
        }
    }

    private static class EpgRunable implements Runnable{
        private WeakReference<EpgFragment> mFragment;
        private String mServiceName;
        private int mServiceId;

        public EpgRunable(EpgFragment fragment,String serviceName,int serviceId) {
            mFragment = new WeakReference<EpgFragment>(fragment);
            mServiceId = serviceId;
            mServiceName = serviceName;
        }

        @Override
        public void run() {
            EpgFragment fragment = mFragment.get();
            if (fragment == null)
                return;
            fragment.mListEpg = EpgUtil.loadEpg(String.format(Locale.ENGLISH, "%s [Program %d]", mServiceName, mServiceId));
            if (fragment.mListEpg == null || fragment.mListEpg.size() == 0){
                fragment.mHandler.sendEmptyMessage(EMPTY_EPG);
            }else
                fragment.mHandler.sendEmptyMessage(EPG_VALIABLE);

        }
    }

}
