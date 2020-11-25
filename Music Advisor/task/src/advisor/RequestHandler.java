package advisor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class RequestHandler implements HttpHandler {

    private final User user;

    public RequestHandler(User user) {
        this.user = user;
    }

    public void handle(HttpExchange exchange) throws IOException {
        // String query = exchange.getRequestURI().getQuery();
        String query = exchange.getRequestURI().toString();
//        System.out.println("query = " + query);
        // When the user confirms or rejects the authorization,
        // the server should return the following text to the browser:
        //"Got the code. Return back to your program." if the query contains the authorization code.
        //"Authorization code not found. Try again." otherwise.
        String msg;
        if (query != null && query.contains("code=")) {
            user.setSpotifyCode(query.split("=")[1]);
            System.out.println("Success!");
            msg = "Got the code. Return back to your program.";
        } else {
            msg = "Authorization code not found. Try again.";
        }
        exchange.sendResponseHeaders(200, msg.length());
        exchange.getResponseBody().write(msg.getBytes());
        exchange.getResponseBody().close();
    }
}
