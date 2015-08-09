package com.guntzergames.medievalwipeout.services;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.guntzergames.medievalwipeout.activities.GameActivity;
import com.guntzergames.medievalwipeout.views.GameView;

public class GameCheckerThread extends Thread {
	
	private static final String TAG = "GameCheckerThread";
	
	private Handler checkGameHandler;
	private long gameId;
	private GameActivity gameActivity;
	private boolean interruptedSignalSent = false, paused = false;

	public boolean isInterruptedSignalSent() {
		return interruptedSignalSent;
	}

	public void setInterruptedSignalSent(boolean interruptedSignalSent) {
		this.interruptedSignalSent = interruptedSignalSent;
	}

	public GameCheckerThread(Handler checkGameHandler, long gameId, GameActivity gameActivity) {
		this.checkGameHandler = checkGameHandler;
		this.gameId = gameId;
		this.gameActivity = gameActivity;
	}

	public void getGame(long gameId) {
		Log.d(TAG, String.format("Get game resource for gameId %s", gameId));
		gameActivity.getGameWebClient().getGame(gameId, this);
	}
	
	public void onGetGame(GameView game) {
		Message message = checkGameHandler.obtainMessage();
		message.obj = game;
		Log.d(TAG, String.format("Sending message to MainActivity for game %s", game));
		checkGameHandler.sendMessage(message);
	}
	
	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public GameActivity getGameActivity() {
		return gameActivity;
	}

	public void setGameActivity(GameActivity gameActivity) {
		this.gameActivity = gameActivity;
	}
	
	public String getFacebookUserId() {
		return gameActivity.getFacebookUserId();
	}

	@Override
	public void run() {
		
		while ( !interruptedSignalSent ) {
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Log.e(TAG, String.format("Error occured while sleeping: %s", e.getMessage()));
			}
			
			if ( !interruptedSignalSent && !paused && !gameActivity.isHttpRequestBeingExecuted() && !gameActivity.isBeingModified() ) {
				getGame(gameId);
			}
			
		}
		
	}

}
