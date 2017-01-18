package com.eardatek.special.player.dao;

import com.eardatek.special.player.bean.ChannelInfo;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Administrator on 16-9-13.
 */
public interface ChannelInfoDao {

    void insert(ChannelInfo info) throws SQLException;

    List<ChannelInfo> getAllChannelInfo() throws SQLException;

    ChannelInfo getChannelInfo(int id) throws SQLException;

    ChannelInfo getChannelInfo(String location) throws SQLException;

    void updateOrInsertChannel(ChannelInfo info) throws SQLException;

    void updateChannelName(String locationOld, String locationNew) throws SQLException;

    void deleteChannel(int id) throws SQLException;

    void deleteChannel(String location) throws SQLException;

    void insertChannelAsync(ChannelInfo info) throws SQLException;

    void deleteAll() throws SQLException;

    void closeRealm() throws SQLException;;
}
