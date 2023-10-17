import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;


public class JSONPlaceholderApiClient {

    public class Task {
        private int userId;
        private int id;
        private String title;
        private boolean completed;

        public int getUserId() {
            return userId;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public boolean isCompleted() {
            return completed;
        }
    }

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/users";

    public static void main(String[] args) {
        try {

            String newUserJson = "{\"name\":\"John Doe\",\"username\":\"johndoe\",\"email\":\"johndoe@example.com\"}";
            createUser(newUserJson);

            int userIdToUpdate = 1;
            String updatedUserJson = "{\"name\":\"Updated Name\",\"username\":\"updateduser\",\"email\":\"updateduser@example.com\"}";
            updateUser(userIdToUpdate, updatedUserJson);

            int userIdToDelete = 1;
            deleteUser(userIdToDelete);

            getAllUsers();


            int userIdToFetch = 2;
            getUserById(userIdToFetch);



            String usernameToFetch = "username";
            getUserByUsername(usernameToFetch);
            int userIdToFetch2 = 2;
            getUserById(userIdToFetch2);

            int postIdToFetch = getLastPostIdByUser(userIdToFetch2);
            if (postIdToFetch > 0) {
                getAndWriteCommentsForPost(userIdToFetch2, postIdToFetch);
            }
            int userIdForOpenTasks = 1;
            printOpenTasksForUser(userIdForOpenTasks);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void createUser(String newUserJson) throws IOException {
        HttpURLConnection connection = createConnection(BASE_URL, "POST");
        writeDataToConnection(connection, newUserJson);
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            System.out.println("User created successfully.");
        } else {
            System.out.println("Failed to create user. Response code: " + responseCode);
        }

        connection.disconnect();
    }

    private static void updateUser(int userId, String updatedUserJson) throws IOException {
        String updateUserUrl = BASE_URL + "/" + userId;
        HttpURLConnection connection = createConnection(updateUserUrl, "PUT");
        writeDataToConnection(connection, updatedUserJson);
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("User updated successfully.");
        } else {
            System.out.println("Failed to update user. Response code: " + responseCode);
        }

        connection.disconnect();
    }

    private static void deleteUser(int userId) throws IOException {
        String deleteUserUrl = BASE_URL + "/" + userId;
        HttpURLConnection connection = createConnection(deleteUserUrl, "DELETE");
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            System.out.println("User deleted successfully.");
        } else {
            System.out.println("Failed to delete user. Response code: " + responseCode);
        }

        connection.disconnect();
    }

    private static void getAllUsers() throws IOException {
        HttpURLConnection connection = createConnection(BASE_URL, "GET");
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(connection);
            System.out.println("All Users:\n" + response);
        } else {
            System.out.println("Failed to get all users. Response code: " + responseCode);
        }

        connection.disconnect();
    }

    private static void getUserById(int userId) throws IOException {
        String getUserUrl = BASE_URL + "/" + userId;
        HttpURLConnection connection = createConnection(getUserUrl, "GET");
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(connection);
            System.out.println("User by ID " + userId + ":\n" + response);
        } else {
            System.out.println("Failed to get user by ID. Response code: " + responseCode);
        }

        connection.disconnect();
    }

    private static void getUserByUsername(String username) throws IOException {
        String getUserByUsernameUrl = BASE_URL + "?username=" + username;
        HttpURLConnection connection = createConnection(getUserByUsernameUrl, "GET");
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(connection);
            System.out.println("User by Username " + username + ":\n" + response);
        } else {
            System.out.println("Failed to get user by username. Response code: " + responseCode);
        }

        connection.disconnect();
    }

    private static HttpURLConnection createConnection(String urlString, String requestMethod) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(requestMethod);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        return connection;
    }

    private static void writeDataToConnection(HttpURLConnection connection, String data) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = data.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    }

    private static String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    private static int getLastPostIdByUser(int userId) throws IOException {
        String userPostsUrl = BASE_URL + "/" + userId + "/posts";
        HttpURLConnection connection = createConnection(userPostsUrl, "GET");
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(connection);
            JsonArray userPosts = new Gson().fromJson(response, JsonArray.class);


            int lastPostId = 0;
            for (int i = 0; i < userPosts.size(); i++) {
                JsonObject post = userPosts.get(i).getAsJsonObject();
                int postId = post.get("id").getAsInt();
                if (postId > lastPostId) {
                    lastPostId = postId;
                }
            }

            connection.disconnect();
            return lastPostId;
        } else {
            System.out.println("Failed to get user posts. Response code: " + responseCode);
            connection.disconnect();
            return 0;
        }
    }
    private static void getAndWriteCommentsForPost(int userId, int postId) throws IOException {
        String postCommentsUrl = "https://jsonplaceholder.typicode.com/posts/" + postId + "/comments";
        HttpURLConnection connection = createConnection(postCommentsUrl, "GET");
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(connection);

            String fileName = "user-" + userId + "-post-" + postId + "-comments.json";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(response);
            }

            System.out.println("Comments for User " + userId + " - Post " + postId + " written to " + fileName);
        } else {
            System.out.println("Failed to get comments. Response code: " + responseCode);
        }

        connection.disconnect();
    }
    private static void printOpenTasksForUser(int userId) throws IOException {
        String userTasksUrl = BASE_URL + "/" + userId + "/todos";
        HttpURLConnection connection = createConnection(userTasksUrl, "GET");
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = readResponse(connection);
            JsonArray tasks = new Gson().fromJson(response, JsonArray.class);

            System.out.println("Open tasks for User " + userId + ":");
            for (int i = 0; i < tasks.size(); i++) {
                JsonObject task = tasks.get(i).getAsJsonObject();
                boolean completed = task.get("completed").getAsBoolean();
                if (!completed) {
                    int taskId = task.get("id").getAsInt();
                    String title = task.get("title").getAsString();
                    System.out.println("Task ID: " + taskId + ", Title: " + title);
                }
            }
        } else {
            System.out.println("Failed to get user tasks. Response code: " + responseCode);
        }

        connection.disconnect();
    }
}
