package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Person;
import service.MyLogger;

import java.sql.*;

public class DbConnectivityClass {

    final static String DB_NAME = "persondb";

    final static String DB_URL =
            "jdbc:mysql://skyl4.mysql.database.azure.com:3306/" + DB_NAME +
                    "?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC";

    final static String USERNAME = "pined20";
    final static String PASSWORD = "Skyluvsme24";

    MyLogger lg = new MyLogger();

    private final ObservableList<Person> data = FXCollections.observableArrayList();

    public ObservableList<Person> getData() {
        data.clear();
        connectToDatabase();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String sql = "SELECT * FROM users";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                lg.makeLog("No data");
            }

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");

                data.add(new Person(id, first_name, last_name, department, major, email, imageURL));
            }

            resultSet.close();
            preparedStatement.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public boolean connectToDatabase() {
        boolean hasRegisteredUsers = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "first_name VARCHAR(200) NOT NULL," +
                    "last_name VARCHAR(200) NOT NULL," +
                    "department VARCHAR(200)," +
                    "major VARCHAR(200)," +
                    "email VARCHAR(200) NOT NULL UNIQUE," +
                    "imageURL VARCHAR(500))";

            statement.executeUpdate(sql);

            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

            if (resultSet.next()) {
                int numUsers = resultSet.getInt(1);
                if (numUsers > 0) {
                    hasRegisteredUsers = true;
                }
            }

            resultSet.close();
            statement.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasRegisteredUsers;
    }

    public void insertUser(Person person) {
        connectToDatabase();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, person.getFirstName());
            preparedStatement.setString(2, person.getLastName());
            preparedStatement.setString(3, person.getDepartment());
            preparedStatement.setString(4, person.getMajor());
            preparedStatement.setString(5, person.getEmail());
            preparedStatement.setString(6, person.getImageURL());

            int row = preparedStatement.executeUpdate();

            if (row > 0) {
                lg.makeLog("A new user was inserted successfully.");
            }

            preparedStatement.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void editUser(int id, Person p) {
        connectToDatabase();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String sql = "UPDATE users SET first_name=?, last_name=?, department=?, major=?, email=?, imageURL=? WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setString(1, p.getFirstName());
            preparedStatement.setString(2, p.getLastName());
            preparedStatement.setString(3, p.getDepartment());
            preparedStatement.setString(4, p.getMajor());
            preparedStatement.setString(5, p.getEmail());
            preparedStatement.setString(6, p.getImageURL());
            preparedStatement.setInt(7, id);

            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteRecord(Person person) {
        int id = person.getId();
        connectToDatabase();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();
            preparedStatement.close();
            conn.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int retrieveId(Person p) {
        connectToDatabase();
        int id = -1;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);

            String sql = "SELECT id FROM users WHERE email=?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, p.getEmail());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                id = resultSet.getInt("id");
            }

            resultSet.close();
            preparedStatement.close();
            conn.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        lg.makeLog(String.valueOf(id));
        return id;
    }
}