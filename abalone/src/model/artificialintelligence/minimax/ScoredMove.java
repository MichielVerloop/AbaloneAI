package model.artificialintelligence.minimax;

import model.gamelogic.PlayableMove;

public class ScoredMove implements Comparable<ScoredMove> {
	//
	PlayableMove move;	
	int score;
	
	ScoredMove(PlayableMove move, int rating) {
		this.move = move;
		this.score = rating;
	}

	@Override
	public int compareTo(ScoredMove o) {
		if (o == null) {
			throw new IllegalArgumentException();
		}
		return this.score < o.score ? -1 : this.score == o.score ? 0 : 1;
	}
	
	
}
