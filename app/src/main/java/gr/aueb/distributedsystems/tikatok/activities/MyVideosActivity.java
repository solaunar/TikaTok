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
import gr.aueb.distributedsystems.tikatok.activities.fragmentMyVideos.MyFileVideoTitleFragment;
import gr.aueb.distributedsystems.tikatok.activities.fragmentMyVideos.MyFileVideoTitleRecyclerViewAdapter;
import gr.aueb.distributedsystems.tikatok.backend.AppNode;

public class MyVideosActivity extends AppCompatActivity implements MyFileVideoTitleFragment.OnFragmentInteractionListener {
    List<File> videos;
    RecyclerView videoFragment;
    static final String APPNODE_USER = "appNode_user";
    AppNode user;

    /**Toolbar Buttons*/
    Button btnSubs;
    Button btnUpload;
    ImageButton btnHome;
    ImageButton btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_videos);

        videoFragment = findViewById(R.id.fragmentMyVideos);
        MyFileVideoTitleRecyclerViewAdapter adapter = new MyFileVideoTitleRecyclerViewAdapter(getVideos(), this);
        videoFragment.setAdapter(adapter);

        Intent i = getIntent();
        user = (AppNode) i.getSerializableExtra(APPNODE_USER);
        System.out.println("MyVideosActivity user: " + user.getChannel());

        /** Toolbar Buttons */
        btnSubs = findViewById(R.id.btnSubsAction);
        btnSubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSubs(user);
            }
        });

        btnHome = findViewById(R.id.btnLogin);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome(user);
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

    private void goToSubs(AppNode user) {
        Intent SubsActivityScreen = new Intent(getApplicationContext(), SubscribedVideosActivity.class);
        SubsActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(SubsActivityScreen);
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