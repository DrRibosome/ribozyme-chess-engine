package chess.eval.e9.pipeline;

/** gives evaluation score, and lower/upper margins if applicable.
 * <p>score+lowerMargin <= score <= score+upperMargin</p>*/
public final class EvalResult {
	/** eval score*/
	public final int score;
	/** upper margin on eval score, score+lowerMargin <= score <= score+upperMargin*/
	public final int upperMargin;
	/** lower margin on eval score, score+lowerMargin <= score <= score+upperMargin*/
	public final int lowerMargin;

	/** final stage at which evaluation occurred before result was constructed*/
	public final int stage;

	EvalResult(int score, int lowerMargin, int upperMargin, int stage) {
		this.score = score;
		this.lowerMargin = lowerMargin;
		this.upperMargin = upperMargin;
		this.stage = stage;
	}
}
