package courses.innotech;

import courses.innotech.utils.Utils;

import java.util.concurrent.TimeUnit;

public class Start {
  public static void main(String[] args) throws InterruptedException {
    Student student = new Student("ivan");
    Studentable proxyStudentable = Utils.cache(student);

    proxyStudentable.addGrade(3);
    proxyStudentable.calcAvgGrades();
    proxyStudentable.addGrade(5);
    proxyStudentable.calcAvgGrades();

    proxyStudentable.addGrade(5);
    proxyStudentable.calcAvgGrades();
    proxyStudentable.delGrade();
    proxyStudentable.calcAvgGrades();
    proxyStudentable.delGrade();
    proxyStudentable.calcAvgGrades();

    proxyStudentable.calcAvgGrades();

    TimeUnit.SECONDS.sleep(5);

    proxyStudentable.calcAvgGrades();
    proxyStudentable.calcAvgGrades();
  }
}
