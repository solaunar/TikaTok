package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.activities.fragmentMyVideos.MyFileVideoTitleRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.activities.fragmentTopics.StringTopicFragment;
import gr.aueb.distributedsystems.tikatok.activities.fragmentTopics.StringTopicRecyclerViewAdapter;

public class SearchActivity extends AppCompatActivity implements StringTopicFragment.OnFragmentInteractionListener{
    List <String> topics;
    RecyclerView topicFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        topicFragment = findViewById(R.id.fragmentAvailable);
        StringTopicRecyclerViewAdapter adapter = new StringTopicRecyclerViewAdapter(getTopics(), this);
        topicFragment.setAdapter(adapter);
    }

    @Override
    public void onSubscribe(String topic) {

    }

    @Override
    public void onView(String topic) {

    }

    @Override
    public List<String> getTopics() {
        topics = new ArrayList<>();
        topics.add("peepee");
        topics.add("poopoo");
        return topics;
    }
}