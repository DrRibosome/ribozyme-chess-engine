package util.magic;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import state4.BitUtil;

public final class HashUtil {
	/**
	 * Generates all the possible sub masks for a given mask C. A sub mask M
	 * is a mask such that (~M & C) == 0 always (hence, M has set bits only
	 * at locations of set bits in C, but may also have unset bits at set locations
	 * in mask C)
	 * @param mask
	 * @return
	 */
	public static long[] getSubMasks(long mask){
		//find locations of flipped bits
		final int length = (int)BitUtil.getSetBits(mask);
		int[] bitPositions = new int[length];
		int index = 0;
		for(long tempMask = mask; tempMask != 0; tempMask &= tempMask-1){
			bitPositions[index++] = BitUtil.lsbIndex(tempMask);
		}

		//build all possible submasks
		final long[] subMasks = new long[1<<length];
		for(int i = 0; i < 1L << length; i++){
			long temp = 0; //mask to be built
			for(int a = 0; a < length; a++){
				if((i & 1L<<a) != 0){
					int pos = bitPositions[a];
					temp |= 1L << pos;
				}
			}
			subMasks[i] = temp;
		}
		return subMasks;
	}
	
	/**
	 * generates a magic for a single equivalence class
	 * @param equiv
	 * @param maxTests
	 * @param bits
	 * @return returns a viable magic, or 0 if fails
	 */
	public static long genMagic(long[] equiv, int maxTests, int bits){
		assert equiv.length != 0;
		
		final Random r = new Random();
		for(int i = 0; i < maxTests; i++){
			long magic = r.nextLong();
			final int index = (int)((equiv[0]*magic) >>> (64-bits));
			boolean fail = false;
			
			for(int a = 1; a < equiv.length && !fail; a++){
				final long attackMask = equiv[a];
				if((int)((attackMask*magic) >>> (64-bits)) != index){
					fail = true;
				}
			}
			if(!fail){
				return magic;
			}
		}
		return 0;
	}
	
	/**
	 * Generates magics. Idea is to find a magic number such that for the eqivalence
	 * class, the indices associated with it (by applying the hash function) form a set disjoint
	 * from all other indices sets associated with other classes
	 * @param equivClasses set of equivalence classes
	 * @param bits bits to use in magic key creation
	 * @return returns a set of magics for the passed equivalence classes, or null if fails
	 */
	public static long[] genMagics(long[][] equivClasses, int bits){
		Set<Integer> reserved = new HashSet<Integer>();
		long[] magics = new long[equivClasses.length];
		
		for(int a = 0; a < equivClasses.length; a++){
			boolean found = false;
			for(int q = 0; q < 9999 && !found; q++){
				long magic = genMagic(equivClasses[a], 999999, bits);
				if(magic != 0){
					int index = (int)((equivClasses[a][0]*magic) >>> (64-bits));
					if(!reserved.contains(index)){
						//working, non-conflicting magic
						magics[a] = magic;
						reserved.add(index);
						found = true;
					}
				}
			}
			if(!found){
				return null;
			}
		}
		return magics;
	}
}
