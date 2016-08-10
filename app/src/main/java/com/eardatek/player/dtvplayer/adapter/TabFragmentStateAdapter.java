package com.eardatek.player.dtvplayer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.eardatek.player.dtvplayer.bean.Tab;

import java.util.List;

/**
 * Created by Administrator on 16-4-5.
 */
public class TabFragmentStateAdapter extends FragmentStatePagerAdapter{

    private List<Tab> mTabs;
    private FragmentManager mManager;

    public TabFragmentStateAdapter(FragmentManager fm,List<Tab> tabs) {
        super(fm);
        mTabs = tabs;
        mManager = fm;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) mTabs.get(position).getFragment().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
