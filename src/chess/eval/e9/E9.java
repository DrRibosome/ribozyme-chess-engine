package chess.eval.e9;

import chess.eval.e9.pipeline.*;
import chess.state4.BitUtil;
import chess.state4.Masks;
import chess.state4.MoveEncoder;
import chess.state4.State4;
import chess.eval.Evaluator;
import chess.eval.PositionMasks;
import chess.eval.ScoreEncoder;
import chess.eval.e9.mobilityEval.MobilityEval;
import chess.eval.e9.pawnEval.PawnEval;

/** evaluator implementation responsible for building eval pipelines
 * and correctly routing evaluation calss*/
public final class E9 implements Evaluator{

	public final static class EvalWeights{
		PieceWeights pieceWeights = new PieceWeights();
		MobilityEval.MobilityWeights mobWeights = new MobilityEval.MobilityWeights();
	}

	/** evaluation pipeline*/
	private final EntryStage pipeline;
	private final PawnHash pawnHash;
	
	public E9(){
		this(16, new EvalWeights());
	}
	
	public E9(int pawnHashSize, EvalWeights weights){
		pawnHash = new PawnHash(pawnHashSize, 16);


		LateStage stage3 = new Stage3(2);
		LateStage stage2 = new Stage2(stage3, 1);
		MidStage mobilityProvider = new MobilityProvider(weights.mobWeights, stage2);
		MidStage stage1 = new Stage1(pawnHash, mobilityProvider, 0);
		EntryStage pipeline = new EvalInitializer(weights.pieceWeights, stage1);

		this.pipeline = pipeline;
	}
	
	@Override
	public EvalResult eval(final int player, final State4 s) {
		return refine(player, s, -90000, 90000, 0);
	}

	@Override
	public EvalResult eval(final int player, final State4 s, final int lowerBound, final int upperBound) {
		return refine(player, s, lowerBound, upperBound, 0);
	}

	@Override
	public EvalResult refine(final int player, final State4 s, final int lowerBound,
			final int upperBound, final long scoreEncoding) {
		
		/*int score = ScoreEncoder.getScore(scoreEncoding);
		int margin = ScoreEncoder.getMargin(scoreEncoding);
		int flags = ScoreEncoder.getFlags(scoreEncoding);
		boolean isLowerBound = ScoreEncoder.isLowerBound(scoreEncoding);
		
		if((flags != 0 && ((score+margin <= lowerBound && isLowerBound) || (score+margin >= upperBound && !isLowerBound))) ||
				flags == 3){
			return scoreEncoding;
		}*/

		return pipeline.eval(player, lowerBound, upperBound, s);
	}
	
	@Override
	public void reset(){}
}
