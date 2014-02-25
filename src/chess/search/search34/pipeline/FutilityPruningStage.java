package chess.search.search34.pipeline;

import chess.search.search34.Search34;
import chess.state4.State4;

/** futility pruning, refute previous passive move when we are really far ahead */
public final class FutilityPruningStage implements MidStage {

	private final MidStage next;

	public FutilityPruningStage(MidStage next){
		this.next = next;
	}

	@Override
	public int eval(SearchContext c, NodeProps props, State4 s) {
		if(c.nt != SearchContext.NODE_TYPE_PV &&
				c.depth <= 3 * Search34.ONE_PLY &&
				!props.pawnPrePromotion &&
				!props.alliedKingAttacked &&
				props.hasNonPawnMaterial &&
				//c.futilityPrune &&
				props.nonMateScore){

			final int futilityMargin;
			if(c.depth <= 1 * Search34.ONE_PLY){
				futilityMargin = 250;
			} else if(c.depth <= 2 * Search34.ONE_PLY){
				futilityMargin = 300;
			} else { //depth <= 3*ONE_PLY
				futilityMargin = 425;
			}

			final int futilityScore = props.eval - futilityMargin;

			if(futilityScore >= c.beta){
				return futilityScore;
			}
		}

		return next.eval(c, props, s);
	}
}
