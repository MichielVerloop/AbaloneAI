package model.gamelogic;

import com.owlike.genson.annotation.JsonProperty;

public class ReplayPlayer extends Player {
	//
	public ReplayPlayer(@JsonProperty("name") String name) {
		super(name);
	}

	@Override
	public PlayableMove determineMove(GameState gameState) {
		return null;
	}

}
