package in.deepaksood.videocutntrim.utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import in.deepaksood.videocutntrim.R;
import in.deepaksood.videocutntrim.activities.CreateStory;

/**
 * Created by Deepak on 22-04-2017.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private List<String> arrayList;
    private CreateStory context;

    public RecyclerViewAdapter(List<String> arrayList, CreateStory context) {
        this.arrayList = arrayList;
        this.context = context;
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
                Toast.makeText(context, "Clicked " + holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
