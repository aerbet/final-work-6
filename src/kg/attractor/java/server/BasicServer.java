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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
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
      monthNameVal = firstDayOfMonth.getMonth().getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, new Locale("ru"));
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

  protected static String getContentType(HttpExchange exchange) {
    return exchange.getRequestHeaders()
            .getOrDefault("Content-Type", List.of(""))
            .getFirst();
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

  protected void setCookie(HttpExchange exchange, Cookie cookie) {
    exchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
  }

  protected static String getCookies(HttpExchange exchange) {
    return exchange.getRequestHeaders()
            .getOrDefault("Cookie", List.of(""))
            .getFirst();

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
