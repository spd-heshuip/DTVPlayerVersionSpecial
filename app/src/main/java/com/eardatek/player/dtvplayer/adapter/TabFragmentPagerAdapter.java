package com.eardatek.player.dtvplayer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;

import com.eardatek.player.dtvplayer.bean.Tab;
import com.eardatek.player.dtvplayer.fragment.EpgFragment;
import com.eardatek.player.dtvplayer.fragment.ProgramFragment;
import com.eardatek.player.dtvplayer.fragment.RadioFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 16-3-30.
 */
public class TabFragmentPagerAdapter extends FragmentPagerAdapter{
    private List<Tab> mTabs;
    private List<Fragment> mFragments = new ArrayList<>(3);
    private FragmentManager manager;
    private boolean[] fragmentUpdateFlag = {false,false,false,false};
    private List<String> mTagList;

    public TabFragmentPagerAdapter(FragmentManager fm,List<Tab> tabs) {
        super(fm);
        this.mTabs = tabs;
        manager = fm;
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
        mFragments.add(fragment);
        return fragment;
    }

    public void setFragmentUpdateFlagPosition(int positon, boolean fragmentUpdateFlag) {
        this.fragmentUpdateFlag[positon] = fragmentUpdateFlag;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container,position);
        String fragmentTag = fragment.getTag();
        if (fragmentUpdateFlag[position % fragmentUpdateFlag.length]){
            FragmentTransaction fragmentTransaction = manager.beginTransaction();
            fragmentTransaction.remove(fragment);
            try {
                fragment = (Fragment) mTabs.get(position).getFragment().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            fragmentTransaction.add(container.getId(),fragment,fragmentTag);
            fragmentTransaction.attach(fragment);
            fragmentTransaction.commit();

            fragmentUpdateFlag[position % fragmentUpdateFlag.length] = false;
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
