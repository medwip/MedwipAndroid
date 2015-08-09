package com.guntzergames.medievalwipeout.listeners;

import android.view.View;
import android.view.View.OnClickListener;

import com.guntzergames.medievalwipeout.activities.GameActivity;
import com.guntzergames.medievalwipeout.activities.R;
import com.guntzergames.medievalwipeout.interfaces.CommonConstants;

public class GameResourceListener implements OnClickListener {

	private GameActivity gameActivity;

	public GameResourceListener(GameActivity gameActivity) {
		this.gameActivity = gameActivity;
	}

	private void selectResource(int resource) {

		gameActivity.getGameWebClient().playCard(gameActivity.getGameId(), null, resource, null, 0);

	}

	@Override
	public void onClick(View v) {

		if (gameActivity.getGameView().isActivePlayer()) {
			if (v.getId() == R.id.gameTrade) {
				selectResource(CommonConstants.GAME_RESOURCE_TRADE);
			}
			if (v.getId() == R.id.gameDefense) {
				selectResource(CommonConstants.GAME_RESOURCE_DEFENSE);
			}
			if (v.getId() == R.id.gameFaith) {
				selectResource(CommonConstants.GAME_RESOURCE_FAITH);
			}
			if (v.getId() == R.id.gameAlchemy) {
				selectResource(CommonConstants.GAME_RESOURCE_ALCHEMY);
			}
		}

	}

}
