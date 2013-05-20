package util;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

public final class UnsafeUtil {
	/** gets a reference to Unsafe through reflection, ignore the errors*/
	public static Unsafe getUnsafe() {
	    try {
	            Field f = Unsafe.class.getDeclaredField("theUnsafe");
	            f.setAccessible(true);
	            return (Unsafe)f.get(null);
	    } catch (Exception e){
	    	System.out.println("unable to get reference to Unsafe class");
	    	e.printStackTrace();
	    }
	    return null;
	}
}
