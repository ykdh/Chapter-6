package com.byted.camp.todolist.db;

import android.provider.BaseColumns;


/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public final class TodoContract {

    // TODO 定义表结构和 SQL 语句常量

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MyEntry.TABLE_NAME + " (" +
                    MyEntry._ID + " INTEGER PRIMARY KEY," +
                    MyEntry.COLUMN_NAME_TITLE + " TEXT," +
                    MyEntry.COLUMN_NAME_STATE + " TEXT," +
                    MyEntry.COLUMN_NAME_SUBTITLE + " TEXT)";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + MyEntry.TABLE_NAME;

    public static final String SQL_ADD_COLUMN = "alter table " +
            MyEntry.TABLE_NAME + " add column " +
            MyEntry.COLUMN_NAME_PRIORITY + " integer default 0";

    private TodoContract() {
    }

    public static class MyEntry implements BaseColumns {

        public static final String TABLE_NAME = "todoList";

        public static final String COLUMN_NAME_TITLE = "todo";

        public static final String COLUMN_NAME_SUBTITLE = "time";

        public static final String COLUMN_NAME_STATE = "state";

        public static final String COLUMN_NAME_PRIORITY = "priority";
    }
}
