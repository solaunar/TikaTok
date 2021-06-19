package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.backend.AppNode;
import gr.aueb.distributedsystems.tikatok.backend.InfoTable;

public class UploadVideoActivity extends AppCompatActivity {

    TextView filePath;
    EditText hashtags;
    EditText title;
    Button btnUpload;

    /**Toolbar Buttons*/
    Button btnSubs;
    Button btnMyVids;
    Button btnRecord;
    ImageButton btnHome;
    ImageButton btnLogout;

    static final String APPNODE_USER = "appNode_user";
    AppNode user;

    private static int VIDEO_REQUEST = 101;
    private Uri videoUri;
    private String videoTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        Intent i = getIntent();
        user = (AppNode) i.getSerializableExtra(APPNODE_USER);
        System.out.println("UploadVideoActivity user: " + user.getChannel());

        filePath = findViewById(R.id.txtSelectedFile);
        hashtags = findViewById(R.id.editTextHashtags);
        title = findViewById(R.id.editTextTitle);
        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (videoUri == null){
                   showErrorMessage("Warning!", "You must choose a video first!");
               }
               else if (!isValidTitle()){
                   showErrorMessage("Warning!", "You must choose a video title containing only English characters!");
               }
               else{
                   Log.i("VIDEO_TITLE", "Video Title set as: " + videoTitle);
                   ArrayList<String> hashtagsList = getHashtags();
                   String formattedDirectory = videoUri.getPath() + "$" + videoTitle + "$" + user.getChannel().getChannelName();
                   Log.i("VIDEO_PATH", "Video path set as: " + formattedDirectory);
                   user.uploadVideo(formattedDirectory, hashtagsList);
                   if(!user.isPublisher()){
                       user.setPublisher(true);
                       Thread t = new Thread(new Runnable() {
                           @Override
                           public void run() {
                               user.connectToBroker();
                           }
                       });
                       t.start();
                       openAppNodeServer();
                   }
                   System.out.println(hashtagsList);
               }
            }
        });

        /** Record Button */
        btnRecord = findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent, VIDEO_REQUEST);
            }
        });

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

    private boolean isValidTitle() {
        videoTitle = title.getText().toString();
        if (videoTitle.isEmpty()) return false;
        Pattern pt = Pattern.compile("^[a-zA-Z]+$");
        Matcher test = pt.matcher(videoTitle); //CAST TO STRING
        return test.matches();
    }

    private ArrayList<String> getHashtags() {
        String hashtagsRaw = hashtags.getText().toString();
        if (hashtagsRaw.isEmpty()) return null;
        ArrayList<String> hashtagsList = new ArrayList<>(Arrays.asList(hashtagsRaw.toLowerCase().replace(" ", "").split(",")));
        return hashtagsList;
    }

    public void showErrorMessage(String title, String msg) {
        new AlertDialog.Builder(UploadVideoActivity.this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, null).create().show();
    }

    public static String getFilePathFromContentUri(Uri contentUri, ContentResolver contentResolver) {
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(contentUri, filePathColumn, null, null, null);

        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /** Video capture */
    public void captureVideo (View view){
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult (videoIntent, VIDEO_REQUEST);
    }

    /** File Chooser */
    public void openFileChooser(View view){
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        startActivityForResult(fileIntent, VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == VIDEO_REQUEST) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            VideoView videoView = new VideoView(this);
            data.getDataString();
            videoUri = data.getData();
            Log.i("VIDEO_TB_UPLOADED", "Video available at " + videoUri.toString());
            //Log.i("VIDEO_TB_UPLOADED", "Video available at path " + new File(videoUri.getPath()));

            videoView.setVideoURI(data.getData());
            videoView.start();
            builder.setView(videoView).show();
            filePath.setText("Selected video: " + getFileName(videoUri));
        }
    }


    /** Toolbar */
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

    private void openAppNodeServer(){
        Thread appNodeServer = new Thread(new Runnable() {
            @Override
            public void run() {
                user.openAppNodeServer();
            }
        });
        appNodeServer.start();
    }
}