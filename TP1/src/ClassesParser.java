import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassesParser extends Parser {



    /**
     *
     * @param firstRow
     */
    public ClassesParser(List<String> firstRow) {
        super(firstRow, Arrays.asList("chemin",
                "class", "methode", "methode_CLOC", "methode_LOC", "methode_DC",
                "classe_BC"));
        this.addcomplexityArray(Arrays.asList("WMC", "classe_BC"));
    }

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
                    tempClassesNames.add(className);
                    treatClassesAndMethodes(methodesParser, startIndex, endIndex, className);
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

    void treatClassesAndMethodes(MethodesParser methodesParser, int startParam, int endParam,
                                 String className) {
        int totalPredicats = 0, startIndex, endIndex;
        float totalClasseCloc, totalClasseNCLoc, totalClasseLoc,
                totalClasseDC, totalClassBC;
        List<String> namesAndIndexes = getClassIndexesAndName(startParam, endParam,
                className);
        startIndex = Integer.parseInt(namesAndIndexes.get(0));
        endIndex = Integer.parseInt(namesAndIndexes.get(1));
        totalClasseCloc = classe_CLOC(this.getCodeToParse().subList(startIndex, endIndex));
        totalClasseNCLoc = classe_NCLOC(this.getCodeToParse().subList(startIndex, endIndex));
        totalClasseLoc = classe_LOC(totalClasseNCLoc, totalClasseCloc);
        totalClasseDC = classe_DC(totalClasseCloc, totalClasseLoc);
        totalPredicats = parseMethodes(methodesParser, className, totalClasseDC);
        totalClassBC = classe_BC(totalClasseDC, totalPredicats);
        this.addcomplexityArray(Arrays.asList(String.valueOf(totalPredicats),
                String.valueOf(totalClassBC)));
        assignFilesData(this.getCurrChemin(), namesAndIndexes, totalClasseCloc,
                totalClasseLoc, totalClasseDC, totalClassBC);
    }

    /**
     *
     * @param chemin
     * @param treatedFile
     */
    float assignFilesData(String chemin,
                          List<String> treatedFile,
                          float totalClasseCloc, float totalClasseLoc, float totalClasseDC, float totalClassBC) {
        List<String> resultedList;
        resultedList = Arrays.asList(chemin, treatedFile.get(2),
                String.valueOf(totalClasseLoc), String.valueOf(totalClasseCloc),
                String.valueOf(totalClasseDC), String.valueOf(totalClassBC));
        this.rearrangeData(resultedList);
        return totalClasseDC;
    }

    /**
     *
     * @param methodesParser
     * @return
     */
    int parseMethodes(MethodesParser methodesParser,
                               String currClasse, float totalClasseDC) {
        methodesParser.setCodeToParse(this.getCodeToParse());
        methodesParser.setCurrChemin(this.getCurrChemin());
        return methodesParser.getFileMethodes(this.getCurrChemin(), currClasse,
                totalClasseDC);
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

    /**
     *
     * @param classe_DC
     * @param WMC
     * @return
     */
    float classe_BC(float classe_DC, float WMC) {
        return calculer_BC(classe_DC, WMC);
    }
}
