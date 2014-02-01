package chess.eval.e9.pipeline;

import chess.state4.State4;

/** eval stage for computations involving {@linkplain chess.eval.e9.pipeline.AdvancedAttributes}
 * computed previously*/
public interface LateStage {
	public EvalResult eval(Team allied, Team enemy,
						   AdvancedAttributes adv,
						   EvalContext c, State4 s,
						   int previousScore);
}
