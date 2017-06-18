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

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class DialogBuilder {
    public Dialog createSelectGroupDialog(final NetworkActivity activity, final BusHandler busHandler) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.selectgroupdialog);
        
        ArrayAdapter<String> groupListAdapter = new ArrayAdapter<String>(activity, android.R.layout.test_list_item);
        final ListView groupList = (ListView)dialog.findViewById(R.id.groupList);
        groupList.setAdapter(groupListAdapter);
        
        List<String> groups = new ArrayList<String>(busHandler.listHostedGroups());
        List<String> joinedGroups = busHandler.listJoinedGroups();
        
        // Combine the list of Hosted Groups and Joined Groups
        for (String group : joinedGroups) {
            if(!groups.contains(group)) {
                groups.add(group);
            }
        }
        
        //Transfer groups from the ArrayList given by the GroupManager to the local ArrayAdapter
        for (String group : groups) {
            groupListAdapter.add(group);
        }
        groupListAdapter.notifyDataSetChanged();
        
        groupList.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create the get peers dialog after the user selects the group
                String name = groupList.getItemAtPosition(position).toString();
                Bundle args = new Bundle();
                args.putString("groupName", name);
                activity.showDialog(NetworkActivity.DIALOG_GET_PEERS_ID, args);
                /*
                 * Android likes to reuse dialogs for performance reasons.  If
                 * we reuse this one, the list of channels will eventually be
                 * wrong since it can change.  We have to tell the Android
                 * application framework to forget about this dialog completely.
                 */
                activity.removeDialog(NetworkActivity.DIALOG_SELECT_GET_PEER_GROUP_ID);
            }
        });
                        
        Button ok = (Button)dialog.findViewById(R.id.cancelSelectGroupButton);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                activity.removeDialog(NetworkActivity.DIALOG_SELECT_GET_PEER_GROUP_ID);
            }
        });
        
        return dialog;
    }
    
    public Dialog createParticipantsDialog(final NetworkActivity activity, final BusHandler busHandler, String groupName, final UIHandler uiHandler) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.peersdialog);
        
        ArrayList<Peers> peers = new ArrayList<Peers>();
        List<String> peerlist = busHandler.getParticipants(groupName);
        for (String peer : peerlist) {
        	int lastDot = peer.indexOf('(');
        	String nickname;
        	if(lastDot != -1) {
        		nickname = activity.getUsername(peer.substring(0, lastDot));
        	} else {
        		nickname = activity.getUsername(peer);
        	}
  	    	if(peer.contains(activity.getHostId(groupName))) {
  	    		peers.add(new Peers(nickname, peer, true, true));
  	    	} else {
  	    		peers.add(new Peers(nickname, peer, false, false));
  	    	}
        }
        final ListAdapter boxAdapter = new ListAdapter(activity.getApplicationContext(), peers);
        final ListView peersList = (ListView)dialog.findViewById(R.id.peerList);
        peersList.setAdapter(boxAdapter);
        boxAdapter.notifyDataSetChanged();
        
        Button done = (Button)dialog.findViewById(R.id.doneButton);
        done.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                List<Peers> permission = new ArrayList<Peers>();
            	for (Peers p : boxAdapter.getBox()) {
          	      if (p.box){
                      int lastDot = p.id.indexOf('(');
                      String nickname;
                      String id;

          	    	if(lastDot != -1) {
                		id = p.id.substring(0, lastDot);
                	} else {
                		id = p.id;
                	}
                	nickname = activity.getUsername(id);
                    permission.add(new Peers(nickname, id, true, true));
                    activity.exportPermission(permission);
          	      }
          	    }
                activity.removeDialog(NetworkActivity.DIALOG_GET_PEERS_ID);
            }
        });

        Button ok = (Button)dialog.findViewById(R.id.closeButton);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                activity.removeDialog(NetworkActivity.DIALOG_GET_PEERS_ID);
            }
        });
        
        Button ask = (Button)dialog.findViewById(R.id.askButton);
        ask.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	uiHandler.sendMessage(uiHandler.obtainMessage(UIHandler.TOAST_MSG, "Permission asked"));
                activity.removeDialog(NetworkActivity.DIALOG_GET_PEERS_ID);
                activity.sendPermission();
            }
        });
        
        return dialog;
    }

	public Dialog createOptionDialog(final NetworkActivity activity, String role) {
        //Log.i(TAG, "createOptionDialog()");
        boolean who = false;
        if(role.equals("instructor"))
            who = true;
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.optiondialog);

        Button create = (Button) dialog.findViewById(R.id.optionCreateorJoin_btn);
        Button destroy = (Button) dialog.findViewById(R.id.optionDestroyorLeave_btn);
        Button peers = (Button) dialog.findViewById(R.id.optionPeers);
        Button cancel = (Button) dialog.findViewById(R.id.optionCancel);

        if(who) {
            create.setText("Create ");
            destroy.setText("Destroy");
        }
        else {
            create.setText("Join ");
            destroy.setText("Leave");
        }
        final boolean whoinner = who;
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(whoinner) {
                    activity.removeDialog(NetworkActivity.DIALOG_OPTION_ID);
                    activity.showDialog(NetworkActivity.DIALOG_CREATE_GROUP_ID);
                }
                else {
                    activity.removeDialog(NetworkActivity.DIALOG_OPTION_ID);
                    activity.showDialog(NetworkActivity.DIALOG_JOIN_GROUP_ID);
                }
            }
        });

        destroy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(whoinner) {
                    activity.removeDialog(NetworkActivity.DIALOG_OPTION_ID);
                    activity.showDialog(NetworkActivity.DIALOG_DESTROY_GROUP_ID);
                }
                else {
                    activity.removeDialog(NetworkActivity.DIALOG_OPTION_ID);
                    activity.showDialog(NetworkActivity.DIALOG_LEAVE_GROUP_ID);
                }
            }
        });
        
        peers.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	//activity.updatename();
                activity.removeDialog(NetworkActivity.DIALOG_OPTION_ID);
                activity.showDialog(NetworkActivity.DIALOG_SELECT_GET_PEER_GROUP_ID);
            }
        });
        
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                activity.removeDialog(NetworkActivity.DIALOG_OPTION_ID);
            }
        });
        
        return dialog;
    }
    
	
	public Dialog createUsernameDialog(final NetworkActivity activity, final UIHandler uiHandler, final BusHandler mBusHandler) {
        //Log.i(TAG, "createNicknameDialog()");
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.usernamedialog);
        
        final EditText groupNameText = (EditText) dialog.findViewById(R.id.userName);
        
        Button create = (Button) dialog.findViewById(R.id.userok);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = groupNameText.getText().toString();
                
                // Make sure the input is valid
                if(nickname.contains(" ") || nickname.equals("")) {
                    uiHandler.sendMessage(uiHandler.obtainMessage(UIHandler.TOAST_MSG, "Please enter a valid username"));
                    return;
                }
                // Call Create Group
                String userId = activity.getUserId();
                activity.setNickname(userId, nickname);
                // Hide the soft keyboard
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                activity.removeDialog(NetworkActivity.DIALOG_USERNAME_ID);
            }
        });
        
        Button no = (Button) dialog.findViewById(R.id.usercancel);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    uiHandler.sendMessage(uiHandler.obtainMessage(UIHandler.TOAST_MSG, "Please enter a username"));
                    return;
                }
        });
        
        return dialog;
    }

}
