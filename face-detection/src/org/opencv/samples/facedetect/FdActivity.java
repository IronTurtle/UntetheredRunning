package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//Primary activity that executes cascade algorithm.
public class FdActivity extends Activity implements CvCameraViewListener {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(255, 0, 0);
    public static final int        JAVA_DETECTOR       = 0;

    private int                    flag = 0;
    private float 				   volume = 0.3f;
    private MediaPlayer			   mp = new MediaPlayer();
    private int					   threadRunning = 0;
    private int 				   mEmergency = 0;
    private boolean                mAppRunning = true;
    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private String[]               mDetectorName;

    private CameraBridgeViewBase   mOpenCvCameraView;
    
	private Button 				   startStopBtn;
    
    //Msg types that get sent back to the UI from the separate thread Handler
  	public static final int STATUS_NOT_CONNECTED = 0;
  	public static final int STATUS_CONNECTED = 1;
  	
  	//Amount of time before emergency TIME_OUT*100ms = x seconds
  	private static final int TIME_OUT = 30;
  	
  	//key to be used by the handler
  	public static final String STATUS_TEXT = "statusText";
  	
  	//Private members
  	private UntetheredBT mUntetheredBT = null;

  	//Initialize cascader and OpenCV manager on the phone.
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.untetheredhighdef);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "untetheredhighdef.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        }

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    //Initializes the Start/Stop UI
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);
    	
        startStopBtn = (Button) findViewById(R.id.buttonStartStop);
        startStopBtn.setText(R.string.START_APP_STRING);
       
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);      
    }

    /**
     * Opens Camera, opens bluetooth connection
     * @param view
     */
    public void startStopAppOnClick(View view) {
    	
    	//If the Start/Stop button is pressed change it to the opposite state
    	if((startStopBtn.getText().toString()).equals(getResources().getString(R.string.START_APP_STRING))) {

    		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
    		startStopBtn.setText(R.string.STOP_APP_STRING);

    		//If start pressed, open bluetooth connection
        	while(true) {
        		if(mUntetheredBT.getBluetoothState() == UntetheredBT.BT_CONNECTED) {
        			//Play "bluetooth connected" sound clip
            		mp = MediaPlayer.create(getApplicationContext(), R.raw.bluetoothconnected);
            		mp.setVolume(volume, volume);  
            	    mp.start();
            	    //mp.release();
            	    break;
        		}
      	    }
    	    /*
        	if(mUntetheredBT.getBluetoothState() == UntetheredBT.BT_CONNECTED) {
	        	//mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	    		//startStopBtn.setText(R.string.STOP_APP_STRING);
        	}
        	else {
        		//error
        		//Toast toast = Toast.makeText(getApplicationContext(), "Bluetooth Not Connected", Toast.LENGTH_SHORT);
        		//toast.show();
        	}*/
    	}
    	else {
    		mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
    		startStopBtn.setText(R.string.START_APP_STRING);
    	}
    }
    
    public void testBluetooth(View view) {
    	
    	setContentView(R.layout.activity_untethered);

    }

	public void urDetection(View view) {
		setContentView(R.layout.face_detect_surface_view);
	}
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	//may have to setup view
    }
    
    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
        
        //Stop the Bluetooth threads
    	mUntetheredBT.stop();
    	mAppRunning = false;
    	if (!mp.equals(null)){
    	   mp.release();
    	}
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        
        //Bluetooth
    	//Create a new UntetheredBT object
        
    	mUntetheredBT = new UntetheredBT(this, mHandler);
    	//Start the UntetheredBT connection rolling.
    	mUntetheredBT.start();
    	mAppRunning = true;
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
        if (!mp.equals(null)){
     	   mp.release();
     	}
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
        if (!mp.equals(null)){
     	   mp.release();
     	}
    }

    //Primary camera operation
    public synchronized Mat onCameraFrame(Mat inputFrame) {   	
    	
    	//If bluetooth is disconnected during use, replays "bluetooth connected" sound clip after reconnecting
    	if(mUntetheredBT.getBluetoothState() == UntetheredBT.BT_NOT_CONNECTED) {
    		flag = 1;
    	}
    	else if (mUntetheredBT.getBluetoothState() == UntetheredBT.BT_CONNECTED && flag == 1) {
    		mp = MediaPlayer.create(getApplicationContext(), R.raw.bluetoothconnected);
    		mp.setVolume(volume, volume);
    	    mp.start();
    	    flag = 0;
    	    //mp.release();
    	}

    	//Copy input frame from camera into mat
        inputFrame.copyTo(mRgba);
        //Convert image to grayscale
        Imgproc.cvtColor(inputFrame, mGray, Imgproc.COLOR_RGBA2GRAY);
        
        //Rotate image to portrait before given to detector
        Point center = new Point(mGray.width()/2,mGray.height()/2);
    	double angle = -90;
    	double scale = 1.0;
    	Mat mapMatrix = Imgproc.getRotationMatrix2D(center, angle, scale);
    	Imgproc.warpAffine(mGray, mGray, mapMatrix, mGray.size(), Imgproc.INTER_LINEAR);

        MatOfRect logo = new MatOfRect();

        //Detector detects logo in frame
        if (mJavaDetector != null)
           mJavaDetector.detectMultiScale(mGray, logo, 1.01, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
            new Size(40, 40), new Size());

        Rect[] logoArray = logo.toArray();
        //Log.i(TAG, Integer.valueOf(mGray.width()).toString());
        //If logo was found, compare to the bounding box
        if (logoArray.length > 0) {
        	
          mEmergency = 0;
          /*
           Log.i(TAG, Integer.valueOf(logoArray[0].width).toString());
           */
        	
           //Draw and create bounding box
           //120 is width of camera view based on current size of camera preview, this width will change if you change camera preview
           //size
          //left edge is 0, right edge is 120
           //((logoArray[0].width * 0.5) + 30) subtracts off from width so can calculate size of the bound from the edge of view
           double threshold = 120 - ((logoArray[0].width * 0.5) + 30); 
           Point leftBound = new Point(threshold, 0); //30 from left edge of view, with current #s
           Point rightBound = new Point(mGray.width() - threshold, 0); //30 from right edge of view, with current #s
           Core.line(mGray, leftBound, new Point(threshold, mGray.height()), FACE_RECT_COLOR, 3);
           Core.line(mGray, rightBound, new Point(mGray.width() - threshold, mGray.height()), FACE_RECT_COLOR, 3);
           Core.rectangle(mGray, logoArray[0].tl(), logoArray[0].br(), FACE_RECT_COLOR, 3);
           
           if (!mp.equals(null)){
         	   //mp.release();
           }
           
           //4 1/2 ft too far back go forward
           if (logoArray[0].width <= 52) { //52 is the width of the logo at 4 1/2 ft, found through testing
        	   //Sends buzz to front and plays "front" sound clip
               mUntetheredBT.sendCMD(UntetheredBT.FRONT_BUZZ);
               mp = MediaPlayer.create(getApplicationContext(), R.raw.frontbuzz);
               mp.setVolume(volume, volume);
       	       mp.start();
           }
           //2 1/2 ft too close go backward
           else if (logoArray[0].width >= 100) { //100 is the width of the logo at 2 1/2 ft, found through testing
        	 //Sends buzz to back and plays "back" sound clip
               mUntetheredBT.sendCMD(UntetheredBT.BACK_BUZZ);
               mp = MediaPlayer.create(getApplicationContext(), R.raw.backbuzz);
               mp.setVolume(volume, volume);
       	       mp.start();
           }
           //move left
           else if (logoArray[0].tl().x <= leftBound.x) { //if logo crosses to left of left bound, left feedback
        	 //Sends buzz to left and plays "left" sound clip
        	   mUntetheredBT.sendCMD(UntetheredBT.LEFT_BUZZ);
        	   mp = MediaPlayer.create(getApplicationContext(), R.raw.leftbuzz);
        	   mp.setVolume(volume, volume);
       	       mp.start();
           }
           //move right
           else if (logoArray[0].br().x >= rightBound.x) { //if logo crosses to right of right bound, right feedback
        	 //Sends buzz to right and plays "right" sound clip
        	   mUntetheredBT.sendCMD(UntetheredBT.RIGHT_BUZZ);
        	   mp = MediaPlayer.create(getApplicationContext(), R.raw.rightbuzz);
        	   mp.setVolume(volume, volume);
       	       mp.start();
           }
           //good position
           else {
        	   mUntetheredBT.sendCMD(UntetheredBT.NO_BUZZ);
           }
        }
        //image cannot be found for x seconds, emergency stop
        else {
        	//mUntetheredBT.sendCMD(UntetheredBT.EMERGENCY_BUZZ);
        	mEmergency = 1;
        	if (threadRunning == 0) {
        		(new Thread() 
        		{
        			public synchronized void run()
        			{
        				int timer = 0;
        				while(mEmergency == 1 && mAppRunning) {
        					try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        					timer++;
        					//Not the best option should fix, probably shouldn't use the count like this
        					if (timer == TIME_OUT) {       	
        						//Buzzes all motors and plays "danger" sound clip
        						mUntetheredBT.sendCMD(UntetheredBT.EMERGENCY_BUZZ);
        						mp = MediaPlayer.create(getApplicationContext(), R.raw.danger);
        						mp.setVolume(volume, volume);
        			       	    mp.start();
        						timer = TIME_OUT;        						
        					}
        				}
        				threadRunning = 0;
        			}
        		}).start();
        		threadRunning = 1;
        	}
        }
        return mGray;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }
    
    /*****************************************************************************/
    //Bluetooth
    /*****************************************************************************/
    
    public void pressFront(View view)
    {
    	mUntetheredBT.sendCMD(UntetheredBT.FRONT_BUZZ);
    }
    
    public void pressLeft(View view)
    {
    	mUntetheredBT.sendCMD(UntetheredBT.LEFT_BUZZ);
    }
    
    public void pressRight(View view)
    {
    	mUntetheredBT.sendCMD(UntetheredBT.RIGHT_BUZZ);
    }
    
    public void pressBack(View view)
    {
    	mUntetheredBT.sendCMD(UntetheredBT.BACK_BUZZ);
    }
    
    public void pressNormal(View view)
    {
    	mUntetheredBT.sendCMD(UntetheredBT.NO_BUZZ);
    }
    
    public void pressEmergency(View view)
    {
    	mUntetheredBT.sendCMD(UntetheredBT.EMERGENCY_BUZZ);
    }
    
  	//Handler that retrieves information from the thread
  	private final Handler mHandler = new Handler() {
  		public void handleMessage(Message msg)
  		{
  			switch(msg.what)
  			{
  				case STATUS_NOT_CONNECTED:
  					//mTextStatus.setText("Not Connected");
  					break;
  				case STATUS_CONNECTED:
  					//mTextStatus.setText("Connected!");
  					break;
  			}
  		}
  	};
}
