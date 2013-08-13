package uci;

import search.MoveSet;
import search.Search4;
import search.search34.Search34v4;
import state4.BitUtil;
import state4.Masks;
import state4.State4;
import util.SearchThread;
import util.TimerThread;
import eval.Evaluator3;
import eval.e9.E9v3;

public final class RibozymeEngine implements UCIEngine{

	private final static String name = "ribozyme 0.1.1";
	
	private final Search4 s;
	private final MoveSet moveStore = new MoveSet();
	private final TimerThread timerThread;
	private final SearchThread searchThread;
	
	public RibozymeEngine(final int hashSize, final int pawnHashSize){
		
		final Evaluator3 e = new E9v3(pawnHashSize);
		
		s = new Search34v4(e, hashSize, true);
		
		searchThread = new SearchThread(s);
		timerThread = new TimerThread(searchThread);
		timerThread.start();
		searchThread.start();
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
			timerThread.startTimePlanningSearch(p.s, player, params.time[player], params.increment[player], moveStore);
		} else if(params.type == GoParams.SearchType.fixedTime){ //fixed time per move
			timerThread.startFixedTimeSearch(p.s, player, params.time[player], moveStore);
		} else if(params.type == GoParams.SearchType.infinite){
			assert false;
			searchThread.startSearch(player, p.s, moveStore);
		} else if(params.type == GoParams.SearchType.fixedDepth){
			assert false;
			searchThread.startSearch(player, p.s, moveStore, params.depth);
		}
	}
	
	private static String posString(int pos){
		return ""+(char)('a'+pos%8)+(char)('1'+pos/8);
	}

	@Override
	public void stop() {
		s.cutoffSearch();
		final Thread t = this.timerThread;
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
