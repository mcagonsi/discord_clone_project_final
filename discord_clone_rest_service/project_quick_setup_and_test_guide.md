## Quick setup & test guide (developer)

Follow these steps to get a local developer instance running and verify the database connection.

### 1) Install MariaDB and a client GUI

- Install MariaDB Server (latest stable) on your machine.
- Install a GUI client such as HeidiSQL and verify you can connect to the local MariaDB instance.
- Load the database schema provided in the repository using the SQL file `discord_clone_db_creation.sql`.

Example using the mysql/mariadb CLI from a Windows command prompt (adjust path to the SQL file):

Notes:
- If you use HeidiSQL, you can connect and run the SQL file from its Query tab instead of using the CLI.
- Ensure the database (schema) was created and tables populated before continuing.

### 2) Install VS Code server extension

- Ensure you have the Community Server extension installed in VS Code (the extension you use to manage Tomcat/TomEE servers from the editor).

### 3) Download and set up Apache TomEE (the linked TomEE distribution)

- Download TomEE (the plus distribution) from the link below and unzip it to a folder of your choice. Example target folder used in the steps below: `C:\tools\apache-tomee-10.1.4-plus`.

Link: https://www.apache.org/dyn/closer.cgi/tomee/tomee-10.1.4/apache-tomee-10.1.4-plus.zip



### 4) Add the JDBC Resource (context.xml)

- The application expects a JNDI DataSource resource to be defined in the server's context. Add a <Resource> entry inside TomEE's `<Context>` element.
- Open the TomEE `conf/context.xml` (or the server manager's context editor provided by your VS Code server extension) and add the DataSource resource.
- Use the template below and substitute your DB username/password, database name, and other values.

Resource template (example) — paste inside `<Context> ... </Context>`:

```xml
<Resource name="jdbc/DiscordDB"
		  auth="Container"
		  type="javax.sql.DataSource"
		  username="YOUR_DB_USER"
		  password="YOUR_DB_PASSWORD"
		  driverClassName="org.mariadb.jdbc.Driver"
		  url="jdbc:mariadb://localhost:3306/discord_clone_db"
		  maxTotal="20"
		  maxIdle="10"
		  maxWaitMillis="10000" />
```

Notes and assumptions:
- The template above uses the JNDI name `jdbc/DiscordDB`. If your webapp expects a different JNDI name, substitute that name.
- The driver class for MariaDB is `org.mariadb.jdbc.Driver`. Ensure the MariaDB JDBC driver JAR is available to TomEE — put the driver JAR into `lib/` under the TomEE installation (for example `C:\tools\apache-tomee-10.1.4-plus\lib\`) so the server can load it.
- The repository also includes a `WEB-INF/web.xml` template. If you prefer to copy the resource XML from there, substitute the values with the credentials/host/port you use locally.

### 5) Build the WAR

- From the repository root, run the Gradle war task for the `app` module. On Windows (cmd.exe):

```cmd
cd "c:\Users\Michael\Documents\COURSE WORK\discord_clone_project_final"
.\gradlew.bat :app:clean :app:war
```

- After a successful build the WAR will be created under `app\build\libs\` (look for `*.war`).

### 6) Deploy the WAR to TomEE

Option A — copy WAR to TomEE webapps (manual):

```cmd
copy "app\build\libs\your-app.war" "C:\tools\apache-tomee-10.1.4-plus\webapps\"
cd C:\tools\apache-tomee-10.1.4-plus\bin
startup.bat
```

Option B — use the VS Code Community Server extension UI to deploy the built WAR if it supports it (the extension should list your TomEE instance and provide deploy actions).

### 7) Verify the app and DB connection

- Open a browser and go to:

	http://localhost:8080/app/discord/permissions

- Expected: the page should load and the data should be fetched from the MariaDB database.
- If the page shows errors, check TomEE logs (`C:\tools\apache-tomee-10.1.4-plus\logs\catalina.out` or `logs` folder) for JDBC lookup errors.

### 8) Troubleshooting checklist

- Ensure MariaDB is running and accessible from the machine where TomEE runs.
- Ensure the JDBC URL, username, and password in the `<Resource>` are correct.
- Ensure the MariaDB JDBC driver JAR is present in TomEE's `lib` folder.
- Check server startup logs for exceptions (ClassNotFoundException for driver, NamingException , SQLExceptions, etc.).
- If you changed the JNDI name, make sure the webapp's lookup matches the name in the server resource.

### Assumptions made

- The webapp expects a DataSource registered under a name (example used: `jdbc/DiscordDB`). If your code uses a different name, update the context.xml accordingly.
- Gradle's `:app:war` task produces a deployable WAR in `app/build/libs/`.
- TomEE is used on localhost at default port 8080.



