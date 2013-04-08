package eval;

import state4.Masks;

/** supplementary masks to aid in eval calculation*/
public final class PositionMasks {
	/** masks to check whether pawn is isolated, indexed [pawn-col]*/
	public final static long[] isolatedPawnMask;
	/** checks whether pawn opposed by another pawn in same col, indexed [player][position-64]*/
	public final static long[][] opposedPawnMask;
	/** single pawn attacks, index [player][position-64]*/
	public final static long[][] pawnAttacks;
	public final static long[][] pawnChainMask;
	
	static{
		isolatedPawnMask = new long[8];
		for(int a = 0; a < 8; a++){
			if(a != 0) isolatedPawnMask[a] |= Masks.colMask[a-1];
			if(a != 7) isolatedPawnMask[a] |= Masks.colMask[a+1];
		}
		
		opposedPawnMask = new long[2][64];
		for(int a = 0; a < 64; a++){
			opposedPawnMask[0][a] = (Masks.colMask[a%8] << a/8*8) & Masks.colMask[a%8];
			opposedPawnMask[1][a] = (Masks.colMask[a%8] >>> 56-a/8*8) & Masks.colMask[a%8];
		}
		
		pawnAttacks = new long[2][64];
		for(int a = 0; a < 64; a++){
			if(a%8 != 0){
				pawnAttacks[0][a] |= 1L << a+7;
				pawnAttacks[1][a] |= 1L << a-9;
			}
			if(a%8 != 7){
				pawnAttacks[0][a] |= 1L << a+9;
				pawnAttacks[1][a] |= 1L << a-7;
			}
		}
		
		pawnChainMask = new long[2][64];
		for(int a = 0; a < 64; a++){
			pawnChainMask[0][a] = pawnAttacks[0][a];
			pawnChainMask[1][a] = pawnAttacks[1][a];
			if(a%8 != 0){
				pawnChainMask[0][a] |= 1L << a-1;
				pawnChainMask[1][a] |= 1L << a-1;
			}
			if(a%8 != 7){
				pawnChainMask[0][a] |= 1L << a+1;
				pawnChainMask[1][a] |= 1L << a+1;
			}
		}
	}
	
	public static void main(String[] args){
		for(int a = 0; a < 64; a++)
			System.out.println(Masks.getString(pawnChainMask[0][a]));
	}
}
