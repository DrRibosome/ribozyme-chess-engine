package chess.search.search34.pipeline;

import chess.search.MoveSet;
import chess.search.search34.MoveGen;
import chess.search.search34.Search34;
import chess.search.search34.TTEntry;
import chess.state4.MoveEncoder;
import chess.state4.State4;

/** internal iterative deepening*/
public class InternalIterativeDeepeningStage implements MidStage {

	private final Search34 searcher;
	private final MidStage next;

	public InternalIterativeDeepeningStage(Search34 searcher, MidStage next){
		this.searcher = searcher;
		this.next = next;
	}

	@Override
	public int eval(SearchContext c, NodeProps props, State4 s) {
		if(!props.hasTTMove && c.depth >= (c.nt == NodeType.pv? 5: 8)*Search34.ONE_PLY && props.nonMateScore &&
				(c.nt == NodeType.pv || (!props.alliedKingAttacked && props.eval+256 >= c.beta))){
			final int d = c.nt == NodeType.pv? c.depth-2*Search34.ONE_PLY: c.depth/2;
			//stack[stackIndex+1].futilityPrune = false; //would never have arrived here if futility pruned above, set false
			searcher.recurse(new SearchContext(c.player, c.alpha, c.beta, d, c.nt, c.stackIndex + 1, true), s);
			final TTEntry temp;
			if((temp = m.get(zkey)) != null && temp.move != 0){
				tteMove = true;
				tteMoveEncoding = temp.move;
				final MoveSet tempMset = mset[w++];
				tempMset.piece = 1L<< MoveEncoder.getPos1(tteMoveEncoding);
				tempMset.moves = 1L<<MoveEncoder.getPos2(tteMoveEncoding);
				tempMset.rank = MoveGen.tteMoveRank;
			}
		}
	}
}
