package in.deepaksood.videocutntrim.utils;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;

/**
 * Created by Deepak on 23-04-2017.
 */

public class Constants {

    // this variable will store the directory path where all the temporary work will be saved and then cleared
    // first try to get the VideoEditor folder, if failed then create a folder in internal storage as VideoEditor
    public static String directoryPath;

    // constant to store the request code for browse the video functionality
    public static final int READ_REQUEST_CODE = 42;

    // TAG that holds position that requested the startActivityForResult()
    public static int update_position = 0;

    // intent.putExtra for uri in RecyclerViewAdapter
    public static final String EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI";
}
