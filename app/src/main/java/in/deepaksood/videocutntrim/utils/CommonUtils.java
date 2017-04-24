package in.deepaksood.videocutntrim.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This is a Singleton Pattern and this will only be initialized once
 * use @method getInstance() to get the instance for this class
 * This is thread safe Lazy initialization singleton class
 * Created by deepak on 16-03-2017.
 */

public class CommonUtils {

    // this will hold the instance of CommonUtil which can only be created once
    private static CommonUtils instance;

    /**
     * This will disallow any outside class to create instance of this class
     */
    private CommonUtils() {}

    /**
     * This class will the only access point to this class
     *
     * @return CommonUtils
     *                  one and only one instance of CommonUtils class will be returned
     */
    public static synchronized CommonUtils getInstance() {
        if (instance == null) {
            instance = new CommonUtils();
        }
        return instance;
    }

    /* --- Common Util member functions and member variables --- */

    // TAG for logging
    private static final String TAG = CommonUtils.class.getSimpleName();

    /* This will hold the global ffmpeg variable */
    public FFmpeg ffmpeg;

    /* This will hold all mediaRecorder functionality*/
    private MediaRecorder mediaRecorder;

    /* This will hold mediaPlayer for playing the recorded media */
    private MediaPlayer mediaPlayer;

    /**
     * This function initializes the directory where all the intermediate file will be stored.
     * Default directory - VideoEditor will be used, if not available then new folder named "VideoEditor" will be created,
     * and working directory initialized to that.
     *
     * @return String
     *              path to the directory created or existing for temporary file storage
     */
    public String checkDirectory() {
        File directoryFile = Environment.getExternalStorageDirectory();
        if(directoryFile != null) {
            File folder = new File(directoryFile + File.separator + "VideoEditor");
            if(folder.exists() && folder.isDirectory()) {
                return folder.getAbsolutePath();
            } else {
                if(folder.mkdirs()) {
                    return folder.getAbsolutePath();
                } else {
                    // not able to create directory
                    return null;
                }
            }

        } else {
            // not able to get environment
            return null;
        }
    }

    /**
     * This function will supply timeStamp for path formation
     *
     * @return
     *         String with timeStamp in yyyyMMddHHmmss format
     */
    public String getTimeStamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
        Date date = new Date();
        return simpleDateFormat.format(date);
    }

    /**
     * This is used to resolve the uri got from browse functionality
     *
     * @param context
     * @param uri
     * @return
     */
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri.
     */
    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * This function will load ffmpeg for use
     */
    public void loadFFMPEGBinary(final Context context) {

        if(ffmpeg == null) {
            ffmpeg = FFmpeg.getInstance(context);
        }
        try {
            ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.v(TAG,"ffmpeg not supported");
                    Toast.makeText(context, "Ffmpeg not supported", Toast.LENGTH_SHORT).show();
                    ((Activity)context).finish();
                }

                @Override
                public void onSuccess() {
                    Log.v(TAG,"ffmpeg supported");
                }

                @Override
                public void onStart() {
                    Log.v(TAG,"ffmpeg started");
                }

                @Override
                public void onFinish() {
                    Log.v(TAG,"ffmpeg loading finished");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    /* --- Media Related Functionalities --- */

    /**
     * This method will start the voice recording
     * @param voiceStoragePath
     *                      String to the path where the file will be stored
     */
    public void startAudioRecording(String voiceStoragePath) {
        if(mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setOutputFile(voiceStoragePath);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            Log.e(TAG, "Not able to prepare mediaRecorder: " + e);
            mediaRecorder.stop();
            mediaRecorder.release();
        }

    }

    /**
     * This function will stop voice recording and release the resources
     */
    public void stopAudioRecording() {
        if(mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            Log.d(TAG, "mediaRecorder: " + mediaRecorder);
        } else {
            Log.d(TAG, "mediaRecorder already stopped");
        }
    }

    /**
     * This function plays the media passed to it
     *
     * @param voiceStoragePath
     *                      String for the path where audio is to be played
     */
    public void startAudioPlaying(String voiceStoragePath) {
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            Log.d(TAG, "media player stopped: " + mediaPlayer);
        }

        try {
            mediaPlayer.setDataSource(voiceStoragePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopAudioPlaying() {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "media player stopped: " + mediaPlayer);
        }
    }

}
