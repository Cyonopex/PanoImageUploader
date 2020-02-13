package com.example.android.panoimageuploader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.panoimageuploader.database.ImageDetails;

import java.util.List;

public class ImageDetailsAdapter extends RecyclerView.Adapter<ImageDetailsAdapter.DetailsViewHolder>{

    private static final String TAG = "ImageDetailsAdapter";
    private List<ImageDetails> data;

    private final ImageDetailsAdapterOnClickHandler mClickHandler;

    public interface ImageDetailsAdapterOnClickHandler {
        void onClick(String imageName);
    }

    public ImageDetailsAdapter(ImageDetailsAdapterOnClickHandler mClickHandler) {
        this.mClickHandler = mClickHandler;
    }

    @NonNull
    @Override
    public DetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int listItemLayoutId = R.layout.status_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(listItemLayoutId, parent, false);

        return new DetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsViewHolder holder, int position) {

        ImageDetails details = data.get(position);
        String fileName = details.getImageName();

        holder.mFileNameText.setText(fileName);

        int status = details.getStatus();

        switch(status) {
            case ImageDetails.UPLOADING:
                String upload = holder.itemView.getContext().getString(R.string.uploading);
                holder.mStatus.setText(upload);
                holder.pg.setVisibility(View.VISIBLE);
                break;
            case ImageDetails.PROCESSING:
                String proc = holder.itemView.getContext().getString(R.string.processing);
                holder.mStatus.setText(proc);
                holder.pg.setVisibility(View.VISIBLE);
                break;
            case ImageDetails.COMPLETED:
                String comp = holder.itemView.getContext().getString(R.string.complete);
                holder.mStatus.setText(comp);
                holder.pg.setVisibility(View.INVISIBLE);
                break;
        }

        Log.d(TAG, "file name is set at " + fileName);
    }

    @Override
    public int getItemCount() {
        if (data == null) return 0;
        return data.size();
    }

    public void setImageData(List<ImageDetails> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    class DetailsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mFileNameText;
        public final TextView mStatus;
        public final ProgressBar pg;

        public DetailsViewHolder(View itemView) {

            super(itemView);

            mFileNameText = (TextView) itemView.findViewById(R.id.fileNameTv);
            mStatus = (TextView) itemView.findViewById(R.id.mStatus);
            pg = (ProgressBar) itemView.findViewById(R.id.mProgbar);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String fileName = data.get(adapterPosition).getImageName();
            mClickHandler.onClick(fileName);
        }
    }
}
