package in.deepaksood.videocutntrim.utils;

import java.util.ArrayList;

/**
 * Created by Deepak on 23-04-2017.
 */

public class Constants {

    // this variable will store the directory path where all the temporary work will be saved and then cleared
    // first try to get the VideoEditor folder, if failed then create a folder in internal storage as VideoEditor
    public static String directoryPath;

    // constant to store the request code for browse the video functionality for edit story
    public static final int EDIT_READ_REQUEST_CODE = 41;

    // constant to store the request code for browse the video functionality
    public static final int VIDEO_READ_REQUEST_CODE = 42;

    // constant to store the request code for browse the video functionality
    public static final int AUDIO_READ_REQUEST_CODE = 43;

    // TAG that holds position that requested the startActivityForResult()
    public static int update_position = 0;

    // intent.putExtra for uri in VideoRecyclerViewAdapter
    public static final String EXTRA_POSITION = "EXTRA_POSITION";

    // intent.putExtra from createStory to EditStory for video playing
    public static final String EDIT_STORY = "EDIT_STORY";
    public static final String STORY_URI = "STORY_URI";

    /* List holding images and audio */
    public static ArrayList<String> imageUriList = new ArrayList<>();
    public static ArrayList<String> audioUriList = new ArrayList<>();
}
