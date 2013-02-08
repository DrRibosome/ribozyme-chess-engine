package util.board4;

/** records previously seen board states to detect 3-fold repition draws*/
final class HistoryMap{
	private final static class Record{
		long zkey;
		/** stores the number of times this state has appeared*/
		int count = 0;
	}
	
	private final Record[][] r;
	private final int size;
	private final int backups;
	private boolean isDrawable = false;
	
	/** size as power of 2*/
	public HistoryMap(int size, int backups){
		r = new Record[1<<size][backups];
		for(int a = 0; a < (1<<size); a++){
			for(int q = 0; q < backups; q++){
				r[a][q] = new Record();
			}
		}
		this.size = size;
		this.backups = backups;
	}
	
	/** clears the mapping, very expensive operation*/
	public void clear(){
		for(int a = 0; a < (1<<size); a++){
			for(int q = 0; q < backups; q++){
				r[a][q].count = 0;
				r[a][q].zkey = 0;
			}
		}
	}
	
	public int getCount(long zkey){
		//return isDrawable;
		int index = (int)(zkey>>>(64-size));
		if(r[index][0].zkey == zkey){
			return r[index][0].count;
		} else{
			for(int a = 1; a < backups; a++){
				if(r[index][a].zkey == zkey){
					return r[index][a].count;
				}
			}
			return 0;
		}
	}
	
	/** inserts a new move, which is same as declining a draw*/
	public void put(long zkey){
		isDrawable = false;
		int index = (int)(zkey>>>(64-size));
		if(r[index][0].count == 0 || r[index][0].zkey == zkey){
			r[index][0].zkey = zkey;
			r[index][0].count++;
			//isDrawable = r[index][0].count >= 3;
		} else{
			for(int a = 1; a < backups; a++){
				if(r[index][a].count == 0 || r[index][a].zkey == zkey){
					r[index][a].zkey = zkey;
					r[index][a].count++;
					//isDrawable = r[index][a].count >= 3;
					return;
				}
			}
			System.out.println("insertion failed!");
			assert false;
		}
	}
	
	public void remove(long zkey){
		int index = (int)(zkey>>>(64-size));
		if(r[index][0].zkey == zkey){
			r[index][0].count--;
			assert r[index][0].count >= 0;
			//isDrawable = r[index][0].count >= 3;
		} else{
			for(int a = 1; a < backups; a++){
				if(r[index][a].zkey == zkey){
					r[index][a].count--;
					assert r[index][a].count >= 0;
					//isDrawable = r[index][a].count >= 3;
					return;
				}
			}
		}
	}
}