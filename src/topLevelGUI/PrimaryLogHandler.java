package topLevelGUI;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * The main log handler. 
 * @author brendano
 *
 */
public class PrimaryLogHandler extends Handler {

	//TODO Write to streams, sysout, etc. 
	
	private SunFishFrame sunfish;
		
	public PrimaryLogHandler(SunFishFrame sunfish) {
		this.sunfish = sunfish;
	}
	
	@Override
	public void publish(LogRecord record) {
		sunfish.setWelcomePanelText(record.getMessage());
	}
	
	@Override
	public void close() throws SecurityException {
		
	}

	@Override
	public void flush() {
		
	}

	

}
