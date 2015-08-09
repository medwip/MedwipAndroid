package com.guntzergames.medievalwipeout.listeners;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.guntzergames.medievalwipeout.activities.GameActivity;
import com.guntzergames.medievalwipeout.activities.R;

public class PlayerChoiceListener implements OnClickListener {

	private static final String TAG = "PlayerChoiceListener";
	
	private GameActivity gameActivity;

	private void playChoice(int resource) {

		gameActivity.getGameWebClient().playResourceCard(gameActivity.getGameId(), resource);

	}

	public PlayerChoiceListener(GameActivity gameActivity) {

		this.gameActivity = gameActivity;
		
	}

	@Override
	public void onClick(View v) {

		int sourceId = 0;
		
		if ( v.getId() == R.id.playerChoiceCard0 ) {
			sourceId = 1;
		}
		if ( v.getId() == R.id.playerChoiceCard1 ) {
			sourceId = 2;
		}
		Log.i(TAG, String.format("Playing resource %d", sourceId));
		
		if ( gameActivity.getGameView().isActivePlayer() ) {
			playChoice(sourceId);
		}
		
	}

}
