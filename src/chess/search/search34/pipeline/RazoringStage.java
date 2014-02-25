package chess.search.search34.pipeline;

import chess.search.search34.Search34;
import chess.state4.State4;

/**quick check for refutation of previous refutation (cut) move */
public class RazoringStage implements MidStage {

	private final Search34 searcher;
	private final MidStage next;

	public RazoringStage(Search34 searcher, MidStage next){
		this.searcher = searcher;
		this.next = next;
	}

	@Override
	public int eval(SearchContext c, NodeProps props, State4 s) {

		if(c.nt == SearchContext.NODE_TYPE_ALL &&
				props.nonMateScore &&
				c.depth <= 1* Search34.ONE_PLY &&
				!props.alliedKingAttacked &&
				!props.hasTTMove &&
				!props.pawnPrePromotion){

			final int razorMargin = 270;
			if(props.eval + razorMargin < c.alpha){
				final int r = c.alpha-razorMargin;
				final int v = searcher.qsearch(c.player, r-1, r, 0, c.stackIndex+1, c.nt, s);
				if(v <= r-1){
					//fail low, probably cant recover from their refutation move
					return v+razorMargin;
				}
			}
		}

		return next.eval(c, props, s);
	}
}
