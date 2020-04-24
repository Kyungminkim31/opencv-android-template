package org.gaval.tutorial;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class FramesHarvester {


    private final int HARVEST_FRAMES_LIMIT = 5;
    private final int HARVEST_FRAMES_PERIOD = 1000;
    private final int HARVEST_FRAMES_DELAY = 5000;

    private String logTag = getClass().getSimpleName();
    private String mNameHarvest;

    private Mat mTempRepoMat;
    private Queue<Mat> mRollFramesQueue = new LinkedList<>();

    private Timer timer;
    private TimerTask timerTask;

    /**
     * Constructor
     * @param nameOfHarvester
     */
    public FramesHarvester(String nameOfHarvester) {
        this.mNameHarvest = nameOfHarvester;
        this.logTag = this.logTag + "::" + nameOfHarvester;
    }

    /**
     * set
     * @param tempRepoMat
     */
    public void setTempRepoMat(Mat tempRepoMat) {
        this.mTempRepoMat = tempRepoMat;
    }

    private Thread harvestThread = new Thread( new Runnable() {
        @Override
        public void run() {
            try {
                Log.d( logTag, "harvest a frames on every a second..." );
                if (mRollFramesQueue.size() > HARVEST_FRAMES_LIMIT) {
                    mRollFramesQueue.remove();
                }
                Mat resultMat = new Mat(mTempRepoMat.cols(), mTempRepoMat.rows(), mTempRepoMat.type());
                Imgproc.cvtColor( mTempRepoMat, resultMat, Imgproc.COLOR_BGR2RGBA );
                mRollFramesQueue.add( resultMat );
            } catch (NoSuchElementException e){
                Log.e(logTag, "Error : run() from harvestThread\n"+e.getMessage());
            }
        }
    } );

    public void startTimerForHarvestThread(){
        Log.d(logTag, "start to "+mNameHarvest+" harvest a frames...");
        timer = new Timer();

        timerTask = new TimerTask(){
            public void run(){
                harvestThread.run();
            }
        };

        timer.schedule( timerTask, HARVEST_FRAMES_DELAY, HARVEST_FRAMES_PERIOD );
    }

    public void stopTimerForHarvestThread() {
        Log.d(logTag, "stop to harvest a frames...");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void collectAndCheckFrames(){

        if(timer != null){
            stopTimerForHarvestThread();
        }

        if(mRollFramesQueue.size() > 0){
            MatOfByte byteMat = new MatOfByte();
            int bufferSize = 1024 * 1024;
//            Imgcodecs.imencode( ".jpg", item, byteMat );

            int i = 0;
            for(Mat item:mRollFramesQueue){
                Imgcodecs.imwrite("/mnt/sdcard/test_image"+String.valueOf(i)+".jpg", item);
                i++;
            }
        }
    }

}
