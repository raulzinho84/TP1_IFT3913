import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassesParser extends Parser {

    /**
     *
     * @param firstRow
     */
    public ClassesParser(List<String> firstRow) { super(firstRow); }

    /**
     *
     * @return
     */
    @Override
    List<String> treatCode(MethodesParser methodesParser) {
        String className = null, tempClassName;
        List<String> tempClassesNames = new ArrayList<>();
        int startIndex = 0, endIndex = 0, currIndex = 0;
        for (String element : this.getCodeToParse()) {
            tempClassName = getClassName(element, getLineElements(element));
            if (element.contains("Copy lines")) {
                endIndex = currIndex;
            }
            if (tempClassName != null) {
                className = tempClassName;
                startIndex = getStartIndex(currIndex);
            }
            if (startIndex != 0 && endIndex != 0) {
                if(!tempClassesNames.contains(className)) {
                    assignFilesData(this.getCurrChemin(),
                            getClassIndexesAndName(startIndex, endIndex, className));
                    tempClassesNames.add(className);
                    parseMethodes(methodesParser, className);
                }
            }
            currIndex++;
        }
        return super.treatCode(methodesParser);
    }

    /**
     *
     * @param element
     * @return
     */
    String[] getLineElements(String element) {
        element = element.replaceAll("\\s+", "+");
        String[] lineElements = element.split("\\+");
        return Arrays.copyOfRange(lineElements, 1, lineElements.length);
    }

    /**
     *
     * @param element
     * @param lineElements
     * @return
     */
    String getClassName(String element, String[] lineElements) {
        String className = null;
        if(detectForClass(element) && detectForComments(element) &&
                (lineElements[0].contentEquals("class") ||
                        detectForPrivacy(lineElements))) {
            if (!lineElements[0].contentEquals("class")) {
                if (!lineElements[1].contentEquals("abstract") &&
                        !lineElements[1].contentEquals("static")) {
                    className = lineElements[2];
                } else { className = lineElements[3]; }
            } else { className = lineElements[1]; }
        }
        return className;
    }

    /**
     *
     * @param lineCodeIndex
     * @return
     */
    int getStartIndex(int lineCodeIndex) {
        int prevLineIndex = lineCodeIndex - 1;
        while(!this.getCodeToParse().get(prevLineIndex).contains("/**") && prevLineIndex > 0) {
            prevLineIndex--;
        }
        return prevLineIndex;
    }

    /**
     *
     * @param startIndex
     * @param endIndex
     * @param className
     * @return
     */
    List<String> getClassIndexesAndName(int startIndex,
    int endIndex,
    String className) {
        return new ArrayList<>(Arrays.asList(String.valueOf(startIndex),
                String.valueOf(endIndex), className));
    }

    /**
     *
     * @param chemin
     * @param treatedFile
     */
    void assignFilesData(String chemin,
                                 List<String> treatedFile) {
        float totalClasseCloc, totalClasseNCLoc, totalClasseLoc;
        List<String> resultedList;
        int startIndex, endIndex;
        startIndex = Integer.parseInt(treatedFile.get(0));
        endIndex = Integer.parseInt(treatedFile.get(1));
        totalClasseCloc = classe_CLOC(this.getCodeToParse().subList(startIndex, endIndex));
        totalClasseNCLoc = classe_NCLOC(this.getCodeToParse().subList(startIndex, endIndex));
        totalClasseLoc = classe_LOC(totalClasseNCLoc, totalClasseCloc);
        resultedList = Arrays.asList(chemin, treatedFile.get(2),
                String.valueOf(totalClasseLoc), String.valueOf(totalClasseCloc),
                String.valueOf(classe_DC(totalClasseCloc, totalClasseLoc)));
        rearrangeData(resultedList);
    }

    /**
     *
     * @param methodesParser
     * @return
     */
    void parseMethodes(MethodesParser methodesParser,
                               String currClasse) {
        methodesParser.setCodeToParse(this.getCodeToParse());
        methodesParser.setCurrChemin(this.getCurrChemin());
        methodesParser.getFileMethodes(this.getCurrChemin(), currClasse);
    }

    /**
     *
     * @param classTextList
     * @return
     */

    float classe_NCLOC(List<String> classTextList) {
        return calculer_NCLOC(classTextList);
    }

    /**
     *
     * @param nClocResult
     * @param clocResult
     * @return
     */
    float classe_LOC(float nClocResult, float clocResult) {
        return calculer_LOC(nClocResult, clocResult);
    }

    /**
     *
     * @param classTextList
     * @return
     */
    float classe_CLOC(List<String> classTextList) {
        return calculer_CLOC(classTextList);
    }

    /**
     *
     * @param commentsDensity
     * @param linesOfCode
     * @return
     */
    float classe_DC(float commentsDensity, float linesOfCode) {
        return calculer_DC(commentsDensity, linesOfCode);
    }
}
