import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ClassesParserTest {

    private List<String> CodeToTest = new ArrayList<>();
    private ClassesParser classesParserTest;

    private int expectedLinesOrComments = 8;
    private float expectedNCLOC = 8.0f;
    private float expectedCLOC = 5.0f;
    private float expectedLOC = 13.0f;
    private float expectedDC = 0.3846154f;
    private float deltaPrecision = 0.00000001f;

    public ClassesParserTest() {
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

        classesParserTest = new ClassesParser(Arrays.asList("chemin",
                "class", "methode", "methode_CLOC", "methode_LOC", "methode_DC"));
    }

    @Test
    public void getClassName() {
        String value = "public abstract class TESTCLASS implements Annotation";
        String expected = "TESTCLASS";
        assertEquals(expected, classesParserTest.getClassName(value, Arrays.asList(value.split(" "))));
    }

    @Test
    public void calculateLinesOrComments() {
        assertEquals(expectedLinesOrComments, classesParserTest.calculateLinesOrComments(CodeToTest, false));
    }

    @Test
    public void classe_NCLOC() {
        assertEquals(expectedNCLOC, classesParserTest.classe_NCLOC(CodeToTest), deltaPrecision);
    }

    @Test
    public void classe_CLOC() {
        assertEquals(expectedCLOC, classesParserTest.classe_CLOC(CodeToTest), deltaPrecision);
    }

    @Test
    public void classe_LOC() {
        assertEquals(expectedLOC, classesParserTest.classe_LOC(expectedNCLOC, expectedCLOC), deltaPrecision);
    }

    @Test
    public void classe_DC() {
        assertEquals(expectedDC, classesParserTest.classe_DC(expectedCLOC, expectedLOC), deltaPrecision);
    }
}