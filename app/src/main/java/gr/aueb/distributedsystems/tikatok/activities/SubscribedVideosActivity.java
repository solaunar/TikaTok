package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos.FileVideoTitleFragment;
import gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos.FileVideoTitleRecyclerViewAdapter;

public class SubscribedVideosActivity extends AppCompatActivity implements FileVideoTitleFragment.OnFragmentInteractionListener {
    List <File> subbed_videos;
    RecyclerView subbedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribed_videos);

        subbedFragment = findViewById(R.id.fragmentSubscribedVideos);
        FileVideoTitleRecyclerViewAdapter adapter = new FileVideoTitleRecyclerViewAdapter(getVideos(), this);
        subbedFragment.setAdapter(adapter);
    }

    @Override
    public void onPlay(File video) {

    }

    @Override
    public void onDownload(File video) {

    }

    @Override
    public List<File> getVideos() {
        subbed_videos = new ArrayList<>();
        subbed_videos.add(new File("\\peepee.mp4"));
        subbed_videos.add(new File("\\poopoo.mp4"));
        return subbed_videos;
    }
}