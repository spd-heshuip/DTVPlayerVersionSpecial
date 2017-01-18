package com.eardatek.special.player.impl;

import android.content.Context;

import com.eardatek.special.player.bean.ChannelInfo;
import com.eardatek.special.player.dao.ChannelInfoDao;
import com.eardatek.special.player.util.RealmUtil;

import java.sql.SQLException;
import java.util.List;

import io.realm.Realm;

/**
 * Created by Luke He on 16-9-13 下午1:53.
 * Email:spd_heshuip@163.com
 * Company:Eardatek
 */
public class ChannelInfoDaoImpl implements ChannelInfoDao {

    private Realm mRealm;

    public ChannelInfoDaoImpl(Context context) {
        mRealm = RealmUtil.getInstance(context).getRealm();
    }

    @Override
    public void insert(ChannelInfo info) throws SQLException {
        mRealm.beginTransaction();
        mRealm.copyToRealm(info);
        mRealm.commitTransaction();
//        mRealm.close();
    }

    @Override
    public List<ChannelInfo> getAllChannelInfo() throws SQLException {
        List<ChannelInfo> channelInfos = null;
        channelInfos = mRealm.where(ChannelInfo.class).findAll().sort("isEncrypt");
//        mRealm.close();
        return channelInfos;
    }

    @Override
    public ChannelInfo getChannelInfo(int id) throws SQLException {
        //        mRealm.close();
        return mRealm.where(ChannelInfo.class)
                .equalTo("id",id)
                .findFirst();
    }

    @Override
    public ChannelInfo getChannelInfo(String location) throws SQLException {
        //        mRealm.close();
        return mRealm.where(ChannelInfo.class)
                .equalTo("mLocation",location)
                .findFirst();
    }

    @Override
    public void updateOrInsertChannel(ChannelInfo info) throws SQLException {
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(info);
        mRealm.commitTransaction();
//        mRealm.close();
    }

    @Override
    public void updateChannelName(String locationOld, String nameNew) throws SQLException {
        mRealm.beginTransaction();
        mRealm.where(ChannelInfo.class)
                .equalTo("mLocation",locationOld)
                .findFirst()
                .setmTitle(nameNew);
        mRealm.commitTransaction();
//        mRealm.close();
    }

    @Override
    public void deleteChannel(int id) throws SQLException {
        ChannelInfo info = mRealm.where(ChannelInfo.class).equalTo("id",id).findFirst();
        mRealm.beginTransaction();
        info.deleteFromRealm();
        mRealm.commitTransaction();
//        mRealm.close();
    }

    @Override
    public void deleteChannel(String location) throws SQLException {
        ChannelInfo info = mRealm.where(ChannelInfo.class).equalTo("mLocation",location).findFirst();
        mRealm.beginTransaction();
        info.deleteFromRealm();
        mRealm.commitTransaction();
//        mRealm.close();
    }

    @Override
    public void insertChannelAsync(final ChannelInfo info) throws SQLException {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.beginTransaction();
                realm.copyToRealm(info);
                realm.commitTransaction();
                realm.close();
            }
        });
//        mRealm.close();
    }

    @Override
    public void deleteAll() throws SQLException {
        mRealm.beginTransaction();
        mRealm.where(ChannelInfo.class).findAll().deleteAllFromRealm();
        mRealm.commitTransaction();
//        mRealm.close();
    }

    @Override
    public void closeRealm() throws SQLException {
        mRealm.close();
    }
}
