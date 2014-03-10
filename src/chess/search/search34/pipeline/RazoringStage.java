package chess.search.search34.pipeline;

import chess.search.search34.Search34;
import chess.search.search34.StackFrame;
import chess.state4.State4;

/**quick check for refutation of previous refutation (cut) move */
public final class RazoringStage implements MidStage {

	private final StackFrame[] stack;
	private final Search34 searcher;
	private final MidStage next;

	public RazoringStage(StackFrame[] stack, Search34 searcher, MidStage next){
		this.stack = stack;
		this.searcher = searcher;
		this.next = next;
	}

	@Override
	public int eval(int player, int alpha, int beta, int depth, int nt, int stackIndex, State4 s) {

		StackFrame frame = stack[stackIndex];

		if(nt == SearchContext.NODE_TYPE_ALL &&
				frame.nonMateScore &&
				depth <= 1* Search34.ONE_PLY &&
				!frame.alliedKingAttacked &&
				!frame.hasTTMove &&
				!frame.pawnPrePromotion){

			final int razorMargin = 270;
			if(frame.eval + razorMargin < alpha){
				final int r = alpha-razorMargin;
				final int v = searcher.qsearch(player, r-1, r, 0, stackIndex+1, nt, s);
				if(v <= r-1){
					//fail low, probably cant recover from their refutation move
					return v+razorMargin;
				}
			}
		}

		return next.eval(player, alpha, beta, depth, nt, stackIndex, s);
	}
}
