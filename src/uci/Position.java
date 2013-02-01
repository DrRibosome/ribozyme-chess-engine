package uci;

import util.board4.State4;

public class Position {
	int sideToMove;
	State4 s;
	int halfMoves;
	int fullMoves;
	
	public static Position startPos(){
		Position p = new Position();
		p.sideToMove = State4.WHITE;
		p.s = new State4();
		p.s.initialize();
		return p;
	}
}
