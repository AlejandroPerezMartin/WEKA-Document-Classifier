
import java.io.*;
import java.util.*;

public class Main
{

    private static HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
    private static File file = new File("results.arff");

    /**
     * Main controller
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        try
        {
            getAllWordOccurrences("/Users/alejandro/Desktop/20_newsgroups/");
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // Sort hashmap by value (number of occurrences)
        Object[] a = dictionary.entrySet().toArray();

        Arrays.sort(a, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Map.Entry<String, Integer>) o2).getValue().compareTo(
                        ((Map.Entry<String, Integer>) o1).getValue());
            }
        });

        // Get the 10000 most repeated words
        int i = 0;
        for (Object e : a)
        {
            if (i >= 10000)
            {
                dictionary.remove(((Map.Entry<String, Integer>) e).getKey());
            }
            i++;
        }

        // Create results file
        FileWriter fileWritter = new FileWriter(file.getName(), false);
        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

        // ARFF File header
        bufferWritter.write("% Author: Alejandro Perez Martin\n");
        bufferWritter.write("% Generated on: " + (new Date()).toString() + "\n");
        bufferWritter.write("\n@RELATION WekaDocumentClassifier\n");
        bufferWritter.write("\n@ATTRIBUTE documentID NUMERIC\n");

        for (String word : dictionary.keySet())
        {
            bufferWritter.write("@ATTRIBUTE " + word + " NUMERIC\n");
        }

        bufferWritter.write("@ATTRIBUTE documentClass {alt.atheism, comp.graphics, rec.autos, sci.electronics, talk.politics.guns}\n");
        bufferWritter.write("\n@DATA\n");

        // Close file
        bufferWritter.close();

        // Append all files occurrences
        findOccurrencesByFile("/Users/alejandro/Desktop/20_newsgroups/alt.atheism");
        findOccurrencesByFile("/Users/alejandro/Desktop/20_newsgroups/comp.graphics");
        findOccurrencesByFile("/Users/alejandro/Desktop/20_newsgroups/rec.autos");
        findOccurrencesByFile("/Users/alejandro/Desktop/20_newsgroups/sci.electronics");
        findOccurrencesByFile("/Users/alejandro/Desktop/20_newsgroups/talk.politics.guns");

        System.out.println("File was successfuly generated! :)");
        System.out.println("Uri: " + file.getAbsolutePath());

    }

    /**
     * List all files contained in a folder (also scans subfolders)
     *
     * @param folderPath
     * @throws IOException
     */
    public static void findOccurrencesByFile(String folderPath) throws IOException
    {
        File directory = new File(folderPath);
        File[] directoryFiles = directory.listFiles();

        String category = directory.getName(); // current category name

        for (final File fileEntry : directoryFiles)
        {
            if (fileEntry.isFile() && !fileEntry.getName().equals(".DS_Store"))
            {
                // Variables
                InputStream in = new FileInputStream(fileEntry);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                int i = 12;
                Integer occurrences;
                String result = fileEntry.getName() + ", ";
                HashMap<String, Integer> fileDictionary = new HashMap<String, Integer>();

                while ((line = reader.readLine()) != null)
                {
                    // Skip first 12 lines (document header)
                    if (i >= 0)
                    {
                        i--;
                        continue;
                    }

                    String[] lineWords = line.split("\\W+");

                    for (String word : lineWords)
                    {
                        // string filtering
                        word = word.replaceAll("[0-9]", "");
                        word = word.replaceAll("[_,.;-]", "");
                        word = word.toLowerCase();

                        if (word.length() <= 2 || isNumeric(word)) continue;

                        occurrences = fileDictionary.get(word);

                        if (occurrences == null)
                        {
                            fileDictionary.put(word, 1);
                        }
                        else
                        {
                            fileDictionary.put(word, occurrences + 1);
                        }
                    }
                }

                for (String word : dictionary.keySet())
                {
                    Integer y = fileDictionary.get(word);

                    if (y != null)
                    {
                        result += fileDictionary.get(word) + ", ";
                    }
                    else
                    {
                        result += "0, ";
                    }
                }

                FileWriter fileWritter = new FileWriter(file.getName(), true);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

                bufferWritter.write(result + category + "\n");

                bufferWritter.close();

                reader.close();
            }
        }
    }

    /**
     * Analises all the documents within the given directory and creates a dictionary
     * containing all the words and the number of occurrences across all the documents
     *
     * @param folderPath
     * @throws IOException
     */
    public static void getAllWordOccurrences(String folderPath) throws IOException
    {
        File directory = new File(folderPath);
        File[] directoryFiles = directory.listFiles();

        for (final File fileEntry : directoryFiles)
        {
            if (fileEntry.isDirectory())
            {
                getAllWordOccurrences(fileEntry.getAbsolutePath());
            }
            else
            {
                if (!fileEntry.getName().equals(".DS_Store"))
                {
                    // Variables
                    InputStream in = new FileInputStream(fileEntry);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    int i = 12;
                    Integer occurrences;

                    while ((line = reader.readLine()) != null)
                    {
                        // Skip first 12 lines (document header)
                        if (i >= 0)
                        {
                            i--;
                            continue;
                        }

                        String[] lineWords = line.split("\\W+");

                        for (String word : lineWords)
                        {
                            // string filtering
                            word = word.replaceAll("[0-9]", "");
                            word = word.replaceAll("[_,.;-]", "");
                            word = word.toLowerCase();

                            if (word.length() <= 2 || isNumeric(word)) continue;

                            occurrences = dictionary.get(word);

                            if (occurrences == null)
                            {
                                dictionary.put(word, 1);
                            }
                            else
                            {
                                dictionary.put(word, occurrences + 1);
                            }
                        }
                    }

                    reader.close();
                }
            }
        }
    }

    /**
     * Returns whether a string contains a number or not
     *
     * @param str
     * @return
     * @source https://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-a-numeric-type-in-java#answer-1102916
     */
    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

}