package model.gamelogic;

import com.owlike.genson.annotation.JsonProperty;

import view.UI;

public class HumanPlayer extends Player {
	//

    HumanPlayer(@JsonProperty("name") String name) {
        super(name);
    }

    @Override
    public PlayableMove determineMove(GameState gameState) {
        return UI.getInstance().getPlayerMove(this, gameState.getBoard());
    }

}
