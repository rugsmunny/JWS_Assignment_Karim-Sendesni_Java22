import org.junit.Test;
import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;


public class MainTest {

    @Test
    public void testSetNewMovie() throws IOException {
        String testInput = "action\nTest Movie\n120\n";
        InputStream in = new ByteArrayInputStream(testInput.getBytes());
        System.setIn(in);

        Scanner userInput = new Scanner(System.in);
        JSONObject movieDetails = Main.setNewMovie(userInput);

        assertEquals("action", movieDetails.get("genre"));
        assertEquals("Test Movie", movieDetails.get("title"));
        assertEquals("120", movieDetails.get("length"));
    }

    @Test
    public void testListAllMovies() throws IOException {
        String testInput = "1\n";
        InputStream in = new ByteArrayInputStream(testInput.getBytes());
        System.setIn(in);
        Scanner userInput = new Scanner(System.in);

        Main.handleListMoviesChoice(userInput);
    }

    @Test
    public void testListMoviesByGenre() throws IOException {
        String testInput = "2\n2"; // val anime
        InputStream in = new ByteArrayInputStream(testInput.getBytes());
        System.setIn(in);
        Scanner userInput = new Scanner(System.in);

        Main.handleListMoviesChoice(userInput);
    }

    @Test
    public void testAddMovie() throws IOException {
        String testInput = "action\nTest Movie\n120\n";
        InputStream in = new ByteArrayInputStream(testInput.getBytes());
        System.setIn(in);
        Scanner userInput = new Scanner(System.in);

        Main.handleAddMovieChoice(userInput);
    }
}