
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CodeDetector {

    public static List<List<String>> classeFilesData = new ArrayList<>();
    public static List<List<String>> methodesFilesData = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        getDataFiles("https://github.com/jfree/jfreechart", new ArrayList<String>());
    }

    public static void getDataFiles(String dataURL, ArrayList<String> list) throws IOException {
        Document doc = getData(dataURL);
        List<String> fileCode, classeTreatedFile, methodesTreatedFiles;
        System.out.println(classeFilesData.size());
        if(doc != null) {
            Elements links = doc.select("a[href]");
            String firstLink = "https://github.com/jfree/jfreechart/tree/master";
            String secondLink = "https://github.com/jfree/jfreechart/blob/master";
            for (Element link : links) {
                String chemin = link.attr("abs:href");
                if ((chemin.contains(firstLink) || chemin.contains(secondLink)) && !chemin.contains("#start-of-content")) {
                    if (!list.contains(chemin)) {
                        list.add(chemin);
                        if (chemin.contains(firstLink)) {
                            getDataFiles(chemin, list);
                        } else {
                            fileCode = extractCode(chemin);
                            if(fileCode != null) {
                                classeTreatedFile = treatFile(fileCode, chemin, true);
                                methodesTreatedFiles = treatFile(fileCode, chemin, false);
                                if(classeTreatedFile != null) {
                                    classeFilesData.add(classeTreatedFile);
                                }
                                if(methodesTreatedFiles != null) {
                                    methodesFilesData.add(methodesTreatedFiles);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static Document getData(String dataURL) throws IOException {
        try {
            return Jsoup.connect(dataURL).get();
        } catch (HttpStatusException ignored) {}
        return null;
    }

    public static List<String> extractCode(String dataURL) throws IOException {
        Document doc = getData(dataURL);
        if(doc != null) {
            String docText = doc.toString().replaceAll("<[^>]*>", "");
            docText = docText.replaceAll("(?m)^[ \t]*\r?\n", "");
            return Arrays.asList(docText.split("\n"));
        }
        return null;
    }

    public static List<String> treatFile(List<String> currentCode, String chemin, boolean estClasse) throws IOException {
        Pattern pattern1 = null, pattern2 = null;
        if(estClasse) {
            pattern1 = Pattern.compile("class \\w+ extends*", Pattern.CASE_INSENSITIVE);
            pattern2 = Pattern.compile("Copy lines", Pattern.CASE_INSENSITIVE);
        } else {
            pattern1 = Pattern.compile("\\w+ \\{", Pattern.CASE_INSENSITIVE);
            pattern2 = Pattern.compile("}", Pattern.CASE_INSENSITIVE);
        }
        currentCode = treatCode(currentCode, pattern1, pattern2, estClasse);
        if(currentCode != null) {
            return getClasseData(currentCode, chemin);
        }
        return null;
    }

    public static List<String> treatCode(List<String> currentCode,
                                         Pattern pattern1, Pattern pattern2,
                                         boolean estClasse) {
        int startIndex = 0, endIndex = 0;
        String className = null;
        for (String element : currentCode) {
            Matcher startSentence = pattern1.matcher(element);
            Matcher endSentence = pattern2.matcher(element);
            if (startSentence.find()) {
                startIndex = currentCode.indexOf(element);
                if(estClasse) {
                    getElementName(element, "class");
                } else {
                    getElementName(element, "");
                }
            }
            if(endSentence.find()) { endIndex = currentCode.indexOf(element); }
        }
        if(startIndex != 0 && endIndex != 0) {
            List<String> treatedCode = new ArrayList<>(currentCode.subList(startIndex, endIndex));
            treatedCode.add(className);
            return treatedCode;
        }
        return null;
    }

    public static String getElementName(String element, String elementName) {
        List<String> classNameLine = Arrays.asList(element.split(" "));
        if(element.contains("public") || element.contains("private") ||
                element.contains("protected")) {
            elementName = assignElementName(element);
//            System.out.println("element : " + element + ", elementName : " + elementName);
        }
        int classNameIndex = classNameLine.indexOf(elementName);
        if(elementName.contentEquals("class")) {
            return classNameLine.get(classNameIndex + 1);
        } else {
            if(classNameLine.get(classNameIndex + 1).contentEquals("abstract")) {
                System.out.println("element : " + element + " is abstract class");
            } else {

            }
        }
        return null;
    }

    public static String assignElementName(String element) {
        if(element.contains("public")) {
            return  "public";
        } else if(element.contains("protected")) {
            return  "protected";
        }
        return "private";
    }

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

    public static float classe_LOC(List<String> classTextList) {
        return calculateLinesOrComments(classTextList,  false);
    }

    public static float classe_CLOC(List<String> classTextList) {
        return calculateLinesOrComments(classTextList,  true);
    }

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

    public static List<String> removeLineSpaces(List<String> elementLine) {
        for (String item : elementLine) {
            if (item.contentEquals("")) {
                elementLine = elementLine.subList(1, elementLine.size());
            }
        }
        return elementLine;
    }

    public static int methode_LOC(String classText) {
        return 1;
    }

    public static int methode_CLOC(String classText) {
        return 1;
    }

    public static float classe_DC(float commentsDensity, float codeDensity) {
        return commentsDensity/codeDensity;
    }

    public static int methode_DC(int methode_CLOC, int methode_LOC) {
        return methode_CLOC/methode_LOC;
    }
}

