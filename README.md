# CSD201 Dungeon Project

Java 17 project for the CSD201 assignment, with:
- console/demo run mode
- manual test mode
- unit test mode
- simple web UI mode

## Project Location

If your folder structure matches the one in this workspace, run commands from:

`C:\Users\admin\Downloads\CSD201\CSD201\CSD201`

## Prerequisites

- Java Development Kit (JDK) 17
  - `java -version`
  - `javac -version`
- Windows PowerShell or Command Prompt

Optional (for Maven commands):
- Maven (`mvn -version`)

## Quick Start (Windows)

Open PowerShell:

```powershell
cd "C:\Users\admin\Downloads\CSD201\CSD201\CSD201"
```

Run one of these:

- Unit tests (default):

```powershell
.\run.cmd
```

- Unit tests (explicit):

```powershell
.\run.cmd unit
```

- Manual test:

```powershell
.\run.cmd manual
```

- Main/demo app:

```powershell
.\run.cmd main
```

- Web UI (default port 8081):

```powershell
.\run-web.cmd
```

- Web UI (custom port, example 9000):

```powershell
.\run-web.cmd 9000
```

## Web UI Notes

When the web UI starts, the terminal keeps running and waits for requests.  
This is expected behavior.

- Open browser at the shown URL (for example, `http://localhost:8080` or `http://localhost:8081`)
- Stop server with `Ctrl + C`

## If PowerShell Blocks Script Execution

Use Command Prompt through PowerShell:

```powershell
cmd /c run.cmd manual
cmd /c run.cmd main
cmd /c run-web.cmd
```

## Alternative Maven Run

If helper scripts fail and Maven is installed:

```powershell
mvn test
mvn "-Dexec.args=8081" exec:java
```

## Troubleshooting

- `java` or `javac` not found:
  - Install JDK 17 and add it to `PATH`
- Port already in use:
  - Start web UI on another port, e.g. `.\run-web.cmd 9000`
- Script starts web server and appears "stuck":
  - It is running normally; open the URL or press `Ctrl + C` to stop