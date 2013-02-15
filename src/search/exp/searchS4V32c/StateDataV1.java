package search.exp.searchS4V32c;

public class StateDataV1 {
	public final static int CUTOFF_TYPE_EXACT = 0;
	public final static int CUTOFF_TYPE_UPPER = 1;
	public final static int CUTOFF_TYPE_LOWER = 2;
	
	public long zkey;
	public double score;
	public long move;
	public int depth;

	/** stores alpha beta cutoff type*/
	public int cutoffType;
	
	public void fill(long zkey, long move, double score, int depth, int cutoffType){
		this.zkey = zkey;
		this.move = move;
		this.score = score;
		this.depth = depth;
		this.cutoffType = cutoffType;
	}
}
