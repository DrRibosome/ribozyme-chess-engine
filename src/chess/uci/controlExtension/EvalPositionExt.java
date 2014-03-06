package chess.uci.controlExtension;

import chess.eval.Evaluator;
import chess.eval.ScoreEncoder;
import chess.eval.e9.pipeline.EvalResult;
import chess.uci.Position;
import chess.uci.UCIEngine;

/** extension for calling the eval function on target board state*/
public final class EvalPositionExt implements ControlExtension {
	@Override
	public void execute(String[] args, Position pos, UCIEngine engine) {
		Evaluator e = engine.getEval();
		EvalResult result = e.eval(pos.sideToMove, pos.s);
		System.out.println("score = "+result.score+", range = ("+
				result.getScoreLowerBound()+", "+result.getScoreUpperBound()+")");
	}
}
