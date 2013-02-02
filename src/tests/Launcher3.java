package tests;

import search.Search3;
import search.SearchS4V31;
import time.TimerThread3;
import util.board4.BitUtil;
import util.board4.State4;
import eval.Evaluator2;
import eval.SuperEvalS4V8;
import eval.expEvalV1.ExpEvalV1;

/**
 * Simple launcher for playing two AIs. Prints board state after each move
 * and stops when a king has died. Uses {@link State2} for board representation
 * @author jdc2172
 *
 */
public class Launcher3 {
	public static void main(String[] args){
		State4 state = new State4();
		state.initialize();
		//State4 state = Debug.loadConfig(OldPositions.queenKingMate);
		//State4 state = Debug.loadConfig(OldPositions.queenRookKingMate);
		//State4 state = Debug.loadConfig(OldPositions.bishopBishopMate);
		
		Evaluator2<State4> e1 =
				//new SuperEvalS4V8();
				new ExpEvalV1();
		
		Evaluator2<State4> e2 = 
				new SuperEvalS4V8();
				//new ExpEvalV1();
		
		final Search3[] search = new Search3[2];
		search[0] = new SearchS4V31(state, e1, 20, false);
		search[1] = new SearchS4V31(state, e2, 20, false);
		
		int[] move = new int[2];
		int turn = State4.WHITE;
		
		System.out.println(state);
		final long startTime = 60*1000;
		long[] time = new long[]{startTime, startTime};
		final long inc = 0;
		
		while(!isMate(turn, state) && time[turn] > 0){
			
			System.out.println("moving player "+turn);
			System.out.println("time remaining = "+time[turn]);
			long t = System.currentTimeMillis();
			TimerThread3.search(search[turn], state, turn, time[turn], inc, move);
			t = System.currentTimeMillis()-t;
			
			time[turn] -= t;
			state.executeMove(turn, 1L<<move[0], 1L<<move[1]);
			System.out.println("search time = "+search[turn].getStats().searchTime);
			System.out.println("move = "+getMoveString(move, 0)+" -> "+getMoveString(move, 1));
			System.out.println(state);
			turn = 1-turn;
		}
		
		System.out.println("player "+(1-turn)+" wins");
		System.exit(0);
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
	
	private static void search(final Search3 s, final int player,
			final int maxPly, final long searchTime, final int[] move){
		Thread t = new Thread(){
			public void run(){
				s.search(player, move, maxPly);
			}
		};
		t.setDaemon(true);
		t.start();
		long start = System.currentTimeMillis();
		while(t.isAlive() && System.currentTimeMillis()-start < searchTime){
			try{
				Thread.sleep(30);
			} catch(InterruptedException e){}
		}
		
		s.cutoffSearch();
		while(t.isAlive()){
			try{
				t.join();
			} catch(InterruptedException e){}
		}
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
