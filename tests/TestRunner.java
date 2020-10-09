import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {
    public static void main(String[] args) {
        Result resultClassesParserTest = JUnitCore.runClasses(ClassesParserTest.class);
        Result resultMethodesParserTest = JUnitCore.runClasses(MethodesParserTest.class);

        addHeadline("ClassesParserTest");
        for (Failure failure : resultClassesParserTest.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.print("Test(s) passed : ");
        System.out.println(resultClassesParserTest.wasSuccessful());

        addHeadline("MethodesParserTest");
        for (Failure failure : resultMethodesParserTest.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.print("Test(s) passed : ");
        System.out.println(resultMethodesParserTest.wasSuccessful());
    }

    private static void addHeadline(String className) {
        System.out.println("|------------------------------------");
        System.out.println("| Results for " + className);
        System.out.println("|------------------------------------");
    }
}