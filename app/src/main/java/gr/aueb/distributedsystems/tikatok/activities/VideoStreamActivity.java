package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.backend.AppNode;

public class VideoStreamActivity extends AppCompatActivity {

    /**Toolbar Buttons*/
    Button btnSubs;
    Button btnMyVids;
    Button btnUpload;
    ImageButton btnHome;
    ImageButton btnLogout;
    ImageButton btnPlayVideo;
    VideoView videoView;
    ProgressBar progressBar;

    static final String APPNODE_USER = "appNode_user";
    static final String VIDEO = "video";
    AppNode user;
    File video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        user = (AppNode) i.getSerializableExtra(APPNODE_USER);
        video = (File) i.getSerializableExtra(VIDEO);

        DownloadVideoTask downloadVideoTask = new DownloadVideoTask();
        downloadVideoTask.execute(video);
        System.out.println("VideoStreamActivity user: " + user.getChannel());

        setContentView(R.layout.activity_video_stream);

        videoView = findViewById(R.id.videoView);
        String videoChosen = video.getPath();
        videoChosen = videoChosen.substring (videoChosen.indexOf("$") + 1, videoChosen.lastIndexOf("$")) + "-" + videoChosen.substring(videoChosen.lastIndexOf("$")+1);
        String videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        videoPath = videoPath + videoChosen.toLowerCase() + ".mp4";
        while(true){
            if (new File(videoPath).exists()) break;
        }
        progressBar = findViewById(R.id.progressBarVideoStream);
        videoView.setVideoPath(videoPath);
        btnPlayVideo = findViewById(R.id.imageButtonPlayVideo);
        btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnPlayVideo.getTag().equals("play")) {
                    btnPlayVideo.setImageResource(R.drawable.ic_media_pause);
                    btnPlayVideo.setTag("pause");
                    videoView.start();
                } else if (btnPlayVideo.getTag().equals("pause") && videoView.isPlaying()) {
                    btnPlayVideo.setImageResource(R.drawable.ic_media_play);
                    btnPlayVideo.setTag("play");
                    videoView.pause();
                } else if (btnPlayVideo.getTag().equals("pause") && !videoView.isPlaying()) {
                    btnPlayVideo.setImageResource(R.drawable.ic_media_play);
                    btnPlayVideo.setTag("replay");
                } else if (btnPlayVideo.getTag().equals("replay")){
                    btnPlayVideo.setImageResource(R.drawable.ic_media_pause);
                    btnPlayVideo.setTag("pause");
                    videoView.start();
                }
            }
        });

        /** Toolbar Buttons */
        btnSubs = findViewById(R.id.btnSubsActionVideoStream);
        btnSubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSubs(user);
            }
        });

        btnMyVids = findViewById(R.id.btnMyVideosActionVideoStream);
        btnMyVids.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMyVids(user);
            }
        });

        btnUpload = findViewById(R.id.btnUploadActionVideoStream);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToUploads(user);
            }
        });

        btnHome = findViewById(R.id.btnLoginVideoStream);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome(user);
            }
        });

        btnLogout = findViewById(R.id.btnLogoutVideoStream);
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

    @Override
    protected void onResume() {
        super.onResume();
        while (videoView.isPlaying()){
            int progress = videoView.getCurrentPosition()/videoView.getDuration();
            progressBar.setProgress(progress, true);
        }
    }
}