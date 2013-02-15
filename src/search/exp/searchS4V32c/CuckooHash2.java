package search.exp.searchS4V32c;


public abstract class CuckooHash2 <T> {
	public final static class BasicCuckooHash <T> extends CuckooHash2<T>{
		public BasicCuckooHash(Encoder<T> e, int size){
			super(e, size);
		}
		
		@Override
		protected int h1(long key) {
			final int z = (int)(key ^ (key >>> 32));
			return z >>> (32-size);
		}

		@Override
		protected int h2(long key) {
			final int z = (int)(key ^ (key >>> 32));
			final int remaining = 32-size;
			return (z << remaining) >>> remaining; //possibly and with ~-1?
		}
	}
	
	private final Encoder<T> e;
	private final long[][] store;
	private final long[][] storeAny;
	protected final int size;
	
	public CuckooHash2(Encoder<T> e, int size){
		this.e = e;
		store = new long[1<<size][e.requiredStoreSize()];
		storeAny = new long[1<<size][e.requiredStoreSize()];
		this.size = size;
	}
	
	public void put(final long key, final T t){
		final int index1 = h1(key);
		if(store[index1][0] == key){
			e.encode(t, store[index1]);
		} else{
			final int index2 = h2(key);
			if(store[index2][0] == key){
				e.encode(t, store[index2]);
			} else{
				/*final long[] temp = store[index2];
				store[index2] = store[index1];
				store[index1] = temp;
				e.encode(t, store[index1]);*/
				
				StateDataV1 s = (StateDataV1)t;
				
				int d1 = (int)(store[index1][3] >>> 32);
				int d2 = (int)(store[index2][3] >>> 32);
				if(s.depth >= d1 || s.depth >= d2){
					if(d1 > d2){
						e.encode(t, store[index2]);
					} else{
						e.encode(t, store[index1]);
					}
				} else{
					put2(key, t);
				}
			}
		}
	}
	
	private void put2(final long key, final T t){
		final int index1 = h1(key);
		if(storeAny[index1][0] == key){
			e.encode(t, storeAny[index1]);
		} else{
			final int index2 = h2(key);
			if(storeAny[index2][0] == key){
				e.encode(t, storeAny[index2]);
			} else{
				final long[] temp = storeAny[index2];
				storeAny[index2] = storeAny[index1];
				storeAny[index1] = temp;
				e.encode(t, storeAny[index1]);
			}
		}
	}
	
	public boolean get(final long key, final T t){
		final int index1 = h1(key);
		if(store[index1][0] == key){
			e.load(t, store[index1]);
			return true;
		} else{
			final int index2 = h2(key);
			if(store[index2][0] == key){
				e.load(t, store[index2]);
				return true;
			}
		}
		return false;
	}
	
	/** hash function 1*/
	protected abstract int h1(long key);
	/** hash function 2*/
	protected abstract int h2(long key);
}
