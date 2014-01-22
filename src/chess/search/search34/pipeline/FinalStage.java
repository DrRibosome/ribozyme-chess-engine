package chess.search.search34.pipeline;

import chess.state4.State4;

public interface FinalStage {
	public int eval(SearchContext c, NodeProps props, KillerMoveSet kms, State4 s);
}
