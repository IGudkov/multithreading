package courses.innotech;

import courses.innotech.utils.Cache;
import courses.innotech.utils.Mutator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ToString
public class Student implements Studentable {
  @Getter @Setter
  private String name;
  private List<Integer> grades = new ArrayList<>();

  public Student(String name) {
    this.name = name;
  }

  public List<Integer> getGrades() {
    return new ArrayList<>(grades);
  }

  @Mutator
  public void addGrade(Integer grade) {
    this.grades.add(grade);
  }

  public void delGrade(){
    if (!this.grades.isEmpty())
      this.grades.remove(this.grades.size()-1);
  }
  @Cache(3000)
  public Double calcAvgGrades(){
    //System.out.println("зашли в метод calcAvgGrades");
    return this.grades
        .stream()
        .mapToInt(a -> a)
        .average()
        .orElse(0);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Student student = (Student) o;
    return Objects.equals(name, student.name) && Objects.equals(grades, student.grades);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, grades);
  }
}
