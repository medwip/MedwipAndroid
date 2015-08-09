package com.guntzergames.medievalwipeout.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

import com.guntzergames.medievalwipeout.activities.GameActivity;
import com.guntzergames.medievalwipeout.activities.R;
import com.guntzergames.medievalwipeout.beans.PlayerDeckCard;
import com.guntzergames.medievalwipeout.beans.PlayerFieldCard;
import com.guntzergames.medievalwipeout.beans.ResourceDeckCard;
import com.guntzergames.medievalwipeout.enums.CardLocation;
import com.guntzergames.medievalwipeout.interfaces.ICard;
import com.guntzergames.medievalwipeout.layouts.CardLayout;
import com.guntzergames.medievalwipeout.webclients.GameWebClient;

public class GameDragListener implements OnDragListener {

	private static final String TAG = "GameDragListener";

	private GameActivity gameActivity;

	private Map<Integer, View> targetViews = new HashMap<Integer, View>();

	public GameDragListener(GameActivity gameActivity) {
		this.gameActivity = gameActivity;
	}

	private boolean isPossibleDest(View dest) {
		return dest.getId() == R.id.playerFieldAttack || dest.getId() == R.id.playerFieldDefense || dest.getId() == R.id.opponentFieldDefense || dest.getId() == R.id.playerHand
				|| dest.getId() == R.id.opponentFieldAttackCard0 || dest.getId() == R.id.opponentFieldAttackCard1 || dest.getId() == R.id.opponentFieldAttackCard2
				|| dest.getId() == R.id.opponentFieldAttackCard3 || dest.getId() == R.id.opponentFieldAttackCard4;
	}

	public String getPossibleTarget(CardLayout cardLayout, View dest) {

		Log.d(TAG, "event=" + cardLayout);
		CardLocation cardLocation = cardLayout.getCardLocation();
		ICard card = cardLayout.getCard();
		View parent = (View) cardLayout.getParent();

		if (cardLocation == CardLocation.MODAL && card instanceof ResourceDeckCard && dest.getId() == R.id.playerHand) {
			return "playerHand";
		}
		if (cardLocation == CardLocation.MODAL && card instanceof PlayerDeckCard && dest.getId() == R.id.playerHand) {
			return "playerHand";
		}
		if (cardLocation == CardLocation.HAND && card instanceof PlayerDeckCard && dest.getId() == R.id.playerFieldAttack) {
			return "playerFieldAttack";
		}
		if (cardLocation == CardLocation.HAND && card instanceof PlayerDeckCard && dest.getId() == R.id.playerFieldDefense) {
			return "playerFieldDefense";
		}
		// Select only player cards
		if (cardLocation == CardLocation.FIELD_ATTACK && card instanceof PlayerFieldCard) {

			PlayerFieldCard playerFieldCard = (PlayerFieldCard) card;
			Log.d(TAG, String.format("Attack card played, (parent.getId() == R.id.playerFieldDefense) == %s", (parent.getId() == R.id.playerFieldDefense)));

			// Cards in attack field and cards with archer property can attack
			if ((parent.getId() == R.id.playerFieldAttack) || ((parent.getId() == R.id.playerFieldDefense) && playerFieldCard.isArcher())) {

				int opponentDefense = gameActivity.getGameView().getOpponents().get(0).getCurrentDefense();
				Log.d(TAG, String.format("opponentDefense = %s, dest = %s", opponentDefense, dest));
				Log.i(TAG, " --> Number of cards: " + gameActivity.getGameView().getOpponents().get(0).getPlayerFieldDefense().getCards());

				// Opponent field defense can be targeted only if opponent's
				// defense is greater than 0
				// or if there is no defense card anymore
				if (dest.getId() == R.id.opponentFieldDefense) {
					if ( opponentDefense > 0 ) {
						return "opponentFieldDefense";
					}
					else if ( gameActivity.getGameView().getOpponents().get(0).getPlayerFieldDefense().getCards().isEmpty() ) {
						return "opponentFieldDefense";
					}
				}
				if (dest instanceof CardLayout) {

					CardLayout cardLayoutDest = (CardLayout) dest;
					Log.i(TAG, String.format("dest instance of CardLayout %s", cardLayoutDest.getCard()));

					if (((CardLayout) dest).getCardLocation() == CardLocation.FIELD_ATTACK) {
						return "opponentCardAttack";
					}
					// Defense cards can be targeted only if opponent's defense
					// is equal to 0
					if ((((CardLayout) dest).getCardLocation() == CardLocation.FIELD_DEFENSE) && opponentDefense == 0) {
						return "opponentCardDefense";
					}

				}
			}
		}
		/*
		 * if ( cardLocation == CardLocation.FIELD_DEFENSE && card instanceof
		 * PlayerDeckCard && dest == R.id.opponentField ) { return
		 * "opponentField"; }
		 */

		return null;

	}

	private void loadTargetViews(CardLayout cardLayout) {

		for (View view : gameActivity.getDragableRegisteredViews()) {
			if (getPossibleTarget(cardLayout, view) != null) {
				targetViews.put(view.getId(), view);
				boolean animated = gameActivity.startTargetAnimation(view);
				Log.i(TAG, String.format("target view: %s animated %s", view, animated));
			}
		}

		Log.i(TAG, String.format("Loaded target views: %s", targetViews));
		
	}

	private void unloadTargetViews(CardLayout cardLayout) {

		for (View view : targetViews.values()) {
			gameActivity.stopTargetAnimation(view);
		}
		
		targetViews.clear();

	}

	@Override
	public boolean onDrag(View dest, DragEvent event) {

		CardLayout cardLayout = (CardLayout) event.getLocalState();
		GameWebClient gameWebClient = gameActivity.getGameWebClient();

		switch (event.getAction()) {

			case DragEvent.ACTION_DRAG_STARTED:

				Log.i(TAG, String.format("ACTION_DRAG_STARTED, dest = %s", dest));
				gameActivity.setBeingModified(true);
				loadTargetViews(cardLayout);
				return true;

			case DragEvent.ACTION_DRAG_ENTERED:

				Log.i(TAG, String.format("ACTION_DRAG_ENTERED, dest = %s", dest));
//				if (getPossibleTarget(cardLayout, dest) != null) {
				if ( targetViews.containsKey(dest.getId()) ) {
					gameActivity.startHighlightAnimation(dest);
					return true;
				}
				return false;

			case DragEvent.ACTION_DRAG_EXITED:

				Log.i(TAG, String.format("ACTION_DRAG_EXITED, dest = %s", dest));
				if ( targetViews.containsKey(dest.getId()) ) {
					gameActivity.stopHightlightAnimation(dest);
					return true;
				}
				return false;

			case DragEvent.ACTION_DROP:

				Log.i(TAG, String.format("ACTION_DROP, dest = %s", dest));
				gameActivity.hideCardLayoutDetail();

				String source = cardLayout.getPossibleSource(((View) cardLayout.getParent()).getId());
				String target = getPossibleTarget(cardLayout, dest);
				if (target != null) {
					gameActivity.stopHightlightAnimation(dest);
				}

				Log.i(TAG, String.format("source=%s, cardLayout.getId()=%s, target=%s, R.id.playerFieldDefense=%s", source, ((View) cardLayout.getParent()).getId(), target,
						R.id.playerFieldDefense));

				if (target != null) {
					int destinationCardId = (dest instanceof CardLayout) ? ((CardLayout) dest).getSeqNum() : -1;
					gameWebClient.playCard(gameActivity.getGameId(), source, cardLayout.getSeqNum(), target, destinationCardId);
					return true;
				}

				break;

			case DragEvent.ACTION_DRAG_ENDED:

				Log.i(TAG, String.format("ACTION_DRAG_ENDED, dest = %s", dest));
				gameActivity.setBeingModified(false);
				unloadTargetViews(cardLayout);
				return false;

			default:

				Log.d(TAG, String.format("Default (not handled action event), dest = %s", dest));
				if ( targetViews.containsKey(dest.getId()) ) {
					return true;
				}
				return false;

		}

		return false;

	}

}
