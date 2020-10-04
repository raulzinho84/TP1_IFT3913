import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodesParser extends Parser {

    /**
     *
     * @param firstRow
     */
    public MethodesParser(List<String> firstRow) { super(firstRow); }

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
    List<List<String>> getFileMethodes(String chemin, String className) {
        String firstMethodeName = null, secondMethodeName, methodeName, tempMethodeName;
        int lineCodeIndex = 0, numPredicats = 0, capturedLineCode = 0;
        List<String> capturedMethode = new ArrayList<>(), reparsedMethode,
                nonCommentsMethode = new ArrayList<>();
        List<List<String>> allMethodesData = new ArrayList<>();
        boolean methodeFound = false, first = true, numPredicatsCounted = false;
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
                        addMethodeData(reparsedMethode, chemin, className, methodeName);
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
        return allMethodesData;
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
     * @param chemin
     * @param currClasse
     * @param methodeName
     * @return
     */
    void addMethodeData(List<String> reparsedMethode,
                                String chemin, String currClasse,
                                String methodeName) {
        List<String> treatedFile = new ArrayList<>();
        treatedFile.add(chemin);
        treatedFile.add(currClasse);
        treatedFile.add(methodeName);
        float commentsDensity = methode_CLOC(reparsedMethode);
        treatedFile.add(String.valueOf(commentsDensity));
        float codeDensity = methode_NCLOC(reparsedMethode);
        float linesOfCode = methode_LOC(codeDensity, commentsDensity);
        treatedFile.add(String.valueOf(linesOfCode));
        float classDensity = methode_DC(commentsDensity, linesOfCode);
        treatedFile.add(String.valueOf(classDensity));
        rearrangeData(treatedFile);
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
        int totalPredicats = 0;
        for(String lineCode : reparsedMethode) {
            elementLine = separateAndRemoveSpaces(lineCode);
            if(!elementLine.get(0).contains("/*") &&
                    !elementLine.get(0).contains("*") &&
                    !elementLine.get(0).contains("//")) {
                totalPredicats += handlePredicats(lineCode);
            }
        }
        System.out.println("totalPredicats(total) : " + totalPredicats);
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
                return handleSwitchPredicats(lineCode);
            } else { return 1; }
        }
        return 0;
    }

    /**
     *
     * @param lineCode
     * @return
     */
    int handleSwitchPredicats(String lineCode) {
        int totalCount = 0;
        return 1;
    }

    /**
     *
     * @param nClocResult
     * @param clocResult
     * @return
     */
    float methode_LOC(float nClocResult, float clocResult) {
        return calculer_LOC(nClocResult, clocResult);
    }

    /**
     *
     * @param methodeText
     * @return
     */
    float methode_CLOC(List<String> methodeText) {
        return calculer_CLOC(methodeText);
    }

    /**
     *
     * @param methodeText
     * @return
     */
    float methode_NCLOC(List<String> methodeText) {
        return calculer_NCLOC(methodeText);
    }

    /**
     *
     * @param commentsDensity
     * @param linesOfcode
     * @return
     */
    float methode_DC(float commentsDensity, float linesOfcode) {
        return calculer_DC(commentsDensity, linesOfcode);
    }
}
