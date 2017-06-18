package com.example.charlie0840.whiteboard1;

import java.io.Serializable;

public class ActionQuad implements PathAction, Serializable {
	private static final long serialVersionUID = 7842411908287759802L;
	private float x1, x2, y1, y2,scale;

	public ActionQuad(float x1, float y1, float x2, float y2, float scale){
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
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
		return PathActionType.QUAD_TO;
	}

	@Override
	public float getX() {
		return 0;
	}

	@Override
	public float getY() {
		return 0;
	}

	@Override
	public float getX1() {
		return x1;
	}

	@Override
	public float getX2() {
		return x2;
	}

	@Override
	public float getY1() {
		return y1;
	}

	@Override
	public float getY2() {
		return y2;
	}

}
