package util.hash;

/** int map through cuckoo hashing, used for hashing historical positions*/
public final class BloomFilter {
	private final static long a = (long)(Math.random()*Long.MAX_VALUE);
	private final static long b = (long)(Math.random()*Long.MAX_VALUE);
	private final static long c = (long)(Math.random()*Long.MAX_VALUE);
	
	private final long[] bloom;
	private final int size;
	
	public BloomFilter(int size){
		bloom = new long[1<<size-7];
		this.size = size;
	}
	
	/** puts one instance of the appeared zkey*/
	public void put(final long zkey){
		final int index1 = h1(zkey, size);
		final int index2 = h2(zkey, size);
		final int index3 = h3(zkey, size);
		bloom[index1/64] |= 1L<<index1;
		bloom[index2/64] |= 1L<<index1;
		bloom[index3/64] |= 1L<<index1;
	}
	
	public boolean contains(final long zkey){
		final int index1 = h1(zkey, size);
		final int index2 = h2(zkey, size);
		final int index3 = h3(zkey, size);
		return (bloom[index1/64] & 1L<<index1) != 0 &&
				(bloom[index2/64] & 1L<<index2) != 0 &&
				(bloom[index3/64] & 1L<<index3) != 0;
	}
	
	private static int h1(final long zkey, final int size){
		return (int)(a*zkey) >>> 32-size;
	}
	
	private static int h2(final long zkey, final int size){
		return (int)(b*zkey >>> 64-size);
	}
	
	private static int h3(final long zkey, final int size){
		final long k = c*zkey;
		return (int)((k&0xFFFFFFFFL) ^ (k>>>32)) >>> 32-size;
	}
}
