package gr.aueb.distributedsystems.tikatok.activities;

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
    Button btnMyVids;
    Button btnUpload;
    ImageButton btnHome;
    ImageButton btnLogout;
    TextView textViewMyVideos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        user = (AppNode) i.getSerializableExtra(APPNODE_USER);
        System.out.println("MyVideosActivity user: " + user.getChannel());
        //setVideos();

        setContentView(R.layout.activity_my_videos);
        videoFragment = findViewById(R.id.fragmentMyVideos);
        MyFileVideoTitleRecyclerViewAdapter adapter = new MyFileVideoTitleRecyclerViewAdapter(getVideos(), this);
        videoFragment.setAdapter(adapter);
        textViewMyVideos = findViewById(R.id.textViewMyVideos);

        if(videos.isEmpty()) textViewMyVideos.setText("You have not uploaded any videos.");
        /** Toolbar Buttons */
        btnSubs = findViewById(R.id.btnSubsActionMyVideos);
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

        btnHome = findViewById(R.id.btnLoginMyVideos);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome(user);
            }
        });

        btnUpload = findViewById(R.id.btnUploadActionMyVideos);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUploads(user);
            }
        });

        btnLogout = findViewById(R.id.btnLogoutMyVideos);
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

    private void goToMyVids(AppNode user) {
        Intent myVidsActivityScreen = new Intent(getApplicationContext(), MyVideosActivity.class);
        myVidsActivityScreen.putExtra(SearchResultsActivity.APPNODE_USER, user);
        startActivity(myVidsActivityScreen);
    }

    @Override
    public void onDelete(File video) {
        user.deleteVideo(video);
        if(videoFragment!=null)
            videoFragment.getAdapter().notifyDataSetChanged();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                user.updateOnDelete(video);
            }
        });
        t.start();
//        UpdateInfoTableTask updateInfoTableTask = new UpdateInfoTableTask();
//        updateInfoTableTask.execute(video);
    }

    @Override
    public List<File> getVideos() {
        videos = new ArrayList<>();
        setVideos();
        System.out.println(videos);
        return videos;
    }

    private void setVideos(){
        videos = user.getChannel().getAllVideosPublished();
    }

    private class UpdateInfoTableTask extends AsyncTask<File, String, AppNode> {

        @Override
        protected AppNode doInBackground(File... videos) {
            return user.updateOnDelete(videos[0]);
        }

        @Override
        protected void onPostExecute(AppNode appNode) {
            super.onPostExecute(user);
            user = appNode;
        }
    }
}