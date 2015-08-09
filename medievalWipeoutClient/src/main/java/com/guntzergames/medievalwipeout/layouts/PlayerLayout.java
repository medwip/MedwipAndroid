package com.guntzergames.medievalwipeout.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.guntzergames.medievalwipeout.activities.R;
import com.guntzergames.medievalwipeout.beans.Player;

public class PlayerLayout extends LinearLayout {

	private static final String TAG = "PlayerLayout";
	
	private static LayoutInflater layoutInflater;

	private Context context;
	private View root;
	private Player player;
	
	private ElementLayout tradeLayout, goldLayout, defenseLayout, faithLayout, playerLifePointsLayout;

	public PlayerLayout(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public PlayerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public void setup(Player player) {
		this.player = player;
		setup();
	}

	public void init() {
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		root = (LinearLayout) layoutInflater.inflate(R.layout.player, (ViewGroup) this);
	}

	private void setup() {

		tradeLayout = (ElementLayout) root.findViewById(R.id.tradeLayout);
		goldLayout = (ElementLayout) root.findViewById(R.id.goldLayout);
		defenseLayout = (ElementLayout) root.findViewById(R.id.defenseLayout);
		faithLayout = (ElementLayout) root.findViewById(R.id.faithLayout);
		playerLifePointsLayout = (ElementLayout) root.findViewById(R.id.playerLifePointsLayout);
		
		tradeLayout.setup(context.getResources().getString(R.string.trade), String.format("%s", player.getTrade()));
		goldLayout.setup(context.getResources().getString(R.string.gold), String.format("%s", player.getGold()));
		defenseLayout.setup(context.getResources().getString(R.string.defense), String.format("%s | %s", player.getDefense(), player.getCurrentDefense()));
		faithLayout.setup(context.getResources().getString(R.string.faith), String.format("%s", player.getFaith()));
		playerLifePointsLayout.setup(context.getResources().getString(R.string.life_points), String.format("%s", player.getLifePoints()));
		
	}

	public ElementLayout getTradeLayout() {
		return tradeLayout;
	}

	public void setTradeLayout(ElementLayout tradeLayout) {
		this.tradeLayout = tradeLayout;
	}

	public ElementLayout getGoldLayout() {
		return goldLayout;
	}

	public void setGoldLayout(ElementLayout goldLayout) {
		this.goldLayout = goldLayout;
	}

	public ElementLayout getDefenseLayout() {
		return defenseLayout;
	}

	public void setDefenseLayout(ElementLayout defenseLayout) {
		this.defenseLayout = defenseLayout;
	}

	public ElementLayout getFaithLayout() {
		return faithLayout;
	}

	public void setFaithLayout(ElementLayout faithLayout) {
		this.faithLayout = faithLayout;
	}

	public ElementLayout getPlayerLifePointsLayout() {
		return playerLifePointsLayout;
	}

	public void setPlayerLifePointsLayout(ElementLayout playerLifePointsLayout) {
		this.playerLifePointsLayout = playerLifePointsLayout;
	}
	
}
