package util.opening1;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


public class BookGenerator
{
	private BookMove root;
	private ArrayList<BookMove> moveQueue;
	private int cullCount;
	
	public BookGenerator(int c)
	{
		root = new BookMove("game start");
		cullCount = c;
	}
	
	public void loadGames(File file)
	{
		try
		{
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			String line;
			boolean inGame = false;
			boolean skippingGame = false;
			String gameBuffer = "";
			int lineNum = 0;
			
			while((line = bufferedReader.readLine()) != null)
			{
				lineNum++;
				if(lineNum % 100000 == 0)
				{
					System.out.println(lineNum);
				}
				if(line.contains("FEN"))
				{
					skippingGame = true;
				}
				if(line.contains("Event"))
				{
					skippingGame = false;
				}
				if(skippingGame)
				{
					continue;
				}
				if(line.equals("") || line.charAt(0) == '[')
				{
					if(inGame)
					{
						if(gameBuffer.contains("{"))
						{
							int c1 = gameBuffer.indexOf('{');
							int c2 = gameBuffer.indexOf('}');
							//System.out.println(file.getName() + gameBuffer);
							gameBuffer.replace(gameBuffer.substring(c1, c2), "");
						}
						parseGame(root, 0, gameBuffer.split(" "));
					}
					inGame = false;
					continue;
				}
				else
				//line has moves on it
				//if(line.charAt(0) >= '1' && line.charAt(0) <= '9')
				{
					//new game?
					if(!inGame)
					{
						//reset game buffer
						gameBuffer = "";
						gameBuffer += line;
						inGame = true;
					}
					else
					{
						gameBuffer += line;
					}
				}
			}
			
			
			fileReader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void parseGame(BookMove rootMove, int index, String[] moves)
	{
		if(moves.length > index)
		{
			String move = moves[index];
			if(move.contains(".."))
			{
				return;
			}
			if(move.equals("") || move.length() < 2)
			{
				parseGame(rootMove, index+1, moves);
				return;
			}
			//System.out.println(move);
			//take out the move numbering or score indication
			if(move.contains("."))
			{
				String[] s = move.split("\\.");
				if(s.length < 2)
				{
					parseGame(rootMove, index+1, moves);
					return;
				}
				move = s[1];
			}
			else if(move.contains("-"))
			{
				return;
			}
			
			if(rootMove.hasMove(move))
			{
				rootMove.getMove(move).count++;
				parseGame(rootMove.getMove(move), index+1, moves);
			}
			else
			{
				BookMove bm = new BookMove(move);
				rootMove.addMove(bm);
				parseGame(bm, index+1, moves);
			}
		} 
	}
	
	public void genFile(String fileName)
	{
		try 
		{
			File file = new File(fileName);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			writeBook(bw);
			bw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void writeBook(BufferedWriter bw)
	{
		int pos = 1;
		moveQueue = new ArrayList<BookMove>();
		cullNodes(root);
		enqueueNodes(root);
		
		for(BookMove bm : moveQueue)
		{
			bm.filePos = pos;
			pos += bm.getSize()+1;
		}
		for(BookMove bm : moveQueue)
		{
			try
			{
				bw.write(bm.getSize() + "\n");
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			for(int i = 0; i < bm.getSize(); i++)
			{
				int jump = bm.get(i).filePos == 0 ? 0 : bm.get(i).filePos;
				try
				{
					bw.write(bm.get(i).move + " " + bm.get(i).count + " " + jump + "\n");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}		
		}
	}
	
	private void enqueueNodes(BookMove bm)
	{
		if(bm.getSize() > 0)
		{
			moveQueue.add(bm);
		}
		else
		{
			return;
		}

		for(int i = 0; i < bm.getSize(); i++)
		{
			enqueueNodes(bm.get(i));
		}
	}
	
	private void cullNodes(BookMove bm)
	{
		Iterator<BookMove> it = bm.continuations.iterator();
		while(it.hasNext())
		{
			BookMove temp = it.next();
			if(temp.count < cullCount)
			{
				it.remove();
			}
			else
			{
				cullNodes(temp);
			}
		}
	}
	
	public static void main(String args[])
	{
		/*BookGenerator bg = new BookGenerator(10);
		//System.out.println(new String("asd").split("s")[0]);
		File f = new File("C:/Users/Kyle/Music/millionbase"); 
		//bg.loadGames(f);
		File[] games = f.listFiles();
		for(int i = 0; i < games.length; i++)
		{
			bg.loadGames(games[i]);
			System.out.println("" + (i+1) + "/" + games.length);
		}
		
		bg.genFile("millionBook.txt");*/
		
		SuperBook sb = new SuperBook();
		sb.init("millionBook.txt");
		int[] move = new int[4];
		sb.getMove(move);
		System.out.println("move from book: " + SuperBook.getMoveString(move));
		sb.update(move);
		sb.getMove(move);
		System.out.println("move from book: " + SuperBook.getMoveString(move));
		sb.update(move);
		sb.getMove(move);
		System.out.println("move from book: " + SuperBook.getMoveString(move));
		sb.update(move);
		sb.getMove(move);
		System.out.println("move from book: " + SuperBook.getMoveString(move));
		sb.update(move);
		sb.getMove(move);
		System.out.println("move from book: " + SuperBook.getMoveString(move));
	}
}
