package time;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import search.Search4;
import search.SearchListener2;
import state4.BitUtil;
import state4.State4;

public final class TimerThread5 extends Thread{
	public static interface Controller{
		public void stopSearch();
		public boolean isFinished();
	}
	
	private final static int failLow = -1;
	private final static int failHigh = 1;
	private final AtomicBoolean stopSearch = new AtomicBoolean(false);
	private final AtomicBoolean isFinished = new AtomicBoolean(false);
	
	private final Search4 search;
	private final State4 s;
	private long time;
	private long inc;
	private int player;
	private int[] moveStore;
	
	private static final boolean debug = false;
	
	/** stores extra time from aspiration window failures*/
	private final LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<Integer>();
	private final static class PlySearchResult{
		int ply;
		long move;
		int score;
	}
	private final LinkedBlockingQueue<PlySearchResult> plyq = new LinkedBlockingQueue<PlySearchResult>();
	
	private final SearchListener2 l = new SearchListener2() {
		@Override
		public void plySearched(long move, int ply, int score) {
			PlySearchResult temp = new PlySearchResult();
			temp.move = move;
			temp.ply = ply;
			temp.score = score;
			plyq.add(temp);
			TimerThread5.this.interrupt();
		}
		@Override
		public void failLow(int ply) {
			//q.add(failLow);
		}
		@Override
		public void failHigh(int ply) {
			//q.add(failHigh);
		}
	};
	
	private TimerThread5(Search4 search, State4 s, int player, long time, long inc, int[] moveStore) {
		setDaemon(true);
		this.search = search;
		this.s = s;
		this.player = player;
		this.time = time;
		this.inc = inc;
		this.moveStore = moveStore;
	}
	
	@Override
	public void run(){
		long start = System.currentTimeMillis();
		final int material = getMaterial(s);
		long target = time / (getHalfMovesRemaining(material)/2);
		target *= .95;
		target += .8*inc;
		
		
		search.setListener(l);
		//final long maxTime = (long)(time*(material >= 60? .04: .06));
		long maxTime = (long)(target * 1.3);
		
		if(debug){
			System.out.println("moves remaining = "+(getHalfMovesRemaining(material)/2));
			System.out.println("target time = "+target);
			System.out.println("max time = "+maxTime);
		}
		
		final Thread t = new Thread(){
			public void run(){
				search.search(player, s, moveStore);
			}
		};
		t.setDaemon(true);
		t.start();
		
		long move = 0;
		int currentPly = 0;
		int lastpvChange = 1;
		int currentScore = 0;
		
		final boolean checking = isChecked(0, s) | isChecked(1, s);
		
		
		int minDepth = 17;
		
		
		while(System.currentTimeMillis()-start < target &&
				System.currentTimeMillis()-start < maxTime && 
				!stopSearch.get()){ //so we dont keep extending forever
			
			while(!plyq.isEmpty()){
				PlySearchResult r = plyq.poll();
				if(r.ply != 1 && r.move != move){
					lastpvChange = r.ply;
				}
				move = r.move;
				currentPly = r.ply;
				currentScore = r.score;
			}
			
			if(currentScore < 70000){
				if(!checking && currentPly-lastpvChange+1 > 7 && currentPly >= 17){ //perhaps increase difficulty with fail lows
					break;
					//target -= target*.2;
				} else if(checking && currentPly-lastpvChange+1 > 8 && currentPly >= 19){
					break;
				}
			}
			if(currentScore > 80000){
				break;
			}
			
			//handle adjustments from search failures
			while(!q.isEmpty()){
				int failType = q.poll();
				//double scale = failType == failHigh? .005: .01;
				double scale = 0;
				target += target*scale;
			}

			final long remainingTime = (target-(System.currentTimeMillis()-start))/2;
			if(remainingTime > 10){
				try{
					Thread.sleep(remainingTime);
				} catch(InterruptedException e){}
			}
		}
		
		search.cutoffSearch();
		while(t.isAlive()){
			search.cutoffSearch();
			try{
				t.join(500);
			} catch(InterruptedException e){}
		}
		isFinished.set(true);
	}
	
	private static long min(long l1, long l2){
		return l1 < l2? l1: l2;
	}
	
	/** tests to see if the passed player is in check*/
	private static boolean isChecked(final int player, final State4 s){
		return State4.isAttacked2(BitUtil.lsbIndex(s.kings[player]), 1-player, s);
	}
	
	public static void searchBlocking(Search4 search, State4 s, int player, long time, long inc, int[] moveStore){
		final TimerThread5 t = new TimerThread5(search, s, player, time, inc, moveStore);
		t.start();
		while(t.isAlive()){
			try{
				t.join();
			} catch(InterruptedException e){}
		}
	}
	
	public static Controller searchNonBlocking(Search4 search, State4 s, int player, long time, long inc, int[] moveStore){
		final TimerThread5 t = new TimerThread5(search, s, player, time, inc, moveStore);
		final Controller temp = new Controller(){
			@Override
			public void stopSearch() {
				t.endSearch();
			}
			@Override
			public boolean isFinished() {
				return t.isFinished.get();
			}
		};
		t.start();
		return temp;
	}
	
	private void endSearch(){
		stopSearch.set(true);
		interrupt();
	}
	
	private static int getMaterial(State4 s){
		int m = 0;
		for(int a = 0; a < 2; a++){
			m += s.pieceCounts[a][State4.PIECE_TYPE_BISHOP]*3;
			m += s.pieceCounts[a][State4.PIECE_TYPE_KNIGHT]*3;
			m += s.pieceCounts[a][State4.PIECE_TYPE_PAWN]*1;
			m += s.pieceCounts[a][State4.PIECE_TYPE_QUEEN]*9;
			m += s.pieceCounts[a][State4.PIECE_TYPE_ROOK]*5;
		}
		return m;
	}
	
	/**
	 * gets remaining half moves as calculated by
	 * 'time management procedures in computer chess'
	 * @param material
	 * @return returns half moves remaining
	 */
	private static int getHalfMovesRemaining(int material){
		if(material < 20){
			return material+10;
		} else if(material <= 60){
			return (int)(3./8*material+22);
		} else{
			return (int)(5./4*material-30);
		}
	}
};
