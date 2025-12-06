package kg.attractor.java.model;

import kg.attractor.java.server.Generator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Patient {
  private String id;
  private String fullName;
  private LocalDate birthDate;
  private String type;
  private String symptoms;
  private LocalTime appointmentTime;

  public Patient(String fullName, LocalDate birthDate, String type, String symptoms, LocalTime appointmentTime) {
    this.id = UUID.randomUUID().toString();
    this.fullName = fullName;
    this.birthDate = birthDate;
    this.type = type;
    this.symptoms = symptoms;
    this.appointmentTime = appointmentTime;
  }

  private Patient() {
    this.id = Generator.makeId();
    this.fullName = null;
    this.birthDate = null;
    this.type = null;
    this.symptoms = null;
    this.appointmentTime = null;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getSymptoms() {
    return symptoms;
  }

  public void setSymptoms(String symptoms) {
    this.symptoms = symptoms;
  }

  public LocalTime getAppointmentTime() {
    return appointmentTime;
  }

  public void setAppointmentTime(LocalTime appointmentTime) {
    this.appointmentTime = appointmentTime;
  }
}
