package kg.attractor.java.model;

public class CalendarDay {
  private final boolean isEmpty;
  private final int dayNumber;
  private final boolean hasAppointments;
  private final int appointmentCount;

  public CalendarDay() {
    this.isEmpty = true;
    this.dayNumber = 0;
    this.hasAppointments = false;
    this.appointmentCount = 0;
  }

  public CalendarDay(int dayNumber, boolean hasAppointments, int appointmentCount) {
    this.isEmpty = false;
    this.dayNumber = dayNumber;
    this.hasAppointments = hasAppointments;
    this.appointmentCount = appointmentCount;
  }

  public boolean isEmpty() {
    return isEmpty;
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
