package gr.aueb.distributedsystems.tikatok.backend;

import android.os.Environment;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * AppNode class extends Node
 *
 * AppNode represents a user of the system
 * Publisher/ Consumer or both
 */
public class AppNode extends Node {

    //userDirectory, the path where the already uploaded files of
    //a user are stored, might be blank forever
    private String userDirectory = "";
    private Address address;
    transient Scanner appNodeInput;
    private Channel channel;
    transient ServerSocket appNodeServerSocket = null;
    transient Socket connection = null;
    private boolean isPublisher = false;
    private boolean isSubscribed = false;
    private static InfoTable infoTable;
    private HashMap<String, ArrayList<File>> subscribedTopics = new HashMap<>();

    public AppNode(Address address) {
        this.address = address;
        appNodeInput = new Scanner(System.in);
    }

    public Address getAddress() {
        return address;
    }

    public Scanner getAppNodeInput() {
        return appNodeInput;
    }

    public boolean isPublisher() {
        return isPublisher;
    }

    public void setPublisher(boolean publisher) {
        isPublisher = publisher;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public synchronized HashMap<String, ArrayList<File>> getSubscribedTopics() {
        return subscribedTopics;
    }

    public InfoTable getInfoTable() {
        return infoTable;
    }

    public void setInfoTable(InfoTable infoTable) {
        this.infoTable = infoTable;
    }

    /**
     * compare method: used to check if 2 Address obj are the same (have the same port and ip)
     * @param appNode the AppNode obj that we are comparing this with
     * @return boolean true if channel/user names are the same or false if they are not
     */
    public boolean compare(AppNode appNode) {
        return this.getAddress().compare(appNode.getAddress());
    }

    /**
     * method downloadVideo sends the video File obj to Appnode requesting it
     *                    and stores it in the downloads folder of that user
     * @param video File obj of video that user asked to be deleted
     */
    public synchronized void downloadVideo (File video) throws IOException {
        Address randomBroker = Node.BROKER_ADDRESSES.get(0);
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Socket appNodeRequestSocket;

        try {
            appNodeRequestSocket = new Socket(randomBroker.getIp(), randomBroker.getPort());
            out = new ObjectOutputStream(appNodeRequestSocket.getOutputStream());
            in = new ObjectInputStream(appNodeRequestSocket.getInputStream());
            String videoChosen = video.getPath();
            videoChosen = videoChosen.substring (videoChosen.indexOf("$") + 1, videoChosen.lastIndexOf("$")) + "-" + videoChosen.substring(videoChosen.lastIndexOf("$")+1);
            out.writeObject(new VideoFile(video));
            out.flush();
            System.out.println(in.readObject());
            out.writeObject(this);
            out.flush();

            ArrayList<VideoFile> chunks = new ArrayList<>();
            while (true) {
                Object response = in.readObject();
                if (response.equals("NO MORE CHUNKS")) break;
                chunks.add((VideoFile) response);
                System.out.println("Received chunk");
                out.writeObject("RECEIVED");
                out.flush();
            }
            out.writeObject("EXIT");
            out.flush();
            System.out.println("[Broker]: " + in.readObject());
            in.close();
            out.close();
            appNodeRequestSocket.close();

            String videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

            System.out.println(videoPath + videoChosen.toLowerCase() + ".mp4");
            FileOutputStream fos = new FileOutputStream(videoPath + videoChosen.toLowerCase() + ".mp4");
            int i = 0;
            for (VideoFile chunk : chunks) {
                i++;
                fos.write(chunk.getData());
            }
            fos.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * method uploadVideo creates a new File obj for the video and updates any data structures needed
     * @param directory String input of user of path of video to be uploaded
     * @param hashtags ArrayList<String> hashtags that user gave for this video
     */
    public synchronized void uploadVideo(String directory, ArrayList<String> hashtags) {
        File videoFile = new File(directory);
        if (getChannel().getAllVideosPublished().contains(videoFile)){
            System.out.println("Video has been uploaded. Please chose upload again if you'd like to upload a NEW video.");
            return;
        }
        HashMap<String, ArrayList<File>> userVideosByHashtag = getChannel().getUserVideosByHashtag();
        if (hashtags == null) hashtags = new ArrayList<>();
        for (String hashtag : hashtags) {
            if (!getChannel().getAllHashtagsPublished().contains(hashtag)) {
                getChannel().getAllHashtagsPublished().add(hashtag);
            }
            if (userVideosByHashtag.containsKey(hashtag)) {
                ArrayList<File> videosByHashtag = userVideosByHashtag.get(hashtag);
                videosByHashtag.add(videoFile);
            } else {
                ArrayList<File> videosByHashtag = new ArrayList<>();
                videosByHashtag.add(videoFile);
                userVideosByHashtag.put(hashtag, videosByHashtag);
            }
        }
        getChannel().getAllVideosPublished().add(videoFile);
        getChannel().getUserHashtagsPerVideo().put(videoFile, hashtags);
    }

    /**
     * method deleteVideo removes the video File obj from any data structures needed
     *                    as well as removes topics that had only that video related to them
     * @param video File obj of video that user asked to be deleted
     */
    public synchronized void deleteVideo(File video) {
        getChannel().getAllVideosPublished().remove(video);
        ArrayList<String> hashtagsAssociated = getChannel().getUserHashtagsPerVideo().get(video);
        getChannel().getUserHashtagsPerVideo().remove(video);
        HashMap<String, ArrayList<File>> userVideosByHashtag = getChannel().getUserVideosByHashtag();
        for (String hashtag :hashtagsAssociated){
            if(userVideosByHashtag.containsKey(hashtag)) {
                ArrayList<File> hashtagsFile = userVideosByHashtag.get(hashtag);
                hashtagsFile.remove(video);
                if (hashtagsFile.isEmpty()){
                    userVideosByHashtag.remove(hashtag);
                }
            }
        }
        getChannel().getAllHashtagsPublished().clear();
        getChannel().getAllHashtagsPublished().addAll(userVideosByHashtag.keySet());
//        System.out.println(getChannel().getAllHashtagsPublished());
//        System.out.println(getChannel().getAllVideosPublished());
//        System.out.println(getChannel().getUserHashtagsPerVideo());
//        System.out.println(getChannel().getUserVideosByHashtag());
    }

    public AppNode updateOnDelete(File toBeDeleted){
        try {
            if (this.isPublisher()) {
                Address randomBroker = Node.BROKER_ADDRESSES.get(0);
                ObjectOutputStream out;
                ObjectInputStream in;
                Socket appNodeRequestSocket;
                appNodeRequestSocket = new Socket(randomBroker.getIp(), randomBroker.getPort());
                out = new ObjectOutputStream(appNodeRequestSocket.getOutputStream());
                in = new ObjectInputStream(appNodeRequestSocket.getInputStream());
                out.writeObject("DELETE");
                out.flush();
                System.out.println("[Publisher]: Notifying brokers of updated content.");
                out.writeObject(this);
                out.flush();
                out.writeObject(toBeDeleted);
                out.flush();
                ArrayList<String> tempAllHashtagsPublished = new ArrayList<>();
                tempAllHashtagsPublished.addAll(this.getChannel().getAllHashtagsPublished());
                out.writeObject(tempAllHashtagsPublished);
                out.flush();
                System.out.println(in.readObject());
                System.out.println("[Consumer]: Sending info table request to Broker.");
                out.writeObject("INFO");
                out.flush();
                in.readObject();
                this.setInfoTable((InfoTable) in.readObject());
                out.writeObject("EXIT");
                out.flush();
                System.out.println("[Broker]: " + in.readObject());
                in.close();
                out.close();
                appNodeRequestSocket.close();
                return this;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AppNode updateInfoTableOnSubscribe(String topic){
        if (this.getSubscribedTopics().containsKey(topic)) return null;
        try {
                Address randomBroker = Node.BROKER_ADDRESSES.get(0);
                ObjectOutputStream out;
                ObjectInputStream in;
                Socket appNodeRequestSocket;
                appNodeRequestSocket = new Socket(randomBroker.getIp(), randomBroker.getPort());
                out = new ObjectOutputStream(appNodeRequestSocket.getOutputStream());
                in = new ObjectInputStream(appNodeRequestSocket.getInputStream());
                out.writeObject("REG");
                out.flush();
                out.writeObject(this);
                out.flush();
                out.writeObject(topic);
                out.flush();
                ArrayList<File> subscribedVideos = new ArrayList<>(this.getInfoTable().getAllVideosByTopic().get(topic));
                if (this.getChannel().getAllHashtagsPublished().contains(topic))
                    subscribedVideos.removeAll(this.getChannel().getUserVideosByHashtag().get(topic));
                this.getSubscribedTopics().put(topic, subscribedVideos);
                this.setSubscribed(true);
                out.writeObject("INFO");
                out.flush();
                in.readObject();
                this.setInfoTable((InfoTable) in.readObject());
                out.writeObject("EXIT");
                out.flush();
                System.out.println("[Broker]: " + in.readObject());
                in.close();
                out.close();
                appNodeRequestSocket.close();
                return this;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * method updateOnSubscriptions checks for any changes of the available videos related to
     *                              the subscribed topics, and if changes exist it returns the
     *                              list mentioned bellow
     * @return ArrayList<String> updatedTopics which is the list of the topics that have
     *         been updated (either video got deleted or uploaded on this subscribed topic)
     */
    public synchronized ArrayList<String> updateOnSubscriptions(){
        ArrayList<String> updatedTopics = new ArrayList<>();
        for (String topic: subscribedTopics.keySet()){
            ArrayList<File> availableVideos = null;
            if(infoTable.getAllVideosByTopic().containsKey(topic))
                availableVideos = new ArrayList<>(infoTable.getAllVideosByTopic().get(topic));
            if (getChannel().getAllHashtagsPublished().contains(topic)){
                availableVideos.removeAll(getChannel().getUserVideosByHashtag().get(topic));
            }
            if(!subscribedTopics.get(topic).equals(availableVideos)){
                updatedTopics.add(topic);
                subscribedTopics.remove(topic);
                subscribedTopics.put(topic, availableVideos);
            }
        }
        return updatedTopics;
    }

    /**
     * method readDirectory lists already uploaded videofiles of user (at the mp4 folder of the userdirectory)
     *                      as well as their hashtags (at the hashtags folder of the userdirectory)
     *                      calls setChannelMaps to update the channel data structures
     */
    public void readDirectory() {
        File[] videoFiles = new File(userDirectory + "mp4").listFiles();
        File[] hashtags = new File(userDirectory + "hashtags").listFiles();
        setChannelMaps(videoFiles, hashtags);
    }

    /**
     * method setChannelMaps gets the 2 mentioned parameters and for each couple of files (one in videoFiles, one in hashtags)
     *                       updates the data structures of the Channel of the Publisher of this AppNode by storing the
     *                       videofiles and reading the files that contain the hashtags
     *                       (such as pex userHashtagsPerVideo)
     * @param videoFiles array of videoFiles already published by appNode
     * @param hashtags array of hashtags files associated with the videos
     */
    public void setChannelMaps(File[] videoFiles, File[] hashtags) {
        HashMap<File, ArrayList<String>> userHashtagsPerVideo = new HashMap<>();
        ArrayList<String> allHashtagsPublished = new ArrayList<>();
        ArrayList<File> allVideosPublished = new ArrayList<>(Arrays.asList(videoFiles));
        for (int i = videoFiles.length -1; i >= 0; i--) {
            File video = videoFiles[i];
            File hashtag = hashtags[i];
            ArrayList<String> hashtagList = readHashtagsFile(hashtag, allHashtagsPublished);
            userHashtagsPerVideo.put(video, hashtagList);
        }
        HashMap<String, ArrayList<File>> userVideosByHashtag = new HashMap<>();
        for (String hashtagPublished : allHashtagsPublished) {
            ArrayList<File> videosForThisHashtag = new ArrayList<>();
            for (Map.Entry videoElement : userHashtagsPerVideo.entrySet()) {
                File video = (File) videoElement.getKey();
                if (((ArrayList<String>) videoElement.getValue()).contains(hashtagPublished)) {
                    videosForThisHashtag.add(video);
                }
            }
            userVideosByHashtag.put(hashtagPublished, videosForThisHashtag);
        }
        channel.setUserHashtagsPerVideo(userHashtagsPerVideo);
        channel.setAllHashtagsPublished(allHashtagsPublished);
        channel.setUserVideosByHashtag(userVideosByHashtag);
        channel.setAllVideosPublished(allVideosPublished);
    }//reads hashtags from txt file and returns them in list of String

    /**
     * method readHashtagsFile reads the file that has the hashtags, updates the channel data structure of allHashtagsPublished
     *                         and returns an ArrayList of Strings, which will be the hashtags read
     * @param hashtag the File obj to read from
     * @param allHashtagsPublished the data structure to be updated
     * @return
     */
    public ArrayList<String> readHashtagsFile(File hashtag, ArrayList<String> allHashtagsPublished) {
        ArrayList<String> hashtagList = new ArrayList<>();
        Scanner hashtagReader = null;
        try {
            hashtagReader = new Scanner(hashtag);
            while (hashtagReader.hasNextLine()) {
                String hashtagRead = hashtagReader.nextLine();
                hashtagList.add(hashtagRead.toLowerCase());
                //add to total published hashtags list
                if (!allHashtagsPublished.contains(hashtagRead)) {
                    allHashtagsPublished.add(hashtagRead.toLowerCase());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return hashtagList;
    }

    /**
     * method init initializes the AppNode, asks user to assign themselves a username
     *             asks them if they have already published content, so that it is uploaded to the system and they can
     *             work as Publishers, in any case (if they do have content or if they don't have prepublished content)
     *             the AppNode will start working as a Consumer as well (search for/ subscribe to topics) and have the
     *             chance later on to become a Publisher
     */
    public void init() {
        System.out.println("[AppNode]: created.");
        System.out.println("[AppNode]: Please enter a username: ");
        String channelName = appNodeInput.nextLine().toLowerCase();
        setChannel(new Channel(channelName));
        System.out.println("Do you have any content to upload?");
        System.out.println("Please respond by typing 1 or 2:\n" +
                "1. Yes.\n" +
                "2. No.");
        int response = appNodeInput.nextInt();
        //If user has preuploaded content we ask for the directory of it (folder which contains mp4 folder and hashtags folder)
        if (response == 1) {
            isPublisher = true;
            System.out.println("Please specify the directory that contains the mp4 and hashtags folders for your existent videos.");
            while (this.userDirectory.isEmpty())
                this.userDirectory = appNodeInput.nextLine();
            //and start a new thread to handle BROKER requests that need to pull video from this publisher
            //as well as start the AppNode server
            Thread appNodeServer = new Thread(new Runnable() {
                @Override
                public void run() {
                    readDirectory();
                    openAppNodeServer();
                }
            });
            appNodeServer.start();
        }
        //Creating a consumer actions handler in any use case
        Thread appNodeConsumer = new AppNodeActionsForConsumers(this);
        appNodeConsumer.start();
    }

    /**
     * method openAppNodeServer creates new ServerSocket and awaits for BROKER requests
     * which will be handled by the AppNodeActionsForBrokers handler class
     */
    public void openAppNodeServer() {
        try {
            appNodeServerSocket = new ServerSocket(address.getPort(), Node.BACKLOG);
            System.out.println("[Publisher]: " + this + " is ready to accept requests.");
            while (true) {
                connection = appNodeServerSocket.accept();
                Thread brokerThread = new AppNodeActionsForBrokers(connection, this);
                brokerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * method hashTopic hashed the given String topic using SHA-1 encoding
     *        and based on the available brokers given by the hashmap in the parameters
     *        returns the Address obj of the broker this topic should be assigned to
     * @param topic String topic to be hashed (assigned to a BROKER)
     * @param hashIDAssociatedWithBrokers HashMap of the brokerAddresses associated with their ids
     * @return
     */
    public Address hashTopic(String topic, HashMap<Address, BigInteger> hashIDAssociatedWithBrokers) {
        byte[] bytesOfMessage = null;
        MessageDigest md = null;
        try {
            bytesOfMessage = topic.getBytes("UTF-8");
            md = MessageDigest.getInstance("SHA-1");
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Unsupported encoding");
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Unsupported hashing");
        }
        byte[] digest = md.digest(bytesOfMessage);
        BigInteger hashTopic = new BigInteger(1, digest);
        ArrayList<BigInteger> brokers = new ArrayList<>(hashIDAssociatedWithBrokers.values());
        Collections.sort(brokers);
        BigInteger maxID = brokers.get(brokers.size() - 1);
        hashTopic = hashTopic.mod(maxID);

        for (BigInteger id : brokers) {
            if (hashTopic.compareTo(id) < 0) {
                for (Map.Entry<Address, BigInteger> entry : hashIDAssociatedWithBrokers.entrySet()) {
                    if (entry.getValue().equals(id)) {
                        return entry.getKey();
                    }
                }
            }
        }
        Random random = new Random();
        return ((ArrayList<Address>) hashIDAssociatedWithBrokers.keySet()).get(random.ints(0, hashIDAssociatedWithBrokers.size()).findFirst().getAsInt());
    }

    public AppNode connectToBroker(){
        try {
            Address randomBroker = Node.BROKER_ADDRESSES.get(0);
            ObjectOutputStream out;
            ObjectInputStream in;
            Socket appNodeRequestSocket;
            appNodeRequestSocket = new Socket(randomBroker.getIp(), randomBroker.getPort());
            out = new ObjectOutputStream(appNodeRequestSocket.getOutputStream());
            in = new ObjectInputStream(appNodeRequestSocket.getInputStream());
            out.writeObject(this);
            out.flush();
            ArrayList<String> tempAllHashtagsPublished = new ArrayList<>();
            tempAllHashtagsPublished.addAll(this.getChannel().getAllHashtagsPublished());
            out.writeObject(tempAllHashtagsPublished);
            out.flush();
            ArrayList<File> tempAllVideosPublished = new ArrayList<>();
            tempAllVideosPublished.addAll(this.getChannel().getAllVideosPublished());
            out.writeObject(tempAllVideosPublished);
            out.flush();
            HashMap<String, ArrayList<File>> tempUserVideosByHashtag = new HashMap<>();
            tempUserVideosByHashtag.putAll(this.getChannel().getUserVideosByHashtag());
            out.writeObject(tempUserVideosByHashtag);
            out.flush();
            boolean isPublisher = this.isPublisher();
            out.writeBoolean(isPublisher);
            out.flush();
            System.out.println(in.readObject());
            System.out.println("[Consumer]: Sending info table request to Broker.");
            out.writeObject("INFO");
            out.flush();
            System.out.println(in.readObject());
            this.setInfoTable((InfoTable) in.readObject());
            //System.out.println(getInfoTable());
            out.writeObject("EXIT");
            out.flush();
            System.out.println("[Broker]: " + in.readObject());
            in.close();
            out.close();
            appNodeRequestSocket.close();
            return this;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * method find iterates through the infotable data structure to check if topic exists and returns an Address obj
     * @param topic String topic that user asked to search or subscribe to
     * @return Address obj if the topic does exist it will be one of the broker addresses, if not then null is returned
     */
    public Address find(String topic) {
        HashMap<Address, ArrayList<String>> topicsAssociatedWithBrokers = this.getInfoTable().getTopicsAssociatedWithBrokers();
        Iterator it = topicsAssociatedWithBrokers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            ArrayList<String> brokerTopics = (ArrayList<String>) pair.getValue();
            for (String topicRegistered : brokerTopics) {
                if (topicRegistered.equals(topic)) {
                    return (Address) pair.getKey();
                }
            }
        }
        return null;
    }
}
