package eval.e8;

public final class PawnHashEntry {
	public int score;
	public long passedPawns;
	public long zkey;
	int seq;
	
	public void clear(){
		score = 0;
		passedPawns = 0;
		zkey = 0;
	}
	
	public static void fill(final PawnHashEntry src, final PawnHashEntry target){
		target.score = src.score;
		target.passedPawns = src.passedPawns;
		target.zkey = src.zkey;
	}
}
