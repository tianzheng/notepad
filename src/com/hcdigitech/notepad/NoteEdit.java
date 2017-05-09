package com.hcdigitech.notepad;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class NoteEdit extends Activity {
	 HCDUtils hcdUtils;
	private static final int DELETE = 0;
	private static final int SAVE = 1;
	private EditText mTitleText;
	private EditText mBodyText;
	private Long mRowId;
	private String temp_title;
	private String temp_body;
	private NotesDbAdapter mDbHelper;
	private CheckBox checkBoxSaveSDCard;
	public final static String TXT_SAVE_PATH = "/notepad/save";

	

    public static String exec(String command) {
    	StringBuffer output = new StringBuffer();
    	Log.d("exec", command);
    	try {
    		Process process = Runtime.getRuntime().exec(command);
    		DataInputStream stdout = new DataInputStream(process.getInputStream());
    		String line;
    		while ((line = stdout.readLine()) != null) {
    			output.append(line).append('\n');
    		}
    		process.waitFor();
    	} catch (Exception e) {
    		output.append('\n').append(e.toString());
    	}
    	return output.toString();
    } 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		 hcdUtils = new HCDUtils();
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();
		setContentView();
		mTitleText = (EditText) findViewById(R.id.title);
		mBodyText = (EditText) findViewById(R.id.body);

		Button confirmButton = (Button) findViewById(R.id.confirm);

		mRowId = savedInstanceState != null ? savedInstanceState
				.getLong(NotesDbAdapter.KEY_ROWID) : null;
			
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
			Log.i("", "1="+mRowId);
		}

		populateFields();

		confirmButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				if (mTitleText.length() == 0) {
					Toast.makeText(view.getContext(), R.string.input_error,
							Toast.LENGTH_LONG).show();
				} else {
					setResult(RESULT_OK);
					saveState();
					finish();
				}
			}
		});
		
	}

	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);
		temp_title = mTitleText.getText().toString();
		temp_body = mBodyText.getText().toString();
		setContentView();

		mTitleText = (EditText) findViewById(R.id.title);
		mBodyText = (EditText) findViewById(R.id.body);
		Button confirmButton = (Button) findViewById(R.id.confirm);
		// mRowId = savedInstanceState != null ?
		// savedInstanceState.getLong(NotesDbAdapter.KEY_ROWID)
		// : null;
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
		}

		populateFields();
		mTitleText.setText(temp_title);
		mBodyText.setText(temp_body);
		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (mTitleText.length() == 0) {
					Toast.makeText(view.getContext(), R.string.input_error,
							Toast.LENGTH_LONG).show();
				} else {
					setResult(RESULT_OK);
					saveState();
					finish();
				}
			}
		});
	}

	private void setContentView() {
			this.setContentView(R.layout.note_edit_v);
		
	}

	private void populateFields() {
		if (mRowId != null) {
			Cursor note = mDbHelper.fetchNote(mRowId);
			startManagingCursor(note);
			mTitleText.setText(note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
			mBodyText.setText(note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
		}

	}



	



	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
	}

	@Override
	protected void onPause() {		
		super.onPause();
		
	    exec("Home_flag 1");
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
		 exec("Home_flag 0");
	}
	

	private void saveState() {
		String title = mTitleText.getText().toString();
		String body = mBodyText.getText().toString();

		if (mRowId == null) {
			if ((title.length() != 0) || (body.length() != 0)) {
				long id = mDbHelper.createNote(title, body);
				if (id > 0) {
					mRowId = id;
				}
			}
		} else {
			mDbHelper.updateNote(mRowId, title, body);
		}
	}

	

	
	
	protected void saveFile(String fileName) throws IOException {
		// TODO Auto-generated method stub
		File f = null;
		File fileFolder;
		if (checkBoxSaveSDCard.isChecked()) {
			fileFolder = new File("/mnt/"+hcdUtils.get_storage_loc()+ TXT_SAVE_PATH);
		} else {
			fileFolder = new File("/"+hcdUtils.get_storage_loc() + TXT_SAVE_PATH);
		}

		if (fileName.length() == 0)
			return;

		if (!fileFolder.exists()) {
			fileFolder.mkdirs();
		}

		if (checkBoxSaveSDCard.isChecked()) {
			f = new File("/mnt/"+hcdUtils.get_storage_loc() + TXT_SAVE_PATH + "/" + fileName + ".txt");
		} else {
			f = new File("/"+hcdUtils.get_storage_loc() + TXT_SAVE_PATH + "/" + fileName
					+ ".txt");
		}

		if (f.exists()) {
			Toast.makeText(this, R.string.file_already_exist_reminder, Toast.LENGTH_LONG).show();
		} else {
			f.createNewFile();
			String title = "SUBJECT:" + mTitleText.getText().toString() + "\n";
			String body = "BODY:" + mBodyText.getText().toString();
			String s = title + body;
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write(s, 0, s.length());
			bw.close();
		}

	}

}
