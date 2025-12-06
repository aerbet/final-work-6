package kg.attractor.java.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import kg.attractor.java.model.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;


public class BasicServer {
  private final static Configuration freemarker = initFreeMarker();
  private Map<String, RouteHandler> routes = new HashMap<>();
  private final String dataDir = "data";
  private final HttpServer server;
  private final HospitalRepository hospitalRepository;

  public BasicServer(String host, int port) throws IOException {
    server = createServer(host, port);
    registerCommonHandlers();
    this.hospitalRepository = new HospitalRepository();

    registerGet("/calendar", this::calendarHandler);
    registerGet("/day", this::dayHandler);
    registerGet("/record", this::recordHandler);
    registerGet("/edit", this::editHandler);

    registerPost("/add-patient", this::addPatientHandler);
    registerPost("/delete-patient", this::deletePatientHandler);
    registerPost("/edit-patient", this::editPatientHandler);
  }

  private void editHandler(HttpExchange exchange) {
    Map<String, Object> data = new HashMap<>();
    String query = exchange.getRequestURI().getQuery();
    Map<String, String> params = Utils.parseUrlEncoded(query, "&");

    String dateStr = params.get("date");
    String id = params.get("id");

    try {
      LocalDate date = LocalDate.parse(dateStr);
      Patient patient = hospitalRepository.getPatientsByDate(date).stream()
              .filter(p -> p.getId().equals(id))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("Пациент не найден"));

      data.put("date", dateStr);
      data.put("patient", patient);

      renderTemplate(exchange, "edit.ftl", data);
    } catch (Exception e) {
      e.printStackTrace();
      renderError(exchange, "Ошибка при загрузке данных: " + e.getMessage());
    }
  }

  private void editPatientHandler(HttpExchange exchange) {
    String rawBody = getRequestBody(exchange);
    Map<String, String> params = Utils.parseUrlEncoded(rawBody, "&");

    try {
      String id = params.get("id");
      String oldDateStr = params.get("oldDate");
      String newDateStr = params.get("newDate");

      String fullName = URLDecoder.decode(params.get("fullName"), StandardCharsets.UTF_8);
      String birthDateStr = params.get("birthDate");
      String timeStr = URLDecoder.decode(params.get("time"), StandardCharsets.UTF_8);
      String type = URLDecoder.decode(params.get("type"), StandardCharsets.UTF_8);
      String symptoms = URLDecoder.decode(params.get("symptoms"), StandardCharsets.UTF_8);

      LocalDate oldDate = LocalDate.parse(oldDateStr);
      LocalDate newDate = LocalDate.parse(newDateStr);
      LocalTime time = LocalTime.parse(timeStr);
      LocalDate birthDate = LocalDate.parse(birthDateStr);

      LocalDateTime newAppointmentDateTime = LocalDateTime.of(newDate, time);

      if (birthDate.isAfter(LocalDate.now())) {
        renderError(exchange, "Ошибка: Дата рождения не может быть в будущем!");
        return;
      }

      if (newAppointmentDateTime.isBefore(LocalDateTime.now())) {
        renderError(exchange, "Ошибка: Нельзя перенести запись на прошедшее время!");
        return;
      }

      hospitalRepository.deletePatient(oldDate, id);

      Patient updatedPatient = new Patient(fullName, birthDate, type, symptoms, time);
      updatedPatient.setId(id);

      boolean success = hospitalRepository.addPatient(newDate, updatedPatient);

      if (!success) {
        renderError(exchange, "Ошибка: Время " + time + " на дату " + newDate + " уже занято.");
        return;
      }

      redirect(exchange, "/day?date=" + newDateStr);

    } catch (Exception e) {
      e.printStackTrace();
      renderError(exchange, "Ошибка редактирования: " + e.getMessage());
    }
  }

  private void recordHandler(HttpExchange exchange) {
    Map<String, Object> data = new HashMap<>();
    String query = exchange.getRequestURI().getQuery();
    Map<String, String> params = Utils.parseUrlEncoded(query, "&");

    String dateStr = params.get("date");

    try {
      if (dateStr == null || dateStr.isBlank()) {
        redirect(exchange, "/calendar");
        return;
      }

      data.put("date", dateStr);

      renderTemplate(exchange, "record.ftl", data);

    } catch (Exception e) {
      e.printStackTrace();
      renderError(exchange, "Ошибка открытия формы записи: " + e.getMessage());
    }
  }

  private void addPatientHandler(HttpExchange exchange) {
    String rawBody = getRequestBody(exchange);
    Map<String, String> params = Utils.parseUrlEncoded(rawBody, "&");

    try {
      String dateStr = params.get("date");
      String fullName = URLDecoder.decode(params.get("fullName"), StandardCharsets.UTF_8);
      String timeStr = URLDecoder.decode(params.get("time"), StandardCharsets.UTF_8);
      String type = URLDecoder.decode(params.get("type"), StandardCharsets.UTF_8);
      String symptoms = URLDecoder.decode(params.get("symptoms"), StandardCharsets.UTF_8);

      String birthDateStr = params.get("birthDate");

      LocalDate date = LocalDate.parse(dateStr);
      LocalTime time = LocalTime.parse(timeStr);

      LocalDate birthDate = LocalDate.parse(birthDateStr);

      LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);

      if (birthDate.isAfter(LocalDate.now())) {
        renderError(exchange, "Ошибка: Дата рождения не может быть в будущем!");
        return;
      }

      if (appointmentDateTime.isBefore(LocalDateTime.now())) {
        renderError(exchange, "Ошибка: Вы пытаетесь записаться на прошедшее время. Выберите актуальное время и дату");
        return;
      }

      Patient newPatient = new Patient(fullName, birthDate, type, symptoms, time);

      boolean success = hospitalRepository.addPatient(date, newPatient);

      if (!success) {
        renderError(exchange, "Ошибка: Время " + time + " на дату " + date + " уже занято.");
        return;
      }

      redirect(exchange, "/day?date=" + dateStr);

    } catch (Exception e) {
      e.printStackTrace();
      renderError(exchange, "Ошибка добавления: " + e.getMessage());
    }
  }

  private void deletePatientHandler(HttpExchange exchange) {
    String rawBody = getRequestBody(exchange);
    Map<String, String> params = Utils.parseUrlEncoded(rawBody, "&");

    try {
      String dateStr = params.get("date");
      String id = params.get("id");

      if (dateStr != null && id != null) {
        LocalDate date = LocalDate.parse(dateStr);
        hospitalRepository.deletePatient(date, id.trim());

        redirect(exchange, "/day?date=" + dateStr);
      }

    } catch (Exception e) {
      e.printStackTrace();
      renderError(exchange, "Ошибка удаления: " + e.getMessage());
    }
  }

  private void dayHandler(HttpExchange exchange) {
    Map<String, Object> data = new HashMap<>();
    String query = exchange.getRequestURI().getQuery();

    Map<String, String> params = Utils.parseUrlEncoded(query, "&");
    String dateStr = params.get("date");

    try {
      if (dateStr == null || dateStr.isBlank()) {
        redirect(exchange, "/calendar");
        return;
      }

      LocalDate date = LocalDate.parse(dateStr);
      List<Patient> patients = hospitalRepository.getPatientsByDate(date);

      data.put("date", date.toString());
      data.put("patients", patients);

      renderTemplate(exchange, "day.ftl", data);

    } catch (Exception e) {
      e.printStackTrace();
      renderError(exchange, "Ошибка отображения дня: " + e.getMessage());
    }
  }

  private void calendarHandler(HttpExchange exchange) {
    Map<String, Object> data = new HashMap<>();

    LocalDate today = LocalDate.now();
    int currentYear = today.getYear();
    int currentMonth = today.getMonthValue();

    try {
      String query = exchange.getRequestURI().getQuery();
      if (query != null) {
        Map<String, String> params = Utils.parseUrlEncoded(query, "&");
        String monthYearStr = params.get("monthYear");

        if (monthYearStr != null && !monthYearStr.isBlank()) {
          LocalDate date = LocalDate.parse(monthYearStr + "-01");
          currentYear = date.getYear();
          currentMonth = date.getMonthValue();
        }
      }
    } catch (Exception e) {
      System.err.println("Ошибка при разборе даты из URL, используем текущую дату: " + e.getMessage());
    }

    LocalDate firstDayOfMonth = LocalDate.of(currentYear, currentMonth, 1);
    int daysInMonth = firstDayOfMonth.lengthOfMonth();
    int offset = firstDayOfMonth.getDayOfWeek().getValue() - 1;

    List<CalendarDay> calendar = new ArrayList<>();

    for (int i = 0; i < offset; i++) {
      calendar.add(new CalendarDay());
    }

    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = LocalDate.of(currentYear, currentMonth, day);
      List<Patient> patients = hospitalRepository.getPatientsByDate(date);
      boolean isToday = date.equals(today);
      calendar.add(new CalendarDay(day, isToday, patients));
    }

    while (calendar.size() % 7 != 0) {
      calendar.add(new CalendarDay());
    }

    LocalDate prevMonthDate = firstDayOfMonth.minusMonths(1);
    LocalDate nextMonthDate = firstDayOfMonth.plusMonths(1);

    data.put("calendar", calendar);
    data.put("currentYear", currentYear);
    data.put("currentMonth", currentMonth);

    String monthNameVal = "Неизвестно";
    try {
      monthNameVal = firstDayOfMonth.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru"));
    } catch (Exception e) {
      monthNameVal = firstDayOfMonth.getMonth().name();
    }
    data.put("monthName", monthNameVal);

    String prevYearStr = String.valueOf(prevMonthDate.getYear());
    String prevMonthStr = String.format("%02d", prevMonthDate.getMonthValue());
    data.put("prevMonthYear", prevYearStr + "-" + prevMonthStr);

    String nextYearStr = String.valueOf(nextMonthDate.getYear());
    String nextMonthStr = String.format("%02d", nextMonthDate.getMonthValue());
    data.put("nextMonthYear", nextYearStr + "-" + nextMonthStr);

    renderTemplate(exchange, "calendar.ftl", data);
  }

  private void renderError(HttpExchange exchange, String errorMessage) {
    Map<String, Object> data = new HashMap<>();
    data.put("error", errorMessage);
    renderTemplate(exchange, "error.html", data);
  }

  private void redirect(HttpExchange exchange, String location) {
    exchange.getResponseHeaders().set("Location", location);
    try {
      exchange.sendResponseHeaders(303, -1);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static Configuration initFreeMarker() {
    try {
      Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
      cfg.setDirectoryForTemplateLoading(new File("data"));

      cfg.setDefaultEncoding("UTF-8");
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      cfg.setLogTemplateExceptions(false);
      cfg.setWrapUncheckedExceptions(true);
      cfg.setFallbackOnNullLoopVariable(false);
      return cfg;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
    try {
      Template temp = freemarker.getTemplate(templateFile);

      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {

        temp.process(dataModel, writer);
        writer.flush();

        var data = stream.toByteArray();

        sendByteData(exchange, ResponseCodes.OK, ContentType.TEXT_HTML, data);
      }
    } catch (IOException | TemplateException e) {
      e.printStackTrace();
      try {
        String errorMessage = "500 Server Error:\n" + e.getMessage();
        byte[] resp = errorMessage.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(500, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.getResponseBody().close();
      } catch (IOException io) {
        System.err.println("Не удалось отправить ошибку клиенту");
      }
    }
  }

  protected static String getRequestBody(HttpExchange exchange) {
    InputStream stream = exchange.getRequestBody();
    Charset charset = StandardCharsets.UTF_8;
    InputStreamReader isr = new InputStreamReader(stream, charset);

    try (BufferedReader br = new BufferedReader(isr)) {
      return br.lines().collect(Collectors.joining(""));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    return "";
  }

  private static String makeKey(String method, String route) {
    route = ensureStartsWithSlash(route);
    return String.format("%s %s", method.toUpperCase(), route);
  }

  private static String makeKey(HttpExchange exchange) {
    String method = exchange.getRequestMethod();
    String path = exchange.getRequestURI().getPath();

    if (path.endsWith("/") && path.length() > 1) {
      path = path.substring(0, path.length() - 1);
    }

    int index = path.lastIndexOf(".");
    String extOrPath = index != -1 ? path.substring(index).toLowerCase() : path;

    return makeKey(method, extOrPath);
  }

  private static String ensureStartsWithSlash(String route) {
    if (route.startsWith(".")) {
      return route;
    }
    return route.startsWith("/") ? route : "/" + route;
  }

  private static void setContentType(HttpExchange exchange, ContentType type) {
    exchange.getResponseHeaders().set("Content-Type", String.valueOf(type));
  }

  private static HttpServer createServer(String host, int port) throws IOException {
    var msg = "Starting server on http://%s:%s/%n";
    System.out.printf(msg, host, port);
    var address = new InetSocketAddress(host, port);
    return HttpServer.create(address, 50);
  }

  private void registerCommonHandlers() {
    server.createContext("/", this::handleIncomingServerRequests);

    registerGet("/", this::calendarHandler);

    registerFileHandler(".css", ContentType.TEXT_CSS);
    registerFileHandler(".html", ContentType.TEXT_HTML);
    registerFileHandler(".jpg", ContentType.IMAGE_JPEG);
    registerFileHandler(".png", ContentType.IMAGE_PNG);

  }

  protected final void registerGet(String route, RouteHandler handler) {
    registerGenericHandler("GET", route, handler);
  }

  protected final void registerPost(String route, RouteHandler handler) {
    registerGenericHandler("POST", route, handler);
  }

  protected final void registerGenericHandler(String method, String route, RouteHandler handler) {
    getRoutes().put(makeKey(method, route), handler);
  }

  protected final void registerFileHandler(String fileExt, ContentType type) {
    registerGet(fileExt, exchange -> sendFile(exchange, makeFilePath(exchange), type));
  }

  protected final Map<String, RouteHandler> getRoutes() {
    return routes;
  }

  protected final void sendFile(HttpExchange exchange, Path pathToFile, ContentType contentType) {
    try {
      if (Files.notExists(pathToFile)) {
        respond404(exchange);
        return;
      }
      var data = Files.readAllBytes(pathToFile);
      sendByteData(exchange, ResponseCodes.OK, contentType, data);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Path makeFilePath(HttpExchange exchange) {
    return makeFilePath(exchange.getRequestURI().getPath());
  }

  protected Path makeFilePath(String... s) {
    return Path.of(dataDir, s);
  }

  protected final void sendByteData(HttpExchange exchange, ResponseCodes responseCode,
                                    ContentType contentType, byte[] data) throws IOException {
    try (var output = exchange.getResponseBody()) {
      setContentType(exchange, contentType);
      exchange.sendResponseHeaders(responseCode.getCode(), 0);
      output.write(data);
      output.flush();
    }
  }

  private void respond404(HttpExchange exchange) {
    try {
      var data = "404 Not found".getBytes();
      sendByteData(exchange, ResponseCodes.NOT_FOUND, ContentType.TEXT_PLAIN, data);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handleIncomingServerRequests(HttpExchange exchange) {
    var route = getRoutes().getOrDefault(makeKey(exchange), this::respond404);
    route.handle(exchange);
  }

  public final void start() {
    server.start();
  }
}
