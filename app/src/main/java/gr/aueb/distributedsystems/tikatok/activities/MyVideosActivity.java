package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.activities.fragmentMyVideos.MyFileVideoTitleFragment;
import gr.aueb.distributedsystems.tikatok.activities.fragmentMyVideos.MyFileVideoTitleRecyclerViewAdapter;

public class MyVideosActivity extends AppCompatActivity implements MyFileVideoTitleFragment.OnFragmentInteractionListener {
    List<File> videos;
    RecyclerView videoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_videos);

        videoFragment = findViewById(R.id.fragmentMyVideos);
        MyFileVideoTitleRecyclerViewAdapter adapter = new MyFileVideoTitleRecyclerViewAdapter(getVideos(), this);
        videoFragment.setAdapter(adapter);
    }

    @Override
    public void onDelete(File video) {
        videos.remove(video);
        if(videoFragment!=null)
            videoFragment.getAdapter().notifyDataSetChanged();
        System.out.println(videos);
    }

    @Override
    public List<File> getVideos() {
        videos = new ArrayList<>();
        videos.add(new File("\\peepee.mp4"));
        videos.add(new File("\\poopoo.mp4"));
        return videos;
    }
}