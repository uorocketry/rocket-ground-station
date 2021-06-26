package uorocketry.basestation.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RssiProcessor {

    private static final Pattern validMessage = Pattern.compile("L\\/R RSSI: .+dco=[^\\s]$");

    private static final Pattern localRSSI = Pattern.compile("L\\/R RSSI: ([^\\s]+)\\/");
    private static final Pattern remoteRSSI = Pattern.compile("L\\/R RSSI: [^\\s]+\\/([^\\s]+)");
    private static final Pattern localNoise = Pattern.compile("L\\/R noise: ([^\\s]+)\\/");
    private static final Pattern remoteNoise = Pattern.compile("L\\/R noise: [^\\s]+\\/([^\\s]+)");
    private static final Pattern packets = Pattern.compile("pkts: ([^\\s]+)");
    private static final Pattern txe = Pattern.compile("txe=([^\\s]+)");
    private static final Pattern rxe = Pattern.compile("rxe=([^\\s]+)");
    private static final Pattern stx = Pattern.compile("stx=([^\\s]+)");
    private static final Pattern srx = Pattern.compile("srx=([^\\s]+)");
    private static final Pattern ecc1 = Pattern.compile("ecc=([^\\s]+)\\/");
    private static final Pattern ecc2 = Pattern.compile("ecc=[^\\s]+\\/([^\\s]+)");
    private static final Pattern temperature = Pattern.compile("temp=([^\\s]+)");
    private static final Pattern dco = Pattern.compile("dco=([^\\s]+)");

    private static final Pattern[] patterns = {
            localRSSI,
            remoteRSSI,
            localNoise,
            remoteNoise,
            packets,
            temperature,
            txe,
            rxe,
            stx,
            srx,
            ecc1,
            ecc2,
            dco
    };

    public static String[] labels = {
            "Local RSSI",
            "Remote RSSI",
            "Local Noise",
            "Remote Noise",
            "Packets",
            "Temperature",
            "txe",
            "rxe",
            "stx",
            "srx",
            "ecc1",
            "ecc2",
            "dco"
    };

    public static boolean isValid(String data) {
        return validMessage.matcher(data).find();
    }

    public static boolean setDataHolder(DataHolder dataHolder, String data) {
        for (int i = 0; i < patterns.length; i++) {
            String value = getCapturedValue(patterns[i], data);

            // If setting failed
            if (value == null || !dataHolder.set(i, value)) {
                return false;
            }
        }

        return true;
    }

    private static String getCapturedValue(Pattern pattern, String data) {
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
