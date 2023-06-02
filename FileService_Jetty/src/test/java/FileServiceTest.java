import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

public class FileServiceTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Part filePart;

    private FileService fileService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this); // Инициализация mock-объектов
        fileService = new FileService();
    }

    @Test
    public void testDoPost() throws Exception {
        // Установка mock объектов и данных запроса
        when(request.getPart("file")).thenReturn(filePart);
        when(filePart.getSubmittedFileName()).thenReturn("test.txt");
        when(request.getContentLengthLong()).thenReturn(500L);

        // Создание ByteArrayOutputStream для записи ответа
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        when(response.getWriter()).thenReturn(writer);

        // Вызов метода doPost()
        fileService.doPost(request, response);

        // Проверка статуса
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void testDoGet_UnknownURL_Returns404() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/unknown-url");

        // Вызов метода
        fileService.doGet(request, response);

        // Проверка статуса
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }


    @Test
    public void testDoGet_GetFileList_ReturnsFileList() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/files");

        // StringWriter чтобы перехватить ответ
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        // Вызов метода
        fileService.doGet(request, response);

        // Проверка, что отправлен JSON
        verify(response).setContentType("application/json");
    }

}
