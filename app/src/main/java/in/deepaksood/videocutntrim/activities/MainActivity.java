package in.deepaksood.videocutntrim.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.appyvet.rangebar.RangeBar;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

import in.deepaksood.videocutntrim.R;

public class MainActivity extends AppCompatActivity {

    // TAG for logging
    private static final String TAG = MainActivity.class.getSimpleName();

    // static variable to store the request code for browse functionality
    private static final int READ_REQUEST_CODE = 42;

    // linear layout hosts both the seek bar and the timer TextView associated with it
    private LinearLayout llCutController;

    // browse button for searching video from file explorer
    private Button btnBrowse;
    private Button btnCut;
    private TextView tvBrowse;
    private RangeBar rbView;
    private TextView tvStartTime;
    private TextView tvEndTime;

    private VideoView vvPlayer;
    private MediaController mediaController;
    private int videoDurationSeconds;

    private Uri uri;
    private int cutStartTimeSeconds;
    private int cutEndTimeSeconds;
    private FFmpeg ffmpeg;
    private String uploadVideoName;
    private String cutVideoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llCutController = (LinearLayout) findViewById(R.id.ll_cut_controller);
        llCutController.setVisibility(View.INVISIBLE);
        tvStartTime = (TextView) findViewById(R.id.tv_start_time);
        tvEndTime = (TextView) findViewById(R.id.tv_end_time);
        rbView = (RangeBar) findViewById(R.id.rb_view);
        vvPlayer = (VideoView) findViewById(R.id.vv_player);

        tvBrowse = (TextView) findViewById(R.id.tv_browse);
        btnCut = (Button) findViewById(R.id.btn_cut);
        btnBrowse = (Button) findViewById(R.id.btn_browse);
        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });

        btnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uri != null && !uri.toString().contentEquals("")) {
                    Log.v(TAG,"cutStartTimeSeconds: "+cutStartTimeSeconds);
                    Log.v(TAG,"cutEndTimeSeconds: "+cutEndTimeSeconds);
                    cutVideo(cutStartTimeSeconds, cutEndTimeSeconds);

                } else {
                    Toast.makeText(MainActivity.this, "please select a video first", Toast.LENGTH_SHORT).show();
                }

            }
        });

        loadFFMPEGBinary();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.v(TAG,"data: "+data);
            if(data != null) {
                uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                tvBrowse.setText(uri.toString());
                uploadVideoName = uri.getLastPathSegment();
                Log.v(TAG,"uri.getLastPathSegment: "+uri.getLastPathSegment());
                setVideoContainer();
            }
        } else {
            Log.v(TAG,"requestCode: "+requestCode);
        }
    }

    private void setVideoContainer() {
        if(uri != null) {
            mediaController = new MediaController(this) {
                @Override
                public void hide() {}
            };
            mediaController.setAnchorView(vvPlayer);
            vvPlayer.setMediaController(mediaController);
            vvPlayer.setVideoURI(uri);
            vvPlayer.requestFocus();
            vvPlayer.start();
            llCutController.setVisibility(View.VISIBLE);
        }

        vvPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                videoDurationSeconds = mp.getDuration()/1000;
                Log.v(TAG,"VideoDurationSeconds: "+videoDurationSeconds);
                rbView.setVisibility(View.VISIBLE);
                rbView.setTickEnd(videoDurationSeconds);
                tvEndTime.setText(convertTime(videoDurationSeconds));
            }
        });

        rbView.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                vvPlayer.seekTo(leftPinIndex*1000);
                cutStartTimeSeconds = leftPinIndex;
                cutEndTimeSeconds = rightPinIndex;
                tvStartTime.setText(convertTime(leftPinIndex));
                tvEndTime.setText(convertTime(rightPinIndex));
            }
        });


    }

    public String convertTime(int videoDurationSeconds) {
        int hr = videoDurationSeconds / 3600;
        int rem = videoDurationSeconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
    }

    private void loadFFMPEGBinary() {

        if(ffmpeg == null) {
            ffmpeg = FFmpeg.getInstance(this);
        }
        try {
            ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.v(TAG,"ffmpeg not supported");
                    Toast.makeText(MainActivity.this, "Ffmpeg not supported", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                }

                @Override
                public void onSuccess() {
                    Log.v(TAG,"ffmpeg supported");
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void cutVideo(int cutStartTimeSeconds, int cutEndTimeSeconds) {

        getSaveDirectory();


    }

    private void getSaveDirectory() {
        File directoryFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        if(directoryFile.exists() && directoryFile.isDirectory()) {
            cutVideoName = directoryFile.getAbsolutePath() + "/cropped_"+uploadVideoName+".mp4";
            Log.v(TAG,"cutVideoName: "+cutVideoName);
        }
    }

}
