package eval.expEvalV3;

import state4.State4;

public final class DefaultEvalWeights {

	private static Weight W(int start, int end){
		return new Weight(start >> 3, end >> 3);
	}
	
	public static synchronized EvalParameters defaultEval(){
		EvalParameters p = new EvalParameters();
		
		p.materialWeights = new int[7];
		p.materialWeights[State4.PIECE_TYPE_BISHOP] = 305;
		p.materialWeights[State4.PIECE_TYPE_KNIGHT] = 300;
		p.materialWeights[State4.PIECE_TYPE_ROOK] = 500;
		p.materialWeights[State4.PIECE_TYPE_QUEEN] = 900;
		p.materialWeights[State4.PIECE_TYPE_PAWN] = 100;

		//all values here divided by 8 (ie, grain size from stockfish)
		final Weight[][] mobilityWeights = new Weight[7][];
		mobilityWeights[State4.PIECE_TYPE_KNIGHT] = new Weight[]{
				W(-38,-33), W(-25,-23), W(-12,-13), W( 0, -3), W(12,  7), W(25, 17), //knights
				W( 31, 22), W( 38, 27), W( 38, 27) };
		mobilityWeights[State4.PIECE_TYPE_BISHOP] = new Weight[]{
				W(-25,-30), W(-11,-16), W(  3, -2), W(17, 12), W(31, 26), W(45, 40), // Bishops
				W( 57, 52), W( 65, 60), W( 71, 65), W(74, 69), W(76, 71), W(78, 73),
				W( 79, 74), W( 80, 75), W( 81, 76), W(81, 76) };
		mobilityWeights[State4.PIECE_TYPE_ROOK] = new Weight[]{
				W(-20,-36), W(-14,-19), W( -8, -3), W(-2, 13), W( 4, 29), W(10, 46), // Rooks
				W( 14, 62), W( 19, 79), W( 23, 95), W(26,106), W(27,111), W(28,114),
				W( 29,116), W( 30,117), W( 31,118), W(32,118) };
		mobilityWeights[State4.PIECE_TYPE_QUEEN] = new Weight[]{
				W(-10,-18), W( -8,-13), W( -6, -7), W(-3, -2), W(-1,  3), W( 1,  8), // Queens
				W(  3, 13), W(  5, 19), W(  8, 23), W(10, 27), W(12, 32), W(15, 34),
				W( 16, 35), W( 17, 35), W( 18, 35), W(20, 35), W(20, 35), W(20, 35),
				W( 20, 35), W( 20, 35), W( 20, 35), W(20, 35), W(20, 35), W(20, 35),
				W( 20, 35), W( 20, 35), W( 20, 35), W(20, 35), W(20, 35), W(20, 35),
				W( 20, 35), W( 20, 35) };
		p.mobilityWeights = mobilityWeights;
		
		final Weight[][] passedPawnRowWeight = new Weight[][]{
				{null,
				new Weight(0, 3),
				new Weight(5, 6),
				new Weight(15, 13),
				new Weight(30, 21),
				new Weight(50, 32),
				new Weight(75, 46),
				new Weight(105, 63),},
				new Weight[8]
		};
		for(int a = 0; a < 8; a++) passedPawnRowWeight[1][a] = passedPawnRowWeight[0][7-a];
		p.passedPawnRowWeight = passedPawnRowWeight;
		
		p.tempo = new Weight(3, 1);
		
		p.doubledPawnsWeight = new Weight(-25, -5);
		p.tripledPawnsWeight = new Weight(-35, -7);
		p.bishopPair = new Weight(10, 25);
		
		p.kingDangerValues = new Weight[128];
		final int maxSlope = 25;
		final int maxDanger = 800;
		for(int x = 0, i = 0; i < p.kingDangerValues.length; i++){
			x = Math.min(maxDanger, Math.min((i * i) / 2, x + maxSlope));
			p.kingDangerValues[i] = new Weight(x, 0);
		}
		
		p.dangerKingAttacks = new int[]{
				0,
				0, 		//king
				3,   	//queen
				2,	    //rook
				1,	    //bishop
				1,	    //knight
				0		//pawn
		};
		
		return p;
	}
}
