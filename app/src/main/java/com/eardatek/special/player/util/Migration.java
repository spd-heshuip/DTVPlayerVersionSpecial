package com.eardatek.special.player.util;


import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by Luke He on 16-10-11 上午11:52.
 * Email:spd_heshuip@163.com
 * Company:Eardatek
 */

public class Migration implements RealmMigration{
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema realmSchema = realm.getSchema();
        if (oldVersion == 0){
            realmSchema.create("ChannelInfo")
                    .addField("mLocation",String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("mTitle",String.class,FieldAttribute.REQUIRED)
                    .addField("isEncrypt",boolean.class);
            oldVersion++;
        }

        if (oldVersion == 1){
            realmSchema.get("ChannelInfo")
                    .addField("videoType",String.class);
            oldVersion++;
        }
    }
}
