package tests;

import java.util.ArrayList;
import java.util.List;

import util.board2.State2;
import util.board4.State4;
import ai.modularAI2.Evaluator2;
import ai.modularAI2.Search2;
import customAI.evaluators.board4.SuperEvalS4V6;
import customAI.evaluators.board4.SuperEvalS4V7;
import customAI.searchers.board4.SearchS4V19qzit;
import customAI.searchers.board4.SearchS4V20qzit;
import eval.TestEval;

/**
 * Simple launcher for playing two AIs. Prints board state after each move
 * and stops when a king has died. Uses {@link State2} for board representation
 * @author jdc2172
 *
 */
public class Launcher3 {
	public static void main(String[] args){
		
		//s2 board for pretty output
		State2 s2 = new State2(100);
		s2.initialize();
		
		
		final long maxTime = 1*1000;
		Evaluator2<State4> e1 =
				new SuperEvalS4V7();
				//new SuperEvalS4V4();
		
		Evaluator2<State4> e2 = 
				//new SuperEvalS4V5();
				//new SuperEvalS4V6();
				new TestEval();
		
		State4 state = new State4();
		state.initialize();
		
		List<Search2<State4>> l = new ArrayList<Search2<State4>>();
		Search2<State4> search1 = new SearchS4V20qzit(12, state, e1, 20);
		Search2<State4> search2 = new SearchS4V20qzit(12, state, e2, 20);
		
		l.add(search1);
		l.add(search2);
		
		State2 b = new State2(100);
		b.initialize();
		int[] move = new int[4];
		int turn = State2.PLAYER_1;
		boolean complete = false;
		
		printBoard(b);
		
		while(!complete){
			System.out.println("moving player "+turn+"...");
			l.get(turn).getMove(move, turn, maxTime);
			state.executeMove(turn, move[0], move[1], move[2], move[3]);
			
			System.out.println("move chosen: "+getMoveString(move, 0)+" -> "+getMoveString(move, 2));
			b.executeMove(turn, move[0], move[1], move[2], move[3]);
			System.out.println("eval: " + e1.eval(state, turn));
			System.out.println("new state:");
			printBoard(b);
			
			if(!b.isAlive(State2.getOppositePlayer(turn))){
				complete = true;
			} else{
				turn = State2.getOppositePlayer(turn);
			}
		}
		
		System.out.println("player "+turn+" wins");
		System.exit(0);
	}
	
	private static void printBoard(State2 b){
		System.out.println(b);
		System.out.println("=======================");
	}
	
	/**
	 * 
	 * @param l
	 * @param i offset
	 * @return
	 */
	private static String getMoveString(int[] l, int i){
		return ""+(char)('A'+l[i])+(l[i+1]+1);
	}
}
