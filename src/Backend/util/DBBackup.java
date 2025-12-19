package Backend.util;

import java.io.*;

public class DBBackup {

    private static final String MYSQL_DUMP_EXE = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";
    private static final String MYSQL_EXE = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe";

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String USER = DB.GetUsername();
    private static final String PASSWORD = DB.GetPassword();

    private static final String[] DATABASES = { "auth_db","erp_db" };

    public static void BackUpAsSQL(File Destination) throws Exception {
        ValidDestination(Destination);

        String[] Command = BuildMySQLDump();

        ProcessBuilder ProcessBuilderInstance = new ProcessBuilder(Command);
        ProcessBuilderInstance.redirectErrorStream(false);
        Process ProcessInstance = ProcessBuilderInstance.start();

        try (InputStream DumpStream = ProcessInstance.getInputStream();
             FileOutputStream FileOutput = new FileOutputStream(Destination)) {

            byte[] Buffer = new byte[8192];
            int Length;
            while ((Length = DumpStream.read(Buffer)) != -1) {
                FileOutput.write(Buffer, 0, Length);
            }
        }

        String ErrorOutput = ReadStream(ProcessInstance.getErrorStream());
        int ExitCode = ProcessInstance.waitFor();

        if (ExitCode != 0) {
            throw new IOException("SQL backup failed. mysqldump exited with code " + ExitCode + (ErrorOutput.isEmpty() ? "" : "\n\n" + ErrorOutput));
        }
    }

    public static void RestoreFromSQL(File SqlFile) throws Exception {
        if (SqlFile == null || !SqlFile.exists()) {
            throw new IllegalArgumentException("SQL file not found: " + SqlFile);
        }

        String[] Command = {
                MYSQL_EXE,
                "-h", HOST,
                "-P", PORT,
                "-u", USER,
                "-p" + PASSWORD
        };

        ProcessBuilder ProcessBuilderInstance = new ProcessBuilder(Command);
        ProcessBuilderInstance.redirectErrorStream(false);
        Process ProcessInstance = ProcessBuilderInstance.start();

        try (OutputStream ProcessInput = ProcessInstance.getOutputStream();
             FileInputStream FileInput = new FileInputStream(SqlFile)) {

            byte[] Buffer = new byte[8192];
            int Length;
            while ((Length = FileInput.read(Buffer)) != -1) {
                ProcessInput.write(Buffer, 0, Length);
            }
            ProcessInput.flush();
        }

        String ErrorOutput = ReadStream(ProcessInstance.getErrorStream());
        int ExitCode = ProcessInstance.waitFor();

        if (ExitCode != 0) {
            throw new IOException("SQL restore failed. mysql exited with code "
                    + ExitCode + (ErrorOutput.isEmpty() ? "" : "\n\n" + ErrorOutput));
        }
    }



    private static String[] BuildMySQLDump() {
        String[] Command = new String[10 + DATABASES.length];
        int Index = 0;

        Command[Index++] = MYSQL_DUMP_EXE;
        Command[Index++] = "--no-tablespaces";
        Command[Index++] = "-h"; Command[Index++] = HOST;
        Command[Index++] = "-P"; Command[Index++] = PORT;
        Command[Index++] = "-u"; Command[Index++] = USER;
        Command[Index++] = "-p" + PASSWORD;
        Command[Index++] = "--databases";

        for (String DatabaseName : DATABASES) {
            Command[Index++] = DatabaseName;
        }
        return Command;
    }

    private static void ValidDestination(File Destination) {
        if (Destination == null) throw new IllegalArgumentException("Destination file is null.");
        File ParentDirectory = Destination.getParentFile();
        if (ParentDirectory != null && !ParentDirectory.exists() && !ParentDirectory.mkdirs()) {
            throw new RuntimeException("Cannot create backup directory: " + ParentDirectory.getAbsolutePath());
        }
    }

    private static String ReadStream(InputStream InputStreamObject) throws IOException {
        StringBuilder StringBuilderInstance = new StringBuilder();
        try (InputStreamReader InputStreamReaderInstance = new InputStreamReader(InputStreamObject);
             BufferedReader BufferedReaderInstance = new BufferedReader(InputStreamReaderInstance)) {

            String Line;
            while ((Line = BufferedReaderInstance.readLine()) != null) {
                StringBuilderInstance.append(Line).append('\n');
            }
        }
        return StringBuilderInstance.toString().trim();
    }
}
