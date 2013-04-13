package state4;

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

	/** returns the index of the least significant bit*/
	public static int lsbIndex(long l){
		//second method supposedly slighty faster, but noticed no improvement
		//return index64[(int)(((l & -l) * debruijn64) >>> 58)];
		return debruijnIndex[(int)(((l ^ (l-1)) * debruijn64) >>> 58)];
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
	public static long lsb(long l){
		return l & -l;
	}
	
	/** clears lsb bit*/
	public static long lsbClear(long l){
		return l & (l-1);
	}
	
	/** return 0 if l=0, 1 otherwise*/
	public static long isDef(long l){
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
