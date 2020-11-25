package advisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class User {
    // developer.spotify.com -> dashboard
    final private String CLIENT_ID = "";
    final private String CLIENT_SECRET = "";
    private String spotifyCode;
    private boolean authorized = false;
    private String accessToken;

    public String getClientId() {
        return CLIENT_ID;
    }

    public String getClientSecret() {
        return CLIENT_SECRET;
    }

    public String getSpotifyCode() {
        return spotifyCode;
    }

    public void setSpotifyCode(String spotifyCode) {
        this.spotifyCode = spotifyCode;
        this.authorized = true;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public String getAccessToken(SpotifyUtils spotifyUtils) {
        if (this.accessToken != null && !this.accessToken.isEmpty()) {
            return this.accessToken;
        }

//        System.out.println("Basic " + Base64.getEncoder().encodeToString((getClientId() + ":" + getClientSecret()).getBytes()));
//        System.out.println("spotifyCode = " + spotifyCode);
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                //                .header("Authentication", "Basic " + Base64.getEncoder().encodeToString(
                //                        (getClientId() + ":" + getClientSecret()).getBytes()))
                .uri(URI.create(spotifyUtils.getAccessServer() + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "client_id=" + getClientId() +
                                "&client_secret=" + getClientSecret() +
                                "&grant_type=authorization_code" +
                                "&code=" + getSpotifyCode() +
                                "&redirect_uri=" + spotifyUtils.getREDIRECT_URI() + "&response_type=code"))
                .build();
        HttpClient client = HttpClient.newBuilder().build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        //{"access_token":"BQD5s_1VV2w7fXykTzQowZd0Bf7TNUxrzpCJggq0Fpvdyhq8aIS5yfshI2JOt9D75n2H0SzHLNGePe2AOliq6sVFvnCvFEqBKq5YiB3tYwMxFHe-Dda2ZyQRrMk5wgfTnQFwH8UhkjXXU1vUGxNqCw2unkpNrfNuv4D_Ug",
        // "token_type":"Bearer",
        // "expires_in":3600,
        // "refresh_token":"AQCl28_TISgeE0sV5wxJGaBQQJYkoG1zkL7vbeXutsHBnMAT_caz-C5pCPUqccCHhg6Ja_TbsWbJX-uN5fVt48gCZY2dVEXujXNOFok4yyHlU84aCtL1x_PH8BhF8Jps-rY",
        // "scope":""
        // }
        String json = response.body();
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        this.accessToken = jo.get("access_token").getAsString();
        return this.accessToken;
    }
}
