package model.gamelogic;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

public class GameStats {
	
	GameState gameState;
	List<TurnStats> statsByTurn;
	
	// Stats that may be discarded in case the depth is not fully evaluated, which happens with time-based iddfs.
	TurnStats currentTurnCurrentDepth;
	// Stats that are from fully evaluated depths.
	public TurnStats currentTurnCommittedDepth;
	
	GameStats(GameState gameState) {
		this.gameState = gameState;
		this.statsByTurn = new ArrayList<>();
		this.currentTurnCurrentDepth = null;
		this.currentTurnCommittedDepth = null;
	}
	
	public void initializeTurn() {
		assert (currentTurnCurrentDepth == null);
		assert (currentTurnCommittedDepth == null);
		assert (gameState.getTurn() == statsByTurn.size());
		
		currentTurnCurrentDepth = new TurnStats(gameState, gameState.getCurrentPlayer());
		currentTurnCommittedDepth = new TurnStats(gameState, gameState.getCurrentPlayer());
	}
	
	public void addNodeVisited() {
		currentTurnCurrentDepth.nodesVisited++;
	}
	
	public void addExactCut() {
		currentTurnCurrentDepth.exactCuts++;
	}
	
	public void addWindowCut() {
		currentTurnCurrentDepth.windowCuts++;
	}
	
	public void addWindowNarrowed() {
		currentTurnCurrentDepth.windowsNarrowed++;
	}
	
	public void addLeafNode() {
		currentTurnCurrentDepth.leafNodes++;
	}
	
	public void registerTiming(long timeSpent) {
		currentTurnCommittedDepth.timeSpent = timeSpent; // commited, not current depth else it's always 0
	}
	
	public void registerBranchingFactor(int branchingFactor, int depth) {
		if (!currentTurnCurrentDepth.branchingFactorByDepth.containsKey(depth)) {
			currentTurnCurrentDepth.branchingFactorByDepth.put(depth, new Average());
		}
		currentTurnCurrentDepth.branchingFactorByDepth.get(depth).add(branchingFactor);
	}
	
	public void registerScore(int score) {
		currentTurnCurrentDepth.ratedScore = score;
	}
	
	public void registerDepth(int depth) {
		currentTurnCurrentDepth.depth = depth;
	}
	
	public void registerTranspositionTableSize(int size) {
		currentTurnCurrentDepth.transpositionTableSize = size;
	}
	
	public void commitDepth() {
		currentTurnCommittedDepth.merge(currentTurnCurrentDepth);
		currentTurnCurrentDepth = new TurnStats(gameState, currentTurnCurrentDepth.player);
	}
	
	public void commitTurn() {
		currentTurnCommittedDepth.actualScore = gameState.getScore();
		statsByTurn.add(currentTurnCommittedDepth);
		currentTurnCommittedDepth = null;
		currentTurnCurrentDepth = null;
	}
	
	private int getMaxDepth() {
		return statsByTurn.stream().mapToInt(e -> e.depth).max().orElseThrow(NoSuchElementException::new);
	}
	
	public void writeToCSV(Writer writer) throws IOException {
		ICSVWriter csvWriter = new CSVWriterBuilder(writer)
			.withSeparator(',')
			.build();
		// Set the header
		List<String> header = new ArrayList<>(Arrays.asList("player"));
		for (Team team : gameState.teams) {
			header.add("score of " + team.toString());
		}
		header.addAll(new ArrayList<>(Arrays.asList("time spent","nodes visited","leaf nodes","rating of game state","depth","exact cuts","window cuts",
		   "windows narrowed","transposition table size")));
		for (int i = 1; i <= getMaxDepth(); i++) {
			header.add("branching factor at depth " + i);
		}
		csvWriter.writeNext(header.toArray(new String[0]));
		
		// Write the contents
		for (TurnStats turn : statsByTurn) {
			csvWriter.writeNext(turn.toStringArray());
			csvWriter.flush();
		}
		csvWriter.close();
	}
	
	public static class TurnStats {
		
		GameState gameState;
		public Player player;
		public GameState.Score actualScore;
		public long timeSpent;
		public int nodesVisited;
		public int leafNodes;
		public int ratedScore;
		public int depth;
		public int exactCuts;
		public int windowCuts;
		public int windowsNarrowed;
		public int transpositionTableSize;
		public Map<Integer, Average> branchingFactorByDepth;
		
		TurnStats(GameState gameState, Player player) {
			this.gameState = gameState;
			this.player = player;
			this.actualScore = null;
			this.timeSpent = 0;
			this.nodesVisited = 0;
			this.leafNodes = 0;
			this.ratedScore = 0;
			this.depth = 0;
			this.exactCuts = 0;
			this.windowCuts = 0;
			this.windowsNarrowed = 0;
			this.transpositionTableSize = 0;
			this.branchingFactorByDepth = new HashMap<>();
		}
		
		void merge(TurnStats newer) {
			assert (this.player == newer.player);
			// actualScore of the older is always taken.
			this.timeSpent += newer.timeSpent;
			this.nodesVisited += newer.nodesVisited;
			this.leafNodes += newer.leafNodes;
			this.ratedScore = newer.ratedScore;
			this.depth = newer.depth;
			this.exactCuts += newer.exactCuts;
			this.windowCuts += newer.windowCuts;
			this.windowsNarrowed += newer.windowsNarrowed;
			this.transpositionTableSize = newer.transpositionTableSize;
			newer.branchingFactorByDepth.forEach(
					(key, value) -> this.branchingFactorByDepth.merge(key, value, (v1, v2) -> v1.add(v2)));
			this.branchingFactorByDepth = newer.branchingFactorByDepth;
		}
		
		public String[] toStringArray() {
			List<String> res = new ArrayList<>();
			res.add(player.getName());
			for (Team team : gameState.teams) {
				res.add(String.valueOf(actualScore.score.get(team))); // Required to enforce an order
			}
			res.add(String.valueOf(timeSpent));
			res.add(String.valueOf(nodesVisited));
			res.add(String.valueOf(leafNodes));
			res.add(String.valueOf(ratedScore));
			res.add(String.valueOf(depth));
			res.add(String.valueOf(exactCuts));
			res.add(String.valueOf(windowCuts));
			res.add(String.valueOf(windowsNarrowed));
			res.add(String.valueOf(transpositionTableSize));
			for (int i : branchingFactorByDepth.keySet().stream().sorted().collect(Collectors.toList())) {
				res.add(String.valueOf(branchingFactorByDepth.get(i).getAverage()));
			}
			return res.toArray(new String[0]);
		}
	}
	
	static class Average {
		int sum;
		int count;
		
		Average() {
			sum = 0;
			count = 0;
		}
		
		void add(int num) {
			sum += num;
			count++;
		}
		
		Average add(Average o) {
			Average res = new Average();
			res.sum = this.sum + o.sum;
			res.count = this.count + o.count;
			return res;
		}
		
		int getAverage() {
			return (int) (sum / count);
		}
		
		int getSum() {
			return sum;
		}
	}
}