package chess.search.search34;

import chess.search.MoveSet;
import chess.state4.MoveEncoder;
import chess.state4.State4;

public class StackFrame {
	public final static class MoveList{
		public final MoveSet[] list;
		public int len = 0;

		MoveList(int size){
			list = new MoveSet[size];
			for(int a = 0; a < defSize; a++) list[a] = new MoveSet();
		}

		public void add(long piece, long move, int rank, int promotionType){
			MoveSet m = list[len++];
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

	private final static int defSize = 128;
	public final MoveList mlist = new MoveList(defSize);
	public boolean skipNullMove = false;
	/** holds killer moves as first 12 bits (ie, masked 0xFFF) of move encoding*/
	public final long[] killer = new long[2];
	/** controls whether we should futility prune*/
	public boolean futilityPrune;
}
