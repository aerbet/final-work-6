package kg.attractor.java.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kg.attractor.java.model.Patient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtil {

  private FileUtil() {
  }

  private static final TypeReference<Map<LocalDate, List<Patient>>> SCHEDULE_TYPE =
          new TypeReference<Map<LocalDate, List<Patient>>>() {};

  private static final ObjectMapper MAPPER = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .enable(SerializationFeature.INDENT_OUTPUT);

  private static final Path SCHEDULE_PATH = Paths.get("src", "kg", "attractor", "java", "calendarData", "schedule.json");

  public static Map<LocalDate, List<Patient>> readSchedule() {
    try {
      if (!Files.exists(SCHEDULE_PATH)) {
        if (SCHEDULE_PATH.getParent() != null) {
          Files.createDirectories(SCHEDULE_PATH.getParent());
        }
        return new HashMap<>();
      }

      return MAPPER.readValue(SCHEDULE_PATH.toFile(), SCHEDULE_TYPE);
    } catch (IOException e) {
      System.err.println("Ошибка чтения schedule.json: " + e.getMessage());
      return new HashMap<>();
    }
  }

  public static void saveSchedule(Map<LocalDate, List<Patient>> data) {
    try {
      if (SCHEDULE_PATH.getParent() != null) {
        Files.createDirectories(SCHEDULE_PATH.getParent());
      }
      MAPPER.writeValue(SCHEDULE_PATH.toFile(), data);
    } catch (IOException e) {
      System.err.println("Ошибка записи schedule.json: " + e.getMessage());
    }
  }
}