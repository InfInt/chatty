
package chatty.util.api;

import chatty.util.DateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Some more simple parsing.
 * 
 * @author tduva
 */
public class Parsing {
    
    private static final Logger LOGGER = Logger.getLogger(Parsing.class.getName());
    
    /**
     * Parse the list of games that was returned by the game search.
     * 
     * @param json
     * @return 
     */
    public static Set<String> parseGameSearch(String json) {
        Set<String> result = new HashSet<>();
        try {
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject)parser.parse(json);
            
            Object games = root.get("games");
            
            if (!(games instanceof JSONArray)) {
                LOGGER.warning("Error parsing game search: Should be array");
                return null;
            }
            Iterator it = ((JSONArray)games).iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof JSONObject) {
                    String name = (String)((JSONObject)obj).get("name");
                    result.add(name);
                }
            }
            return result;
            
        } catch (ParseException ex) {
            LOGGER.warning("Error parsing game search.");
            return null;
        } catch (NullPointerException ex) {
            LOGGER.warning("Error parsing game search: Unexpected null");
            return null;
        } catch (ClassCastException ex) {
            LOGGER.warning("Error parsing game search: Unexpected type");
            return null;
        }
    }
    
    public static long followGetTime(String json) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(json);
            long time = DateTime.parseDatetime((String)root.get("created_at"));
            return time;
        } catch (Exception ex) {
            return -1;
        }
    }
    
    /**
     * Parses the JSON returned from the TwitchAPI that contains the token
     * info into a TokenInfo object.
     * 
     * @param json
     * @return The TokenInfo or null if an error occured.
     */
    public static TokenInfo parseVerifyToken(String json) {
        if (json == null) {
            LOGGER.warning("Error parsing verify token result (null)");
            return null;
        }
        try {
            JSONParser parser = new JSONParser();
            JSONObject root = (JSONObject) parser.parse(json);
            
            JSONObject token = (JSONObject) root.get("token");
            
            if (token == null) {
                return null;
            }
            
            boolean valid = (Boolean)token.get("valid");
            
            if (!valid) {
                return new TokenInfo();
            }
            
            String username = (String)token.get("user_name");
            String id = (String)token.get("user_id");
            JSONObject authorization = (JSONObject)token.get("authorization");
            JSONArray scopes = (JSONArray)authorization.get("scopes");
            
            boolean allowCommercials = scopes.contains("channel_commercial");
            boolean allowEditor = scopes.contains("channel_editor");
            boolean chatAccess = scopes.contains("chat_login");
            boolean userAccess = scopes.contains("user_read");
            boolean readSubscriptions = scopes.contains("channel_subscriptions");
            boolean userEditFollows = scopes.contains("user_follows_edit");
            
            return new TokenInfo(username, id, chatAccess, allowEditor, allowCommercials, userAccess, readSubscriptions, userEditFollows);
        }
        catch (Exception e) {
            return null;
        }
    }

}
