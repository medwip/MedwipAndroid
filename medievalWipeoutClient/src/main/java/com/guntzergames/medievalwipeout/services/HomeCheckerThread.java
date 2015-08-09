package com.guntzergames.medievalwipeout.services;

import com.guntzergames.medievalwipeout.activities.HomeActivity;
import com.guntzergames.medievalwipeout.interfaces.ClientConstants;
import com.guntzergames.medievalwipeout.webclients.GameWebClient;

public class HomeCheckerThread extends Thread {
	
	private GameWebClient gameWebClient;
	private HomeActivity homeActivity = null;
	private boolean checkActivated = false;
	
	public HomeCheckerThread(HomeActivity homeActivity) {
		this.homeActivity = homeActivity;
		gameWebClient = new GameWebClient(ClientConstants.SERVER_IP_ADDRESS, homeActivity);
	}
	
	@Override
	public void run() {
		
		while (true) {
			
			try {
				Thread.sleep(3000);
			}
			catch ( Exception e ) {
				
			}
			
			if ( checkActivated && !homeActivity.isHttpRequestBeingExecuted() ) {
				gameWebClient.checkGame(homeActivity.getGameView().getId());
			}
			
		}
		
	}

	public boolean isCheckActivated() {
		return checkActivated;
	}

	public void setCheckActivated(boolean checkActivated) {
		this.checkActivated = checkActivated;
	}

}
