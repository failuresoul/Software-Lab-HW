# University Management System (CSE 3230 – Lab 4 Home Task)

A small Spring Boot web app with three roles: **Admin**, **Teacher** and **Student**.

- **REST API**: Create / Read / Update / Delete for admins, teachers and students. The API sends JSON back with the right HTTP status code.
- **Spring Security**: login, logout, BCrypt password hashing and role-based access.
- **Docker**: a `Dockerfile` and a `compose.yaml` start the app and a MySQL database together.

Who can do what:

| Role    | /api/admins | /api/teachers | /api/students |
|---------|:-----------:|:-------------:|:-------------:|
| ADMIN   | yes         | yes           | yes           |
| TEACHER | no (403)    | yes           | no (403)      |
| STUDENT | no (403)    | no (403)      | yes           |

---

## 0. Start from zero (read this first)

This part assumes you know nothing yet. Follow the steps in order.

### 0.1 What you need to install

| Tool | Why | How to check it is installed |
|---|---|---|
| **Java JDK 17** | Spring Boot is Java, so you need Java to compile and run it | `java -version` should say `17` |
| **Maven** (optional) | Downloads the libraries and builds the project. IntelliJ already has Maven inside, so you only need it for terminal commands | `mvn -v` |
| **IntelliJ IDEA Community** (free) or **VS Code** | The editor where you open and run the project | – |
| **Docker Desktop** | Runs the app and MySQL in containers | `docker --version` and `docker compose version` |

Run the "check" commands in a terminal: **Command Prompt**, **PowerShell**, or the **Terminal** tab at the bottom of IntelliJ/VS Code.

**Installing Docker Desktop on Windows:**
1. Download it from https://www.docker.com/products/docker-desktop and install it.
2. If it asks to enable **WSL 2**, say yes and restart the PC.
3. Open **Docker Desktop** and wait until the bottom-left shows **"Engine running"** (green).
4. Docker commands only work while Docker Desktop is running.

### 0.2 What is Spring Boot? (simple answer)

- **Spring** is a big Java framework for building web apps.
- **Spring Boot** is Spring with ready-made settings. You add a "starter" library (web, security, database) and it works, with almost no setup.
- It has a **web server (Tomcat) built in**. Running the program starts a website on `http://localhost:8080`.

### 0.3 How is a new Spring Boot project created?

Every Spring Boot project starts from **Spring Initializr**.

1. Go to **https://start.spring.io**
2. Choose:
   - Project: **Maven**
   - Language: **Java**
   - Spring Boot: a 3.x version
   - Group: `com.university`  Artifact: `ums`
   - Packaging: **Jar**  Java: **17**
3. Click **ADD DEPENDENCIES** and add: **Spring Web**, **Spring Data JPA**, **Spring Security**, **MySQL Driver**.
4. Click **GENERATE**. A `.zip` file downloads.
5. Unzip it. That folder is your Spring Boot project.

(IntelliJ Ultimate has the same thing inside: *File → New → Project → Spring Boot*.)

### 0.4 Which files are created automatically?

When Spring Initializr makes a new project, you get these files:

| File | What it is |
|---|---|
| `pom.xml` | **The most important file.** It lists the project name, Java version and all libraries (dependencies). Maven reads it. |
| `src/main/java/.../UmsApplication.java` | **The main class.** It has `@SpringBootApplication` and the `main()` method. **This is the file that runs first** when the app starts. |
| `src/main/resources/application.properties` | The settings file (port, database...). Spring Boot **loads it automatically** at start. It is empty at first. |
| `src/main/resources/static/` | An empty folder for HTML/CSS/JS files. Files here are served to the browser as they are. |
| `src/main/resources/templates/` | For server-side HTML templates. We don't use it. |
| `src/test/java/.../UmsApplicationTests.java` | A sample test. We removed it to keep the project simple. |
| `mvnw`, `mvnw.cmd`, `.mvn/` | The **Maven Wrapper**. It lets you run Maven without installing it (`mvnw spring-boot:run`). We don't use it; we use installed Maven or IntelliJ's. |
| `.gitignore` | Tells Git which files not to upload (like `target/`). |
| `HELP.md` | Links to Spring guides. You can delete it. |

**If the teacher asks "which file runs automatically when you start a Spring Boot project?"**
The main class, **`UmsApplication.java`** (the one with `@SpringBootApplication` and `main()`). When it runs:
1. it reads **`pom.xml`** libraries (already built into the program),
2. it **automatically loads `application.properties`**,
3. it **automatically scans** all classes in its package and sub-packages (`@RestController`, `@Service`, `@Entity`, `@Configuration`...) and creates them,
4. it starts **Tomcat** on port 8080,
5. if someone opens `http://localhost:8080/`, it **automatically shows `static/index.html`**.

**Note:** when you *open* the project in IntelliJ, the editor usually shows the project tree. The file to open first and run is `UmsApplication.java`.

**The files we wrote ourselves** (not auto-generated): the `model`, `repository`, `controller` and `security` folders, `login.html`, `index.html`, `Dockerfile`, `compose.yaml`, `.dockerignore` and this README.

### 0.5 How to open THIS project in IntelliJ IDEA

1. Open IntelliJ → **File → Open...** (or **Open** on the welcome screen).
2. Select the folder **`Software Lab-HW`** (the folder that has `pom.xml` in it) → **OK**.
3. If it asks "Trust project?" → **Trust Project**.
4. IntelliJ sees `pom.xml` and treats it as a **Maven project**. At the bottom-right it shows "Importing / Downloading..." and downloads the libraries. **Wait until it finishes**. The first time takes a few minutes, and it needs internet.
5. Check the Java version: **File → Project Structure → Project → SDK = 17**. If 17 isn't in the list, choose *Add SDK → Download JDK → version 17*.
6. On the left, open `src/main/java/com/university/ums/UmsApplication.java`.
7. Click the green **▶** next to `public static void main` → **Run 'UmsApplication'**.
   - Before this, the database must be running: `docker compose up -d db` (see 0.7).
8. When the console shows `Started UmsApplication in ... seconds`, open **http://localhost:8080** in the browser.
9. To stop: the red **■** button in IntelliJ.

If the code is red or imports are not found: open the **Maven** tab on the right side and click 🔄 **Reload All Maven Projects**.

### 0.6 How to open THIS project in VS Code (another option)

1. Install the extensions **"Extension Pack for Java"** and **"Spring Boot Extension Pack"**.
2. **File → Open Folder...** → choose `Software Lab-HW`.
3. Wait for "Java: Ready" at the bottom bar.
4. Open `UmsApplication.java` and click **Run** above the `main` method.

### 0.7 Docker: the basic ideas

| Word | Simple meaning |
|---|---|
| **Image** | A packed "box" with everything needed to run a program (OS files + Java + our jar). Like a class. |
| **Container** | A running image. Like an object made from a class. |
| **Dockerfile** | A recipe that says **how to build an image** of our app. |
| **Docker Compose** (`compose.yaml`) | A file that **starts several containers together** (our app + MySQL) with one command. |
| **Volume** | A storage place outside the container, so database data isn't lost when the container stops. |
| **Port mapping** `8080:8080` | `PC port : container port`. It lets the browser on your PC reach the container. |

**What happens in our project:**
```
docker compose up --build
   │
   ├── db  container  ← made from the official image "mysql:8.0"
   │
   └── app container  ← made from OUR Dockerfile:
                          1) Maven builds ums.jar
                          2) Java runs ums.jar
                          3) the app connects to "db"
```

### 0.8 Docker: every command you need

Run these **inside the project folder** (where `compose.yaml` is). In IntelliJ, use the **Terminal** tab at the bottom, which is already in the right folder.

| Command | What it does |
|---|---|
| `docker compose up --build` | **Build and start everything** (app + database). Use this the first time and after every code change. Logs show in the terminal. Stop with `Ctrl + C`. |
| `docker compose up -d --build` | The same, but in the background (`-d` = detached), so the terminal stays free. |
| `docker compose up -d db` | Start **only the database**. Use it when you run the app from IntelliJ. |
| `docker compose ps` | Show which containers are running. |
| `docker compose logs app` | Show the app's logs. Add `-f` to keep following them. |
| `docker compose stop` | Stop the containers (they can be started again). |
| `docker compose down` | Stop **and remove** the containers. The data is kept in the volume. |
| `docker compose down -v` | Remove the containers **and the database data** for a fresh start. |
| `docker images` | List the images on your PC. |

**Using only the Dockerfile (without compose):** `docker build -t ums .` builds the image (`-t ums` is the name, `.` means "Dockerfile is in this folder"). On its own it can't run properly, because the app needs a database. That is why we use **compose**, which starts MySQL next to it. You can also see everything in **Docker Desktop → Containers**.

### 0.9 First time, full walkthrough

1. Start **Docker Desktop** and wait for "Engine running".
2. Open a terminal in the project folder and run `docker compose up --build`.
3. The first time, it downloads images and libraries, which takes a few minutes.
4. Wait for the line: `Started UmsApplication in ... seconds`.
5. Open **http://localhost:8080**. You are sent to the login page.
6. Log in as `admin` / `admin123`.
7. Choose **teachers** in the dropdown and fill in Name, Username, Password and subject → **Save**. "Saved! Status: 201" means it was created. **Remember the password you typed.** It is stored as a hash, so nobody can look it up later. If you forget it, the admin can **Edit** the teacher and type a new password.
8. Choose **students** and add one the same way.
9. Click **Logout**, then log in as the teacher you made. You only see **teachers**. If you try `http://localhost:8080/api/students`, you get **403 Forbidden**.
10. When finished: `Ctrl + C`, then `docker compose down`.

### 0.10 Common problems

| Problem | Fix |
|---|---|
| `docker: command not found` / `cannot connect to the Docker daemon` | Docker Desktop isn't installed or isn't running. Start it. |
| `port 8080 is already in use` | Another program uses 8080. Stop it, or stop the app running in IntelliJ. |
| `port 3307 is already in use` | Change `"3307:3306"` in `compose.yaml` to another port, like `"3308:3306"`. |
| App says `Communications link failure` | The database isn't ready yet. Docker restarts the app automatically, so wait a few seconds. If running from IntelliJ, start the DB first with `docker compose up -d db`. |
| Code changed but the app looks the same | Use `--build`: `docker compose up --build`. |
| IntelliJ shows red imports | Maven tab → 🔄 Reload, and check that the SDK is 17. |

### 0.11 Possible viva questions (short answers)

- **What is `pom.xml`?** The Maven file that lists the project info and libraries.
- **Which class starts the app?** `UmsApplication`, which has `@SpringBootApplication` and `main()`.
- **What does `@RestController` do?** It makes a class that answers HTTP requests with JSON.
- **What is an `@Entity`?** A Java class that becomes a database table.
- **What is a Repository?** An interface for database work. Spring writes the code (`findAll`, `save`, `deleteById`...).
- **REST verbs?** GET = read, POST = create, PUT = update, DELETE = delete.
- **Status codes we use?** 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized (not logged in), 403 Forbidden (wrong role), 404 Not Found, 409 Conflict.
- **How are passwords stored?** As **BCrypt hashes**, never as plain text (`BCryptPasswordEncoder`).
- **How does RBAC work here?** `SecurityConfig` says which role can open which URL (`hasRole` / `hasAnyRole`). `MyUserDetailsService` gives each user their role at login.
- **Difference between 401 and 403?** 401 = "who are you?" (not logged in). 403 = "I know you, but you are not allowed".
- **Dockerfile vs compose.yaml?** The Dockerfile builds **one image** (our app). compose.yaml **runs several containers together** (app + MySQL).
- **Why two `FROM` lines in the Dockerfile?** It is a multi-stage build. The first stage builds the jar with Maven, and the second only runs it with Java, so the final image is smaller.
- **How does the app find MySQL in Docker?** By the service name `db` (`jdbc:mysql://db:3306/...`). Compose puts both containers on one network.

---

## 1. How to run

### With Docker (recommended)

```
docker compose up --build
```

Then open **http://localhost:8080** and log in with:

- username: `admin`
- password: `admin123`

This first admin is created on startup. Use it to add teachers and students, then log in as them to see that they only get their own resource.

To stop: `Ctrl + C`, then `docker compose down`. To also delete the database data, use `docker compose down -v`.

### Without Docker for the app (run it from the IDE or Maven)

The app needs a MySQL database. The easiest way is to start **only the database** with Docker:

```
docker compose up -d db
```

Then run the app from IntelliJ (the green ▶ next to `main` in `UmsApplication.java`) or with:

```
mvn spring-boot:run
```

By default the app looks for MySQL at `localhost:3307`, user `root`, password `root`. That is exactly the Docker database above. If you use your own installed MySQL on port 3306 instead, set an environment variable before running:
`DB_URL=jdbc:mysql://localhost:3306/university_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false`

---

## 2. Testing the API (Postman or curl)

The API also accepts **HTTP Basic** login, so you can send a username and password with each request:

```
# list students (as admin)
curl -u admin:admin123 http://localhost:8080/api/students

# create a teacher -> 201 Created
curl -u admin:admin123 -X POST -H "Content-Type: application/json" \
     -d '{"name":"Rahim","username":"t1","password":"123","subject":"Math"}' \
     http://localhost:8080/api/teachers

# a teacher trying to read students -> 403 Forbidden
curl -u t1:123 http://localhost:8080/api/students

# update student 1 -> 200 OK
curl -u admin:admin123 -X PUT -H "Content-Type: application/json" \
     -d '{"name":"Karim","username":"s1","password":"","department":"EEE"}' \
     http://localhost:8080/api/students/1

# delete student 1 -> 204 No Content
curl -u admin:admin123 -X DELETE http://localhost:8080/api/students/1
```

### All endpoints

`{type}` is `admins`, `teachers` or `students`.

| Method | URL                | What it does         | Success code   | Error codes |
|--------|--------------------|----------------------|----------------|-------------|
| GET    | /api/{type}        | list all             | 200 OK         | 401, 403 |
| GET    | /api/{type}/{id}   | get one              | 200 OK         | 404 |
| POST   | /api/{type}        | create new           | 201 Created    | 400 (no username/password), 409 (username taken) |
| PUT    | /api/{type}/{id}   | update               | 200 OK         | 404, 409 |
| DELETE | /api/{type}/{id}   | delete               | 204 No Content | 404 |
| GET    | /api/me            | who is logged in     | 200 OK         | 401 |
| POST   | /login             | log in (form)        | 302 to /index.html | 302 to /login.html?error |
| GET    | /logout            | log out              | 302 to /login.html?logout | |

- **401 Unauthorized** = you are not logged in, or the password is wrong.
- **403 Forbidden** = you are logged in, but your role is not allowed to do this.

Example JSON bodies:

```json
{ "name": "Super Admin", "username": "admin2", "password": "pass" }
{ "name": "Rahim", "username": "t1", "password": "123", "subject": "Math" }
{ "name": "Karim", "username": "s1", "password": "123", "department": "CSE" }
```

The password is never sent back in a response. It is stored in the database as a BCrypt hash, for example `$2a$10$...`.

---

## 3. Project structure

```
Software Lab-HW
├── pom.xml                         -> Maven: project info + libraries
├── Dockerfile                      -> how to build the app image
├── compose.yaml                    -> runs app + MySQL together
├── .dockerignore                   -> files Docker should not copy
├── README.md                       -> this file
└── src/main
    ├── java/com/university/ums
    │   ├── UmsApplication.java     -> starting point + creates first admin
    │   ├── model                   -> database tables
    │   │   ├── Admin.java
    │   │   ├── Teacher.java
    │   │   └── Student.java
    │   ├── repository              -> database access (save, find, delete)
    │   │   ├── AdminRepository.java
    │   │   ├── TeacherRepository.java
    │   │   └── StudentRepository.java
    │   ├── controller              -> REST API endpoints
    │   │   ├── AdminController.java
    │   │   ├── TeacherController.java
    │   │   ├── StudentController.java
    │   │   └── MeController.java
    │   └── security                -> login + roles
    │       ├── SecurityConfig.java
    │       └── MyUserDetailsService.java
    └── resources
        ├── application.properties  -> settings (port, database)
        └── static                  -> plain HTML pages
            ├── login.html
            └── index.html
```

How a request moves through the app:

```
Browser / Postman
      │
      ▼
Spring Security  (are you logged in? is your role allowed?)  ──no──► 401 / 403
      │ yes
      ▼
Controller  (AdminController / TeacherController / StudentController)
      │
      ▼
Repository  (AdminRepository / ...)
      │
      ▼
MySQL database  (tables: admin, teacher, student)
```

---

## 4. Every file explained line by line

### 4.1 `pom.xml`

Maven reads this file to learn which libraries to download and how to build the project.

| Line(s) | Meaning |
|---|---|
| `<?xml version="1.0" ...?>` | Says this is an XML file. |
| `<project xmlns=... >` | The root tag of every Maven file. The `xmlns` parts are standard boilerplate. |
| `<modelVersion>4.0.0</modelVersion>` | The version of the POM format. It is always 4.0.0. |
| `<parent> ... spring-boot-starter-parent 3.3.5 </parent>` | We take default settings from Spring Boot 3.3.5, so we don't have to write a version number for every library. |
| `<groupId>com.university</groupId>` | Our "company/organisation" name. |
| `<artifactId>ums</artifactId>` | The project name (ums = University Management System). |
| `<version>1.0</version>` | Our project's version. |
| `<name>University Management System</name>` | A human-readable name. |
| `<java.version>17</java.version>` | Compile with Java 17. |
| `spring-boot-starter-web` | Library for making a web server and REST API. It includes Tomcat and the JSON converter (Jackson). |
| `spring-boot-starter-data-jpa` | Library for working with the database using Java classes instead of SQL. It includes Hibernate. |
| `spring-boot-starter-security` | Library for login, logout, password hashing and roles. |
| `mysql-connector-j` (scope `runtime`) | The driver that lets Java talk to MySQL. It is only needed when the app runs. |
| `<finalName>ums</finalName>` | The built file will be named `target/ums.jar`. The Dockerfile uses this name. |
| `spring-boot-maven-plugin` | Builds one runnable "fat" jar that contains all libraries, so `java -jar ums.jar` just works. |

---

### 4.2 `UmsApplication.java`

```java
package com.university.ums;
```
This file lives in the package (folder) `com.university.ums`.

```java
import com.university.ums.model.Admin;
import com.university.ums.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
```
These lines bring in the classes we use below, so we can write `Admin` instead of `com.university.ums.model.Admin`.

```java
@SpringBootApplication
```
Marks this as the main Spring Boot class. It turns on auto-configuration and tells Spring to scan this package and its sub-packages for controllers, services and so on.

```java
public class UmsApplication {
```
The class definition.

```java
    public static void main(String[] args) {
        SpringApplication.run(UmsApplication.class, args);
    }
```
The Java `main` method, where the program starts. `SpringApplication.run` starts Spring, which starts the web server on port 8080 and connects to the database.

```java
    // creates a first admin so we can log in the very first time
    @Bean
    CommandLineRunner createDefaultAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
```
`@Bean` hands this object to Spring. A `CommandLineRunner` is code that Spring runs **once, right after the app starts**. Spring passes in the `AdminRepository` (database access) and the `PasswordEncoder` (BCrypt) automatically.

```java
        return args -> {
            if (adminRepository.count() == 0) {
```
If there are **no admins** in the database yet...

```java
                Admin admin = new Admin();
                admin.setName("Default Admin");
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                adminRepository.save(admin);
```
...make a new admin with username `admin` and password `admin123`, and save it. `passwordEncoder.encode(...)` turns the password into a BCrypt hash, so the real password is never stored.

```java
            }
        };
    }
}
```
Closing brackets.

---

### 4.3 `model/Admin.java`, `model/Teacher.java`, `model/Student.java`

A **model** (also called an **entity**) is a Java class that becomes a **database table**. Each object is one row. The three files are almost the same, so here is `Admin.java` explained line by line, followed by the differences.

```java
package com.university.ums.model;
```
This file is in the `model` package.

```java
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
```
`jakarta.persistence.*` are the JPA (database) annotations. `JsonProperty` comes from Jackson, the JSON library.

```java
@Entity
public class Admin {
```
`@Entity` tells JPA: "make a table called `admin` for this class."

```java
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
```
`@Id` makes `id` the **primary key**. `@GeneratedValue(IDENTITY)` makes the database give each new row the next number (1, 2, 3, ...) automatically (AUTO_INCREMENT in MySQL).

```java
    private String name;
```
A normal column called `name`.

```java
    @Column(unique = true)
    private String username;
```
The `username` column. `unique = true` means two admins can't have the same username.

```java
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
```
The `password` column, which stores the BCrypt hash. `WRITE_ONLY` means the password can be **received** in JSON (when creating or updating), but it is **never sent back** in a JSON response. This keeps the hashes hidden.

```java
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    ... (same for name, username, password)
```
**Getters and setters.** The fields are `private`, so other classes read them with `getX()` and change them with `setX(...)`. JPA and Jackson also use these methods to fill the object and turn it into JSON.

**Differences in the other two files:**
- `Teacher.java` has the same fields plus `private String subject;` (with `getSubject` and `setSubject`). It becomes the `teacher` table.
- `Student.java` has the same fields plus `private String department;` (with `getDepartment` and `setDepartment`). It becomes the `student` table.

---

### 4.4 `repository/AdminRepository.java` (and Teacher / Student)

A **repository** is how we talk to the database. We only write an **interface**, and Spring Data JPA writes the real code for us.

```java
package com.university.ums.repository;

import com.university.ums.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
```
Package and imports.

```java
public interface AdminRepository extends JpaRepository<Admin, Long> {
```
By extending `JpaRepository<Admin, Long>` (where `Admin` is the entity and `Long` is the type of its id), we get these methods for free:
`findAll()`, `findById(id)`, `save(obj)`, `deleteById(id)`, `existsById(id)` and `count()`.

```java
    Optional<Admin> findByUsername(String username);
```
Spring reads the **method name** and builds the query `SELECT * FROM admin WHERE username = ?` by itself. `Optional` means "maybe there is an admin, maybe there isn't". It is used at login.

```java
    boolean existsByUsername(String username);
```
Also built from the name. It returns `true` if the username is already taken. It is used to send `409 Conflict`.

`TeacherRepository` and `StudentRepository` are exactly the same, with `Teacher` / `Student` in place of `Admin`.

---

### 4.5 `controller/AdminController.java` (and Teacher / Student)

A **controller** receives the HTTP requests and sends back responses. This is the REST API. The three controllers are the same except for the class names and the extra field. Here is `AdminController` line by line.

```java
package com.university.ums.controller;

import com.university.ums.model.Admin;
import com.university.ums.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
```
Package and imports. `web.bind.annotation.*` brings in `@RestController`, `@GetMapping` and the other mapping annotations.

```java
@RestController
@RequestMapping("/api/admins")
public class AdminController {
```
`@RestController` means every method here returns data (turned into **JSON** automatically), not an HTML page. `@RequestMapping("/api/admins")` means every URL in this class starts with `/api/admins`.

```java
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }
```
**Constructor injection.** When Spring creates the controller, it passes in the repository and the password encoder for us. We keep them in fields so we can use them later.

#### READ all: `GET /api/admins`
```java
    @GetMapping
    public List<Admin> getAll() {
        return adminRepository.findAll();
    }
```
Gets every admin from the database and returns them as a JSON array. The status is **200 OK**.

#### READ one: `GET /api/admins/{id}`
```java
    @GetMapping("/{id}")
    public ResponseEntity<Admin> getOne(@PathVariable Long id) {
```
`{id}` is a value in the URL. `@PathVariable` puts it into the `id` variable, so `/api/admins/5` gives `id = 5`. `ResponseEntity` lets us choose the status code ourselves.

```java
        Optional<Admin> admin = adminRepository.findById(id);
        if (admin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(admin.get());
    }
```
Look up the admin. If none is found, return **404 Not Found**. Otherwise return **200 OK** with the admin as JSON.

#### CREATE: `POST /api/admins`
```java
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Admin admin) {
```
`@RequestBody` turns the JSON in the request into an `Admin` object. `ResponseEntity<?>` means the body can be any type: either an Admin or an error message.

```java
        if (admin.getUsername() == null || admin.getUsername().isBlank()
                || admin.getPassword() == null || admin.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password are required"));
        }
```
If the username or password is missing or empty, return **400 Bad Request** with a JSON error like `{"error": "..."}`.

```java
        if (adminRepository.existsByUsername(admin.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
```
If the username is already taken, return **409 Conflict**.

```java
        admin.setId(null);
```
Ignore any `id` the user sent, so the database makes a new one. This stops someone from overwriting an existing row with POST.

```java
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
```
**Password hashing.** Replace the plain password with its BCrypt hash before saving.

```java
        Admin saved = adminRepository.save(admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
```
Save to the database (this is an INSERT) and return **201 Created** with the new admin, which now has its id.

#### UPDATE: `PUT /api/admins/{id}`
```java
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Admin newData) {
        Optional<Admin> found = adminRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Admin admin = found.get();
```
Get the id from the URL and the new values from the JSON body. If no admin has this id, return **404**. Otherwise `admin` is the old row from the database.

```java
        if (!admin.getUsername().equals(newData.getUsername()) && adminRepository.existsByUsername(newData.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
```
If the username is being **changed** to one that someone else already has, return **409 Conflict**.

```java
        admin.setName(newData.getName());
        admin.setUsername(newData.getUsername());
```
Copy the new values onto the old object.

```java
        if (newData.getPassword() != null && !newData.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(newData.getPassword()));
        }
```
Only change the password if a new one was sent, and hash it first. If the password is empty, the old password stays.

```java
        return ResponseEntity.ok(adminRepository.save(admin));
    }
```
`save` on an object that already has an id does an UPDATE. Return **200 OK** with the updated admin.

#### DELETE: `DELETE /api/admins/{id}`
```java
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!adminRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        adminRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```
If the admin doesn't exist, return **404**. Otherwise delete it and return **204 No Content**, which means "done, nothing to send back". `Void` means there is no body.

**Differences in the other two controllers:**
- `TeacherController`: URL `/api/teachers`, uses `Teacher` and `TeacherRepository`, and the update also copies `teacher.setSubject(newData.getSubject());`.
- `StudentController`: URL `/api/students`, uses `Student` and `StudentRepository`, and the update also copies `student.setDepartment(newData.getDepartment());`.

**Note:** The controllers do **not** check roles themselves. `SecurityConfig` blocks wrong roles before the request ever reaches the controller.

---

### 4.6 `controller/MeController.java`

```java
@RestController
public class MeController {
```
A small REST controller.

```java
    // tells the web page who is logged in and what role they have
    @GetMapping("/api/me")
    public Map<String, String> me(Authentication authentication) {
```
Handles `GET /api/me`. Spring passes in `Authentication`, which holds information about the logged-in user.

```java
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
```
Get the user's first (and only) role, for example `ROLE_TEACHER`, and remove the `ROLE_` prefix, leaving `TEACHER`.

```java
        return Map.of("username", authentication.getName(), "role", role);
    }
}
```
Returns JSON such as `{"username":"t1","role":"TEACHER"}`. `index.html` uses this to decide which resources to show in the dropdown.

---

### 4.7 `security/SecurityConfig.java`

This file holds the main Spring Security settings.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
```
`@Configuration` means this class defines settings and beans. `@EnableWebSecurity` turns on Spring Security for web requests.

```java
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
```
**Password hashing.** It creates one BCrypt encoder that the whole app shares. BCrypt is a one-way hash with a random "salt", so the same password gives a different hash each time and can't be turned back into the password. At login, Spring hashes the typed password the same way and compares.

```java
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
```
`SecurityFilterChain` is the list of security checks that every request passes through. We build it with `http`.

```java
            .csrf(csrf -> csrf.disable())
```
This turns off CSRF protection. CSRF tokens matter for normal web forms, but they would make it harder to test the REST API from Postman or curl. We turn it off to keep things simple, which is common in student REST projects.

```java
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login.html", "/error").permitAll()
```
**Role-based access control (RBAC) starts here.** Anyone can open the login page and the error page without logging in.

```java
                .requestMatchers("/api/admins/**").hasRole("ADMIN")
```
Only **ADMIN** can use `/api/admins` and everything under it.

```java
                .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
```
**ADMIN** and **TEACHER** can manage teachers.

```java
                .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "STUDENT")
```
**ADMIN** and **STUDENT** can manage students.

```java
                .anyRequest().authenticated()
            )
```
Every other URL (like `/index.html` or `/api/me`) needs you to be logged in, with any role.
If you are not logged in you get **401** (or a redirect to the login page in a browser). If your role is wrong you get **403**.

```java
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index.html", true)
                .failureUrl("/login.html?error")
                .permitAll()
            )
```
**Login.**
- `loginPage`: our own login page.
- `loginProcessingUrl("/login")`: the form sends username and password to `POST /login`, and Spring checks them for us. We don't write that code.
- `defaultSuccessUrl`: after a correct login, go to `index.html`.
- `failureUrl`: after a wrong password, go back to the login page with `?error`.
- `permitAll`: anyone can reach these login URLs.

```java
            .httpBasic(Customizer.withDefaults())
```
This also allows **HTTP Basic** login (username and password in a header). Postman and `curl -u user:pass` use it.

```java
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.html?logout")
                .permitAll()
            );
```
**Logout.** Opening `/logout` ends the session (you are logged out), then goes to the login page with `?logout`.

```java
        return http.build();
    }
}
```
Build the filter chain and give it to Spring.

---

### 4.8 `security/MyUserDetailsService.java`

At login, Spring Security asks a `UserDetailsService`: "give me the user with this username." Our users are in **three** tables, so we check all three.

```java
@Service
public class MyUserDetailsService implements UserDetailsService {
```
`@Service` registers this class with Spring. Because it implements `UserDetailsService`, Spring Security uses it automatically at login.

```java
    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public MyUserDetailsService(AdminRepository adminRepository,
                                TeacherRepository teacherRepository,
                                StudentRepository studentRepository) {
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }
```
Spring gives us all three repositories through the constructor.

```java
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
```
Spring calls this method with the username typed on the login form.

```java
        Optional<Admin> admin = adminRepository.findByUsername(username);
        if (admin.isPresent()) {
            return User.withUsername(username).password(admin.get().getPassword()).roles("ADMIN").build();
        }
```
First look in the `admin` table. If the user is found there, build a Spring Security `User` with:
- the username,
- the **hashed** password from the database (Spring compares it with the typed password using BCrypt),
- the role `ADMIN`. Spring stores this as `ROLE_ADMIN`, and that is what `hasRole("ADMIN")` checks.

```java
        Optional<Teacher> teacher = teacherRepository.findByUsername(username);
        if (teacher.isPresent()) {
            return User.withUsername(username).password(teacher.get().getPassword()).roles("TEACHER").build();
        }
```
If the user isn't an admin, look in the `teacher` table and give the role `TEACHER`.

```java
        Optional<Student> student = studentRepository.findByUsername(username);
        if (student.isPresent()) {
            return User.withUsername(username).password(student.get().getPassword()).roles("STUDENT").build();
        }
```
If the user isn't a teacher either, look in the `student` table and give the role `STUDENT`.

```java
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
```
If the user is in none of the tables, the login fails.

---

### 4.9 `resources/application.properties`

Settings that Spring Boot reads when it starts.

| Line | Meaning |
|---|---|
| `server.port=8080` | The web server listens on port 8080. |
| `spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3307/university_db?...}` | The database address. `${DB_URL:default}` means: use the environment variable `DB_URL` if it is set (Docker sets it), otherwise use `localhost:3307`. That is the Docker MySQL started with `docker compose up -d db`. `createDatabaseIfNotExist=true` creates the database if needed. `allowPublicKeyRetrieval=true&useSSL=false` lets the driver log in to MySQL 8 without SSL. |
| `spring.datasource.username=${DB_USERNAME:root}` | The database user (default `root`). |
| `spring.datasource.password=${DB_PASSWORD:root}` | The database password (default `root`). |
| `spring.jpa.hibernate.ddl-auto=update` | Hibernate **creates or updates the tables automatically** from our `@Entity` classes, so we don't need any SQL file. |
| `spring.jpa.show-sql=false` | Don't print every SQL query in the console. Set it to `true` if you want to see them. |

---

### 4.10 `resources/static/login.html`

Files in `static/` are served as they are. This is a plain HTML login page with no CSS.

| Line(s) | Meaning |
|---|---|
| `<!DOCTYPE html> <html> <head> <title>Login</title> </head>` | A normal HTML start, with the browser tab title "Login". |
| `<h2>University Management System - Login</h2>` | The heading. |
| `<p id="msg"></p>` | An empty paragraph where we show "wrong password" or "logged out". |
| `<form action="/login" method="post">` | When submitted, the form sends the data to `POST /login`, which Spring Security handles (see SecurityConfig). |
| `Username: <input type="text" name="username">` | The username box. The name **must** be `username`, because that is what Spring expects. |
| `Password: <input type="password" name="password">` | The password box. The name **must** be `password`. `type="password"` hides the letters. |
| `<button type="submit">Login</button>` | Submits the form. |
| `if (location.search.includes("error")) {...}` | If the URL has `?error` (wrong login), show "Wrong username or password!". |
| `if (location.search.includes("logout")) {...}` | If the URL has `?logout`, show "You have been logged out." |

---

### 4.11 `resources/static/index.html`

The main page after login. It is plain HTML plus a little JavaScript that calls our REST API with `fetch`.

**HTML part**

| Line(s) | Meaning |
|---|---|
| `<h2>University Management System</h2>` | The heading. |
| `<p id="me"></p>` | Will show "Logged in as X (ROLE)". |
| `<a href="/logout">Logout</a>` | A logout link. It calls Spring's `/logout`. |
| `<select id="resource" onchange="loadList()">` | A dropdown to pick admins, teachers or students. Changing it reloads the table. |
| `<button onclick="loadList()">Refresh</button>` | Reloads the list. |
| `<table border="1" id="table"></table>` | An empty table. JavaScript fills it with data. |
| `<input type="hidden" id="id">` | A hidden box that holds the id of the row being edited. If it is empty, we are adding a new row. |
| `Name / Username / Password` inputs | Form boxes for the data. `autocomplete="off"` and `autocomplete="new-password"` stop the browser from auto-filling your own saved login into these boxes. Otherwise a new teacher or student could secretly get the wrong password. |
| `<span id="extraRow">...<input id="extra">` | The box for the extra field (`subject` for teachers, `department` for students). It is hidden for admins. |
| `Save` / `Clear` buttons | Save calls `save()`. Clear empties the form. |
| `<p id="msg"></p>` | Shows the HTTP status after save or delete (for example `201`, `403`). |

**JavaScript part**

| Code | Meaning |
|---|---|
| `var extraField = { admins: null, teachers: "subject", students: "department" };` | Which extra field each resource has. |
| `var items = [];` | Keeps the last loaded list so the Edit button can find a row. |
| `fetch("/api/me")...` | When the page opens, ask the server who is logged in. |
| `document.getElementById("me").innerText = "Logged in as ..."` | Show the username and role. |
| `if (me.role == "ADMIN") allowed = [...]` (3 lines) | Choose which resources this role may see: admin gets all 3, teacher only teachers, student only students. The server enforces this too; the page just hides what you can't use. |
| `for (...) select.innerHTML += "<option>..."` | Put the allowed resources in the dropdown. |
| `loadList();` | Load the first table. |
| **`function loadList()`** | Loads the table. |
| `var resource = ...value;` | The selected resource, for example `teachers`. |
| `...extraRow.style.display = extra ? "" : "none";` | Show the extra box only if this resource has an extra field. |
| `...extraLabel.innerText = extra;` | Set the label text to "subject" or "department". |
| `clearForm();` | Empty the form. |
| `fetch("/api/" + resource)...` | `GET /api/teachers` (for example). It returns a JSON list. |
| `items = list;` | Remember the list. |
| `var html = "<tr><th>ID</th>..."` | Build the table header row. |
| `for (...) { html += "<tr><td>" + list[i].id ... }` | Add one table row per item, with Edit and Delete buttons. |
| `document.getElementById("table").innerHTML = html;` | Put the rows on the page. |
| **`function editItem(i)`** | Copy row `i` into the form so you can change it. The password is left empty, which keeps the old one. |
| **`function deleteItem(id)`** | Ask "Delete id X?". If OK, send `DELETE /api/{resource}/{id}`, show the status (204), and reload. |
| **`function save()`** | Save the form. |
| `var data = { name, username, password };` | Build the JSON object from the form. |
| `if (extra) data[extra] = ...` | Add `subject` or `department` if needed. |
| `var url = "/api/" + resource; var method = "POST";` | By default, create a new item... |
| `if (id) { url = url + "/" + id; method = "PUT"; }` | ...but if an id is in the hidden box, update that item instead. |
| `fetch(url, { method, headers: {"Content-Type": "application/json"}, body: JSON.stringify(data) })` | Send the request with a JSON body. |
| `if (res.ok) { ... "Saved! Status: " ...; loadList(); }` | If it worked (201 or 200), show "Saved!" and reload the table. |
| `else { res.text().then(... "Error " + res.status + ": " + text ...) }` | If it failed (400, 403, 409...), show the status and the server's error message, for example `Error 409: {"error":"username already exists"}`. The form is kept, so you can fix it. |
| **`function clearForm()`** | Empty all the form boxes. |

The browser already holds the login session cookie, so `fetch` calls are automatically logged in.

---

### 4.12 `Dockerfile`

A recipe for building a Docker **image** of our app. It uses two stages so the final image is small.

| Line | Meaning |
|---|---|
| `# Step 1: build the jar file using Maven` | A comment. |
| `FROM maven:3.9-eclipse-temurin-17 AS build` | Start from an image that already has Maven and Java 17. We call this stage `build`. |
| `WORKDIR /app` | Work inside the `/app` folder in the container. |
| `COPY pom.xml .` | Copy `pom.xml` into the container. |
| `COPY src ./src` | Copy the source code into the container. |
| `RUN mvn -q package -DskipTests` | Build the project, which creates `target/ums.jar`. `-q` means quiet, and `-DskipTests` skips tests. |
| `# Step 2: run the jar file using only Java` | A comment. |
| `FROM eclipse-temurin:17-jre` | Start a **new**, smaller image with only the Java runtime (no Maven, no source code). |
| `WORKDIR /app` | Work inside `/app`. |
| `COPY --from=build /app/target/ums.jar app.jar` | Copy only the built jar from the first stage. |
| `EXPOSE 8080` | Says the app uses port 8080. This is documentation for Docker. |
| `ENTRYPOINT ["java", "-jar", "app.jar"]` | The command that runs when the container starts. It starts the app. |

---

### 4.13 `compose.yaml`

Runs **two services together**: the MySQL database and our app.

| Line(s) | Meaning |
|---|---|
| `services:` | The list of containers. |
| `db:` | The first service, named `db`. |
| `image: mysql:8.0` | Use the official MySQL 8 image from Docker Hub. |
| `MYSQL_ROOT_PASSWORD: root` | Set the MySQL root password to `root`. |
| `MYSQL_DATABASE: university_db` | Create the database `university_db` on first start. |
| `ports: - "3307:3306"` | Your PC's port 3307 goes to MySQL's port 3306 inside the container, so you can open it with a DB tool. We use 3307 so it doesn't clash with a MySQL already installed on your PC. |
| `volumes: - db_data:/var/lib/mysql` | Keep the database files in a named volume, so the data survives restarts. |
| `healthcheck:` ... `mysqladmin ping` | Every 5 seconds, check if MySQL is ready, up to 20 times. |
| `app:` | The second service, our Spring Boot app. |
| `build: .` | Build the image from the `Dockerfile` in this folder. |
| `ports: - "8080:8080"` | Open the app at `http://localhost:8080`. |
| `DB_URL: jdbc:mysql://db:3306/university_db?...` | Tells the app where the database is. The host name is **`db`**, the service name above, because containers in one compose file can reach each other by name. This replaces the default in `application.properties`. |
| `DB_USERNAME: root` / `DB_PASSWORD: root` | Database login for the app. |
| `depends_on: db: condition: service_healthy` | Start the app only after MySQL's health check passes. |
| `restart: on-failure` | If the app crashes (for example, if MySQL wasn't fully ready yet), Docker starts it again. |
| `volumes: db_data:` | Declares the named volume used above. |

---

### 4.14 `.dockerignore`

Tells Docker which files **not** to copy into the build: the `target` build folder and IDE settings (`.idea`, `.vscode`, `*.iml`). This makes building faster and cleaner.

---

## 5. Requirement checklist

| Requirement | Where |
|---|---|
| CRUD for Admin, Teacher, Student | `AdminController`, `TeacherController`, `StudentController` |
| REST conventions (GET/POST/PUT/DELETE, `/api/{resource}/{id}`) | controllers |
| JSON responses | `@RestController` + Jackson |
| HTTP status codes 200, 201, 204, 400, 401, 403, 404, 409 | controllers + Spring Security |
| Login / logout | `SecurityConfig` (`formLogin`, `httpBasic`, `logout`), `login.html` |
| Password hashing | `BCryptPasswordEncoder` in `SecurityConfig`, used in controllers and `UmsApplication` |
| Role-based access control | `requestMatchers(...).hasRole/hasAnyRole` in `SecurityConfig`, roles given in `MyUserDetailsService` |
| Dockerfile | `Dockerfile` |
| Docker Compose (app + database together) | `compose.yaml` |
