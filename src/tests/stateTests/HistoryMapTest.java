package tests.stateTests;

import java.util.HashMap;
import java.util.Map;

import util.board4.HistoryMap;

public class HistoryMapTest {
	public static void main(String[] args){
		HistoryMap m = new HistoryMap(12, 4);

		System.out.println(m.isDrawable(123));
		m.put(123);
		System.out.println(m.isDrawable(123));
		m.put(123);
		System.out.println(m.isDrawable(123));
		m.put(123);
		System.out.println(m.isDrawable(123));
		m.remove(123);
		System.out.println(m.isDrawable(123));
		m.remove(123);
		m.remove(123);
		
		for(int a = 0; a < 99999; a++){
			long l = (long)(Math.random()*Long.MAX_VALUE);
			for(int q = 0; q < 2; q++){
				m.put(l);
			}
			assert !m.isDrawable(l);
			
			m.put(l);
			assert m.isDrawable(l);
			for(int q = 0; q < 3; q++){
				m.remove(l);
			}
			assert !m.isDrawable(l);
		}
			
	}
}
