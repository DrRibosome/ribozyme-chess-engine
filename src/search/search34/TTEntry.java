package search.search34;

import eval.ScoreEncoder;

public final class TTEntry {
	public final static int CUTOFF_TYPE_EXACT = 0;
	public final static int CUTOFF_TYPE_UPPER = 1;
	public final static int CUTOFF_TYPE_LOWER = 2;
	
	public long zkey;
	public int score;
	/** score encoding from {@link ScoreEncoder}*/
	public int staticEval;
	public long move;
	public int depth;
	public int seq;
	public int cutoffType;
	
	public void clear(){
		zkey = 0;
		score = 0;
		staticEval = 0;
		move = 0;
		depth = 0;
		seq = 0;
		cutoffType = 0;
	}
	
	public void fill(TTEntry t){
		this.zkey = t.zkey;
		this.move = t.move;
		this.score = t.score;
		this.staticEval = t.staticEval;
		this.depth = t.depth;
		this.cutoffType = t.cutoffType;
		this.seq = t.seq;
	}
}
