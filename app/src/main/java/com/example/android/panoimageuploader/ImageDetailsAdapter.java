package com.example.android.panoimageuploader;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.panoimageuploader.database.ImageDetails;
import com.example.android.panoimageuploader.util.DataUtils;

import java.util.List;

public class ImageDetailsAdapter extends RecyclerView.Adapter<ImageDetailsAdapter.DetailsViewHolder>{

    private static final String TAG = ImageDetailsAdapter.class.getSimpleName();
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

        if (details.getStatus() == ImageDetails.UPLOADING) {

            holder.viewBackground.setBackgroundColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.bg_cancel_background));

            holder.mDelete.setText(holder.itemView.getContext().getString(R.string.cancel));

            holder.deleteIcon.setVisibility(View.INVISIBLE);
            holder.cancelIcon.setVisibility(View.VISIBLE);

        } else {

            holder.viewBackground.setBackgroundColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.bg_delete_background));

            holder.mDelete.setText(holder.itemView.getContext().getString(R.string.delete));

            holder.deleteIcon.setVisibility(View.VISIBLE);
            holder.cancelIcon.setVisibility(View.INVISIBLE);

        }


        int status = details.getStatus();

        switch(status) {
            case ImageDetails.UPLOADING:
                String upload = holder.itemView.getContext().getString(R.string.uploading);
                holder.mStatus.setText(upload);
                holder.pg.setVisibility(View.VISIBLE);
                holder.headerText.setText(upload);
                holder.statusColor.setBackgroundColor(holder.itemView.getContext()
                        .getResources().getColor(R.color.processing_color));
                break;
            case ImageDetails.PROCESSING:
                String proc = holder.itemView.getContext().getString(R.string.processing);
                holder.mStatus.setText(proc);
                holder.pg.setVisibility(View.VISIBLE);
                holder.headerText.setText(proc);
                holder.statusColor.setBackgroundColor(holder.itemView.getContext()
                        .getResources().getColor(R.color.processing_color));
                break;
            case ImageDetails.COMPLETED:
                String comp = holder.itemView.getContext().getString(R.string.complete);
                holder.mStatus.setText(comp);
                holder.pg.setVisibility(View.INVISIBLE);
                holder.headerText.setText(comp);
                holder.statusColor.setBackgroundColor(holder.itemView.getContext()
                        .getResources().getColor(R.color.completed_color));
                break;
            case ImageDetails.UPLOAD_FAILED:
                String fail = holder.itemView.getContext().getString(R.string.upload_failed);
                holder.mStatus.setText(fail);
                holder.pg.setVisibility(View.INVISIBLE);
                String failed = holder.itemView.getContext().getString(R.string.failed_header);
                holder.headerText.setText(failed);
                holder.statusColor.setBackgroundColor(holder.itemView.getContext()
                        .getResources().getColor(R.color.failed_color));
                break;
            case ImageDetails.MISSING:
                String missing = holder.itemView.getContext().getString(R.string.missing);
                holder.mStatus.setText(missing);
                holder.pg.setVisibility(View.INVISIBLE);
                holder.mStatus.setTextColor(Color.GRAY);
                String completed = holder.itemView.getContext().getString(R.string.complete);
                holder.headerText.setText(completed);
                holder.statusColor.setBackgroundColor(holder.itemView.getContext()
                        .getResources().getColor(R.color.failed_color));
                break;
            case ImageDetails.PROCESSING_FAILED:
                String pfail = holder.itemView.getContext().getString(R.string.processing_failed);
                holder.mStatus.setText(pfail);
                holder.pg.setVisibility(View.INVISIBLE);
                String failed2 = holder.itemView.getContext().getString(R.string.failed_header);
                holder.headerText.setText(failed2);
                holder.statusColor.setBackgroundColor(holder.itemView.getContext()
                        .getResources().getColor(R.color.failed_color));
                break;
        }

        byte[] thumbnailBytes = details.getThumbnail();

        if (thumbnailBytes != null) {
            Bitmap thumbnail = DataUtils.getBitmapFromBytes(details.getThumbnail());
            holder.img.setImageBitmap(thumbnail);
        } else {
            Log.e(TAG, "Thumbnail not found");
        }


        if (position > 0 && shareStatusHeader(data.get(position-1).getStatus(), status) ){
            holder.header.setVisibility(View.GONE);
        } else {
            holder.header.setVisibility(View.VISIBLE);
        }

        if (position < data.size()-1 && shareStatusHeader(data.get(position+1).getStatus(), status) ){
            holder.divider.setVisibility(View.VISIBLE);
        } else {
            holder.divider.setVisibility(View.GONE);
        }
    }

    private boolean shareStatusHeader(int status1, int status2) {

        if (status1 == ImageDetails.UPLOAD_FAILED || status1 == ImageDetails.PROCESSING_FAILED) {
            if (status2 == ImageDetails.UPLOAD_FAILED || status2 == ImageDetails.PROCESSING_FAILED) {
                return true;
            }
        }

        if (status1 == ImageDetails.COMPLETED || status1 == ImageDetails.MISSING) {
            if (status2 == ImageDetails.COMPLETED || status2 == ImageDetails.MISSING) {
                return true;
            }
        }

        return status1 == status2;
    }

    @Override
    public int getItemCount() {
        if (data == null) return 0;
        return data.size();
    }

    public List<ImageDetails> getData() {
        return data;
    }

    public void setImageData(List<ImageDetails> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    class DetailsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mFileNameText;
        public final TextView mStatus;
        public final ProgressBar pg;
        public final ImageView img;
        public final ConstraintLayout viewBackground;
        public final ConstraintLayout viewForeground;
        public final TextView mDelete;
        public final ImageView deleteIcon;
        public final ImageView cancelIcon;
        public final View header;
        public final TextView headerText;
        public final View divider;
        public final View statusColor;

        public DetailsViewHolder(View itemView) {

            super(itemView);

            mFileNameText = itemView.findViewById(R.id.fileNameTv);
            mStatus = itemView.findViewById(R.id.mStatus);
            pg = itemView.findViewById(R.id.mProgbar);
            img = itemView.findViewById(R.id.mThumbnail);
            viewBackground = itemView.findViewById(R.id.viewBackground);
            viewForeground = itemView.findViewById(R.id.viewForeground);
            mDelete = itemView.findViewById(R.id.delete_text);
            deleteIcon = itemView.findViewById(R.id.trash_can);
            cancelIcon = itemView.findViewById(R.id.cancel);
            header = itemView.findViewById(R.id.detail_header);
            headerText = itemView.findViewById(R.id.header_text);
            divider = itemView.findViewById(R.id.divider);
            statusColor = itemView.findViewById(R.id.status_color);

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
