package com.guntzergames.medievalwipeout.interfaces;

import java.util.List;

import com.guntzergames.medievalwipeout.beans.Account;
import com.guntzergames.medievalwipeout.beans.CardModel;
import com.guntzergames.medievalwipeout.beans.Packet;
import com.guntzergames.medievalwipeout.views.GameView;

public interface GameWebClientCallbackable {

	public boolean isHttpRequestBeingExecuted();
	public boolean isInterruptedSignalSent();
	
	public void setHttpRequestBeingExecuted(boolean httpRequestBeingExecuted);
	public int getCurrentRequestPriority();
	public void setCurrentRequestPriority(int currentRequestPriority);
	
	public String getFacebookUserId();
	public int getHttpCallsDone();
	
	public void onError(String err);
	
	public void onGetGame(GameView gameView);
	public void onCheckGame(GameView gameView);
	public void onGameJoined(GameView gameView);
	public void onDeleteGame();
	
	public void onGetAccount(Account account);
	public void onGetCardModels(List<CardModel> cardModels);
	public void onGetGames(List<GameView> gameViews);
	public void onOpenPacket(Packet packet);
	
	public void onGetVersion(String version);
	
}
