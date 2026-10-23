# University Management System
### CSE 3230 – Home Task (Lab 4) · Java Spring Boot

A simple web application with three user roles: **Admin**, **Teacher** and **Student**.

| Requirement from the task | How this project does it |
|---|---|
| **REST API**: CRUD for Admin, Teacher, Student with JSON and HTTP status codes | `AdminController`, `TeacherController`, `StudentController` (GET / POST / PUT / DELETE). Also `CourseController` and `RegistrationController` |
| **Spring Security**: login, logout, password hashing, role-based access | `SecurityConfig`, `MyUserDetailsService`, BCrypt, login page with role selection |
| **Dockerization**: Dockerfile + compose.yaml | `Dockerfile` (builds the app) + `compose.yaml` (runs app + MySQL together) |

### Who can manage what (the task's rule)

| Logged in as | Admins | Teachers | Students |
|---|:---:|:---:|:---:|
| **Admin**   | ✅ | ✅ | ✅ |
| **Teacher** | ❌ 403 | ✅ | ❌ 403 |
| **Student** | ❌ 403 | ❌ 403 | ✅ |

Inside their own section, teachers and students work like in a real university:

| Action | Admin | Teacher (in Teachers) | Student (in Students) |
|---|---|---|---|
| **Read**: list of all records | ✅ all three lists | ✅ all teachers | ✅ all students |
| **Create**: add a new record | ✅ | ❌ only the admin creates accounts | ❌ only the admin creates accounts |
| **Update**: edit a record | ✅ anyone | ✅ **only their own account** | ✅ **only their own account** |
| **Delete**: remove a record | ✅ anyone **except themselves** | ✅ **only their own account** (then logged out) | ✅ **only their own account** (then logged out) |

### Extra realistic work: Courses and Course Registration

| | Admin | Teacher | Student |
|---|---|---|---|
| **Courses** | full CRUD, chooses the teacher of a course | sees all courses; **adds their own courses**; edits and deletes **only their own** | sees all courses and clicks **Register** |
| **Registrations** | full CRUD | sees **the students registered in their courses** (read only) | **My Registrations**: register, change section, drop a course |

Breaking any rule gives **403 Forbidden** from the server. The web page also shows only the buttons you are allowed to use.

---

## Table of contents
1. [Ready-made accounts and data](#1-ready-made-accounts-and-data)
2. [How to run in IntelliJ IDEA (step by step)](#2-how-to-run-in-intellij-idea-step-by-step)
3. [How to run with Docker](#3-how-to-run-with-docker)
4. [How to use the app](#4-how-to-use-the-app)
5. [REST API reference](#5-rest-api-reference)
6. [Project structure](#6-project-structure)
7. [Spring Boot basics (for viva)](#7-spring-boot-basics-for-viva)
8. [Every file explained line by line](#8-every-file-explained-line-by-line)
9. [Docker explained](#9-docker-explained)
10. [Viva questions and answers](#10-viva-questions-and-answers)
11. [Common problems](#11-common-problems)

---

## 1. Ready-made accounts and data

When the app starts with empty tables, `DataLoader.java` adds this sample data:

| Login as | Username | Password |
|---|---|---|
| Admin   | `admin`    | `admin123`   |
| Teacher | `teacher1` | `teacher123` |
| Student | `student1` | `student123` |

| Course | Title | Credit | Semester | Teacher |
|---|---|---|---|---|
| CSE3230 | Software Development Lab | 1.5 | 3-2 | teacher1 |
| CSE3201 | Operating Systems | 3.0 | 3-2 | teacher1 |

There is also one registration: **student1** in **CSE3230**, section **A**.

On the login page, **choose the correct role** in "Login as". A teacher who chooses "Student" gets *"Wrong username, password or role!"*.

---

## 2. How to run in IntelliJ IDEA (step by step)

You **don't need to install any database** to run in IntelliJ. The app uses **H2**, a small database saved as a file in the `data` folder of the project.

### What you need
- **IntelliJ IDEA** (the free Community edition is enough)
- **Java JDK 17** (IntelliJ can download it for you, see step 4)
- An internet connection the first time, to download the libraries

### Steps

1. **Open IntelliJ IDEA.**
2. **File → Open...** (or **Open** on the welcome screen) and select the folder **`Software Lab-HW`**, the one that contains `pom.xml`. Click **OK**.
3. If it asks *"Trust and open project?"*, click **Trust Project**.
4. **Set Java 17:** go to **File → Project Structure → Project**, then **SDK**, and choose **17**.
   If 17 is not in the list, choose **Add SDK → Download JDK...**, pick version **17** and click **Download**. Then click **OK**.
5. **Wait for Maven.** IntelliJ reads `pom.xml` and downloads all libraries (progress bar at the bottom right). The first time takes a few minutes.
   If the code shows red errors or "cannot resolve symbol", open the **Maven** tab on the right side and click 🔄 **Reload All Maven Projects**.
6. In the **Project** panel on the left, open:
   `src → main → java → com.university.ums → UmsApplication.java`
7. Click the green **▶** next to `public class UmsApplication` (or next to `main`) and choose **Run 'UmsApplication.main()'**.
8. The **Run** window opens at the bottom. Wait for this line:
   ```
   Started UmsApplication in 5.123 seconds
   ```
9. Open your browser at **http://localhost:8081**
10. Log in, for example *Login as* **Admin**, username `admin`, password `admin123`.
11. **To stop the app:** click the red **■** (Stop) button in the Run window.

> **Why port 8081 and not 8080?** Other programs often use port 8080 (Oracle Database uses it on this PC), so this project uses **8081**.

> **Where is the data?** In the `data` folder in the project (`data/ums.mv.db`). It stays after you stop the app. To start again with fresh data, stop the app, delete the `data` folder and run again. The sample data from section 1 is created again.

### Run with Maven from the terminal (optional)
In IntelliJ's **Terminal** tab (at the bottom), type:
```
mvn spring-boot:run
```
This needs Maven installed. The IntelliJ ▶ button doesn't.

---

## 3. How to run with Docker

With Docker, the app uses a real **MySQL** database instead of H2. Docker Compose starts **two containers together**: MySQL and the app.

1. Install **Docker Desktop** from https://www.docker.com/products/docker-desktop. On Windows, accept **WSL 2** if it asks, then restart the PC.
2. Start **Docker Desktop** and wait until it shows **"Engine running"**.
3. **Stop the app in IntelliJ if it is running.** Both use port 8081.
4. Open a terminal in the project folder (IntelliJ's **Terminal** tab is already in the right place) and run:
   ```
   docker compose up --build
   ```
5. The first time, Docker downloads MySQL, Java and Maven, which takes a few minutes. Wait for `Started UmsApplication`.
6. Open **http://localhost:8081** and log in with the accounts from section 1.
7. To stop: press `Ctrl + C`, then run `docker compose down`.

All Docker commands are explained in [section 9](#9-docker-explained).

---

## 4. How to use the app

### Login page
1. **Login as**: choose Admin, Teacher or Student.
2. Type the **Username** and **Password**.
3. Click **Login**.

If something is wrong, you see *"Wrong username, password or role!"*.

### The dashboard
```
┌────────────────────────────────────────────────────────────────────────────┐
│ University Management System          Logged in as teacher1 (TEACHER) | Logout │
└────────────────────────────────────────────────────────────────────────────┘
  [ Teachers ] [ Courses ] [ Registrations ]     <- tabs: only what your role can use

  All Courses
  All courses. Add your own courses below. You can edit or delete only your own courses (highlighted).
  ┌────┬─────────┬──────────────────────────┬────────┬──────────┬─────────────┬─────────────┐
  │ ID │ Code    │ Title                    │ Credit │ Semester │ Teacher     │ Actions     │
  │ 1  │ CSE3230 │ Software Development Lab │ 1.5    │ 3-2      │ Rahim Uddin │ Edit Delete │ <- yellow = yours
  │ 7  │ CSE8888 │ ...                      │ 3      │ 4-1      │ Rafsan      │ -           │ <- not yours
  └────┴─────────┴──────────────────────────┴────────┴──────────┴─────────────┴─────────────┘
  Added successfully (status 201)               <- message (green = OK, red = error)
  ─────────────────────────────────
  Add New Course
  Course code  [        ]
  Title        [        ]
  Credit       [        ]
  Semester     [        ]
  [Save] [Cancel]
```

- **Yellow rows** are **yours**: your account, your courses, your registrations.
- A row with `-` belongs to someone else, so you can't change it.
- The **Add New** form is only shown where you are allowed to add.

### What each role sees

| Role | Tabs | What they can do |
|---|---|---|
| **Admin** | Admins · Teachers · Students · Courses · Registrations | Everything in every tab. When adding a course, the admin types the **teacher username**. When adding a registration, the admin types the **course code** and **student username**. The admin can't delete their own admin account. |
| **Teacher** | Teachers · Courses · Registrations | **Teachers:** see all teachers, edit or delete their own account. **Courses:** see all, **add** new courses (they become the teacher), edit or delete **their own** courses. **Registrations:** see who registered in **their** courses (read only). |
| **Student** | Students · Courses · My Registrations | **Students:** see all students, edit or delete their own account. **Courses:** see all and click **Register** (asks for the section). **My Registrations:** see their courses, **Edit** = change section, **Drop** = leave the course. They can also register by typing a course code in the form. |

### How to do each operation

| To... | Do this |
|---|---|
| **See all** (Read) | Open a tab. The table reloads after each change. |
| **Add** (Create) | Fill the **Add New ...** form below the table and click **Save**. |
| **Edit** (Update) | Click **Edit** on a row. The form fills with its data. Change it and click **Save**. For people, leave the password empty to keep the old one. |
| **Delete / Drop** | Click **Delete** (or **Drop** for a registration) and confirm. |
| **Register for a course** (student) | **Courses** tab → **Register** on a course → type a section (e.g. `A`) → OK. |
| **Cancel editing** | Click **Cancel**. |
| **Log out** | Click **Logout** at the top right. |

Some notes:
- If you change **your own username or password**, the page says *"Your login details were changed. Please log in again."* and logs you out.
- Deleting **your own account** logs you out.
- Deleting a **course** also deletes its registrations.
- Deleting a **teacher** also deletes their courses and those courses' registrations.
- Deleting a **student** also deletes their registrations.
- Passwords never appear in the table; they are stored as a hash. If someone forgets theirs, the **admin** edits the user and types a new password.

---

## 5. REST API reference

### People: `/api/admins`, `/api/teachers`, `/api/students`

| Method | URL | What it does | Who | Success | Errors |
|---|---|---|---|---|---|
| GET    | `/api/{type}`      | list all    | that section's role + admin | **200** | 401, 403 |
| GET    | `/api/{type}/{id}` | get one     | that section's role + admin | **200** | 401, 403, 404 |
| POST   | `/api/{type}`      | create      | **admin only** | **201** | 400, 401, 403, 409 |
| PUT    | `/api/{type}/{id}` | update      | admin, or **owner** (own account) | **200** | 400, 401, 403, 404, 409 |
| DELETE | `/api/{type}/{id}` | delete      | admin (not themselves), or **owner** | **204** | 400, 401, 403, 404 |

### Courses: `/api/courses`

| Method | URL | What it does | Who | Success | Errors |
|---|---|---|---|---|---|
| GET    | `/api/courses`      | list all courses | everyone logged in | **200** | 401 |
| GET    | `/api/courses/{id}` | get one course   | everyone logged in | **200** | 401, 404 |
| POST   | `/api/courses`      | create a course  | admin, teacher (teacher = the course's teacher) | **201** | 400, 401, 403, 409 |
| PUT    | `/api/courses/{id}` | update a course  | admin, or the course's **own teacher** | **200** | 400, 401, 403, 404, 409 |
| DELETE | `/api/courses/{id}` | delete a course and its registrations | admin, or the course's **own teacher** | **204** | 401, 403, 404 |

### Registrations: `/api/registrations`

| Method | URL | What it does | Who | Success | Errors |
|---|---|---|---|---|---|
| GET    | `/api/registrations`      | list: admin = all, teacher = in their courses, student = their own | everyone logged in | **200** | 401 |
| GET    | `/api/registrations/{id}` | get one (only if you may see it) | everyone logged in | **200** | 401, 403, 404 |
| POST   | `/api/registrations`      | register a student in a course | admin, student (student = themselves) | **201** | 400, 401, 403, 409 |
| PUT    | `/api/registrations/{id}` | change the section | admin, or the **own student** | **200** | 400, 401, 403, 404 |
| DELETE | `/api/registrations/{id}` | drop the course | admin, or the **own student** | **204** | 401, 403, 404 |

### Login and others

| Method | URL | What it does | Result |
|---|---|---|---|
| GET  | `/api/me`  | who is logged in | `{"username":"teacher1","role":"TEACHER"}` |
| POST | `/login`   | log in (form: role, username, password) | 302 → `/index.html`, or `/login.html?error` |
| GET  | `/logout`  | log out | 302 → `/login.html?logout` |

### What the status codes mean

| Code | Meaning | When |
|---|---|---|
| 200 OK | Success | read or update worked |
| 201 Created | New record made | POST worked |
| 204 No Content | Done, nothing to return | DELETE worked |
| 400 Bad Request | Your data is wrong | a required field is missing, teacher/course/student not found, or the admin tries to delete themselves |
| 401 Unauthorized | Who are you? | not logged in, or wrong password |
| 403 Forbidden | Not allowed | your role can't do this, or the record isn't yours |
| 404 Not Found | No such record | wrong id |
| 409 Conflict | Clash | username or course code already exists, or already registered in this course |

### JSON bodies

```json
// admin / teacher / student
{ "name": "Rahim Uddin", "email": "rahim@university.edu", "department": "CSE",
  "designation": "Lecturer", "username": "teacher1", "password": "teacher123" }
{ "name": "Karim Hasan", "email": "karim@university.edu", "roll": "2003001",
  "department": "CSE", "username": "student1", "password": "student123" }

// course (a teacher doesn't send "teacher"; the admin does)
{ "code": "CSE3210", "title": "Software Engineering", "credit": 3, "semester": "3-2",
  "teacher": { "username": "teacher1" } }

// registration (a student doesn't send "student"; the admin does)
{ "course": { "code": "CSE3230" }, "section": "A", "student": { "username": "student1" } }

// change the section of a registration (PUT)
{ "section": "B" }
```

A registration comes back with the full course and student inside it (no passwords):
```json
{ "id": 1, "section": "A",
  "student": { "id": 1, "name": "Karim Hasan", "roll": "2003001", "username": "student1", ... },
  "course":  { "id": 1, "code": "CSE3230", "title": "Software Development Lab", "credit": 1.5,
               "teacher": { "id": 1, "name": "Rahim Uddin", "username": "teacher1", ... } } }
```

### Testing with Postman
1. Choose the method and URL, for example `GET http://localhost:8081/api/courses`.
2. **Authorization** tab → Type **Basic Auth** → username and password (e.g. `teacher1` / `teacher123`).
3. For POST and PUT: **Body** → **raw** → **JSON**, then paste a body from above.
4. Click **Send** and look at the status code.

### Testing with curl (Windows CMD)
```
curl -u student1:student123 http://localhost:8081/api/courses
curl -u student1:student123 -X POST -H "Content-Type: application/json" -d "{\"course\":{\"code\":\"CSE3201\"},\"section\":\"A\"}" http://localhost:8081/api/registrations
curl -u teacher1:teacher123 http://localhost:8081/api/registrations
curl -u teacher1:teacher123 http://localhost:8081/api/students        (-> 403)
```

---

## 6. Project structure

```
Software Lab-HW
├── pom.xml                              Maven: project info + libraries
├── Dockerfile                           recipe to build the app image
├── compose.yaml                         runs app + MySQL together
├── .dockerignore                        files Docker should not copy
├── .gitignore                           files Git should not save
├── README.md                            this file
├── data/                                (made when the app runs) H2 database file
├── target/                              (made when building) compiled code + ums.jar
└── src/main
    ├── java/com/university/ums
    │   ├── UmsApplication.java          ▶ starting point of the app
    │   ├── DataLoader.java              creates the sample data
    │   │
    │   ├── model/                       DATABASE TABLES
    │   │   ├── Admin.java
    │   │   ├── Teacher.java
    │   │   ├── Student.java
    │   │   ├── Course.java              a course + its teacher
    │   │   └── Registration.java        a student registered in a course
    │   │
    │   ├── repository/                  DATABASE ACCESS (find, save, delete)
    │   │   ├── AdminRepository.java
    │   │   ├── TeacherRepository.java
    │   │   ├── StudentRepository.java
    │   │   ├── CourseRepository.java
    │   │   └── RegistrationRepository.java
    │   │
    │   ├── controller/                  REST API (URLs)
    │   │   ├── AdminController.java         /api/admins
    │   │   ├── TeacherController.java       /api/teachers
    │   │   ├── StudentController.java       /api/students
    │   │   ├── CourseController.java        /api/courses
    │   │   ├── RegistrationController.java  /api/registrations
    │   │   └── MeController.java            /api/me
    │   │
    │   └── security/                    LOGIN + ROLES
    │       ├── SecurityConfig.java      who can open which URL, login, logout, BCrypt
    │       └── MyUserDetailsService.java   finds the user at login
    │
    └── resources
        ├── application.properties       settings (port, database)
        └── static/                      WEB PAGES (sent to the browser as they are)
            ├── login.html               login page with role selection
            ├── index.html               dashboard page
            ├── app.js                   dashboard logic (calls the REST API)
            └── style.css                simple design for both pages
```

### Database tables and how they connect

```
 admin            teacher                 student
 ─────            ───────                 ───────
 id               id  ◄──────┐            id  ◄─────────────┐
 name             name       │            name              │
 email            email      │            email             │
 username         department │            roll              │
 password         designation│            department        │
                  username   │            username          │
                  password   │            password          │
                             │                              │
                  course     │            registration      │
                  ──────     │            ────────────      │
                  id  ◄──────┼────┐       id                │
                  code       │    └────── course_id         │
                  title      │            student_id ───────┘
                  credit     │            section
                  semester   │
                  teacher_id─┘
```

- One teacher has **many** courses (`course.teacher_id`).
- One course has **many** registrations, and one student has **many** registrations (`registration.course_id`, `registration.student_id`).
- These links are **foreign keys** with **ON DELETE CASCADE**: deleting the "one" side automatically deletes the "many" rows.

### How one request travels through the app

Example: **teacher1 edits their own course** and clicks **Save**.

```
Browser (app.js)
   │  PUT /api/courses/1   {"code":"CSE3230","title":"...","credit":"1.5","semester":"3-2"}
   ▼
Spring Security (SecurityConfig)                ← 1st check: the ROLE
   │  logged in?              no → 401
   │  role ADMIN or TEACHER?  no → 403
   ▼ yes
CourseController.update()                       ← 2nd check: the OWNER
   │  course 1 exists?                            no → 404
   │  admin, or is course 1 teacher1's own?       no → 403
   │  code and title filled?                      no → 400
   │  new code used by another course?            yes → 409
   │  copy the new values
   ▼
CourseRepository.save()
   ▼
Database (H2 in IntelliJ, MySQL in Docker)  → UPDATE course ... WHERE id = 1
   ▼
Response: 200 OK + the course as JSON → app.js shows "Updated successfully" and reloads the table
```

---

## 7. Spring Boot basics (for viva)

### What is Spring Boot?
- **Spring** is a big Java framework for making web applications.
- **Spring Boot** is Spring with **ready-made settings**. You add "starter" libraries (web, security, JPA) and they work with almost no setup.
- It has a **web server (Tomcat) built in**. Running the `main` method starts a website.

### How is a Spring Boot project created?
1. Go to **https://start.spring.io** (Spring Initializr).
2. Choose **Maven**, **Java**, Spring Boot **3.x**, Java **17**, Packaging **Jar**.
3. Fill in Group `com.university` and Artifact `ums`.
4. **Add dependencies:** Spring Web, Spring Data JPA, Spring Security, H2 Database, MySQL Driver.
5. Click **Generate**, unzip the file, and open the folder in IntelliJ (*File → Open*).

### Which files does Spring Initializr create automatically?

| File | What it is |
|---|---|
| `pom.xml` | Project info and the list of libraries. Maven reads it. |
| `src/main/java/.../UmsApplication.java` | The **main class** with `@SpringBootApplication` and `main()`. |
| `src/main/resources/application.properties` | The settings file. It is empty at first. |
| `src/main/resources/static/` | A folder for HTML / CSS / JS files. |
| `src/main/resources/templates/` | For server-side templates. We don't use it, so we removed it. |
| `src/test/java/.../UmsApplicationTests.java` | A sample test. We removed it to keep the project simple. |
| `mvnw`, `mvnw.cmd`, `.mvn/` | The Maven Wrapper. We removed it, because IntelliJ has Maven built in. |
| `.gitignore`, `HELP.md` | Git settings and help links. |

**Everything else we wrote ourselves:** `DataLoader`, the `model`, `repository`, `controller` and `security` folders, the 4 web files, `Dockerfile`, `compose.yaml`, `.dockerignore` and this README.

### "Which file runs first / automatically?"
**`UmsApplication.java`**, the class with `@SpringBootApplication` and the `main()` method. When you run it, Spring Boot **automatically**:
1. loads **`application.properties`**,
2. **scans** the `com.university.ums` package and its sub-packages, and creates every `@RestController`, `@Service`, `@Component`, `@Configuration` and repository,
3. connects to the **database** and **creates the tables** from the `@Entity` classes,
4. runs **`DataLoader`**, which creates the sample data,
5. starts **Tomcat** on port **8081**,
6. shows **`static/index.html`** when someone opens `http://localhost:8081/`. Because of security, someone who isn't logged in is sent to `login.html` first.

---

## 8. Every file explained line by line

### 8.1 `pom.xml`

The Maven file: what the project is and which libraries it needs.

| Line(s) | Meaning |
|---|---|
| `<?xml version="1.0" encoding="UTF-8"?>` | This is an XML file. |
| `<project xmlns=...>` | The root tag of every Maven project. The `xmlns` parts are fixed boilerplate. |
| `<modelVersion>4.0.0</modelVersion>` | The POM format version. It is always 4.0.0. |
| `<parent> spring-boot-starter-parent 3.3.5 </parent>` | Use Spring Boot 3.3.5's default settings, so we don't need to write a version for every library. |
| `<groupId>com.university</groupId>` | The organisation name. |
| `<artifactId>ums</artifactId>` | The project name (University Management System). |
| `<version>1.0</version>` | Our version. |
| `<name>University Management System</name>` | A readable name. |
| `<java.version>17</java.version>` | Use Java 17. |
| `spring-boot-starter-web` | Web server (Tomcat) + REST API + JSON (Jackson). |
| `spring-boot-starter-data-jpa` | Work with the database using Java classes (Hibernate) instead of SQL. |
| `spring-boot-starter-security` | Login, logout, password hashing, roles. |
| `h2` (scope `runtime`) | The small file database used when running in IntelliJ. `runtime` = only needed when the app runs, not when compiling. |
| `mysql-connector-j` (scope `runtime`) | The MySQL driver, used when running in Docker. |
| `<finalName>ums</finalName>` | The built file is named `target/ums.jar`. The Dockerfile uses this name. |
| `spring-boot-maven-plugin` | Builds one runnable jar with all libraries inside (`java -jar ums.jar`). |

---

### 8.2 `UmsApplication.java`

```java
package com.university.ums;
```
The package (folder) of this class.

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
```
Imports the two Spring Boot classes we use.

```java
@SpringBootApplication
```
Marks the **main class**. It turns on auto-configuration and scanning of this package and its sub-packages.

```java
public class UmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(UmsApplication.class, args);
    }
}
```
The Java `main` method, where the program starts. `SpringApplication.run(...)` starts everything: Spring, the database, Tomcat on port 8081.

---

### 8.3 `DataLoader.java`

It creates the sample data so you can log in and test right away.

```java
// runs once when the app starts and adds sample data (only into empty tables)
@Component
public class DataLoader implements CommandLineRunner {
```
`@Component` tells Spring to create this class. A class that implements **`CommandLineRunner`** has a `run` method that Spring calls **once, right after the app has started**.

```java
    private final AdminRepository adminRepository;
    ... (teacher, student, course, registration repositories and the PasswordEncoder)

    public DataLoader(AdminRepository adminRepository, ...) {
        this.adminRepository = adminRepository;
        ...
    }
```
**Constructor injection:** Spring passes in the 5 repositories and the password encoder, and we keep them in fields.

```java
    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setName("Main Admin");
            admin.setEmail("admin@university.edu");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            adminRepository.save(admin);
        }
```
`count()` is the number of rows. If there are **no admins**, make one. **Hash the password** with `encode(...)` and save it (INSERT). The same blocks follow for `teacher1` and `student1`.

```java
        // two courses of teacher1
        Optional<Teacher> teacher1 = teacherRepository.findByUsername("teacher1");
        if (courseRepository.count() == 0 && teacher1.isPresent()) {
            courseRepository.save(newCourse("CSE3230", "Software Development Lab", 1.5, "3-2", teacher1.get()));
            courseRepository.save(newCourse("CSE3201", "Operating Systems", 3.0, "3-2", teacher1.get()));
        }
```
Find `teacher1`. If there are no courses yet and teacher1 exists, save two courses that belong to teacher1.

```java
        // student1 registered in CSE3230
        Optional<Student> student1 = studentRepository.findByUsername("student1");
        Optional<Course> lab = courseRepository.findByCode("CSE3230");
        if (registrationRepository.count() == 0 && student1.isPresent() && lab.isPresent()) {
            Registration registration = new Registration();
            registration.setStudent(student1.get());
            registration.setCourse(lab.get());
            registration.setSection("A");
            registrationRepository.save(registration);
        }
    }
```
If there are no registrations, and student1 and CSE3230 exist, register student1 in CSE3230, section A.

```java
    private Course newCourse(String code, String title, Double credit, String semester, Teacher teacher) {
        Course course = new Course();
        course.setCode(code);
        ...
        course.setTeacher(teacher);
        return course;
    }
```
A small helper that builds a Course object, so the two lines above stay short.

---

### 8.4 `model/Admin.java`, `model/Teacher.java`, `model/Student.java`

A **model** (or **entity**) is a Java class that becomes a **database table**. Each object is one **row**, and each field is one **column**.

Here is `Admin.java` line by line:

```java
package com.university.ums.model;

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
**`@Entity`** creates a table named `admin` for this class.

```java
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
```
`@Id` makes `id` the **primary key**. `@GeneratedValue(IDENTITY)` makes the database give each new row the next number automatically (1, 2, 3...).

```java
    private String name;

    private String email;
```
Normal columns.

```java
    @Column(unique = true)
    private String username;
```
`unique = true` means two admins can't have the same username.

```java
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
```
The password column, which stores the BCrypt **hash**. `WRITE_ONLY` means the password can be **received** in JSON (create or update) but is **never sent back** in a response.

```java
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    ... getName/setName, getEmail/setEmail, getUsername/setUsername, getPassword/setPassword
```
**Getters and setters.** The fields are private, so other code reads them with `getX()` and changes them with `setX(...)`. Spring (JPA and Jackson) uses these methods too.

| Model | Table | Fields |
|---|---|---|
| `Admin`   | `admin`   | id, name, email, username, password |
| `Teacher` | `teacher` | id, name, email, **department**, **designation**, username, password |
| `Student` | `student` | id, name, email, **roll**, **department**, username, password |

---

### 8.5 `model/Course.java`

```java
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
```
The table `course` with an automatic id (the same as above).

```java
    @Column(unique = true)
    private String code;
```
The course code, like `CSE3230`. It is unique: two courses can't have the same code.

```java
    private String title;

    private Double credit;

    private String semester;
```
The title, the credit (`Double`, so `1.5` works) and the semester, like `3-2`.

```java
    // the teacher of this course (when the teacher is deleted, their courses are deleted too)
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Teacher teacher;
```
- **`@ManyToOne`**: **many** courses can belong to **one** teacher. In the database this becomes the column `teacher_id`, a **foreign key** to the `teacher` table.
- **`@OnDelete(action = OnDeleteAction.CASCADE)`**: the database gets `ON DELETE CASCADE`, so if the teacher row is deleted, the database deletes their courses automatically.
- In JSON, the course contains the whole teacher object: `"teacher": {"id":1, "name":"Rahim Uddin", ...}` (no password).

After the fields come the getters and setters for id, code, title, credit, semester and teacher.

---

### 8.6 `model/Registration.java`

```java
// a student registered in a course
@Entity
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
```
The table `registration` with an automatic id.

```java
    // when the student is deleted, their registrations are deleted too
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;
```
**Which student.** Many registrations belong to one student, stored as the column `student_id`. If the student is deleted, their registrations are deleted.

```java
    // when the course is deleted, its registrations are deleted too
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Course course;
```
**Which course.** Many registrations belong to one course, stored as the column `course_id`. If the course is deleted, its registrations are deleted.

```java
    private String section;
```
The section, like `A` or `B`.

Then come the getters and setters.

---

### 8.7 The repositories

A **repository** is how we talk to the database. We only write an **interface**, and Spring writes the real code.

#### `AdminRepository.java` (Teacher and Student are the same)

```java
public interface AdminRepository extends JpaRepository<Admin, Long> {
```
`JpaRepository<Admin, Long>`: `Admin` is the entity and `Long` is the type of its id. This gives us these methods **for free**:
`findAll()`, `findById(id)`, `save(obj)`, `deleteById(id)`, `existsById(id)`, `count()`.

```java
    Optional<Admin> findByUsername(String username);
```
Spring reads the **method name** and builds the SQL itself: `SELECT * FROM admin WHERE username = ?`. `Optional` means "maybe found, maybe not". It is used at login.

```java
    boolean existsByUsername(String username);
}
```
Returns `true` if the username is already taken. It is used for the 409 error.

#### `CourseRepository.java`

```java
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);
}
```
Find a course by its code (`WHERE code = ?`), and check whether a code is already used.

#### `RegistrationRepository.java`

```java
    // registrations of one student
    List<Registration> findByStudentUsername(String username);
```
The name means "find by **student** → **username**". Spring follows the link from registration to student, like `WHERE student.username = ?`. It is used for **My Registrations**.

```java
    // registrations in the courses of one teacher
    List<Registration> findByCourseTeacherUsername(String username);
```
"find by **course** → **teacher** → **username**": it follows two links, registration → course → teacher. It is used for **Registrations in My Courses**.

```java
    boolean existsByStudentAndCourse(Student student, Course course);
```
Is this student already registered in this course? It is used for the 409 error.

---

### 8.8 `controller/AdminController.java` (and Teacher / Student)

A **controller** receives HTTP requests and returns responses. **This is the REST API.** The three people controllers are almost the same: they differ in the class names, in the fields they copy in `update`, and in the "own account only" rule for teachers and students (8.9). Here is `AdminController` line by line.

```java
package com.university.ums.controller;

import com.university.ums.model.Admin;
import com.university.ums.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
```
Imports. `web.bind.annotation.*` gives `@RestController`, `@GetMapping`, `@PostMapping` and the other annotations.

```java
@RestController
@RequestMapping("/api/admins")
public class AdminController {
```
`@RestController`: every method returns **data, which is turned into JSON automatically**. `@RequestMapping("/api/admins")`: every URL in this class starts with `/api/admins`.

```java
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }
```
Constructor injection: Spring passes in the repository and the BCrypt encoder.

#### READ all: `GET /api/admins`
```java
    // READ all -> 200
    @GetMapping
    public List<Admin> getAll() {
        return adminRepository.findAll();
    }
```
Returns every admin as a JSON array with **200 OK**.

#### READ one: `GET /api/admins/{id}`
```java
    // READ one -> 200 or 404
    @GetMapping("/{id}")
    public ResponseEntity<Admin> getOne(@PathVariable Long id) {
        Optional<Admin> admin = adminRepository.findById(id);
        if (admin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(admin.get());
    }
```
`{id}` is a value in the URL, and `@PathVariable` puts it into `id`. `ResponseEntity` lets us choose the status: not found → **404**, found → **200** with the admin.

#### CREATE: `POST /api/admins`
```java
    // CREATE -> 201, 400 or 409
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Admin admin) {
```
`@RequestBody` turns the JSON in the request into an `Admin` object. `<?>` = the body can be an Admin or an error message.

```java
        if (isBlank(admin.getName()) || isBlank(admin.getUsername()) || isBlank(admin.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "name, username and password are required"));
        }
```
Missing name, username or password → **400 Bad Request** with `{"error": "..."}`.

```java
        if (adminRepository.existsByUsername(admin.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
```
Username already taken → **409 Conflict**.

```java
        admin.setId(null);
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(adminRepository.save(admin));
    }
```
- `setId(null)`: ignore any id that was sent, so the database makes a new one. That way nobody can overwrite an old row with POST.
- `encode`: **hash the password** with BCrypt.
- Save the new row (INSERT) and return **201 Created**.

#### UPDATE: `PUT /api/admins/{id}`
```java
    // UPDATE -> 200, 400, 404 or 409
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Admin newData) {
        Optional<Admin> found = adminRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (isBlank(newData.getName()) || isBlank(newData.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "name and username are required"));
        }
```
The id comes from the URL and the new values from the JSON. No such admin → **404**. Empty name or username → **400**. The password *may* be empty here, which means "keep the old one".

```java
        Admin admin = found.get();
        if (!admin.getUsername().equals(newData.getUsername()) && adminRepository.existsByUsername(newData.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
```
Username **changed** to one that someone else has → **409**.

```java
        admin.setName(newData.getName());
        admin.setEmail(newData.getEmail());
        admin.setUsername(newData.getUsername());
        if (!isBlank(newData.getPassword())) {
            admin.setPassword(passwordEncoder.encode(newData.getPassword()));
        }
        return ResponseEntity.ok(adminRepository.save(admin));
    }
```
Copy the new values. Change the password **only if a new one was typed**, and hash it. `save` on a row that already has an id does an **UPDATE** → **200 OK**.

#### DELETE: `DELETE /api/admins/{id}`
```java
    // DELETE -> 204, 400 or 404  (an admin cannot delete their own account)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
```
**`Authentication auth`**: Spring passes in the **logged-in user**. `auth.getName()` is their username.

```java
        Optional<Admin> found = adminRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (found.get().getUsername().equals(auth.getName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "you cannot delete your own account"));
        }
        adminRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
```
Not found → **404**. If the admin tries to delete **themselves** → **400**, so the system doesn't lose the admin who is using it. Otherwise delete → **204 No Content**.

#### Helper
```java
    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
```
`true` if the text is missing (`null`) or only spaces.

---

### 8.9 `TeacherController` and `StudentController`: the "own account only" rule

They are the same as `AdminController` (with their own fields), plus one rule: a teacher may only **edit or delete their own** account (a student: their own). The admin may change anyone. Here is `TeacherController`; `StudentController` is the same with `Student`.

```java
    // UPDATE -> 200, 400, 403, 404 or 409  (a teacher can only update their own account)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Teacher newData, Authentication auth) {
        Optional<Teacher> found = teacherRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Teacher teacher = found.get();
        if (!isAdmin(auth) && !teacher.getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only edit your own account"));
        }
```
**The ownership check:** if the user is **not an admin** and the record's username is **not theirs** → **403 Forbidden**. After this, `update` works like the admin version, copying `department` and `designation` too (students: `roll` and `department`).

```java
    // DELETE -> 204, 403 or 404  (a teacher can only delete their own account)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        Optional<Teacher> found = teacherRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!isAdmin(auth) && !found.get().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only delete your own account"));
        }
        teacherRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
```
The same check for delete. Deleting a teacher also deletes their courses (CASCADE).

```java
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
```
`true` if the logged-in user has the role `ROLE_ADMIN`. `getAuthorities()` is the user's list of roles, and `anyMatch` checks whether any of them is `ROLE_ADMIN`.

There is no ownership check in `create` (POST), because `SecurityConfig` already allows **only the admin** to POST teachers and students.

---

### 8.10 `controller/CourseController.java`

```java
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
```
URLs start with `/api/courses`. It needs the course repository, plus the teacher repository to find the course's teacher.

```java
    // READ all -> 200  (everyone can see all courses)
    @GetMapping
    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    // READ one -> 200 or 404
    @GetMapping("/{id}")
    public ResponseEntity<Course> getOne(@PathVariable Long id) { ... }
```
Read all or one course, the same as before.

#### CREATE: `POST /api/courses`
```java
    // CREATE -> 201, 400 or 409  (a teacher's new course is their own, the admin chooses the teacher)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Course course, Authentication auth) {
        if (isBlank(course.getCode()) || isBlank(course.getTitle())) {
            return ResponseEntity.badRequest().body(Map.of("error", "course code and title are required"));
        }
        if (courseRepository.existsByCode(course.getCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "course code already exists"));
        }
```
Code and title are required → **400**. The code must be new → **409**.

```java
        Optional<Teacher> teacher = findTeacher(course, auth);
        if (teacher.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "teacher not found"));
        }
        course.setId(null);
        course.setTeacher(teacher.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(courseRepository.save(course));
    }
```
Find **who teaches** the course (see `findTeacher` below). If no teacher is found → **400**. Otherwise set the teacher, save, and return **201**.

#### UPDATE: `PUT /api/courses/{id}`
```java
        Course course = found.get();
        if (!isAdmin(auth) && !course.getTeacher().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only edit your own courses"));
        }
```
After the 404 check comes the **ownership check**: a teacher can only edit a course whose teacher is **themselves** → otherwise **403**.

```java
        // only the admin can give the course to another teacher
        if (isAdmin(auth) && newData.getTeacher() != null) {
            Optional<Teacher> teacher = findTeacher(newData, auth);
            if (teacher.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "teacher not found"));
            }
            course.setTeacher(teacher.get());
        }
        course.setCode(newData.getCode());
        course.setTitle(newData.getTitle());
        course.setCredit(newData.getCredit());
        course.setSemester(newData.getSemester());
        return ResponseEntity.ok(courseRepository.save(course));
```
The 400 and 409 checks come first, the same as in create. Only the **admin** can move a course to another teacher. Then copy the values and save → **200**.

#### DELETE: `DELETE /api/courses/{id}`
```java
        if (!isAdmin(auth) && !found.get().getTeacher().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only delete your own courses"));
        }
        courseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
```
Only the admin or the course's own teacher can delete it → **204**. Its registrations are deleted too (CASCADE).

#### `findTeacher(...)`
```java
    // the admin chooses the teacher by username, a teacher is always the teacher of their own course
    private Optional<Teacher> findTeacher(Course data, Authentication auth) {
        if (!isAdmin(auth)) {
            return teacherRepository.findByUsername(auth.getName());
        }
        if (data.getTeacher() == null) {
            return Optional.empty();
        }
        return teacherRepository.findByUsername(data.getTeacher().getUsername());
    }
```
- **Teacher:** the teacher is **the logged-in teacher**. Whatever was sent in JSON is ignored, so a teacher can't create a course for someone else.
- **Admin:** use the username from the JSON, `"teacher": {"username": "teacher1"}`.

---

### 8.11 `controller/RegistrationController.java`

```java
@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationRepository registrationRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
```
URLs start with `/api/registrations`. It needs three repositories.

#### READ all: different for each role
```java
    // READ all -> 200  (admin: all, teacher: registrations in their courses, student: their own)
    @GetMapping
    public List<Registration> getAll(Authentication auth) {
        if (hasRole(auth, "ADMIN")) {
            return registrationRepository.findAll();
        }
        if (hasRole(auth, "TEACHER")) {
            return registrationRepository.findByCourseTeacherUsername(auth.getName());
        }
        return registrationRepository.findByStudentUsername(auth.getName());
    }
```
The same URL gives **different data for each role**:
- the admin gets everything,
- a teacher gets the registrations in **their** courses,
- a student gets **their own**.

#### READ one
```java
        if (!canSee(registration.get(), auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "this is not your registration"));
        }
```
After the 404 check: if you may not see it → **403**. `canSee` uses the same rules as the list.

#### CREATE: register for a course
```java
        if (isBlank(registration.getSection())) {
            return ResponseEntity.badRequest().body(Map.of("error", "section is required"));
        }
```
A section is required → **400**.

```java
        Optional<Course> course = Optional.empty();
        if (registration.getCourse() != null) {
            course = courseRepository.findByCode(registration.getCourse().getCode());
        }
        if (course.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "course not found"));
        }
```
Find the course by the code sent in JSON, `"course": {"code": "CSE3230"}`. No such course → **400**.

```java
        Optional<Student> student = findStudent(registration, auth);
        if (student.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "student not found"));
        }
```
Find the student: a student registers **themselves**, and the admin sends `"student": {"username": "..."}` (see `findStudent`).

```java
        if (registrationRepository.existsByStudentAndCourse(student.get(), course.get())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "already registered in this course"));
        }
        registration.setId(null);
        registration.setCourse(course.get());
        registration.setStudent(student.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationRepository.save(registration));
```
Already registered in this course → **409**. Otherwise link the course and student, save → **201**.

#### UPDATE: change the section
```java
        Registration registration = found.get();
        if (!isOwnerOrAdmin(registration, auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only change your own registrations"));
        }
        if (isBlank(newData.getSection())) {
            return ResponseEntity.badRequest().body(Map.of("error", "section is required"));
        }
        registration.setSection(newData.getSection());
        return ResponseEntity.ok(registrationRepository.save(registration));
```
Only the admin or the student who owns it → otherwise **403**. **Only the section can change.** To change the course, drop it and register again, like in a real system.

#### DELETE: drop the course
```java
        if (!isOwnerOrAdmin(found.get(), auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only drop your own registrations"));
        }
        registrationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
```
Only the admin or the owner → **204**.

#### Helpers
```java
    // the admin chooses the student by username, a student always registers themself
    private Optional<Student> findStudent(Registration data, Authentication auth) { ... }
```
The same idea as `findTeacher` in the course controller.

```java
    private boolean isOwnerOrAdmin(Registration registration, Authentication auth) {
        return hasRole(auth, "ADMIN") || registration.getStudent().getUsername().equals(auth.getName());
    }
```
`true` for the admin, or if the registration's student is the logged-in user.

```java
    // admin: every registration, teacher: registrations in their courses, student: their own
    private boolean canSee(Registration registration, Authentication auth) { ... }
```
Who may **read** one registration.

```java
    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
```
`hasRole(auth, "TEACHER")` is true if the user has `ROLE_TEACHER`.

---

### 8.12 `controller/MeController.java`

```java
@RestController
public class MeController {

    // tells the web page who is logged in and what role they have
    @GetMapping("/api/me")
    public Map<String, String> me(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return Map.of("username", authentication.getName(), "role", role);
    }
}
```
`GET /api/me` returns e.g. `{"username":"teacher1","role":"TEACHER"}`. The role is the user's first (only) authority, such as `ROLE_TEACHER`, with `ROLE_` removed. The dashboard uses this to show the name and choose the tabs.

---

### 8.13 `security/SecurityConfig.java`

All the Spring Security settings are in this one file.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
```
`@Configuration` = a settings class. `@EnableWebSecurity` = turn on Spring Security.

```java
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
```
**Password hashing.** One BCrypt encoder, used everywhere. BCrypt is a **one-way** hash with a random salt: you can't get the password back from the hash. At login, Spring checks the typed password against the stored hash.

```java
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
```
The **filter chain** is the list of security checks every request passes through. CSRF tokens are turned off to keep REST testing with Postman simple, which is common in student REST projects.

```java
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login.html", "/style.css", "/error").permitAll()
```
**Role-based access control starts here.** Spring checks the rules **from top to bottom** and uses the **first one that matches**. Anyone can open the login page, the CSS and the error page.

```java
                .requestMatchers("/api/admins/**").hasRole("ADMIN")
```
`/api/admins` and everything under it: **ADMIN only**.

```java
                // only the admin can add new teachers and students
                .requestMatchers(HttpMethod.POST, "/api/teachers/**", "/api/students/**").hasRole("ADMIN")
```
**POST** (create) on teachers or students: **ADMIN only**. It must come **before** the next two lines.

```java
                .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "STUDENT")
```
Teachers: **ADMIN or TEACHER**. Students: **ADMIN or STUDENT**. Which record they may change is checked later, in the controller.

```java
                // everyone can see courses, only the admin and teachers can change them
                .requestMatchers(HttpMethod.GET, "/api/courses/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                .requestMatchers("/api/courses/**").hasAnyRole("ADMIN", "TEACHER")
```
**GET** courses: every role. Anything else (POST, PUT, DELETE): **ADMIN or TEACHER**.

```java
                // everyone can see registrations (the controller filters them), only the admin and students can change them
                .requestMatchers(HttpMethod.GET, "/api/registrations/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                .requestMatchers("/api/registrations/**").hasAnyRole("ADMIN", "STUDENT")
```
**GET** registrations: every role (the controller gives each role only what it may see). POST, PUT and DELETE: **ADMIN or STUDENT**.

```java
                .anyRequest().authenticated()
            )
```
Everything else (dashboard, app.js, /api/me) needs any logged-in user.
Not logged in → **401** (a browser is sent to the login page instead). Wrong role → **403**.

```java
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index.html", true)
                .failureUrl("/login.html?error")
                .permitAll()
            )
```
**Login with a form:**
- `loginPage`: our page.
- `loginProcessingUrl`: the form posts to `/login`, and **Spring checks the username and password itself**.
- `defaultSuccessUrl`: after a successful login → the dashboard.
- `failureUrl`: after a failed login → back to the login page with `?error`.

```java
            .httpBasic(Customizer.withDefaults())
```
This also allows **HTTP Basic** login (username and password in a header), which Postman and `curl -u` use.

```java
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.html?logout")
                .permitAll()
            );

        return http.build();
    }
}
```
**Logout:** `/logout` ends the session, then goes to the login page with `?logout`. `http.build()` creates the filter chain.

> **Who checks what?**
> - **`SecurityConfig`** checks the **role**: may this role use this URL and method at all?
> - **The controllers** check the **owner**: is this record yours (your account, your course, your registration)? Only the controller knows which record is being changed.

---

### 8.14 `security/MyUserDetailsService.java`

At login, Spring Security asks a **`UserDetailsService`**: *"give me the user with this username"*. Users are in **three tables**, and the **role chosen on the login page** decides which table to look in.

```java
@Service
public class MyUserDetailsService implements UserDetailsService {
```
`@Service` = Spring creates it. Because it implements `UserDetailsService`, Spring Security uses it at login automatically. The 3 repositories are injected through the constructor.

```java
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String role = selectedRole();
```
Spring calls this with the typed username. First we read the **role from the login page** (`ADMIN`, `TEACHER` or `STUDENT`).

```java
        if (role == null || role.equals("ADMIN")) {
            Optional<Admin> admin = adminRepository.findByUsername(username);
            if (admin.isPresent()) {
                return User.withUsername(username).password(admin.get().getPassword()).roles("ADMIN").build();
            }
        }
```
If "Admin" was selected, look in the **admin** table. If found, build a Spring Security `User` with:
- the username,
- the **hashed** password (Spring compares it with the typed password using BCrypt),
- the role **ADMIN**, stored as `ROLE_ADMIN`, which `hasRole("ADMIN")` checks.

(`role == null` happens with Postman / curl, which don't send a role. Then all three tables are checked in order.)

```java
        if (role == null || role.equals("TEACHER")) { ... teacher table, role TEACHER ... }
        if (role == null || role.equals("STUDENT")) { ... student table, role STUDENT ... }

        throw new UsernameNotFoundException("User not found: " + username);
    }
```
The same for teachers and students. Not found → login fails → *"Wrong username, password or role!"*.

```java
    // the role chosen on the login page (null when logging in from Postman/curl, then all tables are checked)
    private String selectedRole() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest().getParameter("role");
    }
```
`RequestContextHolder` gives the **current HTTP request** (the login form). We read its field `role`, which is the "Login as" dropdown.

---

### 8.15 `resources/application.properties`

| Line | Meaning |
|---|---|
| `# ...` | Lines starting with `#` are comments. |
| `server.port=8081` | The web server listens on port **8081**, so the address is `http://localhost:8081`. |
| `spring.datasource.url=jdbc:h2:file:./data/ums` | Use an **H2 database saved in the file `data/ums`** inside the project. Nothing needs to be installed. |
| `spring.datasource.username=sa` | The H2 user (`sa` is H2's default). |
| `spring.datasource.password=` | An empty password for H2. |
| `spring.jpa.hibernate.ddl-auto=update` | **Create or update the tables automatically** from the `@Entity` classes, so no SQL is needed. |
| `spring.jpa.open-in-view=false` | Close the database connection when the request work is done. This is the recommended setting and removes a warning. |

**In Docker**, `compose.yaml` sets the environment variables `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`. Spring Boot automatically lets environment variables **override** the same settings here, so in Docker the app uses MySQL. No code changes are needed.

---

### 8.16 `resources/static/login.html`

| Line(s) | Meaning |
|---|---|
| `<meta charset="UTF-8">` | Normal text encoding. |
| `<title>Login - ...</title>` | The browser tab title. |
| `<link rel="stylesheet" href="/style.css">` | Use our CSS file. |
| `<div class="login-box">` | The white box in the middle of the page. |
| `<p id="msg"></p>` | An empty line for error and logout messages. |
| `<form action="/login" method="post">` | Submitting sends the form to `POST /login`, which Spring Security handles. |
| `<select name="role">` with 3 `<option>`s | The **"Login as"** dropdown. It sends `role=ADMIN`, `TEACHER` or `STUDENT`, which `MyUserDetailsService` reads. |
| `<input type="text" name="username" required>` | The username box. The name **must** be `username` for Spring. `required` = can't be empty. |
| `<input type="password" name="password" required>` | The password box (the letters are hidden). The name **must** be `password`. |
| `<button type="submit">Login</button>` | Sends the form. |
| `if (location.search.includes("error"))` | If the URL ends with `?error` → red "Wrong username, password or role!". |
| `if (location.search.includes("logout"))` | If the URL ends with `?logout` → green "You have been logged out.". |

---

### 8.17 `resources/static/index.html` (dashboard)

Only the structure. The logic is in `app.js`.

| Line(s) | Meaning |
|---|---|
| `<div class="header">` | The dark bar at the top. |
| `<b>University Management System</b>` | The title on the left. |
| `<span class="right"> Logged in as <b id="me"></b> \| <a href="/logout">Logout</a>` | On the right: name and role (filled by app.js) and the logout link. |
| `<div class="container">` | The white content box. |
| `<div id="tabs" class="tabs"></div>` | Empty. app.js adds the tab buttons for your role. |
| `<h3 id="listTitle"></h3>` | "All Courses", "My Registrations" and so on. |
| `<p id="hint" class="hint"></p>` | The grey help line, e.g. "Click Register to take a course." |
| `<table id="table"></table>` | Empty. app.js fills it with rows. |
| `<p id="msg"></p>` | The success or error message. |
| `<div id="formBox">` | Everything about the form. app.js shows or hides it. |
| `<hr>` | A line above the form. |
| `<h3 id="formTitle"></h3>` | "Add New Course", "Edit Course (ID 2)", "Edit My Account"... |
| `<div id="form"></div>` | Empty. app.js builds the input boxes. |
| `<button onclick="save()">Save</button>` | Calls `save()` in app.js. |
| `<button onclick="resetForm()">Cancel</button>` | Stops editing. |
| `<script src="/app.js"></script>` | Loads the JavaScript. It sits at the end, so the HTML above already exists when it runs. |

---

### 8.18 `resources/static/app.js` (dashboard logic)

This file talks to the REST API with **`fetch`**. The browser already has the login session cookie, so every call is sent as the logged-in user. **One set of functions works for all 5 tabs**, because each tab is described by settings in `TABS`.

#### The settings: `TABS`

```js
var TABS = {
    admins: {
        columns: [["name", "Name"], ["email", "Email"], ["username", "Username"]],
        form: [["name", "Name"], ["email", "Email"], ["username", "Username"]],
        password: true, ownerRole: "ADMIN", ownerPath: "username", adders: ["ADMIN"]
    },
    ... teachers, students (same idea) ...
    courses: {
        columns: [["code", "Code"], ["title", "Title"], ["credit", "Credit"], ["semester", "Semester"], ["teacher.name", "Teacher"]],
        form: [["code", "Course code"], ["title", "Title"], ["credit", "Credit"], ["semester", "Semester"], ["teacher.username", "Teacher username", "admin"]],
        password: false, ownerRole: "TEACHER", ownerPath: "teacher.username", adders: ["ADMIN", "TEACHER"]
    },
    registrations: {
        columns: [["course.code", "Course"], ["course.title", "Title"], ["course.teacher.name", "Teacher"], ["student.name", "Student"], ["student.roll", "Roll"], ["section", "Section"]],
        form: [["course.code", "Course code", "new"], ["student.username", "Student username", "admin new"], ["section", "Section"]],
        password: false, ownerRole: "STUDENT", ownerPath: "student.username", adders: ["ADMIN", "STUDENT"]
    }
};
```
Each tab has:

| Setting | Meaning | Example |
|---|---|---|
| `columns` | table columns as `[path in the JSON, label]`. A path with dots goes **inside** objects | `"teacher.name"` → `row.teacher.name` |
| `form` | form boxes as `[path, label, rule]` | rule `"admin"` = only the admin sees this box. `"new"` = only when adding. `"admin new"` = both |
| `password` | the form also has a password box (people tabs) | `true` |
| `ownerRole` / `ownerPath` | who owns a row: a row is **mine** if my role is `ownerRole` and the username at `ownerPath` is mine | courses: a TEACHER owns a course where `teacher.username` is theirs |
| `adders` | which roles may add rows | courses: admin and teacher |

```js
var ALLOWED = {
    ADMIN: ["admins", "teachers", "students", "courses", "registrations"],
    TEACHER: ["teachers", "courses", "registrations"],
    STUDENT: ["students", "courses", "registrations"]
};
```
Which tabs each role gets. The page only *hides* what you can't use; the **server** is what really blocks it with 403.

```js
var me = null;        // the logged-in user, for example {username: "teacher1", role: "TEACHER"}
var current = "";     // the open tab, for example "teachers"
var items = [];       // the rows loaded from the server
var editing = null;   // the row being edited (null = adding a new one)
```
Four variables that remember the page state.

#### When the page opens
```js
fetch("/api/me").then(function (res) {
    return res.json();
}).then(function (data) {
    me = data;
    document.getElementById("me").innerText = me.username + " (" + me.role + ")";
```
Ask the server who is logged in, keep it in `me`, and show "teacher1 (TEACHER)" in the top bar.

```js
    var html = "";
    ALLOWED[me.role].forEach(function (tab) {
        html += "<button id='tab-" + tab + "' onclick=\"openTab('" + tab + "')\">" + tabName(tab) + "</button> ";
    });
    document.getElementById("tabs").innerHTML = html;

    openTab(ALLOWED[me.role][0]);
});
```
Make one tab button per allowed tab, then open the first one.

#### Small "who am I" functions

| Function | What it returns |
|---|---|
| `isAdmin()` | `true` if the role is ADMIN. |
| `isMine(item)` | `true` if this row is **yours**: your role is the tab's `ownerRole` **and** the username at `ownerPath` is your username. |
| `canAdd()` | `true` if your role is in the tab's `adders`. |
| `tabName(tab)` | The button text. It is "My Registrations" for students, otherwise e.g. "Courses". |
| `hint()` | The grey help line for your role and tab (empty for the admin). |

#### `openTab(tab)`: switch tabs
```js
    current = tab;
    document.querySelectorAll("#tabs button").forEach(function (button) { button.className = ""; });
    document.getElementById("tab-" + tab).className = "active";
```
Remember the tab and highlight its button (dark blue).

```js
    var title = "All " + capital(tab);
    if (tab == "registrations" && me.role == "STUDENT") title = "My Registrations";
    if (tab == "registrations" && me.role == "TEACHER") title = "Registrations in My Courses";
    document.getElementById("listTitle").innerText = title;
    document.getElementById("hint").innerText = hint();

    showMessage("", "");
    resetForm();
    loadList();
```
Set the title and hint, clear the old message, reset the form, and load the table.

#### `loadList()`: READ (GET)
```js
    fetch("/api/" + current).then(function (res) {
        if (!res.ok) {
            showMessage("Could not load the list. Error " + res.status, "error");
            return [];
        }
        return res.json();
    })
```
`GET /api/courses` (for example). On an error, show it and use an empty list.

```js
    .then(function (list) {
        items = list;
        var columns = TABS[current].columns;
        var html = "<tr><th>ID</th>";
        columns.forEach(function (c) { html += "<th>" + c[1] + "</th>"; });
        html += "<th>Actions</th></tr>";
```
Keep the list and build the header row: ID, one column per setting, then Actions.

```js
        if (list.length == 0) {
            html += "<tr><td colspan='" + (columns.length + 2) + "'>No data yet</td></tr>";
        }
        list.forEach(function (item, i) {
            html += isMine(item) ? "<tr class='mine'>" : "<tr>";
            html += "<td>" + item.id + "</td>";
            columns.forEach(function (c) { html += "<td>" + clean(getValue(item, c[0])) + "</td>"; });
            html += "<td>" + buttons(item, i) + "</td></tr>";
        });
        document.getElementById("table").innerHTML = html;
```
"No data yet" if the list is empty. For each row: your own rows get the class `mine` (yellow), then the ID, each column's value (`getValue` reads paths like `teacher.name`), and the allowed buttons.

#### `buttons(item, i)`: which buttons a row gets
```js
    var mine = isMine(item);
    var canEdit = isAdmin() || mine;
    var canDelete = isAdmin() ? !mine : mine;   // admin: everything except their own account, others: only their own rows
```
- **Edit**: the admin can edit any row; others only their own.
- **Delete**: the admin can delete anything except their own account; others only their own rows.

```js
    var html = "";
    if (current == "courses" && me.role == "STUDENT") html += "<button onclick='registerCourse(" + i + ")'>Register</button> ";
    if (canEdit) html += "<button onclick='editRow(" + i + ")'>Edit</button> ";
    if (canDelete) html += "<button onclick='deleteRow(" + i + ")'>" + (current == "registrations" ? "Drop" : "Delete") + "</button> ";
    if (mine && TABS[current].password) html += "<i>(you)</i>";
    return html == "" ? "-" : html;
```
- Students get **Register** on every course.
- Then come **Edit** and **Delete** (**Drop** for registrations) if allowed.
- "(you)" marks your own account row.
- If there is no button at all, show "-".

#### `showField(field)`: is a form box shown?
```js
    var rule = field[2] || "";
    if (rule.indexOf("admin") >= 0 && !isAdmin()) return false;
    if (rule.indexOf("new") >= 0 && editing != null) return false;
    return true;
```
A box with the rule `"admin"` is hidden for non-admins, and a box with the rule `"new"` is hidden while editing. That is why a student editing a registration sees only **Section**.

#### `buildForm()`, `resetForm()`, `editRow(i)`
```js
function buildForm() {
    var html = "";
    TABS[current].form.forEach(function (f) {
        if (showField(f)) {
            html += "<div class='form-row'><label>" + f[1] + "</label><input id='f-" + f[0] + "' autocomplete='off'></div>";
        }
    });
    if (TABS[current].password) {
        html += "<div class='form-row'><label>Password</label><input id='f-password' type='password' autocomplete='new-password'></div>";
    }
    document.getElementById("form").innerHTML = html;
}
```
One label and box for each allowed field. The box id is `f-` + the path (e.g. `f-course.code`). The people tabs also get a password box. `autocomplete` stops the browser from filling in your own saved login.

```js
function resetForm() {
    editing = null;
    buildForm();
    document.getElementById("formTitle").innerText = "Add New " + singular(current);
    document.getElementById("formBox").style.display = canAdd() ? "block" : "none";
}
```
An empty "Add New ..." form. It is **shown only if you may add** in this tab (`block` = show, `none` = hide).

```js
function editRow(i) {
    editing = items[i];
    buildForm();
    var title = "Edit " + singular(current) + " (ID " + editing.id + ")";
    if (isMine(editing) && TABS[current].password) title = "Edit My Account";
    if (TABS[current].password) title += " - leave password empty to keep the old one";
    document.getElementById("formTitle").innerText = title;
    document.getElementById("formBox").style.display = "block";
    TABS[current].form.forEach(function (f) {
        if (showField(f)) {
            document.getElementById("f-" + f[0]).value = getValue(editing, f[0]);
        }
    });
}
```
Remember the row being edited, build the boxes, set the title, show the form, and copy the row's values into the boxes.

#### `save()`: CREATE (POST) or UPDATE (PUT)
```js
    var data = {};
    TABS[current].form.forEach(function (f) {
        if (showField(f)) {
            setValue(data, f[0], document.getElementById("f-" + f[0]).value);
        }
    });
    if (TABS[current].password) {
        data.password = document.getElementById("f-password").value;
    }
```
Read the boxes into a JSON object. `setValue` turns a path into nested objects, e.g. `course.code` → `{course: {code: "CSE3230"}}`, which is exactly what the server expects.

```js
    var isEdit = editing != null;
    var myAccount = isEdit && isMine(editing) && TABS[current].password;
    var url = isEdit ? "/api/" + current + "/" + editing.id : "/api/" + current;

    fetch(url, {
        method: isEdit ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
```
Editing → `PUT /api/courses/3`. Adding → `POST /api/courses`. The data is sent as JSON.

```js
    .then(function (res) {
        if (!res.ok) {
            showError(res);
            return;
        }
        if (myAccount && (data.username != me.username || data.password != "")) {
            alert("Your login details were changed. Please log in again.");
            location.href = "/logout";
            return;
        }
        showMessage(isEdit ? "Updated successfully (status " + res.status + ")" : "Added successfully (status " + res.status + ")", "success");
        resetForm();
        loadList();
    });
```
- On an error: show it in red, and the form keeps your input.
- If you changed **your own** username or password: you're logged out, so you log in with the new details.
- Otherwise: show a green message, reset the form and reload the table.

#### `deleteRow(i)`: DELETE
```js
    var question = "Delete this " + singular(current).toLowerCase() + " (ID " + item.id + ")?";
    if (current == "courses") question = "Delete course " + item.code + "? Its registrations are deleted too.";
    if (current == "registrations") question = "Drop " + item.course.code + " for " + item.student.name + "?";
    if (myAccount) question = "Delete YOUR OWN account? You will be logged out.";
    if (!confirm(question)) {
        return;
    }
```
Ask a clear "Are you sure?" question for each case. If you click Cancel, stop.

```js
    fetch("/api/" + current + "/" + item.id, { method: "DELETE" }).then(function (res) {
        if (!res.ok) { showError(res); return; }
        if (myAccount) { location.href = "/logout"; return; }
        showMessage("Deleted successfully (status " + res.status + ")", "success");
        resetForm();
        loadList();
    });
```
Send DELETE. If you deleted your own account, you're logged out. Otherwise show the message and reload.

#### `registerCourse(i)`: a student registers
```js
    var course = items[i];
    var section = prompt("Register for " + course.code + " - " + course.title + "\nSection (for example A or B):", "A");
    if (section == null) {
        return;
    }
```
A small box asks for the section (default "A"). Cancel → stop.

```js
    fetch("/api/registrations", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ course: { code: course.code }, section: section })
    }).then(function (res) {
        if (!res.ok) { showError(res); return; }
        showMessage("Registered for " + course.code + " (status " + res.status + "). See My Registrations.", "success");
    });
```
`POST /api/registrations` with `{"course":{"code":"CSE3230"},"section":"A"}`. The server knows who the student is from the login. Already registered → "Error 409: already registered in this course".

#### Small helpers

| Function | What it does |
|---|---|
| `getValue(obj, "teacher.name")` | Follows the path through the objects (`obj.teacher.name`). Returns empty text if something is missing. |
| `setValue(obj, "teacher.username", "t1")` | Creates the nested object: `obj.teacher = {username: "t1"}`. |
| `showMessage(text, type)` | Writes into `#msg`. `type` is `"success"` (green) or `"error"` (red). |
| `showError(res)` | Reads the server's answer `{"error":"..."}` and shows "Error 403: you can only edit your own courses". |
| `capital("teachers")` | Returns "Teachers". |
| `singular("teachers")` | Returns "Teacher" (removes the last letter "s"). |
| `clean(value)` | Turns `<` into `&lt;`, so HTML typed into a field can't run on the page. |

---

### 8.19 `resources/static/style.css`

| Block | What it styles |
|---|---|
| `body` | Arial font, light grey background, no outer margin. |
| `.login-box` (+ `label`, `input`, `select`, `button`) | The white 320px box in the middle of the login page, with full-width boxes and button. |
| `.header`, `.header .right`, `.header a` | The dark blue top bar, with "Logged in as ... \| Logout" on the right in white. |
| `.container` | The white content box, centered, max 950px wide. |
| `.tabs button` / `.tabs button.active` | Tab buttons. The open tab is dark blue with white text. |
| `table`, `th, td`, `th` | Table lines, spacing and a grey header row. |
| `tr.mine td` | Light yellow background for **your own** rows. |
| `.hint` | Grey help text. |
| `.form-row`, `.form-row label`, `.form-row input` | Form rows. The label has a fixed width so the boxes line up. |
| `.error` / `.success` | Red / green message text. |

---

### 8.20 `Dockerfile`

The recipe for a **Docker image** of the app, in **two stages**: build with Maven, then run with Java only.

| Line | Meaning |
|---|---|
| `# Step 1: build the jar file using Maven` | A comment. |
| `FROM maven:3.9-eclipse-temurin-17 AS build` | Start from an image that has **Maven + Java 17**. This stage is called `build`. |
| `WORKDIR /app` | Work in the `/app` folder inside the container. |
| `COPY pom.xml .` | Copy `pom.xml` in. |
| `COPY src ./src` | Copy the source code in. |
| `RUN mvn -q package -DskipTests` | Build `target/ums.jar` (`-q` = quiet, `-DskipTests` = skip tests). |
| `# Step 2: run the jar file using only Java` | A comment. |
| `FROM eclipse-temurin:17-jre` | A **new, smaller** image with only the Java runtime. |
| `WORKDIR /app` | Work in `/app`. |
| `COPY --from=build /app/target/ums.jar app.jar` | Copy **only the built jar** from the first stage. |
| `EXPOSE 8081` | Says the app uses port 8081 (documentation). |
| `ENTRYPOINT ["java", "-jar", "app.jar"]` | The command run when the container starts. |

---

### 8.21 `compose.yaml`

It starts **two services together**: `db` (MySQL) and `app` (our app).

| Line(s) | Meaning |
|---|---|
| `services:` | The list of containers. |
| `db:` | Service 1, named **db**. |
| `image: mysql:8.0` | The official MySQL 8 image. |
| `MYSQL_ROOT_PASSWORD: root` | The MySQL root password. |
| `MYSQL_DATABASE: university_db` | Create the database `university_db` on the first start. |
| `ports: - "3307:3306"` | PC port 3307 → MySQL port 3306, so a DB tool can connect to `localhost:3307`. 3307 avoids a clash with an installed MySQL. |
| `volumes: - db_data:/var/lib/mysql` | Keep the MySQL data in a **named volume**, so it survives restarts. |
| `healthcheck:` ... `mysqladmin ping` | Every 5 s, ask "is MySQL ready?", up to 20 times. |
| `app:` | Service 2, our app. |
| `build: .` | Build its image from the `Dockerfile` here. |
| `ports: - "8081:8081"` | Open the app at `http://localhost:8081`. |
| `SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/university_db?...` | **Overrides** the H2 setting, so the app uses MySQL. The host is **`db`**, the service name, because containers in one compose file reach each other by name. `allowPublicKeyRetrieval=true&useSSL=false` lets the driver log in to MySQL 8 without SSL. |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | The MySQL login for the app. |
| `depends_on: db: condition: service_healthy` | Start the app only **after MySQL is healthy**. |
| `restart: on-failure` | If the app crashes (e.g. MySQL wasn't fully ready), start it again. |
| `volumes: db_data:` | Declares the named volume. |

---

### 8.22 `.dockerignore` and `.gitignore`

- **`.dockerignore`**: files Docker should **not** copy into the image: `target` (old builds), `data` (the local H2 database), and IDE settings (`.idea`, `.vscode`, `*.iml`).
- **`.gitignore`**: files Git should **not** save: `target/`, `data/`, `.idea/`, `*.iml`. Build output and the local database don't belong in the repository.

---

## 9. Docker explained

### Basic words

| Word | Simple meaning |
|---|---|
| **Image** | A packed box with everything needed to run a program (system files + Java + our jar). Like a **class**. |
| **Container** | A running image. Like an **object** made from the class. |
| **Dockerfile** | The recipe to **build an image** of our app. |
| **Docker Compose** | Runs **several containers together** (app + MySQL) from one file, `compose.yaml`. |
| **Volume** | Storage outside the container, so the data isn't lost when the container is removed. |
| **Port mapping `8081:8081`** | `PC port : container port`. It lets your browser reach the app inside the container. |

### What happens on `docker compose up --build`
```
docker compose up --build
   │
   ├── db  container  ← image "mysql:8.0" (downloaded from Docker Hub)
   │      waits until healthy
   │
   └── app container  ← image built from OUR Dockerfile
          1) stage 1: Maven builds ums.jar
          2) stage 2: Java runs ums.jar
          3) app connects to MySQL at "db:3306", creates the tables + sample data
          4) open http://localhost:8081
```

### Commands (run them in the project folder)

| Command | What it does |
|---|---|
| `docker --version` | Check that Docker is installed. |
| `docker compose up --build` | **Build and start everything.** Use it the first time and after every code change. Stop with `Ctrl + C`. |
| `docker compose up -d --build` | The same, in the background (`-d` = detached). |
| `docker compose ps` | Show the running containers. |
| `docker compose logs app` | Show the app's logs (`-f` to keep following). |
| `docker compose stop` | Stop the containers (`docker compose start` starts them again). |
| `docker compose down` | Stop **and remove** the containers. The MySQL data stays in the volume. |
| `docker compose down -v` | Remove the containers **and** the MySQL data, for a fresh start. |
| `docker images` | List the images on your PC. |
| `docker build -t ums .` | Build only the app image (`-t ums` = name it "ums", `.` = the Dockerfile is here). |

> **Why not just `docker run` the image?** The app alone would have no MySQL to connect to. **Compose** starts MySQL next to it and connects them. That is why the task asks for `compose.yaml`.

You can also see the containers, logs and stop buttons in **Docker Desktop → Containers**.

---

## 10. Viva questions and answers

| Question | Answer |
|---|---|
| What is Spring Boot? | Spring with automatic configuration and a built-in web server, so a web app starts with just a `main` method. |
| How do you create a Spring Boot project? | On **start.spring.io**: choose Maven, Java 17, add the dependencies, Generate, then open the folder in IntelliJ. |
| Which file runs first? | `UmsApplication.java` (`@SpringBootApplication` + `main()`). |
| What does `@SpringBootApplication` do? | It turns on auto-configuration and scans the package for components. |
| What is `pom.xml`? | The Maven file with the project info and libraries (dependencies). |
| What is `application.properties`? | The settings file (port, database). It is loaded automatically. |
| What is an Entity? | A Java class mapped to a database table (`@Entity`). |
| What is `@ManyToOne`? | A link from many rows to one row, e.g. many courses → one teacher. It becomes a foreign key column (`teacher_id`). |
| What does `@OnDelete(CASCADE)` do? | It adds `ON DELETE CASCADE` to the foreign key: deleting a teacher deletes their courses, and deleting a course deletes its registrations. |
| What is a Repository? | An interface for database work. Spring Data JPA writes the code (`findAll`, `save`, `deleteById`...). |
| How does `findByCourseTeacherUsername` work? | Spring reads the method name and follows the links registration → course → teacher → username to build the query. |
| What is a RestController? | A class that handles HTTP requests and returns JSON. |
| What is REST? | URLs for resources (`/api/courses`) and HTTP methods for actions: GET read, POST create, PUT update, DELETE delete. |
| `@PathVariable` vs `@RequestBody`? | `@PathVariable` takes a value from the URL (`/courses/5`). `@RequestBody` takes the JSON body. |
| Which status codes do you use? | 200, 201, 204, 400, 401, 403, 404, 409 (see section 5). |
| 401 vs 403? | **401** = not logged in or wrong password. **403** = logged in, but not allowed. |
| How are passwords stored? | As **BCrypt hashes** (`BCryptPasswordEncoder`), never as plain text. They are also never returned in JSON (`WRITE_ONLY`). |
| What is BCrypt? | A one-way hash with a random salt. At login, the typed password is checked against the hash. |
| How does login work? | The form posts role, username and password to `/login`. Spring calls `MyUserDetailsService`, which looks in the chosen role's table. Spring compares the password with BCrypt and creates a session. |
| How does logout work? | `/logout` ends the session (configured in `SecurityConfig`). |
| How does RBAC work? | In `SecurityConfig`: URL + method rules (`hasRole` / `hasAnyRole`), e.g. `/api/admins/**` needs ADMIN, POST teachers or students needs ADMIN, GET courses needs any role. |
| Role check vs ownership check? | **Role** ("may teachers use /api/courses?") is in `SecurityConfig`. **Ownership** ("is this course yours?") is in the controller, comparing the record's username with `auth.getName()`. |
| Can a teacher change another teacher's course? | No → 403 "you can only edit your own courses". |
| Why does GET /api/registrations return different data? | The controller checks the role: the admin gets all, a teacher gets the registrations in their courses, a student gets their own. |
| Can a student register twice in one course? | No → 409, checked with `existsByStudentAndCourse`. |
| Why can't a teacher add teachers? | In a real university, the **admin** creates accounts. POST on teachers or students is ADMIN only. |
| What is the Dockerfile for? | It builds an image of the app. Multi-stage: Maven builds the jar, a small Java image runs it. |
| What is compose.yaml for? | It runs the app and MySQL together, with ports, a volume and a health check. |
| How does the app find MySQL in Docker? | By the service name `db`: `jdbc:mysql://db:3306/...`. |
| Why H2 in IntelliJ and MySQL in Docker? | H2 needs no installation. Docker gives a real MySQL. Environment variables in compose switch the app to MySQL. |
| What does `ddl-auto=update` do? | Hibernate creates or updates the tables from the entities automatically. |

---

## 11. Common problems

| Problem | Solution |
|---|---|
| Red code / "Cannot resolve symbol" in IntelliJ | Maven tab → 🔄 **Reload All Maven Projects**. Check that **File → Project Structure → SDK** is **17**. |
| No green ▶ next to `main` | Wait for Maven to finish, or right-click `UmsApplication.java` → **Run**. |
| `Port 8081 was already in use` | The app is already running (in IntelliJ or Docker). Stop the other one first. |
| "Wrong username, password or role!" | Check the **Login as** role. Passwords are case-sensitive. |
| Teacher/student sees `-` instead of buttons | Correct. That row belongs to someone else. You can only change your own (yellow) rows. |
| "Error 403: you can only edit your own ..." | You tried to change someone else's record. Only the admin can do that. |
| "Error 409: already registered in this course" | The student is already in that course. Change the section in **My Registrations** instead. |
| "Error 400: teacher not found" (admin adding a course) | Type an existing teacher **username** (e.g. `teacher1`), not the name. |
| Logged out right after saving my own account | You changed your own username or password. Log in again with the new details. |
| Forgot a user's password | Log in as **admin** → open the tab → **Edit** the user → type a new password → **Save**. |
| Want fresh data (IntelliJ) | Stop the app, delete the `data` folder, run again. |
| Want fresh data (Docker) | `docker compose down -v`, then `docker compose up --build`. |
| `docker` not recognised / cannot connect | Install and **start Docker Desktop**, then wait for "Engine running". |
| Code changed but Docker shows the old version | Use `docker compose up --build`. |
| App in Docker says `Communications link failure` | MySQL wasn't ready yet. The app restarts automatically, so wait a few seconds. |
