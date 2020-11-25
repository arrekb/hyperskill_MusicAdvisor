package advisor;

public interface SpotifyData {

}

class Category implements SpotifyData {

    private String id;
    private String name;

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s", name);
    }
}

class NewRelease implements SpotifyData {
    //albums with artists and links on Spotify
    private String album;
    private String artists;
    private String link;

    public NewRelease(String album, String artists, String link) {
        this.album = album;
        this.artists = artists;
        this.link = link;
    }

    @Override
    public String toString() {
        return String.format("%s\n[%s]\n%s\n", album, artists, link);
    }
}

class Playlist implements SpotifyData {
    private String name;
    private String link;

    public Playlist(String name, String link) {
        this.name = name;
        this.link = link;
    }

    @Override
    public String toString() {
        return String.format("%s\n%s\n", name, link);
    }
}