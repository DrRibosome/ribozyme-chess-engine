package state4;

/** optimized static exchange evaluation implementation*/
public final class SEE {
	private final static long pawnRightAttackMask = Masks.colMaskExc[0];
	private final static long pawnLeftAttackMask = Masks.colMaskExc[7];
	private final static int[] pieceValues = new int[7];
	static{
		pieceValues[State4.PIECE_TYPE_QUEEN] = 9;
		pieceValues[State4.PIECE_TYPE_ROOK] = 5;
		pieceValues[State4.PIECE_TYPE_BISHOP] = 3;
		pieceValues[State4.PIECE_TYPE_KNIGHT] = 3;
		pieceValues[State4.PIECE_TYPE_PAWN] = 1;
	}
	
	/**
	 * move piece on from square to target square
	 * @param player
	 * @param from
	 * @param target
	 * @param s
	 * @return returns 1 if SEE is positive for the passed player, 0 if even, -1 if negative
	 */
	public static int seeSign(final int player, final long from, final long target, final State4 s){
		
		/*System.out.println("-------------------------------\n");
		System.out.println(s);
		System.out.println();*/
		
		assert (from & from-1) == 0 && (target & target-1) == 0;
		assert from != 0 && target != 0;
		
		final long enemyPieces = s.pieces[1-player];
		
		//test for up takes first
		final long alliedPawns = s.pawns[player];
		final long enemyPawns = s.pawns[1-player];
		final long enemyNonPawn = enemyPieces & ~enemyPawns;
		if((alliedPawns & from) != 0 && (enemyNonPawn & target) != 0){
			//System.out.println("pawn up take");
			return 1; //pawn takes non-pawn
		}
		
		final long alliedKnights = s.knights[player];
		final long alliedBishops = s.bishops[player];
		final long alliedMinorPieces = alliedKnights | alliedBishops;
		final long enemyKnights = s.knights[1-player];
		final long enemyBishops = s.bishops[1-player];
		final long enemyMinorPieces = enemyKnights | enemyBishops;
		final long enemyMajorPieces = enemyNonPawn & ~enemyMinorPieces;
		if((alliedMinorPieces & from) != 0 && (enemyMajorPieces & target) != 0){
			//System.out.println("minor piece up take");
			return 1; //minor piece takes major piece
		}
		
		final long alliedRooks = s.rooks[player];
		final long enemyQueens = s.queens[1-player];
		if((alliedRooks & from) != 0 && (enemyQueens & target) != 0){
			//System.out.println("rook up take");
			return 1; //rook takes queen
		}
		
		//test for positive SEE on even or down takes
		//System.out.println("testing for even or down take");
		final int targetIndex = BitUtil.lsbIndex(target);
		final int fromIndex = BitUtil.lsbIndex(from);
		
		int gain = pieceValues[s.mailbox[targetIndex]]; //value taken
		int onValue = pieceValues[s.mailbox[fromIndex]]; //value of piece on the target square
		final long alliedPieces = s.pieces[player];
		long agg = ((enemyPieces | alliedPieces) & ~from) | target;
		
		//System.out.println("initial gain = "+gain);
		//System.out.println("initial onValue = "+onValue);
		
		final long usuableAlliedPawns = alliedPawns & ~from;
		long attackingAlliedPawns;
		long attackingEnemyPawns;
		if(player == 0){
			attackingAlliedPawns = ((((usuableAlliedPawns << 7) & pawnLeftAttackMask) & target) >>> 7) |
					((((usuableAlliedPawns << 9) & pawnRightAttackMask) & target) >>> 9);
			attackingEnemyPawns = ((((enemyPawns >>> 9) & pawnLeftAttackMask) & target) << 9) |
					((((enemyPawns >>> 7) & pawnRightAttackMask) & target) << 7);
		} else{
			attackingEnemyPawns = ((((enemyPawns << 7) & pawnLeftAttackMask) & target) >>> 7) |
					((((enemyPawns << 9) & pawnRightAttackMask) & target) >>> 9);
			attackingAlliedPawns = ((((usuableAlliedPawns >>> 9) & pawnLeftAttackMask) & target) << 9) |
					((((usuableAlliedPawns >>> 7) & pawnRightAttackMask) & target) << 7);
		}
		
		long attackingAlliedKnights = ~from & alliedKnights & Masks.getRawKnightMoves(target);
		long attackingEnemyKnights = Masks.getRawKnightMoves(target) & enemyKnights;
		
		final long rawBishopMoves = Masks.getRawBishopMoves(target, target);
		final long usuableEnemyBishops = enemyBishops & rawBishopMoves; //enemy bishops that can be used in the attack
		long readyEnemyBishops = usuableEnemyBishops; //enemy bishops ready to be used in the attack
		final long usuableAlliedBishops = ~from & alliedBishops & rawBishopMoves;
		long readyAlliedBishops = usuableAlliedBishops;
		
		final long enemyRooks = s.rooks[1-player];
		final long rawRookMoves = Masks.getRawRookMoves(target, target);
		final long usuableEnemyRooks = enemyRooks & rawRookMoves;
		long readyEnemyRooks = usuableEnemyRooks;
		final long usuableAlliedRooks = ~from & alliedRooks & rawRookMoves;
		long readyAlliedRooks = usuableAlliedRooks;
		
		final long usuableEnemyQueens = enemyQueens & (rawBishopMoves | rawRookMoves);
		long readyEnemyQueens = usuableEnemyQueens;
		final long usuableAlliedQueens = (rawBishopMoves | rawRookMoves) & ~from & s.queens[player];
		long readyAlliedQueens = usuableAlliedQueens;
		
		//records if we ever encounter a point where the gain is zero (ie, if a side could stop the trading)
		boolean alliedZeroGain = false;
		boolean enemyZeroGain = false;
		
		for(int a = 0; a < 16; a++){
			
			int gEnemy = -gain;
			//System.out.println("enemy gain = "+gEnemy+", on value = "+onValue);
			if(gEnemy > 0){
				////enemy gain is positive, return negative SEE sign unless zero gain position already encountered
				//System.out.println("returning "+(alliedZeroGain? 0: -1));
				return alliedZeroGain? 0: -1;
			}
			enemyZeroGain |= gEnemy == 0;
			
			//negative gain, attack with next least valuable piece
			if(attackingEnemyPawns != 0){
				//System.out.println("\tenemy pawn take");
				gain = gEnemy + onValue;
				onValue = 1;
				agg &= ~(attackingEnemyPawns & -attackingEnemyPawns);
				attackingEnemyPawns &= attackingEnemyPawns-1;
			} else if(attackingEnemyKnights != 0){
				//System.out.println("\tenemy knight take");
				gain = gEnemy + onValue;
				onValue = 3;
				agg &= ~(attackingEnemyKnights & -attackingEnemyKnights);
				attackingEnemyKnights &= attackingEnemyKnights-1;
			} else if(usuableEnemyBishops != 0 && readyEnemyBishops != 0 &&
					(Masks.getRawBishopMoves(agg, readyEnemyBishops & -readyEnemyBishops) & target) != 0){
				//System.out.println("\tenemy bishop take");
				gain = gEnemy + onValue;
				onValue = 3;
				agg &= ~(readyEnemyBishops & -readyEnemyBishops);
				readyEnemyBishops &= readyEnemyBishops-1;
			} else if(usuableEnemyRooks != 0 && readyEnemyRooks != 0 &&
					(Masks.getRawRookMoves(agg, readyEnemyRooks & -readyEnemyRooks) & target) != 0){
				//System.out.println("\tenemy rook take");
				gain = gEnemy + onValue;
				onValue = 5;
				agg &= ~(readyEnemyRooks & -readyEnemyRooks);
				readyEnemyRooks &= readyEnemyRooks-1;
			} else if(usuableEnemyQueens != 0 && readyEnemyQueens != 0 &&
					(Masks.getRawQueenMoves(agg, readyEnemyQueens & -readyEnemyQueens) & target) != 0){
				//System.out.println("\tenemy queen take");
				gain = gEnemy + onValue;
				onValue = 9;
				agg &= ~(readyEnemyQueens & -readyEnemyQueens);
				readyEnemyQueens &= readyEnemyQueens-1;
			} else{
				//enemy has nothing else to send in
				//if we have encountered a point of zero gain before this, return 0
				//else, allied ends ahead, return 1
				//System.out.println("\tenemy nothing to send in");
				return enemyZeroGain? 0: 1;
			}
			
			
			int gAllied = -gain;
			//System.out.println("allied gain = "+gAllied+", on value = "+onValue);
			if(gAllied > 0){
				////enemy gain is positive, return negative SEE sign unless zero gain position already encountered
				return enemyZeroGain? 0: 1;
			}
			alliedZeroGain |= gAllied == 0;
			
			//negative gain, attack with next least valuable piece
			if(attackingAlliedPawns != 0){
				//System.out.println("\tallied pawn take");
				gain = gAllied + onValue;
				onValue = 1;
				agg &= ~(attackingAlliedPawns & -attackingAlliedPawns);
				attackingAlliedPawns &= attackingAlliedPawns-1;
			} else if(attackingAlliedKnights != 0){
				//System.out.println("\tallied knight take");
				gain = gAllied + onValue;
				onValue = 3;
				agg &= ~(attackingAlliedKnights & -attackingAlliedKnights);
				attackingAlliedKnights &= attackingAlliedKnights-1;
			} else if(usuableAlliedBishops != 0 && readyAlliedBishops != 0 &&
					(Masks.getRawBishopMoves(agg, readyAlliedBishops & -readyAlliedBishops) & target) != 0){
				//System.out.println("\tallied bishop take");
				gain = gAllied + onValue;
				onValue = 3;
				agg &= ~(readyAlliedBishops & -readyAlliedBishops);
				readyAlliedBishops &= readyAlliedBishops-1;
			} else if(usuableAlliedRooks != 0 && readyAlliedRooks != 0 &&
					(Masks.getRawRookMoves(agg, readyAlliedRooks & -readyAlliedRooks) & target) != 0){
				//System.out.println("\tallied rook take");
				gain = gAllied + onValue;
				onValue = 5;
				agg &= ~(readyAlliedRooks & -readyAlliedRooks);
				readyAlliedRooks &= readyAlliedRooks-1;
			} else if(usuableAlliedQueens != 0 && readyAlliedQueens != 0 &&
					(Masks.getRawQueenMoves(agg, readyAlliedQueens & -readyAlliedQueens) & target) != 0){
				//System.out.println("\tallied queen take");
				gain = gAllied + onValue;
				onValue = 9;
				agg &= ~(readyAlliedQueens & -readyAlliedQueens);
				readyAlliedQueens &= readyAlliedQueens-1;
			} else{
				//System.out.println("\tallied nothing to send in");
				return alliedZeroGain? 0: -1;
			}
		}
		
		
		return 0;
	}
}
