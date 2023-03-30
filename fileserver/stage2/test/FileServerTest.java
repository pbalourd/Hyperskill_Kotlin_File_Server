import org.hyperskill.hstest.dynamic.input.DynamicTestingMethod;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.TestedProgram;


public class FileServerTest extends StageTest<String> {

    @DynamicTestingMethod
    CheckResult test() throws InterruptedException {

        TestedProgram client = new TestedProgram("client");
        TestedProgram server = new TestedProgram("server");
        server.setReturnOutputAfterExecution(true);

        server.startInBackground();

        Thread.sleep(1000);
        String serverOutput = server.getOutput().trim();

        String serverStartedMessage = "Server started!";
        if (!serverOutput.equals(serverStartedMessage)) {
            throw new WrongAnswer("Server output should be \"" + serverStartedMessage + "\" until the client connects!");
        }

        String clientOutput = client.start().trim();
        serverOutput = server.getOutput().trim();

        if (clientOutput.isEmpty()) {
            return CheckResult.wrong("Client output shouldn't be empty!");
        }

        if (serverOutput.equals(serverStartedMessage)) {
            return CheckResult.wrong("After the client connects to the server you should output the received data!");
        }

        if (!serverOutput.contains("Received: Give me everything you have!")) {
            return CheckResult.wrong("Server output should contain \"Received: Give me everything you have!\"");
        }

        if (!serverOutput.contains("Sent: All files were sent!")) {
            return CheckResult.wrong("Server output should contain \"Sent: All files were sent!\"");
        }

        if (serverOutput.indexOf("Sent: All files were sent!") < serverOutput.indexOf("Received: Give me everything you have!")) {
            return CheckResult.wrong("The server should print \"Sent: All files were sent!\" only after " +
                    "\"Received: Give me everything you have!\" was printed!");
        }

        if (!clientOutput.contains("Sent: Give me everything you have!")) {
            return CheckResult.wrong("Client output should contain \"Sent: Give me everything you have!\"");
        }

        if (!clientOutput.contains("Received: All files were sent!")) {
            return CheckResult.wrong("Client output should contain \"Received: All files were sent!\"");
        }

        if (clientOutput.indexOf("Received: All files were sent!") < clientOutput.indexOf("Sent: Give me everything you have!")) {
            return CheckResult.wrong("The client should print \"Received: All files were sent!\" only after " +
                    "\"Sent: Give me everything you have!\" was printed!");
        }

        return CheckResult.correct();
    }
}
