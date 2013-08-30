package eval;

import state4.Masks;

/** supplementary (ie, not needed for game state functionality) masks to aid in eval calculation*/
public final class PositionMasks {
	/** masks to check whether pawn is isolated, indexed [pawn-col]*/
	public final static long[] isolatedPawnMask;
	/** checks whether pawn opposed by another pawn in same col, indexed [player][position-64]*/
	public final static long[][] opposedPawnMask;
	/** single pawn attacks, index [player][position-64]*/
	public final static long[][] pawnAttacks;
	/** mask to determine of a pawn is supported by another pawn (ie, chained) <p> indexed [player][position-64]*/
	public final static long[][] pawnChainMask;
	/** mask for bishop square type (ie, light/dark), index=0: dark, index=1, light*/
	public final static long[] bishopSquareMask;
	
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
			pawnChainMask[0][a] = pawnAttacks[1][a];
			pawnChainMask[1][a] = pawnAttacks[0][a];
			if(a%8 != 0){
				pawnChainMask[0][a] |= 1L << a-1;
				pawnChainMask[1][a] |= 1L << a-1;
			}
			if(a%8 != 7){
				pawnChainMask[0][a] |= 1L << a+1;
				pawnChainMask[1][a] |= 1L << a+1;
			}
		}
		
		bishopSquareMask = new long[2];
		for(int a = 0; a < 64; a++){
			if(squareColor(a) == 0) bishopSquareMask[0] |= 1L << a;
			else bishopSquareMask[1] |= 1L << a;
		}
	}
	
	/** gets the color of the specified square index, can be used in conjunction with
	 * {@link #bishopSquareMask} to get correspond mask for that color squares*/
	public static int squareColor(final int index){
		if((index>>>3)%2 == 1){ //odd row
			return index%2 == 0? 1: 0;
		}
		return index%2;
	}
	
	public static void main(String[] args){
		for(int a = 0; a < 64; a++){
			//System.out.println("a="+a);
			//System.out.println(Masks.getString(isolatedPawnMask[a]));
			//System.out.println(Masks.getString(isolatedPawnMask[a%8]));// & Masks.passedPawnMasks[1][a]));
			//System.out.println(Masks.getString(isolatedPawnMask[a]));
		}
		
		for(int a = 0; a < 2; a++){
			System.out.println("a="+a);
			System.out.println(Masks.getString(bishopSquareMask[a]));
		}
	}
}
