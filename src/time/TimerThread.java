package time;

import java.util.concurrent.LinkedBlockingQueue;

import search.Search3;
import util.board4.State4;

public final class TimerThread extends Thread{
	public static interface Callback{
		/** called when the search is complete*/
		public void complete();
	}
	
	private final Callback c;
	
	private ListenableSearch search;
	private State4 s;
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
	
	private boolean sleep = true;
	
	private final SearchListener l = new SearchListener() {
		@Override
		public void plySearched(long move, int ply) {
			PlySearchResult temp = new PlySearchResult();
			temp.move = move;
			temp.ply = ply;
			plyq.add(temp);
		}
		@Override
		public void failLow(int ply) {
			q.add(-1);
		}
		@Override
		public void failHigh(int ply) {
			q.add(1);
		}
	};
	
	public TimerThread(Callback c) {
		setDaemon(true);
		start();
		this.c = c;
	}
	
	@Override
	public void run(){
		synchronized(this){
			for(;;){
				while(sleep){
					try{
						wait();
					} catch(InterruptedException e){}
				}
				
				long start = System.currentTimeMillis();
				final int material = getMaterial(s);
				long target = time / (getHalfMovesRemaining(material)/2);
				q.clear();
				plyq.clear();
				search.setListener(l);
				
				final Thread t = new Thread(){
					public void run(){
						search.search(player, moveStore, 99);
					}
				};
				t.setDaemon(true);
				t.start();
				
				boolean pvChanged = false;
				long move = 0;
				int maxPly = 0;
				while(System.currentTimeMillis()-start < target){
					while(!plyq.isEmpty()){
						PlySearchResult r = plyq.poll();
						if(r.ply != 1 && r.move != move){
							pvChanged = true;
							target += target*.2;
						}
						move = r.move;
						maxPly = r.ply > maxPly? r.ply: maxPly;
					}
					
					if(!pvChanged && maxPly > 8){
						break;
					}
					
					//handle adjustments from search failures
					while(!q.isEmpty()){
						int failType = q.poll();
						target += -failType*target*.3;
					}
				}
				
				search.cutoffSearch();
				while(t.isAlive()){
					try{
						t.join();
					} catch(InterruptedException e){}
				}
				sleep = true;
				c.complete();
			}
		}
	}
	
	public void search(ListenableSearch search, State4 s, int player, long time, long inc, int[] moveStore){
		synchronized(this){
			this.search = search;
			this.s = s;
			this.player = player;
			this.time = time;
			this.inc = inc;
			this.moveStore = moveStore;
			wakeTimer();
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
			return (int)(5./4-30);
		}
	}

	
	private void wakeTimer(){
		synchronized(this){
			sleep = false;
			notify();
		}
	}
};
