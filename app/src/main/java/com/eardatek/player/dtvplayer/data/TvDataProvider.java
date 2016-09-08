package com.eardatek.player.dtvplayer.data;

import android.support.annotation.NonNull;

import com.blazevideo.libdtv.ChannelInfo;
import com.eardatek.player.dtvplayer.database.ChannelInfoDB;
import com.eardatek.player.dtvplayer.util.ListUtil;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 16-7-25.
 */
public class TvDataProvider extends AbstractDataProvider{

    private List<ConcreteData> mChannelList;
    private ConcreteData mLastRemovedData;
    private ChannelInfoDB mDB = ChannelInfoDB.getInstance();
    private int mLastRemovedPosition = -1;

    public TvDataProvider() {
        loadData();
    }

    private void loadData(){
        mChannelList = new ArrayList<>();
        final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

        mChannelList = ListUtil.getListFromFile("Channel.txt");
        if (mChannelList.size() == 0){
            for (int i = 0;i < mDB.getAllVideoProgram().size();i++){
                String text = mDB.getAllVideoProgram().get(i).getLocation();
                mChannelList.add(new ConcreteData(i,0,text,swipeReaction));
            }
        }
    }


    private List<ChannelInfo> convertList(List<ConcreteData> list){
        List<ChannelInfo> infoList = new ArrayList<>();
        for (ConcreteData data : list){
            ChannelInfo channelInfo = new ChannelInfo(data.getText(),
                    mDB.getChannelInfo(data.getText()).getTitle().trim());
            infoList.add(channelInfo);
        }
        return infoList;
    }

    public void updateDataBase(){
        List<ChannelInfo> infoList = convertList(mChannelList);
        mDB.emptyDatabase();
        for (ChannelInfo info : infoList){
            mDB.addChannelInfo(info);
        }
    }

    @Override
    public int getCount() {
        return mChannelList.size();
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }
        return mChannelList.get(index);
    }

    public void addItemToTop(ConcreteData item){
        mChannelList.add(0,item);
    }

    @Override
    public void removeItem(int position) {
        //noinspection UnnecessaryLocalVariable
        final AbstractDataProvider.Data item = mChannelList.get(position);
        String name = ChannelInfoDB.getInstance().getChannelInfo(item.getText()).getTitle().trim();
//        LogUtil.i("SwipeableWithButtonAdapter",name+item.getText());
//        mDB.deleteChanelInfo(name, item.getText());

        mLastRemovedData = mChannelList.remove(position);
        mLastRemovedPosition = position;
    }

    public void removeItemFromDataBase(int position){
        //noinspection UnnecessaryLocalVariable
        final AbstractDataProvider.Data item = mChannelList.get(position);
        String name = ChannelInfoDB.getInstance().getChannelInfo(item.getText()).getTitle().trim();
        mDB.deleteChanelInfo(name, item.getText());

        mLastRemovedData = mChannelList.remove(position);
        mLastRemovedPosition = position;
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        final ConcreteData item = mChannelList.remove(fromPosition);

        mChannelList.add(toPosition, item);
        mLastRemovedPosition = -1;
    }

    @Override
    public void swapItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        Collections.swap(mChannelList, toPosition, fromPosition);
        mLastRemovedPosition = -1;
    }

    public void clearItem(){
        mChannelList.clear();
    }

    public void addItem(List<ConcreteData> list){
        for (int i = 0;i < list.size();i++){
            mChannelList.add(list.get(i));
        }
    }

    @Override
    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mChannelList.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = mChannelList.size();
            }

            mChannelList.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    public List<ConcreteData> getList(){
        return mChannelList;
    }

    public static final class ConcreteData extends Data implements Serializable,Comparable<ConcreteData>{

        private final long mId;
        private final String mText;
        private final int mViewType;
        private boolean mPinned;
        private int mTop = 0;
        private long mTime;

        public ConcreteData(long id, int viewType, String name, int swipeReaction) {
            mId = id;
            mViewType = viewType;
            mText = name;
        }

        private static String makeText(long id, String text, int swipeReaction) {
            final StringBuilder sb = new StringBuilder();

            sb.append(id);
            sb.append(" - ");
            sb.append(text);

            return sb.toString();
        }

        @Override
        public boolean isSectionHeader() {
            return false;
        }

        @Override
        public int getViewType() {
            return mViewType;
        }

        @Override
        public long getId() {
            return mId;
        }

        @Override
        public String toString() {
            return mText;
        }

        @Override
        public String getText() {
            return mText;
        }

        @Override
        public boolean isPinned() {
            return mPinned;
        }

        @Override
        public int isTop() {
            return mTop;
        }

        @Override
        public void setTop(int top) {
            mTop = top;
        }

        @Override
        public void setTime(long time) {
            mTime = time;
        }

        @Override
        public long getTime() {
            return mTime;
        }

        @Override
        public void setPinned(boolean pinned) {
            mPinned = pinned;
        }

        @Override
        public int compareTo(@NonNull ConcreteData another ) {

            int result = 0 - (mTop - another.isTop());
            if (result == 0){
                result = 0 - compareToTime(mTime,another.getTime());
            }

            return result;
        }

        public static int compareToTime(long lhs,long rhs){
            Calendar cLhs = Calendar.getInstance();
            Calendar cRhs = Calendar.getInstance();

            cLhs.setTimeInMillis(lhs);
            cRhs.setTimeInMillis(rhs);

            return cLhs.compareTo(cRhs);
        }

    }
}
