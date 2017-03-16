package in.deepaksood.videocutntrim;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import in.deepaksood.videocutntrim.utils.CommonUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int READ_REQUEST_CODE = 42;

    Button btnBrowse;
    Button btnNext;
    TextView tvBrowse;

    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvBrowse = (TextView) findViewById(R.id.tv_browse);
        btnNext = (Button) findViewById(R.id.btn_next);
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

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(uri != null && !uri.toString().contentEquals("")) {
                    Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                    intent.putExtra(CommonUtils.videoLocationUri, uri.toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "please select a video first", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.v(TAG,"data: "+data);
            if(data != null) {
                uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                tvBrowse.setText(uri.toString());
            }
        } else {
            Log.v(TAG,"requestCode: "+requestCode);
        }
    }
}
