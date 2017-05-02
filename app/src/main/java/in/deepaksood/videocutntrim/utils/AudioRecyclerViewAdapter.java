package in.deepaksood.videocutntrim.utils;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import in.deepaksood.videocutntrim.R;
import in.deepaksood.videocutntrim.activities.CreateStory;

/**
 * Created by Deepak on 28-04-2017.
 */

public class AudioRecyclerViewAdapter extends RecyclerView.Adapter<AudioRecyclerViewAdapter.MyViewHolder> {

    private List<String> audioUriList;
    private CreateStory context;

    public AudioRecyclerViewAdapter(CreateStory context) {
        this.context = context;
        this.audioUriList = Constants.audioUriList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivFilmRoll;
        private ImageView ivContainer;

        MyViewHolder(View view) {
            super(view);
            ivFilmRoll = (ImageView) view.findViewById(R.id.iv_film_roll_audio);
            ivContainer = (ImageView) view.findViewById(R.id.iv_container_audio);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_item_audio, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.ivFilmRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // load audio by starting fileManager intent
                Toast.makeText(context, "Load Audio" + holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/mp3");
                Constants.update_position = holder.getAdapterPosition();
                context.startActivityForResult(intent, Constants.AUDIO_READ_REQUEST_CODE);
            }
        });

        if(!audioUriList.get(position).equals("")) {
            holder.ivContainer.setVisibility(View.VISIBLE);
        } else {
            holder.ivContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return audioUriList.size();
    }
}
