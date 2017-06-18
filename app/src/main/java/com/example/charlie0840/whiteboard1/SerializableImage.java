package com.example.charlie0840.whiteboard1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializableImage implements Serializable {
	private static final long serialVersionUID = 6355841390631811001L; 
	private Bitmap currentImage, readImage;
	byte[] imageByteArray;
	//private final int NO_IMAGE = -1;

	public void setImage(Bitmap mBitmap) {
		currentImage = mBitmap;
	}
	
	public Bitmap getImage() {
		return readImage;
	}
	
	/** Included for serialization - write this layer to the output stream. */
	private void writeObject(ObjectOutputStream out) throws IOException {
		if(currentImage != null) {

	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    currentImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
	    SerializableImage bitmapDataObject = new SerializableImage();     
	    bitmapDataObject.imageByteArray = stream.toByteArray();
	    out.writeObject(bitmapDataObject);
		}
		
	}

	/** Included for serialization - read this object from the supplied input stream. */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int check = in.read();
		if(check != -1) {
	    SerializableImage bitmapDataObject = (SerializableImage)in.readObject();
	    readImage = BitmapFactory.decodeByteArray(bitmapDataObject.imageByteArray, 0, check);
	    setImage(readImage);
		}
	}
}
