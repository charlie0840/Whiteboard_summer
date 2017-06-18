package com.example.charlie0840.whiteboard1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
	Context ctx;
	LayoutInflater lInflater;
	ArrayList<Peers> objects;

	ListAdapter(Context context, ArrayList<Peers> peers) {
		ctx = context;
		objects = peers;
		lInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return objects.size();
	}

	@Override
	public Object getItem(int position) {
		return objects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = lInflater.inflate(R.layout.item, parent, false);
		}

		Peers p = getPeer(position);

		((TextView) view.findViewById(R.id.tvDescr)).setText(p.name);
		((TextView) view.findViewById(R.id.tvPrice)).setText(p.id + "");

		CheckBox present = (CheckBox) view.findViewById(R.id.available);

		//present.setOnCheckedChangeListener(myPresentChangeList);
//		if(getPeer((Integer) present.getTag()).present)
//			present.setChecked(true);
//		else
//			present.setChecked(false);

		present.setChecked(true);

		CheckBox cbBuy = (CheckBox) view.findViewById(R.id.cbBox);
		cbBuy.setOnCheckedChangeListener(myCheckChangList);
		cbBuy.setTag(position);
		cbBuy.setChecked(p.box);
		return view;
	}

	Peers getPeer(int position) {
		return ((Peers) getItem(position));
	}

	ArrayList<Peers> getBox() {
		ArrayList<Peers> box = new ArrayList<Peers>();
		for (Peers p : objects) {
			if (p.box)
				box.add(p);
		}
		return box;
	}

	OnCheckedChangeListener myCheckChangList = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			getPeer((Integer) buttonView.getTag()).box = isChecked;
		}
	};

//	OnCheckedChangeListener myPresentChangeList = new OnCheckedChangeListener() {
//		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//		{
//			if(getPeer((Integer) buttonView.getTag()).present)
//
//		}
//	};
}
