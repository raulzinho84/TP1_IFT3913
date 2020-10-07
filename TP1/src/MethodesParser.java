import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodesParser extends Parser {

    private static boolean isSwitch;

    /**
     *  Constructeur de la classe MethodesParser.
     * @param firstRow C'est les valeurs de la premiere ligne pour MethodesParser.
     */
    public MethodesParser(List<String> firstRow) {
        super(firstRow, Arrays.asList("chemin",
                "class", "methode", "methode_CLOC", "methode_LOC", "methode_DC",
                "methode_BC"));
        this.addcomplexityArray(Arrays.asList("CC", "methode_BC"));
    }

    /**
     * Une methode qui retourne la valeur retouornee si la phrase contient
     * un switch ou non.
     * @return La valeur de la presence d'un switch dans une phrase ou non.
     */
    public boolean isSwitch() { return isSwitch; }

    /**
     * Setter.
     * @param aSwitch Changer la valeur booleene de isSwitch a une nouvelle.
     */
    public void setSwitch(boolean aSwitch) { isSwitch = aSwitch; }

    /**
     * Methode pour extraire les methodes contenues dans un site web qui
     * represente un fichier java.
     * @param chemin Le raccourci du site web qui contient le code qu'on va
     *               extraire les methodes a partir de lequel.
     * @param className Le nom de la classe qui contient les methodes a extraire.
     * @return Le total de la complexite WMC d'une classe.
     */
    int getFileMethodes(String chemin, String className) {
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
     * Methode pour extraire le nom de la methode cherchee.
     * @param element La methode au complet avec les commentaires.
     * @param lineElements La premiere ligne sous forme des mots dans un tableau.
     * @return Le nom de la methode sous forme de nom(xxx)_1er param(xxx)_ ... .
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
                            if ((prevItem.contentEquals("final") &&
                                    prevPrevItem.contentEquals("static")) ||
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
     * Methoude pour ajouter les commentaires de JAVADOC qui se trouve avant
     * la methode a exploiter.
     * @param lineCodeIndex C'est l'index de la ligne de code courante.
     * @return tous les commentaires JAVADOC trouvees avant une methode.
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
     * Methode pour extraire le corps de la methode a exploiter qui commence
     * avec un "{" et se termine avec "}".
     * @param capturedMethode La liste des lignes du code qui commence a partir
     *                        de la premiere ligne de la methode et se termine
     *                        dans la boucle si on trouve la "}" fermante.
     * @return Une liste contenant les lignes du code d'une methode.
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
     * Methode pour iterer a travers les lignes du code pour trouver les "(" ou ")" ou "()".
     * @param splittedLineCode Le code a exploiter.
     * @param comparator Dans ce cas ca sera seulement "()".
     * @param comparatorRegex Ca sera ("//(//)") qui represente la representation
     *                        REGEX pour les "(" ouvrante et ")" fermante.
     * @return le nom de la methode extrait sans "(", ")" ou "()".
     */
    String iterateAndSplitArray(String[] splittedLineCode,
                                              String comparator,
                                              String comparatorRegex) {
        int i = 0;
        while(!splittedLineCode[i].contains(comparator)) { i++; }
        return splittedLineCode[i].split(comparatorRegex)[0];
    }

    /**
     * Methode pour creer le nom de la methode si tous les params sont sur la
     * meme ligne.
     * @param splittedLineCode Liste des mots qui se trouvent dans une phrase.
     * @return Un String sous forme xxx_xxx_... .
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
                        extractedMethode.append("_").append(removeFirstChar(splitElements));
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
     * Methode qui va iterer a travers les lignes pour extraire tous les params
     * contenus dans une methode.
     * @param reparsedMethode Le code de la methode a traiter.
     * @param lineCode La ligne du code actuelle.
     * @return Un String sous forme xxx_xxx_... .
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
     * Methodes pour enlever toutes les espaces blancs.
     * @param element La ligne du code qui represente la phrase a traiter.
     * @return Une liste contenant les mots de la phrase.
     */
    String[] getLineElements(String element) {
        element = element.replaceAll("\\s+", "+");
        String[] lineElements = element.split("\\+");
        return Arrays.copyOfRange(lineElements, 1, lineElements.length);
    }

    /**
     * Methode qui ajoute les donnees obtenues par les calculs dans les
     * listes(attributs) de la classe.
     * @param reparsedMethode La liste contenant les lignes du code a evaluer.
     * @param chemin Le lien de la page web courante.
     * @param currClasse La classe courante qui contient la methode extraite.
     * @param methodeName Le nom de la methode extraite.
     * @param numPredicats Le numero total des predicats calculees.
     * @return Methode_BC pour la ajouter dans la liste(DC,CC).
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
     * Methode pour extraire le nom de la methode soit sur une ou plusieurs lignes.
     * @param reparsedMethode La liste des lignes du code a evaluer.
     * @param firstmethodeName Le nom de la premiere methode pour la ajouter au
     *                         String dont le format est xxx_xxx_... .
     * @return Le nom de la methode extraite et nulle si aucune methode est trouvee.
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
     * Methode pour calculer le nombre total des predicats soit if, else if,
     * while ou switch. Cette methode va s'assurer que les lignes ne sont pas
     * des commentaires et ce sont des lignes du code.
     * @param reparsedMethode La liste des lignes du code a evaluer.
     * @return Le numero total des predicats.
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
        return totalPredicats;
    }

    /**
     * Methode qui calculer les if, else if, while et switch.
     * @param lineCode La ligne du code a evaluer.
     * @return  Le numero total des predicats et 0 s'il n'existe pas des
     *          branchements dans le code.
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
     * Methode pour calculer la complexite d'une methode.
     * @param methode_DC DC
     * @param CC CC
     * @return DC/CC
     */
    float methode_BC(float methode_DC, float CC) {
        return calculer_BC(methode_DC, CC);
    }
}
