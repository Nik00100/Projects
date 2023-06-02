import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface FileServiceInterface {
    void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
