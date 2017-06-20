package chatbot.lib.api;

import chatbot.lib.response.ResponseData;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramgathreya on 6/20/17.
 */
public class GenesisService {
    private static final int timeout = 0;
    private static final String GENESIS_URL = "http://genesis.aksw.org";
    private HttpClient client;

    public GenesisService() {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(this.timeout).build();
        this.client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    }

    private String makeRequest(String endpoint, String url, String key) {
        try {
            HttpPost httpPost = new HttpPost(endpoint);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("url", url));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
            httpPost.setEntity(entity);

            HttpResponse response = client.execute(httpPost);

            // Error Scenario
            if(response.getStatusLine().getStatusCode() >= 400) {
                throw new Exception("Genesis Server could not answer due to: " + response.getStatusLine());
            }

            String entities = EntityUtils.toString(response.getEntity());
            JsonNode rootNode = new ObjectMapper().readTree(entities).get(key);
            String uris = "";
            int count = 0;

            for (JsonNode node : rootNode) {
                count++;
                if (count <= ResponseData.MAX_DATA_SIZE) {
                    uris += "<" + node.get("url").getTextValue() + "> ";
                }
                else {
                    break;
                }
            }
            return uris.trim();
        }
        catch (Exception e) {

        }
        return null;
    }

    public String getSimilarEntities(String uri) {
         return makeRequest(GENESIS_URL + "/api/similar", uri, "similarEntities");
    }

    public String getRelatedEntities(String uri) {
        return makeRequest(GENESIS_URL + "/api/related", uri, "relatedEntities");
    }
}