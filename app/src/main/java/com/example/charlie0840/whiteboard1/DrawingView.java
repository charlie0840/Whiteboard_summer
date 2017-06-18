package com.example.charlie0840.whiteboard1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
//import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Toast;

public class DrawingView extends View {
	//drawing path
	public SerializedPath drawPath;
	public ArrayList<Object> paths = new ArrayList<Object>();
	public ArrayList<Object> tempPaths = new ArrayList<Object>();
	private ArrayList<Object> undonePaths = new ArrayList<Object>();
	private ArrayList<Object> tempundonePaths = new ArrayList<Object>();
	private ArrayList<String> texts = new ArrayList<String>();
	private Map<String, Float> XMap = new HashMap<String, Float>();
	private Map<String, Float> YMap = new HashMap<String, Float>();
	private Map<SerializedPath, Integer> paintMap = new HashMap<SerializedPath, Integer>();
	private Map<SerializedPath, Float> brushMap = new HashMap<SerializedPath, Float>();
	//drawing and canvas paint
	public Paint drawPaint, canvasPaint;
	//initial color
	private int paintColor = 0xFF660000;
	//canvas
	public Canvas drawCanvas;
	//canvas bitmap
	private Bitmap canvasBitmap, mBitmap;
	private float brushSize, lastBrushSize;
	private boolean erase=false;
	private boolean wb = false;
	private Matrix mMatrix;
	private RectF mSrcRectF, mDestRectF;
	private MessagePkg mPkg = new MessagePkg();
	private Package msg = new Package();
	private ArrayList<Package> mOutMsg = new ArrayList<Package>();
	private ArrayList<MessagePkg> mOutPkg = new ArrayList<MessagePkg>();
	private byte[] picArray;
	private String text;
	private Float textX, textY;

	public DrawingView(Context context) {
		super(context);
	}

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupDrawing();
	}

	public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private void setupDrawing(){
		drawPath = new SerializedPath();
		drawPaint = new Paint();
		drawPaint.setColor(paintColor);
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(20);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		canvasPaint = new Paint();
		canvasPaint.setColor(getResources().getColor(android.R.color.transparent));
		canvasPaint.setAntiAlias(true);

		mMatrix = new Matrix();
		mSrcRectF = new RectF();
		mDestRectF = new RectF();


		//get drawing area setup for interaction
		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;
		drawPaint.setStrokeWidth(brushSize);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		//view given size

		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//draw view
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		float scale = getResources().getDisplayMetrics().density;
		if(mBitmap != null)  {
		}
		int i = 0;
		for(i = 0; i < paths.size(); i ++)
		{
			Object p1 = paths.get(i);

			if (p1.getClass() == SerializedPath.class) {
				if(paintMap.get(p1) == null)
				{
					if (!((SerializedPath) p1).getBool()) {
						paths.remove(p1);
						tempPaths.remove(p1);
					}
				}
				else {
					drawPaint.setColor(paintMap.get(p1));
					drawPaint.setStrokeWidth(brushMap.get(p1));
					if (p1 != null) {
						canvas.drawPath((SerializedPath) p1, drawPaint);
						if (!((SerializedPath) p1).getBool()) {
							paths.remove(p1);
							tempPaths.remove(p1);
						}
					}
				}
			}
			else if (p1.getClass() == TextObject.class) {
				Paint paint = new Paint();
				paint.setColor(Color.BLACK);
				paint.setStyle(Style.FILL);
				paint.setTextSize(20 * scale);
				float px = (((TextObject) p1).x * scale + 0.5f);
				float py = (((TextObject) p1).y * scale + 0.5f);
				canvas.drawText(((TextObject) p1).value, px, py, paint);
			}
			else if (p1.getClass() == Bitmap.class) {
				copyBitmap((Bitmap) p1);
				canvas.drawBitmap((Bitmap) p1, mMatrix, drawPaint);
			}
//			}
		}
	}

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	public void touch_start(float x, float y) {
		float scale = getResources().getDisplayMetrics().density;
		float dx = (x - 0.5f)/scale;
		float dy = (y - 0.5f)/scale;
		undonePaths.clear();
		tempundonePaths.clear();
		drawPath.reset();
		drawPath.newmoveTo(dx, dy, scale);
		paintMap.put(drawPath, paintColor);
		brushMap.put(drawPath, brushSize);
		mX = dx;
		mY = dy;
		invalidate();
	}

	public void touch_move(float x, float y) {
		float scale = getResources().getDisplayMetrics().density;
		float dpx = (x - 0.5f)/scale;
		float dpy = (y - 0.5f)/scale;
		float pmx = mX * scale + 0.5f;
		float pmy = mY * scale + 0.5f;
		float dx = Math.abs(x - pmx);
		float dy = Math.abs(y - pmy);
		float tempX = (x + pmx)/2;
		float tempY = (y + pmy)/2;
		float finalX = (tempX - 0.5f)/scale;
		float finalY = (tempY - 0.5f)/scale;
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			drawPath.newquadTo(mX, mY, finalX, finalY, scale);
			drawPath.setBool(false);
			paths.add(drawPath);
			tempPaths.add(drawPath);

			mX = dpx;
			mY = dpy;
		}
		invalidate();
	}

	public void touch_up(Bitmap bitmap, boolean reset) {
		float scale = getResources().getDisplayMetrics().density;
		drawPath.newlineTo(mX, mY, scale);
		drawCanvas.drawPath(drawPath, drawPaint);
		drawPath.setBool(true);

		paths.add(drawPath);
		tempPaths.add(drawPath);
//		if(msg.getOldPicArray() != null)
//			System.out.println("from msg not nulllllllllllllllllllllllllllllllllllllllll");
//		else
//			System.out.println("from msg is nulllllllllllllllllllllllllllllllllllllllll");
		try {
			if(!wb) {
				packingMsgg(paths, drawPath, paintColor, brushSize, reset);
			}
			else {
				packingWifiPkg(drawPath, paintColor, brushSize, bitmap, false, false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		mOutPkg.add(mPkg);
		mOutMsg.add(msg);
//		if(msg.getOldPicArray() != null)
//			Toast.makeText(getContext(), "from msg not null", Toast.LENGTH_SHORT).show();
//		else
//			Toast.makeText(getContext(), "from msg null", Toast.LENGTH_SHORT).show();

		drawPath = new SerializedPath();
	}

	public void onClickUndo() {
		if (paths.size() > 0) {
			undonePaths.add(paths.remove(paths.size()-1));

			if(tempPaths.size() > 0) {
				tempundonePaths.add(tempPaths.remove(tempPaths.size() - 1));

			}
			invalidate();
		}
	}

	public void onClickRedo() {
		if (undonePaths.size()>0) {

			//Toast.makeText(getContext(), "redo add", Toast.LENGTH_SHORT).show();
			paths.add(undonePaths.remove(undonePaths.size()-1));
			tempPaths.add(tempundonePaths.remove(tempundonePaths.size()-1));

			invalidate();
		}
	}

	public void setColor(String newColor){
		//set color
		invalidate();
		paintColor = Color.parseColor(newColor);
		drawPaint.setColor(paintColor);
	}

	public Integer getColor() { return paintColor; }

	public void setBrushSize(float newSize){
		//update size
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				newSize, getResources().getDisplayMetrics());
		brushSize=pixelAmount;
		drawPaint.setStrokeWidth(brushSize);
	}

	public void setLastBrushSize(float lastSize){
		lastBrushSize=lastSize;
	}
	public float getLastBrushSize(){
		return lastBrushSize;
	}

	public void setErase(boolean isErase){
		//set erase true or false
		erase=isErase;
		if(erase) {
			invalidate();
			paintColor = Color.WHITE;
			drawPaint.setColor(paintColor);
		}
	}

	public void startNew(){
		mBitmap = null;
		picArray = null;
		//drawCanvas = new Canvas(canvasBitmap);
		drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		paths.clear();
		undonePaths.clear();
		tempundonePaths.clear();
		tempPaths.clear();
		texts.clear();
		text = null;
		invalidate();
	}

	public void startNewTest() {
		mBitmap = null;
		picArray = null;
		int i = 0;
		while(paths.size() > 1) {
			if(paths.get(0).getClass() == Bitmap.class) {
				i++;
				if(i == 2)
					continue;
			}
			if(i < 2)
				paths.remove(0);
			else {
				if(paths.size() > 1)
					paths.remove(1);
				else
					break;
			}
		}
		drawCanvas.drawColor(0, PorterDuff.Mode.SRC);
		undonePaths.clear();
		tempundonePaths.clear();
		tempPaths.clear();
		texts.clear();
		text = null;
		invalidate();

	}

	public void addBitmap(Bitmap bitmap){
		mBitmap = bitmap;
		copyBitmap(bitmap);

		//Toast.makeText(getContext(), "addbit add", Toast.LENGTH_SHORT).show();
		paths.add(bitmap);
		tempPaths.add(bitmap);
	}

	public Bitmap getBitmap(){
		return mBitmap;
	}

	public void copyBitmap(Bitmap bitmap) {
		if(bitmap!=null){

			// Setting size of Source Rect
			mSrcRectF.set(0, 0,bitmap.getWidth(),bitmap.getHeight());

			// Setting size of Destination Rect
			mDestRectF.set(0, 0, getWidth(), getHeight());

			// Scaling the bitmap to fit the PaintView
			mMatrix.setRectToRect( mSrcRectF , mDestRectF, ScaleToFit.CENTER);
		}
	}

	private void packingMsgg(ArrayList<Object> tpaths, SerializedPath sp, Integer color, Float brush, boolean reset) throws IOException {
		float scale = getResources().getDisplayMetrics().density;
		mPkg = new MessagePkg(sp, color, brush, picArray, text, textX, textY, scale, reset);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(sp);

		text = "";
		picArray = null;
	}

	private void packingWifiPkg(SerializedPath sp, Integer color, Float brush, Bitmap oldpic, boolean undo, boolean redo) throws IOException {


		float scale = getResources().getDisplayMetrics().density;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(sp);
		byte[] array = outputStream.toByteArray();
//		if(oldpic != null)
//			Toast.makeText(getContext(), "packing not null", Toast.LENGTH_SHORT).show();
//		else
//			System.out.println("packing oldpic nullllllllllllllllllllllllllllllllllllllllll");
		if(textX != null && textY != null) {


			float x = (textX - 0.5f) / scale;
			float y = (textY - 0.5f) / scale;
			msg = new Package(array, color, brush, picArray, text, x, y, oldpic, undo, redo);
			byte[] test = msg.getOldPicArray();

//			if(test == null)
//				System.out.println("packing oldpic with nulllllllllllllllllllllllllllll");

		}
		else
			msg = new Package(array, color, brush, picArray, text, textX, textY, oldpic, undo, redo);
		text = "";
		picArray = null;
	}

	public ArrayList<MessagePkg> getOutPkg() {
		return mOutPkg;
	}

	public Package getOutMsg() {
		if(mOutMsg.size() < 1)
			return null;
		return mOutMsg.get(mOutMsg.size()-1);
	}

	public void unpackingPkg (MessagePkg mpkg) {
		float newScale = getResources().getDisplayMetrics().density;
		float oldScale = mpkg.getScale();

		boolean reset = mpkg.getReset();

		if(reset)
		{
			startNew();
			return;
		}
		SerializedPath inPath = new SerializedPath();
		inPath = mpkg.getPath();
		inPath.setScale(newScale);
		inPath.drawThisPath();
		paths.add(inPath);
		tempPaths.add(inPath);
		Integer inColor = new Integer(mpkg.getColor());
		paintMap.put(inPath, inColor);
		Float inBrush = new Float(mpkg.getBrush());
		brushMap.put(inPath, inBrush);

		if(mpkg.checkNull()) {
			Bitmap inPic = mpkg.getImage();
			addBitmap(inPic);
			picArray = null;
		}

		if(mpkg.getxAxis() != null && mpkg.getyAxis() != null && mpkg.getText() != null) {
			String ttext = mpkg.getText();
			Float x = (mpkg.getxAxis() - 0.5f)/oldScale;
			Float y = (mpkg.getyAxis() - 0.5f)/oldScale;
			float px = x * newScale + 0.5f;
			float py = y * newScale + 0.5f;

			if (ttext != "" && ttext != null)
				addNewText(ttext, px, py);
		}
	}

	public void updateBitmap(Bitmap oldpic, NetworkActivity mActivity) {
		addBitmap(oldpic);
		//drawCanvas.drawBitmap(oldpic, 0, 0, drawPaint);

		mActivity.setIsNew(false);
	}

	public void unpackingMsg (Package mpkg, boolean isNew, NetworkActivity mActivity) throws StreamCorruptedException, IOException, ClassNotFoundException {
		float scale = getResources().getDisplayMetrics().density;
		SerializedPath inPath = mpkg.getPath();
		byte[] oldPicArray = mpkg.getOldPicArray();
		boolean isNull = false;
		if(oldPicArray == null){
			isNull = true;
		}


		if (isNew && !isNull) {
			Bitmap tempOldpic = BitmapFactory.decodeByteArray(oldPicArray, 0, oldPicArray.length);
			Bitmap oldpic = Bitmap.createScaledBitmap(tempOldpic, drawCanvas.getWidth(), drawCanvas.getHeight(), false);
			if(oldpic != null) {
				addBitmap(oldpic);
				//drawCanvas.drawBitmap(oldpic, 0, 0, drawPaint);

				mActivity.setIsNew(false);
			}

		}
		else {
			if (inPath == null) {

			}
			else {
				inPath.setScale(scale);
				inPath.drawThisPath();
				int i = paths.size();
				paths.add(inPath);
				//Toast.makeText(getContext(), "before: " + i + " after: " + paths.size(), Toast.LENGTH_SHORT).show();
				tempPaths.add(inPath);
				invalidate();
			}

			Integer inColor = new Integer(mpkg.getColor());
			paintMap.put(inPath, inColor);
			Float inBrush = Float.parseFloat(mpkg.getBrush());
			brushMap.put(inPath, inBrush);

			if (mpkg.checkNull()) {
				Bitmap inPic = mpkg.getImage();
				addBitmap(inPic);
				picArray = null;
			}
			if (mpkg.getxAxis() != null && mpkg.getyAxis() != null && mpkg.getText() != null) {
				String ttext = mpkg.getText();
				Float x = Float.parseFloat(mpkg.getxAxis());
				Float y = Float.parseFloat(mpkg.getyAxis());
				float px = x * scale + 0.5f;
				float py = y * scale + 0.5f;

				if (ttext != "" && ttext != null)
					addNewText(ttext, px, py);
			}

		}
	}

	public void setBoolean(boolean wb)
	{
		this.wb = wb;
	}

	public void addByteArray(byte[] byteArray) {
		picArray = byteArray;
//		if(picArray != null)
//			Toast.makeText(getContext(), "not null", Toast.LENGTH_SHORT).show();
	}

	public void addNewText(String value, float x, float y) {
		float scale = getResources().getDisplayMetrics().density;
		text = value;
		textX = x;
		textY = y;
		float dx = (x - 0.5f)/scale;
		float dy = (y - 0.5f)/scale;

		TextObject res = new TextObject(dx, dy, value);
		if(!value.equals("") && value != null) {
			paths.add(res);
			tempPaths.add(res);
			invalidate();
		}
	}

	public void setPicArrayFree() {
		picArray = null;
	}

	public ArrayList<Object> getPath()
	{
		return tempPaths;
	}

	public void setPaths(ArrayList<Object> newPaths) {
		paths = new ArrayList<Object>(newPaths);
		tempPaths = new ArrayList<Object>(newPaths);
	}

	public void resetTempPath() {
		while(tempPaths.size() != 0)
		{
			tempPaths.remove(tempPaths.size() - 1);
		}
	}

	public void startPacking(SerializedPath p, Integer color, Float brush, Bitmap bitmap, boolean undo, boolean redo) {
		try {
			packingWifiPkg(p, color, brush, bitmap, undo, redo);
		}
		catch(Exception e)
		{

		}
	}

	public void putOutMsg(Package p) { mOutMsg.add(p); }

	public Package getMsg() { return msg; }
}