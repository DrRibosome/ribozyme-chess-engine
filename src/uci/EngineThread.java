package uci;

import java.util.concurrent.Semaphore;

import search.MoveSet;
import search.Search4;
import state4.BitUtil;
import state4.Masks;
import state4.State4;
import time.TimerThread5;

public class EngineThread extends Thread{
	private final static class Params{
		volatile GoParams goParams;
		volatile Position pos;
	}
	
	private final Params p = new Params();
	private volatile boolean search = false;
	private final MoveSet moveStore = new MoveSet();
	private final Search4 s;
	private final Semaphore sem = new Semaphore(1);
	
	public EngineThread(Search4 searcher){
		this.s = searcher;
		
		setDaemon(true);
	}
	
	public void startSearch(GoParams goParams, Position pos){
		synchronized(p){
			p.goParams = goParams;
			p.pos = pos;
			search = true;
			
			notify();
		}
	}
	
	@Override
	public void run(){
		for(;;){
			
			if(search){
				sem.acquireUninterruptibly();
				
				synchronized(this.p){
					final GoParams params = this.p.goParams;
					final Position p = this.p.pos;
					
					final int player = p.sideToMove;
					if(params.type == GoParams.SearchType.plan){ //allocate time
						final int inc = params.increment[player];
						TimerThread5.searchBlocking(s, p.s, player, params.time[player], inc, moveStore);
						System.out.println("bestmove "+buildMoveString(player, p.s, moveStore));
					} else if(params.type == GoParams.SearchType.fixedTime){ //fixed time per move
						/*t = new Thread(){
							public void run(){
								s.search(player, p.s, moveStore);
								System.out.println("bestmove "+buildMoveString(player, p.s, moveStore));
							}
						};
						t.setDaemon(true);
						t.start();
						final Thread timer = new Thread(){
							public void run(){
								final long start = System.currentTimeMillis();
								final long targetTime = params.moveTime;
								long time;
								while((time = System.currentTimeMillis()-start) < targetTime){
									try{
										Thread.sleep(time/2);
									} catch(InterruptedException e){}
								}
								s.cutoffSearch();
							}
						};
						timer.setDaemon(true);
						timer.start();*/
					} else if(params.type == GoParams.SearchType.infinite){
						s.search(player, p.s, moveStore);
						System.out.println("bestmove "+buildMoveString(player, p.s, moveStore));
					} else if(params.type == GoParams.SearchType.fixedDepth){
						s.search(player, p.s, moveStore, params.depth);
						System.out.println("bestmove "+buildMoveString(player, p.s, moveStore));
					}
				}
				
				search = false;
				sem.release();
			}
			
			
			while(!search){
				try{
					wait();
				} catch(InterruptedException e){}
			}
			
		}
	}
	
	public void stopSearch(){
		s.cutoffSearch();
		sem.acquireUninterruptibly(); //wait until search finishes
		sem.release();
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
		if(!isPromoting){
			return move;
		} else{
			return move+promotionChar;
		}
	}
	
	private static String posString(int pos){
		final char[] temp = new char[2];
		temp[0] = (char)('a'+pos%8);
		temp[1] = (char)('1'+pos/8);
		return new String(temp);
	}
}
