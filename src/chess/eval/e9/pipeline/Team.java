package chess.eval.e9.pipeline;


import chess.state4.State4;

public final class Team {
	final long pawns;
	final int pawnCount;
	final long knights;
	final int knightCount;
	final long bishops;
	final int bishopCount;
	final long rooks;
	final int rookCount;
	final long queens;
	final int queenCount;
	final long king;

	private Team(long pawns, int pawnCount,
				 long knights, int knightCount,
				 long bishops, int bishopCount,
				 long rooks, int rookCount,
				 long queens, int queenCount,
				 long king){
		this.pawns = pawns;
		this.pawnCount = pawnCount;
		this.knights = knights;
		this.knightCount = knightCount;
		this.bishops = bishops;
		this.bishopCount = bishopCount;
		this.rooks = rooks;
		this.rookCount = rookCount;
		this.queens = queens;
		this.queenCount = queenCount;
		this.king = king;
	}

	public static Team load(int player, State4 s){
		return new Team(s.pieceMasks[State4.PIECE_TYPE_PAWN][player], s.pieceCounts[player][State4.PIECE_TYPE_PAWN],
				s.pieceMasks[State4.PIECE_TYPE_KNIGHT][player], s.pieceCounts[player][State4.PIECE_TYPE_KNIGHT],
				s.pieceMasks[State4.PIECE_TYPE_BISHOP][player], s.pieceCounts[player][State4.PIECE_TYPE_BISHOP],
				s.pieceMasks[State4.PIECE_TYPE_ROOK][player], s.pieceCounts[player][State4.PIECE_TYPE_ROOK],
				s.pieceMasks[State4.PIECE_TYPE_QUEEN][player], s.pieceCounts[player][State4.PIECE_TYPE_QUEEN],
				s.pieceMasks[State4.PIECE_TYPE_KING][player]);
	}
}
