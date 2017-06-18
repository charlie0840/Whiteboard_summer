package com.example.charlie0840.whiteboard1;

public class Peers {
	String name;
	String id;
	boolean box;
	boolean permission = false;
	boolean present = true;

	Peers(String nick, String genId, boolean check, boolean p) {
	    name = nick;
	    id = genId;
	    box = check;
		permission = p;

	}

	public void setPresent(boolean bool)
	{
		present = bool;
	}

	public void setPermission(boolean bool)
	{
		permission = bool;
	}
}
