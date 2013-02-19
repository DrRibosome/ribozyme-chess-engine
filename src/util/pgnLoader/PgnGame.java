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
		int[][] m = new int[moves.size()][2];
		State4 s = new State4();
		s.initialize();
		final int len = moves.size();
		int player = 0;
		for(int a = 0; a < len; a++){
			System.arraycopy(AlgebraicNotation2.getPos(player, moves.get(a), s), 0, m[a], 0, 2);
			player = 1-player;
		}
		this.moves = m;
		this.outcome = outcome.equals("1-0")? whiteWin: outcome.equals("0-1")? blackWin: tie;
	}
}
