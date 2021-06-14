package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.activities.fragmentTopics.StringTopicFragment;
import gr.aueb.distributedsystems.tikatok.activities.fragmentTopics.StringTopicRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.backend.AppNode;

public class SearchActivity extends AppCompatActivity implements StringTopicFragment.OnFragmentInteractionListener{
    List <String> topics;
    RecyclerView topicFragment;
    static final String APPNODE_USER = "appNode_user";
    AppNode user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        topicFragment = findViewById(R.id.fragmentAvailable);
        StringTopicRecyclerViewAdapter adapter = new StringTopicRecyclerViewAdapter(getTopics(), this);
        topicFragment.setAdapter(adapter);

        Intent i = getIntent();
        user = (AppNode) i.getSerializableExtra(APPNODE_USER);
        System.out.println(user.getChannel());
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