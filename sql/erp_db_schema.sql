DROP DATABASE IF EXISTS erp_db;
CREATE DATABASE IF NOT EXISTS erp_db;
USE erp_db;

CREATE TABLE instructors (
                             UserID INT PRIMARY KEY NOT NULL,
                             Name VARCHAR(200) NOT NULL,
                             Email VARCHAR(100) UNIQUE NOT NULL,
                             Qualification VARCHAR(100) NOT NULL,
                             JoiningDate DATE NOT NULL,
                             Department VARCHAR(100) NOT NULL,
                             InstructorID INT NOT NULL
);

CREATE TABLE courses (
                         CourseId INT PRIMARY KEY,
                         Code VARCHAR(40) NOT NULL UNIQUE,
                         Title VARCHAR(100) UNIQUE NOT NULL,
                         Credits INT NOT NULL
    -- removed instructor id
);

CREATE TABLE students (
                          Name VARCHAR(100) NOT NULL,
                          RollNumber INT PRIMARY KEY,
                          Program VARCHAR(200) NOT NULL,
                          Year INT NOT NULL,
                          UserID INT NOT NULL
);

CREATE TABLE offerings (
                           OfferingID INT PRIMARY KEY,
                           CourseID INT NOT NULL,
                           InstructorID INT NOT NULL,
                           Semester VARCHAR(100) NOT NULL,
                           Year INT NOT NULL,
                           Capacity INT,
                           CurrentEnrollment INT -- added a column to track current registrations
                           GradeSlabs JSON,
                           GradeComponents JSON,
                           LectureSchedule JSON,
                           LabSchedule JSON,
                           Announcements JSON
);

CREATE TABLE registrations (
                               RegistrationNumber INT PRIMARY KEY,
                               StudentRollNumber INT NOT NULL,
                               OfferingID INT NOT NULL,
                               Status VARCHAR(100) NOT NULL
);

CREATE TABLE studentgraderecord (
                                    OfferingID INT NOT NULL,
                                    RollNumber INT,
                                    Grade JSON
);

CREATE TABLE semesters (
    SemData JSON,
    Maintainence int,
    CurrentSem JSON
)

CREATE TABLE StudentNotifications (
                                      StudentRollNumber INT PRIMARY KEY,
                                      NotificationData JSON
);