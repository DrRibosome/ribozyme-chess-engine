package util.opening2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookTools {
	public static void main(String[] args) throws IOException{
		File in = new File("millionbase-2.22.pgn");
		
		List<BookRecord> l = new ArrayList<>();
		DataInputStream dis = new DataInputStream(new FileInputStream(in));
		while(dis.available() > 0){
			l.add(BookRecord.read(dis));
		}
		dis.close();
		
		//filter here
		
		
		File out = new File("temp.bk");
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(out));
		for(BookRecord r: l){
			r.write(dos);
		}
		dos.close();
	}
}
