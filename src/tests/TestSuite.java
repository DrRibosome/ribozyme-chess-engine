package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import search.SearchS4V22qzit;
import search.SearchS4V23qzit;
import util.AlgebraicNotation2;
import util.board4.Debug;
import util.board4.State4;
import ai.modularAI2.Evaluator2;
import ai.modularAI2.Search2;
import customAI.evaluators.board4.EvalS4;
import customAI.evaluators.board4.SuperEvalS4V7;
import customAI.searchers.board4.SearchS4V21qzit;
import eval.TestEval;

public class TestSuite
{
	ArrayList<State4> positions;
	ArrayList<Integer> turnList;
	ArrayList<String> bestMoves;
	
	public TestSuite(String filename)
	{
		initialize(filename);
	}
	
	private void initialize(String filename)
	{
		ArrayList<String> lines = new ArrayList<String>();
		positions = new ArrayList<State4>();
		bestMoves = new ArrayList<String>();
		turnList = new ArrayList<Integer>();
		File file = new File(filename);
		try
		{
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while((line = bufferedReader.readLine()) != null)
			{
				lines.add(line);
			}
			fileReader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		//loading boards
		for(int i = 0; i < lines.size(); i++)
		{ 
			char[][] board = new char[9][8];
			String line = lines.get(i);
			String[] pieces = line.split(" ");
			String[] boardRows = pieces[0].split("/");
			for(int j = 0; j < 8; j++)
			{
				int pos = 0;
				for(int k = 0; k < boardRows[j].length(); k++)
				{
					char c = boardRows[j].charAt(k); 
					if(c >= '1' && c <= '8')
					{
						int spaces = (int)(c - '0');
						for(int l = 0; l < spaces; l++)
						{
							board[j][pos] = ' ';
							pos++;
						}
					}
					else 
					{
						if(Character.isLowerCase(c))
							board[j][pos] = Character.toUpperCase(c);
						else
							board[j][pos] = Character.toLowerCase(c);
						pos++;
					}
				}
			}
			//side to move
			int turn = pieces[1].charAt(0) == 'w' ? 0 : 1;
			turnList.add(turn);
			
			board[8][0] = '1';
			board[8][1] = '1';
			board[8][2] = '1';
			board[8][3] = '1';
			board[8][4] = '1';
			board[8][5] = '1';
			
			if(!pieces[2].equals("-"))
			{
				if(pieces[2].contains("k") || pieces[2].contains("q"))
				{
					board[8][1] = '0';
					if(pieces[2].contains("q"))
						board[8][4] = '0';
					if(pieces[2].contains("k"))
						board[8][5] = '0';
				}
				if(pieces[2].contains("K") || pieces[2].contains("Q"))
				{
					board[8][0] = '0';
					if(pieces[2].contains("Q"))
						board[8][2] = '0';
					if(pieces[2].contains("K"))
						board[8][3] = '0';
				}
			}
			int index = 5;
			String bests = "";
			String best = pieces[index];
			bests += best;
			while(pieces[index].charAt(pieces[index].length()-1) != ';')
			{
				index++;
				best = pieces[index];
				bests += " " + best;
			} 
			bests = bests.replaceAll(";", "");
			bestMoves.add(bests);
			positions.add(Debug.loadConfig(board));
		}
	}
	
	public static void main(String[] args)
	{
		TestSuite ts = new TestSuite("wac.txt");
		//TestSuite ts = new TestSuite("kaufman.txt");
		//TestSuite ts = new TestSuite("silent-but-deadly");
		
		Evaluator2<State4> e2 =
				//new SuperEvalS4V7();
				new EvalS4();
				//new TestEval();
		
		//	1 sec time control
		//	version	depth	correct		notes
		//	20		16		242			
		

		//9/10 on raw eval for 218/230 respectively
		
		int solved = 0;
		for(int i = 0; i < ts.positions.size(); i++)
		//for(int i = 298; i == 298; i++)
		{
			System.out.println(ts.positions.get(i));
			System.out.println("best move: " + ts.bestMoves.get(i));
			System.out.println("side to move: " + ts.turnList.get(i));
			
			State4 s = ts.positions.get(i);
			int player = ts.turnList.get(i);
			
			Search2<State4> search =
					//new SearchS4V21qzit(16, s, e2, 20);
					//new SearchS4V22qzit(20, s, e2, 20);
					new SearchS4V23qzit(20, s, e2, 20);
			
			int[] move = new int[4];
			
			search.getMove(move, player, 1*1000);
			//search.getMove(move, player);
			
			String[] bests = ts.bestMoves.get(i).split(" ");
			for(int j = 0; j < bests.length; j++)
			{
				int[] best = AlgebraicNotation2.getPos(player, bests[j], s);
				System.out.println("best = "+best[0]+" -> "+best[1]);
				if(best[0]%8 == move[0] && best[0]/8 == move[1] && best[1]%8 == move[2] && best[1]/8 == move[3]){
					solved++;
					break;
				}
			}
			
			System.out.println("solved total = "+solved+" / "+(i+1)+"\n");
		}
		
		System.out.println("total solved = "+solved);
	}

}
