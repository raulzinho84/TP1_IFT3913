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
     *  Setter.
     * @param parsedLists La liste qu'on veut la remplacer par la liste originale.
     */
    private void setParsedList(List<List<String>> parsedLists) {
        this.parsedLists = parsedLists;
    }

    /**
     *  Getter.
     * @return
     */
    public List<List<String>> getTotalParsedLists() {
        return totalParsedLists;
    }

    /**
     * Setter.
     * @param totalParsedLists
     */
    public void setTotalParsedLists(List<List<String>> totalParsedLists) {
        this.totalParsedLists = totalParsedLists;
    }

    /**
     * Getter.
     * @return
     */
    public List<String> getCodeToParse() {
        return codeToParse;
    }

    /**
     * Setter.
     * @param codeToParse
     */
    void setCodeToParse(List<String> codeToParse) {
        this.codeToParse = codeToParse;
    }

    /**
     * Getter.
     * @return  le chemin qui contient la page web courante.
     */
    public String getCurrChemin() {
        return currChemin;
    }

    /**
     * Setter.
     * @param currChemin Prend un nouveau site web et le remplace par l'ancien.
     */
    void setCurrChemin(String currChemin) {
        this.currChemin = currChemin;
    }

    /**
     * Ajoute un element a un index x dans la liste parsedLists.
     * @param index L'index ou la liste element sera ajoutee.
     * @param element la liste qui sera ajoute a l'index x.
     */
    void addFirstParserList(int index, List<String> element) {
        this.parsedLists.add(index, element);
    }

    /**
     * Ajoute un element a un index x dans la liste totalParsedLists.
     * @param index L'index ou la liste element sera ajoutee.
     * @param element la liste qui sera ajoute a l'index x.
     */
    void addSecondParserList(int index, List<String> element) {
        this.totalParsedLists.add(index, element);
    }

    /**
     * Getter.
     * @return La liste contenant les complexites pour les classes et les methodes.
     */
    public List<List<String>> getcomplexityFile() { return complexityFile; }

    /**
     * Setter
     * @param complexityFile La nouvelle liste qui va remplacer l'ancienne.
     */
    public void setcomplexityFile(List<List<String>> complexityFile) {
        this.complexityFile = complexityFile;
    }

    /**
     * Ajouter un element a la fin de la liste de complexite.
     * @param arr La liste qui sera ajoutee dans la liste de liste de complexite.
     */
    public void addcomplexityArray(List<String> arr) {
        this.complexityFile.add(arr);
    }

    /**
     * Methode utilisee pour enlever tous les elements de l'ojet parsedLists.
     */
    void clearParsedList() { this.parsedLists.clear(); }

    // Il existe deux methodes treatCode.

    /**
     * C'est la methode utilisee dans la classe ClasseParser.
     * @param methodesParser Utilise pour appeler MethodesParser a partir de
     *                       ClassesParser.
     * @return Cette methode sera surchargee dans la classe ClassesParser.
     */
    void treatCode(MethodesParser methodesParser) {}

    /**
     * C'est une methode pour detectdetecter tout type de commentaires possible.
     * @param element La phrase qu'on cherche a savoir s'il existe des
     *                commentaires ou non.
     * @return Un booleen qui indique si la liste contient des commentaires ou pas.
     */
    boolean detectForNoComments(String element) {
        return !element.contains("//") && !element.contains("/**") &&
                !element.contains("/*") && !element.contains("*") &&
                !element.contains("*/");
    }

    /**
     * C'est la methode responsable a detecter si la phrase contient une classe
     * ou non.
     * @param element La phrase dont on a besoin a detecter.
     * @return Un booleen qui indique que element contient une classe ou non.
     */
    boolean detectForClass(String element) {
        if(!element.contains("(")) {
            return element.contains("class") || element.contains("abstract class") ||
                    element.contains("enum") || element.contains("interface");
        }
        return false;
    }

    boolean detectForMethode(String element) {
        String newElem = element.toLowerCase();
        return newElem.contentEquals("short") || newElem.contentEquals("int") ||
                newElem.contentEquals("float") || newElem.contentEquals("long") ||
                newElem.contentEquals("char") || newElem.contentEquals("boolean") ||
                newElem.contentEquals("double") || newElem.contentEquals("void") ||
                newElem.contains("arraylist") || newElem.contentEquals("list") ||
                newElem.contentEquals("<") || newElem.contentEquals("[");
    }

    /**
     * Cette methode va detecter si le premier mot est (privee, publique ou protegee).
     * @param lineElements La phrase separee en tableau dont le premier element
     *                     sera compare.
     * @return Un booleen pour indiquer que le premier mot est comme mentionne.
     */
    boolean detectForPrivacy(List<String> lineElements) {
        return lineElements.get(0).contentEquals("private") ||
                lineElements.get(0).contentEquals("protected") ||
                lineElements.get(0).contentEquals("public");
    }

    /**
     * Prend un string au lieu d'un tableau.
     * @param line Un string contenant le mot a detecter.
     * @return Un booleen pour indiquer que le premier mot est comme mentionne.
     */
    boolean detectForPrivacy(String line) {
        return line.contains("private") || line.contains("protected") ||
                line.contains("public");
    }

    /**
     * Une methode pour separer les mots qui se trouvent aux extremites des
     * parentheses.
     * @param element La phrase contenant les mots a separer.
     * @return  un seul element si la il n'existe pas des arguments dans la meme
     *          ligne que la methode ou xxx_xxx qui represente le nom de la methode
     *          et le 1er parametre apres la parenthese ouvrante.
     */
    String splitParenthesis(String element) {
        String[] splitElements = element.split("\\(");
        if(splitElements.length > 1) { return splitElements[0] + "_" + splitElements[1]; }
        return splitElements[0];
    }

    /**
     * Methode pour enlever les virgules au debut d'une phrase.
     * @param splitElements Un tableau contenant les mots de la phrase a evaluer.
     * @return Une nouvelle phrase sans virgule en forme de String.
     */
    String removeFirstChar(String[] splitElements) {
        String[] tempList = Arrays.copyOfRange(splitElements, 1, splitElements.length);
        return String.join("", tempList);
    }

    /*  ===========================================================================        */

    //  ---------------------------------------------------------------------------        //
    //  | Méthodes pour calculer les lignes, les commentaires et les predicats :  |        //
    //  ---------------------------------------------------------------------------        //

    /**
     * Methode qui va calculer les lignes sans ou avec commentaires en passant
     * le booleen is_CLOC.
     * @param classTextList C'est la classe qu'on veut la traiter.
     * @param is_CLOC C'est vrai si c'est CLOC(commented) et faux sinon.
     * @return Le nombre des lignes calculees.
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
            } else { if(isNotComment(elementLine)) { totalCount++; } }
        }
        return totalCount;
    }

    /**
     * Une methode pour utilisent les espaces pour separer les mots sous forme
     * d'une liste des mots.
     * @param element La phrase a traiter.
     * @return  Un tableau ou une liste contenant les mots qui se trouvent dans
     *          une phrase.
     */
    List<String> separateAndRemoveSpaces(String element) {
        List<String> elementLine = new ArrayList<>(Arrays.asList(element.split(" ")));
        return removeLineSpaces(elementLine);
    }

    /**
     * Une methode pour enlever les espaces au debut de la phrase("\t").
     * @param elementLine La phrase qu'on va traiter sous forme d'un tableau.
     * @return La phrase sans les espaces au debut.
     */
    List<String> removeLineSpaces(List<String> elementLine) {
        for (String item : elementLine) {
            if (item.contentEquals("")) {
                elementLine = elementLine.subList(1, elementLine.size());
            }
        }
        return elementLine;
    }

    boolean isNotComment(List<String> elementLine) {
       return  !elementLine.get(0).contains("/*") &&
                !elementLine.get(0).contains("*") &&
                !elementLine.get(0).contains("//");
    }

    /**
     * Methode pour calculer les non-commentaires lignes.
     * @param classTextList La classe ou la methode dont on veut calculer
     *                      ses lignes.
     * @return le numero des lignes cherchees.
     */

    float calculer_NCLOC(List<String> classTextList) {
        return calculateLinesOrComments(classTextList,  false);
    }

    /**
     * Methode pour calculer le numero total des lignes.
     * @param nClocResult # des lignes non-commentaires.
     * @param clocResult # des lignes avec commentaires.
     * @return cloc + ncloc.
     */
    float calculer_LOC(float nClocResult, float clocResult) {
        return nClocResult + clocResult;
    }

    /**
     * Methode pour calculer les lignes avec commentaires.
     * @param classTextList La classe ou la methode dont on veut calculer
     *                      ses lignes.
     * @return le numero des lignes cherchees.
     */
    float calculer_CLOC(List<String> classTextList) {
        return calculateLinesOrComments(classTextList,  true);
    }

    /**
     * Methode pour calculer la densite des commentaires dans une classe
     * ou une methode.
     * @param commentsDensity CLOC.
     * @param linesOfCode LOC.
     * @return CLOC/LOC.
     */
    float calculer_DC(float commentsDensity, float linesOfCode) {
        return commentsDensity/linesOfCode;
    }

    /**
     * Methode pour calculer la complexite d'une classe ou une methode.
     * @param densityComments DC(classe ou methode).
     * @param objectComplexity CC(methode) ou WMC(classe).
     * @return DC/CC ou DC/WMC
     */
    float calculer_BC(float densityComments, float objectComplexity) {
        return densityComments/objectComplexity;
    }

    /**
     * Methode pour extraire les 3 classes ou methodes les moins commentes dans projet.
     * @param currFileData La liste des elements qu'on veut ajouter a la liste des listes
     *                     des elements qui representent les donnees des classes et des
     *                     methodes extraites.
     */
    void rearrangeData(List<String> currFileData) {
        List<String> tempFileData;
        int bcIndex, locIndex;
        float currBCRatio;
        if(currFileData.size() == 6) {
            bcIndex = 5;
            locIndex = 2;
            tempFileData = Arrays.asList(currFileData.get(0),
                    currFileData.get(1), currFileData.get(2), currFileData.get(3),
                    currFileData.get(4));
        } else {
            bcIndex = 6;
            locIndex = 3;
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
     * Methode pour iterer sur la liste des listes pour trouver la position
     * actuelle de la nouvelle liste par rapport aux anciens selon le
     * classement de BC et la longueur d'un code si BC est en egalite.
     * @param currFileData Liste des elements(ligne des donnees) a ajouter.
     * @param tempFileData La liste contenant les donnees(BC, CC ou WMC).
     * @param currBCRatio La valeur du BC de la liste courante(a comparer avec
     *                   la liste qu'on cherche a ajouter).
     * @param bcIndex L'index de la colonne qui contient les valeurs du BC.
     * @param locIndex L'index de la colonne qui contient les LOCs.
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
     * La methode responsable a ajouter la liste passe en parametre dans la
     * liste des listes des donnees obtenues par notre recherche.
     * @param currIndex L'index qu'on va ajouter la liste dans lequel.
     * @param currFileData Les donnees a ajouter dans parsedLists.
     * @param tempFileData LEs donnees a ajouter dans totalParsedLists.
     */
    void addToParserList(int currIndex, List<String> currFileData,
                         List<String> tempFileData) {
        this.addFirstParserList(currIndex, tempFileData);
        this.addSecondParserList(currIndex, currFileData);
    }
}
