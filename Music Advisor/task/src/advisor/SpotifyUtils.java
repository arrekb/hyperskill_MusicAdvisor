package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SpotifyUtils {
    private String accessServer = "https://accounts.spotify.com";
    private String apiServerPath = "https://api.spotify.com";
    private int entriesOnPage = 5;
    private String REDIRECT_URI = "http://localhost:8080";
    private List<SpotifyData> spotifyDataList = new ArrayList<>();

    public SpotifyUtils(String[] args) {
        //-access argument should provide authorization server path. The default value should be https://accounts.spotify.com
        //-resource argument should provide API server path. The default value should be https://api.spotify.com
        for (int ii = 0; ii < args.length; ii += 2) {
            switch (args[ii]) {
                case "-access":
                    accessServer = args[ii + 1];
                    break;
                case "-resource":
                    apiServerPath = args[ii + 1];
                    break;
                case "-page":
                    entriesOnPage = Integer.parseInt(args[ii + 1]);
                    break;
                default:
                    System.out.println("Ignoring unknown option: " + args[ii]);
                    break;
            }
        }
    }

    public int getEntriesOnPage() {
        return entriesOnPage;
    }

    public List<SpotifyData> getSpotifyDataList() {
        return spotifyDataList;
    }

    public String getAccessServer() {
        return accessServer;
    }

    public String getREDIRECT_URI() {
        return REDIRECT_URI;
    }

    public String newApiRequest(User user, String apiPath) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + user.getAccessToken(this))
                .uri(URI.create(apiServerPath + apiPath))
                .GET()
                .build();

        HttpClient client = HttpClient.newBuilder().build();
        HttpResponse<String> response = null;
        try {
            response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        //        System.out.println(response.body());
        if (response.statusCode() != 200) {
            String message = parseErrorMessage(response.body());
            System.out.println("message = " + message);
            return "";
        }
        return response.body();
    }

    private String parseErrorMessage(String json) {
        if (!json.contains("message")) {
            return "No message in error response";
        }
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        String message = jo.get("error").getAsJsonObject().get("message").getAsString();
        return message;
    }

    public void initializeFeatured(User user) {
        loadPlaylists(user, "/v1/browse/featured-playlists");
    }

    public void initializeNewAlbums(User user) {
        loadNewReleases(user);
    }

    public void initializeCategories(User user) {
        loadCategories(user);
    }

    public void initializeCategoryPlaylists(User user, String categoryName) {
        String categoryId = getCategoryId(user, categoryName);
        if (categoryId.isEmpty()) {
            System.out.println("Unknown category name.");
            return;
        }
        String url = String.format("/v1/browse/categories/%s/playlists", categoryId);
        String json = newApiRequest(user, url);
        loadPlaylists(user, url);
    }


    private void loadNewReleases(User user) {

        this.spotifyDataList.clear();
        String json = newApiRequest(user, "/v1/browse/new-releases");
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();

        JsonObject albums = jo.get("albums").getAsJsonObject();
        for (JsonElement item : albums.getAsJsonArray("items")) {
            String albumName = item.getAsJsonObject().get("name").getAsString();
            String albumLink = item.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString();
            JsonArray artists = item.getAsJsonObject().get("artists").getAsJsonArray();
            StringBuilder sbArtists = new StringBuilder("");
            for (JsonElement artist : artists) {
                sbArtists.append(", " + artist.getAsJsonObject().get("name").getAsString());
            }
            spotifyDataList.add(new NewRelease(albumName, sbArtists.toString().substring(2), albumLink));
            //            System.out.println(new NewRelease(albumName, sbArtists.toString().substring(2), albumLink).toString());
        }
    }

    private void loadCategories(User user) {
        String json = newApiRequest(user, "/v1/browse/categories");
        this.spotifyDataList.clear();
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject categories = jo.get("categories").getAsJsonObject();

        for (JsonElement item : categories.getAsJsonArray("items")) {
            String id = item.getAsJsonObject().get("id").getAsString();
            String name = item.getAsJsonObject().get("name").getAsString();
            Category category = new Category(id, name);
            this.spotifyDataList.add(category);
        }
    }

    private void loadPlaylists(User user, String apiPath) {
        this.spotifyDataList.clear();
        String json = newApiRequest(user, apiPath);

        try {
            JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
            JsonObject featured = jo.get("playlists").getAsJsonObject();
            for (JsonElement item : featured.getAsJsonArray("items")) {
                String name = item.getAsJsonObject().get("name").getAsString();
                String link = item.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString();
                spotifyDataList.add(new Playlist(name, link));
            }
        } catch (NullPointerException e) {
            System.out.println(parseErrorMessage(json));
            //            e.printStackTrace();
        }
    }


    private String getCategoryId(User user, String categoryName) {
        if (spotifyDataList.size() == 0) {
            loadCategories(user);
        }
        for (SpotifyData spotifyData : spotifyDataList) {
            Category category = (Category) spotifyData;
            if (category.getName().equals(categoryName)) {
                return category.getId();
            }
        }
        return "";
    }

}

