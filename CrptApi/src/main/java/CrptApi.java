import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.regex.Pattern;


public class CrptApi {

    /**
     * Классы для описания документа, информации о продуктах и ответа сервера
     */
    @Getter
    @Setter
    @AllArgsConstructor
    class Response {
        String omsId;
        String reportId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    class Document {
        String usageType;
        String documentFormat;
        String type;
        String participantInn;
        @SerializedName("productionDate")
        LocalDate productionDate;
        @SerializedName("products")
        List<Product> products;
        @SerializedName("produced")
        Produced produced;
        @SerializedName("import")
        Import anImport;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    class Product {
        String code;
        String certificateDocument;
        @SerializedName("certificateDocumentDate")
        LocalDate certificateDocumentDate;
        String certificateDocumentNumber;
        String tnvedCode;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    class Produced {
        String producerInn;
        String ownerInn;
        String productionType;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    class Import {
        @SerializedName("declarationDate")
        LocalDate declarationDate;
        String declarationNumber;
        String customsCode;
        long decisionCode;
    }

    /**
     * Перечисления допустимых значений в документе, согласно описанию в API
     */

    enum UsageType {
        SENT_TO_PRINTER("SENT_TO_PRINTER");

        private final String value;

        UsageType(String value) {
            this.value = value;
        }

        String getValue() {
            return this.value;
        }
    }

    enum DocumentFormat {
        MANUAL("MANUAL");

        private final String value;

        DocumentFormat(String value) {
            this.value = value;
        }

        String getValue() {
            return this.value;
        }
    }

    enum Type {
        LP_INTRODUCE_GOODS_AUTO("LP_INTRODUCE_GOODS_AUTO"),
        LP_GOODS_IMPORT_AUTO("LP_GOODS_IMPORT_AUTO");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        String getValue() {
            return this.value;
        }
    }

    enum CertificateDocument {
        CERTIFICATE("1"),
        DECLARATION("2");

        private final String code;

        CertificateDocument(String code) {
            this.code = code;
        }

        String getCode() {
            return this.code;
        }
    }

    enum ProductionType {
        OWN_PRODUCTION("OWN_PRODUCTION");

        private final String value;

        ProductionType(String value) {
            this.value = value;
        }

        String getValue() {
            return this.value;
        }
    }

    enum InnLength {
        TEN(10),
        TWELVE(12);

        private final int amount;

        InnLength(int amount) {
            this.amount = amount;
        }

        int getAmount() {
            return this.amount;
        }
    }


    /**
     * Класс проверки документа
     */
    class ConfirmationService {

        private static final int MAX_YEARS_FOR_CERTIFICATE_DOCUMENT = 5;
        private static final int TNVED_CODE_LENGTH = 10;
        private static final Pattern REGEX = Pattern.compile("^[0-9]+$");

        boolean isValid(Document document) {
            return document.getUsageType() != null
                    && Arrays.stream(UsageType.values()).anyMatch(usageType -> usageType.getValue().equals(document.getUsageType()))
                    && document.getDocumentFormat() != null
                    && Arrays.stream(DocumentFormat.values()).anyMatch(documentFormat -> documentFormat.getValue().equals(document.getDocumentFormat()))
                    && document.getType() != null
                    && Arrays.stream(Type.values()).anyMatch(type -> type.getValue().equals(document.getType()))
                    && document.getParticipantInn() != null
                    && innIsValid(document.getParticipantInn())
                    && isValid(document.getProductionDate())
                    && isValid(document.getProducts())
                    && isValid(document.getProduced(), document.getType())
                    && isValid(document.getAnImport(), document.getType());
        }

        private boolean isValid(List<Product> products) {
            return products.stream()
                    .noneMatch(product ->
                            product.getTnvedCode() == null
                                    || product.getTnvedCode().length() != TNVED_CODE_LENGTH
                                    || product.getTnvedCode().isBlank()
                                    || !certificateDocumentDateIsValid(product.getCertificateDocumentDate())
                                    || !certificateDocumentIsValid(product.getCertificateDocument())
                                    || product.getCode() == null
                                    || product.getCode().isEmpty()
                                    || product.getCode().isBlank()
                    );
        }

        private boolean isValid(Import importObj, String type) {
            if (type.equals(Type.LP_INTRODUCE_GOODS_AUTO.getValue()) && importObj == null) return true;
            return type.equals(Type.LP_GOODS_IMPORT_AUTO.getValue())
                    && importObj.getDecisionCode() > 0
                    && importObj.getCustomsCode() != null
                    && !importObj.getCustomsCode().isBlank()
                    && importObj.getDeclarationNumber() != null
                    && !importObj.getDeclarationNumber().isBlank()
                    && importObj.getDeclarationDate() != null
                    && isValid(importObj.getDeclarationDate());
        }

        private boolean isValid(Produced produced, String type) {
            if (type.equals(Type.LP_GOODS_IMPORT_AUTO.getValue()) && produced == null) return true;
            return type.equals(Type.LP_INTRODUCE_GOODS_AUTO.getValue())
                    && produced.getProductionType() != null
                    && produced.getProductionType().equals(ProductionType.OWN_PRODUCTION.getValue())
                    && innIsValid(produced.getOwnerInn())
                    && innIsValid(produced.getProducerInn());
        }

        private boolean isValid(LocalDate date) {
            final var nowDate = LocalDate.now();
            return date != null
                    && date.isAfter(ChronoLocalDate.from(nowDate.minusYears(MAX_YEARS_FOR_CERTIFICATE_DOCUMENT)))
                    && date.isBefore(nowDate);
        }

        private boolean innIsValid(String inn) {
            return inn != null
                    && (inn.length() == InnLength.TEN.getAmount() || inn.length() == InnLength.TWELVE.getAmount())
                    && !inn.isBlank()
                    && REGEX.matcher(inn).matches();
        }

        private boolean certificateDocumentDateIsValid(LocalDate date) {
            if (date == null) return true;
            return isValid(date);
        }

        private boolean certificateDocumentIsValid(String certificateDocument) {
            if (certificateDocument == null) return true;
            return
                    certificateDocument.equals(CertificateDocument.CERTIFICATE.getCode())
                            || certificateDocument.equals(CertificateDocument.DECLARATION.getCode());
        }
    }

    /**
     * Константы
    */
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    private static final String URL = "https://<host:port>/api/v2/{extension}/rollout";
    private static final String QUERY_OMS_ID_PARAM = "omsId=<Unique_OMS_identifier>";
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json";
    private static final String CLIENT_TOKEN_HEADER_NAME = "clientToken";
    private static final String USERNAME_HEADER_NAME = "userName";
    private static final String USERNAME_VALUE = "user_name";
    private static final String ACCEPT_HEADER_NAME = "Accept";
    private static final String ACCEPT_VALUE = "*/*";

    private final long delay;
    private final int requestLimit;
    private final ConfirmationService confirmation = new ConfirmationService();
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Переменная - счетчик числа запросов
     */
    private int counter;

    /**
     * @param amount set delay for main Thread
     * @param requestLimit set maximum available amount of requests
     */
    public CrptApi(TimeUnit timeUnit, int amount, int requestLimit) {
        this.requestLimit = Math.max(requestLimit, 0);
        this.delay = amount < 0 ? 0 : timeUnit.toMillis(amount);
    }

    /**
     * Основной метод работы
     */
    public Response getResponse(Document document, String clientToken) throws Exception {
        if (!this.confirmation.isValid(document))
            throw new Exception("Invalid value of the document");
        if (clientToken == null) throw new Exception("Token can not be null");
        try {
            incrementCounter();
            return getResponseFromServer(document, clientToken);
        } catch (Exception e) {
            throw new Exception("The document didn't send: " + document);
        }
    }

    private void incrementCounter() throws InterruptedException {
        try {
            this.lock.lock();
            while (this.counter > this.requestLimit) this.condition.await();
            ++this.counter;
        } finally {
            this.lock.unlock();
        }
    }

    private Response getResponseFromServer(Document document, String clientToken) throws IOException, InterruptedException {
        executor.execute(this::counterDecreaseAfterDelay);
        final var client = HttpClient.newHttpClient();
        final var gson = new GsonBuilder().setDateFormat(DATE_FORMAT_PATTERN).create();
        final var request = HttpRequest.newBuilder()
                .uri(URI.create(URL + "?" + QUERY_OMS_ID_PARAM))
                .header(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_VALUE)
                .header(CLIENT_TOKEN_HEADER_NAME, clientToken)
                .header(USERNAME_HEADER_NAME, USERNAME_VALUE)
                .header(ACCEPT_HEADER_NAME, ACCEPT_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(document)))
                .build();
        final var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), Response.class);
    }

    private void counterDecreaseAfterDelay() {
        try {
            Thread.sleep(this.delay);
            --this.counter;
            this.condition.signalAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}