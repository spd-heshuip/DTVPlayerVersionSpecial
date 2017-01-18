package com.eardatek.special.player.data;

/**
 * Created by Administrator on 16-7-25.
 */
public abstract class AbstractDataProvider {
    public static abstract class Data {
        public abstract long getId();

        public abstract boolean isSectionHeader();

        public abstract int getViewType();

        public abstract String getText();

        public abstract void setPinned(boolean pinned);

        public abstract boolean isPinned();

        public abstract int isTop();

        public abstract void setTop(int top);

        public abstract void setTime(long time);

        public abstract long getTime();

        public abstract String getTitle();

        public abstract boolean isEncrypt();

        public abstract String getVideoType();

        public abstract void setTitle(String title);

    }

    public abstract int getCount();

    public abstract void updateItemName(String location,String nameNew,int position);

    public abstract Data getItem(int index);

    public abstract void removeItem(int position);

    public abstract void moveItem(int fromPosition, int toPosition);

    public abstract void swapItem(int fromPosition, int toPosition);

    public abstract int undoLastRemoval();
}
