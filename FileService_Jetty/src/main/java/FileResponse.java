public class FileResponse {
    private final String id;
    private final String name;
    private final long size;

    public FileResponse(String id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
