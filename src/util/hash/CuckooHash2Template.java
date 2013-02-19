package util.hash;

/** rough but working implementation of cuckoo hashing*/
public final class CuckooHash2Template {
	private final long[] l;
	private final int size;
	private final static int maxAttempts = 16;
	private long a = (long)(Math.random()*Long.MAX_VALUE);
	private long b = (long)(Math.random()*Long.MAX_VALUE);
	
	public CuckooHash2Template(int size){
		l = new long[1<<size];
		this.size = size;
	}
	
	public void put(final long zkey){
		long x = zkey;
		for(int q = 0; q < maxAttempts; q++){
			final int index1 = h1(a, x, size);
			final long temp1 = l[index1];
			l[index1] = x;
			x = temp1;
			if(x == 0){
				return;
			} else{
				final int index2 = h2(b, x, size);
				final long temp2 = l[index2];
				l[index2] = x;
				x = temp2;
				if(x == 0){
					return;
				}
			}
		}
		rehash();
		put(x);
	}
	
	public long get(final long zkey){
		final int index1 = h1(a, zkey, size);
		if(l[index1] == zkey){
			return l[index1];
		} else{
			final int index2 = h2(b, zkey, size);
			if(l[index2] == zkey){
				return l[index2];
			}
		}
		/*long x = zkey;
		x = l[h1(a, x, size)];
		if(x == zkey){
			return x;
		} else{
			x = l[h2(b, x, size)];
			if(x == zkey){
				return x;
			}
		}*/
		return 0;
	}
	
	public void remove(final long zkey){
		final int index1 = h1(a, zkey, size);
		if(l[index1] == zkey){
			l[index1] = 0;
		} else{
			final int index2 = h2(b, zkey, size);
			if(l[index2] == zkey){
				l[index2] = 0;
			}
		}
	}
	
	private void rehash(){
		//a = (long)(Math.random()*Long.MAX_VALUE);
		b = (long)(Math.random()*Long.MAX_VALUE);
		for(int q = 0; q < 1<<size; q++){
			final long temp = l[q];
			if(temp != 0 && h1(a, temp, size) != q && h2(b, temp, size) != q){
				l[q] = 0;
				put(temp);
			}
		}
	}
	
	
	private static int h1(final long a, final long zkey, final int size){
		//return (int)(a*zkey) >>> 32-size;
		return (int)(a*zkey) >>> 32-size;
	}
	
	private static int h2(final long b, final long zkey, final int size){
		//return (int)(zkey >>> 32) >>> 32-size;
		return (int)(b*zkey >>> 64-size);
		//return 
	}
}
