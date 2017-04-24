package in.deepaksood.videocutntrim.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;

import in.deepaksood.videocutntrim.R;
import in.deepaksood.videocutntrim.utils.CommonUtils;
import in.deepaksood.videocutntrim.utils.Constants;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {

    // Tag for loggging
    private static final String TAG = RecordActivity.class.getSimpleName();

    private ImageView ivRecordContainer;
    private Button btnRecord;
    private Button btnPlay;

    /* This int will hold the position for which the activity is called */
    private int recordPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        ivRecordContainer = (ImageView) findViewById(R.id.iv_record_container);
        btnRecord = (Button) findViewById(R.id.btn_record);
        btnPlay = (Button) findViewById(R.id.btn_play);

        recordPosition = getIntent().getIntExtra(Constants.EXTRA_POSITION, 0);

        Picasso.with(this)
                .load(Constants.imageUriList.get(recordPosition))
                .resize(250, 250)
                .centerCrop()
                .into(ivRecordContainer);


        btnRecord.setOnClickListener(this);
        btnPlay.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_record:
                if(btnRecord.getText().equals(getResources().getString(R.string.btn_start_recording))) {
                    // toggle button
                    btnRecord.setText(R.string.btn_stop_recording);

                    String filePath = Constants.directoryPath
                            + File.separator
                            + "audio_"
                            + CommonUtils.getInstance().getTimeStamp()
                            + ".mp3";

                    Constants.audioUriList.set(recordPosition, filePath);

                    // start recording
                    CommonUtils.getInstance().startAudioRecording(filePath);
                } else {
                    // toggle button
                    btnRecord.setText(R.string.btn_start_recording);

                    // stop recording
                    CommonUtils.getInstance().stopAudioRecording();
                }

                break;

            case R.id.btn_play:
                if(btnPlay.getText().equals(getResources().getString(R.string.btn_play_audio))) {
                    File audioFile = new File(Constants.audioUriList.get(recordPosition));
                    if(audioFile.exists() && audioFile.isFile()) {
                        // toggle button
                        btnPlay.setText(R.string.btn_stop_audio);

                        // start audio playing
                        CommonUtils.getInstance().startAudioPlaying(Constants.audioUriList.get(recordPosition));
                    } else {
                        Toast.makeText(this, "Recorded audio not found", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Recorded audio not found: "
                                + Constants.audioUriList.get(recordPosition)
                                + " Pos: " + recordPosition);
                    }
                } else {
                    // toggle button
                    btnPlay.setText(R.string.btn_play_audio);

                    // stop audio playing
                    CommonUtils.getInstance().stopAudioPlaying();
                }
                break;
        }
    }
}
