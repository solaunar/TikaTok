package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
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

import static java.lang.Thread.sleep;

public class SearchActivity extends AppCompatActivity implements StringTopicFragment.OnFragmentInteractionListener{
    public static final String THREAD_ID = "connection_thread_id";
    List <String> topics;
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
        SearchActivity.GetInfoTableTask getInfoTableTask = new SearchActivity.GetInfoTableTask();
        getInfoTableTask.execute("CONNECT_TO_BROKER");
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
                    SearchActivity.GetInfoTableTask getInfoTableTask = new SearchActivity.GetInfoTableTask();
                    getInfoTableTask.execute("CONNECT_TO_BROKER");
                    goToResult(user, searchTerm.getText().toString());
                }
            }
        });
    }

    private class GetInfoTableTask extends AsyncTask<String, String, AppNode> {

        @Override
        protected AppNode doInBackground(String... strings) {
            if(strings[0].equals("CONNECT_TO_BROKER"))
                return user.connectToBroker();
            return null;
        }

        @Override
        protected void onPostExecute(AppNode appNode) {
            super.onPostExecute(user);
            if(appNode != null)
                user = appNode;
        }
    }

    private class SubscribeTask extends AsyncTask<String, String, AppNode> {

        @Override
        protected AppNode doInBackground(String... strings) {
            return user.updateInfoTableOnSubscribe(strings[0]);
        }

        @Override
        protected void onPostExecute(AppNode appNode) {
            super.onPostExecute(user);
            if(appNode != null)
                user = appNode;
        }
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

    public void goToResult(AppNode appNode, String searchTerm){
        Intent resultActivityScreen = new Intent(getApplicationContext(), SearchResultsActivity.class);
        resultActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, appNode);
        resultActivityScreen.putExtra(SearchResultsActivity.SEARCH_TERM, searchTerm);
        startActivity(resultActivityScreen);
    }

    @Override
    public void onSubscribe(String topic) {
        SubscribeTask subscribeTask = new SubscribeTask();
        subscribeTask.execute(topic);
        if (!user.isSubscribed()){
            Thread updateSub = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true){
                            user.connectToBroker();
                            //get the topics in which there has been an update
                            ArrayList<String> topicsUpdated = user.updateOnSubscriptions();
                            if (!topicsUpdated.isEmpty()) {
                                //if there are indeed topics with updated content then for each one
                                //print the list of videos
                                HashMap<String, ArrayList<File>> updatedSubscriptions = user.getSubscribedTopics();
                                System.out.println("Saving the list of videos of topics you are subscribed to...");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showErrorMessage("Subscriptions", "New videos!");
                                    }
                                });
                            }

                            sleep(3000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            updateSub.start();
            user.setSubscribed(true);
        }
    }

    @Override
    public void onView(String topic) {
        goToResult(user, topic);
    }

    @Override
    public List<String> getTopics() {
        topics = new ArrayList<>();
        filterTopicsFromInfoTable();
        return topics;
    }

    public void filterTopicsFromInfoTable(){
        InfoTable infoTable = user.getInfoTable();
        if (infoTable==null) return;
        //System.out.println(infoTable);
        HashMap<String, ArrayList<File>> allVideosByTopic = infoTable.getAllVideosByTopic();
        ArrayList<String> availableTopics = infoTable.getAvailableTopics();
        ArrayList<File> userVideos = infoTable.getAllVideosByTopic().get(user.getChannel().getChannelName());
        for (String topic : availableTopics) {
            ArrayList<File> videosAssociated = allVideosByTopic.get(topic);
            if (videosAssociated == null) continue;
            for (File video : videosAssociated)
                if(userVideos== null)
                    topics.add(topic);
                else {
                    if (!userVideos.contains(video))
                        topics.add(topic);
                }
        }
    }
}