package model.artificialintelligence;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import model.gamelogic.GameState;
import model.gamelogic.Move;
import model.gamelogic.PlayableMove;

/**
 * This strategy plays by the marble ordering.
 */
public class AggressivePusherStrategy implements Strategy {
	//
    @Override
    public PlayableMove determineMove(GameState gameState) {
        Set<PlayableMove> moves = Move.allLegalMoves(gameState);
        
        return moves.stream()
			.sorted(Comparator.reverseOrder())
			.collect(Collectors.toList())
			.get(0);
    }
}
