package chess.state4;

import sun.misc.Unsafe;
import chess.util.UnsafeUtil;

public final class BitUtil {
	private final static int[] debruijnIndex = new int[]{
		0, 47,  1, 56, 48, 27,  2, 60,
	    57, 49, 41, 37, 28, 16,  3, 61,
	    54, 58, 35, 52, 50, 42, 21, 44,
	    38, 32, 29, 23, 17, 11,  4, 62,
	    46, 55, 26, 59, 40, 36, 15, 53,
	    34, 51, 20, 43, 31, 22, 10, 45,
	    25, 39, 14, 33, 19, 30,  9, 24,
	    13, 18,  8, 12,  7,  6,  5, 63
	};
	private final static long debruijn64 = 0x03f79d71b4cb0a89L;
	
	private final static Unsafe u;
	private final static long debruijnIndexPointer;
	
	static{
		u = UnsafeUtil.getUnsafe();
		debruijnIndexPointer = u.allocateMemory(debruijnIndex.length*4);
		for(int a = 0; a < debruijnIndex.length; a++){
			u.putInt(debruijnIndexPointer + a*4, debruijnIndex[a]);
		}
	}

	/** returns the index of the least significant bit*/
	public static int lsbIndex(final long l){
		//return debruijnIndex[(int)(((l ^ (l-1)) * debruijn64) >>> 58)];
		final long index = (((l ^ (l-1)) * debruijn64) >>> 58) << 2;
		return u.getInt(debruijnIndexPointer + index);
	}
	
	/** masks the msb
	 * <p> returns passed long with only the most significant bit set*/
	public static long msb(long l){
		l |= (l >>> 1);
        l |= (l >>> 2);
        l |= (l >>> 4);
        l |= (l >>> 8);
        l |= (l >>> 16);
        l |= (l >>> 32);
        return l & ~(l >>> 1);
	}
	
	/** returns the index of the most significant bit*/
	public static int msbIndex(final long l){
		return lsbIndex(msb(l));
	}
	
	/** masks the lsb*/
	public static long lsb(final long l){
		return l & -l;
	}
	
	/** clears lsb bit*/
	public static long lsbClear(final long l){
		return l & (l-1);
	}
	
	/** return 0 if l=0, 1 otherwise*/
	public static long isDef(final long l){
		//return (l&-l)>>>lsbIndex(l);
		return (l|-l) >>> 63;
	}
	
	/** gets number of set bits*/
	public static long getSetBits(long l){
		l = l - ((l >> 1) & 0x5555555555555555L);
		l = (l & 0x3333333333333333L) + ((l >> 2) & 0x3333333333333333L);
		return (((l + (l >> 4)) & 0xF0F0F0F0F0F0F0FL) * 0x101010101010101L) >> 56;
	}
}
