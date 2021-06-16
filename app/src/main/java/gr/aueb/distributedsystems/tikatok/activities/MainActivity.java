package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.aueb.distributedsystems.tikatok.R;
import gr.aueb.distributedsystems.tikatok.backend.Address;
import gr.aueb.distributedsystems.tikatok.backend.AppNode;
import gr.aueb.distributedsystems.tikatok.backend.Channel;
import gr.aueb.distributedsystems.tikatok.backend.InfoTable;
import gr.aueb.distributedsystems.tikatok.backend.Node;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static int CAMERA_PERMISSION = 100;
    private static int STORAGE_PERMISSION = 101;
    TextView usernameTextView;
    Button loginBtn;
    AppNode appNode;
    ObjectOutputStream out;
    ObjectInputStream in;
    Socket appNodeRequestSocket;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**Permissions for App*/
        getCameraPermission();
        getStoragePermission();

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        System.out.println(ip);
        Address address = new Address(ip, 12000);
        appNode = new AppNode(address);
        usernameTextView = findViewById(R.id.editTxtUsername);
        loginBtn = findViewById(R.id.btnSubmitLogin);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidUsername())
                    showErrorMessage("Warning!", "You cannot use the app without a username! Make sure it only contains English characters!");
                else{
                    Log.i("USERNAME", "Username set as: " + username);
                    appNode.setChannel(new Channel(username));
                    System.out.println(appNode.getChannel());
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            connectToBroker();
                        }
                    });
                    t.start();
                    goToSearch(appNode);
                }
            }
        });
    }

    private boolean isValidUsername() {
        username = getUsername();
        if (username.isEmpty()) return false;
        Pattern pt = Pattern.compile("^[a-zA-Z]+$");
        Matcher test = pt.matcher(username); //CAST TO STRING
        return test.matches();
    }

    public void goToSearch(AppNode appNode){
        Intent searchActivityScreen = new Intent(getApplicationContext(), SearchActivity.class);
        searchActivityScreen.putExtra(SearchActivity.APPNODE_USER, appNode);
        startActivity(searchActivityScreen);
    }

    public String getUsername(){
        return usernameTextView.getText().toString();
    }

    public void showErrorMessage(String title, String msg) {
        new AlertDialog.Builder(MainActivity.this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, null).create().show();
    }

    public void connectToBroker(){
        try {
            Address randomBroker = Node.BROKER_ADDRESSES.get(0);
            appNodeRequestSocket = new Socket(randomBroker.getIp(), randomBroker.getPort());
            out = new ObjectOutputStream(appNodeRequestSocket.getOutputStream());
            in = new ObjectInputStream(appNodeRequestSocket.getInputStream());
            out.writeObject(appNode);
            out.flush();
            ArrayList<String> tempAllHashtagsPublished = new ArrayList<>();
            tempAllHashtagsPublished.addAll(appNode.getChannel().getAllHashtagsPublished());
            out.writeObject(tempAllHashtagsPublished);
            out.flush();
            ArrayList<File> tempAllVideosPublished = new ArrayList<>();
            tempAllVideosPublished.addAll(appNode.getChannel().getAllVideosPublished());
            out.writeObject(tempAllVideosPublished);
            out.flush();
            HashMap<String, ArrayList<File>> tempUserVideosByHashtag = new HashMap<>();
            tempUserVideosByHashtag.putAll(appNode.getChannel().getUserVideosByHashtag());
            out.writeObject(tempUserVideosByHashtag);
            out.flush();
            boolean isPublisher = appNode.isPublisher();
            out.writeBoolean(isPublisher);
            out.flush();
            System.out.println(in.readObject());
            System.out.println("[Consumer]: Sending info table request to Broker.");
            out.writeObject("INFO");
            out.flush();
            System.out.println(in.readObject());
            appNode.setInfoTable((InfoTable) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getCameraPermission () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
            Log.i("VIDEO_PERMISSION", "Asked for Camera Permission.");
        }
    }

    private void getStoragePermission () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
            Log.i("STORAGE_PERMISSION", "Asked for Storage Permission.");
        }
    }

    @Override
    public void onBackPressed(){}
}