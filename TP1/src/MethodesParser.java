import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodesParser extends Parser {

    private static boolean isSwitch;
    private List<List<String>> complexityFile;

    /**
     *
     * @param firstRow
     */
    public MethodesParser(List<String> firstRow) {
        super(firstRow, Arrays.asList("chemin",
                "class", "methode", "methode_CLOC", "methode_LOC", "methode_DC",
                "methode_BC"));
        this.complexityFile = new ArrayList<>();
        this.addcomplexityArray(Arrays.asList("CC", "methode_BC"));
    }

    /**
     *
     * @return
     */
    public boolean isSwitch() { return isSwitch; }

    /**
     *
     * @param aSwitch
     */
    public void setSwitch(boolean aSwitch) { isSwitch = aSwitch; }

    @Override
    List<String> treatCode(String currentClass) {
        return super.treatCode(currentClass);
    }

    /**
     *
     * @param chemin
     * @param className
     * @return
     */
    int getFileMethodes(String chemin, String className, float totalClasseDC) {
        String firstMethodeName = null, secondMethodeName, methodeName, tempMethodeName;
        int lineCodeIndex = 0, numPredicats = 0, totalPredicats = 0;
        List<String> capturedMethode = new ArrayList<>(), reparsedMethode,
                nonCommentsMethode = new ArrayList<>();
        boolean methodeFound = false, first = true;
        for(String lineCode : this.getCodeToParse()) {
            if(!methodeFound) {
                if(first) {
                    firstMethodeName = getMethodName(lineCode,
                            getLineElements(lineCode));
                    if(!firstMethodeName.equals(" ")) { first = false; }
                }
                if(!firstMethodeName.equals(" ")) {
                    capturedMethode = addJavaDOC(lineCodeIndex);
                    if(this.detectForPrivacy(this.getCodeToParse().get(lineCodeIndex - 1))) {
                        capturedMethode.add(this.getCodeToParse().get(lineCodeIndex - 1));
                    }
                    capturedMethode.add(lineCode);
                    nonCommentsMethode.add(lineCode);
                    methodeFound = true;
                }
            } else {
                secondMethodeName = getMethodName(lineCode,
                        getLineElements(lineCode));
                if(!secondMethodeName.equals(" ")) {
                    methodeFound = false;
                    reparsedMethode = reparseMethode(capturedMethode);
                    tempMethodeName = firstMethodeName.split("_")[0];
                    methodeName = extractMethodeName(reparsedMethode, tempMethodeName);
                    if(methodeName != null) {
                        numPredicats = calculerPredicats(nonCommentsMethode);
                        totalPredicats += numPredicats;
                        float methode_BC = addMethodeData(reparsedMethode, chemin,
                                className, methodeName, numPredicats + 1);
                        this.addcomplexityArray(Arrays.asList(String.valueOf(numPredicats + 1),
                                String.valueOf(methode_BC)));
                    }
                    firstMethodeName = secondMethodeName;
                    secondMethodeName = " ";
                    capturedMethode.clear();
                    nonCommentsMethode.clear();
                }
                capturedMethode.add(lineCode);
                nonCommentsMethode.add(lineCode);
            }
            lineCodeIndex++;
        }
        return totalPredicats + 1;
    }

    /**
     *
     * @param element
     * @param lineElements
     * @return
     */
    String getMethodName(String element, String[] lineElements) {
        String prevItem = null, prevPrevItem = null;
        StringBuilder tempString = new StringBuilder(), emptyString = new StringBuilder("");
        int itemIndex;
        if(this.detectForComments(element) && this.detectForPrivacy(lineElements) &&
                !element.contains("=") && !element.contains("class")) {
            for(String item : lineElements) {
                itemIndex = Arrays.asList(lineElements).indexOf(item);
                if(itemIndex >= 2) {
                    prevItem = lineElements[itemIndex - 1];
                    prevPrevItem = lineElements[itemIndex - 2];
                }
                if((item.contains("(") || item.contains(","))) {
                    if(item.contains("(")) {
                        String[] temp = item.split("\\(");
                        tempString = emptyString;
                        if(temp.length >= 2 && !temp[1].contains(")")) {
                            tempString.append(temp[0]).append("_").append(temp[1]);
                        } else { tempString.append(temp[0]); }
                    } else {
                        if(item.contains(",") && itemIndex != lineElements.length - 1) {
                            tempString.append("_").append(lineElements[itemIndex + 1]);
                        }
                    }
                } else {
                    if(itemIndex >=2) {
                        if(lineElements.length - 1 >= itemIndex + 1) {
                            if ((prevItem.contentEquals("final") && prevPrevItem.contentEquals("static")) ||
                                    prevItem.contentEquals("static")) {
                                if(lineElements[itemIndex + 1].contains("(")) {
                                    tempString.append(lineElements[itemIndex + 1]);
                                }
                            }
                        }
                    }
                }
            }
        }
        if(tempString.length() > 0) { return tempString.toString(); }
        return " ";
    }

    /**
     *
     * @param lineCodeIndex
     * @return
     */
    List<String> addJavaDOC(int lineCodeIndex) {
        int prevLineIndex = lineCodeIndex - 1;
        List<String> preComments = new ArrayList<>();
        while(!this.getCodeToParse().get(prevLineIndex).contains("}") &&
                !this.getCodeToParse().get(prevLineIndex).contains("/**")) {
            if(!detectForComments(this.getCodeToParse().get(prevLineIndex)) ||
                    this.getCodeToParse().get(prevLineIndex).contains("@")) {
                preComments.add(0, this.getCodeToParse().get(prevLineIndex));
            }
            prevLineIndex--;
        }
        preComments.add(0, this.getCodeToParse().get(prevLineIndex));
        return preComments;
    }

    /**
     *
     * @param capturedMethode
     * @return
     */
    List<String> reparseMethode(List<String> capturedMethode) {
        int count = 0;
        boolean countStarted = false;
        List<String> reparsedMethode = new ArrayList<>();
        for(String lineCode : capturedMethode) {
            if(lineCode.contains( "{") || lineCode.contains("}")) {
                if(lineCode.contains("{")) {
                    count++;
                    countStarted = true;
                }
                else { count--; }
            } else { if(countStarted && count == 0) { break; } }
            reparsedMethode.add(lineCode);
        }
        return reparsedMethode;
    }

    /**
     *
     * @param splittedLineCode
     * @param comparator
     * @param comparatorRegex
     * @return
     */
    String iterateAndSplitArray(String[] splittedLineCode,
                                              String comparator,
                                              String comparatorRegex) {
        int i = 0;
        while(!splittedLineCode[i].contains(comparator)) { i++; }
        return splittedLineCode[i].split(comparatorRegex)[0];
    }

    /**
     *
     * @param splittedLineCode
     * @return
     */
    String buildStringFromListOneLine(String[] splittedLineCode) {
        StringBuilder extractedMethode = new StringBuilder();
        int currIndex = 0;
        String[] splitElements;
        for (String element : splittedLineCode) {
            if (element.contains("(")) {
                extractedMethode.append(splitParenthesis(element));
            } else {
                if(element.contains(",")) {
                    splitElements = element.split("(?!^)");
                    if(splitElements[0].contentEquals(",")) {
                        extractedMethode.append("_").append(removeFirstChar(element,
                                splitElements));
                    } else {
                        if(splitElements[splitElements.length - 1].contentEquals(",") &&
                                (currIndex != splittedLineCode.length - 1)) {
                            extractedMethode.append("_").append(splittedLineCode[currIndex + 1]);
                        }
                    }
                }
            }
            currIndex++;
        }
        return extractedMethode.toString();
    }

    /**
     *
     * @param reparsedMethode
     * @param lineCode
     * @return
     */
    String buildStringFromMultipleLines(List<String> reparsedMethode,
                                                      String lineCode) {
        int start = 0;
        String[] splittedLineCode;
        StringBuilder multipleLinesParams = new StringBuilder();
        String oneLineList;
        while(!lineCode.contains(")")) {
            splittedLineCode = lineCode.split(" ");
            oneLineList = buildStringFromListOneLine(splittedLineCode);
            if(!oneLineList.equals("")) { multipleLinesParams.append(oneLineList); }
            lineCode = reparsedMethode.get(start++);
        }
        return multipleLinesParams.toString();
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
     * @param reparsedMethode
     * @param chemin
     * @param currClasse
     * @param methodeName
     * @param numPredicats
     * @return
     */
    float addMethodeData(List<String> reparsedMethode,
                                String chemin, String currClasse,
                                String methodeName, int numPredicats) {
        List<String> treatedFile = new ArrayList<>();
        treatedFile.add(chemin);
        treatedFile.add(currClasse);
        treatedFile.add(methodeName);
        float commentsDensity = methode_CLOC(reparsedMethode);
        treatedFile.add(String.valueOf(commentsDensity));
        float codeDensity = methode_NCLOC(reparsedMethode);
        float linesOfCode = methode_LOC(codeDensity, commentsDensity);
        treatedFile.add(String.valueOf(linesOfCode));
        float methodeDensity = methode_DC(commentsDensity, linesOfCode);
        treatedFile.add(String.valueOf(methodeDensity));
        float methode_BC = methode_BC(methodeDensity, numPredicats);
        treatedFile.add(String.valueOf(methode_BC));
        this.rearrangeData(treatedFile);
        return methode_BC;
    }

    /*  ==================================================================  */

    //  ------------------------------------------------------------        //
    //  | Méthodes pour extraire les méthodes :                    |        //
    //  ------------------------------------------------------------        //


    /**
     *
     * @param reparsedMethode
     * @param firstmethodeName
     * @return
     */
    String extractMethodeName(List<String> reparsedMethode,
                                            String firstmethodeName) {
        int start = 0;
        String[] splittedLineCode;
        while(!reparsedMethode.get(start).contains(firstmethodeName)) { start++; }
        String lineCode = reparsedMethode.get(start);
        List<String> updatedReparsedMethode =
                new ArrayList<>(reparsedMethode.subList(start, reparsedMethode.size()));
        if(lineCode.contains(firstmethodeName) &&
                detectForPrivacy(lineCode)) {
            splittedLineCode = lineCode.split(" ");
            if (lineCode.contains("()")) {
                return iterateAndSplitArray(splittedLineCode, "()", "\\(\\)");
            } else if(lineCode.contains("(") && lineCode.contains(")")) {
                return buildStringFromListOneLine(splittedLineCode);
            } else {
                return buildStringFromMultipleLines(updatedReparsedMethode, lineCode);
            }
        }
        return null;
    }

    /**
     *
     * @param reparsedMethode
     * @return
     */
    int calculerPredicats(List<String> reparsedMethode) {
        List<String> elementLine;
        int totalPredicats = 0, temp;
        for(String lineCode : reparsedMethode) {
            elementLine = separateAndRemoveSpaces(lineCode);
            if(!elementLine.get(0).contains("/*") &&
                    !elementLine.get(0).contains("*") &&
                    !elementLine.get(0).contains("//")) {
                totalPredicats += handlePredicats(lineCode);
            }
        }
        return totalPredicats;
    }

    /**
     *
     * @param lineCode
     * @return
     */
    int handlePredicats(String lineCode) {
        if(lineCode.contains("if(") || lineCode.contains("if (")  ||
                lineCode.contains("while(") || lineCode.contains("while (")
                || lineCode.contains("switch(") ||
                lineCode.contains("switch (")) {
            if (lineCode.contains("switch(") || lineCode.contains("switch (")) {
                isSwitch = true;
            } else { return 1; }
        } else {
            if(isSwitch && (lineCode.contains("case") ||
                    lineCode.contains("default"))) {
                if(lineCode.contains("default")) { isSwitch = false; }
                return 1;
            }
        }
        return 0;
    }

    /**
     *
     * @param nClocResult C'est le nombre des lignes sans commentaires.
     * @param clocResult C'est le nombre des lignes avec commentaires.
     * @return Le nombre total des lignes(avec et sans) commentaires.
     */
    float methode_LOC(float nClocResult, float clocResult) {
        return calculer_LOC(nClocResult, clocResult);
    }

    /**
     *
     * @param methodeText C'est la methode dont on va calculer les
     *                    commentaires contenu.
     * @return Le nombre des lignes contenant des commentaires.
     */
    float methode_CLOC(List<String> methodeText) {
        return calculer_CLOC(methodeText);
    }

    /**
     *
     * @param methodeText C'est la methode dont on va calculer les
     *      *                    lignes sans commentaires contenu.
     * @return Le nombre total des lignes sans commentaires dans une methode.
     */
    float methode_NCLOC(List<String> methodeText) {
        return calculer_NCLOC(methodeText);
    }

    /**
     *
     * @param commentsDensity CLOC
     * @param linesOfcode LOC
     * @return CLOC/LOC
     */
    float methode_DC(float commentsDensity, float linesOfcode) {
        return calculer_DC(commentsDensity, linesOfcode);
    }

    /**
     *
     * @param methode_DC
     * @param CC
     * @return
     */
    float methode_BC(float methode_DC, float CC) {
        return calculer_BC(methode_DC, CC);
    }
}
