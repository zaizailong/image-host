import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpFileHelper {

    public static final void httpSend(String upload, InputStream is){
        DataOutputStream bos = null;
        try {
            URL url = new URL(upload);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            conn.setRequestProperty("Content-Type", "multipart/form-data");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestMethod("PUT");
            conn.connect();
            bos = new DataOutputStream(conn.getOutputStream());

            byte[] buf = new byte[1024];
            int length = 0;
            while ((length = is.read(buf)) > 0){
                bos.write(buf,0,length);
            }
            bos.flush();
            bos.close();
            if(conn.getResponseCode() != 200){
                System.out.println(conn.getResponseCode());
                System.out.println(conn.getResponseMessage());
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bos != null){
                try {
                    bos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
