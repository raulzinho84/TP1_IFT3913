import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CodeAnalyzer {

    public static List<String> visitedLinks = new ArrayList<>();

    /**
     * Methode initiale qui contient le chemin absolu, les fichiers Writer qui vont
     * ecrire les donnees recues dans un fichier .csv.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ClassesParser classesParser = new ClassesParser(Arrays.asList("chemin",
                "class", "classe_LOC", "classe_CLOC", "classe_DC"));
        MethodesParser methodesParser = new MethodesParser(Arrays.asList("chemin",
                "class", "methode", "methode_LOC", "methode_CLOC", "methode_DC"));
        getDataFromFile(classesParser, methodesParser);
        String currentPath = getAbsolutePath();
        printFiles(classesParser,methodesParser,currentPath);
    }

    /**
     *  Prend l'integer choisi (1 ou 2) et applique le site web choisi pour
     *  extraire les donnees duquel.
     * @param classesParser Un objet de type ClassesParser qui contient les
     *                      fichier par les methodes appliquees par cette classe.
     * @param methodesParser Un objet de type methodesParser qui contient les
     *      *                fichier par les methodes appliquees par cette classe.
     * @throws IOException
     */
    public static void getDataFromFile(ClassesParser classesParser,
                                       MethodesParser methodesParser)
            throws IOException {
        int choice = provideChoice();
        String linkToPass;
        if(choice == 1) {
            linkToPass = "https://github.com/jfree/jfreechart";
        } else { linkToPass = getLink(); }
        String firstLink = linkToPass + "/tree/master";
        String secondLink = linkToPass + "/blob/master";
        getDataFiles(linkToPass, firstLink, secondLink, classesParser, methodesParser);
    }

    /**
     *  C'est la methode contenant une boucle while pour verifier que le choix
     *  est soit 1 ou 2.
     * @return un integer dont la valeur est 1 ou 2.
     */
    public static int provideChoice() {
        Scanner myObj = new Scanner(System.in);
        int number;
        do {
            showDisplayMessage();
            while (!myObj.hasNextInt()) {
                System.out.println("Wrong choice! Please type a number.");
                showDisplayMessage();
                myObj.next();
            }
            number = myObj.nextInt();
            if(number <= 0 || number > 2) {
                System.out.println("Wrong choice! Please choose a number from " +
                        "1 or 2.");
                number = -1;
            }
        } while (number <= 0);
        return number;
    }

    /**
     *  Afficher un message sur l'ecran pour choisir si on veut le site web donne
     *  a l'enonce ou un autre site web.
     */
    public static void showDisplayMessage() {
        System.out.println("Please choose from the following : \n " +
                "1. https://github.com/jfree/jfreechart \n " +
                "2. A github website from your choice.");
    }

    /**
     * Methode pour demande le link a ouvrir pour extraire ses donnees.
     * @return un lien sur github.
     */
    public static String getLink() {
        Scanner myObj = new Scanner(System.in);
        String website = "github.com";
        String currLink = "";
        do {
            System.out.print("Please enter a github link : ");
            while (!myObj.hasNext()) {
                System.out.println("Wrong choice! Please type a valid phrase!");
                System.out.println("Please enter a github link?");
                myObj.next();
            }
            currLink = myObj.next();
            String[] listCurrLink = currLink.split("/");
            if(listCurrLink.length >= 5 && listCurrLink[2].contentEquals(website)) {
                currLink = buildLink(currLink, website);
            } else {
                currLink = "";
                System.out.println("Please enter a valid github website!");
            }
        } while (currLink.contentEquals(""));
        return currLink;
    }

    /**
     * C'est la methode qui prend le lien mis sur le console et retourne la page
     * principale du projet.
     * @param currLink Le link obtenu par la methode getLink.
     * @param website Le lien github.com
     * @return http://github.com/xxx/xxx
     */
    public static String buildLink(String currLink, String website) {
        String[] temp = currLink.split(website);
        String tempFirst = temp[0];
        String[] tempSecond = temp[1].split("/");
        String userName = tempSecond[1];
        String folder = tempSecond[2];
        return tempFirst + website + "/" + userName + "/" + folder;
    }

    /**
     *
     * @return Le chemin absolu du repertoire qui sera utilise pour creer les
     * fichiers .csv
     */
    public static String getAbsolutePath() {
        Path currentRelativePath = Paths.get("");
        return currentRelativePath.toAbsolutePath().toString();
    }

    /**
     * Methode pour ecrire les donnees classes et methodes sur fichiers .csv.
     * @param classesParser Parseur des classes qui contient les donnees
     *                      des classes.
     * @param methodesParser Parseur des classes qui contient les donnees
     *                      des methodes.
     * @param currentPath Le lien local qui sera utilise pour ecrire
     *                    les deux fichiers dans lequel.
     * @throws IOException
     */
    public static void printFiles(ClassesParser classesParser,
                                  MethodesParser methodesParser,
                                  String currentPath) throws IOException {
        if(classesParser.getParsedList().size() > 1) {
            FileWriter classesWriter =
                    new FileWriter(currentPath + "/classes.csv");
            printCSV(classesParser.getcomplexityFile(), classesWriter);
            printCSV(classesParser.getParsedList(), classesWriter);
            printElements(classesParser.getParsedList(), classesWriter, "classes");
            classesWriter.close();
        } else { System.out.println("Aucune classe est detectee!"); }
        System.out.println("\n-------------------\n");
        if(methodesParser.getParsedList().size() > 1) {
            FileWriter methodesWriter =
                    new FileWriter(currentPath + "/methodes.csv");
            printCSV(methodesParser.getcomplexityFile(), methodesWriter);
            printCSV(methodesParser.getParsedList(), methodesWriter);
            printElements(methodesParser.getParsedList(), methodesWriter, "methodes");
            methodesWriter.close();
        } else { System.out.println("Aucune methode est detectee!"); }
    }

    /**
     *
     * @param fileToPrint C'est le fichier(List) qui sera imprimer dans un fichier .csv
     * @param writer C'est une instanciation de type FileWriter pour ecrire dans
     *               le fichier cree.
     * @throws IOException
     */
    public static void printCSV(List<List<String>> fileToPrint,
                                FileWriter writer) throws IOException {

        for (List<String> methodeData: fileToPrint) {
            String collect = String.join(", ", methodeData);
            printProcedure(writer, collect);
        }
    }

    /**
     *
     * @param fileToPrint La liste contenant soit les classes ou les
     *                    methodes a imprimer.
     * @param writer objet Writer pour ecrire dans un fichier .csv.
     * @param currObject classes/methodes.
     * @throws IOException
     */
    public static void printElements(List<List<String>> fileToPrint, FileWriter writer,
                                    String currObject) throws IOException {
        String header = "les 3 " + currObject + " les moins bien " +
                "commentées sont : \t\r\n";
        writer.write(header);
        int totalLength;
        if(fileToPrint.size() < 4) {
            totalLength = fileToPrint.size();
        } else { totalLength = 4; }
        for(int i = 0; i < totalLength; i++) {
            String collect = String.join(", ", fileToPrint.get(i));
            printProcedure(writer, collect);
        }

    }

    /**
     * Le procedure principal d'ecriture des fichiers.
     * @param writer objet Writer pour ecrire dans un fichier .csv.
     * @param collect La ligne qui represente les donnees d'une classe ou
     *                une methode x.
     * @throws IOException
     */
    public static void printProcedure(FileWriter writer, String collect)
            throws IOException {
        System.out.println(collect);
        writer.write(collect);
        writer.write("\t\r\n");
    }

    /**
     * @param dataURL C'est le raccourci de la page web contenant le projet a traiter sur GITHUB.
     * @return Toute la page web trouvee avec ses elements.
     * @throws IOException
     */
    public static Document getData(String dataURL) throws IOException {
        try { return Jsoup.connect(dataURL).get(); }
        catch (HttpStatusException ignored) {}
        return null;
    }


    /**
     * @param dataURL    C'est l'extension du siteWeb a traiter.
     * @param firstLink  C'est la premiere extension contenant /tree.
     * @param secondLink C'est la deuxieme extension contenant /blob.
     * @throws IOException
     */
    public static void getDataFiles(String dataURL,
                        String firstLink, String secondLink, ClassesParser
                                            classesParser, MethodesParser
                                            methodesParser) throws IOException {
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
                            getDataFiles(chemin, firstLink, secondLink,
                                    classesParser, methodesParser);
                        } else {
                            List<String> extractedCode = extractCode(chemin);
                            if(extractedCode != null) {
                                parseClasses(classesParser, methodesParser,
                                        extractedCode, chemin);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param dataURL C'est le raccourci(chemin ou link) de la page HTML courante.
     * @return Retourne la liste contenant le code extrait de la page HTML.
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

    /**
     * C'est la methode responsable d'ajouter les classes trouvees a l'attribut parsedList.
     * @param classesParser C'est le parseur instancie qui sera responsable a traiter les classes.
     * @param methodesParser C'est le parseur instancie qui sera responsable a traiter les methodes.
     * @param extractedCode C'est le code extrait de la page qui contient les classes et les methodes.
     * @param chemin C'est le chemin courant(link) de la page.
     */
    public static void parseClasses(ClassesParser classesParser,
                                    MethodesParser methodesParser,
                      List<String> extractedCode, String chemin) {
        classesParser.setCodeToParse(extractedCode);
        classesParser.setCurrChemin(chemin);
        classesParser.treatCode(methodesParser);
    }
}
