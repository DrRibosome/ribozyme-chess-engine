package chess.eval.e9.pipeline;

import chess.eval.ScoreEncoder;
import chess.eval.e9.Weight;
import chess.eval.e9.mobilityEval.MobilityEval;
import chess.state4.BitUtil;
import chess.state4.Masks;
import chess.state4.State4;

public final class Stage2 implements MidStage {

	public final static class CutoffCheck implements MidStage{
		private final MidStage next;
		private final int stage;

		public CutoffCheck(MidStage next, int stage){
			this.next = next;
			this.stage = stage;
		}

		@Override
		public EvalResult eval(Team allied, Team enemy, EvalBasics basics, EvalContext c, State4 s, int score) {
			//stage 2 margin related to how much we expect the score to change
			//maximally due to king safety
			final int stage2MarginLower;
			final int stage2MarginUpper;
			if(allied.queenCount != 0 && enemy.queenCount != 0){
				//both sides have queen, apply even margin
				stage2MarginLower = 3;
				stage2MarginUpper = -3;
			} else if(allied.queenCount != 0){
				//score will be higher because allied queen, no enemy queen
				stage2MarginLower = 3;
				stage2MarginUpper = -3;
			}  else if(enemy.queenCount != 0){
				//score will be lower because enemy queen, no allied queen
				stage2MarginLower = 2;
				stage2MarginUpper = -4;
			} else{
				//both sides no queen, aplly even margin
				stage2MarginLower = 0;
				stage2MarginUpper = -0;
			}

			final boolean lowerBoundCutoff = score+stage2MarginLower <= c.lowerBound;
			final boolean upperBoundCutoff = score+stage2MarginUpper >= c.upperBound;
			if(!lowerBoundCutoff || !upperBoundCutoff){
				return new EvalResult(score, stage2MarginLower, stage2MarginUpper, stage);
			} else{
				return next.eval(allied, enemy, basics, c, s, score);
			}
		}
	}

	/**
	 * gives bonus multiplier to the value of sliding pieces
	 * mobilitiy scores based on how cluttered the board is
	 * <p>
	 * sliding pieces with high movement on a clutterd board
	 * are more valuable
	 * <p>
	 * indexed [num-pawn-attacked-squares]
	 */
	private final static double[] clutterIndex;

	private final MidStage cutoffCheck;
	private final int stage;

	static{
		//clutterIndex calculated by linear interpolation
		clutterIndex = new double[64];
		final double start = .8;
		final double end = 1.2;
		final double diff = end-start;
		for(int a = 0; a < 64; a++){
			clutterIndex[a] = start + diff*(a/63.);
		}
	}

	public Stage2(MidStage next, int stage){
		cutoffCheck = new CutoffCheck(next, stage);
		this.stage = stage;
	}

	@Override
	public EvalResult eval(Team allied, Team enemy, EvalBasics basics, EvalContext c, State4 s, int previousScore) {
		final long whitePawnAttacks = Masks.getRawPawnAttacks(0, s.pawns[0]);
		final long blackPawnAttacks = Masks.getRawPawnAttacks(1, s.pawns[1]);
		final long pawnAttacks = whitePawnAttacks | blackPawnAttacks;
		final double clutterMult = clutterIndex[(int) BitUtil.getSetBits(pawnAttacks)];

		int stage2Score = previousScore;

		MobilityEval.MobilityResult alliedMobility = MobilityEval.scoreMobility(c.player, s, clutterMult);
		MobilityEval.MobilityResult enemyMobility = MobilityEval.scoreMobility(1-c.player, s, clutterMult);

		stage2Score += alliedMobility.score - enemyMobility.score;
		
		int score = Weight.interpolate(stage2Score, c.scale) +
				Weight.interpolate(S((int)(Weight.egScore(stage2Score)*.1), 0), c.scale);

		if(allied.queenCount + enemy.queenCount == 0){
			flags++; //increment flag to mark stage 3 as complete
			//return ScoreEncoder.encode(score, 0, flags, true);
			return new EvalResult(score, 0, 0, stage);
		} else{
			//record that attack masks were just calculated in stage 2
			needsAttackMaskRecalc = false;
		}

		return cutoffCheck.eval(allied, enemy, basics, c, s, previousScore);
	}

	/** build a weight scaling from passed start,end values*/
	private static int S(int start, int end){
		return Weight.encode(start, end);
	}
}
