
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CodeAnalyzer {

    public static List<List<String>> classeFilesData = new ArrayList<>();
    public static List<List<String>> methodesFilesData = new ArrayList<>();
    public static List<String> classeNames = new ArrayList<>();
    public static List<String> visitedLinks = new ArrayList<>();
    public static String lastLineItem = null;


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String currentPath = getAbsolutePath();
        System.out.println(currentPath);
        String linkToPass = "https://github.com/jfree/jfreechart";
        String firstLink = linkToPass + "/tree/master";
        String secondLink = linkToPass + "/blob/master";
        classeFilesData.add(Arrays.asList("chemin", "class", "classe_LOC",
                "classe_CLOC", "classe_DC"));
        methodesFilesData.add(Arrays.asList("chemin", "class", "methode", "methode_CLOC",
                "methode_LOC", "methode_DC"));
        getDataFiles("https://github.com/jfree/jfreechart", firstLink, secondLink);
        FileWriter classesWriter = new FileWriter(currentPath + "/classes.csv");
        FileWriter methodesWriter = new FileWriter(currentPath + "/methodes.csv");
        printCSV(classeFilesData, classesWriter);
        printCSV(methodesFilesData, methodesWriter);
    }

    /**
     *
     * @return
     */
    public static String getAbsolutePath() {
        Path currentRelativePath = Paths.get("");
        return currentRelativePath.toAbsolutePath().toString();
    }

    /**
     *
     * @param fileToPrint
     * @param writer
     * @throws IOException
     */
    public static void printCSV(List<List<String>> fileToPrint, FileWriter writer) throws IOException {
        for (List<String> methodeData: fileToPrint) {
            System.out.println(methodeData);
            String collect = String.join(", ", methodeData);
            System.out.println(collect);
            writer.write(collect);
            writer.write("\t\r\n");
        }
        writer.close();
    }

    /**
     * @param dataURL    C'est l'extension du siteWeb a traiter.
     * @param firstLink  C'est la premiere extension contenant /tree.
     * @param secondLink C'est la deuxieme extension contenant /blob.
     * @throws IOException
     */
    public static void getDataFiles(String dataURL, String firstLink,
                                    String secondLink) throws IOException {
        Document doc = getData(dataURL);
        if (doc != null) {
            Elements links = doc.select("a");
            for (Element link : links) {
                String chemin = link.attr("abs:href");
                if ((chemin.contains(firstLink) || chemin.contains(secondLink)) &&
                        !chemin.contains("#start-of-content")) {
                    if (!visitedLinks.contains(chemin)) {
                        visitedLinks.add(chemin);
                        if (chemin.contains(firstLink)) {
                            getDataFiles(chemin, firstLink, secondLink);
                        } else {
                            getAndUseExtractedCode(chemin);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param dataURL
     * @return
     * @throws IOException
     */
    public static Document getData(String dataURL) throws IOException {
        try {
            return Jsoup.connect(dataURL).get();
        } catch (HttpStatusException ignored) {
        }
        return null;
    }

    /**
     * @param chemin
     * @throws IOException
     */
    public static void getAndUseExtractedCode(String chemin) throws IOException {
        List<String> fileCode = extractCode(chemin);
        if (fileCode != null) {
            List<String> classeTreatedFile = treatClass(fileCode, chemin, true, null);
            if (classeTreatedFile != null) {
                assignClassFilesData(chemin, classeTreatedFile, fileCode);
                List<String> methodesTreatedFiles = treatClass(fileCode, chemin,
                        false, classeTreatedFile.get(2));
            }
        }
    }

    public static void assignClassFilesData(String chemin, List<String> classeTreatedFile,
                                            List<String> fileCode) {
        float totalClasseCloc, totalClasseNCLoc, totalClasseLoc;
        int startIndex, endIndex;
        startIndex = Integer.parseInt(classeTreatedFile.get(0));
        endIndex = Integer.parseInt(classeTreatedFile.get(1));
        totalClasseCloc = classe_CLOC(fileCode.subList(startIndex, endIndex));
        totalClasseNCLoc = classe_NCLOC(fileCode.subList(startIndex, endIndex));
        totalClasseLoc = classe_LOC(totalClasseNCLoc, totalClasseCloc);
        classeFilesData.add(Arrays.asList(chemin, classeTreatedFile.get(2),
                String.valueOf(totalClasseLoc), String.valueOf(totalClasseCloc),
                String.valueOf(classe_DC(totalClasseCloc, totalClasseLoc))));
    }

    /**
     * @param dataURL
     * @return
     * @throws IOException
     */
    public static List<String> extractCode(String dataURL) throws IOException {
        Document doc = getData(dataURL);
        if (doc != null) {
            String docText = doc.toString().replaceAll("<[^>]*>", "");
            docText = docText.replaceAll("(?m)^[ \t]*\r?\n", "");
            return Arrays.asList(docText.split("\n"));
        }
        return null;
    }

    public static List<String> treatClass(List<String> currentCode, String chemin,
                                                 boolean isClass,
                                          String currentClass) throws IOException {
        String className = null, tempClassName;
        int startIndex = 0, endIndex = 0, currIndex = 0;
        List<List<String>> allMethodesData = new ArrayList<>();
        if (isClass) {
            for (String element : currentCode) {
                tempClassName = getClassName(element, getLineElements(element));

                if (element.contains("Copy lines")) {
                    endIndex = currIndex;
                }
                if (tempClassName != null) {
                    className = tempClassName;
                    classeNames.add(className);
                    startIndex = currIndex;
                }
                if (startIndex != 0 && endIndex != 0) {
                    return getClassIndexesAndName(startIndex, endIndex, className);
                }
                currIndex++;
            }
        } else { getFileMethodes(currentCode, chemin, currentClass); }
        return null;
    }

    public static String getClassName(String element, String[] lineElements) {
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

    public static List<String> getClassIndexesAndName(int startIndex, int endIndex,
                                                      String className) {
        return new ArrayList<>(Arrays.asList(String.valueOf(startIndex),
                String.valueOf(endIndex), className));
    }

    public static List<List<String>> getFileMethodes(List<String> currentCode, String chemin, String className) {
        String firstMethodeName = null, secondMethodeName, methodeName = null, tempMethodeName;
        List<String> capturedMethode = new ArrayList<>(), reparsedMethode = null,
                methodeData = new ArrayList<>();
        List<List<String>> allMethodesData = new ArrayList<>();
        boolean methodeFound = false, first = true, secondMethodeFound = false;
        for(String lineCode : currentCode) {
            if(!methodeFound) {
                if(first) {
                    firstMethodeName = getMethodName(lineCode,
                            getLineElements(lineCode));
                    if(!firstMethodeName.equals(" ")) { first = false; }
                }
                if(!firstMethodeName.equals(" ")) {
                    capturedMethode.add(lineCode);
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
                        methodeData = getMethodeData(reparsedMethode, chemin, className, methodeName);
                        List<String> tempList = new ArrayList<>(methodeData);
                        methodesFilesData.addAll(Collections.singleton(tempList));
                    }
                    firstMethodeName = secondMethodeName;
                    secondMethodeName = " ";
                    capturedMethode.clear();
                    methodeData.clear();
                }
                capturedMethode.add(lineCode);
            }

        }
        return allMethodesData;
    }

    public static String getMethodName(String element, String[] lineElements) {
        String methodName, prevItem = null, prevPrevItem = null;
        StringBuilder tempString = new StringBuilder(), emptyString = new StringBuilder("");
        int itemIndex;
        if(detectForComments(element) && detectForPrivacy(lineElements) &&
                !element.contains("=") && !element.contains("class")) {
            for(String item : lineElements) {
                itemIndex = Arrays.asList(lineElements).indexOf(item);
                if(itemIndex >= 2) {
                    prevItem = lineElements[itemIndex - 1];
                    prevPrevItem = lineElements[itemIndex - 2];
                }
                if((item.contains("(") || item.contains(","))) {
                    if(item.contains("(")) {
                        if(tempString.toString().contains("(")) {
                        }
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
                if(item.contains(",")) { lastLineItem = item; }
            }
        }
        if(tempString.length() > 0) {
            return tempString.toString();
        }
        return " ";
    }

    public static List<String> reparseMethode(List<String> capturedMethode) {
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
            } else {
                if(countStarted && count == 0) { break; }
            }
            reparsedMethode.add(lineCode);
        }
        return reparsedMethode;
    }

    public static String iterateAndSplitArray(String[] splittedLineCode,
                                              String comparator,
                                              String comparatorRegex) {
        int i = 0;
        while(!splittedLineCode[i].contains(comparator)) {
            i++;
        }
        return splittedLineCode[i].split(comparatorRegex)[0];
    }

    public static String buildStringFromListOneLine(String[] splittedLineCode) {
        StringBuilder extractedMethode = new StringBuilder();
        int currIndex = 0;
        String[] splitElements;
        for (String element : splittedLineCode) {
            if (element.contains("(")) { extractedMethode.append(splitParenthesis(element)); }
            else {
                if(element.contains(",")) {
                    splitElements = element.split("(?!^)");
                    if(splitElements[0].contentEquals(",")) {
                        extractedMethode.append("_").append(removeFirstChar(element, splitElements));
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

    public static String buildStringFromMultipleLines(List<String> reparsedMethode,
                                                      String lineCode) {
        int start = 0;
        String[] splittedLineCode;
        StringBuilder multipleLinesParams = new StringBuilder();
        String oneLineList;
        while(!lineCode.contains(")")) {
//            System.out.println("lineCode : " + lineCode + ", reparsedMethode : " + reparsedMethode + ", start : " + start);
            splittedLineCode = lineCode.split(" ");
            oneLineList = buildStringFromListOneLine(splittedLineCode);
            if(!oneLineList.equals("")) {
                multipleLinesParams.append(oneLineList);
            }
            lineCode = reparsedMethode.get(start++);
        }
        return multipleLinesParams.toString();
    }

    public static String[] getLineElements(String element) {
        element = element.replaceAll("\\s+", "+");
        String[] lineElements = element.split("\\+");
        return Arrays.copyOfRange(lineElements, 1, lineElements.length);
    }

    /**
     *
     * @param currentCode
     * @param chemin
     * @return
     */
    public static List<String> getClasseData(List<String> currentCode, String chemin) {
        String className;
        List<String> treatedFile = new ArrayList<>();
        className = currentCode.remove(currentCode.size() - 1);
        treatedFile.add(chemin);
        treatedFile.add(className);
        float commentsDensity = classe_CLOC(currentCode);
        treatedFile.add(String.valueOf(commentsDensity));
        float codeDensity = classe_NCLOC(currentCode);
        treatedFile.add(String.valueOf(codeDensity));
        float linesOfCode = classe_LOC(codeDensity, commentsDensity);
        treatedFile.add(String.valueOf(linesOfCode));
        float classDensity = classe_DC(commentsDensity, linesOfCode);
        treatedFile.add(String.valueOf(classDensity));
        return treatedFile;
    }

    public static List<String> getMethodeData(List<String> currentCode, String chemin,
                                              String currClasse, String methodeName) {
        List<String> treatedFile = new ArrayList<>();
        treatedFile.add(chemin);
        treatedFile.add(currClasse);
        treatedFile.add(methodeName);
        float commentsDensity = methode_CLOC(currentCode);
        treatedFile.add(String.valueOf(commentsDensity));
        float codeDensity = methode_NCLOC(currentCode);
        float linesOfCode = methode_LOC(codeDensity, commentsDensity);
        treatedFile.add(String.valueOf(linesOfCode));
        float classDensity = methode_DC(commentsDensity, linesOfCode);
        treatedFile.add(String.valueOf(classDensity));
        return treatedFile;
    }

    public static String splitParenthesis(String element) {
        String[] splitElements = element.split("\\(");
        if(splitElements.length > 1) {
            return splitElements[0] + "_" + splitElements[1];
        }
        return splitElements[0];
    }

    public static String removeFirstChar(String element, String[] splitElements) {
        String[] tempList = Arrays.copyOfRange(splitElements, 1, splitElements.length);
        return String.join("", tempList);
    }

    public static boolean detectForComments(String element) {
        return !element.contains("//") && !element.contains("/**") &&
                !element.contains("/*") && !element.contains("*") &&
                !element.contains("*/");
    }

    public static boolean detectForClass(String element) {
        return element.contains("class") || element.contains("abstract class") ||
                element.contains("enum") || element.contains("interface");
    }

    public static boolean detectForPrivacy(String[] lineElements) {
        return lineElements[0].contentEquals("private") ||
                lineElements[0].contentEquals("protected") ||
                lineElements[0].contentEquals("public");
    }

    public static boolean detectForPrivacyString(String line) {
        return line.contains("private") ||
                line.contains("protected") ||
                line.contains("public");
    }
    

    /*  ==================================================================  */

    //  ------------------------------------------------------------        //
    //  | Méthodes pour extraire les méthodes :                    |        //
    //  ------------------------------------------------------------        //


    public static String extractMethodeName(List<String> reparsedMethode,
                                            String firstmethodeName) {
        String[] splittedLineCode;
        String lineCode = reparsedMethode.get(0);
        if(lineCode.contains(firstmethodeName) &&
                detectForPrivacyString(lineCode)) {
            splittedLineCode = lineCode.split(" ");
            if (lineCode.contains("()")) {
                return iterateAndSplitArray(splittedLineCode, "()", "\\(\\)");
            } else if(lineCode.contains("(") && lineCode.contains(")")) {
                return buildStringFromListOneLine(splittedLineCode);
            } else {
                return buildStringFromMultipleLines(reparsedMethode, lineCode);
            }
        }
        return null;
    }

    /*  ==================================================================  */

    //  ------------------------------------------------------------        //
    //  | Méthodes pour calculer les lignes et les commentaires :  |        //
    //  ------------------------------------------------------------        //

    /**
     *
     * @param classTextList
     * @param is_CLOC
     * @return
     */
    public static int calculateLinesOrComments(List<String> classTextList, Boolean is_CLOC) {
        int totalCount = 0;
        for (String element : classTextList) {
            List<String> elementLine = new ArrayList<>(Arrays.asList(element.split(" ")));
            elementLine = removeLineSpaces(elementLine);
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

    /**
     *
     * @param elementLine
     * @return
     */
    public static List<String> removeLineSpaces(List<String> elementLine) {
        for (String item : elementLine) {
            if (item.contentEquals("")) {
                elementLine = elementLine.subList(1, elementLine.size());
            }
        }
        return elementLine;
    }

    /*  ==================================================================  */

    //  -----------------------------                                       //
    //  | Méthodes LOC, CLOC, DC :  |                                       //
    //  -----------------------------                                       //

    /**
     *
     * @param classTextList
     * @return
     */

    public static float classe_NCLOC(List<String> classTextList) {
        return calculateLinesOrComments(classTextList,  false);
    }

    /**
     *
     * @param nClocResult
     * @param clocResult
     * @return
     */
    public static float classe_LOC(float nClocResult, float clocResult) {
        return nClocResult + clocResult;
    }

    /**
     *
     * @param classTextList
     * @return
     */
    public static float classe_CLOC(List<String> classTextList) {
        return calculateLinesOrComments(classTextList,  true);
    }

    /**
     *
     * @param commentsDensity
     * @param linesOfCode
     * @return
     */
    public static float classe_DC(float commentsDensity, float linesOfCode) {
        return commentsDensity/linesOfCode;
    }

    /**
     *
     * @param nClocResult
     * @param clocResult
     * @return
     */
    public static float methode_LOC(float nClocResult, float clocResult) {
        return classe_LOC(nClocResult, clocResult);
    }

    /**
     *
     * @param methodeText
     * @return
     */
    public static float methode_CLOC(List<String> methodeText) {
        return classe_CLOC(methodeText);
    }

    /**
     *
     * @param methodeText
     * @return
     */
    public static float methode_NCLOC(List<String> methodeText) {
        return classe_NCLOC(methodeText);
    }

    /**
     *
     * @param commentsDensity
     * @param linesOfcode
     * @return
     */
    public static float methode_DC(float commentsDensity, float linesOfcode) {
        return classe_DC(commentsDensity, linesOfcode);
    }

    /*  ==================================================================  */
}

