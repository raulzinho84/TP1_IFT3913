
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CodeAnalyzer {

    public static List<List<String>> classeFilesData =
            new ArrayList<>();
    public static List<List<String>> methodesFilesData = new ArrayList<>();
    public static List<String> classeNames = new ArrayList<>();
    public static List<String> methodeNames = new ArrayList<>();
    public static List<String> visitedLinks = new ArrayList<>();
    public static String lastLineItem = null, currClasse = null, currMethode = null;


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String firstLink = "https://github.com/jfree/jfreechart/tree/master";
        String secondLink = "https://github.com/jfree/jfreechart/blob/master";
        classeFilesData.add(Arrays.asList("chemin", "class", "classe_LOC",
                "classe_CLOC", "classe_DC"));
        methodesFilesData.add(Arrays.asList("chemin", "class", "methode", "methode_CLOC",
                "methode_LOC", "methode_DC"));
        getDataFiles("https://github.com/jfree/jfreechart", firstLink, secondLink);
    }

    /**
     * @param dataURL    C'est l'extension du siteWeb a traiter.
     * @param firstLink  C'est la premiere extension contenant /tree.
     * @param secondLink C'est la deuxieme extension contenant /blob.
     * @throws IOException
     */
    public static void getDataFiles(String dataURL, String firstLink, String secondLink) throws IOException {
        Document doc = getData(dataURL);
//        System.out.println(classeNames.size());
//        System.out.println(methodeNames.size());
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
        int startIndex, endIndex;
        float totalClasseCloc, totalClasseLoc;
        String updatedMethode;
        List<String> fileCode = extractCode(chemin);
        if (fileCode != null) {
            List<String> classeTreatedFile = treatClass(fileCode, chemin, true);
            List<String> methodesTreatedFiles = treatClass(fileCode, chemin, false);
            if (classeTreatedFile != null) {
                startIndex = Integer.parseInt(classeTreatedFile.get(0));
                endIndex = Integer.parseInt(classeTreatedFile.get(1));
                totalClasseCloc = classe_CLOC(fileCode.subList(startIndex, endIndex));
                totalClasseLoc = classe_LOC(fileCode.subList(startIndex, endIndex));
                classeFilesData.add(Arrays.asList(chemin, classeTreatedFile.get(2),
                        String.valueOf(totalClasseCloc), String.valueOf(totalClasseLoc),
                        String.valueOf(classe_DC(totalClasseCloc, totalClasseLoc))));
//                System.out.println(classeFilesData);
                if(methodesTreatedFiles != null) {
                    updatedMethode = methodesTreatedFiles.get(0);
                    if(updatedMethode.contains("(")) {
                        if(!updatedMethode.contains(")")) {
                            String[] temp = updatedMethode.split("\\(");
                            updatedMethode = temp[0] + "_" + temp[1];
                        }
                    }
                    methodesFilesData.add(Arrays.asList(chemin, classeTreatedFile.get(2), updatedMethode));
//                    System.out.println(methodesFilesData);
                }
            }
        }
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

    public static List<String> treatClass(List<String> currentCode,
                                                 String chemin,
                                                 boolean isClass) throws IOException {
        String className = null, tempClassName, tempMethodName, methodName;
        int startIndex = 0, endIndex = 0, currIndex = 0;
        List<String> methodesIndexes = new ArrayList<>();
        List<String> listClassIndexes;
        for (String element : currentCode) {
            if(isClass) {
                tempClassName = getClassName(element, getLineElements(element));
                if(element.contains("Copy lines")) { endIndex = currIndex; }
                if (tempClassName != null) {
                    className = tempClassName;
                    classeNames.add(className);
                    startIndex = currIndex;
                }
                if(startIndex != 0 && endIndex != 0) {
                    String[] classIndexes =
                            new String[]{String.valueOf(startIndex),
                                    String.valueOf(endIndex), className};
                    return new ArrayList<>(Arrays.asList(classIndexes));
                }
            } else {
                tempMethodName = getMethodName(element, getLineElements(element));
                if (!tempMethodName.contentEquals(" ")) {
                    methodName = tempMethodName;
                    methodeNames.add(methodName);
                    return new ArrayList<>(Collections.singletonList(methodName));
                }
            }
            currIndex++;
        }
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
                } else {
                    className = lineElements[3];
                }
            } else {
                className = lineElements[1];
            }
        }
        return className;
    }

    public static String getMethodName(String element, String[] lineElements) {
        String methodName, prevItem = null, prevPrevItem = null;
        StringBuilder tempString = new StringBuilder();
        int itemIndex = 0;
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
                        String[] temp = item.split("\\(");
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
        if(tempString.length() > 0) { return tempString.toString(); }
        return " ";
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

    public static String[] getLineElements(String element) {
        element = element.replaceAll("\\s+", "+");
        String[] lineElements = element.split("\\+");
        return Arrays.copyOfRange(lineElements, 1, lineElements.length);
    }
//    /**
//     *
//     * @param currentCode
//     * @param chemin
//     * @param estClasse
//     * @return
//     * @throws IOException
//     */
//    public static List<String> treatFile(List<String> currentCode, String chemin,
//                                         boolean estClasse) throws IOException {
//        Pattern pattern1 = null, pattern2 = null;
////        if(estClasse) {
//            pattern1 = Pattern.compile("class", Pattern.CASE_INSENSITIVE);
//            pattern2 = Pattern.compile("Copy lines", Pattern.CASE_INSENSITIVE);
////        } else {
////            pattern1 = Pattern.compile("\\w+ \\{", Pattern.CASE_INSENSITIVE);
////            pattern2 = Pattern.compile("}", Pattern.CASE_INSENSITIVE);
////        }
//        currentCode = treatCode(currentCode, pattern1, pattern2, estClasse);
//        if(currentCode != null) {
//            return getClasseData(currentCode, chemin);
//        }
//        return null;
//    }

//    /**
//     *
//     * @param currentCode
//     * @param pattern1
//     * @param pattern2
//     * @param estClasse
//     * @return
//     */
//    public static List<String> treatCode(List<String> currentCode,
//                                         Pattern pattern1, Pattern pattern2,
//                                         boolean estClasse) {
//        int startIndex = 0, endIndex = 0;
//        String className = null, methodeName = null;
//        for (String element : currentCode) {
//            Matcher startSentence = pattern1.matcher(element);
//            Matcher endSentence = pattern2.matcher(element);
////            System.out.println(startSentence);
//            if (startSentence.find()) {
//                startIndex = currentCode.indexOf(element);
//                if(estClasse) {
//                    className = getElementName(element, "class");
//                } else {
//                    methodeName = getElementName(element, "");
//                }
//            }
//            if(endSentence.find()) { endIndex = currentCode.indexOf(element); }
//        }
//        if(startIndex != 0 && endIndex != 0) {
//            List<String> treatedCode = new ArrayList<>(currentCode.subList(startIndex, endIndex));
//            treatedCode.add(className);
//            return treatedCode;
//        }
//        return null;
//    }

//    /**
//     *
//     * @param element
//     * @param elementName
//     * @return
//     */
//    public static String getElementName(String element, String elementName) {
//        List<String> elementNameLine = Arrays.asList(element.split(" "));
//        elementNameLine = removeLineSpaces(elementNameLine);
//        int elementNameIndex = elementNameLine.indexOf(elementName);
//        if(elementNameLine.contains("class")) {
//            return elementNameLine.get(elementNameIndex + 1);
//        } else {
//            if(element.contains("public") || element.contains("private") ||
//                    element.contains("protected")) {
//                return buildMethodeName(elementNameLine, elementNameIndex);
//            }
//        }
//        return null;
//    }

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
        float codeDensity = classe_LOC(currentCode);
        treatedFile.add(String.valueOf(codeDensity));
        float classDensity = classe_DC(commentsDensity, codeDensity);
        treatedFile.add(String.valueOf(classDensity));
        return treatedFile;
    }


    /*  ==================================================================  */

    //  ------------------------------------------------------------        //
    //  | Méthodes pour extraire les méthodes :                    |        //
    //  ------------------------------------------------------------        //

    /**
     *
     * @param element
     * @return
     */
    public static String assignElementName(String element) {
        if(element.contains("public")) {
            return  "public";
        } else if(element.contains("protected")) {
            return  "protected";
        }
        return "private";
    }

//    /**
//     *
//     * @param elementNameLine
//     * @param elementNameIndex
//     * @return
//     */
//    public static String buildMethodeName(List<String> elementNameLine, int elementNameIndex) {
//        if(elementNameLine.get(0).contains("public") ||
//                elementNameLine.get(0).contains("private") ||
//                elementNameLine.get(0).contains("protected")) {
//            if(!elementNameLine.contains("abstract") &&
//                    !elementNameLine.contains("interface") &&
//                    !elementNameLine.contains("enum") &&
//                    !elementNameLine.contains("class") &&
//                    !elementNameLine.contains("extends") &&
//                    !elementNameLine.contains("implements")) {
//                StringBuilder methodeName = splitMethode(elementNameLine, elementNameIndex);
//                return methodeName.toString();
//            }
//        }
//        return null;
//    }

//    /**
//     *
//     * @param elementNameLine
//     * @return
//     */
//    public static StringBuilder splitMethode(List<String> elementNameLine) {
//        StringBuilder methodeName = new StringBuilder();
//        elementNameLine = elementNameLine.subList(elementNameIndex + 2, elementNameLine.size());
//        String prev = elementNameLine.get(0);
//        for(String element : elementNameLine) {
//            if(element.contains("(")) {
//                List<String[]> tempVal = new ArrayList<>();
//                tempVal.add(element.split("\\("));
//                if (!tempVal.get(0)[1].contentEquals(")")) {
//                    methodeName.append(tempVal.get(0)[0]).append("_").append(tempVal.get(0)[1]);
//                } else {
//                    methodeName.append(tempVal.get(0)[0]);
//                }
//            } else {
//                if(prev.contentEquals(",")) {
//                    methodeName.append("_").append(element);
//                }
//            }
//            prev = element;
//        }
//        return methodeName;
//    }

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

    public static float classe_LOC(List<String> classTextList) {
        return calculateLinesOrComments(classTextList,  false);
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
     * @param codeDensity
     * @return
     */
    public static float classe_DC(float commentsDensity, float codeDensity) {
        return commentsDensity/codeDensity;
    }

    /**
     *
     * @param classText
     * @return
     */
    public static int methode_LOC(String classText) {
        return 1;
    }

    /**
     *
     * @param classText
     * @return
     */
    public static int methode_CLOC(String classText) {
        return 1;
    }

    /**
     *
     * @param methode_CLOC
     * @param methode_LOC
     * @return
     */
    public static int methode_DC(int methode_CLOC, int methode_LOC) {
        return methode_CLOC/methode_LOC;
    }

    /*  ==================================================================  */
}

