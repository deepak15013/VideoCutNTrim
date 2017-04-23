package in.deepaksood.videocutntrim.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import in.deepaksood.videocutntrim.R;
import in.deepaksood.videocutntrim.utils.CommonUtils;
import in.deepaksood.videocutntrim.utils.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // TAG for logging
    private static final String TAG = MainActivity.class.getSimpleName();

    // constant for storing the runtime permission access for external storage media
    private static final int MY_PERMISSION_WRITE_EXTERNAL_STORAGE = 44;
    private static final int MY_PERMISSION_RECORD_AUDIO = 45;

    // initializing the buttons
    private Button btnCreateStory;
    private Button btnEditStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* First check for permission for external storage, Uses runtime permission */
        checkAndGetRuntimePermissions();

        btnCreateStory = (Button) findViewById(R.id.btn_create_story);
        btnEditStory = (Button) findViewById(R.id.btn_edit_story);

        btnCreateStory.setOnClickListener(this);
        btnEditStory.setOnClickListener(this);

        /* Initializing the directory path where all the temporary files will be stored and retrieved */
        Constants.directoryPath = CommonUtils.getInstance().checkDirectory();
        if(Constants.directoryPath == null) {
            Toast.makeText(this, "Not able to create directory", Toast.LENGTH_SHORT).show();
            finish();
        }

        /* Loading ffmpeg binaries */
        CommonUtils.getInstance().loadFFMPEGBinary(this);
    }

    /**
     * Check if permission for external storage available or not,
     * if not available, then get the permission from user.
     */
    private void checkAndGetRuntimePermissions() {
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSION_RECORD_AUDIO);
        }
    }

    /**
     * This is the callback method after the user permission has been asked for.
     *
     * @param requestCode
     *                  the constant for permission request
     * @param permissions
     * @param grantResults
     *                  the result for the permissions asked
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission Granted for write_external_storage");
                }
                else {
                    Toast.makeText(this, "Storage Access Denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            case MY_PERMISSION_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission Granted for record_audio");
                }
                else {
                    Toast.makeText(this, "Record Audio Access Denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    /**
     * onClick Listener for buttons
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_create_story:
                Toast.makeText(this, "Create Story", Toast.LENGTH_SHORT).show();
                Intent intentCreateStory = new Intent(this, CreateStory.class);
                startActivity(intentCreateStory);
                break;

            case R.id.btn_edit_story:
                Toast.makeText(this, "Edit Story", Toast.LENGTH_SHORT).show();
                Intent intentEditStory = new Intent(this, EditStory.class);
                startActivity(intentEditStory);
                break;
        }
    }
}
