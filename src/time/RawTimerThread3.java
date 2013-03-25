package time;

import java.util.concurrent.LinkedBlockingQueue;

import search.Search4;
import search.SearchListener2;
import state4.State4;

/** timer thread that merely follows the suggested time*/
public final class RawTimerThread3 extends Thread{
	private final static int failLow = -1;
	private final static int failHigh = 1;
	
	private final Search4 search;
	private final State4 s;
	private long time;
	private long inc;
	private int player;
	private int[] moveStore;
	
	/** stores extra time from aspiration window failures*/
	private final LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>();
	private final static class PlySearchResult{
		int ply;
		long move;
	}
	private final LinkedBlockingQueue<PlySearchResult> plyq = new LinkedBlockingQueue<>();
	
	private final SearchListener2 l = new SearchListener2() {
		@Override
		public void plySearched(long move, int ply, int score) {
			PlySearchResult temp = new PlySearchResult();
			temp.move = move;
			temp.ply = ply;
			//plyq.add(temp);
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
	
	private RawTimerThread3(Search4 search, State4 s, int player, long time, long inc, int[] moveStore) {
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
		search.setListener(l);
		
		//System.out.println("moves remaining = "+(getHalfMovesRemaining(material)/2));
		//System.out.println("target time = "+target);
		
		final Thread t = new Thread(){
			public void run(){
				search.search(player, s, moveStore, 99);
			}
		};
		t.setDaemon(true);
		t.start();
		
		while(System.currentTimeMillis()-start < target){
			final long diff = target-(System.currentTimeMillis()-start);
			if(diff/2 >= 10){
				try{
					Thread.sleep(diff/2);
				} catch(InterruptedException e){}
			}
		}
		
		while(t.isAlive()){
			search.cutoffSearch();
			try{
				t.join(500);
			} catch(InterruptedException e){}
		}
	}
	
	public static void search(Search4 search, State4 s, int player, long time, long inc, int[] moveStore){
		RawTimerThread3 t = new RawTimerThread3(search, s, player, time, inc, moveStore);
		t.start();
		while(t.isAlive()){
			try{
				t.join();
			} catch(InterruptedException e){}
		}
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
