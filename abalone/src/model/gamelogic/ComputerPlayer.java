package model.gamelogic;

import com.owlike.genson.annotation.JsonProperty;

import model.artificialintelligence.Strategy;

public class ComputerPlayer extends Player {
	//

    private Strategy strategy;

    /**
     * Constructs a new ComputerPlayer.
     * @param name Name of the ComputerPlayer.
     * @param color Color of the Player.
     * @param strategy Strategy that the player uses.
     */
    public ComputerPlayer(@JsonProperty("name") String name, 
    					  @JsonProperty("strategy") Strategy strategy) {
        super(name);
        this.strategy = strategy;
    }

    @Override
    public PlayableMove determineMove(GameState gameState) {
        return strategy.determineMove(gameState);
    }
    
    public Strategy getStrategy() {
    	return this.strategy;
    }

}
