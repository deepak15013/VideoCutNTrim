package in.deepaksood.videocutntrim.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import in.deepaksood.videocutntrim.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private static final String TAG = VideoPlayerActivity.class.getSimpleName();

    VideoView vvPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        vvPlayer = (VideoView) findViewById(R.id.vv_player);

        Intent intent = getIntent();
        String uriString = intent.getStringExtra(MainActivity.videoLocationUri);
        Log.v(TAG, "uriString: "+uriString);

        Uri uri = Uri.parse(uriString);

        MediaController mediaController= new MediaController(this);
        mediaController.setAnchorView(vvPlayer);
        vvPlayer.setMediaController(mediaController);
        vvPlayer.setVideoURI(uri);
        vvPlayer.requestFocus();
        vvPlayer.start();
    }
}
