package chess.search.search34.moveGen;

import chess.search.MoveSet;
import chess.state4.MoveEncoder;
import chess.state4.State4;

public final class MoveList{
	public final RankedMoveSet[] list;
	public int len = 0;

	public MoveList(int size){
		list = new RankedMoveSet[size];
		for(int a = 0; a < size; a++) list[a] = new RankedMoveSet();
	}

	public void add(long piece, long move, int rank, int promotionType){
		RankedMoveSet m = list[len++];
		m.piece = piece;
		m.moves = move;
		m.rank = rank;
		m.promotionType = promotionType;
	}

	public void add(long piece, long move, int rank){
		add(piece, move, rank, State4.PROMOTE_QUEEN);
	}

	public void add(long encoding, int rank){
		add(1L<< MoveEncoder.getPos1(encoding),
				1L<<MoveEncoder.getPos2(encoding),
				rank,
				MoveEncoder.getPawnPromotionType(encoding));
	}

	/** clear move list and ready list for reuse*/
	public void clear(){
		len = 0;
	}
}
