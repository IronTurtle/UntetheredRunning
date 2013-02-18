package com.android.untetheredrunning;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.OpenCVTestCase;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.android.untetheredrunning.*;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class UntheredRunningActivity extends Activity {
    /** Called when the activity is first created. */

    Button btnTakePhoto;
    ImageView imgTakenPhoto;
    private static final int CAMERA_PIC_REQUEST = 1313;
    final String TAG = "MyCamera";
    
    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
       @Override
       public void onManagerConnected(int status) {
          switch (status) {
             case LoaderCallbackInterface.SUCCESS:
             {
                Log.i(TAG, "OpenCV loaded successfully");
    	        // Create and set View
    	        setContentView(R.layout.activity_untethered_running);
    	        btnTakePhoto = (Button) findViewById(R.id.button1);
    	        imgTakenPhoto = (ImageView) findViewById(R.id.imageView1);

    	        btnTakePhoto.setOnClickListener(new btnTakePhotoClicker());
    	      
    	     } break;
    	     default:
    	     {
    	        super.onManagerConnected(status);
    	     } break;
    	  }
       }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
          Log.e(TAG, "Cannot connect to OpenCV Manager");
        }

        
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       // TODO Auto-generated method stub
       super.onActivityResult(requestCode, resultCode, data);
        
       if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
          if (data != null) {
        	 //Captured frame from Camera App 
             Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
             
             //Convert to grayscale
             thumbnail = toGrayscale(thumbnail);
             //thumbnail = toGrayscale(thumbnail,80,60);
             Mat CamImage = new Mat();
             Mat LogoImage = new Mat();
             Mat CamDescriptors = new Mat();
             Mat LogoDescriptors = new Mat();
             MatOfKeyPoint CamKeypoints = new MatOfKeyPoint();
             MatOfKeyPoint LogoKeypoints = new MatOfKeyPoint();
             Mat o_image1 = new Mat();
	     		 
      		 Mat rgb1 = new Mat();
         	 Mat rgb2 = new Mat();
      	     Mat rgb3 = new Mat();
             //List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
             //Get logo/initial image to compare to
             File rootsd = Environment.getExternalStorageDirectory();
             
             File dcim = new File(rootsd.getAbsolutePath() + "/DCIM/Camera/IMG_20130209_205951.jpg"); 
             
             Bitmap logo = BitmapFactory.decodeFile(dcim.toString());
             
             //File check = new File(rootsd.getAbsolutePath() + "/DCIM/Camera/IMG_20130209_205951.jpg"); 
             
             //Bitmap pic2 = BitmapFactory.decodeFile(check.toString());
             
             //converts to grayscale based on captured picture's width & height (change later)
             logo = toGrayscale(logo);
             thumbnail = toGrayscale(thumbnail);
             
             
             //Configure bitmap pixels
             Bitmap mBitmap1 = thumbnail.copy(Bitmap.Config.ARGB_8888, false); 
             Bitmap mBitmap2 = logo.copy(Bitmap.Config.ARGB_8888, false);
             Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap1, mBitmap2.getWidth(), mBitmap2.getHeight(), false);
             //System.out.println("height==" + resizedBitmap.getHeight() + "width==" + resizedBitmap.getWidth());
            
             //Convert bitmap to mat
             Utils.bitmapToMat(resizedBitmap, CamImage);
      		 Utils.bitmapToMat(mBitmap2, LogoImage);
      		 
         	 //Color for circles
      		 Scalar color1 = new Scalar(0, 255, 0);
      		 Scalar color2 = new Scalar(255,0,0);
      		
      		 //Imgproc.cvtColor(CamImage, rgb1, Imgproc.COLOR_RGBA2RGB);
             //Imgproc.cvtColor(LogoImage, rgb2, Imgproc.COLOR_RGBA2RGB);
             FeatureDetector FAST = FeatureDetector.create(FeatureDetector.ORB);
           
             // extract keypoints
             FAST.detect(CamImage, CamKeypoints);
             FAST.detect(LogoImage, LogoKeypoints);
             
             
             //Color space conversion
             Imgproc.cvtColor(CamImage, rgb1, Imgproc.COLOR_RGBA2RGB);
             Imgproc.cvtColor(LogoImage, rgb2, Imgproc.COLOR_RGBA2RGB);
             
             //Features2d.drawKeypoints(rgb2, LogoKeypoints, o_image1);
             
             DescriptorExtractor extracter = DescriptorExtractor.create(DescriptorExtractor.BRISK);
             
             extracter.compute(rgb1, CamKeypoints, CamDescriptors);
             extracter.compute(rgb2, LogoKeypoints, LogoDescriptors);
             
             DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
             MatOfDMatch matches_tmp = new MatOfDMatch();
             matcher.match(CamDescriptors, LogoDescriptors, matches_tmp);  
             List<DMatch> matches = matches_tmp.toList();
             double max_dist = 0;
             double min_dist = 100;
             int rowCount = matches.size();
             //System.out.println("total matches==" + rowCount);
             for (int i = 0; i < rowCount; i++) {
                double dist = matches.get(i).distance;
                System.out.println("distance dist==" + dist);
                if (dist < min_dist)
                   min_dist = dist;
                if (dist > max_dist)
                   max_dist = dist;
             }
             System.out.println("min dist==" + min_dist);
             System.out.println("max_dist==" + max_dist);
             //int count = 0;
             //int accuracy = 0;
             MatOfDMatch matching = new MatOfDMatch();
             List<DMatch> good_matches = new ArrayList<DMatch>();
             double good_dist = 2 * (min_dist + 1);
             System.out.println("good==" + good_dist);
             for (int i = 0; i < rowCount; i++) {
                if (matches.get(i).distance < good_dist) {
                   good_matches.add(matches.get(i));
                   //count++;
                   System.out.println("good distances==" + matches.get(i).distance);
                   matching.fromList(good_matches);
                }
             }
             
             List<Point> obj = new ArrayList<Point>();
             List<Point> scene = new ArrayList<Point>();
             List<KeyPoint> logoTemp = LogoKeypoints.toList();
             List<KeyPoint> camTemp = CamKeypoints.toList();
             for( int i = 0; i < good_matches.size(); i++ )
             {
               DMatch match = good_matches.get(i);
               obj.add(logoTemp.get(match.trainIdx).pt);
               scene.add(camTemp.get(match.queryIdx).pt);
             }
             
             MatOfPoint2f objPoints = new MatOfPoint2f(obj.toArray(new Point[0]));
             MatOfPoint2f scenePoints  = new MatOfPoint2f(scene.toArray(new Point[0]));
             Mat hmg = Calib3d.findHomography(objPoints, scenePoints, Calib3d.RANSAC, 3);
             //OpenCVTestCase.assertMatEqual(Mat.eye(3, 3, CvType.CV_64F), hmg, 0.001);
             
             List<Point> test = new ArrayList<Point>();
             Point p0 = new Point(0, 0);
             test.add(p0);
             Point p1 = new Point(LogoImage.cols(), 0);
             test.add(p1);
             Point p2 = new Point(LogoImage.cols(), LogoImage.rows());
             test.add(p2);
             Point p3 = new Point(0, LogoImage.rows());
             test.add(p3);
             Mat srcPts = org.opencv.utils.Converters.vector_Point2f_to_Mat(test);
             Mat dstPts = new Mat();
             List<Point> dst = new ArrayList<Point>();
             Core.perspectiveTransform(srcPts, dstPts, hmg);
             org.opencv.utils.Converters.Mat_to_vector_Point(dstPts, dst);
             MatOfByte matchesMask = new MatOfByte();
             Features2d.drawMatches(rgb1, CamKeypoints, rgb2, LogoKeypoints, matching, rgb3, color2, color2, matchesMask, 2);
             Point temp0 = new Point(dst.get(0).x + p1.x, dst.get(0).y + p1.y);
             Point temp1 = new Point(dst.get(1).x + p1.x, dst.get(1).y + p1.y);
             Point temp2 = new Point(dst.get(2).x + p1.x, dst.get(2).y + p1.y);
             Point temp3 = new Point(dst.get(3).x + p1.x, dst.get(3).y + p1.y);
             Core.line(rgb3, temp0, temp1, color1, 10);
             Core.line(rgb3, temp1, temp2, color1, 10);
             Core.line(rgb3, temp2, temp3, color1, 10);
             Core.line(rgb3, temp3, temp0, color1, 10);
             
             Imgproc.cvtColor(rgb3, o_image1, Imgproc.COLOR_RGB2RGBA);
             Bitmap bmp = Bitmap.createBitmap(o_image1.cols(), o_image1.rows(), Bitmap.Config.ARGB_8888);
             Utils.matToBitmap(o_image1, bmp);
             
             imgTakenPhoto.setImageBitmap(bmp);
            
          }
       }
    }
    
    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {        
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();    

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal,int setWidth,int setHeight)
    {  

        Bitmap bmpGrayscale = Bitmap.createBitmap(setWidth, setHeight, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
    
    class btnTakePhotoClicker implements Button.OnClickListener
    {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
               Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
               startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
        }
    }
}
