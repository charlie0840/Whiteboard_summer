package com.example.charlie0840.whiteboard1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
//import android.util.Log;

public class MainActivity extends Activity implements OnClickListener {

	private DrawingView drawView;
	private float smallBrush, mediumBrush, largeBrush;
	private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, importBtn, redoBtn, undoBtn, blueBtn, textBtn;
	int mWidth, mHeight,mTemp;
	//****************bluetooth variables***************//
	String toastText;
	BluetoothAdapter bluetooth;
	int bluecount = 0;
	// Debugging
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_PAINT_WRITE = 6;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Layout Views
	private TextView mTitle;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	// String buffer for outgoing messages
	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	private MessagePkg mOutMsg = new MessagePkg();
	private ArrayList<MessagePkg> mBluetoothMsg = new ArrayList<MessagePkg>();
	//paint map
	private String value;

	private Bitmap curr;
	private boolean reset = false;
	//private boolean isNew = false;
	//private ArrayList<Object> groupMap = new ArrayList<Object>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		drawView = (DrawingView)findViewById(R.id.drawing);
		drawView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				float x = event.getX();
				float y = event.getY();

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						drawView.touch_start(x, y);
						drawView.invalidate();
						break;
					case MotionEvent.ACTION_MOVE:
						drawView.touch_move(x, y);
						drawView.invalidate();
						break;
					case MotionEvent.ACTION_UP:
						drawView.touch_up(curr, reset);
						drawView.invalidate();
						reset = false;
						if(mChatService!=null) {
							try {
								sendPath();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						break;
				}
				return true;
			}
		}); //added

		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
		smallBrush = getResources().getInteger(R.integer.small_size);
		mediumBrush = getResources().getInteger(R.integer.medium_size);
		largeBrush = getResources().getInteger(R.integer.large_size);
		drawBtn = (ImageButton)findViewById(R.id.draw_btn);
		drawBtn.setOnClickListener(this);
		eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
		eraseBtn.setOnClickListener(this);
		newBtn = (ImageButton)findViewById(R.id.new_btn);
		newBtn.setOnClickListener(this);
		saveBtn = (ImageButton)findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);
		importBtn = (ImageButton)findViewById(R.id.importpic_btn);
		importBtn.setOnClickListener(this);
		undoBtn = (ImageButton)findViewById(R.id.undo_btn);
		undoBtn.setOnClickListener(this);
		redoBtn = (ImageButton)findViewById(R.id.redo_btn);
		redoBtn.setOnClickListener(this);
		blueBtn = (ImageButton)findViewById(R.id.bluetooth_btn);
		blueBtn.setOnClickListener(this);
		textBtn = (ImageButton)findViewById(R.id.text_btn);
		textBtn.setOnClickListener(this);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		if(savedInstanceState!=null){
			mWidth = savedInstanceState.getInt("width");
			mHeight = savedInstanceState.getInt("height");
			Bitmap bitmap = savedInstanceState.getParcelable("bitmap");
			if(bitmap!=null){
				//drawView.addBitmap(bitmap);
			}
		}
	}


	@Override
	public void onClick(View view) {

		if(view.getId()==R.id.draw_btn){

			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Brush size:");
			brushDialog.setContentView(R.layout.brush_chooser);

			ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setBrushSize(smallBrush);
					drawView.setLastBrushSize(smallBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});

			ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setBrushSize(mediumBrush);
					drawView.setLastBrushSize(mediumBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});

			ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setBrushSize(largeBrush);
					drawView.setLastBrushSize(largeBrush);
					drawView.setErase(false);
					brushDialog.dismiss();
				}
			});
			brushDialog.show();
		}
		else if(view.getId()==R.id.erase_btn){
			//switch to erase - choose size
			//view.startAnimation(myAnim);
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Eraser size:");
			brushDialog.setContentView(R.layout.brush_chooser);
			ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(smallBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(mediumBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(largeBrush);
					brushDialog.dismiss();
				}
			});
			brushDialog.show();
		}
		else if(view.getId()==R.id.new_btn){
			//new button
			AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
			//newDialog.setTitle("New Whiteboard");
			newDialog.setMessage("Start new whiteboard?");
			newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					drawView.startNew();
					reset = true;
					//if(mChatService!=null) {
					//	try {
					//		sendPath();
					//	} catch (IOException e) {
					//		e.printStackTrace();
					//	}
					//}
					dialog.dismiss();
				}
			});
			newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
			newDialog.show();
		}
		else if(view.getId()==R.id.save_btn){
			//save drawing
			AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
			saveDialog.setMessage("Save the whiteboard?");
			saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					//save drawing
					String title = "Whiteboard_" + System.currentTimeMillis() + ".jpg";
					File filepath = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/AkyumenWhiteboard");
					Boolean mdir = filepath.exists();
					if(!mdir) {
						mdir = filepath.mkdirs();
					}
					String file = (filepath.getAbsolutePath()+"/" + title);
					drawView.setDrawingCacheEnabled(true);
					ContentValues values = new ContentValues();
					values.put(Media.TITLE, title);
					values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
					values.put(Images.Media.MIME_TYPE, "image/jpeg");
					values.put(Images.Media.DATA, file);

					Bitmap mBitmap = drawView.getDrawingCache();
					Uri imgSaved = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
					try {
						OutputStream outStream = getContentResolver().openOutputStream(imgSaved);
						mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
						outStream.flush();
						outStream.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if((imgSaved!=null) && (mdir!=false)){
						Toast savedToast = Toast.makeText(getApplicationContext(),
								"Whiteboard saved", Toast.LENGTH_SHORT);
						savedToast.show();
					}
					else{
						Toast unsavedToast = Toast.makeText(getApplicationContext(),
								"Whiteboard could not be saved", Toast.LENGTH_SHORT);
						unsavedToast.show();
					}
					drawView.destroyDrawingCache();
				}
			});
			saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
			saveDialog.show();
		}
		else if(view.getId()==R.id.importpic_btn){
			Intent i = new Intent();
			i.setType("image/*");
			i.setAction(Intent.ACTION_GET_CONTENT);

			Intent customChooserIntent = Intent.createChooser(i, "Pick an image");
			startActivityForResult(customChooserIntent, 10);
		}
		else if(view.getId()==R.id.undo_btn){
			drawView.onClickUndo();
		}
		else if(view.getId()==R.id.redo_btn){
			drawView.onClickRedo();
		}
		else if(view.getId()==R.id.bluetooth_btn){
			bluetooth = BluetoothAdapter.getDefaultAdapter();
			try {
				CheckBlueToothState();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
		}


		else if(view.getId()==R.id.text_btn) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setMessage("Enter text to add");
			//PopupWindow.setFocusable(true);

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					value = input.getText().toString();
					drawView.setOnTouchListener(new OnTouchListener() {
						public boolean onTouch(View v, MotionEvent event) {
							float x = event.getX();
							float y = event.getY();
							drawView.addNewText(value, x, y);

							drawView.setOnTouchListener(new OnTouchListener() {
								public boolean onTouch(View v, MotionEvent event) {
									float x = event.getX();
									float y = event.getY();

									switch (event.getAction()) {
										case MotionEvent.ACTION_DOWN:
											drawView.touch_start(x, y);
											drawView.invalidate();
											break;
										case MotionEvent.ACTION_MOVE:
											drawView.touch_move(x, y);
											drawView.invalidate();
											break;
										case MotionEvent.ACTION_UP:
											drawView.touch_up(curr, reset);
											reset = false;
											drawView.invalidate();
											if(mChatService!=null) {
												try {
													sendPath();
													//sendPaint();
												} catch (IOException e) {
													e.printStackTrace();
												}
											}
											break;
									}
									return true;
								}
							}); //added
							return false;
						}
					});

				}
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			});

			alert.show();
		}

	}

	private void CheckBlueToothState() throws IOException{
		if (bluetooth == null){
			toastText = "Bluetooth NOT support";
		}
		else{
			if (bluetooth.isEnabled()){
				if(bluetooth.isDiscovering()){
					toastText = "Bluetooth is currently in device discovery process.";
				}
				else{
					String address = bluetooth.getAddress();
					String name = bluetooth.getName();
					toastText = "Bluetooth is Enabled" + " (" + name + " : " + address + ").";
					//scan or discoverable
					AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
					//newDialog.setTitle("Bluetooth connection");
					newDialog.setMessage("Choose an option for bluetooth connection.");
					newDialog.setPositiveButton("Connect a device", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which){
							Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
							startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
						}
					});
					newDialog.setNegativeButton("Make discoverable", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which){
							ensureDiscoverable();
						}
					});
					newDialog.show();
					//Intent intentOpenBluetoothSettings = new Intent();
					//intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
					//startActivity(intentOpenBluetoothSettings);
					//if(bluetooth.getBondedDevices() != null)
					mChatService = new BluetoothChatService(this, mHandler);
					//sendPath();
				}
			}
			else{
				if(bluecount == 0) {
					toastText = "Bluetooth is NOT Enabled!";
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
					bluecount++;
				}
				else {
					toastText = "Bluetooth is NOT Enabled!";
					bluecount = 0;
				}
			}
		}
	}

	public void paintClicked(View view){
		//use chosen color
		drawView.setErase(false);
		drawView.setBrushSize(drawView.getLastBrushSize());
		if(view!=currPaint){
			//update color
			ImageButton imgView = (ImageButton)view;
			String color = view.getTag().toString();
			drawView.setColor(color);
			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint=(ImageButton)view;
		}
	}

	//added
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}


	private Bitmap getBitmapFromUri(Uri data){
		Bitmap bitmap = null;

		// Starting fetch image from file
		InputStream is=null;
		try {

			is = getContentResolver().openInputStream(data);

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;

			// BitmapFactory.decodeFile(path, options);
			BitmapFactory.decodeStream(is, null, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, mWidth, mHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;

			is = getContentResolver().openInputStream(data);

			bitmap = BitmapFactory.decodeStream(is,null,options);


			if(bitmap==null){
				Toast.makeText(getBaseContext(), "Image is not Loaded",Toast.LENGTH_SHORT).show();
				return null;
			}

			is.close();
		}catch (IOException e) {
			e.printStackTrace();
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		return bitmap;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == 10 && resultCode == RESULT_OK && null != intent) {
			Uri data = intent.getData();
			Bitmap bitmap = getBitmapFromUri(data);
			if(bitmap!=null){
				drawView.addBitmap(bitmap);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				drawView.addByteArray(byteArray);
			}
		}
		if(requestCode == REQUEST_ENABLE_BT){
			try {
				CheckBlueToothState();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(requestCode == REQUEST_CONNECT_DEVICE) {
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = intent.getExtras()
						.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = bluetooth.getRemoteDevice(address);
				// Attempt to connect to the device
				mChatService.connect(device);
			}
		}
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		mWidth = drawView.getWidth();
		mHeight = drawView.getHeight();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		outState.putInt("width", mWidth);
		outState.putInt("height", mHeight);
//		if(drawView.getBitmap()!=null){
//			outState.putParcelable("bitmap", drawView.getBitmap());
//		}

		super.onSaveInstanceState(outState);

	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
	}

	//****************bluetooth methods***************//
	private void ensureDiscoverable() {
		//if(D) Log.d(TAG, "ensure discoverable");
		if (bluetooth.getScanMode() !=
				BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private void sendPath() throws IOException {
		//mBluetoothPaths = drawView.getOutPathArray();
		mBluetoothMsg = drawView.getOutPkg();
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}
		//if(mOutMsg != null) {
		if(mBluetoothMsg.size() > 0) {
			mOutMsg = mBluetoothMsg.get(mBluetoothMsg.size()-1);
			mChatService.write(mOutMsg);
		}
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			drawView.invalidate();
			switch (msg.what) {
				case MESSAGE_STATE_CHANGE:
					switch (msg.arg1) {
						case BluetoothChatService.STATE_CONNECTED:
							mTitle.setText(R.string.title_connected_to);
							mTitle.append(mConnectedDeviceName);
							drawView.startNew();
							break;
						case BluetoothChatService.STATE_CONNECTING:
							mTitle.setText(R.string.title_connecting);
							break;
						case BluetoothChatService.STATE_LISTEN:
						case BluetoothChatService.STATE_NONE:
							mTitle.setText(R.string.title_not_connected);
							break;
					}
					break;
				case MESSAGE_WRITE:

					break;

				case MESSAGE_READ:
					MessagePkg readPkg = (MessagePkg) msg.obj;

					// construct a string from the valid bytes in the buffer
					MessagePkg finReadPkg = new MessagePkg(readPkg);

					drawView.unpackingPkg(finReadPkg);

					break;
				case MESSAGE_DEVICE_NAME:
					// save the connected device's name
					mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
					Toast.makeText(getApplicationContext(), "Connected to "
							+ mConnectedDeviceName, Toast.LENGTH_SHORT).show();
					break;
				case MESSAGE_TOAST:
					Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
							Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};


	@Override
	public void onStart() {
		super.onStart();

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if(bluetooth != null) {
			if (!bluetooth.isEnabled()) {
				Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
				// Otherwise, setup the chat session
			} else {
				if (mChatService == null)
					mChatService = new BluetoothChatService(this, mHandler);
			}
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		//if(D) Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();

	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null) mChatService.stop();
	}


}