package com.example.charlie0840.whiteboard1;

import java.io.Serializable;

public class ActionLine implements PathAction, Serializable {
	private static final long serialVersionUID = -8570990712253608731L;
	private float x,y,scale;

	public ActionLine(float x, float y, float scale){
		this.x = x;
		this.y = y;
		this.scale = scale;
	}

	@Override
	public float getScale() { return scale; }

	@Override
	public float setScale(float scale)
	{
		this.scale = scale;
		return scale;
	}

	@Override
	public PathActionType getType() {
		return PathActionType.LINE_TO;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public float getX1() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getX2() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getY1() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getY2() {
		// TODO Auto-generated method stub
		return 0;
	}
}
