package chess.eval.e9.pipeline;

import chess.eval.e9.mobilityEval.MobilityEval;
import chess.state4.BitUtil;
import chess.state4.Masks;
import chess.state4.State4;

/** helper class to provide mobility analysis to later stages*/
public class MobilityProvider implements MidStage{

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

	private final LateStage next;
	private final MobilityEval mobEval;

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

	public MobilityProvider(MobilityEval.MobilityWeights mobWeights, LateStage next) {
		this.next = next;
		this.mobEval = new MobilityEval(mobWeights);
	}

	@Override
	public EvalResult eval(Team allied, Team enemy, BasicAttributes basics, EvalContext c, State4 s, int previousScore) {
		final long whitePawnAttacks = Masks.getRawPawnAttacks(0, s.pawns[0]);
		final long blackPawnAttacks = Masks.getRawPawnAttacks(1, s.pawns[1]);
		final long pawnAttacks = whitePawnAttacks | blackPawnAttacks;
		final double clutterMult = clutterIndex[(int) BitUtil.getSetBits(pawnAttacks)];

		MobilityEval.MobilityResult alliedMobility = mobEval.scoreMobility(c.player, s, clutterMult);
		MobilityEval.MobilityResult enemyMobility = mobEval.scoreMobility(1-c.player, s, clutterMult);

		AdvancedAttributes adv = new AdvancedAttributes(basics, alliedMobility, enemyMobility);
		return next.eval(allied, enemy, adv, c, s, previousScore);
	}
}
