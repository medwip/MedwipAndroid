package com.guntzergames.medievalwipeout.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.guntzergames.medievalwipeout.activities.R;
import com.guntzergames.medievalwipeout.beans.CollectionElement;
import com.guntzergames.medievalwipeout.layouts.CardLayout;

public class CollectionElementAdapter extends BaseAdapter {

	private static LayoutInflater inflater;

	private Activity activity;
	private List<CollectionElement> collectionElements;
	public Resources res;
	int i = 0;

	public static class ViewHolder {

		public TextView name;
		public TextView numberOfCards;
		public ImageView image;

	}

	public CollectionElementAdapter(Activity a, List<CollectionElement> d, Resources resLocal) {

		activity = a;
		collectionElements = d;
		res = resLocal;

		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	public int getCount() {

		if (collectionElements.size() <= 0)
			return 1;
		return collectionElements.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View vi = convertView;
		ViewHolder holder;

		if (convertView == null) {

			/****** Inflate tabitem.xml file for each row ( Defined below ) *******/
			vi = inflater.inflate(R.layout.collection_element_row, null);

			/****** View Holder Object to contain tabitem.xml file elements ******/

			holder = new ViewHolder();
			holder.name = (TextView) vi.findViewById(R.id.name);
			holder.numberOfCards = (TextView) vi.findViewById(R.id.numberOfCards);
			holder.image = (ImageView) vi.findViewById(R.id.cardLayoutImage);

			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();

		if (collectionElements.size() <= 0) {
			holder.name.setText("No Data");

		} else {
			CollectionElement collectionElement = null;
			collectionElement = (CollectionElement) collectionElements.get(position);

			/************ Set Model values in Holder elements ***********/

			holder.name.setText(collectionElement.getName());
			holder.numberOfCards.setText(String.format("%s", collectionElement.getNumberOfCards()));
			try {
				holder.image.setImageDrawable(res.getDrawable(CardLayout.getResourceFromName("card_" + collectionElement.getDrawableResourceName())));
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // res.getIdentifier("com.androidexample.customlistview:drawable/"
				// + tempValues.getImage(), null, null));

		}
		return vi;
	}

}