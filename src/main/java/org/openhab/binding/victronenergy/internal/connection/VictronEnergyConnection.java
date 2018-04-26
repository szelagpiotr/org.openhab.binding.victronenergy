package org.openhab.binding.victronenergy.internal.connection;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.smarthome.io.net.http.HttpUtil;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VictronEnergyConnection {

    private final Logger logger = LoggerFactory.getLogger(VictronEnergyConnection.class);

    private static final String METHOD = "GET";

    private static final String WEB_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6InRoaXMtXCJzZWNyZXRcIi12YWx1ZS1zaG91bGQtYmUtcmVtb3ZlZCJ9.eyJpc3MiOiJ2cm1hcGkudmljdHJvbmVuZXJneS5jb20iLCJhdWQiOiJodHRwczpcL1wvdnJtYXBpLnZpY3Ryb25lbmVyZ3kuY29tXC8iLCJqdGkiOiJ0aGlzLVwic2VjcmV0XCItdmFsdWUtc2hvdWxkLWJlLXJlbW92ZWQiLCJpYXQiOjE1MjQ3MzUxNDIsImV4cCI6MTUyNDgyMTU0MiwidWlkIjoiMTQ0NjkiLCJ0b2tlbl90eXBlIjoiZGVmYXVsdCJ9.wy-Ewy6MliByGr3hHBeyovN77-NF92reLSomb1kuemyYoktO2uI5LnYpF79twvFSThrlAgDcRYY2GYbPo8hoOx0a48l7Y5pPTDYMFFsZtz9sUZOPB5ON-xQSoGx9DuWUqC_aXR-ym4d3laFwwf9xBJD8notTaGUp8bRwShvJrEH9wf-iWy5rdR1jzoSLcGFFPSam8bVGQM_0rdzEKw1J26FpGj9n7yFondcJp_usLYBxWx2goQgPLyT2RCyGP0DR989PZYDzJqlst0NjNV4gzkq0nj1bekP1uH2JVXJRk49NYvdYqhm6BhKd792eAlwg6Vtb--ueVB1C8SsByvyDZg";

    private static final String ID_USER = "14469";

    private static final String ID_SITE = "10854";

    private static final int TIMEOUT = 10 * 1000; // 10s

    public String getResponseFromQuery(String location, String apiKey, String lang) {

        StringBuilder s = new StringBuilder();
        Properties httpHeader = new Properties();

        httpHeader.setProperty("X-Authorization", "Bearer " + WEB_TOKEN);

        try {
            return HttpUtil.executeUrl(METHOD,
                    "https://vrmapi.victronenergy.com" + "/v2/installations/" + ID_SITE + "/stats?type=kwh", httpHeader,
                    null, null, TIMEOUT);
            // HttpResponse response = client.execute(request);
            // BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            // String line = "";
            // while ((line = rd.readLine()) != null) {
            // s.append(line);
            // return s.toString();
            // }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

}