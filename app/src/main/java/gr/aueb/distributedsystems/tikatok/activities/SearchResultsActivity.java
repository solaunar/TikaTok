package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.activities.fragmentMyVideos.MyFileVideoTitleRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos.FileVideoTitleFragment;
import gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos.FileVideoTitleRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.activities.fragmentTopics.StringTopicRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.backend.Address;
import gr.aueb.distributedsystems.tikatok.backend.AppNode;
import gr.aueb.distributedsystems.tikatok.backend.InfoTable;

public class SearchResultsActivity extends AppCompatActivity implements FileVideoTitleFragment.OnFragmentInteractionListener{
    public static final String SEARCH_TERM = "search_term";
    List <File> results;
    RecyclerView resultFragment;

    /**Toolbar Buttons*/
    Button btnSubs;
    Button btnMyVids;
    Button btnUpload;
    ImageButton btnHome;
    ImageButton btnLogout;
    TextView txtResultsForMsg;
    AppNode appNode;

    static final String APPNODE_USER = "appNode_user";
    AppNode user;
    String searchTerm;
    Address resultsExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        System.out.println(ip);
        Address address = new Address(ip, 12000);
        appNode = new AppNode(address);

        Intent i = getIntent();
        user = (AppNode) i.getSerializableExtra(APPNODE_USER);
        searchTerm = i.getStringExtra(SEARCH_TERM);
        System.out.println("SearchResultsActivity user: " + user.getChannel());

        setContentView(R.layout.activity_search_results);

        resultFragment = findViewById(R.id.fragmentSearchResults);
        FileVideoTitleRecyclerViewAdapter adapter = new FileVideoTitleRecyclerViewAdapter(getVideos(), this);
        resultFragment.setAdapter(adapter);
        txtResultsForMsg = findViewById(R.id.txtResultsForMsg);

        if (resultsExist==null || results.isEmpty()) txtResultsForMsg.setText("No results found for topic: " + searchTerm);
        /** Toolbar Buttons */
        btnSubs = findViewById(R.id.btnSubsActionSearchResults);
        btnSubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSubs(user);
            }
        });

        btnMyVids = findViewById(R.id.btnMyVideosActionSearchResults);
        btnMyVids.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMyVids(user);
            }
        });

        btnUpload = findViewById(R.id.btnUploadActionSearchResults);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUploads(user);
            }
        });

        btnHome = findViewById(R.id.btnLoginSearchResults);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome(user);
            }
        });

        btnLogout = findViewById(R.id.btnLogoutSearchResults);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogout(user);
            }
        });
    }

    private class DownloadVideoTask extends AsyncTask<File, String, AppNode> {

        @Override
        protected AppNode doInBackground(File... videos) {
            try {
                user.downloadVideo(videos[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void goToLogout(AppNode user) {
        Intent logoutActivityScreen = new Intent(getApplicationContext(), MainActivity.class);
        logoutActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(logoutActivityScreen);
    }

    private void goToHome(AppNode user) {
        Intent homeActivityScreen = new Intent(getApplicationContext(), SearchActivity.class);
        homeActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(homeActivityScreen);
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

    private void goToUploads(AppNode user) {
        Intent uploadsActivityScreen = new Intent(getApplicationContext(), UploadVideoActivity.class);
        uploadsActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(uploadsActivityScreen);
    }

    @Override
    public void onPlay(File video) {
        Intent videoStrActivityScreen = new Intent(getApplicationContext(), VideoStreamActivity.class);
        videoStrActivityScreen.putExtra(VideoStreamActivity.APPNODE_USER, user);
        videoStrActivityScreen.putExtra(VideoStreamActivity.VIDEO, video);
        startActivity(videoStrActivityScreen);
    }

    @Override
    public void onDownload(File video) {
        SearchResultsActivity.DownloadVideoTask downloadVideoTask = new SearchResultsActivity.DownloadVideoTask();
        downloadVideoTask.execute(video);
    }

    @Override
    public List<File> getVideos() {
        results = new ArrayList<>();
        resultsExist = filterVideosFromInfoTable(searchTerm);
        return results;
    }

    public Address filterVideosFromInfoTable(String searchTerm){
        InfoTable infoTable = user.getInfoTable();
        //System.out.println(infoTable);
        HashMap<String, ArrayList<File>> allVideosByTopic = infoTable.getAllVideosByTopic();
        ArrayList<String> availableTopics = infoTable.getAvailableTopics();
        if (!availableTopics.contains(searchTerm)) return user.find(searchTerm);
        ArrayList<File> userVideos = infoTable.getAllVideosByTopic().get(user.getChannel().getChannelName());
        ArrayList<File> videosAssociated = allVideosByTopic.get(searchTerm);
        if (videosAssociated==null) return null;
        for (File video : videosAssociated)
            if(userVideos== null)
                results.add(video);
            else {
                if (!userVideos.contains(video))
                    results.add(video);
            }

        return user.find(searchTerm);
    }
}