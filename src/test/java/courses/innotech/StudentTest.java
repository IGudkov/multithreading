package courses.innotech;

import courses.innotech.utils.Utils;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

class StudentTest {

  @Test
  @DisplayName("Инкапсуляция. Нет возможности получить ссылку на массив для последующей модификации")
  public void checkModifyGrades(){
    Student student = new Student("Ivan");
    Studentable proxyStudentable = Utils.cache(student);

    List<Integer> grades = student.getGrades();
    grades.add(2);

    Assertions.assertTrue(student.getGrades().isEmpty());
  }

  @Test
  @DisplayName("Устанавливаем значения через addGrade, через calcAvgGrades рассчитываем + кэшируем," +
      " затем удаляем последнее значение и получаем закэшированное значение предыдущего состояния")
  public void methodDelGardeCacheTrue() throws InterruptedException {
    long timeOut = 0;

    Student student = new Student("Ivan");
    Studentable proxyStudentable = Utils.cache(student);

    for (int i = 0; i < 10000000; i++) {
      proxyStudentable.addGrade((int)(Math.random()*(4)) +2);
    }

    //устанавливаем значения через addGrade и рассчитываем + кэшируем через calcAvgGrades
    proxyStudentable.addGrade(3);
    proxyStudentable.calcAvgGrades();
    proxyStudentable.addGrade(4);
    proxyStudentable.calcAvgGrades();
    proxyStudentable.addGrade(5);
    proxyStudentable.calcAvgGrades();

    //рассчитываем время, требуемое для возврата закэшированного значения
    for (int i = 0; i < 10; i++) {
      Instant start = Instant.now();
      Double d = proxyStudentable.calcAvgGrades();
      Instant finish = Instant.now();
      long timeExecute = Duration.between(start, finish).toMillis();
      if (timeExecute > timeOut)
        timeOut = timeExecute;
    }

    //удалили последнее значение
    proxyStudentable.delGrade();

    //получаем закэшированное значение
    Instant start = Instant.now();
    Double d2 = proxyStudentable.calcAvgGrades();
    Instant finish = Instant.now();

    Assertions.assertFalse(Duration.between(start, finish).toMillis() > timeOut);
  }

  @Test
  @DisplayName("Устанавливаем значения через addGrade и рассчитываем + кэшируем через calcAvgGrades," +
      " затем удаляем последнее значение, ждем 5 сек (за это время удаляется закэшированное значение) " +
      "и считаем значение заново")
  public void methodDelGardeCacheFalse() throws InterruptedException {
    long timeOut = 0;

    Student student = new Student("Ivan");
    Studentable proxyStudentable = Utils.cache(student);

    for (int i = 0; i < 10000000; i++) {
      proxyStudentable.addGrade((int)(Math.random()*(4)) +2);
    }

    //устанавливаем значения через addGrade и рассчитываем + кэшируем через calcAvgGrades
    proxyStudentable.addGrade(3);
    proxyStudentable.calcAvgGrades();
    proxyStudentable.addGrade(4);
    proxyStudentable.calcAvgGrades();
    proxyStudentable.addGrade(5);
    proxyStudentable.calcAvgGrades();

    //рассчитываем время, требуемое для возврата закэшированного значения
    for (int i = 0; i < 10; i++) {
      Instant start = Instant.now();
      Double d = proxyStudentable.calcAvgGrades();
      Instant finish = Instant.now();
      long timeExecute = Duration.between(start, finish).toMillis();
      if (timeOut < timeExecute)
        timeOut = timeExecute;
    }

    //удалили последнее значение
    proxyStudentable.delGrade();

    TimeUnit.SECONDS.sleep(5);

    //вызывается метод по расчету значения, т.к. за 5 сек ожидания кэш удалился
    Instant start = Instant.now();
    Double d2 = proxyStudentable.calcAvgGrades();
    Instant finish = Instant.now();

    Assertions.assertTrue(Duration.between(start, finish).toMillis() > timeOut);
  }

}