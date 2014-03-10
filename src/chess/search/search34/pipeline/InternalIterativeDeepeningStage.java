package chess.search.search34.pipeline;

import chess.search.search34.Hash;
import chess.search.search34.Search34;
import chess.search.search34.TTEntry;
import chess.state4.State4;

/** internal iterative deepening*/
public final class InternalIterativeDeepeningStage implements MidStage {

	private final Hash m;
	private final Search34 searcher;
	private final MidStage next;

	public InternalIterativeDeepeningStage(Hash m, Search34 searcher, MidStage next){
		this.m = m;
		this.searcher = searcher;
		this.next = next;
	}

	@Override
	public int eval(SearchContext c, NodeProps props, State4 s) {

		if(!props.hasTTMove && props.nonMateScore &&
				c.depth > 6*Search34.ONE_PLY &&
				c.nt == SearchContext.NODE_TYPE_PV){
			final int d = c.depth/2;
			searcher.recurse(new SearchContext(c.player, c.alpha, c.beta, d, c.nt, c.stackIndex + 1, true), s);
			final TTEntry temp = m.get(props.zkey);
			if(temp != null && temp.move != 0){
				return next.eval(c, props.addTTEMove(temp.move), s);
			}
		}

		return next.eval(c, props, s);
	}
}
