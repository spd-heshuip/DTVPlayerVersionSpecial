package com.eardatek.special.player.bean;

/**
 * Created by Administrator on 16-3-30.
 */
public class Tab {

    private int title;
    private Class fragment;

    public Tab(int title, Class fragment) {
        this.title = title;
        this.fragment = fragment;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public Class getFragment() {
        return fragment;
    }

    public void setFragment(Class fragment) {
        this.fragment = fragment;
    }
}
