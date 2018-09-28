package util;

public class ConcurrentPrintWriter {

	public enum PrintMode {
		ON, OFF
	};

	public static PrintMode mode = PrintMode.ON;

	public static final void printOnSystemOut(String text) {
		synchronized (System.out) {
			if (mode == PrintMode.ON) {
				System.out.println(text);
			}
		}
	}

}
