package controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import model.artificialintelligence.minimax.GameStateEvaluator;
import model.artificialintelligence.minimax.Minimax;
import model.gamelogic.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class JsonTest {
	//
	Json json;
	List<Player> players;
	ByteArrayOutputStream out;
	ByteArrayInputStream in;
	
	@BeforeEach
	void setUp() throws Exception {
		final Player player1 = Player.newPlayer("human playerName", "human");
		final Player player2 = Player.newPlayer("aggressive playerName", "aggressive");
		final Player player3 = Player.newPlayer("random playerName", "random");
		final Player player4 = Player.newPlayer("minimaxDFS", 
				new Minimax.Builder()
					.withDfs(3)
					.enableHashing()
					.enableMarbleOrdering(1, 3),
					new GameStateEvaluator.Builder()
					.withMarbleConqueredWeight(100)
					.withCoherenceWeight(8)
					.withDistanceFromCenterWeight(5)
					.withFormationBreakWeight(20)
					.build());
		players = new ArrayList<>();
		players.add(player1);
		players.add(player2);
		players.add(player3);
		players.add(player4);
		
		out = new ByteArrayOutputStream();
		Json.serializePlayers(players, new OutputStreamWriter(out));
		in = new ByteArrayInputStream(out.toByteArray());
	}
	
	@Test
	void testDeserialazingDoesNotThrow() {
		assertDoesNotThrow(() -> Json.deserializePlayers(new InputStreamReader(in)));
	}
	
	@Test
	void testDeserializedPlayersIsDeepCopyOfPlayers() {
		List<Player> deserializedPlayers = Json.deserializePlayers(new InputStreamReader(in));
		// Ideally you would do .equals here to confirm that deserializedPlayers = players, but since
		// it's full of cyclic references, we don't have that luxury.
		
		assertEquals(players.size(), deserializedPlayers.size());
		for (int i = 0; i < players.size(); i++) {
			assertEquals(deserializedPlayers.get(i).getName(),
					 players.get(i).getName(),
					 "Expected the " + i + "th player with name " + players.get(i).getName()
					 + " to equal player with name " + deserializedPlayers.get(i).getName());
		}
		assertFalse(players.get(0) == deserializedPlayers.get(0));
	}

	
}
