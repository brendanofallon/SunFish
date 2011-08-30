package display;

import java.util.ArrayList;
import java.util.List;

public class DisplayRegistry {

	List<Display> displays = new ArrayList<Display>();
	
	public void addDisplay(Display d) {
		displays.add(d);
	}
	
	public void addDisplay(List<Display> dList) {
		for(Display d : dList) {
			addDisplay(d);
		}
	}
	
	public List<Display> getAllDisplays() {
		return displays;
	}
	
	/**
	 * Returns a list of Displays that can possibly display objects of the given type.
	 * @param clazz
	 * @return
	 */
	public List<Display> getDisplaysForClass(Class clazz) {
		List<Display> okDisplays = new ArrayList<Display>();
		for(Display display : displays) {
			Class[] displayableClasses = display.getDisplayableClasses();
			for(int i=0; i<displayableClasses.length; i++) {
				if (displayableClasses[i].isAssignableFrom(clazz)) {
					okDisplays.add(display);
					break;
				}
			}
		}
		
		return okDisplays;
	}
	
	/**
	 * Pick one display that can handle the object provided. 
	 * @param toDisplay
	 * @return
	 */
	public Display pickDisplayForObject(Object toDisplay) {
		List<Display> possibles = getDisplaysForClass(toDisplay.getClass());
		if (possibles.size()==0) {
			return null;
		}
		else 
			return possibles.get(0);
	}
}
