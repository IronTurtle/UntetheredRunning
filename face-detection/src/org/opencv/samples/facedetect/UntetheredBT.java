package org.opencv.samples.facedetect;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;

public class UntetheredBT {
	//Public state variables
	public static final byte FRONT_BUZZ = 0;
	public static final byte LEFT_BUZZ = 1;
	public static final byte RIGHT_BUZZ = 2;
	public static final byte BACK_BUZZ = 3;
	public static final byte NO_BUZZ = 4;
	public static final byte EMERGENCY_BUZZ = 5;
	
	public static final int BT_NOT_CONNECTED = 0;
	public static final int BT_CONNECTED = 1;
	
	//Standard serial port UUID
	private static final UUID UUID_SERIAL_PORT = 
			UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	
	//Member variables
	private final BluetoothAdapter mBTAdapter;
	private final Handler mHandler;
	private Vibrator mVibrator;
	private int mState;
	private UntetheredConnection mUntetheredThread;
	private boolean mAppRunning = true;
	
	public UntetheredBT(Context context, Handler handler)
	{
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = BT_NOT_CONNECTED;
		mHandler = handler;
		
		//Grab the system Vibrator
		mVibrator = (Vibrator) context.getSystemService("vibrator");
	}
	
	//Start the connection process
	public synchronized void start()
	{
		//Make sure BT is enabled
		setupBT();
		
		if(mUntetheredThread != null)
		{
			mUntetheredThread.cancel();
			mUntetheredThread = null;
		}
		
		//Create and start a new UntetheredConnection thread
		mUntetheredThread = new UntetheredConnection(mBTAdapter);
		mUntetheredThread.start();
	}
	
	//Detect if Bluetooth is on, if it isn't turn it on
	private void setupBT()
	{
		//Check if the BT adapter is enabled or not
		if(!mBTAdapter.isEnabled())
		{
			//Enable the adapter without asking 
			mBTAdapter.enable();
			
			//Wait until the BT adapter is all the way On 
			while(mBTAdapter.getState() != BluetoothAdapter.STATE_ON)
				{}
		}
	}
	
	//If something goes wrong with the connection, tell the UI and retry connecting
	private void retryConnection()
	{
		// Send a failure message back to the Activity
        mHandler.obtainMessage(FdActivity.STATUS_NOT_CONNECTED)
        		.sendToTarget();
        
        //Change the state
        mState = BT_NOT_CONNECTED;
        
        //Restart the service to look for the device again
        UntetheredBT.this.start();
	}
	
	//Public method used to send a command from the UI to the separate thread
	public void sendCMD(byte cmd)
	{
		UntetheredConnection uc;
		
		synchronized (this)
		{
			if(mState != BT_CONNECTED) return;
			uc = mUntetheredThread;
		}
		
		if(cmd == FRONT_BUZZ || cmd == EMERGENCY_BUZZ)
		{
			long[] vibePattern = new long[]{200, 200};
			
			//Turn on the phones vibration pattern
			mVibrator.vibrate(vibePattern, 0);
		}
		else
		{
			mVibrator.cancel();
		}
		
		uc.write(cmd);
	}
	
	public synchronized int getBluetoothState() {
		return mState;
	}
	
	//Stop all threads
	public synchronized void stop()
	{
		if(mUntetheredThread != null)
		{
			mUntetheredThread.cancel();
			mUntetheredThread = null;
		}
		
		synchronized (this)
		{
			mAppRunning = false;
		}
		
	}
	
	private class UntetheredConnection extends Thread 
	{
		private BluetoothAdapter mAdapter;
		private BluetoothDevice mmDevice;
		private BluetoothSocket mmSocket;
		private OutputStream mmOutputStream;
		private InputStream mmInputStream;
		private boolean writeThreadActive = false;
		
		public UntetheredConnection(BluetoothAdapter btAdapter)
		{
			mAdapter = btAdapter;
		}
		
		public void run()
		{
			//Get the untetheredBT device
			getBTDevice();
			
			//Make a connection from the phone to the untetheredBT device
			openBTSocket();
		}
		
		//Go through list of paired devices and find the untetheredBT device
		private void getBTDevice()
		{
			//Find the paired untetheredBT device
			Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
			if(pairedDevices.size() > 0)
			{
				for(BluetoothDevice device : pairedDevices)
				{
					//If untetheredBT is found get the device info
					if(device.getName().equals("untetheredBT"))
					{
						mmDevice = device;
						break;
					}
				}
			}
		}
		
		//Attempt to open a connection between the Android and Arduino
		private void openBTSocket()
		{		
			boolean btConnected = false;
			
				//Will try to get a connection, will loop until successful.
				while (!btConnected && mAppRunning) {
					try {
						mmSocket = mmDevice
								.createRfcommSocketToServiceRecord(UUID_SERIAL_PORT);

						mmSocket.connect();

						mmOutputStream = mmSocket.getOutputStream();
						mmInputStream = mmSocket.getInputStream();

						//If this statement is reached, a connection was successfully established
						btConnected = true;
						
					} catch (IOException e) {

						//Close the socket
						try {
							if(mmSocket != null)
							{
								mmSocket.close();
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			
			/*Only go to connected state if the app is still running*/	
			if(mAppRunning)
			{	
			   //Set the BT state to connected
			   mState = BT_CONNECTED;
			
			   //Send a msg to the UI saying that the device has been connected
			   mHandler.obtainMessage(FdActivity.STATUS_CONNECTED)
					   .sendToTarget();
			}
		}
		
		public void cancel()
		{
			try 
			{
				mmSocket.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		public void write(final byte buffer)
		{	
			synchronized (this) 
			{
				/*Can't create another write thread if one is already active*/
				if (writeThreadActive) 
				{
					return;
				}
			}
			
			/*Create a new thread to check for BT feedback so UI remains responsive*/
			(new Thread() 
			{
				public synchronized void run()
				{
					int timer = 0;
						
					/*The thread is in use now*/
					writeThreadActive = true;
					
					try {	
						//Write the data to the Arduino
						mmOutputStream.write(buffer);
							
						/*Remain stuck in this loop until a good msg comes back, timeout, or the app closes*/
						while(mAppRunning)
						{
							//This loop waits until either data is available or a timeout occurs
							while(mmInputStream.available() == 0 && timer < 5)
							{
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
									
								timer++;
							}
								
							//A timeout occurred, close the socket and try to find the device
							if(timer >= 5)
							{
								mmSocket.close();
								retryConnection();
								
								break;
							}
								
							//if the command sent equals what is sent back from the Arduino,
							//data transfer was good.
							if(buffer == mmInputStream.read())
							{	
								break;
							}
							else//Otherwise, rewrite the data and continue looping
							{
								//Write the data to the Arduino
								mmOutputStream.write(buffer);
							}
						}
					} catch (IOException e) {
						try {
							mmSocket.close();
							retryConnection();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				
					/*The thread is about to die will no longer be in use*/
					writeThreadActive = false;
				}
				
			}).start();
		}
	}
}
