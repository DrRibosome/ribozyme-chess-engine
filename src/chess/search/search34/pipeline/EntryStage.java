package chess.search.search34.pipeline;

import chess.state4.State4;

public interface EntryStage {
	public int eval(SearchContext c, State4 s);
}
