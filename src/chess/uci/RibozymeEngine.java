package chess.uci;

import chess.search.MoveSet;
import chess.search.Search;
import chess.search.search34.Search34;
import chess.util.FenParser;
import chess.util.SearchThread;
import chess.util.TimerThread;
import chess.eval.Evaluator;
import chess.eval.e9.E9;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public final class RibozymeEngine implements UCIEngine{

	private final static String name = "ribozyme";

	private final Evaluator e;
	private final Search s;
	private final MoveSet moveStore = new MoveSet();
	private final TimerThread timerThread;
	private final SearchThread searchThread;
	
	public RibozymeEngine(final int hashSize, final int pawnHashSize, boolean printInfo, boolean warmUp){
		
		this.e = new E9(pawnHashSize);
		
		s = new Search34(e, hashSize, printInfo);

		if(warmUp){
			//warm up via a number of fixed depth searches
			final int searchDepth = 7;
			
			String[] warmUpFens = new String[]{
					"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
					"2k5/pp1r2b1/2p5/7P/2P2r1q/5pN1/PPb2P1P/2Q1RRK1 w - - 0 27",
					"7r/q7/2nbNk1p/3p1B2/1n1P2P1/4Q2P/5PK1/4R3 b - - 0 34",
					"3r2k1/1pb1qppp/2p1r3/p7/P1Q5/1PPbPB1P/3BKPP1/R2R4 w - - 0 21"
			};

			Position p = new Position();
			for(int a = 0; a < warmUpFens.length; a++){
				FenParser.parse(warmUpFens[a], p);
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
	public Evaluator getEval() {
		return e;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public void profile(File fens) {
		try(Scanner scanner = new Scanner(fens)){
			final int searchDepth = 7;

			long nodes = 0;
			long totalTime = 0;
			int entries = 0;

			Position p = new Position();
			while(scanner.hasNextLine()){
				entries++;
				FenParser.parse(scanner.nextLine(), p);

				long start = System.currentTimeMillis();
				s.search(p.sideToMove, p.s, null, searchDepth);
				totalTime += System.currentTimeMillis() - start;
				nodes += s.getStats().nodesSearched;

				s.resetSearch();

				if(entries % 50 == 0 || !scanner.hasNextLine()){
					System.out.println("------------------------");
					System.out.println("avg nodes/msec = "+(nodes*1./totalTime)+" = "+nodes+" / "+totalTime);
					System.out.println("avg nodes = "+(nodes*1./entries)+" = "+nodes+" / "+entries);
					System.out.println("avg time (msec) = "+(totalTime*1./entries)+" = "+totalTime+" / "+entries);
				}
			}

		} catch (IOException e){
			e.printStackTrace();
		}
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
