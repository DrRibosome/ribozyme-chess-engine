package search;

import search.search33.MoveSet;
import state4.State4;


public interface Search4 {
	/** search until {@link #cutoffSearch()} is called, moveStore should be length 2*/
	public void search(int player, State4 s, MoveSet moveStore);
	/** search up to a max depth*/
	public void search(int player, State4 s, MoveSet moveStore, int maxDepth);
	/** cuts off search*/
	public void cutoffSearch();
	public SearchStat getStats();
	public void setListener(SearchListener2 l);
	/** ready search for new game (ie, clear hash, etc)*/
	public void resetSearch();
}
