package com.santu.contact.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

public class MainActivity extends Activity {
	
	private LinkedHashMap<Item,ArrayList<Item>> groupList;
	private ExpandableListView expandableListView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initViews();
       
    }
    
    
    private void initViews(){
    	
    	initContactList();
    	expandableListView = (ExpandableListView)findViewById(R.id.expandableListView);
    	ExpandableAdapter adapter = new ExpandableAdapter(this, expandableListView, groupList);
    	expandableListView.setAdapter(adapter);
    	
    }
    private void initContactList(){
    	groupList = new LinkedHashMap<Item,ArrayList<Item>>();
    	
    	ArrayList<Item> groupsList = fetchGroups();
    	Log.i("GroupsListSize",String.valueOf(groupsList.size()));
    	//if(groupList.size()>0){
    		for(Item item:groupsList){
    			Log.i("Item id",item.id);
    			
    			String[] ids = item.id.split(",");
    			ArrayList<Item> groupMembers =new ArrayList<Item>();
    			for(int i=0;i<ids.length;i++){
    				String groupId = ids[i];
    				Log.i("GroupId",groupId);
    				groupMembers.addAll(fetchGroupMembers(groupId));
    			}
    			
    			item.name = item.name +" ("+groupMembers.size()+")";
    			groupList.put(item,groupMembers);
    		}
    	//}
    	
    }
    
    
    
    private ArrayList<Item> fetchGroups(){
    	ArrayList<Item> groupList = new ArrayList<Item>();
    	String[] projection = new String[]{ContactsContract.Groups._ID,ContactsContract.Groups.TITLE};
    	Cursor cursor = getContentResolver().query(ContactsContract.Groups.CONTENT_URI, 
    			projection, null, null, null);
/*    	Cursor cursor = getContentResolver().query(ContactsContract.Groups.CONTENT_URI, 
				new String[]{ContactsContract.Groups._ID,ContactsContract.Groups.TITLE},
				null, null, "UPPER("+ContactsContract.Groups.TITLE+") ASC");*/
    	
    	ArrayList<String> groupTitle = new ArrayList<String>();
    	while(cursor.moveToNext()){
        		Item item = new Item();
        		item.id = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID));
        		//Log.i("Item id",item.id);
        		String groupName = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));
        		
        	    if(groupName.contains("Group:"))
    				groupName = groupName.substring(groupName.indexOf("Group:")+"Group:".length()).trim();
    			
    			if(groupName.contains("Favorite_"))
    				groupName = "Favorite";
    			 
    			if(groupName.contains("Starred in Android") || groupName.contains("My Contacts"))
    				continue;
    			
    			if(groupTitle.contains(groupName)){
    				for(Item group:groupList){
    					if(group.name.equals(groupName)){
    						group.id += ","+item.id;
    						Log.i("group.id",group.id);
    						break;
    					}
    				}
    			}else{
    				groupTitle.add(groupName);
    				item.name = groupName;
    				Log.i("GroupName",groupName);
    				groupList.add(item);
    			}
        		
    	}
    	
    	cursor.close();
    	Collections.sort(groupList,new Comparator<Item>() {

			public int compare(Item item1, Item item2) {
				// TODO Auto-generated method stub
				return item2.name.compareTo(item1.name)<0
						?0:-1;
			}
		});
    	
    	return groupList;
    }
    
    private ArrayList<Item> fetchGroupMembers(String groupId){
    	ArrayList<Item> groupMembers = new ArrayList<Item>();
    	String where =  CommonDataKinds.GroupMembership.GROUP_ROW_ID +"="+groupId
			       +" AND "
			       +CommonDataKinds.GroupMembership.MIMETYPE+"='"
			       +CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE+"'";
    	String[] projection = new String[]{GroupMembership.RAW_CONTACT_ID,Data.DISPLAY_NAME};
    	Cursor cursor = getContentResolver().query(Data.CONTENT_URI, projection, where,null,
    			Data.DISPLAY_NAME+" COLLATE LOCALIZED ASC");
    	while(cursor.moveToNext()){
    		Item item = new Item();
    		item.name = cursor.getString(cursor.getColumnIndex(Data.DISPLAY_NAME));
    		item.id = cursor.getString(cursor.getColumnIndex(GroupMembership.RAW_CONTACT_ID));
    		Log.i("Item name",item.name);
    		Cursor phoneFetchCursor = getContentResolver().query(Phone.CONTENT_URI,
    				new String[]{Phone.NUMBER,Phone.DISPLAY_NAME,Phone.TYPE},
    				Phone.CONTACT_ID+"="+item.id,null,null);
    		while(phoneFetchCursor.moveToNext()){
    			item.phNo = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(Phone.NUMBER));
    			item.phDisplayName = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(Phone.DISPLAY_NAME));
    			item.phType = phoneFetchCursor.getString(phoneFetchCursor.getColumnIndex(Phone.TYPE));
    		}
    		
    		phoneFetchCursor.close();
    		groupMembers.add(item);
    	}
    	
    	cursor.close();
    	return groupMembers;
    }
}