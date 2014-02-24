package chess.uci;

import chess.state4.State4;

public final class Position {
	public int sideToMove;
	public final State4 s;
	public int halfMoves;
	public int fullMoves;

	public Position(long stateSeed){
		s = new State4(stateSeed);
	}

	public Position(){
		s = new State4();
	}

	public void startPos(){
		sideToMove = State4.WHITE;
		s.initialize();
		halfMoves = 0;
		fullMoves = 0;
	}
}
