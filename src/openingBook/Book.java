package openingBook;

import java.io.File;

public final class Book {
	private final static class BookEntry{
		/** key for this position*/
		long zkey;
		long[] moves;
		/** number of moves that lead to this position*/
		int counts = 0;
	}

	/** hash*/
	private final BookEntry[][] h;
	private final int hsize;
	
	public Book(int hashSize){
		h = new BookEntry[hashSize][];
		hsize = hashSize;
	}
	
	public void add(long zkey, long move){
		int index = (int)(zkey>>>(64-hsize));
		if(h[index] == null){
			h[index] = new BookEntry[1];
		}
		boolean added = false;
		for(int a = 0; a < h[index].length && !added; a++){
			if(h[index][a] == null){
				long[] temp = new long[]{move};
				BookEntry b = new BookEntry();
				b.moves = temp;
				b.zkey = zkey;
				h[index][a] = b;
			} else if(h[index][a].zkey == zkey && !contains(h[index][a].moves, move)){
				appendMove(h[index][a], move);
			}
			added = h[index][a] == null || h[index][a].zkey == zkey;
		}
		if(!added){ //hash entry full, failed to insert
			BookEntry[] temp = new BookEntry[h[index].length+5];
			System.arraycopy(h[index], 0, temp, 0, h[index].length);
			h[index] = temp;
			add(zkey, move);
		}
	}
	
	/** accumulate move counts that lead to each position*/
	public void accumulateCounts(){
		for(int a = 0; a < h.length; a++){
			for(int i = 0; i < h[a].length; i++){
				if(h[a][i] == null)
					break;
				
				for(int q = 0; q < h[a][i].moves.length; q++){
					final long zkey = h[a][i].moves[q];
					int index = (int)(h[a][i].moves[q]>>>(64-hsize));
					for(int k = 0; k < h[index].length; k++){
						if(h[index][k].zkey == zkey){
							h[index][k].counts++;
						}
					}
				}
			}
		}
	}
	
	private static void appendMove(BookEntry e, long move){
		long[] temp = new long[e.moves.length+1];
		System.arraycopy(e.moves, 0, temp, 0, e.moves.length);
		temp[e.moves.length] = move;
		e.moves = temp;
	}
	
	private static boolean contains(long[] l, long t){
		for(int a = 0; a < l.length; a++){
			if(l[a] == t){
				return true;
			}
		}
		return false;
	}
}
