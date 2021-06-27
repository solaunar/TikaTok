package gr.aueb.distributedsystems.tikatok.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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
    static int port = 12000;
    TextView usernameTextView;
    Button loginBtn;
    AppNode appNode;
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
        //System.out.println(ip);
        Address address = new Address(ip, port);
        port += 1000;
        Log.i("APPNODE_IP_PORT", "IP: " + address.getIp() + "PORT: "+ address.getPort());
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
//                    System.out.println(appNode.getChannel());
//                    Thread t = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            appNode.connectToBroker();
//                        }
//                    });
//                    t.start();
//                    long tID = t.getId();
                    GetInfoTableTask getInfoTableTask = new GetInfoTableTask();
                    getInfoTableTask.execute("CONNECT_TO_BROKER");
                    goToSearch(appNode);
                }
            }
        });
    }

    private class GetInfoTableTask extends AsyncTask<String, String, AppNode>{

        @Override
        protected AppNode doInBackground(String... strings) {
            if(strings[0].equals("CONNECT_TO_BROKER"))
                return appNode.connectToBroker();
            return null;
        }

        @Override
        protected void onPostExecute(AppNode user) {
            super.onPostExecute(user);
            appNode = user;
        }
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
//        searchActivityScreen.putExtra(SearchActivity.THREAD_ID, tID);
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