package util;

import java.util.concurrent.Semaphore;

import search.MoveSet;
import search.Search4;
import search.SearchListener2;
import state4.State4;

/** always on daemon thread for searching*/
public final class SearchThread extends Thread{
	private final static class SearchParams{
		volatile boolean infiniteSearch;
		volatile State4 s;
		volatile int player;
		volatile MoveSet moveStore;
		volatile int maxDepth;
	}
	
	private final Search4 s;
	volatile boolean searching = false;
	private final SearchParams p = new SearchParams();
	private final Semaphore sem = new Semaphore(1);
	
	public SearchThread(Search4 s){
		this.s = s;
		setDaemon(false);
	}
	
	public void setSearchListener(SearchListener2 l){
		sem.acquireUninterruptibly();
		s.setListener(l);
		sem.release();
	}
	
	public void startSearch(int player, State4 s, MoveSet moveStore){
		synchronized(p){
			p.infiniteSearch = true;
			p.player = player;
			p.s = s;
			p.moveStore = moveStore;
			
			searching = true;
		}
		
		interrupt();
	}
	
	public void startSearch(int player, State4 s, MoveSet moveStore, int maxDepth){
		synchronized(p){
			p.infiniteSearch = false;
			p.player = player;
			p.s = s;
			p.moveStore = moveStore;
			p.maxDepth = maxDepth;
			
			searching = true;
		}
		
		interrupt();
	}
	
	public void stopSearch(){
		s.cutoffSearch();
		sem.acquireUninterruptibly();
		sem.release();
	}
	
	@Override
	public void run(){
		for(;;){
			if(searching){
				synchronized(p){
					sem.acquireUninterruptibly();
					if(p.infiniteSearch){
						s.search(p.player, p.s, p.moveStore);
					} else{
						s.search(p.player, p.s, p.moveStore, p.maxDepth);
					}
					sem.release();
					searching = false;
				}
			}
			
			if(!searching){
				synchronized(this){
					try{
						wait();
					} catch(InterruptedException e){}
				}
			}
		}
	}
}
