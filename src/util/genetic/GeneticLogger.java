package util.genetic;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import eval.expEvalV3.EvalParameters;

/** logs and loads the logs for genetic simuations*/
public final class GeneticLogger {
	private final static int typeIterationRecord = 0;
	private final static int typeEntityRecord = 1;
	
	private final ByteBuffer b = ByteBuffer.allocate(1<<15);
	private final FileChannel f;

	@SuppressWarnings("resource")
	public GeneticLogger(File f) throws IOException{
		this.f = new FileOutputStream(f).getChannel();
	}
	
	public void recordGEntity(final GEntity e) throws IOException{
		b.clear();
		b.put((byte)typeEntityRecord);
		b.putInt(e.id);
		e.p.write(b);
		
		b.limit(b.position());
		b.rewind();
		f.write(b);
	}
	
	public void recordIteration(final int iteration, final GEntity[] p) throws IOException{
		b.clear();
		b.put((byte)typeIterationRecord);
		b.putInt(iteration);
		b.putInt(p.length);
		for(int a = 0; a < p.length; a++){
			b.putInt(p[a].id);
			b.putInt(p[a].wins.get());
			b.putInt(p[a].losses.get());
			b.putInt(p[a].draws.get());
		}
		
		b.limit(b.position());
		b.rewind();
		f.write(b);
	}
	
	public final static class IterationResult{
		public int iteration;
		/** stores results, each entry formatted [id,wins,losses,draws]*/
		public final List<GEntity> results = new ArrayList<GEntity>();
	}
	/** representation for genetic lods*/
	public final static class GLog{
		/** stores iteration result offsets, indexed [iteration]:offset-index*/
		public final Map<Integer, IterationResult> iterationResults = new HashMap<Integer, IterationResult>();
		public final Map<Integer, EvalParameters> params = new HashMap<Integer, EvalParameters>();
	}
	/** probes passed file for list of game iterations*/
	public static GLog loadLog(final File file) throws IOException{
		final GLog offsets = new GLog();
		final DataInputStream dis = new DataInputStream(new FileInputStream(file));
		
		//buffer for reading eval parameters, must be large enough to store one parameter object dump
		final byte[] buff = new byte[1<<15];
		
		while(dis.available() > 0){
			final int type = dis.read();
			if(type == typeEntityRecord){
				final int id = dis.readInt();
				final int readLen = dis.read(buff);
				ByteBuffer b = ByteBuffer.wrap(buff);
				b.limit(readLen);
				final EvalParameters temp = new EvalParameters();
				temp.read(b);
				dis.skip(-(b.limit()-b.position()));
				offsets.params.put(id, temp);
			} else if(type == typeIterationRecord){
				final IterationResult temp = new IterationResult();
				final int iteration = dis.readInt();
				temp.iteration = iteration;
				final int len = dis.readInt();
				for(int a = 0; a < len; a++){
					GEntity e = new GEntity();
					e.id = dis.readInt();
					e.wins.set(dis.readInt());
					e.losses.set(dis.readInt());
					e.draws.set(dis.readInt());
					temp.results.add(e);
				}
				offsets.iterationResults.put(iteration, temp);
			}
		}
		dis.close();
		return offsets;
	}
}
