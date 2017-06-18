package com.example.charlie0840.whiteboard1;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.example.charlie0840.whiteboard1.PathAction.PathActionType;

import android.graphics.Path;

public class SerializedPath extends Path implements Serializable {
	private static final long serialVersionUID = 2321160187198118046L;

	private ArrayList<PathAction> actions = new ArrayList<PathAction>();

	private boolean bool = false;

	public SerializedPath() {
		super();
	}

	public SerializedPath(SerializedPath sPath) {
		super(sPath);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject();
		//drawThisPath();
	}

	//@Override
	public void newmoveTo(float x, float y, float scale) {
		float px = x * scale + 0.5f;
		float py = y * scale + 0.5f;
		actions.add(new ActionMove(x, y, scale));
		super.moveTo(px, py);

	}

	//@Override
	public void newlineTo(float x, float y, float scale){
		float px = x * scale + 0.5f;
		float py = y * scale + 0.5f;
		actions.add(new ActionLine(x, y, scale));
		super.lineTo(px, py);
	}

	//@Override
	public void newquadTo(float x1, float y1, float x2, float y2, float scale) {
		float px1 = x1 * scale + 0.5f;
		float py1 = y1 * scale + 0.5f;
		float px2 = x2 * scale + 0.5f;
		float py2 = y2 * scale + 0.5f;
		actions.add(new ActionQuad(x1, y1, x2, y2, scale));
		super.quadTo(px1, py1, px2, py2);
	}

	//@Override
	public void reset() {
		super.reset();
	}

	public boolean getBool() { return bool; }

	public void setBool(boolean bool) { this.bool = bool; }

	public void drawThisPath(){
		for(PathAction p : actions){
			float x = p.getX();
			float y = p.getY();
			float x1 = p.getX1();
			float y1 = p.getY1();
			float y2 = p.getY2();
			float x2 = p.getX2();
			float scale = p.getScale();
			float px = x * scale + 0.5f;
			float py = y * scale + 0.5f;
			float px1 = x1 * scale + 0.5f;
			float py1 = y1 * scale + 0.5f;
			float px2 = x2 * scale + 0.5f;
			float py2 = y2 * scale + 0.5f;
			if(p.getType().equals(PathActionType.MOVE_TO)){
				super.moveTo(px, py);
			} else if(p.getType().equals(PathActionType.LINE_TO)){
				super.lineTo(px, py);
			} else if(p.getType().equals(PathActionType.QUAD_TO)){
				super.quadTo(px1, py1, px2, py2);
			}
		}
	}

	public void setScale (float newScale)
	{
		for(PathAction p : actions){
			p.setScale(newScale);
		}
	}
}


