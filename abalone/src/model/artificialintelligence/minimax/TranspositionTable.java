package model.artificialintelligence.minimax;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import model.artificialintelligence.minimax.TranspositionTable.TranspositionValue.Flag;
import model.gamelogic.GameState;
import model.gamelogic.Team;

public class TranspositionTable {
	//
	
	static final int NUMBER_SIZE = 64;
	static final int KEY_SIZE = 22;
	static final int MAP_SIZE = (int) Math.pow(2, KEY_SIZE);
	
	private Map<TranspositionKey, TranspositionValue> table;
	
	TranspositionTable() {
		table = new HashMap<>();
	}
	
	private static TranspositionKey getKey(GameState gameState, Team team) {
		return new TranspositionKey(
				team,  
				gameState.getBoard().getBoardHash());
	}
	
	void put(GameState gameState, Team team, int depthOfSubtree, int evaluationValue, Flag flag) {
		table.put(getKey(gameState, team), 
				new TranspositionValue(gameState, depthOfSubtree, evaluationValue, flag));
	}
	
	TranspositionValue get(GameState gameState, Team team) {
		return get(gameState.getBoard().getBoardHash(), team);
	}
	
	TranspositionValue get(long boardHash, Team team) {
		TranspositionValue val = table.get(new TranspositionKey(team, boardHash));
		if (val == null || val.boardHash != boardHash) {
			return null;
		}
		return val;
	}
	
	int size() {
		return table.size();
	}
	
	
	static class TranspositionKey {
		Team team;
		long partialBoardHash;
		
		TranspositionKey(Team team, long boardHash) {
			this.team = team;
			this.partialBoardHash = boardHash >>> (NUMBER_SIZE - KEY_SIZE);
		}

		@Override
		public int hashCode() {
			return Objects.hash(partialBoardHash, team);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TranspositionKey)) {
				return false;
			}
			TranspositionKey other = (TranspositionKey) obj;
			return partialBoardHash == other.partialBoardHash && Objects.equals(team, other.team);
		}


		
		
	}
	
	static class TranspositionValue {
		long boardHash;
		int depthOfSubtree;
		int value;
		Flag flag;
		
		TranspositionValue(GameState gameState, int depthOfSubtree, int evaluationValue, Flag flag) {
			this(gameState.getBoard().getBoardHash(), depthOfSubtree, evaluationValue, flag);
		}
		
		TranspositionValue(long boardHash, int depthOfSubtree, int evaluationValue, Flag flag) {
			this.boardHash = boardHash;
			this.depthOfSubtree = depthOfSubtree;
			this.value = evaluationValue;
			this.flag = flag;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(boardHash, depthOfSubtree, flag, value);
		}



		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TranspositionValue)) {
				return false;
			}
			TranspositionValue other = (TranspositionValue) obj;
			return boardHash == other.boardHash && depthOfSubtree == other.depthOfSubtree 
					&& flag == other.flag && value == other.value;
		}



		static enum Flag {
			LOWER_BOUND,
			UPPER_BOUND,
			EXACT;
		}
	}
}

