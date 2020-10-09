# CodeAnalyzer #

This program simply works on GITHUB projects and parses all their files recursiverly by opening them one by one then looking for the frequency of comments inside them.

### What is this repository for? ###

* This program simply works on GITHUB projects and parses all their files recursiverly and then looks after every Class and every Methode to extract their page link, their names, all associated parameters which is the parsing part in this code. On the other side, after extracting the Class/Method the program looks forward to count the frequency of comments according to the total lines of code assigned to this object. It extractes some values from each object like : 
  - NLOC(Non Commented Lines of Code).
  - CLOC(Commented Lines of Code).
  - LOC(Lines Of Code).
  - DC(Density Of Comments).
  - CC(Class Complexity).
  - WMC(Weighted Methods per Class).
Which will be calculated for both(Classes and Methods).
* Version : 1.0

### How do I get set up? ###

* Summary of set up
    - This program has been built with Intellij Idea Edu, so you can simply build and run
    by download this repo.
    - To compile and run via terminal :
        1. Access "src" folder on your terminal 
        2. Run this command to compile : ``` javac -cp "jsoup-1.13.1.jar" *.java ```
        3. Run this command to execute : ```  java -cp jsoup-1.13.1.jar:. CodeAnalyzer ```
        4. If the commands do not work, try replacing the ':' with ';' in the dependencies paths.
* Dependencies (all in "src" or "tests" folder)
    - ``` jsoup-1.13.1.jar ```
    - ``` junit-4.13.jar ``` (for unit tests)
    - ``` hamcrest-2.2.jar ``` (for unit tests)
* How to run tests
    - To compile and run via terminal :
        1. Compile the program (see above for instructions)
        2. Copy the ``` .class ``` files to "tests" folder
        3. Run this command to compile : ``` javac -cp "junit-4.13.jar:hamcrest-2.2.jar:." *.java ```
        4. Run this command to execute : ``` java -cp "junit-4.13.jar:hamcrest-2.2.jar:." TestRunner ```
        5. If the commands do not work, try replacing the ':' with ';' in the dependencies paths

### Who do I talk to? ###

* Repo owner or admin
    - [raulzinho84](https://github.com/raulzinho84)
    - [Moore505](https://github.com/Moore505)
