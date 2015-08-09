package com.guntzergames.medievalwipeout.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.guntzergames.medievalwipeout.beans.Account;
import com.guntzergames.medievalwipeout.beans.DeckTemplate;
import com.guntzergames.medievalwipeout.enums.GameState;
import com.guntzergames.medievalwipeout.interfaces.ClientConstants;
import com.guntzergames.medievalwipeout.services.HomeCheckerThread;
import com.guntzergames.medievalwipeout.utils.VersionUtils;
import com.guntzergames.medievalwipeout.views.GameView;

public class HomeActivity extends ApplicationActivity {

	private static final String TAG = "HomeActivity";

	private LinearLayout layout = null;
	private long gameId, selectedDeckTemplateId;
	private GraphUser user = null;
	private Button createGameButton, editDeckButton;
	private TextView debugTextView, currentVersionView, latestVersionView;
	private ProgressBar loader;
	private int gameCheckAttempts;
	private HomeCheckerThread gameCheckerThread = null;
	private LoginFragment loginFragment;
	private List<DeckTemplate> deckTemplates = new ArrayList<DeckTemplate>();
	private List<GameView> gameViews;
	private ListView deckTemplateListView, gameListView;
	private DeckTemplate selectedDeckTemplate;
	private Account account;

	final private Handler checkGameHandler = new Handler() {

		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);
			debugTextView.setText("gameCheckAttempts: " + gameCheckAttempts++);
			loader.setVisibility(View.VISIBLE);
			loader.setProgress(gameCheckAttempts * 10);
		}
	};

	public static final int MAIN_ACTIVITY = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		layout = (LinearLayout) LinearLayout.inflate(this, R.layout.activity_home, null);

		setContentView(layout);

		// New intent
		Intent intent = getIntent();
		
		// Check if there is a more recent version
		gameWebClient.getVersion();

		editDeckButton = (Button) layout.findViewById(R.id.editDeck);
		debugTextView = (TextView) layout.findViewById(R.id.debug);
		currentVersionView = (TextView) layout.findViewById(R.id.currentVersion);
		latestVersionView = (TextView) layout.findViewById(R.id.latestVersion);
		loader = (ProgressBar) layout.findViewById(R.id.progressBar);
		deckTemplateListView = (ListView) layout.findViewById(R.id.deckTemplatesList);
		gameListView = (ListView) layout.findViewById(R.id.gamesList);

		gameId = intent.getLongExtra(ClientConstants.GAME_ID, 0);
		int gameState = intent.getIntExtra(ClientConstants.GAME_STATE, ClientConstants.GAME_NOT_STARTED);
		Log.i(TAG, String.format("onCreate, gameId=%s", gameId));

		if (gameState == ClientConstants.GAME_IN_PROGRESS) {
			Toast.makeText(this, String.format("Returned to home page from game %s", gameId), Toast.LENGTH_SHORT).show();
		} else if (gameState == ClientConstants.GAME_STOPPED) {
			Toast.makeText(this, String.format("Stopped game %s", gameId), Toast.LENGTH_SHORT).show();
		} else if (gameState == ClientConstants.GAME_NOT_STARTED) {
		} else { 
			onError(String.format("Unknown state... %s", gameId));
		}

		if (savedInstanceState == null) {
			// Add the fragment on initial activity setup
			loginFragment = new LoginFragment(this);
			getSupportFragmentManager().beginTransaction().add(R.id.facebookLoginFragment, loginFragment).commit();
		} else {
			// Or set the fragment from restored state info
			loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.facebookLoginFragment);
			loginFragment.setHomeActivity(this);
		}

		createGameButton = (Button) layout.findViewById(R.id.startGame);
		createGameButton.setEnabled(false);
		editDeckButton.setEnabled(true);

		createGameButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					createGame();
					createGameButton.setEnabled(false);
				} catch (Exception e) {
					displayError(e.getMessage());
				}
			}
		}

		);

		editDeckButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startEditDeckActivity();
			}
		});

		Button preferencesButton = (Button) layout.findViewById(R.id.preferences);
		preferencesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this, ApplicationPreferenceActivity.class);
				startActivity(intent);
			}
		}

		);

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (!(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected())) {
			onError("No access to internet");
			createGameButton.setEnabled(false);
		}

		gameCheckerThread = new HomeCheckerThread(this);
		gameCheckerThread.start();

		deckTemplateListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onDeckSelected(id);
			}

		});

		gameListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onGameViewSelected(id);
			}

		});

	}
	
	public GraphUser getUser() {
		return user;
	}
	
	@Override
	public void onError(String err) {
		super.onError("gameId=" + gameId + " " + err);
	}

	@Override
	public String getFacebookUserId() {
		if (user == null) return null;
		return user.getId();
	}

	public void setUser(GraphUser user) {
		Log.d(TAG, String.format("User connected: %s", user.getName()));
		TextView welcome = (TextView) layout.findViewById(R.id.welcome);
		welcome.setText("Welcome, " + user.getName() + " [id=" + user.getId() + "]!");
		this.user = user;
		this.facebookUserId = user.getId();
		gameWebClient.getAccount();
	}

	public void setDebugText(String debugText) {
		debugTextView.setText(debugText);
	}

	public void createGame() {
		if (gameWebClient != null) {
			gameWebClient.joinGame(user != null ? user.getId() : "No user", selectedDeckTemplate.getId());
		} else {
			displayError("web client is null");
		}
	}

	private void displayError(String str) {
		Toast.makeText(this, str, Toast.LENGTH_LONG).show();
	}

	public void onDeckSelected(long id) {

		Log.i(TAG, String.format("id: %d", id));
		selectedDeckTemplateId = id;
		selectedDeckTemplate = deckTemplates.get((int) id);
		createGameButton.setEnabled(true);
		editDeckButton.setEnabled(true);

	}
	
	public void onGameViewSelected(long id) {

		Log.i(TAG, String.format("id: %d", id));
		GameView gameView = gameViews.get((int) id);
		if ( gameView.getGameState() == GameState.STARTED ) {
			startGameActivity(gameView);
		}
		else {
			onError(String.format("Game %s cannot be joined (not started)", gameView.getId()));
		}

	}
	
	@Override
	public void onDeleteGame() {
		onError(String.format("onDeleteGame not supported in class %s", HomeActivity.class.getSimpleName()));
	}

	@Override
	public void onGetGame(GameView gameView) {
		this.gameView = gameView;
		debugTextView.setText("onGetGame: " + gameView);
	}

	@Override
	public void onGetAccount(Account account) {

		this.account = account;
		updateDeckTemplates();
		
		gameWebClient.getOngoingGames();

	}

	@Override
	public void onGetGames(List<GameView> gameViews) {
		super.onGetGames(gameViews);

		this.gameViews = gameViews;
		updateGameViews();
		
	}

	@Override
	public void onCheckGame(GameView gameView) {
		this.gameView = gameView;
		if (gameView.getGameState() == GameState.WAITING_FOR_JOINER) {
			Message message = checkGameHandler.obtainMessage();
			checkGameHandler.sendMessage(message);
		} else {
			onGameJoined(gameView);
		}
	}
	
	@Override
	public void onGameJoined(GameView gameView) {

		this.gameView = gameView;

		if (gameView != null) {

			switch (gameView.getGameState()) {

			case WAITING_FOR_JOINER:
				gameCheckerThread.setCheckActivated(true);
				break;

			case STARTED:
				Toast.makeText(this, String.format("Just joined game %s", gameView.getId()), Toast.LENGTH_LONG).show();
				gameCheckerThread.setCheckActivated(false);
				startGameActivity(gameView);
				break;

			default:
				Toast.makeText(this, String.format("Unsupported game state %s for game %s", gameView.getGameState(), gameView.getId()), Toast.LENGTH_LONG).show();
				break;

			}

		} else {
			Toast.makeText(this, "Game is null", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onGetVersion(String version) {
		
		try {
			AssetManager assetManager = getAssets();
			InputStream in = assetManager.open("version");
			String currentVersion = VersionUtils.getVersion(in);
			currentVersionView.setText("Current version: " + currentVersion);
			
			// New version detected!
			if ( version.compareTo(currentVersion) > 0 ) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://guntzergames.ddns.net:8080/MedievalWipeout/rest/client/package"));
				latestVersionView.setText(String.format("[Installing new version %s]", version));
				startActivity(intent);
			}
			else {
				latestVersionView.setText(String.format("[No new version available]"));
			}
			
		}
		catch ( IOException ioe ) {
			onError(ioe.getMessage());
		}
			
	}

	public void onWaitingForGameCreation() {
		debugTextView.setText("Looking for an opponent...");
	}

	public void updateDeckTemplates() {

		Log.d(TAG, String.format("updateDeckTemplates"));

		List<HashMap<String, String>> listElements = new ArrayList<HashMap<String, String>>();
		deckTemplates = account.getDeckTemplates();
		if ( deckTemplates == null ) deckTemplates = new ArrayList<DeckTemplate>();

		HashMap<String, String> element;

		for (DeckTemplate deckTemplate : deckTemplates) {

			element = new HashMap<String, String>();
			element.put("id", deckTemplate.getId() + "");
			element.put("libel", deckTemplate.getDeckLibel());
			listElements.add(element);

		}

		ListAdapter adapter = new SimpleAdapter(this, listElements, android.R.layout.simple_list_item_2, new String[] { "id", "libel" }, new int[] { android.R.id.text1,
				android.R.id.text2 });

		deckTemplateListView.setAdapter(adapter);

	}
	
	public void updateGameViews() {
		
		List<HashMap<String, String>> listElements = new ArrayList<HashMap<String, String>>();
		if ( gameViews == null ) gameViews = new ArrayList<GameView>();

		HashMap<String, String> element;
		
		for (GameView currentGameView : gameViews) {

			element = new HashMap<String, String>();
			element.put("id", String.format("%s %s", currentGameView.getId(), currentGameView.getGameState()));
			element.put("libel", String.format("%s %s", currentGameView.getTurn(), currentGameView.getPhase()));
			listElements.add(element);

		}

		ListAdapter adapter = new SimpleAdapter(this, listElements, android.R.layout.simple_list_item_2, new String[] { "id", "libel" }, new int[] { android.R.id.text1,
				android.R.id.text2 });

		gameListView.setAdapter(adapter);
		
	}

	public void startGameActivity(GameView gameView) {
		this.gameView = gameView;
		Intent intent = new Intent(HomeActivity.this, GameActivity.class);
		intent.putExtra(ClientConstants.GAME_ID, gameView.getId());
		intent.putExtra(ClientConstants.GAME_COMMAND, "GAME_START");
		Log.i(TAG, String.format("Starting GameActivity, facebookId=%s and gameId=%s", facebookUserId, gameView.getId()));
		intent.putExtra(ClientConstants.FACEBOOK_USER_ID, facebookUserId);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// startActivityForResult(intent, MAIN_ACTIVITY);
		startActivity(intent);
	}

	public void startEditDeckActivity() {
		Intent intent = new Intent(HomeActivity.this, DeckTemplateActivity.class);
		intent.putExtra(ClientConstants.DECK_TEMPLATE_ID, selectedDeckTemplateId);
		Log.i(TAG, "startEditDeckActivity");
		intent.putExtra(ClientConstants.FACEBOOK_USER_ID, user.getId());
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, String.format("Request code = %s", requestCode));
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		if (requestCode == MAIN_ACTIVITY) {
			Log.d(TAG, String.format("resultCode = %s", resultCode));
			if (/* resultCode == RESULT_OK */true) {
				Toast.makeText(this, String.format("Gamed stopped"), Toast.LENGTH_SHORT).show();
				createGameButton.setEnabled(true);
				debugTextView.setText(String.format("requestCode: %s, resultCode: %s", requestCode, resultCode));
			}
		}
	}

}
