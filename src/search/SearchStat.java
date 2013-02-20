package search;

public class SearchStat {
	public long nodesSearched;
	public long searchTime;
	public double endScore;
	/** empiricle branching factor*/
	public double empBranchingFactor;
	public long hashHits;
	
	public static void agg(SearchStat src, SearchStat agg){
		agg.nodesSearched += src.nodesSearched;
		agg.searchTime += src.searchTime;
		agg.empBranchingFactor += src.empBranchingFactor;
		agg.hashHits += src.hashHits;
	}
}
