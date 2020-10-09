import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class MethodesParserTest {

    private List<String> CodeToTest = new ArrayList<>();
    private MethodesParser methodesParserTest;

    private int expectedPredicat = 1;
    private float expectedNCLOC = 8.0f;
    private float expectedCLOC = 5.0f;
    private float expectedLOC = 13.0f;
    private float expectedDC = 0.3846154f;
    private float deltaPrecision = 0.00000001f;

    public MethodesParserTest() {
        CodeToTest.add("/**");
        CodeToTest.add("* Notifies all registered listeners that the annotation has changed. ");
        CodeToTest.add("* ");
        CodeToTest.add("* @param event contains information about the event that triggered the ");
        CodeToTest.add("* notification. ");
        CodeToTest.add("* ");
        CodeToTest.add("* @see #addChangeListener(AnnotationChangeListener) ");
        CodeToTest.add("* @see #removeChangeListener(AnnotationChangeListener) ");
        CodeToTest.add("*/ ");
        CodeToTest.add("protected void notifyListeners(AnnotationChangeEvent event) { ");
        CodeToTest.add("Object[] listeners = this.listenerList.getListenerList(); ");
        CodeToTest.add("for (int i = listeners.length - 2; i &gt;= 0; i -= 2) { ");
        CodeToTest.add("if (listeners[i] == AnnotationChangeListener.class) { ");
        CodeToTest.add("((AnnotationChangeListener) listeners[i + 1]).annotationChanged( ");
        CodeToTest.add("event); ");
        CodeToTest.add("} ");
        CodeToTest.add("} ");

        //Empty lines  in the code are already removed at this point except for fct "calculateLinesOrComments".

        methodesParserTest = new MethodesParser(Arrays.asList("chemin",
                "class", "classe_LOC", "classe_CLOC", "classe_DC"));
    }

    @Test
    public void extractMethodeName() {
        List<String> Code = new ArrayList<>();

        //Testing with no arguments
        Code.add("protected void notifyListeners() { ");
        Code.add("Object[] listeners = this.listenerList.getListenerList(); ");
        Code.add("} ");
        assertEquals("notifyListeners",
                methodesParserTest.buildMethodeName(Code, "notifyListeners"));

        //Testing with 1 arguments
        Code.set(0, "protected void notifyListeners(args arg1)");
        assertEquals("notifyListeners_args",
                methodesParserTest.buildMethodeName(Code, "notifyListeners"));

        //Testing with 2 arguments
        Code.set(0, "protected void notifyListeners(args arg1, args 2)");
        assertEquals("notifyListeners_args_args",
                methodesParserTest.buildMethodeName(Code, "notifyListeners"));

        //Testing with 3 arguments
        Code.set(0, "protected void notifyListeners(args arg1, args 2, args 3)");
        assertEquals("notifyListeners_args_args_args",
                methodesParserTest.buildMethodeName(Code, "notifyListeners"));
    }

    @Test
    public void calculerPredicats() {
        assertEquals(expectedPredicat,methodesParserTest.calculerPredicats(CodeToTest));
    }

    @Test
    public void methode_LOC() {
        assertEquals(expectedLOC, methodesParserTest.methode_LOC(expectedNCLOC, expectedCLOC), deltaPrecision);
    }

    @Test
    public void methode_CLOC() {
        assertEquals(expectedCLOC, methodesParserTest.methode_CLOC(CodeToTest), deltaPrecision);
    }

    @Test
    public void methode_NCLOC() {
        assertEquals(expectedNCLOC, methodesParserTest.methode_NCLOC(CodeToTest), deltaPrecision);
    }

    @Test
    public void methode_DC() {
        assertEquals(expectedDC, methodesParserTest.methode_DC(expectedCLOC, expectedLOC), deltaPrecision);
    }
}