import java.util.*;
import java.io.*;
import java.util.Map.Entry;

public class IRoadTrip{
    String countries[] = new String[253];
    int i = 0;

    //HashMap<String, HashMap<String, double>> inner = new HashMap<>();
    //HashMap<String, inner> outer = new HashMap<>(); //has keys which will receive values based on inner

    HashMap<String, HashMap<String, HashMap<String, Double>>> collection = new HashMap<>();

    IRoadTrip(String[] args) throws Exception{
        Scanner borders = new Scanner(new File(args[0]));
        Scanner caps = new Scanner(new File(args[1]));
        Scanner states = new Scanner(new File(args[2]));

        while(states.hasNextLine()){
            String line[] = states.nextLine().split("\t");
            if(line != null && line.length == 5 && line[4].equals("2020-12-31")){
                HashMap<String, HashMap<String, Double>> inner = new HashMap<>();
                inner.put(" " + line[2] + " ", null);
                collection.put(line[0], inner);
                collection.put(line[1], inner);
                collection.put(line[2], inner);

                if(line[2].contains("(")){
                   int index = (line[2].indexOf("(") + 1);
                   String sub = line[2].substring(0, index - 2);
                   String otherName = line[2].substring(index, line[2].length() - 1);

                   collection.put(sub, inner);
                   collection.put(otherName, inner);
                }
            }
        }
        states.close();

        while(borders.hasNextLine()){
            String line[] = borders.nextLine().split(" = ");
            if(line.length > 1 && exists(line[0])){
                line[1] = line[1].replaceAll(" km", "");
                String bordering[] = line[1].split("; ");
                HashMap<String, Double> temp = new HashMap<>();

                for(String adj : bordering){
                    boolean cont = true;
                    String borderCountry = "";
                    for(int i = 0; i < adj.length() && cont; i++){
                        if(adj.charAt(i) >= '0' && adj.charAt(i) <= '9'){
                            borderCountry = adj.substring(0, i - 1);
                            cont = false;
                        }
                    }
                    if(exists(borderCountry)){
                        temp.put(borderCountry, -1000000.0);
                    }
                    else if(borderCountry.charAt(0) >= 'A' && borderCountry.charAt(0) <= 'Z' && borderCountry.charAt(borderCountry.length() - 1) >= 'A' && borderCountry.charAt(borderCountry.length() - 1) <= 'Z'){
                        if(borderCountry.charAt(1) == 'S'){
                            collection.put(borderCountry, collection.get("USA"));
                        }
                        else{
                            collection.put(borderCountry, collection.get("UKG"));
                        }
                    }
                }
                collection.get(line[0]).put(collection.get(line[0]).keySet().toArray()[0].toString(), temp);
            }
        }
        borders.close();
        
        int i = 0;
        while(caps.hasNextLine()){
            if(i == 0){
                i++;
                caps.nextLine();
            }
            String line[] = caps.nextLine().split(",");
            double distance = Double.parseDouble(line[4]);
            String adj = collection.get(line[3]).keySet().toArray()[0].toString();
            
            if(collection.get(line[1]).get(collection.get(line[1]).keySet().toArray[0].toString()) != null){
                String adjCountries[] = turnString(collection.get(line[1]).get(collection.get(line[1]).keySet().toArray()[0].toString()).keySet().toArray());

                for(String ac : adjCountries){
                    if(adj.equals(ac)){
                        collection.get(line[1]).get(collection.get(line[1]).keySet().toArray()[0].toString()).put(adj, distance);
                    }
                }
            }
        }
        caps.close();
    }

    public boolean exists(String name){
        if(collection.containsKey(name)){
            return true;
        }

        String countryNames[] = turnString(collection.keySet().toArray());
        return similar(name, countryNames);
    }



    public String[] turnString(Object[] obj){
        String[] names = new String[obj.length];
        for(int i = 0; i < obj.length; i++){
            names[i] = String.valueOf(obj[i]);
        }
        return names;
    }

//inner.get("United States").put(inner.get("United States").keySet().toArray()[0].toString(), 80.00);   //generic way of getting key
                                                                                                        //(regardless of country reference inputted
//inner.get("United States").put(inner.get("United States").keySet().toArray()[0].toString(), 80.00);
//inner.get("United States").put(inner.get("United States").keySet().toArray()[0].toString(), 80.00);


    public int[] similarityOf(String word1, String word2){
        int sim = 0, dif = 0;
        word1 = fix(word1);
        word2 = fix(word2);

        String phrase1[] = word1.split(" ");
        String phrase2[] = word2.split(" ");

        if(phrase1.length >= phrase2.length){
            for(int i = 0; i < phrase1.length; i++){
                if(word2.contains(phrase1[i])){
                    sim++;
                }
                else{
                    dif++;
                }
            }
        }
        else{
            for(int i = 0; i < phrase2.length; i++){
                if((word1.contains(phrase2[i]))){
                    sim++;
                }
                else{
                    dif++;
                }
            }
        }

        if(sim == 0){
            return null;
        }

        int ret[] = {sim, dif};

        return ret;
    }

    public boolean similar(String word, String outer[]){
        boolean consider = false;
        String similarWord = "";
        int mostSimilar = 0, leastDif = 1000;
        int sims[] = new int[2];

        for(String country : outer){

            if(country != null && country.length() > 0){
                sims = similarityOf(word, country);

                if(sims != null){

                    if(sims[0] > mostSimilar){
                        mostSimilar = sims[0];
                        leastDif = sims[1];
                        similarWord = country;
                        consider = true;
                    }
                    else if(sims[0] == mostSimilar){

                        if(sims[1] < leastDif){
                            mostSimilar = sims[0];
                            leastDif = sims[1];
                            similarWord = country;
                            consider = true;
                        }
                        else if(sims[1] == leastDif){

                            //inner.get("United States").put(inner.get("United States").keySet().toArray()[0].toString(), 80.00);
                            String inner1 = collection.get(country).keySet().toArray()[0].toString();
                            String inner2 = collection.get(similarWord).keySet().toArray()[0].toString();

                            if(!(inner1.equals(inner2))){
                                consider = false;
                            }
                        }
                    }
                }
            }
        }
        if(consider){
            collection.put(word, collection.get(similarWord));
        }
        return consider;
    }

    public String fix(String phrase){
        phrase = phrase.replace(",", "");
        phrase = phrase.replace("(", "");
        return phrase.replace(")", "");
    }

    public HashMap<String, HashMap<String, HashMap<String, Double>>> getMap(){
        return collection;
    }

    public HashMap<String, HashMap<String, Double>> getInnerMap(){
        HashMap<String, HashMap<String, Double>> innerMap = new HashMap<>();
        for(String outKey : turnString(collection.keySet().toArray())){
            innerMap.put(collection.get(outKey).keySet().toArray()[0].toString(), collection.get(outKey).get(collection.get(outKey).keySet().toArray()[0].toString()));
        }
        return innerMap;
    }

    public static void main(String[] args)throws Exception{
        IRoadTrip t = new IRoadTrip(args);

        String word = "United States of America";
        String similarWord = "";
        int sims[] = new int[2];
        int mostSimilar = -1;
        int leastDif = 1000;
        boolean consider = false;
    }
}