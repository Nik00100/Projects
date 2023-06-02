## Задание

Написать веб-сервис для работы с файлами:  
- Веб-сервер реализовать на Jetty (Embedded Mode).
- Реализовать API методы через Servlet API для работы с файлами.

1) Загрузка файла. <br>
Ограничения:
- Размер файла не больше 100 кб.
- Пропускать файлы с расширением .txt и .csv. <br>
Request: POST /upload <br>
Response: <br>
Content-type: application/json <br>
{ <br>
id: <FILE_ID> <br>
size: <FILE_SIZE_KB> <br>
name:  <FILE_NAME> <br>
} 

2) Скачивание файла: <br>
Request: GET /download/<FILE_ID>

3) Получение списка файлов: <br>
Request GET /files <br>
Content-type: application/json <br>
[{ <br>
id: <FILE_ID> <br>
size: <FILE_SIZE_KB> <br>
name:  <FILE_NAME> <br>
}]

- Для каждого метода API, включая ограничения, написать тесты.

- Модуль для работы с файлами реализовать через Service Provider Interface (SPI) механизм. Для примера, разработать хранение в файловой системе.

Требования к технологиям:
- Java 17 и выше
- Servlet API
- Web server - Jetty 
- Сторонние библиотеки, например Gson и т.д.
- Сборка проекта - maven



## Описание запуска веб-сервера, проверки работы API в Postman и запуска тестов.

- Для того чтобы запустить проект, в папке проекта `FileService` выполните команду `mvn jetty:run` в командной строке. `Maven` автоматически загрузит все зависимости, скомпилирует исходный код и запустит сервер `Jetty` с веб-приложением.

![](pic/server_start.png)
#### Рис.1 Запуск сервера

<br/><br/>
- Для тестов API можно использовать Postman.

![](pic/complete_upload.png)
#### Рис.2 Завершенный POST-запрос на добавление файла

<br/><br/>
![](pic/sizelimit_upload.png)
#### Рис.3 Незавершенный POST-запрос на добавление файла (превышен размер файла)

<br/><br/>
![](pic/extlimit_upload.png)
#### Рис.4 Незавершенный POST-запрос на добавление файла (разрешение файла .txt или .csv)

<br/><br/>
![](pic/get_files.png)
#### Рис.5 GET-запрос (список файлов)

<br/><br/>
![](pic/download_file.png)
#### Рис.6 GET-запрос (загрузить файл)

<br/><br/>
- Для запуска программных тестов запустите IntelliJ IDEA и запустите тесты.

![](pic/tests.png)
#### Рис.7 Запуск тестов