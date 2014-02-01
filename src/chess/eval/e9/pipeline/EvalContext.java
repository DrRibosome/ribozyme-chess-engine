package chess.eval.e9.pipeline;

/** provides general static context for state evaluation*/
public class EvalContext {
	/** player for which the evaluation is for*/
	final int player;

	/** lower bound on the eval score, alpha in the search context*/
	final int lowerBound;
	/** upper bound on the eval score, beta in the search context*/
	final int upperBound;

	/** weight scaling factor*/
	final double scale;

	EvalContext(int player, int lowerBound, int upperBound, double scale){
		this.player = player;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.scale = scale;
	}
}
