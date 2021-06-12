package model.artificialintelligence;

import java.util.Random;
import java.util.Set;
import model.gamelogic.GameState;
import model.gamelogic.Move;
import model.gamelogic.PlayableMove;

public class RandomStrategy implements Strategy {
    //

    @Override
    public PlayableMove determineMove(GameState gameState) {
        Set<PlayableMove> moves = Move.allLegalMoves(gameState);
        int random = new Random().nextInt(moves.size());
        return (PlayableMove) moves.toArray()[random];
    }
}
