package com.hcdigitech.notepad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

public class Notepad extends ListActivity {
	 HCDUtils hcdUtils;
		 
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;

	private static final int ABOUT_ID = Menu.FIRST;
	// private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int IMPORT_ID = Menu.FIRST + 2;

	private NotesDbAdapter mDbHelper;
	private ImageView buttoncreate;
	public final static String TXT_SAVE_PATH = "/notepad/save";
	private static SimpleCursorAdapter notes;

	private String account;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setContentView(R.layout.notes_list);
		// if this.getResources().getConfiguration().orientation
		setContentView();
		hcdUtils = new HCDUtils();
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();
		fillData();
		registerForContextMenu(getListView());

		Bundle bundle = this.getIntent().getExtras();
		try {
			account = bundle.getString("account");
		} catch (Exception e) {
			account = "guest";
		}

		noteadd();
	}

	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);
		setContentView();
		fillData();

		noteadd();
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
		setListAdapter(notes);
	}

	private void noteadd() {
		/* Brian, Test */
		// ImageButton brian_image;
		// brian_image = (ImageButton) findViewById(R.id.notecreate);
		// brian_image.setImageDrawable(getResources().getDrawable(R.drawable.note_btn_create));
		// brian_image.setOnClickListener((android.view.View.OnClickListener)
		// noteadd);
		//
		// this.buttoncreate = (ImageButton) findViewById(R.id.notecreate);
		// buttoncreate.setImageDrawable(getResources().getDrawable(R.drawable.note_btn_create));
		// buttoncreate.setImageResource(R.drawable.note_btn_create);
		buttoncreate = (ImageView) findViewById(R.id.notecreate);
		buttoncreate.setOnClickListener(buttoncreateOnClick);
	}

	private OnClickListener buttoncreateOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			createNote();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	
		return super.onMenuItemSelected(featureId, item);
	}



	private File[] files;

	protected void openFile(final int local) {
		// TODO Auto-generated method stub
		switch (local) {
		case 1:
			files = new File("/"+hcdUtils.get_storage_loc() + TXT_SAVE_PATH.toString())
					.listFiles();
			break;
		case 2:
			files = new File("/mnt/"+hcdUtils.get_storage_loc() + TXT_SAVE_PATH.toString()).listFiles();
			break;
		default:
			break;
		}

		if (files == null) {
			new AlertDialog.Builder(this)
					.setTitle(getString(R.string.note))
					.setMessage(getString(R.string.file_missing))
					.setNegativeButton(getString(R.string.confirmation),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).create().show();

		} else {
			final Dialog mDialog = new Dialog(this);
			mDialog.setTitle(getString(R.string.open_file_select));
			mDialog.setContentView(R.layout.dialog);

			ListView list = (ListView) mDialog.findViewById(R.id.MyTest);

			ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();

			for (int i = 0; i < files.length; i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("text", files[i].getName());
				lstImageItem.add(map);
			}

			SimpleAdapter saImageItems = new SimpleAdapter(this, lstImageItem,
					R.layout.dialog_context, new String[] { "text" },
					new int[] { R.id.text });

			list.setAdapter(saImageItems);

			list.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// mView.select_file(files[arg2].getName());
					try {
						saveToDB(files[arg2].getName(), local);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mDialog.dismiss();
				}
			});

			/*
			 * Set the color that should be used to highlight the currently
			 * selected item.
			 */
			// list.setSelector(new PaintDrawable(Color.RED));
			// list.setDrawingCacheBackgroundColor(Color.WHITE);
			list.setCacheColorHint(Color.WHITE);
			mDialog.show();
		}
	}

	private void saveToDB(String name, int local) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		switch (local) {
		case 1:
			br = new BufferedReader(new FileReader("/"+hcdUtils.get_storage_loc()
					+ TXT_SAVE_PATH + "/" + name));
			break;
		case 2:
			br = new BufferedReader(new FileReader("/mnt/"+hcdUtils.get_storage_loc() + TXT_SAVE_PATH
					+ "/" + name));
			break;
		default:
			break;
		}

		String tempTitle;
		String tempBody = "";
		String tempBody1;
		String tempBody2;
		String tempBody3 = "";
		tempTitle = br.readLine();
		tempTitle = tempTitle.substring(8, tempTitle.length());
		tempBody1 = br.readLine();
		tempBody1 = tempBody1.substring(5, tempBody1.length()) + "\n";

		while ((tempBody2 = br.readLine()) != null) {
			tempBody3 = tempBody3 + tempBody2 + "\n";
		}
		tempBody = tempBody1 + tempBody3;

		// tempBody=br.readLine();
		Log.d(tempTitle, tempTitle);
		Log.d(tempBody, tempBody);

		String title = tempTitle;
		String body = tempBody;

		long id = mDbHelper.createNote(title, body);

		fillData();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			mDbHelper.deleteNote(info.id);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void createNote() {
		Intent i = new Intent(this, NoteEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, NoteEdit.class);
		i.putExtra(NotesDbAdapter.KEY_ROWID, id);
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		fillData();
	}

}
