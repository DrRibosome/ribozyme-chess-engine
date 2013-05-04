package search.parallel;

import java.util.concurrent.atomic.AtomicInteger;

import state4.State4;

public final class SplitPoint {
	public final long[] moveList;
	/** gives number of moves stored*/
	public int len;
	/** move index to search next*/
	public int moveIndex;
	/** stores number of completed moves*/
	public AtomicInteger completed ;
	/** stores total number of moves to search in this node*/
	public int totalMoves;
	public int alpha;
	public int beta;
	public boolean pv;
	public int depth;
	public final AtomicInteger score = new AtomicInteger();
	
	public SplitPoint(int size){
		moveList = new long[size];
	}
	
	/** loads this split point into passed state*/
	public static void loadSplitState(final SplitPoint p, final SplitQueue.SplitState ss){
		final State4 s = ss.s;
		s.initialize();
		final int len = p.len;
		for(int a = 0; a < len; a++){
			s.executeMove(a%2, p.moveList[a]);
		}
		
		ss.moveIndex = p.moveIndex++;
		ss.score = p.score;
		ss.sideToMove = p.len%2;
		ss.alpha = p.alpha;
		ss.beta = p.beta;
		ss.pv = p.pv;
		ss.depth = p.depth;
	}
}
