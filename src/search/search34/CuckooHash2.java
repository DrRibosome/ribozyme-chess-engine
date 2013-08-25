package search.search34;

import java.util.Random;

/** rough but working implementation of cuckoo hashing*/
public final class CuckooHash2{
	private final TTEntry[] l;
	private final int size;
	private final int maxAttempts;
	private final static long a;
	private final static long b;
	
	static{
		final Random r = new Random(58372L);
		a = (long)(r.nextDouble()*Long.MAX_VALUE);
		b = (long)(r.nextDouble()*Long.MAX_VALUE);
	}
	
	public CuckooHash2(int size){
		this(size, 16);
	}
	
	public CuckooHash2(int size, int maxAttempts){
		this.maxAttempts = maxAttempts;
		l = new TTEntry[1<<size];
		for(int q = 0; q < 1<<size; q++){
			l[q] = new TTEntry();
		}
		this.size = size;
	}
	
	public void put(final long zkey, final TTEntry t){
		final int seq = t.seq;
		for(int q = 0; q < maxAttempts; q++){
			final int index1 = h1(a, t.zkey, size);
			TTEntry.swap(l[index1], t);
			if(t.zkey == 0 || t.seq != seq || t.depth > l[index1].depth){
				return;
			} else{
				final int index2 = h2(b, t.zkey, size);
				TTEntry.swap(l[index2], t);
				if(t.zkey == 0 || t.seq != seq || t.depth > l[index2].depth){
					return;
				}
			}
		}
	}
	
	public TTEntry get(final long zkey){
		final int index1 = h1(a, zkey, size);
		if(l[index1].zkey == zkey){
			return l[index1];
		} else{
			final int index2 = h2(b, zkey, size);
			if(l[index2].zkey == zkey){
				return l[index2];
			}
		}
		return null;
	}
	
	private static int h1(final long a, final long zkey, final int size){
		return (int)(a*zkey) >>> 32-size;
	}
	
	private static int h2(final long b, final long zkey, final int size){
		return (int)(b*zkey >>> 64-size);
	}
	
	public void clear(){
		for(int a = 0; a < 1<<size; a++){
			l[a].clear();
		}
	}
}
