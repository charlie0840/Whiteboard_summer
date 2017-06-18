/******************************************************************************
 * Copyright 2013, Qualcomm Innovation Center, Inc.
 *
 *    All rights reserved.
 *    This file is licensed under the 3-clause BSD license in the NOTICE.txt
 *    file for this project. A copy of the 3-clause BSD license is found at:
 *
 *        http://opensource.org/licenses/BSD-3-Clause.
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the license is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the license for the specific language governing permissions and
 *    limitations under the license.
 ******************************************************************************/

package com.example.charlie0840.whiteboard1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.annotation.BusSignalHandler;
import org.alljoyn.cops.peergroupmanager.BusObjectData;
import org.alljoyn.cops.peergroupmanager.PeerGroupListener;
import org.alljoyn.cops.peergroupmanager.PeerGroupManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BusHandler extends Handler {
    private static final String SERVICE_NAME = "org.alljoyn.PeerGroupManagerApp";
    
    private NetworkActivity    mActivity;
    private UIHandler       mUIHandler;
    
    private SimpleService   mSimpleService = new SimpleService();
    private PeerGroupManager  mGroupManager;

    /* These are the messages sent to the BusHandler from the UI. */
    public static final int INIT = 1;
    public static final int CREATE_GROUP = 2;
    public static final int DESTROY_GROUP = 3;
    public static final int JOIN_GROUP = 4;
    public static final int LEAVE_GROUP = 5;
    public static final int DISCONNECT = 10;
    public static final int PING = 13;

    public BusHandler(Looper looper, UIHandler uiHandler, NetworkActivity activity) {
        super(looper);
        mUIHandler = uiHandler;
        mActivity = activity;
    }
    
    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
        case INIT: {
            doInit();
            break;
        }
        case PING: {
            Bundle data = msg.getData();
            String groupName = data.getString("groupName");
            String nick = data.getString("nick");
            byte[] pathArray = data.getByteArray("patharray");
            byte[] picArray = data.getByteArray("picarray");
            Integer color = data.getInt("color");
            String brush = data.getString("brush");
            String text = data.getString("text");
            String xAxis = data.getString("xAxis");
            String yAxis = data.getString("yAxis");
            Integer split = data.getInt("split");
            Integer asked = data.getInt("asked");

            boolean undo = data.getBoolean("undo");
            boolean redo = data.getBoolean("redo");

            byte[] oldPicArray = data.getByteArray("oldPicArray");

            boolean reset = data.getBoolean("reset");
//            if(undo || redo)
//                System.out.println("tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");
//            else
//                System.out.println("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
//            if(groupName == null || asked == null)
//                System.out.println("this is nullllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll");
//            if(nick == null || pathArray == null)
//                System.out.println("this is nullllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll333333333333333333333333333333");
//            if(picArray == null)
//                System.out.println("this is nulllllllllllllllllllllll22222222222222222222222222222222222222222222222222222222222222222222222222222");
//            if(oldPicArray == null)
//                System.out.println("this is nullllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll544444444444444444444");

            doPing(groupName, pathArray, picArray, color, brush, text, xAxis, yAxis, split, groupName, asked, reset, oldPicArray, undo, redo);
            break;
        }
        /*
        case UPDATE_USER: {
        	Bundle user = msg.getData();
        	String groupname = user.getString("groupname");
        	String id = user.getString("id");
        	String username = user.getString("username");
        	doUpdateUser(groupname, id, username);
        }*/
        case DISCONNECT: {
            doDisconnect();
            break;
        }
        default:
            break;
        }
    }

	private void doInit() {
		PeerGroupListener pgListener = new PeerGroupListener() {
        	@Override
            public void foundAdvertisedName(String groupName, short transport) {
                updatePingGroupList();
            }
            

            @Override
            public void lostAdvertisedName(String groupName, short transport) {
                updatePingGroupList();
            }
            
            @Override
            public void groupLost(String groupName) {
                updatePingGroupList();
            }
            
            @Override
            public void peerAdded(String busId, String groupName, int numParticipants){
            	System.out.println(busId);
            	updatePingGroupList();
            }
        };
        
        ArrayList<BusObjectData> busObjects = new ArrayList<BusObjectData>();
        busObjects.add(new BusObjectData(mSimpleService, "/SimpleService"));
        mGroupManager = new PeerGroupManager(SERVICE_NAME, pgListener, busObjects);
        mGroupManager.registerSignalHandlers(this);
        updatePingGroupList();
        //mActivity.importPeerManager(mGroupManager);
    }
    
    private void doDisconnect() {
        if(mGroupManager == null)
            return;
        mGroupManager.cleanup();
        getLooper().quit();
        sendUiMessage(UIHandler.TOAST_MSG, "Disconnected");
    }

    private void doPing(String groupName, byte[] pathArray, byte[] picArray, Integer color, String brush, String text, String xAxis,
                        String yAxis, Integer split, String groupname, Integer asked, boolean reset, byte[] oldPicArray, boolean undo, boolean redo) {
        SimpleInterface simpleInterface = mGroupManager.getSignalInterface(groupName, mSimpleService, SimpleInterface.class);

        try {
            if(simpleInterface != null) {
                simpleInterface.Ping( pathArray, picArray, color, brush, text, xAxis, yAxis, split, groupname, asked, reset, oldPicArray, undo, redo);
            }
        } catch (BusException e) {
            e.printStackTrace();
        }
    }

    /* Helper function to send a message to the UI thread. */
    private void sendUiMessage(int what, Object obj) {
        mUIHandler.sendMessage(mUIHandler.obtainMessage(what, obj));
    }
    
    private void updatePingGroupList() {
    	
        List<String> availableGroups = mGroupManager.listFoundGroups();
        List<String> hostedGroups = mGroupManager.listHostedGroups();
        List<String> joinedGroups = mGroupManager.listJoinedGroups();
        
        availableGroups.removeAll(hostedGroups);
        availableGroups.removeAll(joinedGroups);
        
        String[] availableGroupArray = availableGroups.toArray(new String[0]);
        String[] hostedGroupArray = hostedGroups.toArray(new String[0]);
        String[] joinedGroupArray = joinedGroups.toArray(new String[0]);

        Message msg = mUIHandler.obtainMessage(UIHandler.UPDATE_GROUP_LIST_SPINNER);
        Bundle data = new Bundle();
        
        data.putStringArray("availableGroupList", availableGroupArray);
        data.putStringArray("hostedGroupList", hostedGroupArray);
        data.putStringArray("joinedGroupList", joinedGroupArray);

        msg.setData(data);
        mUIHandler.sendMessage(msg);
    }
    
    public ArrayList<String> listHostedGroups() {
        return mGroupManager.listHostedGroups();
    }
    
    public ArrayList<String> listJoinedGroups() {
        return mGroupManager.listJoinedGroups();
    }
    
    public ArrayList<String> getParticipants(String groupName) {
    	ArrayList<String> peers = mGroupManager.getPeers(groupName);
    	ArrayList<String> participants = new ArrayList<String>();
    	for (String peer : peers) {
            if(peer.contains(mGroupManager.getGroupHostPeerId(groupName)) && peer.contains(mGroupManager.getMyPeerId())) {
            	String mehost = peer + "(Me)(Host)";
            	participants.add(mehost);
            }
            if(peer.contains(mGroupManager.getGroupHostPeerId(groupName)) && !(peer.contains(mGroupManager.getMyPeerId()))) {
            	String host = peer + "(Host)";
            	participants.add(host);
            }
            if(!(peer.contains(mGroupManager.getGroupHostPeerId(groupName))) && peer.contains(mGroupManager.getMyPeerId())) {
            	String me = peer + "(Me)";
            	participants.add(me);
            }
            if(!(peer.contains(mGroupManager.getGroupHostPeerId(groupName))) && !(peer.contains(mGroupManager.getMyPeerId()))) {
            	participants.add(peer);
            }
        }
    	
        return participants;
    }
    
    //int length;
    List<byte[]> merge = new ArrayList<byte[]>();
	byte c[];
	int start = 0;
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
	

    /*
     * Simple class with the empty Ping signal
     */
    class SimpleService implements SimpleInterface, BusObject {
        public void Ping( byte[] pathArray, byte[] picArray, Integer color, String brush, String Str,
                         String xAxis, String yAxis, Integer split, String groupname, Integer asked, boolean reset, byte[] oldPicArray, boolean undo, boolean redo) {
        }
    }
    
    /*
     * Signal Handler for the Ping signal
     */
    @BusSignalHandler(iface = "com.example.charlie0840.whiteboard1.SimpleInterface", signal = "Ping")
    public void Ping(String nick, byte[] pathArray, byte[] picArray, Integer color, String brush, String Str, String xAxis, String yAxis,
                     Integer split, String groupname, Integer asked, boolean reset, byte[] oldPicArray, boolean undo, boolean redo) {


        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
    	if(split > 0) {
    		merge.add(picArray);
    	}
    	
    	if(split == 1) {
    		for(int i = 0; i < merge.size(); i++) {
    			try {
					outputStream.write(merge.get(i));
				} catch (IOException e) {
					e.printStackTrace();
				}
    			c = outputStream.toByteArray( );
    		}
    		Package pkg = new Package(groupname, pathArray, color, brush, c, Str, xAxis, yAxis, mGroupManager.getSenderPeerId(), nick, asked, reset, oldPicArray, undo, redo);
    		sendUiMessage(UIHandler.SEND_MSG, pkg);
    		merge.clear();
    		c = null;
    		outputStream.reset();
    	}

    	else if(split == 0) {
    		Package pkg = new Package(groupname, pathArray, color, brush, picArray, Str, xAxis, yAxis, mGroupManager.getSenderPeerId(), nick, asked, reset, oldPicArray, undo, redo);
    		sendUiMessage(UIHandler.SEND_MSG, pkg);
    		merge.clear();
    		c = null;
    		outputStream.reset();
    	}
    }
}
