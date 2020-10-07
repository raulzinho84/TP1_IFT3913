import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassesParser extends Parser {



    /**
     *  Constructeur de la classe MethodesParser.
     * @param firstRow C'est les valeurs de la premiere ligne pour MethodesParser.
     */
    public ClassesParser(List<String> firstRow) {
        super(firstRow, Arrays.asList("chemin",
                "class", "methode", "methode_CLOC", "methode_LOC", "methode_DC",
                "classe_BC"));
        this.addcomplexityArray(Arrays.asList("WMC", "classe_BC"));
    }

    /**
     * Methode responsable a traiter le code pour extraire les classes a partir
     * de start et end index. Cette methode va extraire les methodes qui se
     * trouvent dans la classe trouvee a partir de la methode
     * treatClassesAndMethodes.
     */
    @Override
    void treatCode(MethodesParser methodesParser) {
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
    }

    /**
     * Methode pour extraire la ligne du code et la transformer en liste des mots.
     * @param element La ligne du code a evaluer.
     * @return Une liste des mots qui represente la ligne element sans espaces
     * blancs.
     */
    String[] getLineElements(String element) {
        element = element.replaceAll("\\s+", "+");
        String[] lineElements = element.split("\\+");
        return Arrays.copyOfRange(lineElements, 1, lineElements.length);
    }

    /**
     * Methode pour extraire le nom de la classe.
     * @param element La ligne du code.
     * @param lineElements La liste de mots qui represente cette ligne du code.
     * @return Le nom de la classe trouvee sinon elle retourne nulle.
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
     * Methode pour extraire la premiere ligne du code qui contient le nom de
     * la classe.
     * @param lineCodeIndex L'index de la ligne du code courante.
     * @return L'index de la premiere ligne du code de la classe.
     */
    int getStartIndex(int lineCodeIndex) {
        int prevLineIndex = lineCodeIndex - 1;
        while(!this.getCodeToParse().get(prevLineIndex).contains("/**") && prevLineIndex > 0) {
            prevLineIndex--;
        }
        return prevLineIndex;
    }

    /**
     * Methode qui retourne le nom, l'ndex du debut et de la fin sous forme d'une
     * liste des String.
     * @param startIndex L'index du debut de la classe.
     * @param endIndex L'index de la fin de la classe.
     * @param className Le nom de la classe trouvee.
     * @return La liste des elements(index debut, fin , nom de la classe).
     */
    List<String> getClassIndexesAndName(int startIndex,
    int endIndex,
    String className) {
        return new ArrayList<>(Arrays.asList(String.valueOf(startIndex),
                String.valueOf(endIndex), className));
    }

    /**
     * Methode pour extraire les elements d'une classe dans une liste qui sera
     * ajoutee a la liste de liste qui sera imprimer dans un fichier .csv
     * plus tard.
     * @param methodesParser UNe instance de MethodesParser pour avoir acces
     *                       aux methodes de cette classe.
     * @param startParam Start index.
     * @param endParam End index.
     * @param className Nom de la classe.
     */
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
        totalPredicats = parseMethodes(methodesParser, className);
        totalClassBC = classe_BC(totalClasseDC, totalPredicats);
        this.addcomplexityArray(Arrays.asList(String.valueOf(totalPredicats),
                String.valueOf(totalClassBC)));
        assignFilesData(this.getCurrChemin(), namesAndIndexes, totalClasseCloc,
                totalClasseLoc, totalClasseDC, totalClassBC);
    }

    /**
     * Methode pour assigner les elements de la liste dans les listes des attributs
     * apres qu'on utilise la methode rearrange pour emplacer le liste obtenue dans
     * sa place destinee.
     * @param chemin Le lien de la page du code a evaluer.
     * @param treatedFile La liste des elements extraits dans treatClassesAndMethodes.
     * @param totalClasseCloc CLOC de la classe.
     * @param totalClasseLoc LOC de la classe.
     * @param totalClasseDC DC de la classe.
     * @param totalClassBC BC de la classe.
     * @return DC qui sera utilise pour la liste DC,WMC.
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
     * Une methode pour extraire les methodes qui se trouvent dans la classe extraite.
     * @param methodesParser Une instance de MethodeParser pour acceder aux methodes
     *                      d'objet MethodesParser.
     * @param currClasse Le nom de la classe courante qui sera utilise dans la liste
     *                   des elements a imprimer pour les methodes.
     * @return Total predicats afin de calculer la complexite de la classe extraite.
     */
    int parseMethodes(MethodesParser methodesParser,
                               String currClasse) {
        methodesParser.setCodeToParse(this.getCodeToParse());
        methodesParser.setCurrChemin(this.getCurrChemin());
        return methodesParser.getFileMethodes(this.getCurrChemin(), currClasse);
    }

    /**
     * Methode pour calculer les non-commentaires lignes.
     * @param classTextList La classe ou la methode dont on veut calculer
     *                      ses lignes.
     * @return le numero des lignes cherchees.
     */

    float classe_NCLOC(List<String> classTextList) {
        return calculer_NCLOC(classTextList);
    }

    /**
     * Methode pour calculer le numero total des lignes.
     * @param nClocResult # des lignes non-commentaires.
     * @param clocResult # des lignes avec commentaires.
     * @return cloc + ncloc.
     */
    float classe_LOC(float nClocResult, float clocResult) {
        return calculer_LOC(nClocResult, clocResult);
    }

    /**
     * Methode pour calculer les lignes avec commentaires.
     * @param classTextList La classe ou la methode dont on veut calculer
     *                      ses lignes.
     * @return le numero des lignes cherchees.
     */
    float classe_CLOC(List<String> classTextList) {
        return calculer_CLOC(classTextList);
    }

    /**
     * Methode pour calculer la densite des commentaires dans une classe
     * ou une methode.
     * @param commentsDensity CLOC.
     * @param linesOfCode LOC.
     * @return CLOC/LOC.
     */
    float classe_DC(float commentsDensity, float linesOfCode) {
        return calculer_DC(commentsDensity, linesOfCode);
    }

    /**
     * Methode pour calculer la complexite d'une classe
     * @param classe_DC DC(classe).
     * @param WMC WMC(classe).
     * @return DC/WMC
     */
    float classe_BC(float classe_DC, float WMC) {
        return calculer_BC(classe_DC, WMC);
    }
}
