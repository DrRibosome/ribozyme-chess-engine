package eval.evalV9;

import static eval.evalV9.Weight.W;

final class EvalWeights
{
	final static int pieceValues[] = {
		0,
		0, 		//king
		900,   	//queen
		500,	//rook
		330,	//bishop
		320,	//knight
		100		//pawn
	};

	//non material scores are divided by this number
	final static int GRAIN_SIZE = 3;

	//arbitrary bonus for having the side to move
	final static Weight BONUS_TEMPO = W(24, 11);

	//bonus for being ahead 10% on material, used to reward trading when ahead on material
	final static int BONUS_MATERIAL_ADVANTAGE = 10;

	//pawn evaluation weights 
	final static Weight PENALTY_DOUBLED_PAWNS = W(39, 39);
	final static Weight PENALTY_TRIPLED_PAWNS = W(78, 78);
	//pawn with no allied pawns in adjacent columns
	final static Weight PENALTY_ISOLATED_PAWN = W(25, 40);

	//bishop evaluation weights
	final static Weight BONUS_BISHOP_PAIR = W(40, 100);

	//rook evaluation weights
	final static Weight BONUS_ROOK_ON_7TH = W(3, 20);
	final static Weight BONUS_ROOK_OPEN_FILE = W(38, 21);
	final static Weight BONUS_ROOK_HALF_OPEN_FILE = W(19, 10);

	//king weights		
	final static Weight BONUS_CAN_CASTLE_KINGSIDE = W(10, 0);
	final static Weight BONUS_CAN_CASTLE_QUEENSIDE = W(10, 0);
	final static Weight BONUS_CASTLED = W(35, 0);

	//bonus indexed by number of squares attacked that arent occupied by friendly pieces
	final static Weight[][] MOBILITY_BONUSES = {
		{}, {}, {},
		{W(-20,-36), W(-14,-19), W( -8, -3), W(-2, 13), W( 4, 29), W(10, 46), // Rooks
			W( 14, 62), W( 19, 79), W( 23, 95), W(26,106), W(27,111), W(28,114),
			W( 29,116), W( 30,117), W( 31,118), W(32,118) },
			{W(-25,-30), W(-11,-16), W(  3, -2), W(17, 12), W(31, 26), W(45, 40), // Bishops
				W( 57, 52), W( 65, 60), W( 71, 65), W(74, 69), W(76, 71), W(78, 73),
				W( 79, 74), W( 80, 75), W( 81, 76), W(81, 76) },
				{W(-38,-33), W(-25,-23), W(-12,-13), W( 0, -3), W(12,  7), W(25, 17), // Knights
					W( 31, 22), W( 38, 27), W( 38, 27) },
					{},
	};

	public final static Weight[][][] pieceSquareTables = {
		{
			{
				W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0), W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0),
				W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0), W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0),
				W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0), W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0),
				W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0), W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0),
				W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0), W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0),
				W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0), W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0),
				W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0), W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0),
				W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0), W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0),
			},
			{ // King
				W(287, 18), W(311, 77), W(262,105), W(214,135), W(214,135), W(262,105), W(311, 77), W(287, 18),
				W(262, 77), W(287,135), W(238,165), W(190,193), W(190,193), W(238,165), W(287,135), W(262, 77),
				W(214,105), W(238,165), W(190,193), W(142,222), W(142,222), W(190,193), W(238,165), W(214,105),
				W(190,135), W(214,193), W(167,222), W(119,251), W(119,251), W(167,222), W(214,193), W(190,135),
				W(167,135), W(190,193), W(142,222), W( 94,251), W( 94,251), W(142,222), W(190,193), W(167,135),
				W(142,105), W(167,165), W(119,193), W( 69,222), W( 69,222), W(119,193), W(167,165), W(142,105),
				W(119, 77), W(142,135), W( 94,165), W( 46,193), W( 46,193), W( 94,165), W(142,135), W(119, 77),
				W(94,  18), W(119, 77), W( 69,105), W( 21,135), W( 21,135), W( 69,105), W(119, 77), W( 94, 18)
			},
			{ // Queen
				W(8,-80), W(8,-54), W(8,-42), W(8,-30), W(8,-30), W(8,-42), W(8,-54), W(8,-80),
				W(8,-54), W(8,-30), W(8,-18), W(8, -6), W(8, -6), W(8,-18), W(8,-30), W(8,-54),
				W(8,-42), W(8,-18), W(8, -6), W(8,  6), W(8,  6), W(8, -6), W(8,-18), W(8,-42),
				W(8,-30), W(8, -6), W(8,  6), W(8, 18), W(8, 18), W(8,  6), W(8, -6), W(8,-30),
				W(8,-30), W(8, -6), W(8,  6), W(8, 18), W(8, 18), W(8,  6), W(8, -6), W(8,-30),
				W(8,-42), W(8,-18), W(8, -6), W(8,  6), W(8,  6), W(8, -6), W(8,-18), W(8,-42),
				W(8,-54), W(8,-30), W(8,-18), W(8, -6), W(8, -6), W(8,-18), W(8,-30), W(8,-54),
				W(8,-80), W(8,-54), W(8,-42), W(8,-30), W(8,-30), W(8,-42), W(8,-54), W(8,-80)
			},
			{ // Rook
				W(-12, 3), W(-7, 3), W(-2, 3), W(2, 3), W(2, 3), W(-2, 3), W(-7, 3), W(-12, 3),
				W(-12, 3), W(-7, 3), W(-2, 3), W(2, 3), W(2, 3), W(-2, 3), W(-7, 3), W(-12, 3),
				W(-12, 3), W(-7, 3), W(-2, 3), W(2, 3), W(2, 3), W(-2, 3), W(-7, 3), W(-12, 3),
				W(-12, 3), W(-7, 3), W(-2, 3), W(2, 3), W(2, 3), W(-2, 3), W(-7, 3), W(-12, 3),
				W(-12, 3), W(-7, 3), W(-2, 3), W(2, 3), W(2, 3), W(-2, 3), W(-7, 3), W(-12, 3),
				W(-12, 3), W(-7, 3), W(-2, 3), W(2, 3), W(2, 3), W(-2, 3), W(-7, 3), W(-12, 3),
				W(-12, 3), W(-7, 3), W(-2, 3), W(2, 3), W(2, 3), W(-2, 3), W(-7, 3), W(-12, 3),
				W(-12, 3), W(-7, 3), W(-2, 3), W(2, 3), W(2, 3), W(-2, 3), W(-7, 3), W(-12, 3)
			},
			{ // Bishop
				W(-40,-59), W(-40,-42), W(-35,-35), W(-30,-26), W(-30,-26), W(-35,-35), W(-40,-42), W(-40,-59),
				W(-17,-42), W(  0,-26), W( -4,-18), W(  0,-11), W(  0,-11), W( -4,-18), W(  0,-26), W(-17,-42),
				W(-13,-35), W( -4,-18), W(  8,-11), W(  4, -4), W(  4, -4), W(  8,-11), W( -4,-18), W(-13,-35),
				W( -8,-26), W(  0,-11), W(  4, -4), W( 17,  4), W( 17,  4), W(  4, -4), W(  0,-11), W( -8,-26),
				W( -8,-26), W(  0,-11), W(  4, -4), W( 17,  4), W( 17,  4), W(  4, -4), W(  0,-11), W( -8,-26),
				W(-13,-35), W( -4,-18), W(  8,-11), W(  4, -4), W(  4, -4), W(  8,-11), W( -4,-18), W(-13,-35),
				W(-17,-42), W(  0,-26), W( -4,-18), W(  0,-11), W(  0,-11), W( -4,-18), W(  0,-26), W(-17,-42),
				W(-17,-59), W(-17,-42), W(-13,-35), W( -8,-26), W( -8,-26), W(-13,-35), W(-17,-42), W(-17,-59)
			},
			{ // Knight
				W(-135,-104), W(-107,-79), W(-80,-55), W(-67,-42), W(-67,-42), W(-80,-55), W(-107,-79), W(-135,-104),
				W( -93, -79), W( -67,-55), W(-39,-30), W(-25,-17), W(-25,-17), W(-39,-30), W( -67,-55), W( -93, -79),
				W( -53, -55), W( -25,-30), W(  1, -6), W( 13,  5), W( 13,  5), W(  1, -6), W( -25,-30), W( -53, -55),
				W( -25, -42), W(   1,-17), W( 27,  5), W( 41, 18), W( 41, 18), W( 27,  5), W(   1,-17), W( -25, -42),
				W( -11, -42), W(  13,-17), W( 41,  5), W( 55, 18), W( 55, 18), W( 41,  5), W(  13,-17), W( -11, -42),
				W( -11, -55), W(  13,-30), W( 41, -6), W( 55,  5), W( 55,  5), W( 41, -6), W(  13,-30), W( -11, -55),
				W( -53, -79), W( -25,-55), W(  1,-30), W( 13,-17), W( 13,-17), W(  1,-30), W( -25,-55), W( -53, -79),
				W(-193,-104), W( -67,-79), W(-39,-55), W(-25,-42), W(-25,-42), W(-39,-55), W( -67,-79), W(-193,-104)
			},
			{ // Pawn
				W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0), W(0,  0), W( 0, 0), W( 0, 0), W(  0, 0),
				W(-28,-8), W(-6,-8), W( 4,-8), W(14,-8), W(14,-8), W( 4,-8), W(-6,-8), W(-28,-8),
				W(-28,-8), W(-6,-8), W( 9,-8), W(36,-8), W(36,-8), W( 9,-8), W(-6,-8), W(-28,-8),
				W(-28,-8), W(-6,-8), W(17,-8), W(58,-8), W(58,-8), W(17,-8), W(-6,-8), W(-28,-8),
				W(-28,-8), W(-6,-8), W(17,-8), W(36,-8), W(36,-8), W(17,-8), W(-6,-8), W(-28,-8),
				W(-28,-8), W(-6,-8), W( 9,-8), W(14,-8), W(14,-8), W( 9,-8), W(-6,-8), W(-28,-8),
				W(-28,-8), W(-6,-8), W( 4,-8), W(14,-8), W(14,-8), W( 4,-8), W(-6,-8), W(-28,-8),
				W(  0, 0), W( 0, 0), W( 0, 0), W( 0, 0), W(0,  0), W( 0, 0), W( 0, 0), W(  0, 0)
			},		  
		},
		{},

	}; 

	//table to get the final king danger value from
	final static Weight[] kingDangerValues = new Weight[128];

	//danger bonus to add to king danger index
	final static int DANGER_PAWN_SHIELD_GAP = 3;

	//danger based on the rank of approaching pawn by [player][rank]
	final static int[][] DANGER_STORMING_PAWN = {
		{0, 3, 3, 2, 1, 0, 0, 0},
		{0, 0, 0, 1, 2, 3, 3, 0}
	};

	//danger bonus for attacking squares next to king
	final static int DANGER_KING_ATTACKS[] = {
		0,
		0, 		//king
		3,   	//queen
		2,	    //rook
		1,	    //bishop
		1,	    //knight
		0		//pawn
	};

	//squares that add penalties to king danger index by [player][sq]
	final static int[][] kingDangerSquares = {
		{
			2,  0,  2,  3,  3,  2,  0,  2,
			2,  2,  4,  8,  8,  4,  2,  2,
			7, 10, 12, 12, 12, 12, 10,  7,
			15, 15, 15, 15, 15, 15, 15, 15,
			15, 15, 15, 15, 15, 15, 15, 15,
			15, 15, 15, 15, 15, 15, 15, 15,
			15, 15, 15, 15, 15, 15, 15, 15,
			15, 15, 15, 15, 15, 15, 15, 15
		}, {}
	};

	static
	{
		pieceSquareTables[1] = new Weight[7][64];
		for(int i = 1; i < 7; i++)
		{
			for(int j = 0; j < 64; j++)
			{
				pieceSquareTables[1][i][j] = pieceSquareTables[0][i][63-j];
			}  
		}

		kingDangerSquares[1] = new int[64];
		for(int i = 0; i < 64; i++)
		{
			kingDangerSquares[1][i] = kingDangerSquares[0][63-i];
		}

		int maxSlope = 25;
		int maxDanger = 800;
		for(int x = 0, i = 0; i < 127; i++){
			x = Math.min(maxDanger, Math.min((i * i) / 2, x + maxSlope));
			kingDangerValues[i] = W(x, 0);
		}
	}
}
