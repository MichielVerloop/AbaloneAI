package model.gamelogic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.artificialintelligence.AggressivePusherStrategy;
import model.artificialintelligence.MinimaxStrategy;
import model.artificialintelligence.RandomStrategy;
import model.artificialintelligence.minimax.GameStateEvaluator;
import model.artificialintelligence.minimax.Minimax;

class PlayerTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testNewPlayerComplexConstructor() {
		GameStateEvaluator evaluator = new GameStateEvaluator.Builder()
				.withMarbleConqueredWeight(1)
				.build();
		Minimax.Builder miniBuilder = new Minimax.Builder()
				.withDfs(3)
				.enableHashing()
				.enableMarbleOrdering(1, Integer.MAX_VALUE);
		Player dfsPlayer = Player.newPlayer("dfs playername", miniBuilder, evaluator);
		assertEquals("dfs playername", dfsPlayer.getName());
		assertTrue(dfsPlayer instanceof ComputerPlayer);
		assertTrue(((ComputerPlayer)dfsPlayer).getStrategy() instanceof MinimaxStrategy);
		
		assertThrows(IllegalArgumentException.class, () -> Player.newPlayer("", miniBuilder, evaluator));
	}

	@Test
	void testNewPlayerSimpleConstructor() {
		Player humanPlayer = Player.newPlayer("human playername", "human");
		assertEquals("human playername", humanPlayer.getName());
		assertTrue(humanPlayer instanceof HumanPlayer);
		
		Player randomPlayer = Player.newPlayer("random playername", "random");
		assertEquals("random playername", randomPlayer.getName());
		assertTrue(randomPlayer instanceof ComputerPlayer);
		assertTrue(((ComputerPlayer)randomPlayer).getStrategy() instanceof RandomStrategy);
		
		Player aggressivePlayer = Player.newPlayer("aggressive random playername", "aggressive");
		assertEquals("aggressive random playername", aggressivePlayer.getName());
		assertTrue(aggressivePlayer instanceof ComputerPlayer);
		assertTrue(((ComputerPlayer)aggressivePlayer).getStrategy() instanceof AggressivePusherStrategy);
		
		assertThrows(IllegalArgumentException.class, () -> Player.newPlayer("", "human"));
		assertThrows(IllegalArgumentException.class, () -> Player.newPlayer("legal name", "illegal type"));
	}

}
