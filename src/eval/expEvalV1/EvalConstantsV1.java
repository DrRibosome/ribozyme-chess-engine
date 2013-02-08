package eval.expEvalV1;

import state4.State4;

class EvalConstantsV1 {
	//general weights
	final static int[] materialWeights;
	/** stores max number of moves by piece type*/
	final static int[] pieceMobility;
	/** mobility bonus for 3 levels of mobility, by piece type*/
	final static int[][] mobilityBonus;
	final static int canCastleWeight = 10;
	final static int bishopPairWeight = 20;
	final static int[] knightEntropyWeight = new int[]{0, 15, 30};
	
	//pawn weights
	/** bonus for pawns in given row*/
	final static int[][] pawnRowBonus;
	final static int passedPawnWeight = 55;
	final static int unopposedPawnWeight = 5;
	final static int doubledPawnsWeight = -10;
	final static int tripledPawnsWeight = -15;
	final static int supportedPawn = 5;
	final static int supportedPassedPawn = 15;
	
	static{
		materialWeights = new int[7];
		materialWeights[State4.PIECE_TYPE_BISHOP] = 310;
		materialWeights[State4.PIECE_TYPE_KNIGHT] = 300;
		materialWeights[State4.PIECE_TYPE_ROOK] = 500;
		materialWeights[State4.PIECE_TYPE_QUEEN] = 900;
		materialWeights[State4.PIECE_TYPE_PAWN] = 100;

		pieceMobility = new int[7];
		pieceMobility[State4.PIECE_TYPE_BISHOP] = 14;
		pieceMobility[State4.PIECE_TYPE_KNIGHT] = 8;
		pieceMobility[State4.PIECE_TYPE_ROOK] = 15;
		pieceMobility[State4.PIECE_TYPE_QUEEN] = 28;
		
		final int s = 2; //scale
		mobilityBonus = new int[7][3];
		mobilityBonus[State4.PIECE_TYPE_BISHOP] = new int[]{	-35/s,	0/s,	35/s};
		mobilityBonus[State4.PIECE_TYPE_KNIGHT] = new int[]{	-30/s,	5/s,	30/s};
		mobilityBonus[State4.PIECE_TYPE_ROOK] = new int[]{		-40/s,	0/s,	40/s};
		mobilityBonus[State4.PIECE_TYPE_QUEEN] = new int[]{		-50/s,	0/s,	50/s};
		
		
		//note, bonuses here will be in addition to the value of the pawn
		pawnRowBonus = new int[2][8];
		pawnRowBonus[0] = new int[]{0, 0, 10, 20, 35, 80, 130, 0};
		pawnRowBonus[1] = new int[8];
		for(int a = 0; a < 8; a++){ pawnRowBonus[1][a] = pawnRowBonus[0][7-a];}
	}
}
