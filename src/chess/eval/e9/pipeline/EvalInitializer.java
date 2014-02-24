package chess.eval.e9.pipeline;


import chess.state4.State4;

public class EvalInitializer implements EntryStage{

	private final static int endMaterial;
	private final static int scaleMargin;

	private final MidStage next;

	static{
		int startMaterial = (PieceWeights.pawn * 8 +
				PieceWeights.knight * 2 +
				PieceWeights.bishop * 2 +
				PieceWeights.rook * 2 +
				PieceWeights.queen
		) * 2;
		endMaterial = (PieceWeights.rook + PieceWeights.queen) * 2;

		scaleMargin = scaleMargin(startMaterial, endMaterial);
	}

	public EvalInitializer(MidStage next){
		this.next = next;
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

	private static BasicAttributes scoreMaterial(TeamCount allied, TeamCount enemy){
		int alliedMaterialScore = 0;
		alliedMaterialScore += allied.bishopCount * PieceWeights.bishop;
		alliedMaterialScore += allied.knightCount * PieceWeights.knight;
		alliedMaterialScore += allied.rookCount * PieceWeights.rook;
		alliedMaterialScore += allied.queenCount * PieceWeights.queen;

		int enemyMaterialScore = 0;
		enemyMaterialScore += enemy.bishopCount * PieceWeights.bishop;
		enemyMaterialScore += enemy.knightCount * PieceWeights.knight;
		enemyMaterialScore += enemy.rookCount * PieceWeights.rook;
		enemyMaterialScore += enemy.queenCount * PieceWeights.queen;

		int nonPawnScore = alliedMaterialScore - enemyMaterialScore;

		alliedMaterialScore += allied.pawnCount * PieceWeights.pawn;
		enemyMaterialScore += enemy.pawnCount * PieceWeights.pawn;

		int materialScore = alliedMaterialScore - enemyMaterialScore;
		int totalMaterialScore = alliedMaterialScore + enemyMaterialScore;

		return new BasicAttributes(materialScore, nonPawnScore, totalMaterialScore);
	}
}
