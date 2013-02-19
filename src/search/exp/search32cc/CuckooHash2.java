package search.exp.search32cc;

/** rough but working implementation of cuckoo hashing*/
public final class CuckooHash2 {
	private final StateData[] l;
	private final int size;
	private final static int maxAttempts = 16;
	private long a = (long)(Math.random()*Long.MAX_VALUE);
	private long b = (long)(Math.random()*Long.MAX_VALUE);
	
	public CuckooHash2(int size){
		l = new StateData[1<<size];
		for(int q = 0; q < 1<<size; q++){
			l[q] = new StateData();
		}
		this.size = size;
	}
	
	public void put(final long zkey, final StateData t){
		//keep replacing until find an empty entry, entry with bad seq num, or entry with more limited depth
		StateData entry = null;
		if((entry = get(zkey)) == null){
			for(int q = 0; q < maxAttempts; q++){
				final int index1 = h1(a, t.zkey, size);
				StateData.swap(l[index1], t);
				if(t.zkey == 0 || t.seq < l[index1].seq || t.depth < l[index1].depth){
					return;
				} else{
					final int index2 = h2(b, t.zkey, size);
					StateData.swap(l[index2], t);
					if(t.zkey == 0 || t.seq < l[index2].seq || t.depth < l[index2].depth){
						return;
					}
				}
			}
		} else if(t.depth >  entry.depth){
			StateData.swap(t, entry);
		}
	}
	
	public StateData get(final long zkey){
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
}
