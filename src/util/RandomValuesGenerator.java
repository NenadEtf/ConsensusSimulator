package util;

import java.nio.charset.Charset;
import java.util.concurrent.ThreadLocalRandom;

import consensus.Peer.ConnectionType;

public class RandomValuesGenerator {

	public static final int LAN_MIN_IN_MILIS = 1;
	public static final int LAN_MAX_IN_MILIS = 5;
	public static final int WAN_MIN_IN_MILIS = 70;
	public static final int WAN_MAX_IN_MILIS = 100;
	public static final int MIN_STRING_LENGTH = 5;
	public static final int MAX_STRING_LENGTH = 15;

	public static long getRandomMessageTripDuration(ConnectionType linkType) {
		if (linkType == ConnectionType.WAN) {
			return ThreadLocalRandom.current().nextInt(WAN_MIN_IN_MILIS, WAN_MAX_IN_MILIS + 1);
		}
		if (linkType == ConnectionType.LAN) {
			return ThreadLocalRandom.current().nextInt(LAN_MIN_IN_MILIS, LAN_MAX_IN_MILIS + 1);
		}
		return 0;
	}

	public static String getRandomString() {
		int size = ThreadLocalRandom.current().nextInt(MIN_STRING_LENGTH, MAX_STRING_LENGTH + 1);
		byte[] array = new byte[size];
		ThreadLocalRandom.current().nextBytes(array);
		String generatedString = new String(array, Charset.forName("UTF-8"));
		return generatedString;
	}

}
