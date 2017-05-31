package motiontracker.mcv.com;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import motiontracker.mcv.com.data.GlobalData;
import motiontracker.mcv.com.data.Preferences;
import motiontracker.mcv.com.detection.AggregateLumaMotionDetection;
import motiontracker.mcv.com.detection.IMotionDetection;
import motiontracker.mcv.com.detection.LumaMotionDetection;
import motiontracker.mcv.com.detection.RgbMotionDetection;
import motiontracker.mcv.com.image.ImageProcessing;


/**
 * This class extends Activity to handle a picture preview, process the frame
 * for motion, and then save the file to the SD card.
 *
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class MotionDetectionActivity extends SensorsActivity {

    private static final String TAG = "MotionDetectionActivity";
    public static final String UPLOAD_URL =
            "http://www.americancuervo.com/motion_detection/upload.php";

    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static boolean inPreview = false;
    private static long mReferenceTime = 0;
    private static IMotionDetection detector = null;

    //Declaring views
    private Button buttonChoose;
    private Button buttonUpload;
    private ImageView imageView;
    private EditText editText;

    //Image request code
    private int PICK_IMAGE_REQUEST = 1;

    //storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;

    //Bitmap to get image from gallery
    private Bitmap bitmap;

    private static volatile AtomicBoolean processing = new AtomicBoolean(false);

    private File motionDetectionDirectory;
    private File currentCaptureDirectory;

    private String mAlertId = "0";
    private String mDeviceName = "";
    private String mServerUrl = "";
    private String mDirectoryUrl = "";
    private String mRemoteString = "";
    private boolean mRunning = false;
    private boolean mTakingRemoteImg = false;
    private boolean mSendEmail = false;
    private Handler mHandler = new Handler();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (Preferences.USE_RGB) {
            detector = new RgbMotionDetection();
        } else if (Preferences.USE_LUMA) {
            detector = new LumaMotionDetection();
        } else {
            // Using State based (aggregate map)
            detector = new AggregateLumaMotionDetection();
        }

        String motionDetectionString = "motion_detection";
        motionDetectionDirectory =
                new File(Environment.getExternalStorageDirectory(), motionDetectionString);
        if (!motionDetectionDirectory.exists()) {
            motionDetectionDirectory.mkdirs();
        }

        String name = String.valueOf(System.currentTimeMillis());
        currentCaptureDirectory = new File(motionDetectionDirectory, name);
        if (!currentCaptureDirectory.exists()) {
            currentCaptureDirectory.mkdirs();
        }

        // mSendEmail = true;

        mDeviceName = android.os.Build.MODEL;
        mServerUrl = "http://americancuervo.com/motion_detection/";
        String link = mServerUrl + "init.php?device=" + mDeviceName;
        mDirectoryUrl = mServerUrl + "images/" + mDeviceName + "/";
        new updateData().execute(link);

        mHandler.postDelayed(runnable, 6000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mRemoteString = getRemoteString();
            if (mRemoteString != null && mRemoteString.equals("1")) {
                mTakingRemoteImg = true;
                mSendEmail = true;
            } else {
                mHandler.postDelayed(this, 6000);
            }
        }
    };

    private String getRemoteString() {
        try {
            return new getRemoteTxtData().execute(mDirectoryUrl + "remote.txt").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        camera.setPreviewCallback(null);
        if (inPreview) camera.stopPreview();
        inPreview = false;
        camera.release();
        camera = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        camera = Camera.open();
    }

    private PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) return;
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) return;

            if (!GlobalData.isPhoneInMotion()) {
                DetectionThread thread = new DetectionThread(data, size.width, size.height);
                thread.start();
            }
        }
    };

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                // Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
            inPreview = true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) result = size;
                }
            }
        }

        return result;
    }

    private final class DetectionThread extends Thread {

        private byte[] data;
        private int width;
        private int height;

        public DetectionThread(byte[] data, int width, int height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            if (!processing.compareAndSet(false, true)) return;

            //  Log.d(TAG, "BEGIN PROCESSING...");
            try {
                // Previous frame
                int[] pre = null;
                if (Preferences.SAVE_PREVIOUS) pre = detector.getPrevious();

                // Current frame (with changes)
                // long bConversion = System.currentTimeMillis();
                int[] img = null;
                if (Preferences.USE_RGB) {
                    img = ImageProcessing.decodeYUV420SPtoRGB(data, width, height);
                } else {
                    img = ImageProcessing.decodeYUV420SPtoLuma(data, width, height);
                }
                // long aConversion = System.currentTimeMillis();
                // Log.d(TAG, "Converstion=" + "(aConversion-bConversion)");

                // Current frame (without changes)
                int[] org = null;
                if (Preferences.SAVE_ORIGINAL && img != null) org = img.clone();

                if (img != null && (detector.detect(img, width, height) || mTakingRemoteImg)) {
                    mTakingRemoteImg = false;
                    // The delay is necessary to avoid taking a picture while in
                    // the
                    // middle of taking another. This problem can causes some
                    // phones
                    // to reboot.
                    long now = System.currentTimeMillis();
                    if (now > (mReferenceTime + Preferences.PICTURE_DELAY)) {
                        mReferenceTime = now;

                        Bitmap previous = null;
                        if (Preferences.SAVE_PREVIOUS && pre != null) {
                            if (Preferences.USE_RGB)
                                previous = ImageProcessing.rgbToBitmap(pre, width, height);
                            else previous = ImageProcessing.lumaToGreyscale(pre, width, height);
                        }

                        Bitmap original = null;
                        if (Preferences.SAVE_ORIGINAL && org != null) {
                            if (Preferences.USE_RGB)
                                original = ImageProcessing.rgbToBitmap(org, width, height);
                            else original = ImageProcessing.lumaToGreyscale(org, width, height);
                        }

                        Bitmap bitmap = null;
                        if (Preferences.SAVE_CHANGES) {
                            if (Preferences.USE_RGB)
                                bitmap = ImageProcessing.rgbToBitmap(img, width, height);
                            else bitmap = ImageProcessing.lumaToGreyscale(img, width, height);
                        }

                        Log.i(TAG,
                                " --------------------------------------------------------------------------------- ");
                        Log.i(TAG, "Saving.. previous=" + previous + " original=" + original +
                                " bitmap=" + bitmap);
                        Log.i(TAG,
                                " --------------------------------------------------------------------------------- ");

                        Looper.prepare();
                        new SavePhotoTask().execute(previous, original, bitmap);
                    } else {
                        //  Log.i(TAG, "Not taking picture because not enough time has passed since the creation of the Surface");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                processing.set(false);
            }
            // Log.d(TAG, "END PROCESSING...");

            processing.set(false);
        }
    }

    private final class SavePhotoTask extends AsyncTask<Bitmap, Integer, Integer> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected Integer doInBackground(Bitmap... data) {
            //Log.i(TAG, " i: " + i);
            Bitmap bitmap = data[1];
            String name = String.valueOf(System.currentTimeMillis());
            //Log.i(TAG, " i: " + i);
            if (bitmap != null) save(name, bitmap);
           /* for (int i = 0; i < data.length; i++) {
                Log.i(TAG, " i: " + i);
                Bitmap bitmap = data[i];
                String name = String.valueOf(System.currentTimeMillis());
                if (bitmap != null) save(name, bitmap);
            }*/
            return 1;
        }

        private void save(String name, Bitmap bitmap) {
            /*String downloadFolder = Environment
                    .getExternalStoragePublicDirectory() (Environment.DIRECTORY_DOWNLOADS) +
                    "/" + mContext.getString(R.string.app_name) + "_icons";*/

            File photo = new File(currentCaptureDirectory, name + ".jpg");
            if (photo.exists()) photo.delete();

            try {
                FileOutputStream fos = new FileOutputStream(photo.getPath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                uploadMultipart(photo.getAbsolutePath(), name + ".jpg");
            } catch (java.io.IOException e) {
                Log.e("PictureDemo", "Exception in photoCallback", e);
            }
        }
    }

    /*
  * This is the method responsible for image upload
  * We need the full image path and the name for the image in this method
  * */
    public void uploadMultipart(String path, final String name) {
        //getting name for the image
        // String name = editText.getText().toString().trim();

        //getting the actual path of the image
        // String path = getPath(filePath);

        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, UPLOAD_URL)
                    .addFileToUpload(path, "image") //Adding file
                    .addParameter("name", name) //Adding text parameter to the request
                    .addParameter("alert", mAlertId) //Adding text parameter to the request
                    .addParameter("device",
                            android.os.Build.MODEL) //Adding text parameter to the request
                    .addParameter("title", currentCaptureDirectory.getName())
                    .setNotificationConfig(new UploadNotificationConfig()).setMaxRetries(2)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                            // your code here
                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo,
                                            Exception exception) {
                            // your code here
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo,
                                                ServerResponse serverResponse) {
                            Log.i(" _ ", "  ");
                            Log.d(TAG, "JSON RESPONSE::: " + serverResponse.toString());
                            Log.d(TAG, "JSON RESPONSE::: " + serverResponse.getBodyAsString());
                            Log.i(" _ ", "  ");

                            File photo = new File(currentCaptureDirectory, name);
                            if (photo.exists()) photo.delete();

                            if (mSendEmail) {
                                mSendEmail = false;
                                String link = mServerUrl + "sendEmail.php?device=" + mDeviceName +
                                        "&name=" + name + "&title=" +
                                        currentCaptureDirectory.getName();
                                new updateData().execute(link);

                                String link2 = mServerUrl + "init.php?device=" + mDeviceName;
                                new updateData().execute(link2);

                                mHandler.postDelayed(runnable, 6000);
                            }

                            // your code here
                            // if you have mapped your server response to a POJO, you can easily get it:
                            // YourClass obj = new Gson().fromJson(serverResponse.getBodyAsString(), YourClass.class);
                            // File photo = new File(Environment.getExternalStorageDirectory(), name + ".jpg");
                            // if (photo.exists()) photo.delete();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            // your code here
                        }
                    }).startUpload(); //Starting the upload

        } catch (Exception exc) {
            Log.i(TAG, exc.getMessage());
        }
    }

    //method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver()
                .query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                        MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }


    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat
                .requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage",
                        Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    public class updateData extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection conn = null;

            try {
                URL url;
                url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                } else {
                    InputStream err = conn.getErrorStream();
                }
                return "Done";
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }

    public class getRemoteTxtData extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String str = null;
            try {
                // Create a URL for the desired page
                URL url;
                url = new URL(params[0]);

                // Read all the text returned by the server
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                str = in.readLine();
                in.close();
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            }
            return str;
        }
    }
}