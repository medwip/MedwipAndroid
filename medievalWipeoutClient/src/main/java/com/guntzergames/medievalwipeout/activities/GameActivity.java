package com.guntzergames.medievalwipeout.activities;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import android.R.layout;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.guntzergames.medievalwipeout.abstracts.AbstractCard;
import com.guntzergames.medievalwipeout.abstracts.AbstractCardList;
import com.guntzergames.medievalwipeout.beans.GameEvent;
import com.guntzergames.medievalwipeout.beans.GameEvent.PlayerType;
import com.guntzergames.medievalwipeout.beans.GameEventDeckCard;
import com.guntzergames.medievalwipeout.beans.GameEventPlayCard;
import com.guntzergames.medievalwipeout.beans.GameEventPlayCard.EventType;
import com.guntzergames.medievalwipeout.beans.GameEventIncreaseDecrease;
import com.guntzergames.medievalwipeout.beans.GameEventResourceCard;
import com.guntzergames.medievalwipeout.beans.Player;
import com.guntzergames.medievalwipeout.beans.PlayerDeckCard;
import com.guntzergames.medievalwipeout.beans.PlayerFieldCard;
import com.guntzergames.medievalwipeout.beans.PlayerFieldCard.Location;
import com.guntzergames.medievalwipeout.beans.PlayerHandCard;
import com.guntzergames.medievalwipeout.beans.ResourceDeckCard;
import com.guntzergames.medievalwipeout.enums.CardLocation;
import com.guntzergames.medievalwipeout.enums.Phase;
import com.guntzergames.medievalwipeout.interfaces.ClientConstants;
import com.guntzergames.medievalwipeout.interfaces.ICard;
import com.guntzergames.medievalwipeout.layouts.CardLayout;
import com.guntzergames.medievalwipeout.layouts.PlayerLayout;
import com.guntzergames.medievalwipeout.listeners.GameAnimationListener;
import com.guntzergames.medievalwipeout.listeners.GameDragListener;
import com.guntzergames.medievalwipeout.listeners.GameResourceListener;
import com.guntzergames.medievalwipeout.listeners.HighlightAnimationListener;
import com.guntzergames.medievalwipeout.listeners.PlayerChoiceListener;
import com.guntzergames.medievalwipeout.services.GameCheckerThread;
import com.guntzergames.medievalwipeout.views.GameView;
import com.guntzergames.medievalwipeout.webclients.GameWebClient;

public class GameActivity extends ApplicationActivity {

	private static final String TAG = "GameActivity";

	private RelativeLayout rootLayout = null, playerChoicesLayout;
	private long gameId;
	private String gameCommand;
	private Player player, opponent;
	private String facebookUserId;
	private GameCheckerThread gameCheckerThread;
	private boolean beingModified = false, opponentDefenseDown = false;
	private CardLayout cardLayoutDetail = null;
	private PlayerLayout playerPlayerLayout, opponentPlayerLayout;

	private CardDetailListener cardDetailListener;
	private GameResourceListener gameResourceListener;
	private PlayerChoiceListener playerResourceListener;
	private GameDragListener gameDragListener;

	private GameAnimationListener gameAnimationListener;
	private HighlightAnimationListener highlightAnimationListener;
	private AnimationSet cardEventAnimationSet, highlightAnimationSet, increaseDecreasePrimaryAnimationSet, increaseDecreaseSecondaryAnimationSet;
	private TranslateAnimation cardEventTranslationAnimation, highlightTranslationAnimation, increaseDecreasePrimaryTranslateAnimation,
			increaseDecreaseSecondaryTranslateAnimation;
	private AlphaAnimation highlightAlphaAnimation;

	private LinearLayout playerHandLayout, opponentFieldDefenseLayout, opponentFieldAttackLayout, playerFieldDefenseLayout, playerFieldAttackLayout, highlightLayout;
	private GridLayout gameResourcesLayout;
	private int touchEvents = 0;
	private CardLayout playerChoiceCard1Layout, playerChoiceCard2Layout, gameEventLayout;

	private HorizontalScrollView handScrollView;

	private TextView gameInfos, gameTrade, gameDefense, gameFaith, gameAlchemy, increaseDecreasePrimaryText, increaseDecreaseSecondaryText;

	private Set<View> dragableRegisteredViews = new HashSet<View>();
	private Set<View> targetableRegisteredViews = new HashSet<View>();
	private Set<View> highlightableRegisteredViews = new HashSet<View>();

	private Button nextPhaseButton;

	final private Handler checkGameHandler = new Handler() {

		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);
			GameView game = (GameView) message.obj;
			if (!beingModified) {
				onGetGame(game);
			} else {
				++httpCallsAborted;
			}
		}
	};

	public long getGameId() {
		return gameId;
	}

	public boolean isBeingModified() {
		return beingModified;
	}

	public void setBeingModified(boolean beingModified) {
		this.beingModified = beingModified;
		ImageView beingModifiedImageView = (ImageView) rootLayout.findViewById(R.id.beingModified);
		beingModifiedImageView.setImageDrawable(getResources().getDrawable(beingModified ? R.drawable.red : R.drawable.green));
	}

	private class CardDetailListener implements OnTouchListener {

		private static final String TAG = "CardDetailListener";

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			setBeingModified(true);

			CardLayout cardLayout = ((CardLayout) v);
			ICard card = cardLayout.getCard();

			Log.d(TAG, String.format("onTouch event detected: %s for event %s", event.getAction(), v.getClass().getName()));

			if (event.getAction() == MotionEvent.ACTION_DOWN) {

				cardLayoutDetail.setDetailShown(true);
				cardLayoutDetail.setup(getMainActivity(), card, cardLayout.getSeqNum(), cardLayout.getCardLocation());
				cardLayoutDetail.show();
				// v.setBackgroundColor(getResources().getColor(R.color.com_facebook_blue));
				Log.i(TAG, String.format("Showing %s, touchEvents: %s", card, ++touchEvents));

				v.performClick();

				if (gameView.isActivePlayer()) {

					if ((cardLayout.getCard() instanceof ResourceDeckCard) && gameView.getPhase() == getDuringPlayerResourceChoosePhase()) {
						Log.i(TAG, "Drag initiated during player resource draw");
						ClipData data = ClipData.newPlainText("", "");
						DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(cardLayout);
						cardLayout.startDrag(data, shadowBuilder, cardLayout, 0);
					}

					if ((cardLayout.getCard() instanceof PlayerDeckCard) && gameView.getPhase() == getDuringPlayerDeckDrawPhase()) {
						Log.i(TAG, "Drag initiated during player deck draw");
						ClipData data = ClipData.newPlainText("", "");
						DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(cardLayout);
						cardLayout.startDrag(data, shadowBuilder, cardLayout, 0);
					}

					if ((cardLayout.getCard() instanceof PlayerHandCard) && gameView.getPhase() == getDuringPlayerPlayPhase() && ((PlayerHandCard) card).isPlayable()) {
						Log.i(TAG, "Drag initiated during player play from hand");
						ClipData data = ClipData.newPlainText("", "");
						DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
						v.startDrag(data, shadowBuilder, v, 0);
					}

					if ((cardLayout.getCard() instanceof PlayerFieldCard) && gameView.getPhase() == getDuringPlayerPlayPhase() && !((PlayerFieldCard) card).isPlayed()) {
						Log.i(TAG, "Drag initiated during player play from field");
						ClipData data = ClipData.newPlainText("", "");
						DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
						v.startDrag(data, shadowBuilder, v, 0);
					}

				}

				return true;

			}

			if (event.getAction() == MotionEvent.ACTION_UP) {

				hideCardLayoutDetail();
				// v.setBackgroundColor(getResources().getColor(R.color.red));
				Log.d(TAG, "Hiding " + card);
				setBeingModified(false);
				return true;

			}

			setBeingModified(false);
			return false;

		}

	}

	private Phase getDuringPlayerPlayPhase() {

		return Phase.DURING_PLAY;

	}

	private Phase getDuringPlayerResourceChoosePhase() {

		return Phase.DURING_RESOURCE_CHOOSE;

	}

	private Phase getDuringPlayerResourceSelectPhase() {

		return Phase.DURING_RESOURCE_SELECT;

	}

	private Phase getDuringPlayerDeckDrawPhase() {

		return Phase.DURING_DECK_DRAW;

	}

	protected GameActivity getMainActivity() {
		return this;
	}

	public Set<View> getDragableRegisteredViews() {
		return dragableRegisteredViews;
	}

	public void setDragableRegisteredViews(Set<View> dragableRegisteredViews) {
		this.dragableRegisteredViews = dragableRegisteredViews;
	}

	public void hideCardLayoutDetail() {
		cardLayoutDetail.hide();
	}

	private void initField(LinearLayout layout) {

		for (int i = 0; i < layout.getChildCount(); i++) {

			CardLayout cardInFieldLayout = (CardLayout) layout.getChildAt(i);
			cardInFieldLayout.setOnTouchListener(cardDetailListener);
			cardInFieldLayout.hide();

		}

	}

	private void initOpponentDragListener() {

		int numberofAttackOpponents = opponent.getPlayerFieldAttack().getCards().size();
		int numberofDefenseOpponents = opponent.getPlayerFieldDefense().getCards().size();

		for (int i = 0; i < Math.min(opponentFieldAttackLayout.getChildCount(), numberofAttackOpponents); i++) {

			CardLayout cardInFieldLayout = (CardLayout) opponentFieldAttackLayout.getChildAt(i);
			registerDragListener(cardInFieldLayout);

		}

		if (opponentDefenseDown) {

			for (int i = 0; i < Math.min(opponentFieldDefenseLayout.getChildCount(), numberofDefenseOpponents); i++) {

				CardLayout cardInFieldLayout = (CardLayout) opponentFieldDefenseLayout.getChildAt(i);
				registerDragListener(cardInFieldLayout);

			}
		}

		if (opponentDefenseDown && numberofDefenseOpponents > 0) {
			unregisterDragListener(opponentFieldDefenseLayout);
		} else {
			registerDragListener(opponentFieldDefenseLayout);
		}

	}
	
	private void setupField(AbstractCardList<?> cardList, String layoutPrefix, int numElem, CardLocation cardLocation) {

		int num = cardList.getCards().size();
		for (int i = 0; i < numElem; i++) {

			CardLayout cardLayout = (CardLayout) rootLayout.findViewById(CardLayout.getCardFromId(layoutPrefix, i));
			if (i < num) {
				AbstractCard card = cardList.getCards().get(i);
				cardLayout.setup(this, card, i, cardLocation);
				cardLayout.show();
				Log.d(TAG, "Set view " + cardLayout + " visible " + cardLayout.getVisibility());
			} else {
				cardLayout.hide();
			}
		}

	}

	private void initHighlightLayout() {

		highlightAnimationSet = new AnimationSet(false);
		highlightAnimationSet.setDuration(1500);

		highlightTranslationAnimation = new TranslateAnimation(0, 0, 0, 0);
		highlightTranslationAnimation.setFillAfter(true);
		highlightTranslationAnimation.setFillEnabled(true);
		highlightAnimationSet.addAnimation(highlightTranslationAnimation);

		highlightAlphaAnimation = new AlphaAnimation(0f, .8f);
		highlightAnimationSet.addAnimation(highlightAlphaAnimation);

		highlightAnimationSet.setAnimationListener(highlightAnimationListener);

	}

	private AnimationDrawable getAnimationDrawable(View view, int index) {

		Drawable background = view.getBackground();

		if (background instanceof LayerDrawable) {

			LayerDrawable layerDrawable = (LayerDrawable) background;
			int num = 0;

			for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {

				Drawable drawable = layerDrawable.getDrawable(i);
				if (drawable instanceof AnimationDrawable) {
					AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
					if (num++ == index) {
						return animationDrawable;
					}
				}

			}

		}

		return null;

	}

	public boolean startAnimation(View view, int index) {

		boolean animated = false;

		AnimationDrawable animationDrawable = getAnimationDrawable(view, index);
		if (animationDrawable != null) {
			animationDrawable.setAlpha(255);
			animationDrawable.start();
			animated = true;
		}

		return animated;

	}

	public void stopAnimation(View view, int index) {

		AnimationDrawable animationDrawable = getAnimationDrawable(view, index);
		if (animationDrawable != null) {
			animationDrawable.setAlpha(0);
			animationDrawable.stop();
		}

	}

	public void startHighlightAnimation(View view) {

		startAnimation(view, 0);

	}

	public void stopHightlightAnimation(View view) {

		stopAnimation(view, 0);

	}

	public boolean startTargetAnimation(View view) {

		return startAnimation(view, 1);

	}

	public void startTargetAnimationLayout(LinearLayout layout) {

		for (int i = 0; i < layout.getChildCount(); i++) {
			startTargetAnimation(layout.getChildAt(i));
		}

	}

	public void stopTargetAnimationLayout(ViewGroup layout) {

		for (int i = 0; i < layout.getChildCount(); i++) {
			stopTargetAnimation(layout.getChildAt(i));
		}

	}

	public void stopTargetAnimation(View view) {

		stopAnimation(view, 1);

	}

	private void registerDragListener(View view) {

		view.setOnDragListener(gameDragListener);
		dragableRegisteredViews.add(view);

	}

	private void unregisterDragListener(View view) {

		view.setOnDragListener(null);
		dragableRegisteredViews.remove(view);

	}

	public void updateCardsDisplay() {

		Log.i(TAG, String.format("Entering updateCardsDisplay() [%s], phase=%s", httpCallsDone, gameView.getPhase()));

		if (gameView.isActivePlayer()) {
			player.updatePlayableHandCards();
		}

		setupField(opponent.getPlayerFieldDefense(), "opponentFieldDefense", 5, CardLocation.FIELD_DEFENSE);
		setupField(opponent.getPlayerFieldAttack(), "opponentFieldAttack", 5, CardLocation.FIELD_ATTACK);
		setupField(player.getPlayerFieldDefense(), "playerFieldDefense", 5, CardLocation.FIELD_DEFENSE);
		setupField(player.getPlayerFieldAttack(), "playerFieldAttack", 5, CardLocation.FIELD_ATTACK);
		setupField(player.getPlayerHand(), "playerHand", 10, CardLocation.HAND);

		playerChoiceCard1Layout.hide();
		playerChoiceCard2Layout.hide();
		playerChoicesLayout.setVisibility(View.INVISIBLE);

		for ( View view : targetableRegisteredViews ) {
			stopTargetAnimation(view);
			if ( view instanceof ViewGroup ) {
				stopTargetAnimationLayout((ViewGroup)view);
			}
		}
		for ( View view : highlightableRegisteredViews ) {
			stopHightlightAnimation(view);
		}
		
		Phase phase = gameView.getPhase();

		switch (phase) {
			case DURING_RESOURCE_CHOOSE:
				displayPlayerChoices(gameView.getResourceCard1(), gameView.getResourceCard2(), false);
				break;
			case DURING_DECK_DRAW:
				displayPlayerChoices(player.getPlayerDeckCard1(), player.getPlayerDeckCard2(), !gameView.isActivePlayer());
				break;
			case DURING_RESOURCE_SELECT:
				if (gameView.isActivePlayer()) {
					startHighlightAnimation(gameResourcesLayout);
				}
				break;
			case DURING_PLAY:
				initOpponentDragListener();
				break;
			default:
				onError(String.format("Unsupported phase: %s", phase));
				break;
		}

		Log.i(TAG, String.format("Leaving updateCardsDisplay() [%s]", httpCallsDone));

	}

	private void displayPlayerChoices(ICard cardLayout1, ICard cardLayout2, boolean showBack) {

		playerChoiceCard1Layout.setup(this, cardLayout1, 1, showBack ? CardLocation.BACK : CardLocation.MODAL);
		playerChoiceCard2Layout.setup(this, cardLayout2, 2, showBack ? CardLocation.BACK : CardLocation.MODAL);

		if (gameView.isActivePlayer()) {
			startHighlightAnimation(playerChoiceCard1Layout);
			startHighlightAnimation(playerChoiceCard2Layout);
		}

		playerChoiceCard1Layout.show();
		playerChoiceCard2Layout.show();
		playerChoicesLayout.setVisibility(View.VISIBLE);

	}

	public GameWebClient getGameWebClient() {
		return gameWebClient;
	}

	@Override
	protected void onResume() {

		super.onResume();

		initIntentExtras();
		Log.i(TAG, String.format("onResume, gameId=%s", gameId));
		gameCheckerThread.setPaused(false);
		gameCheckerThread.getGame(gameId);

	}

	@Override
	protected void onNewIntent(Intent intent) {

		super.onNewIntent(intent);
		Log.i(TAG, String.format("onNewIntent, gameId=%s", gameId));

	}

	@Override
	protected void onPause() {

		super.onPause();

		Log.i(TAG, "onPause");
		gameCheckerThread.setPaused(true);

	}
	
	private void registerAnimatedViews(ViewGroup parent, int index, Set<View> registeredViews) {
		
		for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            final View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
            	registerTargetableViews((ViewGroup) child);
            	AnimationDrawable animationDrawable = getAnimationDrawable(child, index);
        		if (animationDrawable != null) {
        			registeredViews.add(child);
        			Log.i(TAG, String.format("Added view %s in targetableRegisteredViews", child));
        		}
            } else {
                if (child != null) {
                	AnimationDrawable animationDrawable = getAnimationDrawable(child, index);
            		if (animationDrawable != null) {
            			registeredViews.add(child);
            			Log.i(TAG, String.format("Added view %s in targetableRegisteredViews", child));
            		}
                }
            }
        }
		
	}
	
	private void registerTargetableViews(ViewGroup parent) {
		
        registerAnimatedViews(parent, 1, targetableRegisteredViews);
        
    }
	
	private void registerHighligthableViews(ViewGroup parent) {
		
        registerAnimatedViews(parent, 0, highlightableRegisteredViews);
        
    }
	
	private void registerViews() {
		
		registerHighligthableViews(rootLayout);
		registerTargetableViews(rootLayout);
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate");

		init();

		cardLayoutDetail = (CardLayout) rootLayout.findViewById(R.id.card_layout_detail);
		cardLayoutDetail.setDetailShown(true);
		hideCardLayoutDetail();
		gameEventLayout = (CardLayout) rootLayout.findViewById(R.id.gameEvent);
		gameEventLayout.setVisibility(View.INVISIBLE);
		opponentPlayerLayout = (PlayerLayout) rootLayout.findViewById(R.id.opponentPlayerLayout);
		playerPlayerLayout = (PlayerLayout) rootLayout.findViewById(R.id.playerPlayerLayout);

		playerHandLayout = (LinearLayout) rootLayout.findViewById(R.id.playerHand);
		playerFieldDefenseLayout = (LinearLayout) rootLayout.findViewById(R.id.playerFieldDefense);
		playerFieldAttackLayout = (LinearLayout) rootLayout.findViewById(R.id.playerFieldAttack);
		opponentFieldDefenseLayout = (LinearLayout) rootLayout.findViewById(R.id.opponentFieldDefense);
		opponentFieldAttackLayout = (LinearLayout) rootLayout.findViewById(R.id.opponentFieldAttack);
		playerChoicesLayout = (RelativeLayout) rootLayout.findViewById(R.id.playerChoices);
		gameResourcesLayout = (GridLayout) rootLayout.findViewById(R.id.gameResources);

		gameTrade = (TextView) rootLayout.findViewById(R.id.gameTrade);
		gameDefense = (TextView) rootLayout.findViewById(R.id.gameDefense);
		gameFaith = (TextView) rootLayout.findViewById(R.id.gameFaith);
		gameAlchemy = (TextView) rootLayout.findViewById(R.id.gameAlchemy);

		increaseDecreasePrimaryText = (TextView) rootLayout.findViewById(R.id.increaseDecreasePrimaryText);
		increaseDecreaseSecondaryText = (TextView) rootLayout.findViewById(R.id.increaseDecreaseSecondaryText);

		cardDetailListener = new CardDetailListener();
		gameResourceListener = new GameResourceListener(this);
		playerResourceListener = new PlayerChoiceListener(this);
		gameDragListener = new GameDragListener(this);

		gameAnimationListener = new GameAnimationListener(this);
		highlightAnimationListener = new HighlightAnimationListener(highlightLayout);

		handScrollView = (HorizontalScrollView) rootLayout.findViewById(R.id.handScrollView);

		initField(playerHandLayout);
		initField(opponentFieldDefenseLayout);
		initField(opponentFieldAttackLayout);
		initField(playerFieldDefenseLayout);
		initField(playerFieldAttackLayout);

		// TODO: Verify if this is still needed
		initHighlightLayout();
		initCardEventAnimation();
		initIncreaseDecreaseAnimation();

		Button stopGameButton = (Button) rootLayout.findViewById(R.id.stopGameButton);
		stopGameButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "Clicked on delete for game " + gameId);
				gameCheckerThread.setInterruptedSignalSent(true);
				gameWebClient.deleteGame(gameId);
			}
		});

		Button homeButton = (Button) rootLayout.findViewById(R.id.homeButton);
		homeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				returnHome(ClientConstants.GAME_IN_PROGRESS);
			}
		});

		Button getGameButton = (Button) rootLayout.findViewById(R.id.getGameButton);
		getGameButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cardLayoutDetail.show();
				setBeingModified(false);
				getGame(gameId);
			}
		});

		nextPhaseButton = (Button) rootLayout.findViewById(R.id.nextPhaseButton);
		nextPhaseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				nextPhase(gameId);
			}
		});

		gameTrade.setOnClickListener(gameResourceListener);
		gameDefense.setOnClickListener(gameResourceListener);
		gameFaith.setOnClickListener(gameResourceListener);
		gameAlchemy.setOnClickListener(gameResourceListener);

		registerDragListener(rootLayout);
		registerDragListener(playerHandLayout);
		registerDragListener(playerFieldDefenseLayout);
		playerFieldDefenseLayout.setOnDragListener(gameDragListener);
		registerDragListener(playerFieldAttackLayout);
		registerDragListener(opponentFieldDefenseLayout);
		registerDragListener(opponentFieldAttackLayout);
		
		registerViews();

		playerChoiceCard1Layout = (CardLayout) rootLayout.findViewById(CardLayout.getCardFromId("playerChoice", 0));
		playerChoiceCard1Layout.setOnClickListener(playerResourceListener);
		playerChoiceCard1Layout.setDetailShown(true);
		playerChoiceCard2Layout = (CardLayout) rootLayout.findViewById(CardLayout.getCardFromId("playerChoice", 1));
		playerChoiceCard2Layout.setOnClickListener(playerResourceListener);
		playerChoiceCard2Layout.setDetailShown(true);

		gameCheckerThread = new GameCheckerThread(checkGameHandler, gameId, this);
		gameCheckerThread.start();
		hideCardLayoutDetail();

		handScrollView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {
					hideCardLayoutDetail();
				}
				v.performClick();

				return false;
			}
		});

	}

	@Override
	protected void onStop() {
		super.onStop();
		gameCheckerThread.setInterruptedSignalSent(true);
	}

	private void initIntentExtras() {

		gameId = getIntent().getExtras().getLong(ClientConstants.GAME_ID);
		gameCommand = getIntent().getExtras().getString(ClientConstants.GAME_COMMAND);
		facebookUserId = getIntent().getExtras().getString(ClientConstants.FACEBOOK_USER_ID);

		Log.i(TAG, String.format("Command %s for game ID: %s, facebookUserId=%s", gameCommand, gameId, facebookUserId));

	}

	private void init() {

		initIntentExtras();

		rootLayout = (RelativeLayout) LinearLayout.inflate(this, R.layout.activity_game, null);

		setContentView(rootLayout);

		getGame(gameId);

	}

	public void returnHome(int status) {
		/*
		 * Intent result = new Intent(); setResult(RESULT_OK, result); finish();
		 */

		setInterruptedSignalSent(true);
		Intent intent = new Intent(GameActivity.this, HomeActivity.class);
		intent.putExtra(ClientConstants.GAME_ID, gameId);
		intent.putExtra(ClientConstants.GAME_STATE, status);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		gameCheckerThread.setInterruptedSignalSent(true);
		finish();
	}

	public String getFacebookUserId() {
		return facebookUserId;
	}

	public void setFacebookUserId(String facebookUserId) {
		this.facebookUserId = facebookUserId;
	}

	public void getGame(long gameId) {
		Log.i(TAG, "Enterring getGame()");
		gameWebClient.getGame(gameId);
		Log.i(TAG, "Leaving getGame()");
	}

	public void nextPhase(long gameId) {
		gameWebClient.nextPhase(gameId);
	}

	private boolean displayEvents() {

		Log.i(TAG, "Enterring displayEvents()");

		boolean handleUpdateDisplay = false;
		LinkedList<GameEvent> events = player.getEvents();
		String str = "Events... ";

		if (events != null && events.size() > 0) {

			for (GameEvent event : events) {

				if (event instanceof GameEventPlayCard) {

					GameEventPlayCard gameEventPlayCard = (GameEventPlayCard) event;
					PlayerDeckCard source = gameEventPlayCard.getSource();
					PlayerDeckCard destination = gameEventPlayCard.getDestination();
					PlayerType type = gameEventPlayCard.getPlayerType();

					Log.i(TAG, String.format("Display event: %s, type=%s, source=%s, destination=%s", gameEventPlayCard, type, source, destination));

					switch (gameView.getPhase()) {

						case DURING_PLAY:

							if (destination instanceof PlayerFieldCard) {

								PlayerFieldCard playerFieldCardDestination = (PlayerFieldCard) destination;
								handleUpdateDisplay = true;

								// Play from hand
								if (source instanceof PlayerHandCard) {

									if (type == PlayerType.PLAYER) {

										CardLayout sourceCardLayout = (CardLayout) rootLayout.findViewById(CardLayout.getCardFromId("playerHand",
												gameEventPlayCard.getSourceIndex()));
										animateCardEvent(sourceCardLayout.getCard(), sourceCardLayout,
												playerFieldCardDestination.getLocation() == Location.ATTACK ? playerFieldAttackLayout : playerFieldDefenseLayout);

									} else {

										animateCardEvent(destination, gameInfos, playerFieldCardDestination.getLocation().equals(Location.DEFENSE) ? opponentFieldDefenseLayout
												: opponentFieldAttackLayout);
									}

								}

								// Play from field
								else if ( source instanceof PlayerFieldCard ) {

									PlayerFieldCard playerFieldCardSource = (PlayerFieldCard) source;
									CardLayout sourceCardLayout = (CardLayout) rootLayout.findViewById(CardLayout.getCardFromId(playerFieldCardSource.getField(type),
											gameEventPlayCard.getSourceIndex()));
									CardLayout destinationCardLayout = (CardLayout) rootLayout.findViewById(CardLayout.getCardFromId(playerFieldCardDestination.getField(type),
											gameEventPlayCard.getDestinationIndex()));
									View destinationView = destinationCardLayout;

									if (gameEventPlayCard.getEventType() == EventType.ATTACK_DEFENSE_FIELD) {
										destinationView = (type == PlayerType.PLAYER ? opponentFieldDefenseLayout : playerFieldDefenseLayout);
									} else if (gameEventPlayCard.getEventType() == EventType.ATTACK_ATTACK_CARD) {
										destinationView = destinationCardLayout;
									}

									Log.i(TAG,
											String.format("playerFieldCard=%s, location=%s, playerFieldCard.getField()=%s", playerFieldCardSource,
													playerFieldCardSource.getLocation(), playerFieldCardSource.getField(type)));

									animateCardEvent(sourceCardLayout.getCard(), sourceCardLayout, destinationView);

								} else {
									str += gameEventPlayCard.toString() + "\n";
								}

							} else {
								str += gameEventPlayCard.toString() + "\n";
							}

							break;

						default:
							onError(String.format("Unhandled phase in displayEvent: %s", gameView.getPhase()));

					}

				}

				else if (event instanceof GameEventResourceCard) {

					GameEventResourceCard gameEventResourceCard = (GameEventResourceCard) event;
					CardLayout cardLayout = gameEventResourceCard.getResourceId() == 1 ? playerChoiceCard1Layout : playerChoiceCard2Layout;
					animateCardEvent(gameEventResourceCard.getResourceDeckCard(), cardLayout, playerFieldAttackLayout);
					handleUpdateDisplay = true;

				}

				else if (event instanceof GameEventDeckCard) {

					GameEventDeckCard gameEventDeckCard = (GameEventDeckCard) event;
					CardLayout cardLayout = gameEventDeckCard.getResourceId() == 1 ? playerChoiceCard1Layout : playerChoiceCard2Layout;
					animateCardEvent(gameEventDeckCard.getPlayerDeckCard(), cardLayout, playerFieldAttackLayout, !gameView.isActivePlayer());
					handleUpdateDisplay = true;

				}

				else if (event instanceof GameEventIncreaseDecrease) {

					GameEventIncreaseDecrease gameEventIncreaseDecrease = (GameEventIncreaseDecrease) event;
					PlayerLayout playerLayout = (gameEventIncreaseDecrease.getPlayerType() == PlayerType.PLAYER ? playerPlayerLayout : opponentPlayerLayout);

					switch (gameEventIncreaseDecrease.getTarget()) {

						case PLAYER_CURRENT_DEFENSE:
							animateIncreaseDecreaseEvent(gameEventIncreaseDecrease, playerLayout.getDefenseLayout(), increaseDecreasePrimaryText,
									increaseDecreasePrimaryAnimationSet);
							break;

						case PLAYER_LIFE_POINTS:
							animateIncreaseDecreaseEvent(gameEventIncreaseDecrease, playerLayout.getPlayerLifePointsLayout(), increaseDecreaseSecondaryText,
									increaseDecreaseSecondaryAnimationSet);
							break;
							
						case PLAYER_CARD_LIFE_POINTS:
							String layoutPrefix = "";
							PlayerDeckCard playerDeckCard = gameEventIncreaseDecrease.getCard();
							
							if ( playerDeckCard instanceof PlayerHandCard ) {
								if ( gameEventIncreaseDecrease.getPlayerType() == PlayerType.PLAYER ) {
									layoutPrefix = "playerHand";
								}
								else {
									layoutPrefix = "opponentHand";
								}
							}
							if ( playerDeckCard instanceof PlayerFieldCard ) {
								PlayerFieldCard playerFieldCard = (PlayerFieldCard)playerDeckCard;
								if ( playerFieldCard.getLocation() == Location.ATTACK ) {
									if ( gameEventIncreaseDecrease.getPlayerType() == PlayerType.PLAYER ) {
										layoutPrefix = "playerFieldAttack";
									}
									else {
										layoutPrefix = "opponentFieldAttack";
									}
								}
								else {
									if ( gameEventIncreaseDecrease.getPlayerType() == PlayerType.PLAYER ) {
										layoutPrefix = "playerFieldDefense";
									}
									else {
										layoutPrefix = "opponentFieldDefense";
									}
								}
							}
							
							CardLayout cardLayout = (CardLayout) rootLayout.findViewById(CardLayout.getCardFromId(layoutPrefix, gameEventIncreaseDecrease.getIndex()));
							animateIncreaseDecreaseEvent(gameEventIncreaseDecrease,
									cardLayout,
									increaseDecreasePrimaryText,
									increaseDecreasePrimaryAnimationSet);
							break;

						default:
							break;

					}

				}

				else {

					onError(String.format("Unhandled event class %s", event.getClass()));

				}

			}

			Toast.makeText(this, str, Toast.LENGTH_SHORT).show();

		}

		Log.i(TAG, "Leaving displayEvents()");

		return handleUpdateDisplay;

	}

	private void initCardEventAnimation() {

		cardEventAnimationSet = (AnimationSet) AnimationUtils.loadAnimation(this, R.anim.card_animation);
		cardEventAnimationSet.setDuration(1000);
		cardEventAnimationSet.setFillAfter(false);
		cardEventAnimationSet.setFillEnabled(true);

		cardEventTranslationAnimation = new TranslateAnimation(0, 0, 0, 0);
		cardEventAnimationSet.setAnimationListener(gameAnimationListener);
		cardEventAnimationSet.addAnimation(cardEventTranslationAnimation);

	}

	private void initIncreaseDecreaseAnimation() {

		increaseDecreasePrimaryAnimationSet = (AnimationSet) AnimationUtils.loadAnimation(this, R.anim.card_animation);
		increaseDecreasePrimaryAnimationSet.setDuration(3000);
		increaseDecreasePrimaryAnimationSet.setFillAfter(false);
		increaseDecreasePrimaryAnimationSet.setFillEnabled(true);

		increaseDecreasePrimaryTranslateAnimation = new TranslateAnimation(0, 0, 0, 0);
		increaseDecreasePrimaryAnimationSet.setAnimationListener(gameAnimationListener);
		increaseDecreasePrimaryAnimationSet.addAnimation(increaseDecreasePrimaryTranslateAnimation);

		increaseDecreaseSecondaryAnimationSet = (AnimationSet) AnimationUtils.loadAnimation(this, R.anim.card_animation);
		increaseDecreaseSecondaryAnimationSet.setDuration(3000);
		increaseDecreaseSecondaryAnimationSet.setFillAfter(false);
		increaseDecreaseSecondaryAnimationSet.setFillEnabled(true);

		increaseDecreaseSecondaryTranslateAnimation = new TranslateAnimation(0, 0, 0, 0);
		increaseDecreaseSecondaryAnimationSet.setAnimationListener(gameAnimationListener);
		increaseDecreaseSecondaryAnimationSet.addAnimation(increaseDecreaseSecondaryTranslateAnimation);

	}

	private void animateCardEvent(ICard card, View sourceLayout, View destinationLayout) {

		animateCardEvent(card, sourceLayout, destinationLayout, false);

	}

	private void animateCardEvent(ICard card, View sourceLayout, View destinationLayout, boolean showBack) {

		int[] sourceCoordinates = new int[2];
		sourceLayout.getLocationInWindow(sourceCoordinates);
		int[] destinationCoordinates = new int[2];
		destinationLayout.getLocationInWindow(destinationCoordinates);
		int[] gameEventLayoutCoordinates = new int[2];
		gameEventLayout.getLocationInWindow(gameEventLayoutCoordinates);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		cardEventAnimationSet.getAnimations().set(
				0,
				new TranslateAnimation(Animation.ABSOLUTE, sourceCoordinates[0] /*
																				 * metrics
																				 * .
																				 * widthPixels
																				 * /
																				 * 2
																				 */- gameEventLayoutCoordinates[0], Animation.ABSOLUTE, destinationCoordinates[0]
						- gameEventLayoutCoordinates[0], Animation.ABSOLUTE, sourceCoordinates[1] - gameEventLayoutCoordinates[1], Animation.ABSOLUTE, destinationCoordinates[1]
						- gameEventLayoutCoordinates[1]));

		gameEventLayout.setup(this, card, 0, showBack ? CardLocation.BACK : CardLocation.ANIMATION);
		gameEventLayout.startAnimation(cardEventAnimationSet);

	}

	private void animateIncreaseDecreaseEvent(GameEventIncreaseDecrease event, View layout, TextView increaseDecreaseText, AnimationSet animationSet) {

		int[] sourceCoordinates = new int[2];
		layout.getLocationInWindow(sourceCoordinates);
		int[] increaseDecreaseLayoutCoordinates = new int[2];
		increaseDecreaseText.getLocationInWindow(increaseDecreaseLayoutCoordinates);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		animationSet.getAnimations().set(
				0,
				new TranslateAnimation(Animation.ABSOLUTE, sourceCoordinates[0] - increaseDecreaseLayoutCoordinates[0], Animation.ABSOLUTE, sourceCoordinates[0]
						- increaseDecreaseLayoutCoordinates[0], Animation.ABSOLUTE, sourceCoordinates[1] - increaseDecreaseLayoutCoordinates[1], Animation.ABSOLUTE,
						sourceCoordinates[1] - increaseDecreaseLayoutCoordinates[1]));

		String txt = String.format("%s%s", event.getQuantity() > 0 ? "+" : "", event.getQuantity());
		increaseDecreaseText.setText(txt);
		onError(event.getTarget() + " " + txt + " " + increaseDecreaseText);
		increaseDecreaseText.startAnimation(animationSet);

	}

	public CardLayout getGameEventLayout() {
		return gameEventLayout;
	}

	public void setGameEventLayout(CardLayout gameEventLayout) {
		this.gameEventLayout = gameEventLayout;
	}

	@Override
	public void onGetGame(GameView gameView) {

		if (gameView == null) {
			onError("Unable to get game");
			return;
		}

		Log.i(TAG, String.format("Perf Monitor: Entering onGetGame: state=%s, phase=%s", gameView.getGameState(), gameView.getPhase()));

		gameInfos = (TextView) rootLayout.findViewById(R.id.gameInfos);
		this.gameView = gameView;

		player = gameView.getPlayer();
		opponent = gameView.getOpponents().get(0);
		opponentPlayerLayout.setup(opponent);
		playerPlayerLayout.setup(player);
		opponentDefenseDown = opponent.getCurrentDefense() > 0 ? false : true;

		switch (gameView.getGameState()) {

			case STARTED:

				if (gameView.getPhase() == Phase.DURING_PLAY) {
					boolean handleDisplay = displayEvents();

					if (!handleDisplay) {
						updateCardsDisplay();
					}
				} else {
					updateCardsDisplay();
					displayEvents();
				}

				break;

			default:
				Log.e("onGetGame", String.format("Invalid game state: %s", gameView.getGameState()));
				break;

		}

		gameInfos.setText(String.format("%s [%s / %s], token=%s",
				gameView.toString(), ++httpCallsDone, httpCallsAborted,
				gameView.getToken() != null ? gameView.getToken().getUid() : "N/A"));
		gameTrade.setText(String.format("%s", gameView.getTrade()));
		gameDefense.setText(String.format("%s", gameView.getDefense()));
		gameFaith.setText(String.format("%s", gameView.getFaith()));
		gameAlchemy.setText(String.format("%s", gameView.getAlchemy()));

		// Activate Next Phase only if this is the active player
		if (gameView.isActivePlayer()) {
			nextPhaseButton.setEnabled(true);
		} else {
			nextPhaseButton.setEnabled(false);
		}

		Log.i(TAG, String.format("Perf Monitor: Leaving onGetGame()"));

	}

	@Override
	public void onError(String err) {
		Toast.makeText(this, String.format("Error occured: %s", err), Toast.LENGTH_LONG).show();
	}

	public void onDeleteGame() {
		returnHome(ClientConstants.GAME_STOPPED);
		finish();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
