package util.pgnLoader;

import java.util.List;

import state4.State4;
import util.AlgebraicNotation2;

public final class PgnGame {
	public final static int whiteWin = 0;
	public final static int blackWin = 1;
	public final static int tie = 2;
	
	/** moves, indexed [halfmove-num][move]*/
	public final int[][] moves;
	/** game outcome*/
	public final int outcome;
	
	PgnGame(final List<String> moves, final String outcome){
		//System.out.println("building game...");
		int[][] m = new int[moves.size()][2];
		State4 s = new State4();
		s.initialize();
		final int len = moves.size();
		int turn = 0;
		//System.out.println("game moves = "+len);
		for(int a = 0; a < len; a++){
			try{
				//System.out.println("move "+a+" = "+moves.get(a));
				System.arraycopy(AlgebraicNotation2.getPos(turn, moves.get(a), s), 0, m[a], 0, 2);
				s.executeMove(turn, 1L<<m[a][0], 1L<<m[a][1]);
				turn = 1-turn;
				s.resetHistory();
			} catch(AssertionError e){
				int[][] tempm = new int[a][2];
				for(int q = 0; q < a; q++) System.arraycopy(m[q], 0, tempm[q], 0, 2);
				m = tempm;
				break;
			}
		}
		this.moves = m;
		this.outcome = outcome.equals("1-0")? whiteWin: outcome.equals("0-1")? blackWin: tie;
		//System.out.println("game complete");
	}
}
