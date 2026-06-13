package executorservice;

import awesome.code.base.properties.IPropertiesProvider;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.SessionExpiryHook;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;

import javax.management.relation.RelationSupport;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class KiteConnectProvider {


    private final Map<String,String> idUserMapping;
    private final Map<String, Map<String,String>> clientIdPropertiesMapping;

    public KiteConnectProvider(IPropertiesProvider propertiesProvider)
    {
        clientIdPropertiesMapping = new HashMap<>();
        String clientUserMappingString = propertiesProvider.getStringProperty("option.trading.user.client.mapping",null);
        idUserMapping = getClientUserMapping(clientUserMappingString);
        for(Map.Entry<String, String>  entry: idUserMapping.entrySet())
        {
            String clientId = entry.getValue();
            String userId = propertiesProvider.getStringProperty(String.format("option.trading.user.id.%s",clientId), null);
            String username = propertiesProvider.getStringProperty(String.format("option.trading.user.name.%s",clientId), null);
            String apiSecret = propertiesProvider.getStringProperty(String.format("option.trading.user.api.secret.%s",clientId), null);
            String apiKey = propertiesProvider.getStringProperty(String.format("option.trading.user.api.key.%s",clientId), null);
            String accessToken = propertiesProvider.getStringProperty(String.format("option.trading.user.access.token.%s",clientId), null);
            String publicToken = propertiesProvider.getStringProperty(String.format("option.trading.user.public.token.%s",clientId), null);
            Map<String, String> clientIdProperties = new HashMap<>();
            clientIdProperties.put(Constants.USER_ID, userId);
            clientIdProperties.put(Constants.API_SECRET, apiSecret);
            clientIdProperties.put(Constants.API_KEY, apiKey);
            clientIdProperties.put(Constants.ACCESS_TOKEN, accessToken);
            clientIdProperties.put(Constants.PUBLIC_TOKEN, publicToken);
            clientIdProperties.put(Constants.USER_NAME, username);
            clientIdPropertiesMapping.put(clientId, clientIdProperties);
        }
    }

    public KiteConnect getKiteSDKForUserID(String clientId)  {
        String apiKey = clientIdPropertiesMapping.get(clientId).get(Constants.API_KEY);
        String userId = clientIdPropertiesMapping.get(clientId).get(Constants.USER_ID);
        String accessToken = clientIdPropertiesMapping.get(clientId).get(Constants.ACCESS_TOKEN);
        String publicToken = clientIdPropertiesMapping.get(clientId).get(Constants.PUBLIC_TOKEN);

        KiteConnect kiteSdk = null;
        kiteSdk = new KiteConnect(apiKey);
        kiteSdk.setUserId(userId);
        kiteSdk.setAccessToken(accessToken);
        kiteSdk.setPublicToken(publicToken);
        kiteSdk.setSessionExpiryHook(new SessionExpiryHook() {
            @Override
            public void sessionExpired() {
                System.out.println("session expired");
            }
        });

        return kiteSdk;
    }

    public String getUserName(String clientId)
    {
        String userName = clientIdPropertiesMapping.get(clientId).get(Constants.USER_NAME);
        return userName;
    }

    public Map<Integer, KiteConnect> getKiteConnectForAllClint() {
        Map<Integer, KiteConnect> result = new HashMap<Integer, KiteConnect>();
        for (Map.Entry<String, Map<String,String>> entry : clientIdPropertiesMapping.entrySet())
        {
            int clientId = Integer.parseInt(entry.getKey());
            KiteConnect kiteConnect = getKiteSDKForUserID(entry.getKey());
            result.put(clientId, kiteConnect);
        }
        return result;
    }

    public String clientId(String clientUserId)
    {
        return idUserMapping.get(clientUserId);
    }

    private Map<String,String> getClientUserMapping(String clientUserMapping)
    {
        List<String> clientUsers = Arrays.stream(clientUserMapping.split(",")).collect(Collectors.toList());
        Map<String, String> clientUserMap = new HashMap<>();
        for (String clientUser: clientUsers) {
            List<String> idUser = Arrays.stream(clientUser.split(":")).collect(Collectors.toList());
            clientUserMap.put(idUser.get(0),idUser.get(1));
        }
        return clientUserMap;
    }

}
