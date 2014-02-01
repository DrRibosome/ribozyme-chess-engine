package chess.eval.e9.pipeline;

import chess.eval.e9.Weight;
import chess.eval.e9.mobilityEval.MobilityEval;
import chess.state4.State4;

/** mobility scoring stage, requires {@linkplain chess.eval.e9.pipeline.MobilityProvider}*/
public final class Stage2 implements LateStage {

	public final static class CutoffCheck implements LateStage{
		private final MidStage next;
		private final int stage;

		public CutoffCheck(MidStage next, int stage){
			this.next = next;
			this.stage = stage;
		}

		@Override
		public EvalResult eval(Team allied, Team enemy, AdvancedAttributes basics, EvalContext c, State4 s, int score) {
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

	private final CutoffCheck cutoffCheck;
	private final int stage;

	public Stage2(MidStage next, int stage){
		cutoffCheck = new CutoffCheck(next, stage);
		this.stage = stage;
	}

	@Override
	public EvalResult eval(Team allied, Team enemy, AdvancedAttributes adv, EvalContext c, State4 s, int previousScore) {
		int stage2Score = previousScore;

		MobilityEval.MobilityResult alliedMobility = adv.alliedMobility;
		MobilityEval.MobilityResult enemyMobility = adv.enemyMobility;

		stage2Score += alliedMobility.score - enemyMobility.score;

		int score = Weight.interpolate(stage2Score, c.scale) +
				Weight.interpolate(S((int)(Weight.egScore(stage2Score)*.1), 0), c.scale);

		return cutoffCheck.eval(allied, enemy, adv, c, s, previousScore);
	}

	/** build a weight scaling from passed start,end values*/
	private static int S(int start, int end){
		return Weight.encode(start, end);
	}
}
