package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.activities.fragmentMyVideos.MyFileVideoTitleFragment;

public class MyVideosActivity extends AppCompatActivity implements MyFileVideoTitleFragment.OnFragmentInteractionListener {
    List<File> videos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_videos);
    }

    @Override
    public void onDelete(File video) {
        videos.remove(video);
        System.out.println(videos);
    }

    @Override
    public List<File> getVideos() {
        videos = new ArrayList<>();
        videos.add(new File("/peepee.mp4"));
        videos.add(new File("/poopoo.mp4"));
        return videos;
    }
}