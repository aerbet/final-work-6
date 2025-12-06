package kg.attractor.java.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarDay {
  private final boolean isEmpty;
  private final int dayNumber;
  private final boolean isToday;
  private final boolean hasAppointments;
  private final int appointmentCount;
  private final List<Patient> appointments;

  public CalendarDay() {
    this.isEmpty = true;
    this.dayNumber = 0;
    this.isToday = false;
    this.hasAppointments = false;
    this.appointmentCount = 0;
    this.appointments = Collections.emptyList();
  }

  public CalendarDay(int dayNumber, boolean isToday, List<Patient> patients) {
    this.isEmpty = false;
    this.dayNumber = dayNumber;
    this.isToday = isToday;

    boolean hasAppts = patients != null && !patients.isEmpty();
    this.hasAppointments = hasAppts;
    this.appointmentCount = hasAppts ? patients.size() : 0;


    this.appointments = hasAppts ? patients.stream().limit(2).collect(Collectors.toList()) : Collections.emptyList();
  }

  public boolean isEmpty() {
    return isEmpty;
  }

  public boolean isToday() {
    return isToday;
  }

  public List<Patient> getAppointments() {
    return appointments;
  }

  public int getDayNumber() {
    return dayNumber;
  }

  public boolean isHasAppointments() {
    return hasAppointments;
  }

  public int getAppointmentCount() {
    return appointmentCount;
  }
}
