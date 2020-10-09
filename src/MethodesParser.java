import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodesParser extends Parser {

    private static boolean isSwitch;
    private static String currentMethode;
    private static final List<String> methodesNames = new ArrayList<>();

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
     * Une methode qui retourne la valeur retourne si la phrase contient
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
    int getFileMethodes(String chemin, String className, int startIndex, int endIndex) {
        String firstMethodeName = null, secondMethodeName, fullMethodeName;
        int lineCodeIndex = startIndex, methodesIndex = 0, totalPredicats = 0, newMethodeIndex = 0;
        List<String> capturedMethode = new ArrayList<>(),
                nonCommentsMethode = new ArrayList<>(), reparsedMethode;
        boolean methodeFound = false, first = true;
        for(String lineCode : this.getCodeToParse().subList(startIndex,endIndex)) {
            if(!methodeFound) {
                if(first) {
                    firstMethodeName = getMethodName(lineCode,
                            getLineElements(lineCode));
                    if(!firstMethodeName.equals(" ")) {
                        first = false;
                        methodesNames.add(currentMethode);
                    }

                }
                if(lineCodeIndex == endIndex - 2 && currentMethode != null) {
                    List<String> nonComments =
                            this.getCodeToParse().subList(newMethodeIndex, endIndex);
                    List<String> comments = addJavaDOC(newMethodeIndex);
                    comments.addAll(nonComments);
                    fullMethodeName = buildMethodeName(comments, currentMethode);
                    totalPredicats = getMethodeData(chemin, className,
                            comments, nonComments, fullMethodeName);
                    break;
                }

                if(!firstMethodeName.equals(" ")) {
                    capturedMethode = addJavaDOC(lineCodeIndex);
                    capturedMethode.add(this.getCodeToParse().get(lineCodeIndex - 1));
                    capturedMethode.add(lineCode);
                    nonCommentsMethode.add(lineCode);
                    methodeFound = true;
                }
            } else {
                secondMethodeName = getMethodName(lineCode,
                        getLineElements(lineCode));
                if(!secondMethodeName.equals(" ")) {
                    newMethodeIndex = lineCodeIndex;
                    methodeFound = false;
                    reparsedMethode = reparseMethode(capturedMethode);
                    methodesNames.add(currentMethode);
                    fullMethodeName = buildMethodeName(reparsedMethode,
                            methodesNames.get(methodesNames.size() - 2));
                    if(fullMethodeName != null) {
                        totalPredicats = getMethodeData(chemin, className,
                                capturedMethode, nonCommentsMethode,
                                fullMethodeName);
                    }
                    methodesIndex++;
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
        methodesNames.clear();
        currentMethode = null;
        return totalPredicats + 1;
    }

    /**
     * La methode qui traite tous les donnees et les ajoutent a une liste dans
     * la liste de liste qui contient les methodes traitees et retourne le
     * nombre total des predicats obtenus.
     * @param chemin Le lien contenant le fichier traite.
     * @param className Le nom de la classe.
     * @param capturedMethode Le code en total avec les commentaires.
     * @param nonCommentsMethode Le code de la methode sans commentaires.
     * @param firstMethodeName Le nom de la methode qui sera inclue dans le fichier .csv.
     * @return Le nombre total des predicats dans le code.
     */
    int getMethodeData(String chemin, String className,List<String> capturedMethode,
                        List<String> nonCommentsMethode, String firstMethodeName) {
        List<String> reparsedMethode;
        int numPredicats, totalPredicats = 0;
        reparsedMethode = reparseMethode(capturedMethode);
        if(firstMethodeName != null) {
            numPredicats = calculerPredicats(nonCommentsMethode);
            totalPredicats += numPredicats;
            float methode_BC = addMethodeData(reparsedMethode, chemin,
                    className, firstMethodeName, numPredicats + 1);
            this.addcomplexityArray(Arrays.asList(String.valueOf(numPredicats + 1),
                    String.valueOf(methode_BC)));
        }
        return totalPredicats;
    }

    /**
     * Methode pour extraire le nom de la methode cherchee.
     * @param element La methode au complet avec les commentaires.
     * @param lineElements La premiere ligne sous forme des mots dans un tableau.
     * @return Le nom de la methode sous forme de nom(xxx)_1er param(xxx)_ ... .
     */
    String getMethodName(String element, List<String> lineElements) {
        String prevItem = null, prevPrevItem = null, item;
        StringBuilder tempString = new StringBuilder(), emptyString = new StringBuilder("");
        int itemIndex = 0;
        if(lineElements.size() > 0) {
            if (this.detectForNoComments(lineElements.get(0)) && !element.contains("=") &&
                    element.contains("{")) {
                if (this.detectForPrivacy(lineElements) ||
                        (detectForMethode(lineElements.get(0)) && element.contains("("))) {
                    while (itemIndex < lineElements.size()) {
                        item = lineElements.get(itemIndex);
                        if (itemIndex >= 2) {
                            prevItem = lineElements.get(itemIndex - 1);
                            prevPrevItem = lineElements.get(itemIndex - 2);
                        }
                        if ((item.contains("(") || item.contains(","))) {
                            if(item.contains("()")) {
                                tempString.append(item.split("\\(\\)")[0]);
                                currentMethode = item.split("\\(\\)")[0];
                                break;
                            }
                            else {
                                if (item.contains("(")) {
                                    List<String> temp = Arrays.asList(item.split("\\("));
                                    currentMethode = temp.get(0);
                                    tempString = emptyString;
                                    if (!temp.get(1).contains(")")) {    // methode()
                                        tempString.append(temp.get(0)).append("_").append(temp.get(1));
                                        if (temp.get(1).contentEquals("final")) {
                                            itemIndex++;
                                            tempString.append("_").append(lineElements.get(itemIndex));
                                            if (!lineElements.get(itemIndex + 1).contains(",")) {
                                                itemIndex++;
                                            }
                                        }
                                    }
                                } else {
                                    if (item.contains(",") && itemIndex != lineElements.size() - 1) {
                                        if (!item.split("")[0].contentEquals(",")) {
                                            itemIndex++;
                                        }
                                        tempString.append("_").append(lineElements.get(itemIndex));
                                    }
                                }
                            }
                        } else {
                            if (itemIndex >= 2) {
                                if (lineElements.size() - 1 >= itemIndex + 1) {
                                    if ((prevItem.contentEquals("final") &&
                                            prevPrevItem.contentEquals("static")) ||
                                            prevItem.contentEquals("static")) {
                                        itemIndex++;
                                        if (lineElements.get(itemIndex).contains("(")) {
                                            tempString.append(lineElements.get(itemIndex));
                                        }
                                    }
                                }
                            }
                        }
                        itemIndex++;
                    }
                }
            }
        }
        if(tempString.length() > 0) {
            return tempString.toString();
        }
        return " ";
    }

    /**
     * Methode pour ajouter les commentaires de JAVADOC qui se trouve avant
     * la methode a exploiter.
     * @param lineCodeIndex C'est l'index de la ligne de code courante.
     * @return tous les commentaires JAVADOC trouvees avant une methode.
     */
    List<String> addJavaDOC(int lineCodeIndex) {
        int prevLineIndex = lineCodeIndex - 1;
        List<String> preComments = new ArrayList<>();
        while(!this.getCodeToParse().get(prevLineIndex).contains("}") &&
                !this.getCodeToParse().get(prevLineIndex).contains("/**")) {
            if(!detectForNoComments(this.getCodeToParse().get(prevLineIndex)) ||
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
     * Methode pour extraire le nom de la methode soit sur une ou plusieurs lignes.
     * @param firstmethodeName Le nom de la premiere methode pour la ajouter au
     *                         String dont le format est xxx_xxx_... .
     * @return Le nom de la methode extraite et nulle si aucune methode est trouvee.
     */
    String buildMethodeName(List<String> reparsedList, String firstmethodeName) {
        String[] splittedLineCode;
        int start = getStartIndex(reparsedList, firstmethodeName);
        if(start != -1) {
            String lineCode = reparsedList.get(start);
            if (lineCode.contains(firstmethodeName)) {
                splittedLineCode = lineCode.split(" ");
                if (lineCode.contains("()")) {
                    return iterateAndSplitArray(splittedLineCode, "()", "\\(\\)");
                } else if (lineCode.contains("(") && lineCode.contains(")")) {
                    return buildStringFromListOneLine(splittedLineCode);
                } else {
                    return buildStringFromMultipleLines(reparsedList, lineCode, start);
                }
            }
        }
        return null;
    }

    /**
     * C'est une methode pour extraire le debut de la methode a partir de son
     * definition.
     * @param reparsedList List contenant les commentaires et le code a traiter.
     * @param methodeName Le nom de la methode qu'on cherche.
     * @return L'index de la ligne qui contient le nom de la methode.
     */
    int getStartIndex(List<String> reparsedList, String methodeName) {
        int index = 0;
        if(methodeName != null) {
            for (String lineCode : reparsedList) {
                if (lineCode.contains(methodeName)) break;
                index++;
            }
            if(index == reparsedList.size()) return -1;
            return index;
        }
        return -1;
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
     * @return Un String sous forme xxx_xxx_... .
     */
    String buildStringFromListOneLine(String[] splittedLineCode) {
        StringBuilder extractedMethode = new StringBuilder();
        int currIndex = 0;
        String[] splitElements ;
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
     * @param lineCode La ligne du code actuelle.
     * @return Un String sous forme xxx_xxx_... .
     */
    String buildStringFromMultipleLines(List<String> reparsedList,String lineCode, int start) {
        String[] splittedLineCode;
        StringBuilder multipleLinesParams = new StringBuilder();
        String oneLineList;
        while(!lineCode.contains(")")) {
            splittedLineCode = lineCode.split(" ");
            oneLineList = buildStringFromListOneLine(splittedLineCode);
            if(!oneLineList.equals("")) { multipleLinesParams.append(oneLineList); }
            lineCode = reparsedList.get(start++);
        }
        return multipleLinesParams.toString();
    }

    /**
     * Methodes pour enlever toutes les espaces blancs.
     * @param element La ligne du code qui represente la phrase a traiter.
     * @return Une liste contenant les mots de la phrase.
     */
    List<String> getLineElements(String element) {
        element = element.replaceAll("\\s+", "+");
        List<String> lineElements = Arrays.asList(element.split("\\+"));
        return lineElements.subList(1, lineElements.size());
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
        float codeDensity = methode_NCLOC(reparsedMethode);
        float linesOfCode = methode_LOC(codeDensity, commentsDensity);
        treatedFile.add(String.valueOf(linesOfCode));
        treatedFile.add(String.valueOf(commentsDensity));
        float methodeDensity = methode_DC(commentsDensity, linesOfCode);
        treatedFile.add(String.valueOf(methodeDensity));
        float methode_BC = methode_BC(methodeDensity, numPredicats);
        treatedFile.add(String.valueOf(methode_BC));
        this.rearrangeData(treatedFile);
        return methode_BC;
    }

    /*  ==================================================================  */

    //  ------------------------------------------------------------        //
    //  | Méthodes pour extraire les calculs  :                    |        //
    //  ------------------------------------------------------------        //

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
