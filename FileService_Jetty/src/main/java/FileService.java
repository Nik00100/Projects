import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@WebServlet(name = "FileService", urlPatterns = {"/upload", "/download/*", "/files"}, loadOnStartup = 1)
@MultipartConfig
public class FileService extends HttpServlet implements FileServiceInterface {

    private static final long MAX_FILE_SIZE = 100 * 1024; // 100 KB
    private static final String DIRECTORY = "C:\\Users\\Nik Kirillov\\Desktop\\New folder\\Downloads";
    private Map<String, String> mapForIdAndFileNames = new HashMap<>();

    public Map<String, String> getMapForIdAndFileNames() {
        return mapForIdAndFileNames;
    }



    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Убедиться, что папка для сохранения файлов существует
        Path uploadDirectory = Path.of(DIRECTORY);
        if (!Files.exists(uploadDirectory)) {
            try {
                Files.createDirectories(uploadDirectory);
            } catch (IOException e) {
                throw new ServletException("Couldn't upload to requested directory", e);
            }
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Получаем информацию о файле из запроса
        Part filePart = request.getPart("file");
        String fileName = filePart.getSubmittedFileName();
        long contentLength = request.getContentLengthLong();

        // Проверяем размер файла
        if (contentLength > MAX_FILE_SIZE) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("File size more than 100 KB");
            return;
        }

        // Проверяем расширение файла
        if (fileName != null && (fileName.endsWith(".txt") || fileName.endsWith(".csv"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Not supported .txt and .csv files");
            return;
        }

        // Генерируем уникальный идентификатор файла
        String fileId = UUID.randomUUID().toString();
        // Определяем расширение файла
        String fileExtension = FilenameUtils.getExtension(fileName);
        // Сохраняем файл на сервере с оригинальным расширением
        filePart.write(DIRECTORY + "\\" + fileId + "." + fileExtension);
        // Обновляем мапу
        mapForIdAndFileNames.put(fileId, fileName);
        // Формируем и отправляем ответ
        response.setContentType("application/json");
        response.getWriter().printf("{\"id\":\"%s\",\"size\":%d,\"name\":\"%s\"}",
                fileId, contentLength / 1024, fileName);
    }


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/download/")) {
            // Обрабатываем GET-запрос на скачивание файла
            downloadFile(request, response);
        } else if (requestURI.equals("/files")) {
            // Обрабатываем GET-запрос на получение списка файлов
            getFileList(response);
        } else {
            // Возвращаем ошибку 404 для неизвестного URL
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void downloadFile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fileId = request.getRequestURI().substring("/download/".length());

        // Проверяем наличие файла в мапе
        String fileName = mapForIdAndFileNames.get(fileId);
        if (fileName == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Определяем путь к файлу
        String fileExtension = FilenameUtils.getExtension(fileName);
        String filePath = DIRECTORY + "\\" + fileId + "." + fileExtension;

        // Проверяем наличие файла
        File file = new File(filePath);
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Устанавливаем заголовки для скачивания файла
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentType(getServletContext().getMimeType(fileName));
        response.setContentLength((int) file.length());

        // Копируем файл в ответ
        FileUtils.copyFile(file, response.getOutputStream());
    }

    private void getFileList(HttpServletResponse response) throws ServletException, IOException {
        // Получение списка файлов
        List<FileResponse> fileResponses = new ArrayList<>();
        for (Map.Entry<String, String> entry : mapForIdAndFileNames.entrySet()) {
            String fileId = entry.getKey();
            String fileName = entry.getValue();
            long fileSize = getFileSize(fileId);
            FileResponse fileResponse = new FileResponse(fileId, fileName, fileSize);
            fileResponses.add(fileResponse);
        }

        // Преобразование списка в JSON
        Gson gson = new Gson();
        String json = gson.toJson(fileResponses);

        // Установка Content-Type и запись JSON в response
        response.setContentType("application/json");
        response.getWriter().write(json);
    }

    private long getFileSize(String fileId) {
        Path filePath = Paths.get(DIRECTORY, fileId);
        try {
            return Files.size(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
