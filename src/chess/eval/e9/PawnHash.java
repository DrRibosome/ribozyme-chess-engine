package chess.eval.e9;

import java.util.Random;


/** cucko cucko hashing implementation for storing pawn scores*/
public final class PawnHash{
	private final PawnHashEntry[] l;
	private final int size;
	private final int maxAttempts;
	private final static long a;
	private final static long b;
	private PawnHashEntry loader = new PawnHashEntry();
	private int seq = 0;
	
	static{
		final Random r = new Random(58372L);
		a = (long)(r.nextDouble()*Long.MAX_VALUE);
		b = (long)(r.nextDouble()*Long.MAX_VALUE);
	}
	
	public PawnHash(int size){
		this(size, 16);
	}
	
	public PawnHash(int size, int maxAttempts){
		this.maxAttempts = maxAttempts;
		l = new PawnHashEntry[1<<size];
		for(int q = 0; q < 1<<size; q++){
			l[q] = new PawnHashEntry();
		}
		this.size = size;
	}
	
	public void put(final long zkey, final PawnHashEntry t){
		PawnHashEntry.fill(t, loader);
		loader.seq = seq++;
		for(int q = 0; q < maxAttempts; q++){
			final int index1 = h1(a, t.zkey, size);
			
			if(q == 0 || loader.seq > l[index1].seq){
				final PawnHashEntry temp = loader;
				loader = l[index1];
				l[index1] = temp;
			}
			
			if(loader.zkey == 0){
				return;
			} else{
				final int index2 = h2(b, t.zkey, size);
				
				if(loader.seq > l[index2].seq){
					final PawnHashEntry temp2 = loader;
					loader = l[index2];
					l[index2] = temp2;
				}
				
				if(loader.zkey == 0){
					return;
				}
			}
		}
	}
	
	public PawnHashEntry get(final long zkey){
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
