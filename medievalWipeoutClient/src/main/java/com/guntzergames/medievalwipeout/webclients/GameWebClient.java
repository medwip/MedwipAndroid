package com.guntzergames.medievalwipeout.webclients;

import android.util.Log;

import com.guntzergames.medievalwipeout.activities.GameActivity;
import com.guntzergames.medievalwipeout.interfaces.GameWebClientCallbackable;
import com.guntzergames.medievalwipeout.services.GameCheckerThread;
import com.guntzergames.medievalwipeout.webclients.OnGetGameWebAsyncResponse.ResponseType;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

public class GameWebClient {
	
	private static final String TAG = "GameWebClient";

	private AsyncHttpClient client = null;
	private OnGetGameWebAsyncResponse onGetGameWebAsyncResponse;
	private GameWebClientCallbackable callbackable = null;
	private String ip = null;
	
	public void get(String url, boolean fullJsonForced, OnGetGameWebAsyncResponse.ResponseType responseType) {

		onGetGameWebAsyncResponse.setResponseType(responseType);
		onGetGameWebAsyncResponse.setUrl(url);
		onGetGameWebAsyncResponse.setFullJsonForced(fullJsonForced);
		
		RequestParams params = new RequestParams();
		params.add("fullJson", Boolean.valueOf(fullJsonForced).toString());
		
		if ( !(callbackable.isHttpRequestBeingExecuted() && responseType.getPriority() < callbackable.getCurrentRequestPriority()) ) {
			Log.i(TAG, String.format("Perf Monitor: GET request sent: %s", url));
			client.get(url,	params, onGetGameWebAsyncResponse);
		}
		else {
			callbackable.onError("Another HTTP request with a highest priority is already begin executed, transaction aborted...");
		}
		
	}

	public GameWebClient(String ip, GameWebClientCallbackable callbackable) {
		client = new AsyncHttpClient();
		this.callbackable = callbackable;
		this.ip = ip;
		onGetGameWebAsyncResponse = new OnGetGameWebAsyncResponse(callbackable, this);
	}

	public GameWebClientCallbackable getCallbackable() {
		return callbackable;
	}

	public void setCallbackable(GameWebClientCallbackable callbackable) {
		this.callbackable = callbackable;
	}

	public void joinGame(String facebookUserId, long deckId) {
		get("http://" + ip + ":8080/MedievalWipeout/rest/game/join/" + facebookUserId + "/" + deckId, true, ResponseType.JOIN_GAME);
	}

	public void getGame(long gameId) {
		get("http://" + ip + ":8080/MedievalWipeout/rest/game/get/" + gameId + "/" + callbackable.getFacebookUserId(), onGetGameWebAsyncResponse.getFullJson(), ResponseType.GET_GAME);
	}

	public void getGame(long gameId, final GameCheckerThread gameCheckerThread) {
		get("http://" + ip + ":8080/MedievalWipeout/rest/game/get/" + gameId + "/" + gameCheckerThread.getFacebookUserId(), onGetGameWebAsyncResponse.getFullJson(), ResponseType.GET_GAME);
	}

	public void getAccount() {
		get("http://" + ip + ":8080/MedievalWipeout/rest/account/get/" + callbackable.getFacebookUserId(), true, ResponseType.GET_ACCOUNT);
	}

	public void getOngoingGames() {
		get("http://" + ip + ":8080/MedievalWipeout/rest/account/getGames/" + callbackable.getFacebookUserId(), true, ResponseType.GET_GAMES);
	}

	public void addDeckTemplateElement(long deckTemplateId, long collectionElementId) {
		get("http://" + ip + ":8080/MedievalWipeout/rest/account/addDeckTemplateElement/" + callbackable.getFacebookUserId() + "/" + deckTemplateId + "/" + collectionElementId, true, ResponseType.GET_ACCOUNT);
	}

	public void openPacket() {
		get("http://" + ip + ":8080/MedievalWipeout/rest/account/openPacket/" + callbackable.getFacebookUserId(), true, ResponseType.OPEN_PACKET);
	}

	public void nextPhase(long gameId) {
		get("http://" + ip + ":8080/MedievalWipeout/rest/game/nextPhase/" + gameId + "/" + callbackable.getFacebookUserId(), false, ResponseType.NEXT_PHASE);
	}

	public void playCard(long gameId, String sourceLayout, long sourceCardId, String destinationLayout, long destinationCardId) {
		get("http://" + ip + ":8080/MedievalWipeout/rest/game/play/" + callbackable.getFacebookUserId() + "/" + gameId + "/" + sourceLayout + "/" + sourceCardId + "/" + destinationLayout + "/" + destinationCardId, false, ResponseType.GET_GAME);
	}
	
	public void playResourceCard(long gameId, long sourceCardId) {
		playCard(gameId, null, sourceCardId, null, -1);
	}

	public void deleteGame(long gameId) {
		get("http://" + ip + ":8080/MedievalWipeout/rest/game/delete/" + gameId, true, ResponseType.DELETE_GAME);
	}
	
	public void drawInitialHand(long gameId, GameActivity mainActivity) {
		get("http://" + ip + ":8080/MedievalWipeout/rest/game/drawInitialHand/" + gameId + "/" + mainActivity.getFacebookUserId(), false, ResponseType.GET_GAME);
	}

	public void getCardModels() {
		get("http://" + ip + ":8080/MedievalWipeout/rest/account/getCardModels", true, ResponseType.GET_CARD_MODELS);
	}

	public void checkGame(long gameId) {
		get("http://" + ip + ":8080/MedievalWipeout/rest/game/get/" + gameId + "/" + callbackable.getFacebookUserId(), onGetGameWebAsyncResponse.getFullJson(), ResponseType.CHECK_GAME);
	}
	
	public void getVersion() {
		get("http://" + ip + ":8080/MedievalWipeout/rest/client/version", true, ResponseType.GET_VERSION);
	}
	
}
