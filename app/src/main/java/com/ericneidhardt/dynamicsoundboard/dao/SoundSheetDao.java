package com.ericneidhardt.dynamicsoundboard.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table SOUND_SHEET.
*/
public class SoundSheetDao extends AbstractDao<SoundSheet, Long> {

    public static final String TABLENAME = "SOUND_SHEET";

    /**
     * Properties of entity SoundSheet.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property FragmentTag = new Property(1, String.class, "fragmentTag", false, "FRAGMENT_TAG");
        public final static Property Label = new Property(2, String.class, "label", false, "LABEL");
    };


    public SoundSheetDao(DaoConfig config) {
        super(config);
    }
    
    public SoundSheetDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'SOUND_SHEET' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'FRAGMENT_TAG' TEXT," + // 1: fragmentTag
                "'LABEL' TEXT);"); // 2: label
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'SOUND_SHEET'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, SoundSheet entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String fragmentTag = entity.getFragmentTag();
        if (fragmentTag != null) {
            stmt.bindString(2, fragmentTag);
        }
 
        String label = entity.getLabel();
        if (label != null) {
            stmt.bindString(3, label);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public SoundSheet readEntity(Cursor cursor, int offset) {
        SoundSheet entity = new SoundSheet( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // fragmentTag
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // label
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, SoundSheet entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setFragmentTag(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setLabel(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(SoundSheet entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(SoundSheet entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
