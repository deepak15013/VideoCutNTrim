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
import in.deepaksood.videocutntrim.utils.CommonUtils;
import in.deepaksood.videocutntrim.utils.Constants;
import in.deepaksood.videocutntrim.utils.RecyclerViewAdapter;

public class CreateStory extends AppCompatActivity implements View.OnClickListener {

    // TAG for logging
    private static final String TAG = CreateStory.class.getSimpleName();

    // RecyclerView to show video roll container
    private RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;

    private Button btnAddContainer;
    private Button btnDeleteContainer;
    private Button btnCreate;

    // Uri for data
    private Uri uri;

    // Progress Dialog to show when the ffmpeg command is running
    private ProgressDialog progressDialog;

    // holds the maximum number of commands that are goind to run
    int max_num_of_commands = 0;

    // holds the commands that are completed in a given moment
    int num_of_commands_completed = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);

        btnAddContainer = (Button) findViewById(R.id.btn_add_container);
        btnDeleteContainer = (Button) findViewById(R.id.btn_delete_container);
        btnCreate = (Button) findViewById(R.id.btn_create);

        // this is for populating recycler view
        Constants.imageUriList.add("");
        Constants.audioUriList.add("");
        recyclerViewAdapter= new RecyclerViewAdapter(this);

        /* Recycler view initialization */
        recyclerView = (RecyclerView) findViewById(R.id.rv_timeline);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);

        btnAddContainer.setOnClickListener(this);
        btnDeleteContainer.setOnClickListener(this);
        btnCreate.setOnClickListener(this);

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
        if(requestCode == Constants.READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(data != null) {
                uri = data.getData();

                /* Set the data returned from the FileManager intent to the position requested */
                Log.i(TAG, "Uri: " + uri.toString());
                Constants.imageUriList.set(Constants.update_position, uri.toString());
                recyclerViewAdapter.notifyDataSetChanged();
            }
        } else {
            Log.v(TAG,"requestCode: "+requestCode);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_container:
                Toast.makeText(this, "add", Toast.LENGTH_SHORT).show();
                Constants.imageUriList.add("");
                Constants.audioUriList.add("");
                recyclerViewAdapter.notifyDataSetChanged();
                break;

            case R.id.btn_delete_container:
                if(Constants.imageUriList.size() != 0) {
                    Toast.makeText(this, "delete", Toast.LENGTH_SHORT).show();
                    Constants.imageUriList.remove(Constants.imageUriList.size() - 1);
                    Constants.audioUriList.remove(Constants.audioUriList.size() - 1);
                    recyclerViewAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Timeline empty", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_create:
                Toast.makeText(this, "create story", Toast.LENGTH_SHORT).show();
                createStory();
                break;
        }
    }

    /**
     * This will merge image and audio and then merge all videos
     */
    private void createStory() {
        int numOfFrames = Constants.imageUriList.size();

        boolean runCommand = true;
        for(int i = 0; i < numOfFrames; i++) {
            if(Constants.imageUriList.get(i).equals("")) {
                runCommand = false;
            }
            if(Constants.audioUriList.get(i).equals("")) {
                runCommand = false;
            }
        }

        if(runCommand) {
            max_num_of_commands = (numOfFrames * 2) + 1;
            Log.d(TAG, "Max Commands to execute: " + max_num_of_commands);

            ArrayList<String> intermediateFiles = new ArrayList<>();

            for(int i = 0; i < numOfFrames; i++) {
                String intermediateFilePath = Constants.directoryPath
                        + File.separator
                        + "intermediate_"
                        + i + ".ts";
                intermediateFiles.add(intermediateFilePath);

                String[] merge_command = {"-y", "-loop", "1",
                        "-i", CommonUtils.getInstance().getPath(this, Uri.parse(Constants.imageUriList.get(i))),
                        "-i", Constants.audioUriList.get(i),
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
                    + "Story_"
                    + CommonUtils.getInstance().getTimeStamp()
                    + ".mp4";

            String[] join_command = {"-y", "-i", concat_command,
                        "-c", "copy", storyVideoName};

            Log.d(TAG, "join_command: " + Arrays.toString(join_command));

            execFFmpegBinary(join_command);
        } else {
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
                            progressDialog.dismiss();

                            /*// All the intermediate file must be deleted, for next round of cutting
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
                            }*/

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
