package chess.eval.e9.pipeline;


import chess.state4.State4;

public class EvalInitializer implements EntryStage{

	private final int endMaterial;
	private final int scaleMargin;

	private final MidStage next;

	private final int pawnWeight;
	private final int bishopWeight;
	private final int knightWeight;
	private final int rookWeight;
	private final int queenWeight;

	public EvalInitializer(PieceWeights pieceWeights, MidStage next){
		this.next = next;

		this.pawnWeight = pieceWeights.pawn;
		this.knightWeight = pieceWeights.knight;
		this.bishopWeight = pieceWeights.bishop;
		this.rookWeight = pieceWeights.rook;
		this.queenWeight = pieceWeights.queen;

		int startMaterial = (pawnWeight * 8 +
				knightWeight* 2 +
				bishopWeight * 2 +
				rookWeight * 2 +
				queenWeight
		) * 2;
		endMaterial = (rookWeight + queenWeight) * 2;

		scaleMargin = scaleMargin(startMaterial, endMaterial);
	}

	@Override
	public EvalResult eval(int player, int lowerBound, int upperBound, State4 s) {
		Team allied = Team.load(player, s);
		Team enemy = Team.load(1-player, s);

		BasicAttributes basics = scoreMaterial(TeamCount.load(player, s), TeamCount.load(1-player, s));

		double scale = getScale(basics.totalMaterialScore, endMaterial, scaleMargin);

		EvalContext c = new EvalContext(player, lowerBound, upperBound, scale);
		return next.eval(allied, enemy, basics, c, s, 0);
	}

	/** gets the interpolatino factor for the weight*/
	private static double getScale(final int totalMaterialScore, final int endMaterial, final int margin){
		return Math.min(1-(endMaterial-totalMaterialScore)*1./margin, 1);
	}

	/**  calculates the scale margin to use in {@link #getScale(int, int, int)}*/
	private static int scaleMargin(final int startMaterial, final int endMaterial){
		return endMaterial-startMaterial;
	}

	private BasicAttributes scoreMaterial(TeamCount allied, TeamCount enemy){
		int alliedMaterialScore = 0;
		alliedMaterialScore += allied.bishopCount * bishopWeight;
		alliedMaterialScore += allied.knightCount * knightWeight;
		alliedMaterialScore += allied.rookCount * rookWeight;
		alliedMaterialScore += allied.queenCount * queenWeight;

		int enemyMaterialScore = 0;
		enemyMaterialScore += enemy.bishopCount * bishopWeight;
		enemyMaterialScore += enemy.knightCount * knightWeight;
		enemyMaterialScore += enemy.rookCount * rookWeight;
		enemyMaterialScore += enemy.queenCount * queenWeight;

		int nonPawnScore = alliedMaterialScore - enemyMaterialScore;

		alliedMaterialScore += allied.pawnCount * pawnWeight;
		enemyMaterialScore += enemy.pawnCount * pawnWeight;

		int materialScore = alliedMaterialScore - enemyMaterialScore;
		int totalMaterialScore = alliedMaterialScore + enemyMaterialScore;

		return new BasicAttributes(materialScore, nonPawnScore, totalMaterialScore);
	}
}
