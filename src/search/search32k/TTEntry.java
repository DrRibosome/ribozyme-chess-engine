package search.search32k;

public final class TTEntry {
	public final static int CUTOFF_TYPE_EXACT = 0;
	public final static int CUTOFF_TYPE_UPPER = 1;
	public final static int CUTOFF_TYPE_LOWER = 2;
	
	public long zkey;
	public double score;
	public long move;
	public int depth;
	public int seq;
	public int cutoffType;
	
	public void clear(){
		zkey = 0;
		score = 0;
		move = 0;
		depth = 0;
		seq = 0;
		cutoffType = 0;
	}
	
	public void fill(final long zkey, final long move, final double score, final int depth,
			final int cutoffType, final int seq){
		this.zkey = zkey;
		this.move = move;
		this.score = score;
		this.depth = depth;
		this.cutoffType = cutoffType;
		this.seq = seq;
	}
	
	public void fill(TTEntry t){
		this.zkey = t.zkey;
		this.move = t.move;
		this.score = t.score;
		this.depth = t.depth;
		this.cutoffType = t.cutoffType;
		this.seq = t.seq;
	}
	
	public static void swap(TTEntry s1, TTEntry s2){
		final long temp1 = s1.zkey;
		s1.zkey = s2.zkey;
		s2.zkey = temp1;
		
		final double temp2 = s1.score;
		s1.score = s2.score;
		s2.score = temp2;
		
		final long temp3 = s1.move;
		s1.move = s2.move;
		s2.move = temp3;
		
		final int temp4 = s1.depth;
		s1.depth = s2.depth;
		s2.depth = temp4;
		
		final int temp5 = s1.seq;
		s1.seq = s2.seq;
		s2.seq = temp5;
		
		final int temp6 = s1.cutoffType;
		s1.cutoffType = s2.cutoffType;
		s2.cutoffType = temp6;
	}
}
