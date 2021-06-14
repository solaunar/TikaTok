package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.activities.fragmentMyVideos.MyFileVideoTitleRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos.FileVideoTitleFragment;
import gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos.FileVideoTitleRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.activities.fragmentTopics.StringTopicRecyclerViewAdapter;

public class SearchResultsActivity extends AppCompatActivity implements FileVideoTitleFragment.OnFragmentInteractionListener{
    List <File> results;
    RecyclerView resultFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        resultFragment = findViewById(R.id.fragmentSearchResults);
        FileVideoTitleRecyclerViewAdapter adapter = new FileVideoTitleRecyclerViewAdapter(getVideos(), this);
        resultFragment.setAdapter(adapter);
    }

    @Override
    public void onPlay(File video) {

    }

    @Override
    public void onDownload(File video) {

    }

    @Override
    public List<File> getVideos() {
        results = new ArrayList<>();
        results.add(new File("\\peepee.mp4"));
        results.add(new File("\\poopoo.mp4"));
        return results;
    }
}