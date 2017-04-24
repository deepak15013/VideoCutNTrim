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
 * Created by Deepak on 22-04-2017.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private List<String> imageUriList;
    private CreateStory context;

    public RecyclerViewAdapter(CreateStory context, List<String> imageUriList) {
        this.context = context;
        this.imageUriList = imageUriList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivFilmRoll;
        private ImageView ivContainer;

        public MyViewHolder(View view) {
            super(view);
            ivFilmRoll = (ImageView) view.findViewById(R.id.iv_film_roll);
            ivContainer = (ImageView) view.findViewById(R.id.iv_container);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_item_view, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.ivFilmRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(imageUriList.get(position).equals("")) {
                    Toast.makeText(context, "Load Image" + holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    Constants.update_position = holder.getAdapterPosition();
                    context.startActivityForResult(intent, Constants.READ_REQUEST_CODE);
                } else {

                }
            }
        });

        if(!imageUriList.get(position).equals("")) {
            holder.ivContainer.setVisibility(View.VISIBLE);
            Picasso.with(context)
                    .load(imageUriList.get(position))
                    .resize(148, 98)
                    .centerCrop()
                    .into(holder.ivContainer);
        } else {
            holder.ivContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return imageUriList.size();
    }
}
