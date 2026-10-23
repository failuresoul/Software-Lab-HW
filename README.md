# University Management System
### CSE 3230 – Home Task (Lab 4) · Java Spring Boot · Spring Security · Docker

This is a simple web application with three user roles: **Admin**, **Teacher** and **Student**.
This README explains **everything**: how to run it (IntelliJ and Docker), how to use it, **how to present it to your teacher**, and what every file and line of code does.

---

## The task and how this project meets it

| Requirement from the task | How this project does it | Where to look |
|---|---|---|
| **REST API**: CRUD (Create, Read, Update, Delete) for Admin, Teacher and Student, with REST conventions, JSON responses and HTTP status codes | `GET` / `POST` / `PUT` / `DELETE` on `/api/admins`, `/api/teachers`, `/api/students`. Answers are JSON with status codes 200, 201, 204, 400, 401, 403, 404, 409 | `controller/` folder |
| **Spring Security**: login, logout, password hashing, role-based access control (RBAC) | Login page with role selection, `/logout`, **BCrypt** password hashes, URL rules per role, plus "own account only" checks | `security/` folder + controllers |
| **Dockerization**: Dockerfile and compose.yaml, services run together with Docker Compose | `Dockerfile` builds the app image. `compose.yaml` runs the **app + MySQL** together | `Dockerfile`, `compose.yaml` |
| **Note**: Admins manage all three resources, teachers only teachers, students only students | Admin → all 3. Teacher → only `/api/teachers` (403 for the others). Student → only `/api/students` (403 for the others) | `SecurityConfig.java` |

### The role rule (from the task's note)

| Logged in as | Admins | Teachers | Students |
|---|:---:|:---:|:---:|
| **Admin**   | ✅ | ✅ | ✅ |
| **Teacher** | ❌ 403 | ✅ | ❌ 403 |
| **Student** | ❌ 403 | ❌ 403 | ✅ |

Inside their own section, teachers and students work like in a real university:

| Action | Admin | Teacher (in Teachers) | Student (in Students) |
|---|---|---|---|
| **Read**: see all records | ✅ all three lists | ✅ all teachers | ✅ all students |
| **Create**: add a record | ✅ | ❌ only the admin creates accounts | ❌ only the admin creates accounts |
| **Update**: edit a record | ✅ anyone | ✅ **only their own account** | ✅ **only their own account** |
| **Delete**: remove a record | ✅ anyone **except themselves** | ✅ **only their own account** | ✅ **only their own account** |

### Extra (beyond the task): Courses and Course Registration

| | Admin | Teacher | Student |
|---|---|---|---|
| **Courses** | full CRUD, chooses the teacher | sees all; **adds own courses**; edits and deletes **only own** | sees all; clicks **Register** |
| **Registrations** | full CRUD | sees **students in own courses** (read only) | **My Registrations**: register, change section, drop |

---

## Table of contents

1. [What you need to install](#1-what-you-need-to-install)
2. [Run the project in IntelliJ IDEA](#2-run-the-project-in-intellij-idea)
3. [Docker: the complete guide](#3-docker-the-complete-guide)
4. [Accounts and sample data](#4-accounts-and-sample-data)
5. [How to use the app](#5-how-to-use-the-app)
6. [REST API reference](#6-rest-api-reference)
7. [⭐ How to present the project to your teacher](#7--how-to-present-the-project-to-your-teacher)
8. [Project structure](#8-project-structure)
9. [Spring Boot basics](#9-spring-boot-basics)
10. [Every file explained line by line](#10-every-file-explained-line-by-line)
11. [Viva questions and answers](#11-viva-questions-and-answers)
12. [Common problems](#12-common-problems)

---

## 1. What you need to install

| Tool | Needed for | Download | Check it works (type in a terminal) |
|---|---|---|---|
| **IntelliJ IDEA** (Community, free) | opening and running the project | https://www.jetbrains.com/idea/download | – |
| **Java JDK 17** | running Java (IntelliJ can download it for you) | inside IntelliJ, or https://adoptium.net | `java -version` → shows `17` |
| **Docker Desktop** | the Docker part of the task | https://www.docker.com/products/docker-desktop | `docker --version` and `docker compose version` |
| **Postman** (free) | showing the REST API to your teacher | https://www.postman.com/downloads | – |
| A web browser | using the app | – | – |

> You do **not** need to install MySQL or Maven.
> - In IntelliJ, the app uses a built-in database file (**H2**).
> - In Docker, MySQL runs inside a container.
> - IntelliJ already contains Maven.

---

## 2. Run the project in IntelliJ IDEA

### 2.1 First time: open the project

1. Start **IntelliJ IDEA**.
2. On the welcome screen click **Open** (or use **File → Open...** in an open window).
3. Find and select the folder **`Software Lab-HW`**, the one that contains `pom.xml`. Click **OK**.
4. If it asks *"Trust and Open Project?"*, click **Trust Project**.
5. IntelliJ sees `pom.xml` and knows it is a **Maven project**. It starts downloading all libraries. Look at the **progress bar at the bottom right** and **wait until it disappears**. The first time this takes 2–5 minutes and needs internet.

### 2.2 First time: set Java 17

1. Go to **File → Project Structure...** (or press `Ctrl + Alt + Shift + S`).
2. On the left, click **Project**.
3. In **SDK**, choose a **17** version.
   - If there is no 17: click the SDK list → **Download JDK...** → Version **17**, any vendor (e.g. *Eclipse Temurin*) → **Download**.
4. Click **OK**.

### 2.3 Run the app

1. In the **Project** panel on the left, open:
   ```
   Software Lab-HW
   └── src
       └── main
           └── java
               └── com.university.ums
                   └── UmsApplication.java     ← open this file
   ```
2. You see a **green ▶ triangle** next to `public class UmsApplication` (line 7). Click it → **Run 'UmsApplication.main()'**.
3. The **Run** window opens at the bottom, and many log lines appear. Wait for this line:
   ```
   Started UmsApplication in 4.5 seconds
   ```
4. Open the browser at **http://localhost:8081**. The login page appears.
5. Log in with **Login as: Admin**, username **`admin`**, password **`admin123`**.

### 2.4 Stop, run again, reset

| To... | Do this |
|---|---|
| **Stop** the app | Click the red **■ Stop** button in the Run window (or `Ctrl + F2`). |
| **Run again** | Click the green **▶ Run** button at the top right (or `Shift + F10`). |
| **Restart after changing code** | Stop, then Run again. |
| **Start with fresh data** | Stop the app → delete the **`data`** folder in the project → Run again. The sample data from [section 4](#4-accounts-and-sample-data) is created again. |

> **Where is my data?** In the file `data/ums.mv.db` inside the project. It stays when you stop the app.

### 2.5 If something goes wrong in IntelliJ

| Problem | Fix |
|---|---|
| Red underlined code, "Cannot resolve symbol" | Open the **Maven** tab on the right edge → click 🔄 **Reload All Maven Projects**. Wait for it to finish. |
| No green ▶ next to the class | Maven isn't finished yet, so wait. Or right-click `UmsApplication.java` → **Run 'UmsApplication.main()'**. |
| `Port 8081 was already in use` | The app is already running (maybe in Docker or another Run window). Stop the other one first. |
| "java: error: release version 17 not supported" | The SDK isn't 17. Repeat [2.2](#22-first-time-set-java-17). |
| Login says "Wrong username, password or role!" | Choose the correct **Login as** role. Passwords are case-sensitive. |

### 2.6 Optional: run with Maven in a terminal
If Maven is installed: open IntelliJ's **Terminal** tab (bottom) and type `mvn spring-boot:run`. Stop with `Ctrl + C`.

---

## 3. Docker: the complete guide

### 3.1 What is Docker? (simple explanation)

Docker runs programs inside **containers**. A container is like a small, separate computer that already has everything the program needs. So the app runs **the same way on every PC**, without installing Java or MySQL on the PC itself.

| Word | Simple meaning | In this project |
|---|---|---|
| **Image** | A packed "box" with everything a program needs (system files + Java + our app). Like a **class** in Java. | `mysql:8.0` (downloaded) and **our app's image** (built from the `Dockerfile`) |
| **Container** | A **running** image. Like an **object** created from a class. | 2 containers: **db** and **app** |
| **Dockerfile** | A **recipe** that says how to build an image. | Builds our Spring Boot app into an image |
| **Docker Compose** | Starts **several containers together** from one file. | `compose.yaml` starts **MySQL + our app** |
| **Volume** | Storage outside the container, so data is not lost. | `db_data` keeps the MySQL data |
| **Port mapping** `8081:8081` | `PC port : container port`, so the browser on your PC can reach the container. | app → 8081, MySQL → 3307 |
| **Docker Hub** | An online store of ready images. | `mysql`, `maven`, `eclipse-temurin` come from there |

### 3.2 How THIS project uses Docker

```
                         your PC (Windows)
 ┌────────────────────────────────────────────────────────────────────┐
 │  Browser / Postman                                                 │
 │      │  http://localhost:8081                                      │
 │      ▼                                                             │
 │  ┌───────────────── Docker (started by compose.yaml) ───────────┐  │
 │  │                                                              │  │
 │  │   app container                     db container             │  │
 │  │  ┌──────────────────────┐          ┌──────────────────────┐  │  │
 │  │  │ Java 17              │  JDBC    │ MySQL 8              │  │  │
 │  │  │ ums.jar (our app)    │ ───────► │ database             │  │  │
 │  │  │ port 8081            │ "db:3306"│ "university_db"      │  │  │
 │  │  └──────────────────────┘          └──────────┬───────────┘  │  │
 │  │   built from our Dockerfile                   │ volume       │  │
 │  │                                               ▼ "db_data"    │  │
 │  └──────────────────────────────────────────────────────────────┘  │
 └────────────────────────────────────────────────────────────────────┘
```

| | Running in **IntelliJ** | Running in **Docker** |
|---|---|---|
| Database | **H2** (a file in `data/`) | **MySQL 8** (its own container) |
| Who starts the app | You, with the ▶ button | Docker Compose |
| Address | http://localhost:8081 | http://localhost:8081 (same) |
| How the database is chosen | `application.properties` | `compose.yaml` **overrides** it with environment variables |

**The code is exactly the same in both cases.** Only the database settings change, and Docker Compose changes them from outside.

### 3.3 Install Docker Desktop on Windows (once)

1. **Download** Docker Desktop: https://www.docker.com/products/docker-desktop → *Download for Windows*.
2. **Run the installer.** Keep **"Use WSL 2 instead of Hyper-V"** ticked → **OK**.
3. **Restart** the PC when it asks.
4. Start **Docker Desktop** from the Start menu. Accept the terms, and skip sign-in if you want (you don't need an account).
5. Wait until the bottom-left corner shows **"Engine running"** (green).
6. Check in a terminal (CMD, PowerShell or IntelliJ's Terminal):
   ```
   docker --version
   docker compose version
   ```
   Both print a version number → Docker works. ✅

| Install problem | Fix |
|---|---|
| "WSL 2 installation is incomplete" | Open **PowerShell as Administrator**, run `wsl --install`, restart the PC. |
| "Virtualization must be enabled" | Restart → open BIOS (usually `F2`, `F10` or `Del` while starting) → enable **Intel VT-x** / **AMD-V (SVM)** → save and restart. |
| `docker` is not recognized | Docker Desktop isn't installed, or the terminal was opened before installing. Close and reopen the terminal. |
| "Cannot connect to the Docker daemon" | Docker Desktop isn't running. Start it and wait for "Engine running". |

### 3.4 Run the project with Docker, step by step

1. **Start Docker Desktop** and wait for **"Engine running"**.
2. **Stop the app in IntelliJ** if it is running (red ■), because both use port **8081**.
3. Open a terminal **in the project folder**. The easiest way is IntelliJ's **Terminal** tab at the bottom, which already opens in the project folder.
4. Type:
   ```
   docker compose up --build
   ```
   What happens now:
   1. Docker downloads the images **mysql:8.0**, **maven** and **eclipse-temurin** (only the first time, a few minutes).
   2. It **builds our app image** from the `Dockerfile`, with Maven compiling the code inside Docker.
   3. It **starts the db container** (MySQL) and waits until MySQL is healthy.
   4. It **starts the app container**, which connects to MySQL, creates the tables and adds the sample data.
5. Wait for this line in the output:
   ```
   app-1  | ... Started UmsApplication in 7.9 seconds
   ```
6. Open **http://localhost:8081** and log in (`admin` / `admin123`).
7. **To stop:** press `Ctrl + C` in the terminal, then type:
   ```
   docker compose down
   ```

> **Run in the background instead:** `docker compose up -d --build` (`-d` = detached). The terminal is free again. See the logs with `docker compose logs -f app` (stop watching with `Ctrl + C`).

### 3.5 Check that the containers are running

```
docker compose ps
```
The output looks similar to this (the names come from the folder name):
```
NAME                 IMAGE                SERVICE   STATUS                   PORTS
softwarelab-hw-app-1 softwarelab-hw-app   app       Up 2 minutes             0.0.0.0:8081->8081/tcp
softwarelab-hw-db-1  mysql:8.0            db        Up 2 minutes (healthy)   0.0.0.0:3307->3306/tcp
```
**Two services run together**: `app` and `db`. That is exactly what the task asks for.

You can also see them in **Docker Desktop → Containers**, with their logs and Start/Stop buttons.

### 3.6 Look inside MySQL (proof of password hashing)

While the containers are running, type:
```
docker compose exec db mysql -uroot -proot university_db -e "SELECT username, password FROM teacher;"
```
The output looks like this:
```
+----------+--------------------------------------------------------------+
| username | password                                                     |
+----------+--------------------------------------------------------------+
| teacher1 | $2a$10$FiyCgK1LufVmK89JZ3FxAeHCubH7a8zaIWMc.BS271UE2rZ3XokDW |
+----------+--------------------------------------------------------------+
```
The password column holds a **BCrypt hash** (it starts with `$2a$10$`), **not** `teacher123`. That proves **password hashing**.
(The warning *"Using a password on the command line interface can be insecure"* is normal; ignore it.)

Other useful commands:
```
docker compose exec db mysql -uroot -proot university_db -e "SHOW TABLES;"
docker compose exec db mysql -uroot -proot university_db -e "SELECT * FROM course;"
```

### 3.7 After you change the code

Docker uses the **image** it built before. After changing code, **build again**:
```
docker compose up --build
```

### 3.8 Stop and clean up

| Command | What it does | Your data |
|---|---|---|
| `Ctrl + C` | Stops the containers when they run in the foreground. | kept |
| `docker compose stop` | Stops the containers (start again: `docker compose start`). | kept |
| `docker compose down` | Stops **and removes** the containers. | **kept** (in the volume) |
| `docker compose down -v` | Removes the containers **and the volume**, for a fresh start next time. | **deleted** |

### 3.9 All Docker commands in one place

| Command | Meaning |
|---|---|
| `docker --version` | Is Docker installed? |
| `docker compose up --build` | Build the app image and start app + MySQL (with logs on the screen). |
| `docker compose up -d --build` | The same, but in the background. |
| `docker compose ps` | Which containers are running. |
| `docker compose logs app` | The app's logs (`-f` keeps following). |
| `docker compose logs db` | MySQL's logs. |
| `docker compose exec db mysql -uroot -proot university_db` | Open the MySQL console inside the db container (type `exit;` to leave). |
| `docker compose down` | Stop and remove the containers. |
| `docker compose down -v` | The same + delete the MySQL data. |
| `docker images` | List the images on your PC. |
| `docker build -t ums .` | Build only the app image from the Dockerfile. |

### 3.10 The `Dockerfile` line by line

The Dockerfile has **two stages**. Stage 1 **builds** the jar with Maven. Stage 2 **runs** it with only Java. The final image is small because it doesn't contain Maven or the source code.

```dockerfile
# Step 1: build the jar file using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# Step 2: run the jar file using only Java
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/ums.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

| Line | Meaning |
|---|---|
| `# Step 1: ...` | A comment. |
| `FROM maven:3.9-eclipse-temurin-17 AS build` | Start from an image that has **Maven 3.9 + Java 17**. This stage is called `build`. |
| `WORKDIR /app` | Inside the container, work in the folder `/app` (it is created if missing). |
| `COPY pom.xml .` | Copy `pom.xml` from the PC into `/app`. |
| `COPY src ./src` | Copy the `src` folder (all the code) into `/app/src`. |
| `RUN mvn -q package -DskipTests` | Run Maven: compile and package into `/app/target/ums.jar`. `-q` = quiet, `-DskipTests` = don't run tests. |
| `# Step 2: ...` | A comment. |
| `FROM eclipse-temurin:17-jre` | Start a **new, small** image with only the **Java 17 runtime**. |
| `WORKDIR /app` | Work in `/app`. |
| `COPY --from=build /app/target/ums.jar app.jar` | Copy **only the jar** from stage 1 and name it `app.jar`. |
| `EXPOSE 8081` | Says the app listens on port 8081 (documentation). |
| `ENTRYPOINT ["java", "-jar", "app.jar"]` | The command that runs when the container starts: start our app. |

### 3.11 The `compose.yaml` line by line

```yaml
services:
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: university_db
    ports:
      - "3307:3306"
    volumes:
      - db_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-proot"]
      interval: 5s
      timeout: 5s
      retries: 20

  app:
    build: .
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/university_db?allowPublicKeyRetrieval=true&useSSL=false
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      db:
        condition: service_healthy
    restart: on-failure

volumes:
  db_data:
```

| Line(s) | Meaning |
|---|---|
| `services:` | The list of containers to run together. |
| `db:` | Service 1, named **db**. The name is also its address inside Docker. |
| `image: mysql:8.0` | Use the official **MySQL 8** image from Docker Hub. |
| `MYSQL_ROOT_PASSWORD: root` | Set the MySQL root password to `root`. |
| `MYSQL_DATABASE: university_db` | Create a database named `university_db` on the first start. |
| `ports: - "3307:3306"` | PC port **3307** → MySQL's port 3306 in the container. 3307 avoids a clash with a MySQL installed on the PC. |
| `volumes: - db_data:/var/lib/mysql` | Save MySQL's files (`/var/lib/mysql`) in the volume **db_data**, so the data survives `docker compose down`. |
| `healthcheck:` | How Docker checks that MySQL is ready. |
| `test: [... "mysqladmin", "ping" ...]` | Run `mysqladmin ping`. If it answers, MySQL is **healthy**. |
| `interval: 5s` / `timeout: 5s` / `retries: 20` | Check every 5 seconds, wait at most 5 seconds per check, and try up to 20 times. |
| `app:` | Service 2, named **app**: our Spring Boot app. |
| `build: .` | Build the image from the **Dockerfile in this folder** (`.`). |
| `ports: - "8081:8081"` | PC port 8081 → app port 8081, so **http://localhost:8081** works. |
| `SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/...` | **Replaces** the H2 database setting with MySQL. The host is **`db`**, the service name above, because containers in one compose file find each other by service name. `allowPublicKeyRetrieval=true&useSSL=false` lets the Java driver log in to MySQL 8 without SSL. |
| `SPRING_DATASOURCE_USERNAME: root` / `..._PASSWORD: root` | The MySQL login for the app. |
| `depends_on: db: condition: service_healthy` | Start the app **only after MySQL is healthy**. |
| `restart: on-failure` | If the app crashes (for example, MySQL was not 100% ready), Docker starts it again. |
| `volumes: db_data:` | Declares the named volume used by `db`. |

> **How do the environment variables change the database?** Spring Boot reads settings from `application.properties` **and** from environment variables. An environment variable named `SPRING_DATASOURCE_URL` **wins over** `spring.datasource.url` in the file. So the same code uses H2 in IntelliJ and MySQL in Docker.

### 3.12 Docker problems while running

| Problem | Fix |
|---|---|
| `Bind for 0.0.0.0:8081 failed: port is already allocated` | Something else uses 8081, usually the app in IntelliJ. Stop it and run again. |
| `Bind for 0.0.0.0:3307 failed` | Change `"3307:3306"` to `"3308:3306"` in `compose.yaml`. |
| App logs show `Communications link failure` | MySQL wasn't ready yet. The app restarts by itself (`restart: on-failure`), so wait a few seconds. |
| The app shows old behaviour after code changes | Run `docker compose up --build`, with `--build`. |
| You want empty tables again | `docker compose down -v`, then `docker compose up --build`. |
| Build fails with download errors | Check the internet connection (Maven downloads libraries inside Docker) and try again. |

---

## 4. Accounts and sample data

When the database is empty, `DataLoader.java` creates:

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

---

## 5. How to use the app

### 5.1 Login page
1. **Login as**: choose **Admin**, **Teacher** or **Student**.
2. Type the **Username** and **Password**.
3. Click **Login**.

A wrong password **or a wrong role** shows *"Wrong username, password or role!"*.

### 5.2 The dashboard
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
- **Yellow rows are yours**: your account, your courses, your registrations.
- **`-`** means the row belongs to someone else, so you can't change it.
- The **Add New** form appears only where you are allowed to add.

### 5.3 What each role can do

| Role | Tabs | What they can do |
|---|---|---|
| **Admin** | Admins · Teachers · Students · Courses · Registrations | **Everything** in every tab. When adding a course, type the teacher's **username**. When adding a registration, type the **course code** and the student's **username**. The admin can't delete their own admin account. |
| **Teacher** | Teachers · Courses · Registrations | **Teachers:** see all; edit or delete own account. **Courses:** see all; **add** courses (they become the teacher); edit or delete **own** courses. **Registrations:** see students registered in **own** courses (read only). |
| **Student** | Students · Courses · My Registrations | **Students:** see all; edit or delete own account. **Courses:** see all; click **Register** (asks for a section). **My Registrations:** **Edit** = change section, **Drop** = leave the course. |

### 5.4 How to do each operation

| To... | Do this |
|---|---|
| **Read** (see all) | Open a tab. The table reloads after every change. |
| **Create** (add) | Fill the **Add New ...** form below the table → **Save**. |
| **Update** (edit) | Click **Edit** on a row → change the values → **Save**. For people, leave the password empty to keep the old one. |
| **Delete** | Click **Delete** (**Drop** for a registration) → confirm. |
| **Register for a course** | Student → **Courses** → **Register** → type a section, e.g. `A` → **OK**. |
| **Log out** | **Logout**, top right. |

**Good to know:**
- If you change **your own** username or password, you are logged out, so you can log in with the new details.
- If you delete **your own** account, you are logged out.
- Deleting a **course** also deletes its registrations.
- Deleting a **teacher** also deletes their courses (and those courses' registrations).
- Deleting a **student** also deletes their registrations.
- **Passwords are never shown.** If someone forgets theirs, the **admin** edits the user and types a new one.

---

## 6. REST API reference

### 6.1 People: `/api/admins`, `/api/teachers`, `/api/students`

| Method | URL | Action | Who may do it | Success | Errors |
|---|---|---|---|---|---|
| GET    | `/api/{type}`      | list all | that section's role + admin | **200 OK** | 401, 403 |
| GET    | `/api/{type}/{id}` | get one  | that section's role + admin | **200 OK** | 401, 403, 404 |
| POST   | `/api/{type}`      | create   | **admin only** | **201 Created** | 400, 401, 403, 409 |
| PUT    | `/api/{type}/{id}` | update   | admin, or the **owner** (own account) | **200 OK** | 400, 401, 403, 404, 409 |
| DELETE | `/api/{type}/{id}` | delete   | admin (not themselves), or the **owner** | **204 No Content** | 400, 401, 403, 404 |

### 6.2 Courses: `/api/courses`

| Method | URL | Action | Who | Success | Errors |
|---|---|---|---|---|---|
| GET    | `/api/courses`      | list all | everyone logged in | **200** | 401 |
| GET    | `/api/courses/{id}` | get one  | everyone logged in | **200** | 401, 404 |
| POST   | `/api/courses`      | create   | admin, teacher (the teacher becomes the course's teacher) | **201** | 400, 401, 403, 409 |
| PUT    | `/api/courses/{id}` | update   | admin, or the course's **own teacher** | **200** | 400, 401, 403, 404, 409 |
| DELETE | `/api/courses/{id}` | delete (and its registrations) | admin, or the course's **own teacher** | **204** | 401, 403, 404 |

### 6.3 Registrations: `/api/registrations`

| Method | URL | Action | Who | Success | Errors |
|---|---|---|---|---|---|
| GET    | `/api/registrations`      | list: **admin** all, **teacher** in own courses, **student** own | everyone logged in | **200** | 401 |
| GET    | `/api/registrations/{id}` | get one (only if you may see it) | everyone logged in | **200** | 401, 403, 404 |
| POST   | `/api/registrations`      | register a student in a course | admin, student (registers themself) | **201** | 400, 401, 403, 409 |
| PUT    | `/api/registrations/{id}` | change the section | admin, or the **own student** | **200** | 400, 401, 403, 404 |
| DELETE | `/api/registrations/{id}` | drop the course | admin, or the **own student** | **204** | 401, 403, 404 |

### 6.4 Login and others

| Method | URL | What it does | Result |
|---|---|---|---|
| GET  | `/api/me` | who is logged in | `{"username":"teacher1","role":"TEACHER"}` |
| POST | `/login`  | log in (form fields: `role`, `username`, `password`) | 302 → `/index.html`, or `/login.html?error` |
| GET  | `/logout` | log out | 302 → `/login.html?logout` |

### 6.5 Status codes used

| Code | Name | Meaning in this app |
|---|---|---|
| **200** | OK | Read or update worked. |
| **201** | Created | A new record was created (POST). |
| **204** | No Content | Deleted; there is nothing to send back. |
| **400** | Bad Request | Required data is missing, a teacher/course/student wasn't found, or the admin tried to delete themselves. |
| **401** | Unauthorized | Not logged in, or wrong password. |
| **403** | Forbidden | Logged in, but your role may not do this, or the record isn't yours. |
| **404** | Not Found | No record with this id. |
| **409** | Conflict | The username or course code already exists, or the student is already registered. |

### 6.6 JSON bodies

```json
// admin
{ "name": "Second Admin", "email": "a2@university.edu", "username": "admin2", "password": "pass123" }

// teacher
{ "name": "Rahim Uddin", "email": "rahim@university.edu", "department": "CSE",
  "designation": "Lecturer", "username": "teacher1", "password": "teacher123" }

// student
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
Responses **never contain a password**. Example response for a teacher:
```json
{ "id": 1, "name": "Rahim Uddin", "email": "rahim@university.edu", "department": "CSE",
  "designation": "Lecturer", "username": "teacher1" }
```

### 6.7 Testing with Postman
**The easy way:** import the ready collection `UMS.postman_collection.json` (see [7.2](#72-get-postman-ready-import-the-collection)).

**By hand:**
1. Choose the method and URL, e.g. `GET http://localhost:8081/api/teachers`.
2. **Authorization** tab → **Basic Auth** → username + password (e.g. `admin` / `admin123`).
3. For POST and PUT: **Body** → **raw** → **JSON** → paste a body from 6.6.
4. **Send**, then look at the **status code** (top right of the response) and the JSON.

---

## 7. ⭐ How to present the project to your teacher

The teacher will check **three things** plus the **role note**. This plan shows **each one clearly**, in order, with what to click and what to say. It takes about **10–15 minutes**.

### 7.1 Prepare before the presentation (the day before)

| ✔ | Task |
|---|---|
| ☐ | IntelliJ installed, project opens, **runs** (section 2). |
| ☐ | Docker Desktop installed, `docker compose up --build` has run **at least once** (so the images are already downloaded and the demo is fast). |
| ☐ | Postman installed, collection **imported** (7.2). |
| ☐ | **Fresh data:** stop the app and delete the `data` folder (IntelliJ), or run `docker compose down -v` (Docker). |
| ☐ | Read [section 11 (viva questions)](#11-viva-questions-and-answers). |
| ☐ | Decide: demo with **Docker** (recommended, it shows all 3 requirements in one run) or with IntelliJ + Docker at the end. |

**Right before the presentation:**
1. Start **Docker Desktop** and wait for "Engine running".
2. Open the project in **IntelliJ** (to show the code).
3. Open **Postman** with the collection.
4. Open the **browser**.

### 7.2 Get Postman ready (import the collection)

The project contains **`UMS.postman_collection.json`**: **40 ready requests** in 3 folders, matching the 3 requirements. Each request has a **test** that checks the status code, so Postman shows ✅ **PASS** next to it.

1. Open Postman → click **Import** (top left).
2. Drag the file **`UMS.postman_collection.json`** from the project folder into the window (or click *files* and choose it) → **Import**.
3. On the left you now see **"University Management System - Demo"** with 3 folders:
   - **1. REST API - CRUD + JSON + status codes (as admin)**
   - **2. Spring Security - login, roles (RBAC), own account**
   - **3. Extra - Courses and Registrations**
4. **Run requests one by one:** click a request → **Send**. You'll see the JSON, the status code and **Test Results**.
5. **Run a whole folder at once:** hover over the folder → **⋯** → **Run folder** → **Run**. Every request shows PASS or FAIL.

> The requests already contain the right username and password (Authorization tab → Basic Auth) and the right JSON body. The address is in the collection **Variables** tab (`baseUrl = http://localhost:8081`).
> **Run the folders in order (1 → 2 → 3).** Some requests use ids saved by earlier requests.

### 7.3 The presentation plan

| # | Part | Time | Shows the requirement |
|---|---|---|---|
| 1 | Introduction | 1 min | – |
| 2 | Start with Docker | 2 min | **Dockerization** |
| 3 | Short code tour | 2 min | structure of all three |
| 4 | REST API with Postman | 3 min | **REST API: CRUD, JSON, status codes** |
| 5 | Security in the browser + Postman | 4 min | **Login, logout, hashing, RBAC** + the role note |
| 6 | Extra: courses and registrations | 1–2 min | bonus |
| 7 | Stop Docker + summary | 1 min | – |

---

### Step 1: Introduction (1 minute)

**Say:**
> "I built a University Management System with Java Spring Boot. It has three roles: Admin, Teacher and Student.
> It has a REST API with full CRUD for all three, Spring Security for login, logout, BCrypt password hashing and role-based access, and it runs with Docker Compose as two services: the app and a MySQL database.
> As required, the admin can manage all three resources, teachers only teachers, and students only students."

---

### Step 2: Dockerization (2 minutes)

1. In IntelliJ, open **`Dockerfile`** and **say:**
   > "The Dockerfile has two stages. The first uses a Maven image to build the jar. The second copies only the jar into a small Java 17 runtime image and runs it on port 8081."
2. Open **`compose.yaml`** and **say:**
   > "Compose runs two services together: `db` is MySQL 8 with a volume for the data and a health check, and `app` is built from my Dockerfile. The app waits until MySQL is healthy, and environment variables point it at MySQL using the service name `db`."
3. In IntelliJ's **Terminal**, run:
   ```
   docker compose up -d --build
   ```
   Then:
   ```
   docker compose ps
   ```
   **Show** the two containers, `app` and `db (healthy)`. **Say:** "Both services are running together."
4. Open **http://localhost:8081**. The login page appears. **Say:** "The app is now running from Docker."

> If building takes too long during the presentation, run `docker compose up -d --build` **before** the presentation starts and just show `docker compose ps`.

---

### Step 3: Short code tour (2 minutes)

In IntelliJ, open the **Project** panel and show the folders. **Say** one line per folder:

| Folder / file | What to say |
|---|---|
| `model/` | "Entities: each class is a database table: Admin, Teacher, Student (plus Course and Registration)." |
| `repository/` | "Spring Data JPA repositories: I only write interfaces, and Spring makes the database code." |
| `controller/` | "REST controllers: one per resource, with GET, POST, PUT and DELETE." |
| `security/SecurityConfig.java` | "All security rules: login, logout, BCrypt, and which role may use which URL." |
| `security/MyUserDetailsService.java` | "Loads the user at login from the table of the selected role." |
| `application.properties` | "Port 8081 and the database settings." |

---

### Step 4: Requirement 1, REST API (3 minutes)

1. Open **`TeacherController.java`** and point at:
   - `@RestController` + `@RequestMapping("/api/teachers")` → "returns JSON, all URLs start with /api/teachers"
   - `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` → "the four CRUD operations"
   - `ResponseEntity.status(HttpStatus.CREATED)`, `notFound()`, `noContent()` → "proper status codes"
2. In **Postman**, open folder **"1. REST API"** and **Send** these one by one (or run the folder):

| Request | Show the teacher |
|---|---|
| Teachers: GET all → 200 | the JSON list and **200 OK** |
| Teachers: POST create → 201 | **201 Created**, the new JSON with its `id`, and **no password** in the answer |
| Teachers: GET one → 200 | reading one record by id |
| Teachers: PUT update → 200 | the changed `designation` |
| Teachers: POST same username → 409 | **409 Conflict** + `{"error":"username already exists"}` |
| Teachers: POST without password → 400 | **400 Bad Request** |
| Teachers: DELETE → 204 | **204 No Content** |
| Teachers: GET deleted one → 404 | **404 Not Found** |

**Say:**
> "Every resource follows REST conventions: the URL is the resource, the HTTP method is the action, the body is JSON, and every answer has the right status code. Admins and Students work the same way."
> Then run the Admins and Students requests too (or run the whole folder: all PASS ✅).

---

### Step 5: Requirement 2, Spring Security (4 minutes)

#### a) Login with role, and logout (browser)
1. On the login page, choose **Login as: Teacher**, `teacher1` / `teacher123` → **Login**. The dashboard shows **"Logged in as teacher1 (TEACHER)"** and only the **Teachers** tab (plus Courses and Registrations).
2. Click **Logout** → *"You have been logged out."*
3. **Wrong role:** choose **Login as: Student**, `teacher1` / `teacher123` → *"Wrong username, password or role!"*.
   **Say:** "The selected role decides which table is checked, so a teacher can't log in as a student."
4. Show **`SecurityConfig.java`**: `formLogin(...)` (login) and `logout(...)` (logout).

#### b) Password hashing
1. Show **`SecurityConfig.java`** → `new BCryptPasswordEncoder()`, and **`TeacherController.java`** → `passwordEncoder.encode(...)` in `create`.
2. Show the **hash in the database**:
   - **Docker:**
     ```
     docker compose exec db mysql -uroot -proot university_db -e "SELECT username, password FROM teacher;"
     ```
   - **IntelliJ (H2)**, while the app is running, in IntelliJ's Terminal (PowerShell):
     ```
     java -cp "$env:USERPROFILE\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar" org.h2.tools.Shell -url "jdbc:h2:file:./data/ums;AUTO_SERVER=TRUE" -user sa -sql "SELECT USERNAME, PASSWORD FROM TEACHER"
     ```
     (In CMD, write `%USERPROFILE%` instead of `$env:USERPROFILE`.)
3. **Say:**
   > "Passwords are stored as BCrypt hashes like `$2a$10$...`, never as plain text. BCrypt is one-way with a random salt. The password is also never sent back in JSON, because of `@JsonProperty(WRITE_ONLY)`."

#### c) Role-based access control (RBAC)
1. Show the rules in **`SecurityConfig.java`**:
   ```java
   .requestMatchers("/api/admins/**").hasRole("ADMIN")
   .requestMatchers(HttpMethod.POST, "/api/teachers/**", "/api/students/**").hasRole("ADMIN")
   .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
   .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "STUDENT")
   ```
   **Say:** "This is exactly the task's note: admin → all three, teacher → only teachers, student → only students."
2. In **Postman**, run folder **"2. Spring Security"** (or Send one by one):

| Request | Result | What it proves |
|---|---|---|
| No login → 401 | **401** | you must log in |
| Wrong password → 401 | **401** | wrong passwords are rejected |
| Teacher reads teachers → 200 | **200** | teacher → teachers ✅ |
| Teacher reads students → 403 | **403** | teacher ✗ students |
| Teacher reads admins → 403 | **403** | teacher ✗ admins |
| Student reads students → 200 | **200** | student → students ✅ |
| Student reads teachers / admins → 403 | **403** | student ✗ others |
| Teacher creates a teacher → 403 | **403** | only the admin creates accounts |
| Teacher edits OWN account → 200 | **200** | own account ✅ |
| Teacher edits / deletes ANOTHER teacher → 403 | **403** | not someone else's ✗ |
| Admin deletes OWN account → 400 | **400** | the admin can't remove themselves |

3. **In the browser**, log in as **student1** and show: only the **Students** tab (plus Courses and My Registrations), every student is visible, but **Edit/Delete only on their own yellow row**.
   **Say:**
   > "There are two levels of security: `SecurityConfig` checks the **role** for each URL, and the controllers check that a teacher or student only changes **their own** record."

---

### Step 6: Extra, courses and registrations (1–2 minutes)

In the browser:
1. As **teacher1** → **Courses** → add a course (e.g. `CSE3210`, *Software Engineering*, 3, 3-2) → it appears **yellow** (yours).
2. **Logout** → as **student1** → **Courses** → **Register** on CSE3210 → section `A` → **My Registrations** → **Edit** (change section) → **Drop**.
3. As **teacher1** → **Registrations** → see the students in your courses.

(Or run Postman folder **"3. Extra"**.)

**Say:**
> "To make teacher and student work realistic, teachers manage their own courses and students register for courses. The tables are linked with `@ManyToOne` foreign keys, and deleting a course also deletes its registrations."

---

### Step 7: Stop and summary (1 minute)

```
docker compose down
```
**Say:**
> "To summarise: there is a REST API with CRUD for Admin, Teacher and Student, with JSON and correct status codes. Spring Security handles login, logout, BCrypt hashing and role-based access, so admin manages all, teacher only teachers, and student only students. And the app is containerized: a Dockerfile builds it, and Docker Compose runs it together with MySQL."

---

### 7.4 If something goes wrong during the demo

| Problem | Quick fix |
|---|---|
| Docker build is slow or has no internet | Run the app from **IntelliJ** (▶) and only **show** the Dockerfile and compose.yaml. |
| Port 8081 already in use | Stop the IntelliJ app or `docker compose down`. Only one of the two can run at a time. |
| Postman request fails with 409 | The data from an earlier run still exists. Use fresh data (`docker compose down -v`, or delete `data/`), or just explain that 409 = already exists. |
| Postman request fails with 404 on `{{demoTeacherId}}` | Run the folder **from the top**. Earlier requests save the ids. |
| Login fails | Check the **Login as** role and the password (section 4). |

### 7.5 Requirement checklist (show this if the teacher asks "where is X?")

| Teacher asks... | Show this |
|---|---|
| "Where is CRUD?" | `controller/*Controller.java` → `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` |
| "Where are the status codes?" | `ResponseEntity.ok`, `status(HttpStatus.CREATED)`, `noContent()`, `notFound()`, `badRequest()`, `HttpStatus.CONFLICT`, `HttpStatus.FORBIDDEN` |
| "Where is JSON?" | `@RestController` + `@RequestBody` (Jackson converts Java ↔ JSON automatically) |
| "Where is login / logout?" | `SecurityConfig.java` → `formLogin(...)`, `httpBasic(...)`, `logout(...)` + `login.html` |
| "Where is password hashing?" | `SecurityConfig.passwordEncoder()` → `BCryptPasswordEncoder`, `passwordEncoder.encode(...)` in the controllers, the hash in the database |
| "Where is RBAC?" | `SecurityConfig.java` → `hasRole` / `hasAnyRole`; `MyUserDetailsService` → `.roles("TEACHER")`; controllers → `isAdmin(auth)` / own-account checks |
| "Where is Docker?" | `Dockerfile`, `compose.yaml`, `docker compose ps` |
| "How do the services talk?" | `compose.yaml` → `SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/...` (`db` = the service name) |

---

## 8. Project structure

```
Software Lab-HW
├── pom.xml                              Maven: project info + libraries
├── Dockerfile                           recipe to build the app image
├── compose.yaml                         runs app + MySQL together
├── .dockerignore                        files Docker should not copy
├── .gitignore                           files Git should not save
├── UMS.postman_collection.json          ready Postman requests for the presentation
├── README.md                            this file
├── data/                                (created when the app runs in IntelliJ) H2 database file
├── target/                              (created when building) compiled code + ums.jar
└── src/main
    ├── java/com/university/ums
    │   ├── UmsApplication.java          ▶ starting point of the app
    │   ├── DataLoader.java              creates the sample data
    │   │
    │   ├── model/                       DATABASE TABLES (entities)
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
    │       ├── SecurityConfig.java      URL rules, login, logout, BCrypt
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

### 8.1 The layers (how the code is organized)

```
  Browser / Postman
        │  HTTP + JSON
        ▼
  ┌─────────────────────┐
  │ Spring Security     │  security/   → logged in? right role?
  ├─────────────────────┤
  │ Controllers         │  controller/ → URLs, checks, status codes
  ├─────────────────────┤
  │ Repositories        │  repository/ → save / find / delete
  ├─────────────────────┤
  │ Entities            │  model/      → the tables
  └─────────────────────┘
        │  JDBC
        ▼
  Database (H2 in IntelliJ, MySQL in Docker)
```

### 8.2 Database tables and how they connect

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
- One teacher → **many** courses (`course.teacher_id`).
- One course → **many** registrations, and one student → **many** registrations.
- These links are **foreign keys** with **ON DELETE CASCADE**: deleting the "one" side deletes the "many" rows automatically.

### 8.3 How one request travels through the app

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
Database  → UPDATE course ... WHERE id = 1
   ▼
Response: 200 OK + the course as JSON → app.js shows "Updated successfully" and reloads the table
```

---

## 9. Spring Boot basics

### 9.1 What is Spring Boot?
- **Spring** is a big Java framework for making web applications.
- **Spring Boot** is Spring with **ready-made settings**. You add "starter" libraries (web, security, JPA) and they work with almost no setup.
- It has a **web server (Tomcat) built in**. Running the `main` method starts a website.

### 9.2 How is a Spring Boot project created?
1. Go to **https://start.spring.io** (Spring Initializr).
2. Choose **Maven**, **Java**, Spring Boot **3.x**, Java **17**, Packaging **Jar**.
3. Group `com.university`, Artifact `ums`.
4. **Add dependencies:** Spring Web, Spring Data JPA, Spring Security, H2 Database, MySQL Driver.
5. **Generate** → unzip → open the folder in IntelliJ (*File → Open*).

### 9.3 Which files are created automatically?

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

**Everything else was written for this project:** `DataLoader`, the `model`, `repository`, `controller` and `security` folders, the 4 web files, `Dockerfile`, `compose.yaml`, `.dockerignore`, the Postman collection and this README.

### 9.4 "Which file runs first / automatically?"
**`UmsApplication.java`** (`@SpringBootApplication` + `main()`). When it runs, Spring Boot **automatically**:
1. loads **`application.properties`**,
2. **scans** `com.university.ums` and its sub-packages, and creates every `@RestController`, `@Service`, `@Component`, `@Configuration` and repository,
3. connects to the **database** and **creates the tables** from the `@Entity` classes,
4. runs **`DataLoader`**, which adds the sample data,
5. starts **Tomcat** on port **8081**,
6. serves **`static/index.html`** at `http://localhost:8081/`. Because of security, someone who isn't logged in is sent to `login.html` first.

---

## 10. Every file explained line by line

### 10.1 `pom.xml`

| Line(s) | Meaning |
|---|---|
| `<?xml version="1.0" encoding="UTF-8"?>` | This is an XML file. |
| `<project xmlns=...>` | The root tag of every Maven project. The `xmlns` parts are fixed boilerplate. |
| `<modelVersion>4.0.0</modelVersion>` | The POM format version. It is always 4.0.0. |
| `<parent> spring-boot-starter-parent 3.3.5 </parent>` | Use Spring Boot 3.3.5's default settings, so we don't need versions for each library. |
| `<groupId>com.university</groupId>` | The organisation name. |
| `<artifactId>ums</artifactId>` | The project name. |
| `<version>1.0</version>` | Our version. |
| `<name>University Management System</name>` | A readable name. |
| `<java.version>17</java.version>` | Use Java 17. |
| `spring-boot-starter-web` | Web server (Tomcat) + REST API + JSON (Jackson). |
| `spring-boot-starter-data-jpa` | Database work with Java classes (Hibernate) instead of SQL. |
| `spring-boot-starter-security` | Login, logout, password hashing, roles. |
| `h2` (scope `runtime`) | The file database used in IntelliJ. `runtime` = only needed when the app runs. |
| `mysql-connector-j` (scope `runtime`) | The MySQL driver, used in Docker. |
| `<finalName>ums</finalName>` | The built file is named `target/ums.jar` (the Dockerfile uses this name). |
| `spring-boot-maven-plugin` | Builds one runnable jar with all libraries inside. |

---

### 10.2 `UmsApplication.java`

```java
package com.university.ums;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(UmsApplication.class, args);
    }
}
```
| Line | Meaning |
|---|---|
| `package com.university.ums;` | The package (folder) of this class. |
| `import ...` | The two Spring Boot classes we use. |
| `@SpringBootApplication` | Marks the **main class**: auto-configuration + scanning of this package and its sub-packages. |
| `public static void main(String[] args)` | The Java starting point. |
| `SpringApplication.run(UmsApplication.class, args);` | Starts everything: Spring, the database, Tomcat on port 8081. |

---

### 10.3 `DataLoader.java`

Creates the sample data so you can log in right away.

```java
// runs once when the app starts and adds sample data (only into empty tables)
@Component
public class DataLoader implements CommandLineRunner {
```
`@Component` = Spring creates this class. **`CommandLineRunner`** = Spring calls its `run` method **once, right after start-up**.

```java
    private final AdminRepository adminRepository;
    ... (teacher, student, course, registration repositories and the PasswordEncoder)

    public DataLoader(AdminRepository adminRepository, ...) {
        this.adminRepository = adminRepository;
        ...
    }
```
**Constructor injection:** Spring passes in the 5 repositories and the password encoder.

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
`count()` = the number of rows. If there are **no admins**, create one with a **hashed password** (`encode`), and `save` it (INSERT). The same blocks follow for `teacher1` and `student1`.

```java
        // two courses of teacher1
        Optional<Teacher> teacher1 = teacherRepository.findByUsername("teacher1");
        if (courseRepository.count() == 0 && teacher1.isPresent()) {
            courseRepository.save(newCourse("CSE3230", "Software Development Lab", 1.5, "3-2", teacher1.get()));
            courseRepository.save(newCourse("CSE3201", "Operating Systems", 3.0, "3-2", teacher1.get()));
        }
```
If there are no courses and teacher1 exists, create two courses taught by teacher1.

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
If there are no registrations, register student1 in CSE3230, section A.

```java
    private Course newCourse(String code, String title, Double credit, String semester, Teacher teacher) {
        Course course = new Course();
        course.setCode(code);
        course.setTitle(title);
        course.setCredit(credit);
        course.setSemester(semester);
        course.setTeacher(teacher);
        return course;
    }
```
A small helper that builds a Course object.

---

### 10.4 `model/Admin.java`, `Teacher.java`, `Student.java`

A **model** (entity) is a Java class that becomes a **database table**: each object is a **row**, each field is a **column**. Here is `Admin.java`:

```java
package com.university.ums.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
```
`jakarta.persistence.*` = the JPA (database) annotations. `JsonProperty` comes from Jackson (JSON).

```java
@Entity
public class Admin {
```
**`@Entity`** → a table named `admin`.

```java
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
```
`@Id` = **primary key**. `IDENTITY` = the database numbers new rows automatically (1, 2, 3...).

```java
    private String name;

    private String email;
```
Normal columns.

```java
    @Column(unique = true)
    private String username;
```
`unique = true` = no two admins with the same username.

```java
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
```
Holds the BCrypt **hash**. `WRITE_ONLY` = the password can be **received** in JSON but is **never sent back**.

```java
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    ... getName/setName, getEmail/setEmail, getUsername/setUsername, getPassword/setPassword
```
**Getters and setters**: read and change the private fields. JPA and Jackson use them too.

| Model | Table | Fields |
|---|---|---|
| `Admin`   | `admin`   | id, name, email, username, password |
| `Teacher` | `teacher` | id, name, email, **department**, **designation**, username, password |
| `Student` | `student` | id, name, email, **roll**, **department**, username, password |

---

### 10.5 `model/Course.java`

```java
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private String title;

    private Double credit;

    private String semester;
```
The table `course`: id, a **unique** code (e.g. `CSE3230`), the title, the credit (`Double`, so `1.5` works) and the semester (e.g. `3-2`).

```java
    // the teacher of this course (when the teacher is deleted, their courses are deleted too)
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Teacher teacher;
```
- **`@ManyToOne`**: **many** courses → **one** teacher. This becomes the column `teacher_id`, a **foreign key** to `teacher`.
- **`@OnDelete(CASCADE)`**: the database gets `ON DELETE CASCADE`, so deleting a teacher deletes their courses.
- In JSON, the course contains the whole teacher object (without the password).

Then come the getters and setters.

---

### 10.6 `model/Registration.java`

```java
// a student registered in a course
@Entity
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // when the student is deleted, their registrations are deleted too
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;

    // when the course is deleted, its registrations are deleted too
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Course course;

    private String section;
```
The table `registration`:
- `student_id` → which student (many registrations → one student),
- `course_id` → which course (many registrations → one course),
- `section` (e.g. `A`).

Deleting the student or the course deletes the registration (CASCADE). Then come the getters and setters.

---

### 10.7 The repositories

A **repository** = database access. We write only an **interface**, and Spring writes the code.

**`AdminRepository.java`** (Teacher and Student are the same):
```java
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);
}
```
| Line | Meaning |
|---|---|
| `extends JpaRepository<Admin, Long>` | For entity `Admin` with an id of type `Long`, we get `findAll()`, `findById(id)`, `save(obj)`, `deleteById(id)`, `existsById(id)` and `count()` **for free**. |
| `findByUsername` | Spring reads the **method name** and builds `SELECT * FROM admin WHERE username = ?`. `Optional` = maybe found, maybe not. Used at login. |
| `existsByUsername` | `true` if the username is taken → used for 409. |

**`CourseRepository.java`:**
```java
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);

    boolean existsByCode(String code);
}
```
Find a course by its code, and check whether a code is already used.

**`RegistrationRepository.java`:**
```java
    // registrations of one student
    List<Registration> findByStudentUsername(String username);

    // registrations in the courses of one teacher
    List<Registration> findByCourseTeacherUsername(String username);

    boolean existsByStudentAndCourse(Student student, Course course);
```
| Method | Meaning |
|---|---|
| `findByStudentUsername` | Follows registration → **student** → **username**. Used for "My Registrations". |
| `findByCourseTeacherUsername` | Follows registration → **course** → **teacher** → **username**. Used for "Registrations in My Courses". |
| `existsByStudentAndCourse` | Is this student already in this course? → 409. |

---

### 10.8 `controller/AdminController.java`

A **controller** receives HTTP requests and returns responses. **This is the REST API.**

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
Imports. `web.bind.annotation.*` gives `@RestController`, `@GetMapping` and the other annotations.

```java
@RestController
@RequestMapping("/api/admins")
public class AdminController {
```
`@RestController` = methods return **data → JSON automatically**. `@RequestMapping("/api/admins")` = every URL here starts with `/api/admins`.

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
All admins as a JSON array → **200 OK**.

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
`@PathVariable` takes `{id}` from the URL (`/api/admins/5` → 5). `ResponseEntity` lets us choose the status: **404** if missing, **200** + the admin if found.

#### CREATE: `POST /api/admins`
```java
    // CREATE -> 201, 400 or 409
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Admin admin) {
        if (isBlank(admin.getName()) || isBlank(admin.getUsername()) || isBlank(admin.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "name, username and password are required"));
        }
        if (adminRepository.existsByUsername(admin.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        admin.setId(null);
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(adminRepository.save(admin));
    }
```
| Line | Meaning |
|---|---|
| `@RequestBody Admin admin` | Turns the JSON body into an `Admin` object. |
| `ResponseEntity<?>` | The answer can be an Admin or an error message. |
| `if (isBlank(...))` → `badRequest()` | Missing name, username or password → **400** + `{"error": "..."}`. |
| `existsByUsername` → `CONFLICT` | Username taken → **409**. |
| `admin.setId(null);` | Ignore any id that was sent, so the database makes a new one. |
| `passwordEncoder.encode(...)` | **Hash the password** with BCrypt. |
| `status(HttpStatus.CREATED).body(save(...))` | Save (INSERT) → **201 Created** + the new admin. |

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
        Admin admin = found.get();
        if (!admin.getUsername().equals(newData.getUsername()) && adminRepository.existsByUsername(newData.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        admin.setName(newData.getName());
        admin.setEmail(newData.getEmail());
        admin.setUsername(newData.getUsername());
        if (!isBlank(newData.getPassword())) {
            admin.setPassword(passwordEncoder.encode(newData.getPassword()));
        }
        return ResponseEntity.ok(adminRepository.save(admin));
    }
```
| Step | Meaning |
|---|---|
| `findById` → `notFound()` | No such admin → **404**. |
| `isBlank(name / username)` | Empty → **400**. (The password *may* be empty, which means "keep the old one".) |
| Username changed and taken | → **409**. |
| `setName / setEmail / setUsername` | Copy the new values. |
| `if (!isBlank(password)) encode` | Change the password only if a new one was typed, and hash it. |
| `ok(save(admin))` | `save` on an existing id = **UPDATE** → **200 OK**. |

#### DELETE: `DELETE /api/admins/{id}`
```java
    // DELETE -> 204, 400 or 404  (an admin cannot delete their own account)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
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
`Authentication auth` = **the logged-in user** (Spring passes it in), and `auth.getName()` = their username. Missing → **404**. Deleting yourself → **400**, so the system doesn't lose the admin who is using it. Otherwise delete → **204 No Content**.

```java
    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
```
`true` if the text is missing or only spaces.

---

### 10.9 `TeacherController.java` and `StudentController.java`

They are the same as `AdminController` (URL `/api/teachers` or `/api/students`, with their own fields), plus the **"own account only"** rule. Here is `TeacherController`:

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
        ... (the same 400 / 409 checks, copy name, email, department, designation, username, password)
    }
```
**The ownership check:** not an admin **and** not your own record → **403 Forbidden**. `StudentController` copies `roll` and `department` instead.

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
The same check for delete.

```java
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
```
`getAuthorities()` = the user's roles. `anyMatch(... "ROLE_ADMIN")` = is one of them ADMIN?

There is no ownership check in `create`, because `SecurityConfig` lets **only the admin** POST teachers and students.

---

### 10.10 `controller/CourseController.java`

```java
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
```
URLs start with `/api/courses`. It needs the teacher repository to find a course's teacher.

```java
    // READ all -> 200  (everyone can see all courses)
    @GetMapping
    public List<Course> getAll() {
        return courseRepository.findAll();
    }
```
All courses. `getOne` works like before (200 / 404).

**CREATE:**
```java
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Course course, Authentication auth) {
        if (isBlank(course.getCode()) || isBlank(course.getTitle())) {
            return ResponseEntity.badRequest().body(Map.of("error", "course code and title are required"));
        }
        if (courseRepository.existsByCode(course.getCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "course code already exists"));
        }
        Optional<Teacher> teacher = findTeacher(course, auth);
        if (teacher.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "teacher not found"));
        }
        course.setId(null);
        course.setTeacher(teacher.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(courseRepository.save(course));
    }
```
Code and title are required (**400**), and the code must be new (**409**). Find the teacher (**400** if not found), link them, save → **201**.

**UPDATE:**
```java
        Course course = found.get();
        if (!isAdmin(auth) && !course.getTeacher().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only edit your own courses"));
        }
        ... 400 / 409 checks ...
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
A teacher can only edit **their own** course (**403**). Only the admin may change the teacher. Copy the values → **200**.

**DELETE:** the same ownership check (**403**), then delete → **204**. Its registrations are deleted too (CASCADE).

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
A **teacher** is always the teacher of their new course (anything they send is ignored). The **admin** sends `"teacher": {"username": "..."}`.

---

### 10.11 `controller/RegistrationController.java`

```java
@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
```
URLs start with `/api/registrations`. It needs the registration, course and student repositories.

**READ all: different data for each role**
```java
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
Admin → all. Teacher → in **their** courses. Student → **their own**.

**READ one:** 404 if missing, **403** if `canSee(...)` is false ("this is not your registration").

**CREATE: register for a course**
```java
        if (isBlank(registration.getSection())) {
            return ResponseEntity.badRequest().body(Map.of("error", "section is required"));
        }
        Optional<Course> course = Optional.empty();
        if (registration.getCourse() != null) {
            course = courseRepository.findByCode(registration.getCourse().getCode());
        }
        if (course.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "course not found"));
        }
        Optional<Student> student = findStudent(registration, auth);
        if (student.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "student not found"));
        }
        if (registrationRepository.existsByStudentAndCourse(student.get(), course.get())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "already registered in this course"));
        }
        registration.setId(null);
        registration.setCourse(course.get());
        registration.setStudent(student.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationRepository.save(registration));
```
- A section is required → **400**.
- Find the course by its **code** → **400** if not found.
- Find the student (a student = themselves, the admin = by username) → **400** if not found.
- Already registered → **409**.
- Otherwise link and save → **201**.

**UPDATE: change the section only**
```java
        if (!isOwnerOrAdmin(registration, auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only change your own registrations"));
        }
        if (isBlank(newData.getSection())) { ... 400 ... }
        registration.setSection(newData.getSection());
        return ResponseEntity.ok(registrationRepository.save(registration));
```
Only the admin or the owner (**403** otherwise). Only the section changes. To change the course, drop it and register again.

**DELETE: drop the course.** Admin or owner only (**403**) → **204**.

**Helpers:**

| Method | Meaning |
|---|---|
| `findStudent(data, auth)` | A student → the logged-in student. The admin → the student with the username sent in JSON. |
| `isOwnerOrAdmin(registration, auth)` | Admin, or the registration's student is the logged-in user. |
| `canSee(registration, auth)` | Admin → yes. Teacher → if it is in their course. Student → if it is theirs. |
| `hasRole(auth, "TEACHER")` | `true` if the user has `ROLE_TEACHER`. |

---

### 10.12 `controller/MeController.java`

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
`GET /api/me` → `{"username":"teacher1","role":"TEACHER"}`. It takes the user's role (`ROLE_TEACHER`) and removes `ROLE_`. The dashboard uses it to show the name and choose the tabs.

---

### 10.13 `security/SecurityConfig.java`

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
**Password hashing:** one BCrypt encoder, used everywhere. BCrypt is **one-way** with a random salt. At login, Spring checks the typed password against the hash.

```java
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
```
The **filter chain** = the security checks every request passes through. CSRF tokens are turned off to keep REST testing with Postman simple, which is common in student REST projects.

```java
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login.html", "/style.css", "/error").permitAll()
```
**RBAC starts here.** Rules are checked **from top to bottom**, and the **first match wins**. Anyone can open the login page, the CSS and the error page.

```java
                .requestMatchers("/api/admins/**").hasRole("ADMIN")
```
Admins: **ADMIN only**.

```java
                // only the admin can add new teachers and students
                .requestMatchers(HttpMethod.POST, "/api/teachers/**", "/api/students/**").hasRole("ADMIN")
```
**Creating** teachers or students: **ADMIN only**. This line must come **before** the next two.

```java
                .requestMatchers("/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "STUDENT")
```
Teachers: **ADMIN or TEACHER**. Students: **ADMIN or STUDENT**. This is **the task's note**.

```java
                // everyone can see courses, only the admin and teachers can change them
                .requestMatchers(HttpMethod.GET, "/api/courses/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                .requestMatchers("/api/courses/**").hasAnyRole("ADMIN", "TEACHER")
                // everyone can see registrations (the controller filters them), only the admin and students can change them
                .requestMatchers(HttpMethod.GET, "/api/registrations/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                .requestMatchers("/api/registrations/**").hasAnyRole("ADMIN", "STUDENT")
```
Courses: everyone can **read**, admin and teacher can **change**. Registrations: everyone can **read** (filtered per role), admin and student can **change**.

```java
                .anyRequest().authenticated()
            )
```
Everything else (dashboard, app.js, /api/me) needs any logged-in user. Not logged in → **401** (a browser is sent to the login page instead). Wrong role → **403**.

```java
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index.html", true)
                .failureUrl("/login.html?error")
                .permitAll()
            )
```
**Login:**
- `loginPage` = our page.
- `loginProcessingUrl` = the form posts to `/login`, and Spring checks the password itself.
- `defaultSuccessUrl` = after a successful login → the dashboard.
- `failureUrl` = after a failed login → back with `?error`.

```java
            .httpBasic(Customizer.withDefaults())
```
This also allows **HTTP Basic** login (username and password in a header), which Postman and curl use.

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
**Logout:** `/logout` ends the session, then goes to the login page with `?logout`. `http.build()` creates the chain.

> **Two levels of security:**
> - **`SecurityConfig`** checks the **role** for each URL and method.
> - **The controllers** check the **owner** (`isAdmin(auth)` / `auth.getName()`), because only they know which record is being changed.

---

### 10.14 `security/MyUserDetailsService.java`

At login, Spring Security asks: *"give me the user with this username"*. Users are in **three tables**, and the **role chosen on the login page** decides which one to look in.

```java
@Service
public class MyUserDetailsService implements UserDetailsService {
```
`@Service` = Spring creates it. Because it implements `UserDetailsService`, Spring Security uses it at login. The 3 repositories are injected through the constructor.

```java
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String role = selectedRole();

        if (role == null || role.equals("ADMIN")) {
            Optional<Admin> admin = adminRepository.findByUsername(username);
            if (admin.isPresent()) {
                return User.withUsername(username).password(admin.get().getPassword()).roles("ADMIN").build();
            }
        }
        if (role == null || role.equals("TEACHER")) { ... teacher table, role TEACHER ... }
        if (role == null || role.equals("STUDENT")) { ... student table, role STUDENT ... }

        throw new UsernameNotFoundException("User not found: " + username);
    }
```
- Read the selected role.
- Look in the **matching table**. If the user is found, return a Spring Security `User` with the username, the **hashed** password (Spring compares it with BCrypt) and the role (stored as `ROLE_ADMIN` and so on).
- Not found → the login fails.
- `role == null` happens with Postman / curl (no form). Then all three tables are checked in order.

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
`RequestContextHolder` gives the **current HTTP request**. We read its form field `role`, which is the "Login as" dropdown.

---

### 10.15 `resources/application.properties`

```properties
# the app runs on http://localhost:8081
server.port=8081

# database: an H2 file database saved in the "data" folder (nothing to install)
# AUTO_SERVER=TRUE lets another program look inside the database while the app is running
# Docker Compose replaces these three values with MySQL ones
spring.datasource.url=jdbc:h2:file:./data/ums;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=

# create / update the tables automatically from the @Entity classes
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
```
| Line | Meaning |
|---|---|
| `# ...` | Comments. |
| `server.port=8081` | The address is `http://localhost:8081`. |
| `spring.datasource.url=jdbc:h2:file:./data/ums;AUTO_SERVER=TRUE` | Use an **H2 database in the file `data/ums`**. `AUTO_SERVER=TRUE` lets a second program (like the H2 Shell command in step 5b of the presentation) read the database while the app runs. |
| `spring.datasource.username=sa` / `password=` | The H2 login (`sa`, empty password). |
| `spring.jpa.hibernate.ddl-auto=update` | **Create or update the tables automatically** from the entities. |
| `spring.jpa.open-in-view=false` | Close the database connection when the request work is done (recommended; removes a warning). |

In Docker, the environment variables in `compose.yaml` replace the three `spring.datasource.*` values with MySQL ones.

---

### 10.16 `resources/static/login.html`

| Line(s) | Meaning |
|---|---|
| `<meta charset="UTF-8">` | Normal text encoding. |
| `<title>Login - ...</title>` | The browser tab title. |
| `<link rel="stylesheet" href="/style.css">` | Use our CSS. |
| `<div class="login-box">` | The white box in the middle. |
| `<p id="msg"></p>` | The place for error and logout messages. |
| `<form action="/login" method="post">` | Sends the form to `POST /login` (handled by Spring Security). |
| `<select name="role">` + 3 `<option>`s | The **"Login as"** dropdown → `role=ADMIN/TEACHER/STUDENT`. |
| `<input type="text" name="username" required>` | The username. The name **must** be `username`. `required` = can't be empty. |
| `<input type="password" name="password" required>` | The password (hidden letters). The name **must** be `password`. |
| `<button type="submit">Login</button>` | Sends the form. |
| `if (location.search.includes("error"))` | The URL has `?error` → red "Wrong username, password or role!". |
| `if (location.search.includes("logout"))` | The URL has `?logout` → green "You have been logged out.". |

---

### 10.17 `resources/static/index.html` (dashboard)

Only the structure. The logic is in `app.js`.

| Line(s) | Meaning |
|---|---|
| `<div class="header">` | The dark top bar. |
| `<b>University Management System</b>` | The title on the left. |
| `<span class="right"> Logged in as <b id="me"></b> \| <a href="/logout">Logout</a>` | On the right: the name and role (filled by app.js) and the logout link. |
| `<div class="container">` | The white content box. |
| `<div id="tabs" class="tabs"></div>` | app.js adds the tab buttons here. |
| `<h3 id="listTitle"></h3>` | "All Courses", "My Registrations"... |
| `<p id="hint" class="hint"></p>` | The grey help line. |
| `<table id="table"></table>` | app.js fills it with rows. |
| `<p id="msg"></p>` | The success or error message. |
| `<div id="formBox">` ... `</div>` | The form area. app.js shows or hides it. |
| `<hr>` | A line above the form. |
| `<h3 id="formTitle"></h3>` | "Add New Course", "Edit My Account"... |
| `<div id="form"></div>` | app.js builds the input boxes here. |
| `<button onclick="save()">Save</button>` | Calls `save()`. |
| `<button onclick="resetForm()">Cancel</button>` | Stops editing. |
| `<script src="/app.js"></script>` | Loads the JavaScript. It sits at the end, so the HTML above already exists. |

---

### 10.18 `resources/static/app.js` (dashboard logic)

It talks to the REST API with **`fetch`**. The browser sends the login session cookie automatically. **One set of functions works for all 5 tabs**, because each tab is described by settings in `TABS`.

#### The settings: `TABS`, `ALLOWED`, state variables

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
| Setting | Meaning | Example |
|---|---|---|
| `columns` | table columns `[path, label]`. A dotted path goes **inside** objects | `"teacher.name"` → `row.teacher.name` |
| `form` | form boxes `[path, label, rule]` | rule `"admin"` = only the admin sees it. `"new"` = only when adding |
| `password` | the form has a password box (people tabs) | `true` |
| `ownerRole` / `ownerPath` | a row is **mine** if my role is `ownerRole` and the username at `ownerPath` is mine | a course is a TEACHER's if `teacher.username` is theirs |
| `adders` | which roles may add rows | courses: admin and teacher |

```js
var ALLOWED = {
    ADMIN: ["admins", "teachers", "students", "courses", "registrations"],
    TEACHER: ["teachers", "courses", "registrations"],
    STUDENT: ["students", "courses", "registrations"]
};

var me = null;        // the logged-in user, for example {username: "teacher1", role: "TEACHER"}
var current = "";     // the open tab, for example "teachers"
var items = [];       // the rows loaded from the server
var editing = null;   // the row being edited (null = adding a new one)
```
The tabs per role, plus four variables that remember the page state. The page only *hides* what you can't use; the **server** really blocks it with 403.

#### When the page opens
```js
fetch("/api/me").then(function (res) {
    return res.json();
}).then(function (data) {
    me = data;
    document.getElementById("me").innerText = me.username + " (" + me.role + ")";

    var html = "";
    ALLOWED[me.role].forEach(function (tab) {
        html += "<button id='tab-" + tab + "' onclick=\"openTab('" + tab + "')\">" + tabName(tab) + "</button> ";
    });
    document.getElementById("tabs").innerHTML = html;

    openTab(ALLOWED[me.role][0]);
});
```
Ask who is logged in, show it in the top bar, make one button per allowed tab, and open the first tab.

#### Small "who am I" functions

| Function | Returns |
|---|---|
| `isAdmin()` | `true` if the role is ADMIN. |
| `isMine(item)` | `true` if the row is **yours** (your role = `ownerRole` **and** the username at `ownerPath` = yours). |
| `canAdd()` | `true` if your role is in the tab's `adders`. |
| `tabName(tab)` | The button text ("My Registrations" for students). |
| `hint()` | The grey help line for your role and tab. |

#### `openTab(tab)`
```js
    current = tab;
    document.querySelectorAll("#tabs button").forEach(function (button) { button.className = ""; });
    document.getElementById("tab-" + tab).className = "active";

    var title = "All " + capital(tab);
    if (tab == "registrations" && me.role == "STUDENT") title = "My Registrations";
    if (tab == "registrations" && me.role == "TEACHER") title = "Registrations in My Courses";
    document.getElementById("listTitle").innerText = title;
    document.getElementById("hint").innerText = hint();

    showMessage("", "");
    resetForm();
    loadList();
```
Remember the tab, highlight its button, set the title and hint, clear the message, reset the form, and load the table.

#### `loadList()`: READ
```js
    fetch("/api/" + current).then(function (res) {
        if (!res.ok) {
            showMessage("Could not load the list. Error " + res.status, "error");
            return [];
        }
        return res.json();
    }).then(function (list) {
        items = list;
        var columns = TABS[current].columns;
        var html = "<tr><th>ID</th>";
        columns.forEach(function (c) { html += "<th>" + c[1] + "</th>"; });
        html += "<th>Actions</th></tr>";
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
    });
```
`GET /api/{tab}`. On an error, show it. Otherwise build the header row, then one row per item: your rows get the class `mine` (yellow), each column's value (`getValue` reads paths like `teacher.name`), and the allowed buttons.

#### `buttons(item, i)`
```js
    var mine = isMine(item);
    var canEdit = isAdmin() || mine;
    var canDelete = isAdmin() ? !mine : mine;   // admin: everything except their own account, others: only their own rows

    var html = "";
    if (current == "courses" && me.role == "STUDENT") html += "<button onclick='registerCourse(" + i + ")'>Register</button> ";
    if (canEdit) html += "<button onclick='editRow(" + i + ")'>Edit</button> ";
    if (canDelete) html += "<button onclick='deleteRow(" + i + ")'>" + (current == "registrations" ? "Drop" : "Delete") + "</button> ";
    if (mine && TABS[current].password) html += "<i>(you)</i>";
    return html == "" ? "-" : html;
```
- **Edit**: the admin on any row, others only on their own.
- **Delete**: the admin on everything except their own account, others only on their own rows.
- Students get **Register** on courses. Registrations say **Drop** instead of Delete.
- "(you)" marks your account row, and `-` means no buttons.

#### `showField`, `buildForm`, `resetForm`, `editRow`
```js
function showField(field) {
    var rule = field[2] || "";
    if (rule.indexOf("admin") >= 0 && !isAdmin()) return false;
    if (rule.indexOf("new") >= 0 && editing != null) return false;
    return true;
}
```
Hides `"admin"` boxes from non-admins, and `"new"` boxes while editing. (That is why a student editing a registration only sees **Section**.)

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
One label and box per allowed field (the box id is `f-` + the path), plus a password box for people. `autocomplete` stops the browser from filling in your own saved login.

```js
function resetForm() {
    editing = null;
    buildForm();
    document.getElementById("formTitle").innerText = "Add New " + singular(current);
    document.getElementById("formBox").style.display = canAdd() ? "block" : "none";
}
```
An empty "Add New" form, **shown only if you may add** in this tab.

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
Remember the row, build the boxes, set the title, show the form, and fill in the values.

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

    var isEdit = editing != null;
    var myAccount = isEdit && isMine(editing) && TABS[current].password;
    var url = isEdit ? "/api/" + current + "/" + editing.id : "/api/" + current;

    fetch(url, {
        method: isEdit ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    }).then(function (res) {
        if (!res.ok) {
            showError(res);
            return;
        }
        // a new username or password only works after logging in again
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
- Read the boxes into JSON. `setValue` turns `course.code` into `{course: {code: ...}}`.
- Editing → **PUT** `/api/{tab}/{id}`. Adding → **POST** `/api/{tab}`.
- On an error → show it in red.
- If you changed your own username or password → log out.
- Otherwise → a green message, reset the form and reload the table.

#### `deleteRow(i)`: DELETE
```js
    var question = "Delete this " + singular(current).toLowerCase() + " (ID " + item.id + ")?";
    if (current == "courses") question = "Delete course " + item.code + "? Its registrations are deleted too.";
    if (current == "registrations") question = "Drop " + item.course.code + " for " + item.student.name + "?";
    if (myAccount) question = "Delete YOUR OWN account? You will be logged out.";
    if (!confirm(question)) {
        return;
    }
    fetch("/api/" + current + "/" + item.id, { method: "DELETE" }).then(function (res) {
        if (!res.ok) { showError(res); return; }
        if (myAccount) { location.href = "/logout"; return; }
        showMessage("Deleted successfully (status " + res.status + ")", "success");
        resetForm();
        loadList();
    });
```
Ask a clear question, then send DELETE. If you deleted your own account, you're logged out. Otherwise show the message and reload.

#### `registerCourse(i)`: a student registers
```js
    var course = items[i];
    var section = prompt("Register for " + course.code + " - " + course.title + "\nSection (for example A or B):", "A");
    if (section == null) {
        return;
    }
    fetch("/api/registrations", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ course: { code: course.code }, section: section })
    }).then(function (res) {
        if (!res.ok) { showError(res); return; }
        showMessage("Registered for " + course.code + " (status " + res.status + "). See My Registrations.", "success");
    });
```
Ask for the section, then `POST /api/registrations`. The server knows the student from the login.

#### Helpers

| Function | What it does |
|---|---|
| `getValue(obj, "teacher.name")` | Follows the path (`obj.teacher.name`). Returns empty text if something is missing. |
| `setValue(obj, "teacher.username", "t1")` | Creates `obj.teacher = {username: "t1"}`. |
| `showMessage(text, type)` | Writes into `#msg`. `"success"` = green, `"error"` = red. |
| `showError(res)` | Reads `{"error":"..."}` from the server and shows "Error 403: ...". |
| `capital("teachers")` | Returns "Teachers". |
| `singular("teachers")` | Returns "Teacher". |
| `clean(value)` | Turns `<` into `&lt;`, so typed HTML can't run on the page. |

---

### 10.19 `resources/static/style.css`

| Block | What it styles |
|---|---|
| `body` | Arial font, light grey background. |
| `.login-box` (+ `label`, `input`, `select`, `button`) | The white login box in the middle, with full-width fields. |
| `.header`, `.header .right`, `.header a` | The dark blue top bar, with "Logged in as ... \| Logout" on the right. |
| `.container` | The white content box, centered, max 950px wide. |
| `.tabs button` / `.tabs button.active` | Tab buttons. The open tab is dark blue. |
| `table`, `th, td`, `th` | Table lines, spacing, a grey header row. |
| `tr.mine td` | **Your own rows** in light yellow. |
| `.hint` | Grey help text. |
| `.form-row`, `label`, `input` | Form rows. The fixed-width labels line up the boxes. |
| `.error` / `.success` | Red / green messages. |

---

### 10.20 `Dockerfile` and `compose.yaml`
Explained line by line in [3.10](#310-the-dockerfile-line-by-line) and [3.11](#311-the-composeyaml-line-by-line).

### 10.21 `.dockerignore` and `.gitignore`
- **`.dockerignore`**: files Docker should **not** copy into the image: `target`, `data` (the local H2 database), `.idea`, `.vscode`, `*.iml`.
- **`.gitignore`**: files Git should **not** save: `target/`, `data/`, `.idea/`, `*.iml`.

### 10.22 `UMS.postman_collection.json`
A Postman file with **40 ready requests** in 3 folders (REST API, Security, Courses). Each request has:
- the method, the URL (`{{baseUrl}}/api/...`) and a JSON body,
- **Basic Auth** with the right user,
- a **test** that checks the expected status code (e.g. `pm.response.to.have.status(201)`),
- for some requests, a small script that saves an id for later requests (e.g. `pm.collectionVariables.set("demoTeacherId", ...)`).

Import it into Postman as described in [7.2](#72-get-postman-ready-import-the-collection).

---

## 11. Viva questions and answers

### About Spring Boot
| Question | Answer |
|---|---|
| What is Spring Boot? | Spring with automatic configuration and a built-in Tomcat server, so a web app starts with just a `main` method. |
| How do you create a Spring Boot project? | On **start.spring.io**: Maven, Java 17, add the dependencies, Generate, open in IntelliJ. |
| Which file runs first? | `UmsApplication.java` (`@SpringBootApplication` + `main()`). |
| What does `@SpringBootApplication` do? | It turns on auto-configuration and scans the package for components. |
| What is `pom.xml`? | The Maven file with the project info and dependencies. |
| What is `application.properties`? | The settings file (port, database). It is loaded automatically. |
| What is dependency injection? | Spring creates the objects and passes them into constructors (e.g. a repository into a controller). |

### About the REST API
| Question | Answer |
|---|---|
| What is REST? | URLs are resources (`/api/teachers`), HTTP methods are actions (GET read, POST create, PUT update, DELETE delete), and data is JSON. |
| What does `@RestController` do? | It makes a class whose methods return data that is turned into JSON automatically. |
| `@PathVariable` vs `@RequestBody`? | `@PathVariable` takes a value from the URL (`/teachers/5`). `@RequestBody` takes the JSON body. |
| How do you return a status code? | With `ResponseEntity`: `ok()` 200, `status(CREATED)` 201, `noContent()` 204, `badRequest()` 400, `notFound()` 404, `status(CONFLICT)` 409, `status(FORBIDDEN)` 403. |
| Why 201 and not 200 for POST? | 201 Created means a new resource was made. That is the REST convention. |
| Why 204 for DELETE? | The delete worked and there is nothing to send back. |
| Who turns Java into JSON? | **Jackson**, which is included in `spring-boot-starter-web`. |

### About the database
| Question | Answer |
|---|---|
| What is an Entity? | A Java class mapped to a table (`@Entity`). |
| What is a Repository? | An interface. Spring Data JPA writes the database code (`findAll`, `save`, `deleteById`...). |
| How does `findByUsername` work without code? | Spring reads the method name and builds the query. |
| What does `ddl-auto=update` do? | Hibernate creates or updates the tables from the entities. |
| What is `@ManyToOne`? | A link from many rows to one row (many courses → one teacher). It becomes a foreign key. |
| What does `@OnDelete(CASCADE)` do? | Deleting the "one" side deletes the linked rows (teacher → courses → registrations). |
| Why H2 in IntelliJ and MySQL in Docker? | H2 needs no installation. Docker gives a real MySQL. Compose switches the database with environment variables. |

### About Spring Security
| Question | Answer |
|---|---|
| How does login work? | The form posts role, username and password to `/login`. Spring calls `MyUserDetailsService`, which loads the user from the chosen role's table. Spring compares the password with BCrypt and creates a session. |
| How does logout work? | `/logout` ends the session (configured in `SecurityConfig.logout(...)`). |
| How are passwords stored? | As **BCrypt hashes** (`$2a$10$...`), never as plain text. They are never returned in JSON (`WRITE_ONLY`). |
| What is BCrypt? | A one-way hash with a random salt. The same password gives different hashes. At login, the typed password is checked against the hash. |
| What is RBAC? | Role-Based Access Control: what you may do depends on your role. |
| Where is RBAC in your code? | `SecurityConfig` → `hasRole` / `hasAnyRole` per URL. Each user gets their role in `MyUserDetailsService` → `.roles("TEACHER")`. |
| 401 vs 403? | **401** = not logged in or wrong password. **403** = logged in, but not allowed. |
| Role check vs ownership check? | **Role** is checked in `SecurityConfig` for each URL. **Ownership** ("is this record yours?") is checked in the controller with `auth.getName()`. |
| Can a teacher change another teacher? | No → 403. Only their own account. |
| Why can't a teacher create teachers? | In a real university, the admin creates accounts. POST on teachers or students is ADMIN only. |
| Why is CSRF disabled? | To keep REST testing with Postman simple. In a production app with forms, it should be on. |
| What is HTTP Basic? | Username and password sent in a header with each request. Postman and curl use it. |

### About Docker
| Question | Answer |
|---|---|
| What is Docker? | A tool that runs programs in containers with everything they need, so they work the same everywhere. |
| Image vs container? | An image is the packed program (like a class). A container is a running image (like an object). |
| What does your Dockerfile do? | It is a 2-stage build: Maven builds the jar, then a small Java 17 image runs it. |
| Why two stages? | The final image is smaller: no Maven and no source code, only Java + the jar. |
| What does compose.yaml do? | It runs **two services together**: MySQL (`db`) and the app (`app`), with ports, a volume, a health check and environment variables. |
| How does the app find MySQL? | By the service name: `jdbc:mysql://db:3306/...`. Compose puts both containers on one network. |
| Why the health check and `depends_on`? | The app starts only when MySQL is ready to accept connections. |
| What is the volume for? | The MySQL data survives `docker compose down`. |
| How do you start and stop it? | `docker compose up --build` / `docker compose down`. |

---

## 12. Common problems

| Problem | Solution |
|---|---|
| Red code / "Cannot resolve symbol" | Maven tab → 🔄 **Reload All Maven Projects**. Check that the SDK is **17**. |
| `Port 8081 was already in use` | The app is already running (IntelliJ or Docker). Stop the other one. |
| "Wrong username, password or role!" | Check the **Login as** role. Passwords are case-sensitive (section 4). |
| Teacher/student sees `-` instead of buttons | Correct. That row belongs to someone else. |
| "Error 403: you can only edit your own ..." | You tried to change someone else's record. Only the admin can do that. |
| "Error 409: already registered in this course" | Change the section in **My Registrations** instead. |
| "Error 400: teacher not found" (admin adding a course) | Type the teacher's **username** (e.g. `teacher1`), not the name. |
| Logged out after saving my own account | You changed your own username or password. Log in again with the new details. |
| Forgot a password | Log in as **admin** → the right tab → **Edit** the user → type a new password → **Save**. |
| Want fresh data | IntelliJ: stop the app, delete `data/`, run. Docker: `docker compose down -v`, then `docker compose up --build`. |
| Postman requests fail with 409 or 404 | Run the folders **in order from the top**, and start with fresh data. |
| Docker problems | See [3.3](#33-install-docker-desktop-on-windows-once) and [3.12](#312-docker-problems-while-running). |
