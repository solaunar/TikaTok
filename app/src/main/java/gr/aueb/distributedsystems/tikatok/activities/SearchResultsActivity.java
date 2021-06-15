package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.activities.fragmentMyVideos.MyFileVideoTitleRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos.FileVideoTitleFragment;
import gr.aueb.distributedsystems.tikatok.activities.fragmentOtherUserVideos.FileVideoTitleRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.activities.fragmentTopics.StringTopicRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.backend.AppNode;

public class SearchResultsActivity extends AppCompatActivity implements FileVideoTitleFragment.OnFragmentInteractionListener{
    List <File> results;
    RecyclerView resultFragment;

    /**Toolbar Buttons*/
    Button btnSubs;
    Button btnMyVids;
    Button btnUpload;
    ImageButton btnHome;
    ImageButton btnLogout;

    static final String APPNODE_USER = "appNode_user";
    AppNode user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        resultFragment = findViewById(R.id.fragmentSearchResults);
        FileVideoTitleRecyclerViewAdapter adapter = new FileVideoTitleRecyclerViewAdapter(getVideos(), this);
        resultFragment.setAdapter(adapter);

        Intent i = getIntent();
        user = (AppNode) i.getSerializableExtra(APPNODE_USER);
        System.out.println("SearchResultsActivity user: " + user.getChannel());

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

        btnHome = findViewById(R.id.btnLogin);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome(user);
            }
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogout(user);
            }
        });
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
        startActivity(videoStrActivityScreen);
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