package chess.search.search34.pipeline;

import chess.state4.State4;

public interface MidStage {
	public int eval(int player, int alpha, int beta, int depth, int nodeType, int stackIndex, State4 s);
}
