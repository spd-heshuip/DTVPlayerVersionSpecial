package com.eardatek.special.player.data;

import android.support.annotation.NonNull;

import com.eardatek.special.player.bean.ChannelInfo;
import com.eardatek.special.player.impl.ChannelInfoDaoImpl;
import com.eardatek.special.player.system.DTVApplication;
import com.eardatek.special.player.util.ListUtil;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.io.Serializable;
import java.sql.SQLException;
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
    private int mLastRemovedPosition = -1;

    public TvDataProvider() {
        loadData();
    }

    private void loadData(){
        mChannelList = new ArrayList<>();
        final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

        mChannelList = ListUtil.getListFromFile("Channel.txt");

        ChannelInfoDaoImpl channelInfoDao = new ChannelInfoDaoImpl(DTVApplication.getAppContext());
        List<ChannelInfo> infos ;
        try {
            infos = channelInfoDao.getAllChannelInfo();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (infos.size() == 0){
            mChannelList.clear();
            ListUtil.clearFile("Channel.txt");
        }

        if (mChannelList.size() == 0 && infos.size() > 0){
            for (int i = 0;i < infos.size();i++){
                String text = infos.get(i).getLocation();
                String title = infos.get(i).getTitle();
                boolean isEncrypt = infos.get(i).isProgramEncrypt();
                String videoType = infos.get(i).getVideoType();
                mChannelList.add(new ConcreteData(i,0,text,swipeReaction,title,isEncrypt,videoType));
            }
        }
        try {
            channelInfoDao.closeRealm();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return mChannelList.size();
    }

    @Override
    public void updateItemName(String location,String nameNew,int postion) {

        ConcreteData data = mChannelList.get(postion);

        data.setTitle(nameNew);

        mChannelList.remove(postion);

        mChannelList.add(postion,data);

        ChannelInfoDaoImpl channelInfoDao = new ChannelInfoDaoImpl(DTVApplication.getAppContext());
        try {
            channelInfoDao.updateChannelName(location,nameNew);
            channelInfoDao.closeRealm();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
//        LogUtil.i("SwipeableWithButtonAdapter",name+item.getText());
//        mDB.deleteChanelInfo(name, item.getText());

        mLastRemovedData = mChannelList.remove(position);
        mLastRemovedPosition = position;
    }

    public void removeItemFromDataBase(int position){
        //noinspection UnnecessaryLocalVariable
        final AbstractDataProvider.Data item = mChannelList.get(position);
        ChannelInfoDaoImpl channelInfoDao = new ChannelInfoDaoImpl(DTVApplication.getAppContext());
        try {
            channelInfoDao.deleteChannel(item.getText());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mLastRemovedData = mChannelList.remove(position);
        mLastRemovedPosition = position;
        try {
            channelInfoDao.closeRealm();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        private String mText;
        private final int mViewType;
        private boolean mPinned;
        private int mTop = 0;
        private long mTime;
        private String mTitle;
        private boolean mIsEncrypt;
        private String mVideoType;

        public ConcreteData(long id, int viewType, String name, int swipeReaction,
                            String title,boolean isEncrypt,String videoType) {
            mId = id;
            mViewType = viewType;
            mText = name;
            mTitle = title;
            mIsEncrypt = isEncrypt;
            mVideoType = videoType;
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
        public String getTitle() {
            return mTitle;
        }

        @Override
        public boolean isEncrypt() {
            return mIsEncrypt;
        }

        @Override
        public String getVideoType() {
            return mVideoType;
        }

        @Override
        public void setTitle(String title) {
            mTitle = title;
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
