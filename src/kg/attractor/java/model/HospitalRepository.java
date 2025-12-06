package kg.attractor.java.model;

import kg.attractor.java.utils.FileUtil;
import kg.attractor.java.server.Generator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class HospitalRepository {
  private Map<LocalDate, List<Patient>> schedule = new HashMap<>();

  public HospitalRepository() {
    loadData();

    if (schedule.isEmpty()) {
      initDemoData();
      saveToJson();
    }
  }

  private void loadData() {
    try {
      Map<LocalDate, List<Patient>> loadedData = FileUtil.readSchedule();
      if (loadedData != null) {
        this.schedule = loadedData.entrySet().stream()
                .filter(entry -> entry.getKey().isAfter(LocalDate.now().minusDays(1)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      }
    } catch (Exception e) {
      System.err.println("Ошибка при загрузке расписания: " + e.getMessage());
    }
  }

  private void saveToJson() {
    try {
      FileUtil.saveSchedule(this.schedule);
    } catch (Exception e) {
      System.err.println("Ошибка при сохранении расписания: " + e.getMessage());
    }
  }

  private void initDemoData() {
    LocalDate today = LocalDate.now();
    String[] types = {"Первичный", "Вторичный"};

    for (int i = 0; i < 7; i++) {
      LocalDate date = today.plusDays(i);
      int count = ThreadLocalRandom.current().nextInt(2, 6);

      for (int j = 0; j < count; j++) {
        LocalTime time = LocalTime.of(9 + j, ThreadLocalRandom.current().nextBoolean() ? 0 : 30);

        String fullName = Generator.makeName();
        String symptoms = Generator.makeDescription();
        String type = types[ThreadLocalRandom.current().nextInt(types.length)];
        LocalDate dob = LocalDate.of(1970 + ThreadLocalRandom.current().nextInt(30),
                ThreadLocalRandom.current().nextInt(12) + 1,
                ThreadLocalRandom.current().nextInt(28) + 1);
        String notes = Generator.makeDescription();

        Patient newPatient = new Patient(fullName, dob, type, symptoms, time);
        addPatientInternal(date, newPatient);
      }
    }
    System.out.println("Сгенерированы демо-данные на 7 дней.");
  }

  private boolean addPatientInternal(LocalDate date, Patient patient) {
    List<Patient> dailyList = schedule.computeIfAbsent(date, k -> new ArrayList<>());

    boolean timeBusy = dailyList.stream().anyMatch(p -> p.getAppointmentTime().equals(patient.getAppointmentTime()));
    if (timeBusy) {
      return false;
    }
    dailyList.add(patient);
    return true;
  }

  public List<Patient> getPatientsByDate(LocalDate date) {
    List<Patient> list = schedule.getOrDefault(date, new ArrayList<>());
    list.sort(Comparator.comparing(Patient::getAppointmentTime));
    return list;
  }
}