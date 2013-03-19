package util.opening2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BookRecord {
	public final long key;
	public final int[] wins;
	public final int count;
	
	public BookRecord(long key, int[] wins, int count){
		this.key = key;
		this.count = count;
		this.wins = new int[2];
		System.arraycopy(wins, 0, this.wins, 0, 2);
	}
	
	public static BookRecord read(DataInputStream dis) throws IOException{
		final long key = dis.readLong();
		final int count = dis.readShort();
		final int[] wins = new int[2];
		wins[0] = dis.readShort();
		wins[1] = dis.readShort();
		return new BookRecord(key, wins, count);
	}
	
	public void write(DataOutputStream dos) throws IOException{
		dos.writeLong(key);
		dos.writeShort(count);
		dos.writeShort(wins[0]);
		dos.writeShort(wins[1]);
	}
}
