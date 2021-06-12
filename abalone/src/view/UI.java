package view;

import model.gamelogic.Board;
import model.gamelogic.GameState;
import model.gamelogic.PlayableMove;
import model.gamelogic.Player;
import model.gamelogic.Team;

public abstract class UI {
    //

    private static UI instance;

    public static UI getInstance() {
        return instance;
    }

    public static void setInstance(UI instance) {
        UI.instance = instance;
    }

    /**
     * Displays the current game state.
     */
    public abstract void updateGameState(GameState gameState, Player current);

    /**
     * Displays the game result of the game.
     * @param winner The team that won the game or null if the game
     *     ended in a draw.
     */
    public abstract void showGameResult(GameState gameState, Team winner);

    public abstract PlayableMove getPlayerMove(Player player, Board board);

    public abstract void showMessage(String message);

    public abstract void showHint(GameState gameState, PlayableMove move);

    public abstract void showException(String message);

    public abstract void updateQueues(int twoPlayerSize, int threePlayerSize, int fourPlayerSize);

    public abstract void updateQueue(int gameSize, int queueSize);
}
