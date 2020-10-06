import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private List<List<String>> parsedLists;
    private List<String> codeToParse;
    private String currChemin;
    private List<List<String>> totalParsedLists;
    private List<List<String>> complexityFile;

    /**
     *  Constructeur de la classe Parser
     * @param firstRow C'est les valeurs de la premiere ligne pour les deux
     *                 sous-classes(MethodesParser et ClassesParser).
     */
    public Parser(List<String> firstRow, List<String> firstTotalRow) {
        this.parsedLists = new ArrayList<>();
        this.parsedLists.add(firstRow);
        this.codeToParse = new ArrayList<>();
        this.currChemin = null;
        this.totalParsedLists = new ArrayList<>();
        this.totalParsedLists.add(firstTotalRow);
        this.complexityFile = new ArrayList<>();
    }

    /**
     *  Getter
     * @return La liste parsedList.
     */
    public List<List<String>> getParsedList() {
        return parsedLists;
    }

    /**
     *  Setter
     * @param parsedLists La liste qu'on veut la remplacer par la liste originale.
     */
    private void setParsedList(List<List<String>> parsedLists) {
        this.parsedLists = parsedLists;
    }

    /**
     *
     * @return
     */
    public List<List<String>> getTotalParsedLists() {
        return totalParsedLists;
    }

    /**
     *
     * @param totalParsedLists
     */
    public void setTotalParsedLists(List<List<String>> totalParsedLists) {
        this.totalParsedLists = totalParsedLists;
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
     * @param index
     * @param element
     */
    void addSecondParserList(int index, List<String> element) {
        this.totalParsedLists.add(index, element);
    }

    /**
     *
     * @return
     */
    public List<List<String>> getcomplexityFile() { return complexityFile; }

    /**
     *
     * @param complexityFile
     */
    public void setcomplexityFile(List<List<String>> complexityFile) {
        this.complexityFile = complexityFile;
    }

    /**
     *
     * @param arr
     */
    public void addcomplexityArray(List<String> arr) {
        this.complexityFile.add(arr);
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
     * @param densityComments
     * @param objectComplexity
     * @return
     */
    float calculer_BC(float densityComments, float objectComplexity) {
        return densityComments/objectComplexity;
    }

    /**
     *
     * @param currFileData
     */
    void rearrangeData(List<String> currFileData) {
        List<String> tempFileData;
        int bcIndex, locIndex;
        float currBCRatio;
        if(currFileData.size() == 6) {
            bcIndex = 5;
            locIndex = 3;
            tempFileData = Arrays.asList(currFileData.get(0),
                    currFileData.get(1), currFileData.get(2), currFileData.get(3),
                    currFileData.get(4));
        } else {
            bcIndex = 6;
            locIndex = 4;
            tempFileData = Arrays.asList(currFileData.get(0),
                    currFileData.get(1), currFileData.get(2), currFileData.get(3),
                    currFileData.get(4), currFileData.get(5));
        }
        currBCRatio = Float.parseFloat(currFileData.get(bcIndex));
        if(this.totalParsedLists.size() > 1) {
            iterateOverFileData(currFileData, tempFileData, currBCRatio, bcIndex,
                    locIndex);
        } else {
            addFirstParserList(1, tempFileData);
            addSecondParserList(1, currFileData);
        }
    }

    /**
     *
     * @param currFileData
     * @param tempFileData
     * @param currBCRatio
     * @param bcIndex
     * @return
     */
    void iterateOverFileData(List<String> currFileData, List<String> tempFileData,
                             float currBCRatio, int bcIndex, int locIndex) {
        int currIndex = 0;
        float currIndexBcRatio, currFileLoc, currIndexLoc;
        currFileLoc = Float.parseFloat(currFileData.get(locIndex));
        for (List<String> currIndexData : this.getTotalParsedLists()) {
            if (currIndex > 0) {
                currIndexBcRatio = Float.parseFloat(currIndexData.get(bcIndex));
                currIndexLoc = Float.parseFloat(currIndexData.get(locIndex));
                if (currBCRatio < currIndexBcRatio) {
                    addToParserList(currIndex, currFileData, tempFileData);
                    return;
                } else {
                    if(currBCRatio == currIndexBcRatio) {
                        if(currFileLoc > currIndexLoc) {
                            addToParserList(currIndex, currFileData, tempFileData);
                            return;
                        }
                    }
                }
            }
            currIndex++;
        }
        addToParserList(currIndex, currFileData, tempFileData);
    }

    /**
     *
     * @param currIndex
     * @param currFileData
     * @param tempFileData
     */
    void addToParserList(int currIndex, List<String> currFileData,
                         List<String> tempFileData) {
        this.addFirstParserList(currIndex, tempFileData);
        this.addSecondParserList(currIndex, currFileData);
    }
}
