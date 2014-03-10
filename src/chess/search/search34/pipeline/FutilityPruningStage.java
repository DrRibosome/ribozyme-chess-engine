package chess.search.search34.pipeline;

import chess.search.search34.Search34;
import chess.search.search34.StackFrame;
import chess.state4.State4;

/** futility pruning, refute previous passive move when we are really far ahead */
public final class FutilityPruningStage implements MidStage {

	private final StackFrame[] stack;
	private final MidStage next;

	public FutilityPruningStage(StackFrame[] stack, MidStage next){
		this.stack = stack;
		this.next = next;
	}

	@Override
	public int eval(int player, int alpha, int beta, int depth, int nt, int stackIndex, State4 s) {

		StackFrame frame = stack[stackIndex];

		if(nt != SearchContext.NODE_TYPE_PV &&
				depth <= 3 * Search34.ONE_PLY &&
				!frame.pawnPrePromotion &&
				!frame.alliedKingAttacked &&
				frame.hasNonPawnMaterial &&
				//c.futilityPrune &&
				frame.nonMateScore){

			final int futilityMargin;
			if(depth <= 1 * Search34.ONE_PLY){
				futilityMargin = 250;
			} else if(depth <= 2 * Search34.ONE_PLY){
				futilityMargin = 300;
			} else { //depth <= 3*ONE_PLY
				futilityMargin = 425;
			}

			final int futilityScore = frame.eval - futilityMargin;

			if(futilityScore >= beta){
				return futilityScore;
			}
		}

		return next.eval(player, alpha, beta, depth, nt, stackIndex, s);
	}
}
