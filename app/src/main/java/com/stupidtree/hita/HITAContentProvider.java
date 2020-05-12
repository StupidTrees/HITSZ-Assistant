package com.stupidtree.hita;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HITAContentProvider extends ContentProvider {


    private Context mContext;
    HITADBHelper mDbHelper = null;
    SQLiteDatabase db = null;

    public static final String AUTHORITY = "com.stupidtree.hita.provider";
    // 设置ContentProvider的唯一标识

    public static final int TIMETABLE_CODE = 1;
    public static final int TASK_CODE = 2;
    public static final int CURRICULUM_CODE = 3;
    public static final int SUBJECT_CODE = 4;
    // UriMatcher类使用:在ContentProvider 中注册URI
    private static final UriMatcher mMatcher;
    static{
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 初始化
        mMatcher.addURI(AUTHORITY,"timetable", TIMETABLE_CODE);
        mMatcher.addURI(AUTHORITY, "task", TASK_CODE);
        mMatcher.addURI(AUTHORITY, "curriculum", CURRICULUM_CODE);
        mMatcher.addURI(AUTHORITY, "subject", SUBJECT_CODE);
        // 若URI资源路径 = content://com.stupidtree.hita.provider/timetable ，则返回注册码TIMETABLE_CODE
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        // 在ContentProvider创建时对数据库进行初始化
        // 运行在主线程，故不能做耗时操作,此处仅作展示
        mDbHelper = new HITADBHelper(mContext);
        db = mDbHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
        // 该方法在最下面
        String table = getTableName(uri);
        // 查询数据
        return db.query(table,projection,selection,selectionArgs,null,null,sortOrder,null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
        String table = getTableName(uri);
        // 向该表添加数据
        db.insert(table, null, values);
        // 当该URI的ContentProvider数据发生变化时，通知外界（即访问该ContentProvider数据的访问者）
        mContext.getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table = getTableName(uri);
        return db.delete(table,selection,selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table = getTableName(uri);
        return db.update(table,values,selection,selectionArgs);
    }



    /**
     * 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
     */
    private String getTableName(Uri uri){
        String tableName = null;
        switch (mMatcher.match(uri)) {
            case TIMETABLE_CODE:
                tableName = HITADBHelper.TIMETABLE_TABLE_NAME;
                break;
            case SUBJECT_CODE:
                tableName = HITADBHelper.SUBJECT_TABLE_NAME;
                break;
            case CURRICULUM_CODE:
                tableName = HITADBHelper.CURRICULUM_TABLE_NAME;
                break;
            case TASK_CODE:
                tableName = HITADBHelper.TASK_TABLE_NAME;
                break;
        }
        return tableName;
    }

}
