package chess.eval.e9.pipeline;


import chess.state4.State4;

public final class Team {
	final long pawns;
	final long knights;
	final long bishops;
	final long rooks;
	final long queens;
	final long king;

	private Team(long pawns,
				 long knights,
				 long bishops,
				 long rooks,
				 long queens,
				 long king){
		this.pawns = pawns;
		this.knights = knights;
		this.bishops = bishops;
		this.rooks = rooks;
		this.queens = queens;
		this.king = king;
	}

	public static Team load(int player, State4 s){
		return new Team(s.pieceMasks[State4.PIECE_TYPE_PAWN][player],
				s.pieceMasks[State4.PIECE_TYPE_KNIGHT][player],
				s.pieceMasks[State4.PIECE_TYPE_BISHOP][player],
				s.pieceMasks[State4.PIECE_TYPE_ROOK][player],
				s.pieceMasks[State4.PIECE_TYPE_QUEEN][player],
				s.pieceMasks[State4.PIECE_TYPE_KING][player]);
	}
}
