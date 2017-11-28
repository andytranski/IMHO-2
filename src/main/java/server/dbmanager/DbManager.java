package server.dbmanager;

import server.models.*;
import server.utility.Crypter;
import server.utility.Globals;

import java.sql.*;
import java.util.ArrayList;

public class DbManager {

    // Creating the connection for the database
    private static final String URL = "jdbc:mysql://"
            + Globals.config.getDatabaseHost() + ":"
            + Globals.config.getDatabasePort() + "/"
            + Globals.config.getDatabaseName() + "?useSSL=false&serverTimezone=GMT";
    private static final String USERNAME = Globals.config.getDatabaseUsername();
    private static final String PASSWORD = Globals.config.getDatabasePassword();
    private static Connection connection = null;

    Crypter crypter = new Crypter();

    public DbManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    //Method for closing the connection
    private void close() {
        try {
            connection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

    }

    //Method for authorizing the user. Prepared statement are used, and a user object is returned.
    public User authorizeUser(String username, String password) throws IllegalArgumentException {
        //ResultSet and User to temporary contain values from SQL statement
        ResultSet resultSet = null;
        User user = null;

        //Try-catch method to avoid the program crashing on exceptions
        try {
            //SQL statement
            PreparedStatement authorizeUser = connection
                    .prepareStatement("SELECT * from User where username = ? AND password = ?");
            //Setting parameters for user object
            authorizeUser.setString(1, username);
            authorizeUser.setString(2, password);

            //ResultSet consisting parameters from SQL statement
            resultSet = authorizeUser.executeQuery();

            //Method will run as long as there is content in the next line of the resultSet
            while (resultSet.next()) {
                user = new User();
                user.setUserId(resultSet.getInt("user_id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setType(resultSet.getInt("type"));
                user.setTimeCreated(resultSet.getInt("time_created"));

            }
            //Exception to avoid crashing
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                //closing the resultSet, because its a temporary table of content
                resultSet.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
                close();
            }
        }
        return user;
    }

    // Method for creating a user - Boolean returned, which decides if the user is created or not
    public User createUser(User user) throws IllegalArgumentException {
        //Try-catch method to avoid the program crashing on exceptions
        try {
            PreparedStatement createUser = connection.prepareStatement("INSERT INTO User (username, password, time_created) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
            createUser.setString(1, user.getUsername());
            createUser.setString(2, user.getPassword());
            createUser.setLong(3, user.getTimeCreated());

            //rowsAffected
            int rowsAffected = createUser.executeUpdate();
            if (rowsAffected == 1) {
                ResultSet rs = createUser.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    int autoIncrementedUserId = rs.getInt(1);
                    user.setUserId(autoIncrementedUserId);
                } else {
                    user = null;
                }
                user.setType(2);
                return user;
            }
            //Exception to avoid crashing
        } catch (SQLException exception) {
            exception.printStackTrace();
            close();
        }
        return null;
    }

    // Method for creating a quiz
    public Quiz createQuiz(Quiz quiz) throws IllegalArgumentException {
        //Try-catch
        try {
            //SQL statement to create a quiz
            PreparedStatement createQuiz = connection
                    .prepareStatement("INSERT INTO Quiz (created_by, question_count, quiz_title, quiz_description, course_id) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            //Setting parameters for quiz object
            createQuiz.setString(1, quiz.getCreatedBy());
            createQuiz.setInt(2, quiz.getQuestionCount());
            createQuiz.setString(3, quiz.getQuizTitle());
            createQuiz.setString(4, quiz.getQuizDescription());
            createQuiz.setInt(5, quiz.getCourseId());

            int rowsAffected = createQuiz.executeUpdate();
            if (rowsAffected == 1) {
                ResultSet rs = createQuiz.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    int autoIncrementedQuizId = rs.getInt(1);
                    quiz.setQuizId(autoIncrementedQuizId);
                } else {
                    quiz = null;
                }
                return quiz;
            }

            //Exception to avoid crashing
        } catch (SQLException exception) {
            exception.printStackTrace();
            close();
        }
        return null;
    }

    public boolean updateQuestionCount(int quizId, int questionCount) throws IllegalArgumentException {
        try {
            PreparedStatement updateQuiz = connection.prepareStatement("UPDATE Quiz SET question_count = ? WHERE quiz_id = ?");
            updateQuiz.setInt(1, questionCount);
            updateQuiz.setInt(2, quizId);

            int rowsAffected = updateQuiz.executeUpdate();

            if (rowsAffected == 1) {
                return true;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }


    // Method for creating a question
    public Question createQuestion(Question question) throws IllegalArgumentException {
        //Try-catch
        try {
            //SQL statement
            PreparedStatement createQuestion = connection
                    .prepareStatement("INSERT INTO Question (question, quiz_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            //Setting parameters
            createQuestion.setString(1, question.getQuestion());
            createQuestion.setInt(2, question.getQuestionToQuizId());

            int rowsAffected = createQuestion.executeUpdate();
            if (rowsAffected == 1) {
                ResultSet rs = createQuestion.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    int autoIncrementedQuestionId = rs.getInt(1);
                    question.setQuestionId(autoIncrementedQuestionId);
                } else {
                    question = null;
                }
                return question;
            }
            //Exception to avoid crashing
        } catch (SQLException exception) {
            exception.printStackTrace();
            close();
        }
        return null;
    }

    /* Method for creating a option */
    public Option createOption(Option option) throws IllegalArgumentException {
        //Try-catch
        try {
            //SQL statement
            PreparedStatement createOption = connection
                    .prepareStatement("INSERT INTO `Option` (`option`, question_id, is_correct) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
            //Setting parameters for user object
            createOption.setString(1, option.getOption());
            createOption.setInt(2, option.getOptionToQuestionId());
            createOption.setInt(3, option.getIsCorrect());

            int rowsAffected = createOption.executeUpdate();
            if (rowsAffected == 1) {
                ResultSet rs = createOption.getGeneratedKeys();
                if (rs != null && rs.next()) {
                    int autoIncrementedOptionId = rs.getInt(1);
                    option.setOptionId(autoIncrementedOptionId);
                } else {
                    option = null;
                }
                return option;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            close();
        }
        return null;
    }

    // Method for loading courses
    public ArrayList<Course> loadCourses() {   //loadCourses
        ResultSet resultSet = null;
        ArrayList<Course> courses = new ArrayList<Course>();

        //Try-catch
        try {
            //SQL statement
            PreparedStatement loadCourse = connection.prepareStatement("SELECT * FROM Course");
            resultSet = loadCourse.executeQuery();
            while (resultSet.next()) {
                Course course = new Course();
                course.setCourseId(resultSet.getInt("course_id"));
                course.setCourseTitle(resultSet.getString("course_title"));
                courses.add(course);
            }
            //Exception to avoid crashing
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                //closing the resultSet, because its a temporary table of content
                resultSet.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
                close();
            }
        }
        return courses;
    }

    /* Method for seeing available quizzes within a chosen course */
    public ArrayList<Quiz> loadQuizzes(int courseId) {
        ResultSet resultSet = null;
        ArrayList<Quiz> quizzes = new ArrayList<Quiz>();

        //Try-catch
        try {
            //SQL statement
            PreparedStatement loadQuizzes = connection.prepareStatement("SELECT * FROM Quiz WHERE course_id = ?");
            loadQuizzes.setInt(1, courseId);

            //resultSet gets the value of the SQL statement
            resultSet = loadQuizzes.executeQuery();

            //Method will run as long as there is content in the next line of the resultset
            while (resultSet.next()) {
                //Adding values in ResultSet to quiz object
                Quiz quiz = new Quiz();
                quiz.setQuizId(resultSet.getInt("quiz_id"));
                quiz.setCreatedBy(resultSet.getString("created_by"));
                quiz.setQuestionCount(resultSet.getInt("question_count"));
                quiz.setQuizDescription(resultSet.getString("quiz_description"));
                quiz.setQuizTitle(resultSet.getString("quiz_title"));
                quiz.setCourseId(resultSet.getInt("course_id"));
                quizzes.add(quiz);
            }
            //Exception to avoid crashing
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                //closing the resultSet, because its a temporary table of content
                resultSet.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
                close();
            }
        }
        return quizzes;
    }

    //Method for loading questions
    public ArrayList<Question> loadQuestions(int quizId) {
        //Resultset to temporary contain values from SQL statement, first given the value of null
        ResultSet resultSet = null;
        //Arraylist of question object
        ArrayList<Question> questions = new ArrayList<>();

        ///Try-catch
        try {
            //SQL query sent to SQL to get values from the database. Done thoug the connection method - see top of class.
            PreparedStatement loadQuestions = connection
                    .prepareStatement("SELECT * FROM Question WHERE quiz_id = ?");
            //Gives the SQL statement value to parameter - the quizid integer
            loadQuestions.setInt(1, quizId);
            //Adds values from SQL statement to resultset (temporarty table)
            resultSet = loadQuestions.executeQuery();

            //Method will run as long as there is content in the next line of the resultset
            while (resultSet.next()) {
                //Creating question object
                Question question = new Question();
                //Adding values to parameter variables in the question object
                question.setQuestion(resultSet.getString("question"));
                question.setQuestionId(resultSet.getInt("question_id"));
                question.setQuestionToQuizId(resultSet.getInt("quiz_id"));
                //Adding the questoin object to the arraylist of question objects
                questions.add(question);
            }
            //Question to avoid crashes on exceptions
        } catch (SQLException exception) {
            exception.printStackTrace();
            //Always close the resultset as it is a temporary table of content
        } finally {
            try {
                resultSet.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
                close();
            }
        }
        // Retuning the ArrayList of question objects found in database with given quiz id
        return questions;
    }

    //Method for loading options to a given question
    public ArrayList<Option> loadOptions(int questionId) {
        //Resultset to temporary contain values from SQL statement
        ResultSet resultSet = null;
        //Arraylist of Option object
        ArrayList<Option> options = new ArrayList<Option>();

        //Try-catch
        try {
            //SQL statement
            PreparedStatement loadQuestions = connection
                    .prepareStatement("SELECT * FROM `Option` WHERE question_id = ?");
            loadQuestions.setInt(1, questionId);
            //Resultset gets the value of the SQL statement
            resultSet = loadQuestions.executeQuery();

            //Method will run as long as there is content in the next line of the resultset
            while (resultSet.next()) {
                //Adding values in resultset to option objet.
                Option option = new Option();
                option.setOptionId(resultSet.getInt("option_id"));
                option.setOptionToQuestionId(resultSet.getInt("question_id"));
                option.setIsCorrect(resultSet.getInt("is_correct"));
                option.setOption(resultSet.getString("option"));
                //Adding option object with given parameter values to the arraylist of several option objects.
                options.add(option);
            }
            //Catch to avoid crashing on exceptions
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        //Always close the resultSet as it is a temporary table of content
        finally {
            try {
                resultSet.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
                close();
            }
        }
        //Returning the arraylist of option objects
        return options;
    }


    public void createToken(String token, int userId) {
        try {
            PreparedStatement addTokenStatement = connection.prepareStatement("INSERT INTO Tokens (token, token_user_id) VALUES (?,?)");
            addTokenStatement.setString(1, token);
            addTokenStatement.setInt(2, userId);
            addTokenStatement.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
            close();
        }
    }

    public boolean deleteToken(int userId) throws SQLException {
        try {
            PreparedStatement deleteTokenStatement = connection.prepareStatement("DELETE FROM Tokens WHERE token_user_id = ?");
            deleteTokenStatement.setInt(1, userId);

            int rowsAffected = deleteTokenStatement.executeUpdate();
            if (rowsAffected == 1) {
                return true;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            close();
        }
        return false;
    }

    public User getUserFromToken(String token) throws SQLException {
        ResultSet resultSet = null;
        User userFromToken = null;

        try {
            PreparedStatement getUserFromToken = connection
                    .prepareStatement("SELECT username, user_id, `type`, time_created FROM `User` u INNER JOIN Tokens t ON t.token_user_id = u.user_id WHERE t.token = ?");

            getUserFromToken.setString(1, token);
            resultSet = getUserFromToken.executeQuery();

            while (resultSet.next()) {
                userFromToken = new User();
                userFromToken.setUserId(resultSet.getInt("user_id"));
                userFromToken.setUsername(resultSet.getString("username"));
                userFromToken.setType(resultSet.getInt("type"));
                userFromToken.setTimeCreated(resultSet.getInt("time_created"));

            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
                close();
            }
        }
        return userFromToken;

    }

    public User getTimeCreatedByUsername(String username) {
        User user = null;
        ResultSet resultSet = null;

        try {
            //SQL statement
            PreparedStatement getTimeCreatedByUsername = connection.prepareStatement("SELECT * FROM User WHERE username = ?");
            getTimeCreatedByUsername.setString(1, username);
            resultSet = getTimeCreatedByUsername.executeQuery();
            while (resultSet.next()) {
                user = new User();
                user.setTimeCreated(resultSet.getLong("time_created"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
                close();
            }
        }
        return user;
    }

    //Method for deleting a quiz and all it's sub-tables
    public boolean deleteQuiz(int quizId) throws IllegalArgumentException {
        //Try-catch
        try {
            //SQL statement that deletes a quiz and all it's sub-tables
            PreparedStatement deleteQuiz = connection
                    .prepareStatement("DELETE FROM Quiz WHERE quiz_id = ?");
            deleteQuiz.setInt(1, quizId);
            int rowsAffected = deleteQuiz.executeUpdate();
            if (rowsAffected == 1) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            close();
        }
        return false;
    }

    //Method to get the number of correct answers of a quiz using quiz ID og user ID as parameters
    public int getCorrectAnswersCount(int quizId, int userId) {
        ResultSet resultSet = null;
        int correctAnswersCount = 0;
        //Try-catch
        try {
            //SQL statement that gets all the correct answers the user have on a quiz
            //In the SELECT part we define that we need the DB to return quiz description and count. we use count
            PreparedStatement getCorrectAnswersCount = connection.prepareStatement("SELECT q.quiz_description, count(*)\n" +
                    "FROM user u\n" + // we define that it is from user, her after known as u.
                    "INNER JOIN answer a\n" + // we inner join the statement with answer. now know as a.
                    "ON u.user_id = a.user_id\n" + // and that is should do it where user id from the user table = user id from answer table
                    "INNER JOIN `option` o\n" + // the statement is continued with option o.
                    "ON a.option_id = o.option_id\n" +//where a option id = o option id.
                    "INNER JOIN question qt\n" + // same as before with question qt.
                    "ON o.question_id = qt.question_id\n" + // where o question id = qt question id.
                    "INNER JOIN quiz q\n" + // finally inner join with quiz q.
                    "ON qt.quiz_id = q.quiz_id\n" + // on qt quiz id = q id quiz
                    "WHERE quiz_id = ? \n" + // where quiz id = ?. ? is defined by the user.
                    "\tAND user_id = ?\n" + // and user id = + ?. also defined by the user.
                    "GROUP BY o.is_correct" // in the end it is sorted by o is correct. so the final count is returned.
            );

            getCorrectAnswersCount.setInt(1, quizId);
            getCorrectAnswersCount.setInt(2, userId);

            resultSet = getCorrectAnswersCount.executeQuery();


            while (resultSet.next()) {
                //gets the count of the total correct answers and writes it to score.
                correctAnswersCount = (resultSet.getInt("count(*)"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                close();
            }
        }
        return correctAnswersCount;
    }

    //Method gets the number of questions using quiz ID as a parameter
    public int getQuestionCount(int quizId) {
        ResultSet resultSet = null;
        int questionCount = 0;
        //Try-catch
        try {
            //SQL statement that gets all the questions on a quiz
            PreparedStatement getQuestionCount = connection.prepareStatement("SELECT q.quiz_id, count(*)\n" +
                    "FROM quiz q\n" +
                    "INNER JOIN question qt\n" +
                    "ON q.quiz_id = qt.quiz_id\n" +
                    "WHERE quiz_id = ?\n" +
                    "GROUP BY quiz_id");

            getQuestionCount.setInt(1, quizId);
            resultSet = getQuestionCount.executeQuery();

            while (resultSet.next()) {
                //gets the count of the total questions and writes it to nrQuestion.
                questionCount = (resultSet.getInt("count(*)"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                close();
            }
        }
        return questionCount;
    }

    public boolean deleteAnswer(int userId) throws IllegalArgumentException {
        try {
            PreparedStatement deleteAnswer = connection
                    .prepareStatement("DELETE FROM Answer WHERE user_id = ?");

            deleteAnswer.setInt(1, userId);
            int rowsAffected = deleteAnswer.executeUpdate();
            if (rowsAffected == 1) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            close();
        }
        return false;
    }
}