package chess.eval.e9.pipeline;

import chess.state4.State4;

public final class StageWrapper implements MidStage {

	private final MidStage next;

	public StageWrapper(MidStage next){
		this.next = next;
	}

	@Override
	public EvalResult eval(Team allied, Team enemy, EvalBasics basics, EvalContext c, State4 s, int previousScore) {
		return next.eval(allied, enemy, basics, c, s, previousScore);
	}
}
