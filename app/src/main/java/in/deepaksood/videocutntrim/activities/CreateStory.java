package in.deepaksood.videocutntrim.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import in.deepaksood.videocutntrim.R;
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

    // Uri for data
    private Uri uri;

    ArrayList<String> imageUriList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story);

        btnAddContainer = (Button) findViewById(R.id.btn_add_container);
        btnDeleteContainer = (Button) findViewById(R.id.btn_delete_container);

        // this is for populating recycler view
        imageUriList =new ArrayList<>();
        imageUriList.add("");
        recyclerViewAdapter= new RecyclerViewAdapter(this, imageUriList);

        /* Recycler view initialization */
        recyclerView = (RecyclerView) findViewById(R.id.rv_timeline);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);

        btnAddContainer.setOnClickListener(this);
        btnDeleteContainer.setOnClickListener(this);
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
                imageUriList.set(Constants.update_position, uri.toString());
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
                imageUriList.add("");
                recyclerViewAdapter.notifyDataSetChanged();
                break;

            case R.id.btn_delete_container:
                Toast.makeText(this, "delete", Toast.LENGTH_SHORT).show();
                imageUriList.remove(imageUriList.size()-1);
                recyclerViewAdapter.notifyDataSetChanged();
                break;
        }
    }
}
