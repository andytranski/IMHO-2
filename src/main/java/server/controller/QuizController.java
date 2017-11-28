package server.controller;

import com.google.gson.Gson;
import server.dbmanager.DbManager;
import server.models.Course;
import server.models.Option;
import server.models.Question;
import server.models.Quiz;
import server.utility.Globals;

import java.util.ArrayList;

public class QuizController {


    public QuizController() {
    }

    //Method for loading courses
    public ArrayList<Course> loadCourses() {
        DbManager dbManager = new DbManager();
        ArrayList<Course> courses = dbManager.loadCourses();

        if(!courses.isEmpty()) {
            return courses;
        } else {
            return null;
        }

    }

    // Method for loading quizzes
    public ArrayList<Quiz> loadQuizzes(int courseId) {
        DbManager dbManager = new DbManager();
        ArrayList<Quiz> quizzes = dbManager.loadQuizzes(courseId);

        if(!quizzes.isEmpty()) {
            return quizzes;
        } else {
            return null;
        }

    }

    //Method for creating a question
    public Quiz createQuiz(Quiz quiz) {
        DbManager dbManager = new DbManager();
        Quiz createdQuiz = dbManager.createQuiz(quiz);
        if (createdQuiz != null) {
            return createdQuiz;
        } else {
            return null;
        }
    }

    public boolean updateQuestionCount(int quizId, int questionCount) {
        DbManager dbManager = new DbManager();
        Boolean updatedCount = dbManager.updateQuestionCount(quizId, questionCount);
        if(updatedCount = true) {
            return true;
        } else {
            return false;
        }
    }

    //Method for deleting a quiz and all it's sub-tables
    public Boolean deleteQuiz(int quizId) {
        DbManager dbManager = new DbManager();
        Boolean ifDeleted = dbManager.deleteQuiz(quizId);
        return ifDeleted;
    }

    public ArrayList<Question> loadQuestions(int quizId) {
        DbManager dbManager = new DbManager();
        ArrayList<Question> question = dbManager.loadQuestions(quizId);

        if(!question.isEmpty()) {
            return question;
        } else {
            return null;
        }
    }

    //Method for creating a question
    public Question createQuestion(Question question) {
        DbManager dbManager = new DbManager();
        Question createdQuestion = dbManager.createQuestion(question);

        if (createdQuestion != null) {
            return createdQuestion;
        } else {
            return null;
        }
    }

    //Method for creating a option
    public Option createOption(Option option) {
        DbManager dbManager = new DbManager();
        Option newOption = dbManager.createOption(option);
        if (newOption != null) {
            return newOption;
        } else {
            return null;
        }

    }
    //Method for loading options
    public ArrayList<Option> loadOptions(int questionId) {
        DbManager dbManager = new DbManager();
        ArrayList<Option> options = dbManager.loadOptions(questionId);

        if(!options.isEmpty()) {
            return options;
        } else {
            return null;
        }
    }



}
