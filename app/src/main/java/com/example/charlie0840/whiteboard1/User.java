package com.example.charlie0840.whiteboard1;

public class User {
	String id;
	String name;
	boolean permission;
	  

	User(String num, String nick) {
	    id = num;
	    name = nick;
		permission = false;
	}
	public void setPermission(boolean p)
	{
		permission = p;
	}
}
