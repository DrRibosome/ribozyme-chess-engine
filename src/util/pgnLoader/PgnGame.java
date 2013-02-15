package util.pgnLoader;

import java.util.List;

import state4.State4;
import util.AlgebraicNotation2;

public class PgnGame {
	public final static int whiteWin = 0;
	public final static int blackWin = 0;
	public final static int tie = 0;
	
	List<String> moves;
	String outcome;
	
	public static int[][] getMoves(final PgnGame g){
		int[][] m = new int[g.moves.size()][2];
		State4 s = new State4();
		s.initialize();
		final int len = g.moves.size();
		int player = 0;
		for(int a = 0; a < len; a++){
			System.arraycopy(AlgebraicNotation2.getPos(player, g.moves.get(a), s), 0, m[a], 0, 2);
			player = 1-player;
		}
		return m;
	}
	
	public static int getWinningPlayer(){
		return 0;
	}
}
