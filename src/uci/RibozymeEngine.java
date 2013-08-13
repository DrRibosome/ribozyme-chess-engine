package uci;

import search.MoveSet;
import search.Search4;
import search.search34.Search34v4;
import state4.BitUtil;
import state4.Masks;
import state4.State4;
import time.TimerThread5;
import eval.Evaluator3;
import eval.e9.E9v3;

public final class RibozymeEngine implements UCIEngine{

	private final static String name = "ribozyme 0.1.1";
	
	private final Search4 s;
	private Thread t;
	private final MoveSet moveStore = new MoveSet();
	
	public RibozymeEngine(final int hashSize, final int pawnHashSize){
		
		final Evaluator3 e = new E9v3(pawnHashSize);
		
		s = new Search34v4(e, hashSize, true);
	}
	
	@Override
	public String getName(){
		return name;
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
	
	@Override
	public void go(final GoParams params, final Position p) {
		final int player = p.sideToMove;
		if(params.type == GoParams.SearchType.plan){ //allocate time
			t = new Thread(){
				public void run(){
					final int inc = params.increment[player];
					TimerThread5.searchBlocking(s, p.s, player, params.time[player], inc, moveStore);
					System.out.println("bestmove "+buildMoveString(player, p.s, moveStore));
				}
			};
			t.setDaemon(true);
			t.start();
		} else if(params.type == GoParams.SearchType.fixedTime){ //fixed time per move
			t = new Thread(){
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
			timer.start();
		} else if(params.type == GoParams.SearchType.infinite){
			assert false;
			t = new Thread(){
				public void run(){
					s.search(player, p.s, moveStore);
					System.out.println("bestmove "+buildMoveString(player, p.s, moveStore));
				}
			};
			t.setDaemon(true);
			t.start();
		} else if(params.type == GoParams.SearchType.fixedDepth){
			assert false;
			t = new Thread(){
				public void run(){
					s.search(player, p.s, moveStore, params.depth);
					System.out.println("bestmove "+buildMoveString(player, p.s, moveStore));
				}
			};
			t.setDaemon(true);
			t.start();
		}
	}
	
	private static String posString(int pos){
		return ""+(char)('a'+pos%8)+(char)('1'+pos/8);
	}

	@Override
	public void stop() {
		s.cutoffSearch();
		final Thread t = this.t;
		if(t != null){
			while(t.isAlive()){
				try{
					t.join();
				} catch(InterruptedException e){}
			}
		}
	}

	@Override
	public void resetEngine() {
		stop();
		s.resetSearch();
	}

}
