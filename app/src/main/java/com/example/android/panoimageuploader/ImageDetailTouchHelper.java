package com.example.android.panoimageuploader;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.panoimageuploader.database.ImageDetails;

import java.util.List;

public class ImageDetailTouchHelper extends ItemTouchHelper.SimpleCallback {

    private ImageDetailTouchHelperListener listener;

    public interface ImageDetailTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }

    public ImageDetailTouchHelper(int dragDir, int swipeDir, ImageDetailTouchHelperListener listener) {
        super(dragDir, swipeDir);
        this.listener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View foregroundView = ((ImageDetailsAdapter.DetailsViewHolder) viewHolder).viewForeground;
            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        final View foregroundView = ((ImageDetailsAdapter.DetailsViewHolder) viewHolder).viewForeground;
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((ImageDetailsAdapter.DetailsViewHolder) viewHolder).viewForeground;
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((ImageDetailsAdapter.DetailsViewHolder) viewHolder).viewForeground;
        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction,viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

        ImageDetailsAdapter adapter = (ImageDetailsAdapter) recyclerView.getAdapter();
        List<ImageDetails> detailsList = adapter.getData();
        int position = viewHolder.getAdapterPosition();

        ImageDetails details;

        if (position != -1) {
            details = detailsList.get(position);

            if (details.isDeletable()) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

        }

        return 0;

    }
}
