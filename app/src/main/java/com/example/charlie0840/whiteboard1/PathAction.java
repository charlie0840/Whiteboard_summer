package com.example.charlie0840.whiteboard1;

public interface PathAction {
	public enum PathActionType {LINE_TO,MOVE_TO, QUAD_TO};
    public PathActionType getType();
    public float getX();
    public float getY();
    public float getX1();
    public float getX2();
    public float getY1();
    public float getY2();
    public float getScale();
    public float setScale(float scale);

}
