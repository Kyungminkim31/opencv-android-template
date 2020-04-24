package org.gaval.tutorial;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.CAMERA;

public class HelloOpenCvActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private final static String TAG = HelloOpenCvActivity.class.getSimpleName();
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;

    private FramesHarvester mHarvestor;

    static {
        System.loadLibrary( "opencv_java4" );
        System.loadLibrary( "native-lib" );
    }
    public native void convertRGBtoGray(long matAddrInput, long matAddrResult);
    public native void initVideoWriter(int w, int h);
    public native void recordVideo(long matAddrInput);
    public native void closeVideoWriter();


    // Callback Method
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback( this ){
        @Override
        public void onManagerConnected(int status){
            switch(status){
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    break;
                case LoaderCallbackInterface.INIT_FAILED:
                    Log.i(TAG, "failed to load OpenCV");
                    mOpenCvCameraView.disableView();
                    break;
                default:
                    super.onManagerConnected( status );
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState){
        Log.i(TAG, "called onCreate");
        super.onCreate( savedInstanceState );

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_helloopencv);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility( SurfaceView.VISIBLE );
        mOpenCvCameraView.setCvCameraViewListener( this );
        mOpenCvCameraView.setCameraIndex( 0 );

        mHarvestor = new FramesHarvester("PreFrameHarvester");
        mHarvestor.startTimerForHarvestThread();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
        mHarvestor.stopTimerForHarvestThread();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.i(TAG, "called onResume and Internal OpenCV library not found");
            OpenCVLoader.initAsync( OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback );
        } else {
            Log.i(TAG, "called onResume, found OpenCV library inside the package, and use it");
            mLoaderCallback.onManagerConnected( LoaderCallbackInterface.SUCCESS );
        }
    }

    ////////////////////////////////////////////////////
    // Methods for OpenCV the Camera Library
    ////////////////////////////////////////////////////
    public void onCameraViewStarted(int width, int height){
        Log.i(TAG, "calls onCameraViewsStarted( "+width+", "+height+")");
        initVideoWriter(width, height);
    }

    public void onCameraViewStopped() {
        Log.i(TAG, "calls onCameraViewStopped()");
        closeVideoWriter();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        // todo code for manipulation for frames
        matInput = inputFrame.rgba();
        mHarvestor.setTempRepoMat( matInput );

        if( matResult == null){
            matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());
        }

//        recordVideo( matInput.getNativeObjAddr());

        return matInput;
    }

    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList( mOpenCvCameraView );
    }

    protected void onCameraPermissionGranted() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();
        if (cameraViews == null) {
            return;
        }
        for (CameraBridgeViewBase cameraBridgeViewBase : cameraViews) {
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.setCameraPermissionGranted();
            }
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Code for Camera Permission
    // OpenCV camera doesn't act properly without below code
    /////////////////////////////////////////////////////////////////////

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onStart() {
        super.onStart();
        boolean havePermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission( CAMERA ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions( new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE );
                havePermission = false;
            }
        }
        if (havePermission) {
            onCameraPermissionGranted();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onCameraPermissionGranted();
        } else {
            showDialogForPermission( "Check permission for the camera app." );
        }
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder( HelloOpenCvActivity.this );
        builder.setTitle( "Notification" );
        builder.setMessage( msg );
        builder.setCancelable( false );
        builder.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                requestPermissions( new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE );
            }
        } );
        builder.setNegativeButton( "No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        } );
        builder.create().show();
    }

    public void onCheckClick(View view){
        Log.d(TAG, "check button clicked");
        mHarvestor.collectAndCheckFrames();
    }

}
