package chess.search;

public class SearchStat {
	public long nodesSearched;
	public long searchTime;
	/** empirical branching factor*/
	public double empBranchingFactor;
	public long hashHits;
	/** final predicted score*/
	public int predictedScore;
	
	public static void agg(SearchStat src, SearchStat agg){
		agg.nodesSearched += src.nodesSearched;
		agg.searchTime += src.searchTime;
		agg.empBranchingFactor += src.empBranchingFactor;
		agg.hashHits += src.hashHits;
	}
}
