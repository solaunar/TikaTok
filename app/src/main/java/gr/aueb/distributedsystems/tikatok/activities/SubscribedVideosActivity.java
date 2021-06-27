package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos.FileVideoTitleFragment;
import gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos.FileVideoTitleRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.backend.AppNode;

public class SubscribedVideosActivity extends AppCompatActivity implements FileVideoTitleFragment.OnFragmentInteractionListener {
    List <File> subbed_videos;
    RecyclerView subbedFragment;
    static final String APPNODE_USER = "appNode_user";
    AppNode user;

    /**Toolbar Buttons*/
    Button btnSubs;
    Button btnMyVids;
    Button btnUpload;
    ImageButton btnHome;
    ImageButton btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        user = (AppNode) i.getSerializableExtra(APPNODE_USER);
        user.updateOnSubscriptions();
        System.out.println("SubscribedVideosActivity user: " + user.getChannel());

        setContentView(R.layout.activity_subscribed_videos);

        subbedFragment = findViewById(R.id.fragmentSubscribedVideos);
        FileVideoTitleRecyclerViewAdapter adapter = new FileVideoTitleRecyclerViewAdapter(getVideos(), this);
        subbedFragment.setAdapter(adapter);
        /** Toolbar Buttons */

        btnSubs = findViewById(R.id.btnSubsActionSearchResults);
        btnSubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSubs(user);
            }
        });

        btnMyVids = findViewById(R.id.btnSubsActionSubscribedVideos);
        btnMyVids.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMyVids(user);
            }
        });

        btnUpload = findViewById(R.id.btnUploadActionSubscribedVideos);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUploads(user);
            }
        });

        btnHome = findViewById(R.id.btnLoginSubscribedVideos);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome(user);
            }
        });

        btnLogout = findViewById(R.id.btnLogoutSubscribedVideos);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogout(user);
            }
        });


    }

    private void goToSubs(AppNode user) {
        Intent SubsActivityScreen = new Intent(getApplicationContext(), SubscribedVideosActivity.class);
        SubsActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(SubsActivityScreen);
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

    @Override
    public void onPlay(File video) {
        Intent videoStrActivityScreen = new Intent(getApplicationContext(), VideoStreamActivity.class);
        videoStrActivityScreen.putExtra(VideoStreamActivity.APPNODE_USER, user);
        videoStrActivityScreen.putExtra(VideoStreamActivity.VIDEO, video);
        startActivity(videoStrActivityScreen);
    }

    @Override
    public void onDownload(File video) {
        DownloadVideoTask downloadVideoTask = new DownloadVideoTask();
        downloadVideoTask.execute(video);
    }

    @Override
    public List<File> getVideos() {
        subbed_videos = new ArrayList<>();
        HashMap<String, ArrayList<File>> subscribedTopics = user.getSubscribedTopics();
        Set<String> topics = subscribedTopics.keySet();
        for (String topic : topics)
            subbed_videos.addAll(subscribedTopics.get(topic));
        return subbed_videos;
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
}