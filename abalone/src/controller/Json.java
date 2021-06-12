package controller;

import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;

import java.io.Reader;
import java.io.Writer;
import java.util.List;

import model.artificialintelligence.AggressivePusherStrategy;
import model.artificialintelligence.MinimaxStrategy;
import model.artificialintelligence.RandomStrategy;
import model.artificialintelligence.minimax.GameStateEvaluator;
import model.artificialintelligence.minimax.Minimax;
import model.gamelogic.ComputerPlayer;
import model.gamelogic.GameState.Game;
import model.gamelogic.HumanPlayer;
import model.gamelogic.Player;
import model.gamelogic.ReplayPlayer;

public class Json {
	//
	private static final Genson genson = init();
	
	/**
	 * Constructs a Json. 
	 */
	private static final Genson init() {
		return new GensonBuilder()
				.useClassMetadata(true)
				.useRuntimeType(true)
				.useIndentation(true)
				//.failOnMissingProperty(true)
				.failOnNullPrimitive(true)
				.addAlias("human", HumanPlayer.class)
				.addAlias("computer", ComputerPlayer.class)
				.addAlias("random", RandomStrategy.class)
				.addAlias("aggressive", AggressivePusherStrategy.class)
				.addAlias("replay", ReplayPlayer.class)
				.addAlias("minimax", MinimaxStrategy.class)
				.addAlias("evaluator", GameStateEvaluator.class)
				.addAlias("builder", Minimax.Builder.class)
				.exclude("color")
				.exclude("marbles")
				.exclude("team")
				.exclude("transpositionTable")
				.exclude("gameScoreHashes")
				.exclude("totalTime")
				.exclude("weight")
				.exclude("ratingLowerBound")
				.exclude("ratingUpperBound")
				.exclude("boardhashes")
			.create();
	}
	
	/**
	 * Serializes players to the given writer.
	 * @param players The list of players that will be serialized.
	 * @param out The output that is written to.
	 */
	public static void serializePlayers(List<Player> players, Writer out) {
		genson.serialize(players, out);
    }
	
	/**
	 * Deserializes players from the given reader.
	 * @param in The input that is read from.
	 */
	public static List<Player> deserializePlayers(Reader in) {
		return genson.deserialize(in, new GenericType<List<Player>>() {});
	}

	public static void serializeGame(Game game, Writer out) {
		genson.serialize(game, out);
	}
	
	public static Game deserializeGame(Reader in) {
		return genson.deserialize(in, Game.class);
	}
}
