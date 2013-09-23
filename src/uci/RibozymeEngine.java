package uci;

import search.MoveSet;
import search.Search4;
import search.search34.Search34v4;
import util.SearchThread;
import util.TimerThread;
import eval.Evaluator3;
import eval.e9.E9;

public final class RibozymeEngine implements UCIEngine{

	private final static String name = "ribozyme 0.1.2";
	
	private final Search4 s;
	private final MoveSet moveStore = new MoveSet();
	private final TimerThread timerThread;
	private final SearchThread searchThread;
	
	public RibozymeEngine(final int hashSize, final int pawnHashSize, boolean printInfo){
		
		final Evaluator3 e = new E9(pawnHashSize);
		
		s = new Search34v4(e, hashSize, printInfo);
		
		searchThread = new SearchThread(s);
		timerThread = new TimerThread(searchThread);
		timerThread.start();
		searchThread.start();
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public void go(final GoParams params, final Position p) {
		final int player = p.sideToMove;
		if(params.type == GoParams.SearchType.plan){ //allocate time
			timerThread.startTimePlanningSearch(p.s, player, params.time[player], params.increment[player], moveStore);
		} else if(params.type == GoParams.SearchType.fixedTime){ //fixed time per move
			timerThread.startFixedTimeSearch(p.s, player, params.time[player], moveStore);
		} else if(params.type == GoParams.SearchType.infinite){
			searchThread.startSearch(player, p.s, moveStore);
		} else if(params.type == GoParams.SearchType.fixedDepth){
			searchThread.startSearch(player, p.s, moveStore, params.depth);
		}
	}

	@Override
	public void stop() {
		timerThread.stopSearch();
	}

	@Override
	public void resetEngine() {
		stop();
		s.resetSearch();
	}

}
