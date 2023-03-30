package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private String method;
    private String path;
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String> params = new HashMap<String, String>();

    public HttpRequest(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            if ((line = br.readLine()) == null) {
                return;
            }
            log.debug(line);
            processRequestLine(line);
            line = br.readLine();
            while (!line.equals("")) {
                log.debug(line);
                String[] tokens = line.split(":");
                headers.put(tokens[0].trim(), tokens[1].trim());
                line = br.readLine();
            }

            if (method.equals("POST")) {
                String httpbody = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(httpbody);
            }


        } catch (IOException io) {
            log.error(io.getMessage());
        }
    }

    private void processRequestLine(String line) {
        String[] tokens = line.split(" ");
        method = tokens[0];
        if (method.equals("POST")) {
            path = tokens[1];
            return;
        }
        int index = tokens[1].indexOf("?");
        if (index == -1) {
            path = tokens[1];
        } else {
            path = tokens[1].substring(0, index);
            params = HttpRequestUtils.parseQueryString(tokens[1].substring(index + 1));
        }

    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParam(String name) {
        return params.get(name);
    }
}
