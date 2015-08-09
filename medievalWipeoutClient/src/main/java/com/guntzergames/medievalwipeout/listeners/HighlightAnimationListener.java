package com.guntzergames.medievalwipeout.listeners;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class HighlightAnimationListener implements AnimationListener {
	
	private View highlightView;
	
	public HighlightAnimationListener(View highlightView) {
		this.highlightView = highlightView;
	}

	@Override
	public void onAnimationStart(Animation animation) {
		highlightView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		highlightView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

}
