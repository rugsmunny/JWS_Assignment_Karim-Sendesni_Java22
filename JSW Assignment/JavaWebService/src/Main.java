import org.json.simple.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    static Socket socket = null;


    public static void main(String[] args) throws IOException {
        runClient();
    }

    private static void runClient() throws IOException {
        Scanner userInput = new Scanner(System.in);

        boolean run = true;
        while (run) {
            System.out.print("Movie DB Menu\n\n1. List movies\n2. Add movie\n3. Shut down\n\nChoice: ");

            switch (userInput.nextLine()) {
                case "1":
                    handleListMoviesChoice(userInput);
                    break;
                case "2":
                    handleAddMovieChoice(userInput);
                    break;
                case "3":
                    handleShutDownChoice();
                    run = false;
                    break;
                default:
                    System.out.println("Something went wrong, please try again.");
            }
        }
    }

    static void handleListMoviesChoice(Scanner userInput) throws IOException {
        System.out.println("1. All movies\n2. Select Category\n");
        switch (userInput.nextLine()) {
            case "1":
                sendClientRequest("GET", "/all", new JSONObject());
                break;
            case "2": {
                sendClientRequest("GET", "/listGenres", new JSONObject());
                sendClientRequest("GET", "/genre/" + userInput.nextLine(), new JSONObject());
            }
            break;
        }
        disconnectServer();
    }

    static void handleAddMovieChoice(Scanner userInput) throws IOException {
        sendClientRequest("POST", "/addMovie", setNewMovie(userInput));
        disconnectServer();
    }

    private static void handleShutDownChoice() throws IOException {
        sendClientRequest("QUIT", "/", new JSONObject());
        disconnectServer();
        System.out.println("Thank you, come again!");
    }
    static JSONObject setNewMovie(Scanner userInput) throws IOException {
        JSONObject movieDetails = new JSONObject();

        sendClientRequest("GET", "/listGenres", new JSONObject());
        System.out.println("Choose a genre or type in a new one : ");
        movieDetails.put("genre", userInput.nextLine());
        System.out.println("Name of movie : ");
        movieDetails.put("title", userInput.nextLine());
        System.out.println("Movie length in minutes : ");
        movieDetails.put("length", userInput.nextLine());

        return movieDetails;
    }

    private static void sendClientRequest(String httpMethod, String path, JSONObject object) throws IOException {

        JSONObject jsonRequest = new JSONObject();

        // Skapa JSON-objekt med begäransinformation
        jsonRequest.put("HTTPMethod", httpMethod);
        jsonRequest.put("URLParameter", path);
        jsonRequest.put("ContentType", "application/json");
        jsonRequest.put("Body", object);

        try (Socket socket = new Socket("localhost", 4321);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {


            // Skicka JSON-objektet till servern
            outputStream.writeObject(jsonRequest);
            outputStream.flush();


            // Ta emot svaret från servern
            JSONObject jsonResponse = (JSONObject) inputStream.readObject();

            System.out.println("\n" + jsonResponse.get("body"));

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private static void disconnectServer() throws IOException {

        if (socket != null) socket.close();

        System.out.println("Request avslutad, kopplingar nedstängda\n");
    }
}
