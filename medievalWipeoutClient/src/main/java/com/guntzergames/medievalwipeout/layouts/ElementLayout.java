package com.guntzergames.medievalwipeout.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guntzergames.medievalwipeout.activities.R;

public class ElementLayout extends LinearLayout {

	private static final String TAG = "ElementLayout";

	private static LayoutInflater layoutInflater;

	private Context context;
	private View root;

	private TextView elementLibelView, elementValueView;
	private ImageView elementImageView;

	public ElementLayout(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public ElementLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}
	
	public void init() {
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		root = (LinearLayout) layoutInflater.inflate(R.layout.element, (ViewGroup) this);
		elementLibelView = (TextView) root.findViewById(R.id.elementLibel);
		elementImageView = (ImageView) root.findViewById(R.id.elementImage);		
		elementValueView = (TextView) root.findViewById(R.id.elementValue);
	}
	
	public void setup(int imageId, int value) {
		
		setup(imageId, String.format("%d", value));
		
	}

	public void setup(int imageId, String value) {
		
		elementImageView.setImageResource(imageId);
		elementLibelView.setVisibility(View.INVISIBLE);
		elementValueView.setText(value);
		
	}
	
	public void setup(String libel, int value) {
		
		setup(libel, String.format("%d", value));
		
	}

	public void setup(String libel, String value) {
		
		elementImageView.setVisibility(View.INVISIBLE);
		elementLibelView.setText(libel);
		elementValueView.setText(value);

	}

}
