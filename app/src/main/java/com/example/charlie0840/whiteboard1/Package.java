package com.example.charlie0840.whiteboard1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class Package {

	//@Position(0)
	//@Signature("ay")
	public byte[] pathArray;

	//@Position(1)
	//@Signature("ay")
	public byte[] picArray;

	//@Position(2)
	//@Signature("i")
	public Integer color = new Integer(0);

	//@Position(3)
	//@Signature("d")
	public Float brush = new Float(0);

	//@Position(4)
	//@Signature("s")
	public String sText = "";

	//@Position(5)
	//@Signature("d")
	public Float xAxis = new Float(0);

	//@Position(6)
	//@Signature("d")
	public Float yAxis = new Float(0);

	public String sendId = "";
	public String nickname = "";
	public Integer asked = 0;
	public String groupname = "";


	private boolean reset = false;
	private Bitmap oldpic;
	private byte[] oldPicArray;

	private boolean undo = false;
	private boolean redo = false;

	public Package() {}

	public Package(byte[] path, Integer clr, Float bru, byte[] pic, String text, Float x, Float y, Bitmap oldpic, boolean undo, boolean redo) {
		this.pathArray = path;
		this.color = clr;
		this.brush = bru;
		this.picArray = pic;
		this.sText = text;
		this.xAxis = x;
		this.yAxis = y;
		this.oldpic = oldpic;

		this.undo = undo;
		this.redo = redo;

		if(oldpic != null)
			this.oldPicArray = convertToByte(oldpic);
	}

	public Package(byte[] path, Integer clr, String bru, byte[] pic, String text, String x, String y, Bitmap oldpic, boolean undo, boolean redo) {
		this.pathArray = path;
		this.color = clr;
		this.brush = Float.parseFloat(bru);
		this.picArray = pic;
		this.sText = text;
		this.xAxis = Float.parseFloat(x);
		this.yAxis = Float.parseFloat(y);
		this.oldpic = oldpic;
		this.undo = undo;
		this.redo = redo;
		if(oldpic != null)
			this.oldPicArray = convertToByte(oldpic);
	}

	public Package(String group, byte[] path, Integer clr, String bru, byte[] pic, String text, String x, String y, String send, String nick, Integer ask, boolean reset, byte[] oldPicArray, boolean undo, boolean redo) {
		this.pathArray = path;
		this.color = clr;
		this.brush = Float.parseFloat(bru);
		this.picArray = pic;
		this.sText = text;
		this.xAxis = Float.parseFloat(x);
		this.yAxis = Float.parseFloat(y);
		this.sendId = send;
		this.nickname = nick;
		this.asked = ask;
		this.groupname = group;
		this.reset = reset;
		this.oldPicArray = oldPicArray;
		this.undo = undo;
		this.redo = redo;
	}


	public Package(Package pkg) {
		byte[] path = pkg.getPathArray();
		Integer clr = pkg.getColor();
		Float bru = Float.parseFloat(pkg.getBrush());
		String text = pkg.getText();
		Float x = Float.parseFloat(pkg.getxAxis());
		Float y = Float.parseFloat(pkg.getyAxis());
		String id = pkg.getSendId();
		String nick = pkg.getnick();
		Integer ask = pkg.getask();
		byte[] pic = pkg.getPicArray();
		String group = pkg.getgroupname();
		this.pathArray = path;
		this.color = clr;
		this.brush = bru;
		this.picArray = pic;
		this.sText = text;
		this.xAxis = x;
		this.yAxis = y;
		this.sendId = id;
		this.nickname = nick;
		this.asked = ask;
		this.groupname = group;
		this.reset = pkg.getReset();
		this.undo = pkg.getUndo();
		this.redo = pkg.getRedo();
	}

	public String getgroupname() {
		return groupname;
	}

	public Integer getask() {
		return asked;
	}

	public String getnick() {
		return nickname;
	}

	public String getSendId() {
		return sendId;
	}

	public String getText() {
		return sText;
	}

	public String getxAxis() {
		String x = "";
		if(xAxis != null) {
			x = Float.toString(xAxis);
		} else {
			x = "0";
		}
		return x;
	}

	public String getyAxis() {
		String y = "";
		if(yAxis != null) {
			y = Float.toString(yAxis);
		} else {
			y = "0";
		}
		return y;
	}

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

	public byte[] getPathArray() {
		return pathArray;
	}

	public SerializedPath getPath() throws StreamCorruptedException, IOException, ClassNotFoundException {
		SerializedPath sPath = null;
		if(pathArray!=null && pathArray.length > 0) {
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(pathArray));
			sPath =(SerializedPath) in.readObject();
			in.close();
		}
		return sPath; //fix
	}

	public void setPath(byte[] sp) {
		this.pathArray = sp;
	}

	public Integer getColor() {
		return color;
	}

	public void setPaintMap(Integer clr) {
		this.color = clr;
	}

	public String getBrush() {
		String b = Float.toString(brush);
		return b;
	}

	public void setBrush(Float bru) {
		this.brush = bru;
	}

	public boolean getReset() { return reset; }

	private byte[] convertToByte(Bitmap oldpic){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		oldpic.compress(Bitmap.CompressFormat.JPEG, 50, stream);
		byte[] oldPicArray = stream.toByteArray();
		return oldPicArray;
	}

	public byte[] getOldPicArray() { return oldPicArray; }

	public boolean getUndo() { return undo; }

	public boolean getRedo() { return redo; }

}
