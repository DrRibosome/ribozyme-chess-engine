package state4;

import sun.misc.Unsafe;
import util.UnsafeUtil;


public final class Masks {
	public final static long[] knightMoves;
	public final static long[] kingMoves;
	public final static long[] rookMoves;
	public final static long[] queenMoves;
	public final static long[] bishopMoves;
	
	public final static long[][] castleMask;
	/** masks squares to check that no pieces block the castling king/rook, indexed [player][side] with side==left? 0: 1*/
	public final static long[][] castleBlockedMask;
	/** masks the squares for checking if castling through check or not, indexed [player][side] with side==left? 0: 1*/
	public final static long[][] castleThroughCheck;
	
	public final static long[] colMask;
	/** exclude specified col (colMaskExc[i] = ~colMask[i]*/
	public final static long[] colMaskExc;
	
	/** passed pawns have no pawns opposing them, and no pawns in either side col*/
	public final static long[][] passedPawnMasks;
	public final static long[] pawnPromotionMask;
	/** mask to detect pawn on 7th row*/
	public final static long[] pawnPrePromote = new long[]{0xFFL<<48, 0xFFL<<8};
	/** masks for pawns that are not passed, but have no pawns in their col*/
	public final static long[][] unopposePawnMasks;
	
	/** stores masks for starting locations of rooks, indexed [player][side] with side==left? 0: 1*/
	public final static long[][] rookStartingPos;
	
	private final static Unsafe u;
	private final static long bishopMovesPointer;
	private final static long rookMovesPointer;
	
	static{
		u = UnsafeUtil.getUnsafe();
		knightMoves = genKnightMoves();
		kingMoves = genKingMoves();
		rookMoves = genRookMoves();
		bishopMoves = genBishopMoves();
		queenMoves = genQueenMoves();
		
		bishopMovesPointer = u.allocateMemory(bishopMoves.length*8);
		for(int a = 0; a < bishopMoves.length; a++){
			u.putLong(bishopMovesPointer + a*8, bishopMoves[a]);
		}
		
		rookMovesPointer = u.allocateMemory(rookMoves.length*8);
		for(int a = 0; a < rookMoves.length; a++){
			u.putLong(rookMovesPointer + a*8, rookMoves[a]);
		}
		
		castleMask = new long[2][2];
		castleMask[0][0] = 1L << 2; //white, castle left
		castleMask[0][1] = 1L << 6;
		castleMask[1][0] = 1L << 58;
		castleMask[1][1] = 1L << 62;
		castleBlockedMask = new long[2][2];
		castleBlockedMask[0][0] = 0xEL; //blocked on the left
		castleBlockedMask[0][1] = 0x60L; //right
		castleBlockedMask[1][0] = 0xEL << 56;
		castleBlockedMask[1][1] = 0x60L << 56;
		castleThroughCheck = new long[2][2];
		castleThroughCheck[0][0] = 0x1CL;
		castleThroughCheck[0][1] = 0x70L;
		castleThroughCheck[1][0] = 0x1CL << 56;
		castleThroughCheck[1][1] = 0x70L << 56;
		
		colMask = new long[8];
		long col = 0x0101010101010101L;
		colMask[0] = col;
		for(int i = 1; i < 8; i++){
			colMask[i] = col << i;
		}
		colMaskExc = new long[8];
		for(int i = 0; i < 8; i++){
			colMaskExc[i] = ~colMask[i];
		}
		
		passedPawnMasks = genPassedPawnMasks();
		pawnPromotionMask = new long[]{0xFFL<<56, 0xFFL};
		unopposePawnMasks = genUnopposedPawnMasks();
		
		rookStartingPos = new long[2][2];
		rookStartingPos[0][0] = 0x1L;
		rookStartingPos[0][1] = 0x80L;
		rookStartingPos[1][0] = 0x1L << 56;
		rookStartingPos[1][1] = 0x80L << 56;
		
		//System.out.println(getString(pawnPromotionMask[0]));
		/*for(int i = 0; i < 2; i++){
			System.out.println(i);
			System.out.println(getString(rookStartingPos[1][i]));
		}*/
	}
	
	/** gets all pawn moves (including single or double movements)*/
	public static long getRawAggPawnMoves(final int player, final long aggPieces, final long pawns){
		final long open = ~aggPieces;
		final long l1moves = (player == 0? pawns << 8: pawns >>> 8) & open;
		final long candidates = (player == 0? 0xFF00L: 0xFF000000000000L) & pawns;
		final long l2moves = player == 0?
				(((candidates << 8) & open) << 8) & open:
				(((candidates >>> 8) & open) >>> 8) & open;
		return l1moves | l2moves; 
	}
	
	private static long[][] genUnopposedPawnMasks(){
		long[][] l = new long[2][64];
		long col = 0x0101010101010101L;
		for(int a = 0; a < 64; a++){
			for(int p = 0; p < 2; p++){
				l[p][a] = p==0? col<<(a+8): col>>>64-(a);
			}
		}
		return l;
	}
	
	private static long[][] genPassedPawnMasks(){
		long[][] l = new long[2][64];
		long col = 0x0101010101010101L;
		for(int a = 0; a < 64; a++){
			for(int p = 0; p < 2; p++){
				l[p][a] = p==0? col<<(a+8): col>>>64-(a);
				if(a%8 != 0){
					l[p][a] |= p==0? col<<(a-1+8): col>>>64-(a-1);
				}
				if(a%8 != 7){
					l[p][a] |= p==0? col<<(a+1+8): col>>>64-(a+1);
				}
				
				if(p == 0 && a >= 56) l[p][a] = 0;
				if(p == 1 && a <= 7) l[p][a] = 0;
			}
		}
		return l;
	}
	
	
	/** generates knight moves from each board position*/
	private static long[] genKnightMoves(){
		final int[][] offsets = new int[][]{
				{-1,2},{1,2},{2,1},{2,-1},
				{1,-2},{-1,-2},{-2,-1},{-2,1}
		};
		return genMoves(offsets, false);
	}
	
	/** generates possible king moves from each board position*/
	private static long[] genKingMoves(){
		int[][] offsets = new int[8][];
		int index = 0;
		for(int x = -1; x <= 1; x++){
			for(int y = -1; y <= 1; y++){
				if(!(x==0 && y==0)){
					offsets[index++] = new int[]{x,y};
				}
			}
		}
		return genMoves(offsets, false);
	}
	
	/** generates possible rook moves from each board position*/
	private static long[] genRookMoves(){
		final long[] moves = new long[64];
		final long hoirzMask = 0x7E;
		final long vertMask = 0x1010101010100L;
		/*final long hoirzMask = 0xFF;
		final long vertMask = 0x101010101010101L;*/
		//System.out.println(getString(vertMask));
		for(int i = 0; i < 64; i++){
			moves[i] = (hoirzMask << 8*(i/8)) | (vertMask << i%8);
			moves[i] |= 1L << i;
			//System.out.println(getString(moves[i]));
		}
		return moves;
	}
	
	private static long[] genQueenMoves(){
		final long[] moves = new long[64];
		for(int i = 0; i < 64; i++){
			moves[i] = rookMoves[i] | bishopMoves[i];
		}
		return moves;
	}
	
	private static long[] genBishopMoves(){
		int[][] offsets = new int[8*4][];
		int index = 0;
		for(int i = 0; i < 8; i++){
			offsets[index] = new int[]{i, i};
			offsets[index+1] = new int[]{i, -i};
			offsets[index+2] = new int[]{-i, -i};
			offsets[index+3] = new int[]{-i, i};
			index += 4;
		}
		long[] moves = genMoves(offsets, true);
		for(int i = 0; i < 64; i++){
			moves[i] |= 1L << i;
		}
		return moves;
	}
	
	/**
	 * Generates all moves at each position for the given offsets
	 * @param offsets
	 * @param avoidEdges if set, avoids placing pieces on board edges
	 * (ie, for queen, rook, bishop)
	 * @return returns moves indexed by position
	 */
	public static long[] genMoves(int[][] offsets, boolean avoidEdges){
		final long[] moves = new long[64];
		final int leftEdge = !avoidEdges? 0: 1;
		final int rightEdge = !avoidEdges? 8: 7;
		for(int i = 0; i < 64; i++){
			final boolean[][] b = new boolean[8][8];
			for(int a = 0; a < offsets.length; a++){
				int x = i%8+offsets[a][0];
				int y = i/8+offsets[a][1];
				if(x >= leftEdge && x < rightEdge && y >= leftEdge && y < rightEdge){
					b[y][x] = true;
				}
			}
			moves[i] = convert(b);
		}
		return moves;
	}
	
	/** converts passed boolean matrix into a bitboarb*/
	private static long convert(boolean[][] b){
		long l = 0;
		long q = 1;
		for(int i = 0; i < 64; i++){
			if(b[i/8][i%8]){
				l |= q << i;
			}
		}
		return l;
	}
	
	/** gets a sting representation of the board specified by passed long
	 * with 0 index in bottom left, 63 index in upper right*/
	public static String getString(final long l){
		final long q = 1;
		String s = "";
		String temp = "";
		for(int i = 63; i >= 0; i--){
			long mask = q << i;
			if((l & mask) == mask){
				temp = "X "+temp;
			} else{
				temp = "- "+temp;
			}
			
			if(i % 8 == 0){
				s += temp+"\n";
				temp = "";
			}
		}
		return s;
	}
	
	public static String getString(final int l){
		final long q = 1;
		String s = "";
		String temp = "";
		for(int i = 63; i >= 0; i--){
			long mask = q << i;
			if((l & mask) == mask && i < 32){
				temp = "X "+temp;
			} else{
				temp = "- "+temp;
			}
			
			if(i % 8 == 0){
				s += temp+"\n";
				temp = "";
			}
		}
		return s;
	}
	
	/** gets all rook moves given all pieces and location of the rook irrespective of piece side*/
	public static long getRawRookMoves(final long aggPieces, final long rook){
		/*final int pos = BitUtil.lsbIndex(rook);
		final long attackMask = Masks.rookMoves[pos] & aggPieces;
		final int hashIndex = (int)(attackMask*Magics.rookMagics[pos] >>> (64-Magics.rookBits));
		return Magics.rookMoveLookup[pos][hashIndex];*/
		
		final int pos = BitUtil.lsbIndex(rook);
		final int posOffset = pos << 3;
		final long attackMask = u.getLong(rookMovesPointer+posOffset) & aggPieces;
		final long hashIndex = attackMask*u.getLong(Magics.rookMagicsPointer+posOffset) >>> (64-Magics.rookBits);
		return u.getLong(Magics.rookMovePointer+((Magics.rookMoveWidth*pos+hashIndex)<<3));
	}

	/** gets all bishop moves given all pieces and location of the bishop irrespective of piece side*/
	public static long getRawBishopMoves(final long aggPieces, final long bishop){
		/*final int pos = BitUtil.lsbIndex(bishop);
		final long attackMask = Masks.bishopMoves[pos] & aggPieces;
		final int hashIndex = (int)(attackMask*Magics.bishopMagics[pos] >>> (64-Magics.bishopBits));
		return Magics.bishopMoveLookup[pos][hashIndex];*/
		
		final int pos = BitUtil.lsbIndex(bishop);
		final int posOffset = pos << 3;
		final long attackMask = u.getLong(bishopMovesPointer+posOffset) & aggPieces;
		final long hashIndex = attackMask*u.getLong(Magics.bishopMagicsPointer+posOffset) >>> (64-Magics.bishopBits);
		return u.getLong(Magics.bishopMovePointer+((Magics.bishopMoveWidth*pos+hashIndex)<<3));
	}

	/** gets all queen moves given all pieces and location of the queen irrespective of piece side*/
	public static long getRawQueenMoves(final long aggPieces, final long queen){
		return getRawRookMoves(aggPieces, queen) | getRawBishopMoves(aggPieces, queen);
	}
	
	/** gets all knight moves given all pieces and location of the knight irrespective of piece side*/
	public static long getRawKnightMoves(final long knight){
		final int index = BitUtil.lsbIndex(knight);
		return Masks.knightMoves[index];
	}
	
	/** gets all king moves given all pieces and location of the king irrespective of piece side*/
	public static long getRawKingMoves(final long king){
		final int index = BitUtil.lsbIndex(king);
		return Masks.kingMoves[index];
	}
	
	private final static long[] pawnColMasks = new long[]{Masks.colMaskExc[7], Masks.colMaskExc[0]};
	public static long getRawPawnAttacks(final int player, final long pawns){
		//final long colMask1 = player == 0? Masks.colMaskExc[7]: Masks.colMaskExc[0];
		//final long colMask2 = player == 0? Masks.colMaskExc[0]: Masks.colMaskExc[7];
		final long colMask1 = pawnColMasks[player];
		final long colMask2 = pawnColMasks[1-player];
		final long attacks1 = player == 0? (pawns << 7) & colMask1: (pawns >>> 7) & colMask1;
		final long attacks2 = player == 0? (pawns << 9) & colMask2: (pawns >>> 9) & colMask2;
		return attacks1 | attacks2;
	}
}
