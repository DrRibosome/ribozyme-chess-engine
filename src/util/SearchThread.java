package util;

import java.util.concurrent.Semaphore;

import search.MoveSet;
import search.Search4;
import search.SearchListener2;
import state4.BitUtil;
import state4.Masks;
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
					System.out.println("bestmove "+buildMoveString(p.player, p.s, p.moveStore));
					sem.release();
					searching = false;
				}
			}
			
			while(!searching){
				synchronized(this){
					try{
						wait();
					} catch(InterruptedException e){}
				}
			}
		}
	}
	
	private static String buildMoveString(final int player, final State4 s, final MoveSet moveStore){
		final char promotionChar;
		switch(moveStore.promotionType){
		case State4.PROMOTE_QUEEN:
			promotionChar = 'q';
			break;
		case State4.PROMOTE_ROOK:
			promotionChar = 'r';
			break;
		case State4.PROMOTE_BISHOP:
			promotionChar = 'b';
			break;
		case State4.PROMOTE_KNIGHT:
			promotionChar = 'n';
			break;
		default:
			promotionChar = 'x';
			assert false;
			break;
		}
		final boolean isPromoting = (s.pawns[player] & moveStore.piece) != 0 && (moveStore.moves & Masks.pawnPromotionMask[player]) != 0;
		final String move = posString(BitUtil.lsbIndex(moveStore.piece))+posString(BitUtil.lsbIndex(moveStore.moves));
		return move+(isPromoting? promotionChar: "");
	}

	private static String posString(int pos){
		return ""+(char)('a'+pos%8)+(char)('1'+pos/8);
	}
}
