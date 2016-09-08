package com.eardatek.player.dtvplayer.util;

import android.app.Activity;

import com.eardatek.player.dtvplayer.data.TvDataProvider;
import com.eardatek.player.dtvplayer.system.DTVApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 16-7-28.
 */
public class ListUtil {

    private static final String TAG = ListUtil.class.getSimpleName();

    public static void ListToFile(List<TvDataProvider.ConcreteData> list, String fileName){
        File file = new File(fileName);
        if (!file.exists()){
            file.mkdirs();
        }
        try {
            FileOutputStream outputStream = DTVApplication.getAppContext().
                    openFileOutput(fileName, Activity.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(list);
            LogUtil.i(TAG,"success");
            oos.close();
        } catch (FileNotFoundException e) {
            LogUtil.i(TAG,"FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            LogUtil.i(TAG,"IOException");
            e.printStackTrace();
        }
    }

    public static List<TvDataProvider.ConcreteData> getListFromFile(String fileName){
        List<TvDataProvider.ConcreteData> list = new ArrayList<>();
        try {
            FileInputStream inputStream = DTVApplication.getAppContext().openFileInput(fileName);
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            list = (List<TvDataProvider.ConcreteData>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            LogUtil.i(TAG,"FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            LogUtil.i(TAG,"IOException");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            LogUtil.i(TAG,"ClassNotFoundException");
            e.printStackTrace();
        }

        return list;
    }

    public static void clearFile(String fileName){
        File file = new File(fileName);
        if (!file.exists()){
            file.mkdirs();
        }

        List<TvDataProvider.ConcreteData> list = new ArrayList<>();
        try {
            FileOutputStream outputStream = DTVApplication.getAppContext().
                    openFileOutput(fileName, Activity.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(list);
            LogUtil.i(TAG,"success");
            oos.close();
        } catch (FileNotFoundException e) {
            LogUtil.i(TAG,"FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            LogUtil.i(TAG,"IOException");
            e.printStackTrace();
        }
    }
}
