package com.example.charlie0840.whiteboard1;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class MessagePkg implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3523943577706324902L;
	private SerializedPath sPath = new SerializedPath();
	//private SerializableImage sImage = new SerializableImage();
	private byte[] picArray;
	private Integer color = new Integer(0);
	private Float brush = new Float(0);
	private String sText = "";
	private Float xAxis = new Float(0);
	private Float yAxis = new Float(0);
	private float scale;
	private boolean reset = false;
	//private Map<SerializedPath, Integer> paintMap;

	public MessagePkg() {
		this.sPath = sPath;
		//color = Integer.valueOf(0);
		this.color = color;
		this.brush = brush;
		this.picArray = picArray;
		this.sText = sText;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		//this.sImage = sImage;
	}

	public MessagePkg(SerializedPath sp, Integer clr, Float bru, byte[] pic, String text, Float x, Float y, float scale, boolean reset) {
		this.sPath = sp;
		this.color = clr;
		this.brush = bru;
		this.picArray = pic;
		this.sText = text;
		this.xAxis = x;
		this.yAxis = y;
		this.scale = scale;
		this.reset = reset;
	}

	public MessagePkg(SerializedPath sp, Integer clr, Float bru, byte[] pic, String text, Float x, Float y) {

		this.sPath = sp;
		this.color = clr;
		this.brush = bru;
		this.picArray = pic;
		this.sText = text;
		this.xAxis = x;
		this.yAxis = y;

	}

	public MessagePkg(MessagePkg pkg) {
		SerializedPath path = pkg.getPath();
		Integer clr = pkg.getColor();
		Float bru = pkg.getBrush();
		String text = pkg.getText();
		Float x = pkg.getxAxis();
		Float y = pkg.getyAxis();
		//SerializableImage img = pkg.getImage();
		//if(img != null) {
		//	Bitmap bit = img.getImage();
		//	sImage.setImage(bit);
		//}
		byte[] pic = pkg.getPicArray();
		this.sPath = path;
		this.color = clr;
		this.brush = bru;
		this.picArray = pic;
		this.sText = text;
		this.xAxis = x;
		this.yAxis = y;
		this.scale = pkg.getScale();
	}

	public String getText() {
		return sText;
	}

	public Float getxAxis() {
		return xAxis;
	}

	public Float getyAxis() {
		return yAxis;
	}

	/*public SerializableImage getImage() {
		return sImage;
	}

	public void setImage(Bitmap bit) {
		sImage.setImage(bit);
	}*/
	public byte[] getPicArray() {
		return picArray;
	}

	public Bitmap getImage() {
		Bitmap bitmap = null;
		if(picArray!=null && picArray.length > 0) {
			bitmap = BitmapFactory.decodeByteArray(picArray, 0, picArray.length);
		}
		return bitmap;
	}

	public boolean checkNull() {
		return getImage() != null;
	}
	/*
	public boolean checkPath() {
		return getPath() != null;
	}*/

	public SerializedPath getPath() {
		return sPath;
	}

	public void setPath(SerializedPath sp) {
		//sPath.addPath(sp);
		this.sPath = sp;
	}

	public float getScale() { return scale; }

	public Integer getColor() {
		return color;//
	}

	public void setPaintMap(Integer clr) {
		//paintMap.put(sPath, clr);
		//color = Integer.valueOf(clr);
		this.color = clr;
	}

	public Float getBrush() {
		return brush;
	}

	public void setBrush(Float bru) {
		this.brush = bru;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws NotActiveException, IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

	public boolean getReset() { return reset;}

}
