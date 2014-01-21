package chess.uci;

import chess.state4.State4;

public final class Position {
	public int sideToMove;
	public State4 s;
	public int halfMoves;
	public int fullMoves;
	
	public static Position startPos(){
		Position p = new Position();
		p.sideToMove = State4.WHITE;
		p.s = new State4();
		p.s.initialize();
		return p;
	}
}
