package chess.eval.e9.pipeline;

import chess.state4.State4;

/** enter evalueation pipeline*/
public interface EntryStage {
	public EvalResult eval(int player, int lowerBound, int upperBound, State4 s);
}
