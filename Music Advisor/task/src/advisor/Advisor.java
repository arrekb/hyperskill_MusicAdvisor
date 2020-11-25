package advisor;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Advisor {
    // controller
    User user;
    SpotifyUtils spotifyUtils;
    int currentPage;

    public Advisor(String[] args) {
        user = new User();
        spotifyUtils = new SpotifyUtils(args);
    }

    public void mainMenu() {
        Scanner scanner = new Scanner(System.in);
        String menuChoice;
        do {
            menuChoice = scanner.nextLine();
            if ("auth".equals(menuChoice)) {
                if (user.isAuthorized()) {
                    System.out.println("User already authorized.");
                }
                authorize();
            } else {
                if (!user.isAuthorized()) {
                    System.out.println("Please, provide access for application.");
                    continue;
                }
                if ("featured".equals(menuChoice)) {
                    spotifyUtils.initializeFeatured(user);
                    displayPage(1);
                } else if ("new".equals(menuChoice)) {
                    spotifyUtils.initializeNewAlbums(user);
                    displayPage(1);
                } else if ("categories".equals(menuChoice)) {
                    spotifyUtils.initializeCategories(user);
                    displayPage(1);
                } else if (menuChoice.startsWith("playlists")) {
                    String categoryName = menuChoice.substring("playlists".length()).strip();
                    spotifyUtils.initializeCategoryPlaylists(user, categoryName);
                    displayPage(1);
                } else if ("next".equals(menuChoice)) {
                    displayPage(currentPage + 1);
                } else if ("prev".equals(menuChoice)) {
                    displayPage(currentPage - 1);
                }
            }
        } while (!"exit".equals(menuChoice));
        System.out.println("---GOODBYE!---");
    }

    private void authorize() {

        //        HandlerData handlerData = new HandlerData();
        //        handlerData.setAccessServer(spotifySettings.getAccessServer());

        // On the auth command, before printing the auth link (from the previous stage),
        // you should start an HTTP server that will listen for the incoming requests.

        HttpServer server = null;
        try {
            server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //        handlerData.setServer(server);

        server.createContext("/", new RequestHandler(user));
        server.start();

        // This code is bound to each user who has a Spotify account and uses your app.
        // Actually, you should ask this code once for each new user and save it somewhere.


        String url = String.format(spotifyUtils.getAccessServer() + "/authorize?client_id=%s&" +
                "redirect_uri=" + spotifyUtils.getREDIRECT_URI() + "&response_type=code", user.getClientId());
        System.out.println(url);
        System.out.println("waiting for code...");

        do {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!user.isAuthorized());

        // After the code is received, the server must shut down
        server.stop(1);

        // and you should get access_token by making a POST request on https://accounts.spotify.com/api/token
        // with parameters described in the guide, and then print the response body.
        //        System.out.println(user.getAccessToken(spotifySettings));
    }

    private void displayPage(int requiredPage) {

        if (requiredPage < 1) {
            System.out.println("No more pages.");
            return;
        }

//        System.out.println("spotifyUtils.getEntriesOnPage() = " + spotifyUtils.getEntriesOnPage());
        int totalPages;
        if (spotifyUtils.getEntriesOnPage() == 1) {
            totalPages = spotifyUtils.getSpotifyDataList().size();
        } else {
            totalPages = spotifyUtils.getSpotifyDataList().size() / spotifyUtils.getEntriesOnPage();
            if (spotifyUtils.getSpotifyDataList().size() % spotifyUtils.getEntriesOnPage() > 0) {
                totalPages++;
            }
        }
        if (requiredPage > totalPages) {
            System.out.println("No more pages.");
            return;
        }

        int firstItemToDisplay = (requiredPage - 1) * spotifyUtils.getEntriesOnPage();


        for (int ii = firstItemToDisplay; ii < firstItemToDisplay + spotifyUtils.getEntriesOnPage(); ii++) {
            if (ii >= spotifyUtils.getSpotifyDataList().size()) {
                break;
            }
            System.out.println(spotifyUtils.getSpotifyDataList().get(ii).toString());
        }
        currentPage = requiredPage;
        System.out.printf("---PAGE %d OF %d---\n", currentPage, totalPages);
    }
}

