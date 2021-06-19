package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.activities.fragmentTopics.StringTopicFragment;
import gr.aueb.distributedsystems.tikatok.activities.fragmentTopics.StringTopicRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.backend.AppNode;
import gr.aueb.distributedsystems.tikatok.backend.Channel;
import gr.aueb.distributedsystems.tikatok.backend.InfoTable;

public class SearchActivity extends AppCompatActivity implements StringTopicFragment.OnFragmentInteractionListener{
    public static final String THREAD_ID = "connection_thread_id";
    List <String> topics = new ArrayList<>();
    RecyclerView topicFragment;
    TextView searchTerm;
    Button btnSearch;

    /**Toolbar Buttons*/
    Button btnSubs;
    Button btnMyVids;
    Button btnUpload;
    ImageButton btnLogout;

    static final String APPNODE_USER = "appNode_user";
    AppNode user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent i = getIntent();
        user = (AppNode) i.getSerializableExtra(APPNODE_USER);
//        long tID = i.getLongExtra(THREAD_ID, 0);
//        //Give you set of Threads
//        Set<Thread> setOfThread = Thread.getAllStackTraces().keySet();
//
//        //Iterate over set to find yours
//        for(Thread thread : setOfThread){
//            if(thread.getId()==tID){
//                thread.interrupt();
//            }
//        }
        System.out.println("SearchActivity user: " + user.getChannel());
        setContentView(R.layout.activity_search);

        topicFragment = findViewById(R.id.fragmentAvailable);
        StringTopicRecyclerViewAdapter adapter = new StringTopicRecyclerViewAdapter(getTopics(), this);
        topicFragment.setAdapter(adapter);


        /** Toolbar Buttons */
        btnSubs = findViewById(R.id.btnSubsAction);
        btnSubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSubs(user);
            }
        });

        btnMyVids = findViewById(R.id.btnMyVideosAction);
        btnMyVids.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMyVids(user);
            }
        });

        btnUpload = findViewById(R.id.btnUploadAction);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUploads(user);
            }
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogout(user);
            }
        });

        searchTerm = findViewById(R.id.editTxtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchTerm.getText().toString().isEmpty())
                    showErrorMessage("Warning!", "Search field must not be empty!");
                else{
                    goToResult(user);
                }
            }
        });
    }

    private void goToLogout(AppNode user) {
        Intent logoutActivityScreen = new Intent(getApplicationContext(), MainActivity.class);
        logoutActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(logoutActivityScreen);
    }

    private void goToUploads(AppNode user) {
        Intent uploadsActivityScreen = new Intent(getApplicationContext(), UploadVideoActivity.class);
        uploadsActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(uploadsActivityScreen);
    }

    private void goToMyVids(AppNode user) {
        Intent myVidsActivityScreen = new Intent(getApplicationContext(), MyVideosActivity.class);
        myVidsActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(myVidsActivityScreen);
    }

    private void goToSubs(AppNode user) {
        Intent SubsActivityScreen = new Intent(getApplicationContext(), SubscribedVideosActivity.class);
        SubsActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(SubsActivityScreen);
    }

    public void showErrorMessage(String title, String msg) {
        new AlertDialog.Builder(SearchActivity.this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, null).create().show();
    }

    public void goToResult(AppNode appNode){
        Intent resultActivityScreen = new Intent(getApplicationContext(), SearchResultsActivity.class);
        resultActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, appNode);
        startActivity(resultActivityScreen);
    }

    @Override
    public void onSubscribe(String topic) {

    }

    @Override
    public void onView(String topic) {
        Intent resultActivityScreen = new Intent(getApplicationContext(), SearchResultsActivity.class);
        resultActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(resultActivityScreen);
    }

    @Override
    public List<String> getTopics() {
        filterTopicsFromInfoTable();
        return topics;
    }

    public void filterTopicsFromInfoTable(){
        InfoTable infoTable = user.getInfoTable();
        //System.out.println(infoTable);
        HashMap<String, ArrayList<File>> allVideosByTopic = infoTable.getAllVideosByTopic();
        ArrayList<String> availableTopics = infoTable.getAvailableTopics();
        ArrayList<File> userVideos = infoTable.getAllVideosByTopic().get(user.getChannel().getChannelName());
        for (String topic : availableTopics) {
            ArrayList<File> videosAssociated = allVideosByTopic.get(topic);
            for (File video : videosAssociated)
                if (!userVideos.contains(video))
                    topics.add(topic);
        }
    }
}