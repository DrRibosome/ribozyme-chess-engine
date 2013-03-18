package util.opening1;


import java.util.ArrayList;

public class BookMove
{
	public ArrayList<BookMove> continuations;
	public String move;
	public int count, filePos;
	
	public BookMove(String m)
	{
		this.continuations = new ArrayList<BookMove>();
		this.count = 1;
		this.move = m;
	}
	
	public void addMove(BookMove bm)
	{
		continuations.add(bm);
	}
	
	public boolean hasMove(String s)
	{
		for(BookMove bm : continuations)
		{
			//System.out.println("comparing " + s + " and " + bm.move + bm.move.equals(s));
			if(bm.move.equals(s))
				return true;
		}
		return false;
	}
	
	public BookMove getMove(String s)
	{
		for(BookMove bm : continuations)
		{
			if(bm.move.equals(s))
				return bm;
		}
		return null; 
	}
	
	public BookMove get(int i)
	{
		return continuations.get(i);
	}
	
	public int getSize()
	{
		return continuations.size();
	}
	
	public String toString()
	{
		String s = move + " continuations:\n";
		for(BookMove bm : continuations)
		{
			s += bm.move + " " + bm.count+ "\n";
		}
		return s;
	}
}
