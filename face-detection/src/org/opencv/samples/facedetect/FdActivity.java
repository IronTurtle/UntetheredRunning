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

public class FdActivity extends Activity implements CvCameraViewListener {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;


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
  	
  	//key to be used by the handler
  	public static final String STATUS_TEXT = "statusText";
  	
  	//Private members
  	private UntetheredBT mUntetheredBT = null;

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
                        InputStream is = getResources().openRawResource(R.raw.classifierwallclock);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "classifierwallclock.xml");
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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);
        //Bluetooth
    	//Create a new UntetheredBT object
    	//mUntetheredBT = new UntetheredBT(this, mHandler);
    	
    	//Start the UntetheredBT connection rolling.
    	//mUntetheredBT.start();
    	
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
    	
    	if((startStopBtn.getText().toString()).equals(getResources().getString(R.string.START_APP_STRING))) {
    		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
    		startStopBtn.setText(R.string.STOP_APP_STRING);
        	/*if(mUntetheredBT.getBluetoothState() == UntetheredBT.BT_CONNECTED) {
	        	mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	    		startStopBtn.setText(R.string.STOP_APP_STRING);
        	}
        	else {
        		//error
        		Toast toast = Toast.makeText(getApplicationContext(), "Bluetooth Not Connected", Toast.LENGTH_SHORT);
        		toast.show();
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
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(Mat inputFrame) {

        inputFrame.copyTo(mRgba);
        Imgproc.cvtColor(inputFrame, mGray, Imgproc.COLOR_RGBA2GRAY);
        
        Point center = new Point(mGray.width()/2,mGray.height()/2);
    	double angle = -90;
    	double scale = 1.0;

    	Mat mapMatrix = Imgproc.getRotationMatrix2D(center, angle, scale);
    	
    	Imgproc.warpAffine(mGray, mGray, mapMatrix, mGray.size(), Imgproc.INTER_LINEAR);

        MatOfRect faces = new MatOfRect();

        if (mJavaDetector != null)
           mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
            new Size(20, 20), new Size());

        Rect[] facesArray = faces.toArray();
        //int middle = mGray.width() / 2;
        int threshold = 5;
        
       // Log.i(TAG, Integer.valueOf(mGray.width()).toString());
        
        
        
        if (facesArray.length > 0) {
        	Point leftBound = new Point(facesArray[0].width + threshold, 0);
            Point rightBound = new Point(mGray.width() - facesArray[0].width - threshold, 0);
            Core.line(mGray, leftBound, new Point(facesArray[0].width + threshold, mGray.height()), FACE_RECT_COLOR, 3);
            Core.line(mGray, rightBound, new Point(mGray.width() - facesArray[0].width - threshold, mGray.height()), FACE_RECT_COLOR, 3);
           Core.rectangle(mGray, facesArray[0].tl(), facesArray[0].br(), FACE_RECT_COLOR, 3);

           if (facesArray[0].tl().x <= leftBound.x) {
               mUntetheredBT.sendCMD(UntetheredBT.LEFT_BUZZ);
           }
           else if (facesArray[0].br().x >= rightBound.x) {
               mUntetheredBT.sendCMD(UntetheredBT.RIGHT_BUZZ);
           }
           else {
        	   mUntetheredBT.sendCMD(UntetheredBT.NO_BUZZ);
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
