import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CodeAnalyzer {

    public static List<String> visitedLinks = new ArrayList<>();
    public static List<List<String>> classeFilesData = new ArrayList<>();
    public static List<List<String>> methodesFilesData = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        String currentPath = getAbsolutePath();
        ClassesParser classesParser = new ClassesParser(Arrays.asList("chemin",
                "class", "methode", "methode_CLOC", "methode_LOC", "methode_DC"));
        MethodesParser methodesParser = new MethodesParser(Arrays.asList("chemin",
                "class", "classe_LOC", "classe_CLOC", "classe_DC"));
        String linkToPass = "https://github.com/jfree/jfreechart";
        String firstLink = linkToPass + "/tree/master";
        String secondLink = linkToPass + "/blob/master";
        getDataFiles("https://github.com/jfree/jfreechart", firstLink,
                        secondLink, classesParser, methodesParser);
        FileWriter classesWriter = new FileWriter(currentPath + "/classes.csv");
        FileWriter methodesWriter = new FileWriter(currentPath + "/methodes.csv");
        printCSV(classesParser.getParsedList(), classesWriter);
        System.out.println("\n-------------------\n");
        printCSV(methodesParser.getParsedList(), methodesWriter);
        classesWriter.close();
        methodesWriter.close();
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
     * @param fileToPrint C'est le fichier(List) qui sera imprimer dans un fichier .csv
     * @param writer C'est une instanciation de type FileWriter pour ecrire dans le fichier cree.
     * @throws IOException
     */
    public static void printCSV(List<List<String>> fileToPrint, FileWriter writer) throws IOException {
        for (List<String> methodeData: fileToPrint) {
            String collect = String.join(", ", methodeData);
            System.out.println(collect);
            writer.write(collect);
            writer.write("\t\r\n");
        }
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
        List<List<List<String>>> classesAndMethodes = new ArrayList<>();
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
     * @return
     */
    public static List<String> parseClasses(ClassesParser classesParser,
                                            MethodesParser methodesParser,
                                            List<String> extractedCode,
                                            String chemin) {
        classesParser.setCodeToParse(extractedCode);
        classesParser.setCurrChemin(chemin);
        return classesParser.treatCode(methodesParser);
    }
}
