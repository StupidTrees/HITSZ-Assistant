package com.stupidtree.hita.core;

import android.content.Context;
import android.content.Entity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;

import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.loadDataFromCloud;
import static com.stupidtree.hita.HITAApplication.mDBHelper;

public class HITADBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 36;
    private static final String CREATE_TABLE_TIMETABLE  =
            "create table timetable("
            + "name text  not null,"
            +" type integer not null,"
            + "weeks text not null,"
            +"dow integer not null,"
            +"from_hour integer not null,"
            +"from_minute integr not null,"
            +"to_hour integer not null,"
            +"to_minute integer not null,"
            +"tag2 text,"
            +"tag3 text,"
            +"tag4 text,"
            +"curriculum_code text not null,"
            +"is_whole_day integer not null,"
            +"uuid text primary key"
            + ");";;

    private static final String CREATE_TABLE_SUBJECT  =
            "create table subject ("
                    + "name text  primary key,"
                    +" type text not null,"
                    + "is_mooc integer not null,"
                    +"is_exam integer not null,"
                    +"is_default integer not null,"
                    +"xnxq text,"
                    +"school text,"
                    +"point text,"
                    +"compulsory text,"
                    +"total_courses text,"
                    +"code text,"
                    +"curriculum_code text  not null,"
                    +"scores text,"
                    +"rates text"
                    + ");";;

    private static final String CREATE_TABLE_TASK  =
            "create table task ("
                    + "name text  not null,"
                    +" has_ddl integer not null,"
                    + "ddl_name text ,"
                    +"from_week integer,"
                    +"from_dow integer,"
                    +"from_hour integer,"
                    +"from_minute integer,"
                    +"to_week integer,"
                    +"to_dow integer,"
                    +"to_hour integer,"
                    +"to_minute integer,"
                    +"curriculum_code text not null,"
                    +"every_day integer not null,"
                    +"has_length integer not null,"
                    +"length integer,"
                    +"progress integer,"
                    +"type integer,"
                    +"priority integer,"
                    +"uuid text primary key,"
                    +"event_map text,"
                    +"tag text,"
                    +"finished integer"
                    + ");";

    private static final String CREATE_TABLE_CURRICULUM =
            "create table curriculum("
                    + "name text  not null,"
                    +"curriculum_code text primary key not null,"
                    +"total_weeks integer not null,"
                    +"start_year integer not null,"
                    +"start_month integer not null,"
                    +"start_day integer not null,"
                    +"curriculum_text text not null,"
                    +"object_id text "
                    + ");";;
    public HITADBHelper(Context context) {
        // 传递数据库名与版本号给父类
        super(context, "hita.db", null, DB_VERSION);
    }

//    public static HITADBHelper getInstance(Context context){
//        if(mHTADBHelper==null){
//            mHTADBHelper = new HITADBHelper(context);
//        }
//        return mHTADBHelper;
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CURRICULUM);
        db.execSQL(CREATE_TABLE_SUBJECT);
        db.execSQL(CREATE_TABLE_TIMETABLE);
        db.execSQL(CREATE_TABLE_TASK);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 数据库版本号变更会调用 onUpgrade 函数，在这根据版本号进行升级数据库
        if(oldVersion<30){
           // if(!isColumnExist(db,"subject","compulsory"))  addColumn(db,"subject","compulsory","text");
            //if(isColumnExist(db,"timetable","hashcode"))deleteColumn(db,"timetable","hashcode");
            Log.e("更新表：",oldVersion+"->"+newVersion);
            db.execSQL("DROP TABLE IF EXISTS timetable");
            db.execSQL("DROP TABLE IF EXISTS subject");
            db.execSQL("DROP TABLE IF EXISTS task");
            db.execSQL("DROP TABLE IF EXISTS curriculum");
            db.execSQL(CREATE_TABLE_SUBJECT);
            db.execSQL(CREATE_TABLE_TIMETABLE);
            db.execSQL(CREATE_TABLE_TASK);
            db.execSQL(CREATE_TABLE_CURRICULUM);
        }else if(oldVersion<=35){
           //db.execSQL("DROP TABLE IF EXISTS timetable");
            //db.execSQL("DROP TABLE IF EXISTS task");
            //db.execSQL(CREATE_TABLE_TIMETABLE);
            //db.execSQL(CREATE_TABLE_TASK);
            db.execSQL("alter table task add column event_map text;");
            db.execSQL("alter table task add column uuid text;");
            db.execSQL("alter table task add column tag text;");
            db.execSQL("alter table task add column finished integer;");
            db.execSQL("alter table timetable add column uuid text;");
            db.delete("task",null,null);
            db.delete("timetable","type=?",new String[]{TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT+""});
            db.delete("timetable","type=?",new String[]{TimeTable.TIMETABLE_EVENT_TYPE_REMIND+""});
            db.delete("timetable","type=?",new String[]{TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE+""});
            db.delete("timetable","type=?",new String[]{TimeTable.TIMETABLE_EVENT_TYPE_DYNAMIC+""});
        }
    }


    public void clearTables(){
        SQLiteDatabase sd = getWritableDatabase();
        sd.delete("timetable", null, null);
        sd.delete("curriculum", null, null);
        sd.delete("task", null, null);
        sd.delete("subject", null, null);

    }



    private void addColumn(SQLiteDatabase db,String tableName,String columnName,String dataType){
        db.execSQL("ALTER TABLE " + tableName + " ADD " + columnName + " " + dataType);
    }


    private void deleteColumn(SQLiteDatabase db,String tableName,String columnName){
        db.execSQL("ALTER TABLE " + tableName + " DELETE " + columnName);
    }
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // 启动外键
            db.execSQL("PRAGMA foreign_keys = 1;");
        }
    }

    static boolean isTableExist(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
            boolean hasNext = cursor.moveToNext();
            return hasNext && cursor.getInt(0) > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}