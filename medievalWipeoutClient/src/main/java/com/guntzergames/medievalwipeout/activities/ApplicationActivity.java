package com.guntzergames.medievalwipeout.activities;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.guntzergames.medievalwipeout.beans.Account;
import com.guntzergames.medievalwipeout.beans.CardModel;
import com.guntzergames.medievalwipeout.beans.Packet;
import com.guntzergames.medievalwipeout.interfaces.ClientConstants;
import com.guntzergames.medievalwipeout.interfaces.GameWebClientCallbackable;
import com.guntzergames.medievalwipeout.views.GameView;
import com.guntzergames.medievalwipeout.webclients.GameWebClient;

public class ApplicationActivity extends FragmentActivity implements GameWebClientCallbackable {

	protected GameWebClient gameWebClient;
	protected GameView gameView;
	protected boolean httpRequestBeingExecuted = false, interruptedSignalSent = false;
	protected int currentRequestPriority;
	protected String facebookUserId;
	protected int httpCallsDone = 0, httpCallsAborted = 0;
	
	public GameWebClient getGameWebClient() {
		return gameWebClient;
	}

	public void setGameWebClient(GameWebClient gameWebClient) {
		this.gameWebClient = gameWebClient;
	}

	public GameView getGameView() {
		return gameView;
	}

	public void setGameView(GameView gameView) {
		this.gameView = gameView;
	}

	public boolean isHttpRequestBeingExecuted() {
		return httpRequestBeingExecuted;
	}
	
	public boolean isInterruptedSignalSent() {
		return interruptedSignalSent;
	}

	public void setInterruptedSignalSent(boolean interruptedSignalSent) {
		this.interruptedSignalSent = interruptedSignalSent;
	}

	public int getCurrentRequestPriority() {
		return currentRequestPriority;
	}
	
	public void setCurrentRequestPriority(int currentRequestPriority) {
		this.currentRequestPriority = currentRequestPriority;
	}

	@Override
	public int getHttpCallsDone() {
		return httpCallsDone;
	}

	public void setHttpCallsDone(int httpCallsDone) {
		this.httpCallsDone = httpCallsDone;
	}

	public int getHttpCallsAborted() {
		return httpCallsAborted;
	}

	public void setHttpCallsAborted(int httpCallsAborted) {
		this.httpCallsAborted = httpCallsAborted;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		gameWebClient = new GameWebClient(ClientConstants.SERVER_IP_ADDRESS, this);
		
	}
	
	public void getGame(long gameId) {
		gameWebClient.getGame(gameId);
	}

	@Override
	public void onError(String err) {
		Toast.makeText(this, String.format("Error occured: %s", err), Toast.LENGTH_LONG).show();
	}

	@Override
	public void setHttpRequestBeingExecuted(boolean httpRequestBeingExecuted) {
		this.httpRequestBeingExecuted = httpRequestBeingExecuted;
	}

	@Override
	public String getFacebookUserId() {
		return null;
	}

	@Override
	public void onGetGame(GameView gameView) {
	}

	@Override
	public void onCheckGame(GameView gameView) {
	}

	@Override
	public void onGameJoined(GameView gameView) {
	}

	@Override
	public void onDeleteGame() {
	}

	@Override
	public void onGetAccount(Account account) {
	}

	@Override
	public void onGetCardModels(List<CardModel> cardModels) {
	}

	@Override
	public void onGetGames(List<GameView> gameViews) {
	}

	@Override
	public void onOpenPacket(Packet packet) {
	}

	@Override
	public void onGetVersion(String version) {
	}

}
