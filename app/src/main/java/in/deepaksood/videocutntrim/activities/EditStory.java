package in.deepaksood.videocutntrim.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import in.deepaksood.videocutntrim.R;
import in.deepaksood.videocutntrim.utils.CommonUtils;
import in.deepaksood.videocutntrim.utils.Constants;

/**
 * EditStory where all the processing is happening
 * Layout - activity_edit_storydit_story.xml
 * @author deepak
 */
public class EditStory extends AppCompatActivity {

    // TAG for logging
    private static final String TAG = EditStory.class.getSimpleName();

    // constant to store the request code for browse the video functionality
    private static final int READ_REQUEST_CODE = 42;

    // linear layout hosts both the seek bar and the timer TextView associated with it
    private LinearLayout llCutController;

    // browse button for searching video from file explorer
    private Button btnBrowse;
    private Button btnCut;
    private RangeBar rbView;
    private TextView tvStartTime;
    private TextView tvEndTime;

    private VideoView vvPlayer;
    private MediaController mediaController;
    private int videoDurationSeconds;

    private Uri uri;

    // the seek bar start position
    private int cutStartTimeSeconds;

    // the seek bar end position
    private int cutEndTimeSeconds;
    private String uploadVideoName;
    private String cutVideoName;

    // Path for intermediate video location
    String cut1Location;
    String cut2Location;
    String temp1Location;
    String temp2Location;

    // Progress Dialog to show when the ffmpeg command is running
    private ProgressDialog progressDialog;

    // holds the maximum number of commands that are goind to run
    int max_num_of_commands = 0;

    // holds the commands that are completed in a given moment
    int num_of_commands_completed = 0;

    /**
     * This is the first state that activity enters
     * @param savedInstanceState
     *                          Used when the application is restarted
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_story);

        /* map all the layout items with respective objects and also set a listener for the buttons */
        llCutController = (LinearLayout) findViewById(R.id.ll_cut_controller);
        llCutController.setVisibility(View.INVISIBLE);
        tvStartTime = (TextView) findViewById(R.id.tv_start_time);
        tvEndTime = (TextView) findViewById(R.id.tv_end_time);
        rbView = (RangeBar) findViewById(R.id.rb_view);
        vvPlayer = (VideoView) findViewById(R.id.vv_player);

        btnCut = (Button) findViewById(R.id.btn_cut);
        btnBrowse = (Button) findViewById(R.id.btn_browse);
        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditStory.this, "Clicked", Toast.LENGTH_SHORT).show();
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
                    cutVideo(cutStartTimeSeconds, cutEndTimeSeconds);

                } else {
                    Toast.makeText(EditStory.this, "please select a video first", Toast.LENGTH_SHORT).show();
                }

            }
        });

        /* initializing progress bar, used when ffmpeg commands are run */
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cutting file");
        progressDialog.setCancelable(false);
    }

    /**
     * This is callback method after the browse intent has been fired.
     *
     * @param requestCode
     *                  constant containing the request from the activity
     * @param resultCode
     *                  constant that contain the result returned after the intent returns
     * @param data
     *                  the selected video uri returned
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.v(TAG,"data: "+data);
            if(data != null) {
                uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                setVideoContainer();
            }
        } else {
            Log.v(TAG,"requestCode: "+requestCode);
        }
    }

    private void setVideoContainer() {
        if(uri != null) {
            mediaController = new MediaController(this);
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
                rbView.setTickEnd(videoDurationSeconds);
                tvEndTime.setText(convertTime(videoDurationSeconds));

                /* for setting the tick pin to start and end positions every time */
                rbView.setRangePinsByIndices(0, videoDurationSeconds);
                rbView.setVisibility(View.VISIBLE);
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

    private void cutVideo(int cutStartTimeSeconds, int cutEndTimeSeconds) {

        String videoLocation = CommonUtils.getInstance().getPath(EditStory.this, uri);

        // ex - /storage/emulated/0/VideoEditor/videoplayback.mp4
        if(videoLocation != null) {
            String[] splitted = videoLocation.split("/");
            uploadVideoName = splitted[splitted.length-1];
            Log.d(TAG,"UploadVideoName: "+uploadVideoName);
        }

        if(getSaveDirectory()) {
            cut1Location = Constants.directoryPath + File.separator + "cut1.mp4";
            cut2Location = Constants.directoryPath + File.separator + "cut2.mp4";
            temp1Location = Constants.directoryPath + File.separator + "intermediate1.ts";
            temp2Location = Constants.directoryPath + File.separator + "intermediate2.ts";

            Log.d(TAG, "video start time: " + 0);
            Log.d(TAG, "video end time: " + videoDurationSeconds);
            Log.d(TAG, "cutStartTimeSeconds: " + cutStartTimeSeconds);
            Log.d(TAG, "cutEndTimeSeconds: " + cutEndTimeSeconds);
            Log.d(TAG, "uri: " + uri);
            Log.d(TAG, "videoLocation: " + videoLocation);
            Log.d(TAG, "cutVideoName: " + cutVideoName);

            max_num_of_commands = 5;
            num_of_commands_completed = 0;

            // ffmpeg -y -ss 0 -i videoplayback.mp4 -t 20 -c copy cut1.mp4
            String[] cmd_cut_1 = {"-y" ,"-ss", "" + 0, "-i", videoLocation, "-t", "" + cutStartTimeSeconds, "-c", "copy", cut1Location};
            System.out.println("cmd_cut_1: " + Arrays.toString(cmd_cut_1));
            execFFmpegBinary(cmd_cut_1);

            // ffmpeg -y -ss 40 -i videoplayback.mp4 -t 235 -c copy cut2.mp4
            String[] cmd_cut_2 = {"-y", "-ss", "" + cutEndTimeSeconds, "-i", videoLocation, "-t", "" + videoDurationSeconds, "-c", "copy", cut2Location};
            System.out.println("cmd_cut_2: " + Arrays.toString(cmd_cut_2));
            execFFmpegBinary(cmd_cut_2);

            // ffmpeg -y -i cut1.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts intermediate1.ts
            String[] cmd_convert_1 = {"-y", "-i", cut1Location, "-c", "copy", "-bsf:v", "h264_mp4toannexb", "-f", "mpegts", temp1Location};
            System.out.println("cmd_convert_1: " + Arrays.toString(cmd_convert_1));
            execFFmpegBinary(cmd_convert_1);

            // ffmpeg -y -i cut2.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts intermediate2.ts
            String[] cmd_convert_2 = {"-y", "-i", cut2Location, "-c", "copy", "-bsf:v", "h264_mp4toannexb", "-f", "mpegts", temp2Location};
            System.out.println("cmd_convert_2: " + Arrays.toString(cmd_convert_2));
            execFFmpegBinary(cmd_convert_2);

            // ffmpeg -y -i "concat:intermediate1.ts|intermediate2.ts" -c copy -bsf:a aac_adtstoasc final.mp4
            String[] cmd_join = {"-y", "-i", "concat:"+temp1Location+"|"+temp2Location, "-c", "copy", "-bsf:a", "aac_adtstoasc", cutVideoName};
            System.out.println("cmd_join: " + Arrays.toString(cmd_join));
            execFFmpegBinary(cmd_join);
        } else {
            Toast.makeText(this, "VideoName not fetched", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean getSaveDirectory() {
        if(uploadVideoName != null && !uploadVideoName.equals("")) {
            File directoryFile = new File(Constants.directoryPath);
            if(directoryFile.exists() && directoryFile.isDirectory()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                Date date = new Date();
                String timeStamp = simpleDateFormat.format(date);
                cutVideoName = directoryFile.getAbsolutePath() + "/cropped_" + timeStamp + "_" + uploadVideoName;
                Log.v(TAG,"cutVideoName: "+cutVideoName);
            }
            return true;
        } else {
            return false;
        }
    }

    private void execFFmpegBinary(final String[] command) {
        if(CommonUtils.getInstance().ffmpeg != null) {
            try {

                CommonUtils.getInstance().ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onFailure(String s) {
                        Log.d(TAG, "FAILED with output : " + s);
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.d(TAG, "SUCCESS with output : " + s);

                    }

                    @Override
                    public void onProgress(String s) {
                        Log.d(TAG, "progress : " + s);
                    }

                    @Override
                    public void onStart() {
                        Log.d(TAG, "Started command");
                        progressDialog.setMessage("Processing...");
                        progressDialog.show();
                    }

                    @Override
                    public void onFinish() {
                        num_of_commands_completed++;
                        Log.d(TAG, "Finished command");
                        // progress dialog must be removed when all the commands are completed
                        if(num_of_commands_completed == max_num_of_commands) {
                            Log.d(TAG, "All commands processed");
                            progressDialog.dismiss();

                            // All the intermediate file must be deleted, for next round of cutting
                            Log.d(TAG, "Deleting file in progress");
                            File file = new File(cut1Location);
                            if(file.exists()) {
                                Log.d(TAG, ""+file.delete());
                            }
                            file = new File(cut2Location);
                            if(file.exists()) {
                                Log.d(TAG, ""+file.delete());
                            }
                            file = new File(temp1Location);
                            if(file.exists()) {
                                Log.d(TAG, ""+file.delete());
                            }
                            file = new File(temp2Location);
                            if(file.exists()) {
                                Log.d(TAG, ""+file.delete());
                            }

                        /* Convert the new file created to uri to play it in videoView using setVideoContainer() */
                            uri = Uri.fromFile(new File(cutVideoName));
                            setVideoContainer();
                        }
                    }
                });
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "FFMPEG not loaded", Toast.LENGTH_SHORT).show();
        }
    }
}
