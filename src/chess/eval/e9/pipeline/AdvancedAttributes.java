package chess.eval.e9.pipeline;

import chess.eval.e9.mobilityEval.MobilityEval;

/** holds state attributes gleaned through time intensive late stage evaluations*/
public final class AdvancedAttributes extends EvalBasics{
	public final MobilityEval.MobilityResult alliedMobility;
	public final MobilityEval.MobilityResult enemyMobility;

	public AdvancedAttributes(EvalBasics basics,
							  MobilityEval.MobilityResult alliedMobility,
							  MobilityEval.MobilityResult enemyMobility) {
		super(basics.materialScore, basics.nonPawnMaterialScore, basics.totalMaterialScore);
		this.alliedMobility = alliedMobility;
		this.enemyMobility = enemyMobility;
	}
}
