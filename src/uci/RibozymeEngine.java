package uci;

import search.MoveSet;
import search.Search;
import search.search34.Search34;
import util.FenParser;
import util.SearchThread;
import util.TimerThread;
import eval.Evaluator;
import eval.e9.E9;

public final class RibozymeEngine implements UCIEngine{

	private final static String name = "ribozyme";
	
	private final Search s;
	private final MoveSet moveStore = new MoveSet();
	private final TimerThread timerThread;
	private final SearchThread searchThread;
	
	public RibozymeEngine(final int hashSize, final int pawnHashSize, boolean printInfo, boolean warmUp){
		
		final Evaluator e = new E9(pawnHashSize);
		
		s = new Search34(e, hashSize, printInfo);

		if(warmUp){
			//warm up via a number of fixed depth searches
			final long fenSeed = 34827L;
			final int searchDepth = 7;
			
			String[] warmUpFens = new String[]{
					"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
					"2k5/pp1r2b1/2p5/7P/2P2r1q/5pN1/PPb2P1P/2Q1RRK1 w - - 0 27",
					"7r/q7/2nbNk1p/3p1B2/1n1P2P1/4Q2P/5PK1/4R3 b - - 0 34",
					"3r2k1/1pb1qppp/2p1r3/p7/P1Q5/1PPbPB1P/3BKPP1/R2R4 w - - 0 21"
			};
			
			for(int a = 0; a < warmUpFens.length; a++){
				Position p = FenParser.parse(warmUpFens[a], fenSeed);
				s.search(p.sideToMove, p.s, null, searchDepth);
			}
			
			s.resetSearch();
		}
		
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
