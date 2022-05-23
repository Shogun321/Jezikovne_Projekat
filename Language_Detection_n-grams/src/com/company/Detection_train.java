package com.company;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.*;
import java.util.*;


public class Detection_train {

    static final String Language_path = "B:\\skola\\FERI\\Jezikovne tehnologije\\Seminarska\\GithubCode\\Language_Detection_n-grams\\dslcc4_Korpus";//"data/";
    static HashMap<String, List<String>> LanguageProfiles = new HashMap<String, List<String>>();

    public static void main(String[] args) throws IOException {

        Properties props = new Properties();
        props.put("annotators", "tokenize");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


        String detected="";
        int optimumscore=1000000;
        //Reading the Language Documents
        File folder = new File(Language_path);
        System.out.println(folder);
        for (File subdirec : folder.listFiles()) {
            if (subdirec.isDirectory()) {
                System.out.println("Reading " + subdirec+"........we.");
                File[] listOfFiles = subdirec.listFiles();
                String text = "";
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().endsWith(".txt")) {
                        BufferedReader br = new BufferedReader(new FileReader(subdirec + "/" +
                                file.getName()));
                        System.out.println("Reading " + file + " something.");
                        try {
                            String line = br.readLine();
                            //int j =0;
                            while (line != null) {//line != null j++<5000
                                if (line.length() > 1) {
                                    //System.out.println(line);
                                    text += line.replaceAll("\\d+", "")
                                            .replaceAll("<.*?>", "")
                                            .replaceAll("[&\\/\\\\#,+()$~%.\":*?!<>{}]", "")
                                            .replaceAll("\\s+", " ")
                                            .trim()
                                            .toLowerCase();
                                }
                                line = br.readLine();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        br.close();
                    }

                }//end of external for

                System.out.println("Preparing Language Model......");
                List<String> LanguageProfile = PrepareProfile(text, pipeline);

                System.out.println("Serializing Language Model......" + "\n\n");
                //Serialize the language profile
                try{
                    System.out.println("Language_Models_Serialized_top_600_K_10000/"+
                            subdirec.toString().split("/")[0]);
                    FileOutputStream fos= new FileOutputStream("Language_Models_Serialized_top_600_K_10000/"
                            );
                    System.out.println(LanguageProfile.size());
                    ObjectOutputStream oos= new ObjectOutputStream(fos);
                    oos.writeObject(LanguageProfile);
                    oos.close();
                    fos.close();

                }catch(IOException ioe){
                    ioe.printStackTrace();
                }

/*
                int score=OutOfPlaceScore(LanguageProfile, TestProfile);
                System.out.println("Score:" +score + "\n" + "================================\n");
                if(score<optimumscore){
                    optimumscore=score;
                    String[] temp= subdirec.toString().split("/");
                    detected=temp[1];
                }
*/
            }
        }
        //System.out.println("The document is most likely to be written in: "+ detected);
    }


    public static HashMap<String, Integer> getNgrams(String text, int n, StanfordCoreNLP pipeline) {
        HashMap<String, Integer> Ngrams = new HashMap<>();
        //text=parts[0].concat(parts[1]);

        Annotation document = new Annotation(text);
        text=null;
        pipeline.annotate(document);
        List<CoreLabel> tokens = document.get(TokensAnnotation.class);
        document=null;
        for (int i = 0; i < tokens.size() - (n - 1); i++) {
            String gram = "";
            for (int j = i; j < i + n; j++) {
                gram += (tokens.get(j).toString() + " ");
            }
            String temp = gram.trim();

            if (Ngrams.containsKey(temp)) {
                Ngrams.put(temp, Ngrams.get(temp) + 1);
            } else {
                Ngrams.put(temp, 1);
            }
        }
        return Ngrams;
    }
//Potential quick fixes: https://stackoverflow.com/questions/37335/how-to-deal-with-java-lang-outofmemoryerror-java-heap-space-error
    //https://stackoverflow.com/questions/47974590/stanford-corenlp-exception-in-thread-main-java-lang-outofmemoryerror-java-h


    public static List<String> PrepareProfile(String test, StanfordCoreNLP pipeline) {

        HashMap<String, Integer> TwoGrams = getNgrams(test, 2, pipeline);
        HashMap<String, Integer> ThreeGrams = getNgrams(test, 3, pipeline);
        TwoGrams.putAll(ThreeGrams);

        //Sort HashMap according to frequency
        Set<Map.Entry<String, Integer>> set = TwoGrams.entrySet();

        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(set);

        try {
            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    if ((o2.getValue()).compareTo(o1.getValue()) == 0) {
                        if (o2.getKey().length() > o1.getKey().length()) {
                            return 1;
                        }
                        return -1;
                    }
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            });
        }
        catch(IllegalArgumentException i){

            System.out.println("error");
        }


        int top = 600;  //Number of Top values to be taken

        List<String> Profile = new ArrayList<String>();

        for (Map.Entry<String, Integer> entry : list) {
            //   sortedMap.put(entry.getKey(), entry.getValue());
            Profile.add(entry.getKey());
            top--;
            if (top == 1) {
                break;
            }
        }
        return Profile;
    }


    public static int OutOfPlaceScore(List<String> langProfile, List<String> docProfile) {
        int score = 0;

        //penalty
        int K = 10000;

        for (String element : docProfile) {
            if (langProfile.contains(element)) {
                int temp = langProfile.indexOf(element) - docProfile.indexOf(element);
                score += temp;
                //System.out.println(element + " :: " + temp + "\n");
            } else {
                score += K;
                // System.out.println(element + " :: " + K + "\n");
            }
        }
        return score;
    }
}
