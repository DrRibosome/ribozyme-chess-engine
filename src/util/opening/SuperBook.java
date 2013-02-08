package util.opening;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import state4.State4;
import util.AlgebraicNotation2;

public class SuperBook
{
	private class Entry
	{
		public String move;
		public int count, jumpLine;
		public Entry(String m, int c, int j)
		{
			move = m;
			count = c;
			jumpLine = j;
		}
	}
	
	private ArrayList<Entry> availableMoves;
	private File file;
	private FileReader fileReader;
	private BufferedReader bufferedReader;
	private int filePos, player;
	private State4 s;
	
	public SuperBook()
	{
		availableMoves = new ArrayList<Entry>();
	}
	
	public void init(String fileName)
	{
		try
		{
			file = new File(fileName);
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			filePos = 1;
			player = 0;
			String count = bufferedReader.readLine();
			filePos++;
			availableMoves = new ArrayList<Entry>();
			s = new State4();
			s.initialize();
			for(int i = 0; i < Integer.parseInt(count); i++)
			{
				String line = bufferedReader.readLine();
				filePos++;
				String[] props = line.split(" ");
				int[] move = AlgebraicNotation2.getPos(player, props[0], s);
				int[] converted = new int[]{move[0]%8, move[0]/8, move[1]%8, move[1]/8};
				Entry e = new Entry(getMoveString(converted), Integer.parseInt(props[1]), Integer.parseInt(props[2]));
				availableMoves.add(e);
				//System.out.println("adding " + e.move);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void getMove(int[] move)
	{
		Random r = new Random();
		int totalProb = 0;
		for(Entry e : availableMoves)
		{
			totalProb += e.count;
		}
		int prob = r.nextInt(totalProb);
		//System.out.println("prob = " + prob);
		for(Entry e : availableMoves)
		{
			prob -= e.count;
			//System.out.println("prob = " + prob + " move: " + e.move);
			if(prob <= 0)
			{
				System.arraycopy(getMoveArray(e.move), 0, move, 0, 4);
				break;
			}
		}
	}
	
	public void update(int[] m)
	{
		String move = getMoveString(m);
		Entry e = null;
		s.executeMove(player, m[0], m[1], m[2], m[3]);
		player = 1-player;
		for(Entry temp : availableMoves)
		{
			if(temp.move.equals(move))
			{
				e = temp;
			}
		}
		availableMoves = new ArrayList<Entry>();
		if(e != null && e.jumpLine != 0)
		{
			while(filePos < e.jumpLine)
			{
				try
				{
					bufferedReader.readLine();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				filePos++;
			}
			//System.out.println("line " + filePos + " from " + e.count);
			
			try
			{
				String count = bufferedReader.readLine();
				filePos++;
				for(int i = 0; i < Integer.parseInt(count); i++)
				{
					String line = bufferedReader.readLine();
					filePos++;
					String[] props = line.split(" ");
					int[] temp = AlgebraicNotation2.getPos(player, props[0], s);
					int[] converted = new int[]{temp[0]%8, temp[0]/8, temp[1]%8, temp[1]/8};
					Entry t = new Entry(getMoveString(converted), Integer.parseInt(props[1]), Integer.parseInt(props[2]));
					availableMoves.add(t);
					//System.out.println("adding " + t.move);
				}
			}
			catch (IOException exception)
			{
				exception.printStackTrace();
			}
		}
		
	}
	
	public boolean hasMoves()
	{
		return availableMoves.size() > 0;
	}
	
	public static String getMoveString(int[] l)
	{
		return ""+(char)('a'+l[0])+(l[1]+1)+(char)('a'+l[2])+(l[3]+1);
	}
	
	public static int[] getMoveArray(String s)
	{
		int[] m = new int[4];
		m[0] = s.charAt(0) - 'a';
		m[1] = s.charAt(1) - '1';
		m[2] = s.charAt(2) - 'a';
		m[3] = s.charAt(3) - '1';
		return m;
	}
}
