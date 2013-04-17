package eval.expEvalV3.gparams;

import state4.State4;
import eval.Weight;
import eval.expEvalV3.EvalParameters;

public class GParams1v2 {

	private static Weight W(int start, int end){
		final int shift = 1;
		return new Weight(start >> shift, end >> shift);
	}
	
	public static EvalParameters buildEval(){
		EvalParameters p = new EvalParameters();
		
		//--------------------------------------------------------------
		//general weights
		
		/*king = 0
		queen = 889
		rook = 428
		bishop = 258
		knight = 153
		pawn = 100*/
		
		p.materialWeights = new int[7];
		p.materialWeights[State4.PIECE_TYPE_QUEEN] = 898;
		p.materialWeights[State4.PIECE_TYPE_ROOK] = 487;
		p.materialWeights[State4.PIECE_TYPE_BISHOP] = 291;
		p.materialWeights[State4.PIECE_TYPE_KNIGHT] = 288;
		p.materialWeights[State4.PIECE_TYPE_PAWN] = 100;
		
		p.tempo = new Weight(3, 1);
		p.bishopPair = new Weight(5, 12);

		//note, all values here divided by 8
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
		mobilityWeights[State4.PIECE_TYPE_EMPTY] = new Weight[0];
		mobilityWeights[State4.PIECE_TYPE_KING] = new Weight[0];
		mobilityWeights[State4.PIECE_TYPE_PAWN] = new Weight[0];
		p.mobilityWeights = mobilityWeights;
		
		//---------------------------------------------------------------------
		//king values

		final int[][] kingDangerSquares = {
				{
					2,  0,  2,  3,  3,  2,  0,  2,
					2,  2,  4,  8,  8,  4,  2,  2,
					7, 10, 12, 12, 12, 12, 10,  7,
					15, 15, 15, 15, 15, 15, 15, 15,
					15, 15, 15, 15, 15, 15, 15, 15,
					15, 15, 15, 15, 15, 15, 15, 15,
					15, 15, 15, 15, 15, 15, 15, 15,
					15, 15, 15, 15, 15, 15, 15, 15
				}, new int[64]
		};
		for(int a = 0; a < 64; a++) kingDangerSquares[1][a] = kingDangerSquares[0][63-a];
		p.kingDangerSquares = kingDangerSquares;
		
		p.contactCheckQueen = 6;
		p.contactCheckRook = 4;
		p.queenCheck = 3;
		p.rookCheck = 2;
		p.knightCheck = 1;
		p.bishopCheck = 1;
		
		p.kingDangerValues = new Weight[128];
		final int maxSlope = 25;
		final int maxDanger = 800;
		for(int x = 0, i = 0; i < p.kingDangerValues.length; i++){
			x = Math.min(maxDanger, Math.min((i * i) / 2, x + maxSlope));
			p.kingDangerValues[i] = W(x, 0);
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

		p.pawnShelter = new int[][]{ //only need 7 indeces, pawn cant be on last row
				{0, 30, 20, 8, 2, 0, 0},
				{0, 70, 45, 20, 5, 0, 0},
				//{0, 61, 45, 17, 5, 0, 0},
				//{0, 141, 103, 39, 13, 0, 0},
		};
		
		p.pawnStorm = new int[][]{ //indexed [type][distance]
				{-20, -18, -14, -10, -5, 0}, //no allied pawn
				{-15, -13, -10, -6, -5, 0}, //has allied pawn, enemy not blocked
				{-6, -5, -4, -3, -1, 0}, //enemy pawn blocked by allied pawn
		};
		
		//-------------------------------------------------------------------------
		//pawn values
		
		final Weight[][] passedPawnRowWeight = new Weight[][]{
				{
				new Weight(-999, -999), //unused
				new Weight(0, 0),
				new Weight(5, 5),
				new Weight(23, 17),
				new Weight(44, 31),
				new Weight(67, 43),
				new Weight(96, 60),
				new Weight(-999, -999),}, //unused
				new Weight[8]
		};
		for(int a = 0; a < 8; a++) passedPawnRowWeight[1][a] = passedPawnRowWeight[0][7-a];
		p.passedPawnRowWeight = passedPawnRowWeight;

		final Weight[][] isolatedPawns = new Weight[][]{
				{W(-37, -45), W(-54, -52), W(-60, -52), W(-60, -52), W(-60, -52), W(-60, -52), W(-54, -52), W(-37, -45)},
				{W(-25, -30), W(-36, -35), W(-40, -35), W(-40, -35), W(-40, -35), W(-40, -35), W(-36, -35), W(-25, -30)},
		};
		p.isolatedPawns = isolatedPawns;

		//doubled pawn weights halved because added twice (once for each doubled pawn)
		p.doubledPawns = new Weight[][]{{
				W(-13/2, -43/2), W(-20/2, -48/2), W(-23/2, -48/2), W(-23/2, -48/2),
				W(-23/2, -48/2), W(-23/2, -48/2), W(-20/2, -48/2), W(-13/2, -43/2)
			},{
				W(-13/2, -43/2), W(-20/2, -48/2), W(-23/2, -48/2), W(-23/2, -48/2),
				W(-23/2, -48/2), W(-23/2, -48/2), W(-20/2, -48/2), W(-13/2, -43/2)
			},
		};

		p.backwardPawns = new Weight[][]{
				{W(-30, -42), W(-43, -46), W(-49, -46), W(-49, -46), W(-49, -46), W(-49, -46), W(-43, -46), W(-30, -42)},
				{W(-20, -28), W(-29, -31), W(-33, -31), W(-33, -31), W(-33, -31), W(-33, -31), W(-29, -31), W(-20, -28)},
		};
		
		p.pawnChain = new Weight[]{
				W(5,0), W(6,0), W(8,0), W(10,0),
			    W(10,0), W(8,0), W(6,0), W(5,0)
		};
		
		return p;
	}
}