package model.artificialintelligence.minimax;

import java.util.HashMap;
import java.util.Map;

import model.gamelogic.PlayableMove;

public class MoveHistoryTable {
	//
	private Map<PlayableMove, Integer> table;
	
	public MoveHistoryTable() {
		this.table = new HashMap<>();
	}
	
	/**
	 * Increments the kills by 2 to the power of inverseDepth.
	 * @param move The move whose kill score is incremented.
	 * @param inverseDepth Should be depthLimit - currentDepth.
	 */
	void incrementKills(PlayableMove move, int inverseDepth) {
		table.merge(move, inverseDepth * inverseDepth, Integer::sum);
	}
	
	int get(PlayableMove move) {
		return table.getOrDefault(move, 0);
	}
}
