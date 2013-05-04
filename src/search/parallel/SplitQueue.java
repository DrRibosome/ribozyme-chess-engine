package search.parallel;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import state4.State4;

public final class SplitQueue {
	public final static class SplitState{
		public final State4 s;
		/** index of the move to be considered by this thread*/
		public int moveIndex;
		/** node score <p> note, this could be out of date for the final thread*/
		public AtomicInteger score;
		public int sideToMove;
		public int alpha;
		public int beta;
		public boolean pv;
		public int depth;
		public AtomicBoolean betaCufoff = new AtomicBoolean();
		public SplitPoint sp;
		public SplitPoint parent;
		public SplitState(State4 s){
			this.s = s;
		}
	}
	
	private final SplitPoint[] q;
	/** stores queue length*/
	private final int maxLen;
	/** gets index of first unused index*/
	private AtomicInteger endIndex = new AtomicInteger();
	/** gets index of first used index*/
	private AtomicInteger startIndex = new AtomicInteger();
	private AtomicInteger len = new AtomicInteger();
	
	public SplitQueue(final int queueLen, final int historyLen){
		q = new SplitPoint[queueLen];
		for(int a = 0; a < queueLen; a++){
			q[a] = new SplitPoint(historyLen);
		}
		this.maxLen = queueLen;
	}
	
	public boolean get(SplitState ss){
		if(len.get() != 0){
			final int index = startIndex.get()%maxLen;
			final SplitPoint p = q[index];
			synchronized(p){
				if(p.completed++ < p.totalMoves){
					SplitPoint.loadSplitState(p, ss);
					return true;
				}
			}
			startIndex.incrementAndGet();
			return get(ss);
		}
		return false;
	}
	
	public boolean add(final State4 s, final int moveIndex, final int totalMoves,
			final int initialScore, final int alpha, final int beta){
		if(len.get() < maxLen){
			final int index = this.endIndex.getAndIncrement()%maxLen;
			final SplitPoint p = q[index];
			synchronized(p){
				if(p.completed >= p.totalMoves){
					load(s, moveIndex, totalMoves, p, initialScore, alpha, beta);
					return true;
				}
			}
			return add(s, moveIndex, totalMoves, initialScore, alpha, beta);
		}
		return false;
	}
	
	/** load node information into passed split point*/
	private static void load(final State4 s, final int moveIndex,
			final int totalMoves, final SplitPoint p, final int initialScore,
			final int alpha, final int beta){
		synchronized(p){
			p.moveIndex = moveIndex;
			p.completed = moveIndex;
			p.totalMoves = totalMoves;
			p.len = s.copyHistory(p.moveList);
			p.score.set(initialScore);
		}
	}
}
