package com.example.mercury.photocapturer;

import android.app.Dialog;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by mercury on 7/31/2015.
 */
public class BufferFragment extends Fragment {

    private ImageView picture;
    private Button saveButton;
    private Button cancelButton;
    private EditText editText;
    private SaveProgress saveProgress;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buffer, container, false);
        picture = (ImageView) rootView.findViewById(R.id.id_picture);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_buffer_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                callSaveDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void callSaveDialog() {

        final Dialog saveDialog = new Dialog(getActivity());
        saveDialog.setTitle(getResources().getString(R.string.dialog_title));
        saveDialog.setContentView(R.layout.dialog_save);
        saveButton = (Button) saveDialog.findViewById(R.id.save_button);
        cancelButton = (Button) saveDialog.findViewById(R.id.cancel_button);
        editText = (EditText) saveDialog.findViewById(R.id.enter_name);
        saveDialog.show();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDialog.dismiss();
                saveProgress = new SaveProgress();
                saveProgress.execute(editText.getText().toString());
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDialog.dismiss();
            }
        });
    }

    public void saveImageToInternalStorage(String files_name){

        File file = new File(Environment.getExternalStorageDirectory() + "/Pictures");
        if (!file.isDirectory()) {
            file.mkdir();
        }
        file = new File(Environment.getExternalStorageDirectory() + "/Pictures", files_name + ".png");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            picture.buildDrawingCache();
            Bitmap bitmap = picture.getDrawingCache();
            Log.d("TAAG", bitmap == null? "null": "not null");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (FileNotFoundException e) {
            Log.e("MyError", "smth wrong with bitmap");
        }
    }

    public class SaveProgress extends AsyncTask<String, Void, Void>{

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            saveImageToInternalStorage(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), "Image is saved", Toast.LENGTH_SHORT).show();
            super.onPostExecute(aVoid);
            saveProgress = null;
        }
    }
}