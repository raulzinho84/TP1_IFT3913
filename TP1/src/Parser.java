import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private List<List<String>> parsedLists;
    private List<String> codeToParse;
    private String currChemin;

    /**
     *
     * @param firstRow
     */
    public Parser(List<String> firstRow) {
        this.parsedLists = new ArrayList<>();
        this.parsedLists.add(firstRow);
        this.codeToParse = new ArrayList<>();
        this.currChemin = null;
    }

    /**
     *
     * @return
     */
    public List<List<String>> getParsedList() {
        return parsedLists;
    }

    /**
     *
     * @param parsedLists
     */
    private void setParsedList(List<List<String>> parsedLists) {
        this.parsedLists = parsedLists;
    }

    /**
     *
     * @return
     */
    public List<String> getCodeToParse() {
        return codeToParse;
    }

    /**
     *
     * @param codeToParse
     */
    void setCodeToParse(List<String> codeToParse) {
        this.codeToParse = codeToParse;
    }

    /**
     *
     * @return
     */
    public String getCurrChemin() {
        return currChemin;
    }

    /**
     *
     * @param currChemin
     */
    void setCurrChemin(String currChemin) {
        this.currChemin = currChemin;
    }

    /**
     *
     * @param index
     * @param element
     */
    void addFirstParserList(int index, List<String> element) {
        this.parsedLists.add(index, element);
    }

    /**
     *
     */
    void clearParsedList() { this.parsedLists.clear(); }

    /**
     *
     * @param methodesParser
     * @return
     */
    List<String> treatCode(MethodesParser methodesParser) {
        return null;
    }

    /**
     *
     * @param currentClass
     * @return
     */
    List<String> treatCode(String currentClass) {
        return null;
    }

    /**
     *
     * @param element
     * @return
     */
    boolean detectForComments(String element) {
        return !element.contains("//") && !element.contains("/**") &&
                !element.contains("/*") && !element.contains("*") &&
                !element.contains("*/");
    }

    /**
     *
     * @param element
     * @return
     */
    boolean detectForClass(String element) {
        return element.contains("class") || element.contains("abstract class") ||
                element.contains("enum") || element.contains("interface");
    }

    /**
     *
     * @param lineElements
     * @return
     */
    boolean detectForPrivacy(String[] lineElements) {
        return lineElements[0].contentEquals("private") ||
                lineElements[0].contentEquals("protected") ||
                lineElements[0].contentEquals("public");
    }

    /**
     *
     * @param line
     * @return
     */
    boolean detectForPrivacy(String line) {
        return line.contains("private") || line.contains("protected") ||
                line.contains("public");
    }

    /**
     *
     * @param element
     * @return
     */
    String splitParenthesis(String element) {
        String[] splitElements = element.split("\\(");
        if(splitElements.length > 1) { return splitElements[0] + "_" + splitElements[1]; }
        return splitElements[0];
    }

    /**
     *
     * @param element
     * @param splitElements
     * @return
     */
    String removeFirstChar(String element, String[] splitElements) {
        String[] tempList = Arrays.copyOfRange(splitElements, 1, splitElements.length);
        return String.join("", tempList);
    }

    /*  ===========================================================================        */

    //  ---------------------------------------------------------------------------        //
    //  | Méthodes pour calculer les lignes, les commentaires et les predicats :  |        //
    //  ---------------------------------------------------------------------------        //

    /**
     *
     * @param classTextList
     * @param is_CLOC
     * @return
     */
    int calculateLinesOrComments(List<String> classTextList, Boolean is_CLOC) {
        int totalCount = 0;
        List<String> elementLine;
        for (String element : classTextList) {
            elementLine = separateAndRemoveSpaces(element);
            if(is_CLOC) {
                if (elementLine.size() > 1 &&
                        (elementLine.get(0).contains("/**") ||
                                elementLine.get(0).contains("*") ||
                                elementLine.contains("//") ||
                                elementLine.contains("/*") ||
                                elementLine.contains("*/"))) {
                    totalCount++;
                }
            } else {
                if(!elementLine.get(0).contains("/*") &&
                        !elementLine.get(0).contains("*") &&
                        !elementLine.get(0).contains("//")) {
                    totalCount++;
                }
            }
        }
        return totalCount;
    }

    List<String> separateAndRemoveSpaces(String element) {
        List<String> elementLine = new ArrayList<>(Arrays.asList(element.split(" ")));
        return removeLineSpaces(elementLine);
    }

    /**
     *
     * @param elementLine
     * @return
     */
    List<String> removeLineSpaces(List<String> elementLine) {
        for (String item : elementLine) {
            if (item.contentEquals("")) {
                elementLine = elementLine.subList(1, elementLine.size());
            }
        }
        return elementLine;
    }

    /**
     *
     * @param classTextList
     * @return
     */

    float calculer_NCLOC(List<String> classTextList) {
        return calculateLinesOrComments(classTextList,  false);
    }

    /**
     *
     * @param nClocResult
     * @param clocResult
     * @return
     */
    float calculer_LOC(float nClocResult, float clocResult) {
        return nClocResult + clocResult;
    }

    /**
     *
     * @param classTextList
     * @return
     */
    float calculer_CLOC(List<String> classTextList) {
        return calculateLinesOrComments(classTextList,  true);
    }

    /**
     *
     * @param commentsDensity
     * @param linesOfCode
     * @return
     */
    float calculer_DC(float commentsDensity, float linesOfCode) {
        return commentsDensity/linesOfCode;
    }

    /**
     *
     * @param currFileData
     */
    void rearrangeData(List<String> currFileData) {
        int currIndex = 0, dcIndex, locIndex, nlocIndex;
        float currDcRatio, currLoc, currNLoc, currIndexDcRatio, currIndexLoc,
                currIndexNLoc;
        if(currFileData.size() == 5) {
            dcIndex = 4;
            locIndex = 2;
            nlocIndex = 3;
        } else {
            dcIndex = 5;
            locIndex = 3;
            nlocIndex = 4;
        }
        currDcRatio = Float.parseFloat(currFileData.get(dcIndex));
        currLoc = Float.parseFloat(currFileData.get(locIndex));
        currNLoc = Float.parseFloat(currFileData.get(nlocIndex));
        if(this.getParsedList().size() > 1) {
            for (List<String> currIndexData : this.getParsedList()) {
                if (currIndex > 0) {
                    currIndexDcRatio = Float.parseFloat(currIndexData.get(dcIndex));
                    currIndexLoc = Float.parseFloat(currIndexData.get(locIndex));
                    currIndexNLoc = Float.parseFloat(currIndexData.get(nlocIndex));
                    if (currDcRatio < currIndexDcRatio) {
                        addFirstParserList(currIndex, currFileData);
                        break;
                    } else {
                        if (currDcRatio == currIndexDcRatio) {
                            if (currNLoc > currIndexNLoc) {
                                addFirstParserList(currIndex, currFileData);
                                break;
                            }
                        }
                    }
                }
                currIndex++;
            }
        } else { addFirstParserList(1, currFileData); }
    }
}
