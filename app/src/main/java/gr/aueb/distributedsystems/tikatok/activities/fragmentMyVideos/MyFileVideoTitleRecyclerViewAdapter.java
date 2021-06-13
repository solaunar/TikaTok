package gr.aueb.distributedsystems.tikatok.activities.fragmentMyVideos;

import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import gr.aueb.distributedsystems.tikatok.R;
import java.io.File;
import java.util.List;

public class MyFileVideoTitleRecyclerViewAdapter extends RecyclerView.Adapter<MyFileVideoTitleRecyclerViewAdapter.ViewHolder> {

    private final List<File> mValues;
    private MyFileVideoTitleFragment.OnFragmentInteractionListener listener;

    public MyFileVideoTitleRecyclerViewAdapter(List<File> items, MyFileVideoTitleFragment.OnFragmentInteractionListener listener) {
        mValues = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.fragment_my_file_video_title, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final File video = mValues.get(position);
        holder.mItem = video;
        String videoTitle = video.getPath();
        videoTitle = videoTitle.substring(videoTitle.lastIndexOf('\\') + 1, videoTitle.indexOf(".mp4"));
        holder.txtMyFileVideoTitle.setText(videoTitle);
        holder.imageButtonDeleteVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(video);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView txtMyFileVideoTitle;
        public final ImageButton imageButtonDeleteVideo;
        public File mItem;
        public final View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            txtMyFileVideoTitle = view.findViewById(R.id.txtFileVideoTitle);
            imageButtonDeleteVideo = view.findViewById(R.id.imageButtonDeleteVideo);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + txtMyFileVideoTitle.getText() + "'";
        }
    }
}