package errorHandling;

public class FileParseException extends Exception {

	int badLineNumber = -1;
	String badFileName = "";
	
	
	public FileParseException(String msg) {
		super(msg);
	}

	public FileParseException(String msg, int linenum) {
		super(msg);
		badLineNumber = linenum;
	}
	
	public FileParseException(String msg, int linenum, String filename) {
		super(msg);
		badLineNumber = linenum;
		badFileName = filename;
	}
	
	public int getLineNumber() {
		return badLineNumber;
	}
	
	public String getFileName() {
		return badFileName;
	}
	
}
