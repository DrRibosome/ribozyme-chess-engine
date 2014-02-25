package chess.search.search34.pipeline;

import chess.search.search34.Hash;
import chess.search.search34.Search34;
import chess.search.search34.TTEntry;
import chess.state4.State4;

/** internal iterative deepening*/
public class InternalIterativeDeepeningStage implements MidStage {

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
		if(!props.hasTTMove && c.depth >= (c.nt == SearchContext.NODE_TYPE_PV? 5: 8)*Search34.ONE_PLY && props.nonMateScore &&
				(c.nt == SearchContext.NODE_TYPE_PV || (!props.alliedKingAttacked && props.eval+256 >= c.beta))){
			final int d = c.nt == SearchContext.NODE_TYPE_PV? c.depth-2*Search34.ONE_PLY: c.depth/2;
			//stack[stackIndex+1].futilityPrune = false; //would never have arrived here if futility pruned above, set false
			searcher.recurse(new SearchContext(c.player, c.alpha, c.beta, d, c.nt, c.stackIndex + 1, true), s);
			final TTEntry temp = m.get(props.zkey);
			if(temp != null && temp.move != 0){
				return next.eval(c, props.addTTEMove(temp.move), s);
			}
		}

		return next.eval(c, props, s);
	}
}
