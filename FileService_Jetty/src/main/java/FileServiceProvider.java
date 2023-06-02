import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.ServiceLoader;

public class FileServiceProvider extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private FileServiceInterface fileService;

    @Override
    public void init() throws ServletException {
        ServiceLoader<FileServiceInterface> loader = ServiceLoader.load(FileServiceInterface.class);
        Iterator<FileServiceInterface> iterator = loader.iterator();
        if (iterator.hasNext()) {
            fileService = iterator.next();
        } else {
            throw new ServletException("No implementation found for FileServiceInterface.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        fileService.doPost(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        fileService.doGet(request, response);
    }
}
