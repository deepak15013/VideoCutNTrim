package in.deepaksood.videocutntrim.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import in.deepaksood.videocutntrim.R;
import in.deepaksood.videocutntrim.utils.Constants;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivRecordContainer;
    private Button btnRecord;
    private Button btnPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        ivRecordContainer = (ImageView) findViewById(R.id.iv_record_container);

        String uriImage = getIntent().getStringExtra(Constants.EXTRA_IMAGE_URI);
        if(!uriImage.equals("")) {
            Picasso.with(this)
                    .load(uriImage)
                    .resize(250, 250)
                    .centerCrop()
                    .into(ivRecordContainer);
        }

        btnRecord.setOnClickListener(this);
        btnPlay.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_record:
                Toast.makeText(this, "Record", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_play:
                Toast.makeText(this, "Play", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
