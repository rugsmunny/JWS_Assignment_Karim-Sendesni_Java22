import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Main {
    static boolean serverRunning = true;
    static File jsonDb = new File("src/jsonDb.json");

    public static void main(String[] args) {

        System.out.println("- Server är nu Redo -\n");
        runLocalClientServerPort();

    }

    private static void runLocalClientServerPort() {

        while (serverRunning) {
            try (ServerSocket serverSocket = new ServerSocket(4321);
                 Socket socket = serverSocket.accept()) {

                // Initiera input och output stream över socket
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

                // Casta Objekt till JSON objektet 'jsonRequest'
                JSONObject jsonRequest = (JSONObject) inputStream.readObject();

                // Hantera begäran, skapa ett JSON-svar och
                // skicka response tillbaka till klienten
                outputStream.writeObject(handleRequest(jsonRequest));
                outputStream.flush();

            } catch (ClassNotFoundException | ParseException | IOException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("- Request avslutad -\n");
            }
        }
    }

    private static JSONObject handleRequest(JSONObject jsonRequest) throws IOException, ParseException {

        System.out.println("- Req mottagen och hanteras -\n");

        String method = jsonRequest.get("HTTPMethod").toString();
        String[] path = jsonRequest.get("URLParameter").toString().split("/");

        JSONObject requestBody = (JSONObject) jsonRequest.get("Body");

        JSONObject jsonResponse = new JSONObject();

        switch (method) {

            case "GET":
                String movieList = getFilms(path);
                if (!movieList.equals("")) {

                    jsonResponse.put("status", "200 OK");
                    jsonResponse.put("body", movieList);

                } else {

                    jsonResponse.put("status", "404 Not Found");
                    jsonResponse.put("body", "");
                }
                break;

            case "POST":

                addMovie(requestBody);

                jsonResponse.put("status", "200 OK");
                jsonResponse.put("contentType", "text/plain");
                jsonResponse.put("body", "Movie added.");

                break;

            case "QUIT":

                jsonResponse.put("status", "200 OK");
                jsonResponse.put("contentType", "text/plain");
                jsonResponse.put("body", "Server shutting down.");
                serverRunning = false;

                break;

            default:

                jsonResponse.put("status", "400 Bad Request");
                jsonResponse.put("body", "");

        }

        return jsonResponse;
    }

    public static String getFilms(String[] path) throws IOException, ParseException {

        StringBuilder searchResult = new StringBuilder();
        JSONParser parser = new JSONParser();
        JSONObject films = (JSONObject) parser.parse(new FileReader(jsonDb));

        assert films != null;

        ArrayList genres = new ArrayList<>(films.keySet());
        Collections.sort(genres);


        switch (path[1]) {
            case "listGenres": { //Tar fram alla tillgängliga kategorier och presenterar för klienten
                int i = 1;
                for (Object genre : genres) {
                    searchResult.append(i + ". " + genre + "\n");
                    i++;
                }
                break;
            }
            case "all": { //Presenterar alla filmer för klienten
                for (int i = 0; i < genres.size(); i++) {

                    JSONArray allMovies = (JSONArray) films.get(genres.get(i));
                    List<JSONObject> filmList = new ArrayList<>();
                    for (int j = 0; j < allMovies.size(); j++) {
                        filmList.add((JSONObject) allMovies.get(j));
                    }
                    searchResult.append("\n");
                    for (JSONObject film : filmList) {
                        String titel = film.get("title").toString();
                        String längd = film.get("length").toString();
                        searchResult.append(titel).append(" - ").append(längd).append(" min (" + genres.get(i) + ")\n");
                    }
                }
                break;
            }
            case "genre": { //Presenterar alla filmer inom en genre för klienten
                JSONArray movieGenre = (JSONArray) films.get(genres.get(Integer.parseInt(path[2]) - 1));
                searchResult.append(String.valueOf(genres.get(Integer.parseInt(path[2]) - 1)).toUpperCase() + ":\n\n");
                for (int i = 0; i < movieGenre.size(); i++) {
                    JSONObject movie = (JSONObject) movieGenre.get(i);
                    String titel = movie.get("title").toString();
                    String längd = movie.get("length").toString();
                    searchResult.append(titel).append(" - ").append(längd).append(" min\n");

                }
                break;
            }
        }

        return searchResult.toString();

    }


    private static void addMovie(JSONObject requestBody) throws IOException, ParseException {

        JSONParser parser = new JSONParser();
        JSONObject jsonTempDbObj;
        JSONObject movieDetails = new JSONObject();

        JSONObject films = (JSONObject) parser.parse(new FileReader(jsonDb));

        assert films != null;

        ArrayList genres = new ArrayList<>(films.keySet());
        Collections.sort(genres);

        movieDetails.put("title", fixTitle((String) requestBody.get("title")));
        movieDetails.put("length", requestBody.get("length").toString());
        String genre = requestBody.get("genre").toString().toLowerCase();

        //Client kan lägga till film i genre som redan finns via nummer val alt. skriva in ny kategori
        //Därav digit test nedan
        boolean isSingleDigit = genre.matches("\\d");

        if (isSingleDigit) { //Om true sätt genre till den genre numret representerar
            genre = (String) genres.get(Integer.parseInt(genre) - 1);
        }
        try {
            jsonTempDbObj = (JSONObject) parser.parse(new FileReader(jsonDb));

            assert jsonTempDbObj != null;
            if (jsonTempDbObj.containsKey(genre)) {
                JSONArray addNewMovieToGenre = (JSONArray) jsonTempDbObj.get(genre);

                addNewMovieToGenre.add(movieDetails);

            } else {
                JSONArray addNewGenre = new JSONArray();
                addNewGenre.add(movieDetails);
                jsonTempDbObj.put(genre, addNewGenre);
            }

            try (FileWriter fileWriter = new FileWriter(jsonDb)) {
                fileWriter.write(jsonTempDbObj.toJSONString());
                fileWriter.flush();
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }


    }

    private static String fixTitle(String title) {

        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < title.length(); i++) {

            if (i == 0 || (i > 1 && (' ' == title.charAt(i - 1)))) {

                strBuilder.append(Character.toUpperCase(title.charAt(i)));

            } else strBuilder.append(Character.toLowerCase(title.charAt(i)));

        }
        return strBuilder.toString();
    }

}
