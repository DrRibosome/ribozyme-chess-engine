package chess.search;

import chess.state4.State4;


public interface Search {
	/** chess.search until {@link #cutoffSearch()} is called, moveStore should be length 2*/
	public void search(int player, State4 s, MoveSet moveStore);
	/** chess.search up to a max depth*/
	public void search(int player, State4 s, MoveSet moveStore, int maxDepth);
	/** cuts off chess.search*/
	public void cutoffSearch();
	public SearchStat getStats();
	public void setListener(SearchListener2 l);
	/** ready chess.search for new game (ie, clear hash, etc)*/
	public void resetSearch();
}
