package gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import gr.aueb.distributedsystems.tikatok.R;

import java.io.File;
import java.util.List;

public class FileVideoTitleRecyclerViewAdapter extends RecyclerView.Adapter<FileVideoTitleRecyclerViewAdapter.ViewHolder> {

    private final List<File> mValues;
    private FileVideoTitleFragment.OnFragmentInteractionListener listener;
    public FileVideoTitleRecyclerViewAdapter(List<File> items, FileVideoTitleFragment.OnFragmentInteractionListener listener) {
        mValues = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.fragment_file_video_title, parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final File video = mValues.get(position);
        holder.mItem = video;
        String videoTitle = video.getPath();
        videoTitle = videoTitle.substring(videoTitle.lastIndexOf('\\') + 1, videoTitle.indexOf(".mp4"));
        holder.txtFileVideoTitle.setText(videoTitle);
        holder.imageButtonPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlay(video);
            }
        });
        holder.imageButtonDownloadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDownload(video);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView txtFileVideoTitle;
        public final ImageButton imageButtonPlayVideo;
        public final ImageButton imageButtonDownloadVideo;
        public File mItem;
        public final View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            txtFileVideoTitle = view.findViewById(R.id.txtFileVideoTitle);
            imageButtonPlayVideo = view.findViewById(R.id.imageButtonPlayVideo);
            imageButtonDownloadVideo = view.findViewById(R.id.imageButtonDownloadVideo);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + txtFileVideoTitle.getText() + "'";
        }
    }
}