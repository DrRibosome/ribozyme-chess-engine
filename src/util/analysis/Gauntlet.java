package util.analysis;

import java.io.File;
import java.io.IOException;

import search.Search4;
import search.search32k.SearchS4V32k;
import search.search33.SearchS4V33t;
import state4.BitUtil;
import state4.State4;
import state4.StateUtil;
import time.RawTimerThread3;
import util.opening2.Book;
import eval.Evaluator2;
import eval.evalV9.SuperEvalS4V9;

/**
 * Simple launcher for playing two AIs. Prints board state after each move
 * and stops when a king has died. Uses {@link State2} for board representation
 * @author jdc2172
 *
 */
public class Gauntlet {
	private static final int whiteWin = 0;
	private static final int blackWin = 1;
	private static final int draw = 2;
	
	public static void main(String[] args) throws IOException{
		
		final boolean print = false;
		final int tests = 99999;
		final int maxDepth = 5;
		final int hashSize = 20;
		final long time = 15*1000;

		Evaluator2<State4> e1 =
				new SuperEvalS4V9();
				//new ExpEvalV2();

		Evaluator2<State4> e2 = 
				new SuperEvalS4V9();
				//new SuperEvalS4V8();
				//new ExpEvalV1();
		
		final Search4[] search = new Search4[2];
		search[0] = new SearchS4V33t(e1, hashSize, false);
		search[1] = new SearchS4V32k(e2, hashSize, false);
		final Book b = new Book(new File("megabook.bk"));

		final int[] wins = new int[2];
		int draws = 0;

		int[] move = new int[2];
		for(int w = 0; w < tests; w++){
			State4 state = new State4(b.getSeed());
			state.initialize();
			int turn = 0;
			final int searchOffset = w%2;
			boolean draw = false;
			for(int a = 0; a < 2; a++) search[a].resetSearch();

			while(state.pieceCounts[turn][State4.PIECE_TYPE_KING] != 0 &&
					!isMate(turn, state) && !state.isForcedDraw()){

				if(print) System.out.println("===========================================================");
				if(print) System.out.println("moving player "+turn);
				if(print) System.out.println(StateUtil.fen(turn, state));
				if(print) System.out.println("search state:\n"+state);
				if(print) System.out.println("draw count = "+state.drawCount);

				int[] bookMove = b.getRandomMove(turn, state);
				if(bookMove != null){
					System.arraycopy(bookMove, 0, move, 0, 2);
					if(print) System.out.println("book move");
				} else{
					//search[(turn+searchOffset)%2].search(turn, state, move, maxDepth);
					RawTimerThread3.search(search[(turn+searchOffset)%2], state, turn, time, 0, move);
				}
				
				if(print) System.out.println("search time = "+search[turn].getStats().searchTime);
				if(print) System.out.println("move = "+getMoveString(move, 0)+" -> "+getMoveString(move, 1));
				if(print) System.out.println();
				
				if(move[0] == move[1]){
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
			} else{
				final int winner = 1-turn;
				//fos.write(winner == 0? whiteWin: blackWin);
				if(print) System.out.println("player "+winner+" wins");
				wins[(winner+searchOffset)%2]++;
			}
			
			System.out.println("(w0,w1,d) = ("+wins[0]+","+wins[1]+","+draws+")");
		}
		System.out.println("draws = "+draws);
		System.out.println("wins[0] = "+wins[0]+", wins[1] = "+wins[1]);
		System.out.println("to find prob that one is better, run:");
		System.out.println("binom.test("+wins[0]+","+wins[1]+",.5,alternative=\"two.sided\")");
		System.out.println("including draws as wins for both players:");
		System.out.println("binom.test("+(wins[0]+draws)+","+(wins[1]+draws)+",.5,alternative=\"two.sided\")");
		System.out.println("eval 1 = "+e1.getClass().getName());
		System.out.println("eval 2 = "+e2.getClass().getName());
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
