package com.guntzergames.medievalwipeout.listeners;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.guntzergames.medievalwipeout.activities.GameActivity;

public class GameAnimationListener implements AnimationListener {
	
	private GameActivity gameActivity;
	
	public GameAnimationListener(GameActivity gameActivity) {
		this.gameActivity = gameActivity;
	}

	@Override
	public void onAnimationStart(Animation animation) {
		gameActivity.setBeingModified(true);
		gameActivity.getGameEventLayout().setVisibility(View.VISIBLE);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		gameActivity.setBeingModified(false);
		gameActivity.getGameEventLayout().setVisibility(View.INVISIBLE);
		Log.i("GameActivity", "Updating cards display from listener");
		gameActivity.updateCardsDisplay();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

}
