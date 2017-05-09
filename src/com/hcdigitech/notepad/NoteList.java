package com.hcdigitech.notepad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class NoteList extends Activity {
	HCDUtils hcdUtils;

	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;

	private static final int ABOUT_ID = Menu.FIRST;
	// private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int IMPORT_ID = Menu.FIRST + 2;

	private NotesDbAdapter mDbHelper;
	private ImageView buttoncreate;
	private TextView 	emptyInfo;
	private ListView noteList;
	public final static String TXT_SAVE_PATH = "/notepad/save";
	private static SimpleCursorAdapter notes;

	// private String account;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView();
		hcdUtils = new HCDUtils();
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();
		noteadd();
		fillData();

	}

	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);
		setContentView();
		noteadd();
		fillData();
	}

	private void setContentView() {
		// TODO Auto-generated method stub

		this.setContentView(R.layout.notes_list_v);

	}

	private void fillData() {
		Cursor notesCursor = mDbHelper.fetchAllNotes();
		startManagingCursor(notesCursor);

		// Create an array to specify the fields we want to display in the list
		// (only TITLE)
		String[] from = new String[] { NotesDbAdapter.KEY_TITLE };

		// and an array of the fields we want to bind those fields to (in this
		// case just text1)
		int[] to = new int[] { R.id.text1 };

		// Now create a simple cursor adapter and set it to display
		notes = new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor,
				from, to);
		
		noteList.setAdapter(notes);
		
Log.i("", "数组大小"+notes.getCount());
if(notes.getCount()>=1){
	emptyInfo.setVisibility(View.GONE);
}else {
	emptyInfo.setVisibility(View.VISIBLE);
}
	}

	private void noteadd() {
		emptyInfo= (TextView) findViewById(R.id.empty_info);
		buttoncreate = (ImageView) findViewById(R.id.notecreate);
		noteList = (ListView) findViewById(R.id.note_list);
		buttoncreate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				createNote();

			}
		});
		noteList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(NoteList.this, NoteEdit.class);
				Log.i("", "2=" + id);
				i.putExtra(NotesDbAdapter.KEY_ROWID, id);
				startActivityForResult(i, ACTIVITY_EDIT);

			}
		});

		noteList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				dialog(id);
				return true;
			}
		});

	}

	protected void dialog(final long id) {
		AlertDialog.Builder builder = new Builder(NoteList.this);
		builder.setMessage("确定删除吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确定", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				mDbHelper.deleteNote(id);
				fillData();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});

		builder.create().show();

	}

	private void createNote() {
		Intent i = new Intent(NoteList.this, NoteEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
	}

}
