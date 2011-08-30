
package sunfish;

import topLevelGUI.SunFishFrame;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * The Mac OS handles application exit (quit) events differently than Linux, and therefore
 * we need a separate listener for mac quit events. This stuff relies on libraries that
 * will not exist on non-mac systems, so it is imperative that THIS CLASS IS NOT LOADED
 * ON NON-MAC SYSTEMS!
 * 
 * @author brendan
 *
 */
public class MacQuitHandler extends ApplicationAdapter {

	SunFishFrame sunfish;
	
	public MacQuitHandler(SunFishFrame sunfish) {
		this.sunfish = sunfish;
	    Application macApplication = Application.getApplication();
	    macApplication.addApplicationListener(this);
	}

	public void handleQuit(ApplicationEvent arg0) {
		SunFishApp.getApplication().shutdown();
	}

}

