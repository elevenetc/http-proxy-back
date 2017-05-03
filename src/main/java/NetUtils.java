import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by eugene.levenetc on 19/04/2017.
 */
public class NetUtils {

    public static String executePost(String targetUrl, Map<String, String> bodyParams) throws IOException {
        String params = "";
        for (String key : bodyParams.keySet()) {
            params += key + "=" + bodyParams.get(key) + "&";
        }
        return executePost(targetUrl, params);
    }

    public static String executePost(String targetURL, String bodyParams) throws IOException {

        URL url = new URL(targetURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", Integer.toString(bodyParams.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");
        connection.setUseCaches(false);
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(bodyParams);
        wr.close();

        InputStream is = connection.getInputStream();

        String result = streamToString(is);

        connection.disconnect();

        return result;

    }


    public static String streamToString(InputStream inputStream) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, "UTF-8");
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }
}
