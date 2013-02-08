package search;


public interface Search3 {
	/** search until {@link #cutoffSearch()} is called, moveStore should be length 2*/
	public void search(int player, int[] moveStore);
	/** search up to a max depth*/
	public void search(int player, int[] moveStore, int maxDepth);
	/** cuts off search*/
	public void cutoffSearch();
	public SearchStat getStats();
	public void setListener(SearchListener l);
}
