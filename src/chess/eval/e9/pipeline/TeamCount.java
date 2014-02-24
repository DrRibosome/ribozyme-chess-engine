package chess.eval.e9.pipeline;


import chess.state4.State4;

public final class TeamCount {
	final int pawnCount;
	final int knightCount;
	final int bishopCount;
	final int rookCount;
	final int queenCount;

	private TeamCount(int pawnCount,
					  int knightCount,
					  int bishopCount,
					  int rookCount,
					  int queenCount){
		this.pawnCount = pawnCount;
		this.knightCount = knightCount;
		this.bishopCount = bishopCount;
		this.rookCount = rookCount;
		this.queenCount = queenCount;
	}

	public static TeamCount load(int player, State4 s){
		return new TeamCount(s.pieceCounts[player][State4.PIECE_TYPE_PAWN],
				s.pieceCounts[player][State4.PIECE_TYPE_KNIGHT],
				s.pieceCounts[player][State4.PIECE_TYPE_BISHOP],
				s.pieceCounts[player][State4.PIECE_TYPE_ROOK],
				s.pieceCounts[player][State4.PIECE_TYPE_QUEEN]);
	}
}
