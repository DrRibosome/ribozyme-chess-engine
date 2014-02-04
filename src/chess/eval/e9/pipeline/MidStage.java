package chess.eval.e9.pipeline;

import chess.state4.State4;

public interface MidStage {
	/** evaluates given board state features and calculates static score */
	public EvalResult eval(Team allied, Team enemy, BasicAttributes basics, EvalContext c, State4 s, int previousScore);
}
