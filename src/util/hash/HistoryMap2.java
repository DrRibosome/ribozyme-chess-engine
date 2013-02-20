package util.hash;

/** int map through cuckoo hashing, used for hashing historical positions*/
public final class HistoryMap2 {
	private final long[] keys;
	private final int[] values;
	private final int size;
	private final static int maxAttempts = 32;
	private long a = (long)(Math.random()*Long.MAX_VALUE);
	private long b = (long)(Math.random()*Long.MAX_VALUE);
	
	public HistoryMap2(int size){
		keys = new long[1<<size];
		values = new int[1<<size];
		this.size = size;
	}
	
	public void put(final long key){
		put(key, 1);
	}
	
	private void put(final long zkey, final int amount){
		final int testIndex1 = h1(a, zkey, size);
		if(keys[testIndex1] == zkey){
			values[testIndex1] += amount;
		} else{
			final int testIndex2 = h2(b, zkey, size);
			if(keys[testIndex2] == zkey){
				values[testIndex2] += amount;
			} else{
				long k = zkey;
				int v = amount;
				for(int q = 0; q < maxAttempts; q++){
					final int index1 = h1(a, k, size);
					final long tempk1 = keys[index1];
					final int tempv1 = values[index1];
					keys[index1] = k;
					values[index1] = v;
					k = tempk1;
					v = tempv1;
					if(v == 0 || k == 0){ //slot empty or unused
						return;
					} else{
						final int index2 = h2(a, k, size);
						final long tempk2 = keys[index2];
						final int tempv2 = values[index2];
						keys[index2] = k;
						values[index2] = v;
						k = tempk2;
						v = tempv2;
						if(v == 0 || k == 0){
							return;
						}
					}
				}
				rehash();
				put(k);
			}
		}
	}
	
	public long get(final long zkey){
		final int index1 = h1(a, zkey, size);
		if(keys[index1] == zkey){
			return values[index1];
		} else{
			final int index2 = h2(b, zkey, size);
			if(keys[index2] == zkey){
				return values[index2];
			}
		}
		return 0;
	}
	
	public void remove(final long zkey){
		final int index1 = h1(a, zkey, size);
		if(keys[index1] == zkey){
			values[index1]--;
		} else{
			final int index2 = h2(b, zkey, size);
			if(keys[index2] == zkey){
				values[index2]--;
			}
		}
	}
	
	private void rehash(){
		a = (long)(Math.random()*Long.MAX_VALUE);
		b = (long)(Math.random()*Long.MAX_VALUE);
		for(int q = 0; q < 1<<size; q++){
			final long tempk = keys[q];
			final int tempv = values[q];
			if(tempv != 0 && tempk != 0 && h1(a, tempk, size) != q && h2(b, tempk, size) != q){
				keys[q] = 0;
				values[q] = 0;
				put(tempk, tempv);
			}
		}
	}
	
	
	private static int h1(final long a, final long zkey, final int size){
		return (int)(a*zkey) >>> 32-size;
	}
	
	private static int h2(final long b, final long zkey, final int size){
		return (int)(b*zkey >>> 64-size);
	}
}
