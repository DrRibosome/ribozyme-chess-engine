package util.analysis;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

import search.Search4;
import search.search33.Search33v5;
import state4.BitUtil;
import state4.State4;
import state4.StateUtil;
import time.TimerThread5;
import util.opening2.Book;
import eval.Evaluator2;
import eval.e7.E7v7;

/**
 * Simple launcher for playing two AIs. Prints board state after each move
 * and stops when a king has died. Uses {@link State2} for board representation
 * @author jdc2172
 *
 */
public class GauntletP {
	final static Book b = new Book(new File("megabook.bk"));
	
	private enum SearchType{
		Depth,
		FixedTime,
		ControlledTime
	}
	
	private final static class GauntletThread extends Thread{
		private final long testTime;
		private final long[] regressTimes;
		
		public final AtomicInteger[][] counts;
		private final int maxDrawCount;
		private final int minCutoffScore;
		private final SearchType type;
		private final Evaluator2 test;
		public final Evaluator2[] regressors;
		
		GauntletThread(long testTime, long[] regressTimes, Evaluator2 test, Evaluator2[] regressors,
				int maxDrawCount, int minCutoffScore, SearchType type){
			this.testTime = testTime;
			this.regressTimes = regressTimes;
			this.maxDrawCount = maxDrawCount;
			this.minCutoffScore = minCutoffScore;
			this.type = type;
			
			this.test = test;
			this.regressors = regressors;
			counts = new AtomicInteger[regressors.length][3];
			for(int q = 0; q < regressors.length; q++){
				for(int a = 0; a < counts[0].length; a++){
					counts[q][a] = new AtomicInteger(0);
				}
			}
		}
		
		public void run(){
			final boolean print = false;

			final int[] wins = new int[2];
			int draws = 0;

			int[] move = new int[2];
			for(int w = 0; ; w++){
				State4 state = new State4(b.getSeed(), maxDrawCount);
				state.initialize();
				int turn = 0;
				boolean draw = false;
				boolean outOfBook = false;
				
				final int rindex = w%regressors.length;
				final AtomicInteger[] counts = this.counts[rindex];
				final int searchOffset = (counts[0].get()+counts[1].get()+counts[2].get())%2;
				
				//(w0,w1,d) = (1040,1451,1041)
				final Search4[] search = new Search4[]{
						new Search33v5(test, 20, false),
						new Search33v5(regressors[rindex], 20, false),
				};
				final long[] times = new long[]{
						testTime, regressTimes[rindex]
				};
				
				boolean[] endScoreCutoff = new boolean[2];

				while(state.pieceCounts[turn][State4.PIECE_TYPE_KING] != 0 &&
						!isMate(turn, state) && !state.isForcedDraw()){
					
					final int index = (turn+searchOffset)%2;
					final long searchTime = times[index];
					final Search4 searcher = search[index];

					if(print) System.out.println("===========================================================");
					if(print) System.out.println("search0 = "+search[0].getClass().getSimpleName()+
							", search1 = "+search[1].getClass().getSimpleName());
					if(print) System.out.println("current: (w0,w1,d) = ("+wins[0]+","+wins[1]+","+draws+")");
					if(print) System.out.println("moving player "+turn);
					if(print) System.out.println(StateUtil.fen(turn, state));
					if(print) System.out.println("search state:\n"+state);
					if(print) System.out.println("draw count = "+state.drawCount);
					
					
					int depth = 3; //default 6
					for(int a = 0; a < 2; a++){
						if(state.pieceCounts[a][State4.PIECE_TYPE_QUEEN] == 0) depth++;
						int pieces = state.pieceCounts[a][State4.PIECE_TYPE_BISHOP]+
								state.pieceCounts[a][State4.PIECE_TYPE_KNIGHT]+
								state.pieceCounts[a][State4.PIECE_TYPE_ROOK];
						if(pieces == 0) depth++;
						if(pieces <= 3) depth++;
					}
					//System.out.println("searching with "+search[(turn+searchOffset)%2].getClass().getSimpleName());

					int[] bookMove = b.getRandomMove(turn, state, 100);
					if(bookMove != null && !outOfBook){
						System.arraycopy(bookMove, 0, move, 0, 2);
						if(print) System.out.println("book move");
					} else{
						outOfBook = true;
						if(type == SearchType.Depth) search[(turn+searchOffset)%2].search(turn, state, move, depth);
						if(type == SearchType.ControlledTime) TimerThread5.searchBlocking(searcher, state, turn, searchTime, 0, move);
						if(type == SearchType.FixedTime) search(turn, state, searcher, move, searchTime);
						
						
						endScoreCutoff[(turn+searchOffset)%2] = Math.abs(search[(turn+searchOffset)%2].getStats().predictedScore) >= minCutoffScore;
						if(endScoreCutoff[0] && endScoreCutoff[1]){
							turn = search[(turn+searchOffset)%2].getStats().predictedScore < 0? turn: 1-turn;
							break;
						}
					}
					
					if(print) System.out.println("search time = "+search[turn].getStats().searchTime);
					if(print) System.out.println("move = "+getMoveString(move, 0)+" -> "+getMoveString(move, 1));
					if(print) System.out.println();
					
					if(move[0] == move[1]){
						//System.out.println("no move draw");
						draw = true;
						break;
					}
					
					state.executeMove(turn, 1L<<move[0], 1L<<move[1]);
					turn = 1-turn;
				}

				if(state.isForcedDraw() || draw){
					//fos.write(draw);
					if(print) System.out.println("draw");
					draws++;
					counts[2].incrementAndGet();
				} else{
					final int winner = 1-turn;
					//System.out.println(evals[(winner+searchOffset)%2]+" wins");
					//fos.write(winner == 0? whiteWin: blackWin);
					if(print) System.out.println("player "+winner+" wins");
					wins[(winner+searchOffset)%2]++;
					counts[(winner+searchOffset)%2].incrementAndGet();
				}
				
				//System.out.println("--- (w0,w1,d) = ("+wins[0]+","+wins[1]+","+draws+")");
			}
		}
	}
	
	public static void main(String[] args) throws IOException{
		
		final long testTime = 700; //time for test eval
		final long[] regressTimes = new long[]{ //times for regress evals
				700
		};
		
		final int maxDrawCount = 50;
		
		final int minCutoffScore = 800; //score before cutting off a game
		
		final SearchType searchType = SearchType.Depth;

		final int threads = 4;
		final GauntletThread[] t = new GauntletThread[threads];
		
		//(w0,w1,d) = (0,1,6)
		
		System.out.print("initializing... ");
		for(int a = 0; a < threads; a++){
			
			final Evaluator2 test = 
					new E7v7();
			
			final Evaluator2[] regressors = new Evaluator2[]{
					//new E7(),
					//new E7v2(),
					//new E7v3(),
					//new E7v4(),
					new E7v7(),
					//new E7v7temp(),
			};
			
			t[a] = new GauntletThread(testTime, regressTimes, 
					test, regressors,
					maxDrawCount, minCutoffScore,
					searchType);
			t[a].setDaemon(true);
		}
		System.out.println("complete");
		
		final DecimalFormat df = new DecimalFormat("#.######");
		Thread controller = new Thread(){
			public void run(){
				final int[][] counts = new int[t[0].counts.length][3]; //wins0, wins1, draws
				for(;;){
					
					for(int w = 0; w < t[0].counts.length; w++)
						for(int a = 0; a < 3; a++)
							counts[w][a] = 0;
					
					for(int a = 0; a < threads; a++){
						for(int w = 0; w < t[a].counts.length; w++){
							for(int q = 0; q < 3; q++){
								counts[w][q] += t[a].counts[w][q].get();
							}
							
							
						}
					}

					System.out.println("-------------------------------");
					for(int a = 0; a < counts.length; a++){
						System.out.println(t[0].test.getClass().getSimpleName()+" - "+
								t[0].regressors[a].getClass().getSimpleName()+":");
						
						//bound on probability of observing these results,
						//calculated using hoeffding's inequality
						final int n = counts[a][0]+counts[a][1]+counts[a][2];
						final double t = Math.abs(counts[a][0]-counts[a][1])*1./n;
						final double prob = n != 0? Math.exp(-Math.pow(t, 2)*n/2.): 1;
						
						System.out.println("agg"+a+" (w0,w1,d) = ("+counts[a][0]+","+counts[a][1]+","+counts[a][2]+"), prob <= "+df.format(prob));
						
					}
					
					try{
						Thread.sleep(5*1000);
					} catch(InterruptedException e){}
				}
			}
		};
		controller.start();
		
		for(int a = 0; a < threads; a++){
			t[a].start();
		}
	}
	
	private static void search(final int player, final State4 s, final Search4 search, final int[] moveStore, final long targetTime){
		final long start = System.currentTimeMillis();
		
		final Thread t = new Thread(){
			public void run(){
				search.search(player, s, moveStore, 99);
			}
		};
		t.setDaemon(true);
		t.start();
		
		while(System.currentTimeMillis()-start < targetTime){
			final long diff = targetTime-(System.currentTimeMillis()-start);
			if(diff/2 >= 1){
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

	private final static int[] pawnOffset = new int[]{9,7,8,16};
	/** checks that the player to move is not mated using their king*/
	private static boolean isMate(int turn, State4 s){
		if(!State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s)){
			return false;
		}
		final long kings = s.kings[turn];
		for(long m = State4.getKingMoves(turn, s.pieces, s.kings[turn]); m != 0; m &= m-1){
			s.executeMove(turn, kings, m&-m);
			boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
			s.undoMove();
			if(!attacked) return false;
		}
		for(long queens = s.queens[turn]; queens != 0; queens &= queens-1){
			final long queen = queens&-queens;
			for(long m = State4.getQueenMoves(turn, s.pieces, queen); m != 0; m &= m-1){
				s.executeMove(turn, queen, m&-m);
				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
				s.undoMove();
				if(!attacked) return false;
			}
		}
		for(long knights = s.knights[turn]; knights != 0; knights &= knights-1){
			final long knight = knights&-knights;
			for(long m = State4.getKnightMoves(turn, s.pieces, knight); m != 0; m &= m-1){
				s.executeMove(turn, knight, m&-m);
				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
				s.undoMove();
				if(!attacked) return false;
			}
		}
		for(long bishops = s.knights[turn]; bishops != 0; bishops &= bishops-1){
			final long bishop = bishops&-bishops;
			for(long m = State4.getBishopMoves(turn, s.pieces, bishop); m != 0; m &= m-1){
				s.executeMove(turn, bishop, m&-m);
				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
				s.undoMove();
				if(!attacked) return false;
			}
		}
		for(long rooks = s.knights[turn]; rooks != 0; rooks &= rooks-1){
			final long rook = rooks&-rooks;
			for(long m = State4.getRookMoves(turn, s.pieces, rook); m != 0; m &= m-1){
				s.executeMove(turn, rook, m&-m);
				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
				s.undoMove();
				if(!attacked) return false;
			}
		}
		final long[] temp = new long[4];
		temp[0] = State4.getRightPawnAttacks(turn, s.pieces, s.enPassante, s.pawns[turn]);
		temp[1] = State4.getLeftPawnAttacks(turn, s.pieces, s.enPassante, s.pawns[turn]);
		temp[2] = State4.getPawnMoves(turn, s.pieces, s.pawns[turn]);
		temp[3] = State4.getPawnMoves2(turn, s.pieces, s.pawns[turn]);
		for(int a = 0; a < 4; a++){
			for(long pawnsMoves = temp[a]; pawnsMoves != 0; pawnsMoves &= pawnsMoves-1){
				final long move = pawnsMoves&-pawnsMoves;
				final long pawn = turn == 0? move>>>pawnOffset[a]: move<<pawnOffset[a];
				s.executeMove(turn, pawn, move&-move);
				boolean attacked = State4.isAttacked2(BitUtil.lsbIndex(s.kings[turn]), 1-turn, s);
				s.undoMove();
				if(!attacked) return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param l
	 * @param i offset
	 * @return
	 */
	private static String getMoveString(int[] l, int i){
		return ""+(char)('A'+l[i]%8)+(l[i]/8+1);
	}
}
