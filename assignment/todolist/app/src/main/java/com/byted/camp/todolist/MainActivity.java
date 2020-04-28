package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Collections;
import java.util.Comparator;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new TodoDbHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans

        List<Note> myNote = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(TodoContract.MyEntry.TABLE_NAME, null, null, null, null, null, null);
        int count = cursor.getCount();
        while (cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(TodoContract.MyEntry._ID));
            String title = cursor.getString(cursor.getColumnIndex(TodoContract.MyEntry.COLUMN_NAME_TITLE));
            String subTitle = cursor.getString(cursor.getColumnIndex(TodoContract.MyEntry.COLUMN_NAME_SUBTITLE));
            String state = cursor.getString(cursor.getColumnIndex(TodoContract.MyEntry.COLUMN_NAME_STATE));
            String priority = cursor.getString(cursor.getColumnIndex(TodoContract.MyEntry.COLUMN_NAME_PRIORITY));

            try {
                //id
                Note note = new Note(itemId);
                //内容
                note.setContent(title);
                //时间
                SimpleDateFormat sd = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
                Date subTitleDate = sd.parse(subTitle);
                note.setDate(subTitleDate);
                //优先级
                note.setPriority(Integer.parseInt(priority));
                //状态
                if (state.equals("1")) {
                    note.setState(State.DONE);
                } else {
                    note.setState(State.TODO);
                }

                //加入列表
                myNote.add(note);

            } catch (Exception e) {
                Log.d("日期错误", e.toString());
            }
        }
        cursor.close();

        //按优先级排序
        Collections.sort(myNote, new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                Note data1 = (Note) o1;
                Note data2 = (Note) o2;
                return (int) (data2.getPriority() - data1.getPriority());
            }
        });

        if (myNote.size() != 0) {
            return myNote;
        }
        return null;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = TodoContract.MyEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(note.id)};
        int deleteRows = db.delete(TodoContract.MyEntry.TABLE_NAME, selection, selectionArgs);
        //刷新
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    private void updateNode(Note note) {
        // TODO 更新数据
        //连接数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //要更新的数据
        SimpleDateFormat sd = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
        Date curDate = note.getDate();
        String subtitle = sd.format(curDate);
        ContentValues values = new ContentValues();
        values.put(TodoContract.MyEntry.COLUMN_NAME_TITLE, note.getContent());
        values.put(TodoContract.MyEntry.COLUMN_NAME_SUBTITLE, subtitle);
        values.put(TodoContract.MyEntry.COLUMN_NAME_STATE, note.getState().ordinal());
        values.put(TodoContract.MyEntry.COLUMN_NAME_PRIORITY, note.getPriority());

        //要更新的行
        String selection = TodoContract.MyEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(note.id)};

        //更新
        int count = db.update(TodoContract.MyEntry.TABLE_NAME, values, selection, selectionArgs);

        //刷新页面
        notesAdapter.refresh(loadNotesFromDatabase());
    }
}
