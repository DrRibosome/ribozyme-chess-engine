package chess.search.search34.pipeline;

import chess.state4.State4;

public interface MidStage {
	public int eval(SearchContext c, NodeProps props, State4 s);
}
