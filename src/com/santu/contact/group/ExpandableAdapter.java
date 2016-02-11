package com.santu.contact.group;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandableAdapter extends BaseExpandableListAdapter {

	private LayoutInflater layoutInflater;
	private LinkedHashMap<Item, ArrayList<Item>> groupList;
	private ArrayList<Item> mainGroup;
	private int[] groupStatus;
	@SuppressWarnings("unused")
	private ExpandableListView listView;

	public ExpandableAdapter(Context context, ExpandableListView listView,
			LinkedHashMap<Item, ArrayList<Item>> groupsList) {
		layoutInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		this.groupList = groupsList;
		groupStatus = new int[groupsList.size()];

		listView.setOnGroupExpandListener(new OnGroupExpandListener() {

			public void onGroupExpand(int groupPosition) {
				Item group = mainGroup.get(groupPosition);
				if (groupList.get(group).size() > 0)
					groupStatus[groupPosition] = 1;

			}
		});

		listView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

			public void onGroupCollapse(int groupPosition) {
				Item group = mainGroup.get(groupPosition);
				if (groupList.get(group).size() > 0)
					groupStatus[groupPosition] = 0;

			}
		});

		mainGroup = new ArrayList<Item>();
		for (Map.Entry<Item, ArrayList<Item>> mapEntry : groupList.entrySet()) {
			mainGroup.add(mapEntry.getKey());
		}

	}

	public Item getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		Item item = mainGroup.get(groupPosition);
		return groupList.get(item).get(childPosition);

	}

	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		final ChildHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.group_item, null);
			holder = new ChildHolder();
			holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		} else {
			holder = (ChildHolder) convertView.getTag();
		}
		final Item child = getChild(groupPosition, childPosition);
		holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Item parentGroup = getGroup(groupPosition);
				child.isChecked = isChecked;
				if (isChecked) {
					// child.isChecked =true;
					ArrayList<Item> childList = getChild(parentGroup);
					int childIndex = childList.indexOf(child);
					boolean isAllChildClicked = true;
					for (int i = 0; i < childList.size(); i++) {
						if (i != childIndex) {
							Item siblings = childList.get(i);
							if (!siblings.isChecked) {
								isAllChildClicked = false;
								// if(DataHolder.checkedChilds.containsKey(child.name)==false){
								DataHolder.checkedChilds.put(child.name,
										parentGroup.name);
								// }
								break;
							}
						}
					}

					if (isAllChildClicked) {
						parentGroup.isChecked = true;
						if (!(DataHolder.checkedChilds.containsKey(child.name) == true)) {
							DataHolder.checkedChilds.put(child.name,
									parentGroup.name);
						}
						checkAll = false;
					}

				} else {
					if (parentGroup.isChecked) {
						parentGroup.isChecked = false;
						checkAll = false;
						DataHolder.checkedChilds.remove(child.name);
					} else {
						checkAll = true;
						DataHolder.checkedChilds.remove(child.name);
					}
					// child.isChecked =false;
				}
				notifyDataSetChanged();

			}

		});

		holder.cb.setChecked(child.isChecked);
		holder.title.setText(child.name);
		Log.i("childs are", DataHolder.checkedChilds.toString());
		return convertView;
	}

	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		Item item = mainGroup.get(groupPosition);
		return groupList.get(item).size();
	}

	public Item getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return mainGroup.get(groupPosition);
	}

	public int getGroupCount() {
		// TODO Auto-generated method stub
		return mainGroup.size();
	}

	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getGroupView(final int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		final GroupHolder holder;

		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.group_list, null);
			holder = new GroupHolder();
			holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.label_indicator);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		} else {
			holder = (GroupHolder) convertView.getTag();
		}

		holder.imageView
				.setImageResource(groupStatus[groupPosition] == 0 ? R.drawable.group_down
						: R.drawable.group_up);
		final Item groupItem = getGroup(groupPosition);

		holder.title.setText(groupItem.name);

		holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (checkAll) {
					ArrayList<Item> childItem = getChild(groupItem);
					for (Item children : childItem)
						children.isChecked = isChecked;
				}
				groupItem.isChecked = isChecked;
				notifyDataSetChanged();
				new Handler().postDelayed(new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						if (!checkAll)
							checkAll = true;
					}
				}, 50);

			}

		});
		holder.cb.setChecked(groupItem.isChecked);
		return convertView;
	}

	private boolean checkAll = true;

	private ArrayList<Item> getChild(Item group) {
		return groupList.get(group);
	}

	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	private class GroupHolder {
		public ImageView imageView;
		public CheckBox cb;
		public TextView title;

	}

	private class ChildHolder {
		public TextView title;
		public CheckBox cb;
	}
}
