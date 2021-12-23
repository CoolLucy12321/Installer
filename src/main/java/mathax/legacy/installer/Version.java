package mathax.legacy.installer;

import mathax.legacy.json.JSONUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Version {
    private final String string;
    private final int[] numbers;

    public Version(String string) {
        this.string = string;
        this.numbers = new int[3];

        String[] split = string.split("\\.");
        if (split.length != 3) throw new IllegalArgumentException("Version string needs to have 3 numbers.");

        for (int i = 0; i < 3; i++) {
            try {
                numbers[i] = Integer.parseInt(split[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Failed to parse version string.");
            }
        }
    }

    public boolean isHigherThan(Version version) {
        for (int i = 0; i < 3; i++) {
            if (numbers[i] > version.numbers[i]) return true;
            if (numbers[i] < version.numbers[i]) return false;
        }

        return false;
    }

    public static Version get() throws IOException {
        InputStream is = Version.class.getResourceAsStream("/metadata.json");
        Scanner s = new Scanner(is).useDelimiter("\\A");

        JSONObject json = new JSONObject(s.hasNext() ? s.next() : "");
        return new Version(json.getString("version"));
    }

    @Override
    public String toString() {
        return string;
    }

    public static class UpdateChecker {
        public static String getLatest() {
            String latestVer = null;
            try {
                JSONObject json = JSONUtils.readJsonFromUrl(Installer.API_URL + "metadata.json");
                latestVer = json.getString("version");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (latestVer == null) return null;
            return latestVer.replace("\n", "");
        }

        public static CheckStatus checkLatest() throws IOException {
            String latestVersion = getLatest();

            if (latestVersion == null) return CheckStatus.Cant_Check;
            else {
                Version latestVer = new Version(latestVersion);
                Version currentVer = get();
                if (latestVer.isHigherThan(currentVer)) return CheckStatus.Newer_Found;
                else return CheckStatus.Latest;
            }
        }

        public enum CheckStatus {
            Cant_Check,
            Newer_Found,
            Latest
        }
    }
}
