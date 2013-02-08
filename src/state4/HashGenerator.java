package state4;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class HashGenerator {
	public final static int PIECE_TYPE_ROOK = 0;
	public final static int PIECE_TYPE_BISHOP = 1;
	
	/**
	 * generates the set of equivalence classes of attack masks under
	 * an operation to find the resultant move mask
	 * @param mask attack mask of the piece, should have the edge bits non-flagged
	 * (ex, there will be at most 12 flagged bits for a rook)
	 * @param pieceIndex position index of the piece on the board
	 * @param offsets offsets to use simulating piece movements (ie, -1,1,8,-8 for rook)
	 * @return returns set of equivalence classes under move mask generation
	 */
	public static Map<Long, Set<Long>> genEquivalenceClasses(final long mask, int pieceIndex, int pieceType){
		//find locations of flipped bits
		int[] bitPositions = new int[13];
		int length = 0;
		long tempMask = mask;
		while(tempMask != 0){
			bitPositions[length++] = BitUtil.lsbIndex(tempMask & -tempMask);
			tempMask = tempMask & (tempMask-1);
		}
		
		//build all possible submasks
		final long q = 1;
		final long[] subMasks = new long[1<<length];
		for(int i = 0; i < 1 << length; i++){
			long temp = 0; //mask to be built
			for(int a = 0; a < length; a++){
				if((i & q<<a) != 0){
					int pos = bitPositions[a];
					temp |= q << pos;
				}
			}
			//System.out.println(Masks.getString(temp));
			temp |= q<<pieceIndex;
			subMasks[i] = temp;
		}
		
		//group into equivalent submasks
		final Map<Long, Set<Long>> m = new HashMap<Long, Set<Long>>();
		for(int i = 0; i < subMasks.length; i++){
			long l = subMasks[i];
			long moves = pieceType == PIECE_TYPE_ROOK? getRookMoveSet(l, pieceIndex):
														getBishopMoveSet(l, pieceIndex);
			if(!m.containsKey(moves)){
				m.put(moves, new HashSet<Long>());
			}
			m.get(moves).add(l);
			
			/*if(pieceIndex == 2){
				System.out.println("index = "+pieceIndex);
				System.out.println(Masks.getString(mask)+"\n"+Masks.getString(l)+"\n"+Masks.getString(moves));
				System.out.println("------------");
			}*/
		}
		
		return m;
	}
	
	/**
	 * Generates magics. idea is to find a magic number such that for each of eqivalence
	 * classes, the indices associated with it (by applying the hash
	 * formula to each board in the class) form a set disjoint
	 * from all other indices sets associated with other classes
	 * @param m equivalence classes under a single move mask (maps move masks
	 * to underlying generating boards)
	 * @param maxTests
	 * @param bits
	 * @return returns a magic, or 0 if fails
	 */
	public static long genMagics(Map<Long, Set<Long>> m, int maxTests, int bits){
		final Random r = new Random();
		for(int i = 0; i < maxTests; i++){
			long magic = r.nextLong();
			Map<Integer, Long> indexClassMap = new HashMap<Integer, Long>(); //maps indices into their equiv class
			boolean fail = false;
			
			for(long moveMask: m.keySet()){
				//System.out.println("testing "+moveMask);
				Set<Long> s = m.get(moveMask); //equivalence class
				for(long attackMask: s){
					final int index = (int)((attackMask*magic) >>> (64-bits));
					if(indexClassMap.containsKey(index) && indexClassMap.get(index) != moveMask){
						//this index already mapped to another class
						fail = true;
						break;
					} else{
						indexClassMap.put(index, moveMask);
					}
				}
				if(fail){
					break;
				}
			}
			if(!fail){
				//System.out.println("pos = "+pieceIndex+", magic="+magic);
				return magic;
			}
		}
		return 0;
	}
	
	/**
	 * slowly builds the attack set for a piece located at the passed index
	 * with specified attack mask
	 * @param l
	 * @param index
	 * @param offsets offsets to use simulating piece movements (ie, -1,1,8,-8 for rook)
	 * @return
	 */
	private static long getRookMoveSet(long l, int index){
		final int[] offsets = new int[]{
				1,-1,8,-8
		};
		final long q = 1;
		long result = 0;
		l = l & ~(q << index);
		for(int i = 0; i < offsets.length; i++){
			int pos = index;
			int x = pos%8;
			int y = pos/8;
			int diffx = offsets[i]%8;
			int diffy = offsets[i]/8;
			while(x >= 0 && x < 8 && y >= 0 && y < 8){
				result |= q << x+8*y;
				if((q << x+8*y & l) != 0){ //occupied
					break;
				}
				x += diffx;
				y += diffy;
			}
		}
		return result;
	}
	
	private static long getBishopMoveSet(long l, int index){
		final long q = 1;
		long result = 0;
		l = l & ~(q << index);
		int[][] off = new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}};
		for(int i = 0; i < off.length; i++){
			int pos = index;
			int x = pos%8;
			int y = pos/8;
			int diffx = off[i][0];
			int diffy = off[i][1];
			//System.out.println("diffx="+diffx+", diffy"+diffy);
			while(x >= 0 && x < 8 && y >= 0 && y < 8){
				result |= q << x+8*y;
				if((q << x+8*y & l) != 0){ //occupied
					break;
				}
				x += diffx;
				y += diffy;
			}
		}
		return result;
	}
	
	private static int mod(int x, int p){
		while(x < 0)
			x += p;
		return x%p;
	}
	
	/**
	 * From magics, generate move lookup hash.
	 * @param moves initial move masks (ex, {@link Masks#rookMoves})
	 * @param offsets
	 * @param magics magics associated with thes moves
	 * @param bits bits in the magic
	 * @return returns mapping of indices to move masks, sorted by
	 * board position index
	 */
	public static long[][] genMapping(long[] moves, int[] offsets, long[] magics, int bits, int pieceType){
		long[][] map = new long[64][1 << bits];
		for(int i = 0; i < 64; i++){
			Map<Long, Set<Long>> m = genEquivalenceClasses(moves[i], i, pieceType);
			for(long moveMask: m.keySet()){
				Set<Long> s = m.get(moveMask);
				for(long attackMask: s){
					final int index = (int)((attackMask*magics[i]) >>> (64-bits));
					map[i][index] = moveMask;
				}
			}
		}
		return map;
	}
	
	/** call to generate rook magics*/
	private static void generateRookMagics(int maxTests, int bits){
		final long[] magics = new long[64];
		for(int i = 0; i < 64; i++){
			Map<Long, Set<Long>> m = genEquivalenceClasses(Masks.rookMoves[i], i, PIECE_TYPE_ROOK);
			magics[i] = genMagics(m, maxTests, bits);
			System.out.print(magics[i]+"L, ");
			if((i+1) % 4 == 0){
				System.out.println();
			}
		}
	}
	
	/** call to generate bishop magics*/
	private static void generateBishopMagics(int maxTests, int bits){
		final long[] magics = new long[64];
		for(int i = 0; i < 64; i++){
			Map<Long, Set<Long>> m = genEquivalenceClasses(Masks.bishopMoves[i], i, PIECE_TYPE_BISHOP);
			magics[i] = genMagics(m, maxTests, bits);
			System.out.print(magics[i]+"L, ");
			if((i+1) % 4 == 0){
				System.out.println();
			}
		}
	}
	
	public static void main(String[] args){
		final int maxTests = 9999999;
		final int bits = 13;
		
		//generateRookMagics(maxTests, bits);
		generateBishopMagics(maxTests, bits);
	}
}
