package in.deepaksood.videocutntrim.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import in.deepaksood.videocutntrim.R;
import in.deepaksood.videocutntrim.utils.AudioRecyclerViewAdapter;
import in.deepaksood.videocutntrim.utils.CommonUtils;
import in.deepaksood.videocutntrim.utils.Constants;
import in.deepaksood.videocutntrim.utils.VideoRecyclerViewAdapter;

public class CreateStory extends AppCompatActivity implements View.OnClickListener {

    // TAG for logging
    private static final String TAG = CreateStory.class.getSimpleName();

    // RecyclerView to show video roll container
    private RecyclerView videoRecyclerView;
    private VideoRecyclerViewAdapter videoRecyclerViewAdapter;

    // RecyclerView to show audio roll container
    private RecyclerView audioRecyclerView;
    private AudioRecyclerViewAdapter audioRecyclerViewAdapter;

    private Button btnAddContainer;
    private Button btnDeleteContainer;
    private Button btnCreate;
    private Button btnRecordAudio;
    private Button btnPlayStory;

    // Uri for data
    private Uri uri;

    // Progress Dialog to show when the ffmpeg command is running
    private ProgressDialog progressDialog;

    // holds the maximum number of commands that are goind to run
    int max_num_of_commands = 0;

    // holds the commands that are completed in a given moment
    int num_of_commands_completed = 0;

    /* Store all intermediateFiles and delete after use */
    ArrayList<String> intermediateFiles;

    /* Video Created - true, Audio Created - false */
    boolean videoCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);

        btnAddContainer = (Button) findViewById(R.id.btn_add_container);
        btnDeleteContainer = (Button) findViewById(R.id.btn_delete_container);
        btnCreate = (Button) findViewById(R.id.btn_create);
        btnRecordAudio = (Button) findViewById(R.id.btn_record_audio);
        btnPlayStory = (Button) findViewById(R.id.btn_play_story);

        // this is for populating recycler view
        Constants.imageUriList.add("");
        Constants.audioUriList.add("");
        videoRecyclerViewAdapter = new VideoRecyclerViewAdapter(this);
        audioRecyclerViewAdapter = new AudioRecyclerViewAdapter(this);

        /* Recycler view initialization */
        videoRecyclerView = (RecyclerView) findViewById(R.id.rv_timeline_video);
        LinearLayoutManager videoLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        videoRecyclerView.setLayoutManager(videoLinearLayoutManager);
        videoRecyclerView.setAdapter(videoRecyclerViewAdapter);

        audioRecyclerView = (RecyclerView) findViewById(R.id.rv_timeline_audio);
        LinearLayoutManager audioLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        audioRecyclerView.setLayoutManager(audioLinearLayoutManager);
        audioRecyclerView.setAdapter(audioRecyclerViewAdapter);

        btnAddContainer.setOnClickListener(this);
        btnDeleteContainer.setOnClickListener(this);
        btnCreate.setOnClickListener(this);
        btnRecordAudio.setOnClickListener(this);
        btnPlayStory.setOnClickListener(this);

        /* initializing progress bar, used when ffmpeg commands are run */
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Merging files");
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
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.VIDEO_READ_REQUEST_CODE:
                    if(data != null) {
                        uri = data.getData();

                        /* Set the data returned from the FileManager intent to the position requested */
                        Log.i(TAG, "Uri Video: " + uri.toString());
                        Constants.imageUriList.set(Constants.update_position, uri.toString());
                        videoRecyclerViewAdapter.notifyDataSetChanged();
                    }
                    break;

                case Constants.AUDIO_READ_REQUEST_CODE:
                    if(data != null) {
                        uri = data.getData();

                        /* Set the data returned from the FileManager intent to the position requested */
                        Log.i(TAG, "Uri Audio: " + uri.toString());
                        Constants.audioUriList.set(Constants.update_position, uri.toString());
                        audioRecyclerViewAdapter.notifyDataSetChanged();
                    }
                    break;

                default:
                    Toast.makeText(this, "invalid request", Toast.LENGTH_SHORT).show();
                    Log.v(TAG,"requestCode: "+requestCode);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_container:
                Toast.makeText(this, "add", Toast.LENGTH_SHORT).show();
                Constants.imageUriList.add("");
                Constants.audioUriList.add("");
                videoRecyclerViewAdapter.notifyItemInserted(Constants.imageUriList.size() - 1);
                videoRecyclerView.getLayoutManager().scrollToPosition(Constants.imageUriList.size() - 1);

                audioRecyclerViewAdapter.notifyItemInserted(Constants.audioUriList.size() -1);
                audioRecyclerView.getLayoutManager().scrollToPosition(Constants.audioUriList.size() -1);
                break;

            case R.id.btn_delete_container:
                if(Constants.imageUriList.size() != 0) {
                    Toast.makeText(this, "delete", Toast.LENGTH_SHORT).show();
                    Constants.imageUriList.remove(Constants.imageUriList.size() - 1);
                    Constants.audioUriList.remove(Constants.audioUriList.size() - 1);
                    videoRecyclerViewAdapter.notifyDataSetChanged();
                    audioRecyclerViewAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Timeline empty", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_create:
                Toast.makeText(this, "create story", Toast.LENGTH_SHORT).show();
                createStory();
                break;

            case R.id.btn_record_audio:
                Toast.makeText(this, "Record audio clip", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, RecordActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_play_story:
                if(videoCreated) {
                    Toast.makeText(this, "Playing the created video story", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Playing the created audio story", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * This will merge image and audio and then merge all videos
     */
    private void createStory() {
        int numOfFrames = Constants.audioUriList.size();
        Log.d(TAG, "numOfFrames: " + numOfFrames);

        /* will hold the flags if the timeline is null or not */
        boolean imageTimeline = true;
        boolean audioTimeline = true;
        for(int i = 0; i < numOfFrames; i++) {
            if(Constants.imageUriList.get(i).equals("")) {
                imageTimeline = false;
            }
            if(Constants.audioUriList.get(i).equals("")) {
                audioTimeline = false;
            }
        }

        if(imageTimeline && audioTimeline) {
            max_num_of_commands = numOfFrames + 1;
            Log.d(TAG, "Max Commands to execute: " + max_num_of_commands);

            intermediateFiles = new ArrayList<>();

            for(int i = 0; i < numOfFrames; i++) {
                // both image and audio present create a image+audio merged video
                String intermediateFilePath = Constants.directoryPath
                        + File.separator
                        + "intermediate_"
                        + i + ".ts";
                intermediateFiles.add(intermediateFilePath);

                String[] merge_command = {"-y", "-loop", "1",
                        "-i", CommonUtils.getInstance().getPath(this, Uri.parse(Constants.imageUriList.get(i))),
                        "-i", CommonUtils.getInstance().getPath(this, Uri.parse(Constants.audioUriList.get(i))),
                        "-acodec", "aac",
                        "-vcodec", "mpeg4",
                        "-f", "mpegts",
                        "-shortest", intermediateFilePath};
                Log.d(TAG, "merge_command: " + Arrays.toString(merge_command));

                execFFmpegBinary(merge_command);
            }

            String concat_command = "concat:";
            for(int i = 0; i < numOfFrames; i++) {
                concat_command += intermediateFiles.get(i);
                if(i != intermediateFiles.size()-1) {
                    concat_command += "|";
                }
            }
            String storyVideoName = Constants.directoryPath
                    + File.separator
                    + "Video_Story_"
                    + CommonUtils.getInstance().getTimeStamp()
                    + ".mp4";

            String[] join_command = {"-y", "-i", concat_command,
                        "-c", "copy", "-bsf:a", "aac_adtstoasc", storyVideoName};

            Log.d(TAG, "join_command: " + Arrays.toString(join_command));

            videoCreated = true;

            execFFmpegBinary(join_command);
        } else if(!imageTimeline && audioTimeline) {
            // imageTimeline null and audioTimeline not null, create a merge audio mp3
            Toast.makeText(this, "Creating merged audio file", Toast.LENGTH_SHORT).show();

            max_num_of_commands = 1;
            Log.d(TAG, "Max Commands to execute: " + max_num_of_commands);

            String concat_command = "concat:";
            for(int i = 0; i < numOfFrames; i++) {
                concat_command += CommonUtils.getInstance().getPath(this, Uri.parse(Constants.audioUriList.get(i)));
                if(i != numOfFrames-1) {
                    concat_command += "|";
                }
            }
            String storyVideoName = Constants.directoryPath
                    + File.separator
                    + "Audio_Story_"
                    + CommonUtils.getInstance().getTimeStamp()
                    + ".mp3";

            String[] join_command = {"-y", "-i", concat_command,
                    "-c", "copy", storyVideoName};

            Log.d(TAG, "join_command: " + Arrays.toString(join_command));

            videoCreated = false;

            execFFmpegBinary(join_command);

        } else {
            // both imageTimeline and audioTimeline null
            Toast.makeText(this, "Please fill the images and audios, and delete rest", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This will execute the ffmpeg command
     *
     * @param command
     */
    private void execFFmpegBinary(final String[] command) {
        if(CommonUtils.getInstance().ffmpeg != null) {
            try {

                CommonUtils.getInstance().ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onFailure(String s) {
                        Log.d(TAG, "FAILED with output : " + s);
                        progressDialog.dismiss();
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
                        Log.d(TAG, "Finished command: " + num_of_commands_completed);
                        // progress dialog must be removed when all the commands are completed
                        if(num_of_commands_completed == max_num_of_commands) {
                            Log.d(TAG, "All commands processed");
                            Toast.makeText(CreateStory.this, "Merged Successfully", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                            btnPlayStory.setVisibility(View.VISIBLE);

                            if(videoCreated) {
                                // All the intermediate file must be deleted, for next round of cutting only when video_story is created
                                Log.d(TAG, "Deleting file in progress");
                                for(int i = 0; i < intermediateFiles.size(); i++) {
                                    File file = new File(intermediateFiles.get(i));
                                    if(file.exists()) {
                                        Log.d(TAG, "deleting intermediate files: "+file.delete());
                                    }
                                }
                            }
                            num_of_commands_completed = 0;
                        /* Convert the new file created to uri to play it in videoView using setVideoContainer() */
                            /*uri = Uri.fromFile(new File(cutVideoName));
                            setVideoContainer();*/
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
