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
            if(line != null && line.length == 5 && line[4].equals("2020-12-31")){ //checks for valid countries
                HashMap<String, HashMap<String, Double>> inner = new HashMap<>(); //this hashmap has 3 levels: the top layer which references "the world",
                                                                                  //the "outer layer" which references every alias of every country
                                                                                  //and the "inner layer" which references every country's official (real) name,
                                                                                  //and the inner layer directly references a hashmap of key:value pairs
                                                                                  //of borderCountry:distance to borderCountry capital
                inner.put(" " + line[2] + " ", null);
                collection.put(line[0], inner);
                collection.put(line[1], inner);
                collection.put(line[2], inner);

                if(line[2].contains("(")){
                   int index = (line[2].indexOf("(") + 1);
                   String sub = line[2].substring(0, index - 2); //some countries have multiple names like Congo, Democratic Republic of (Zaire)
                   String otherName = line[2].substring(index, line[2].length() - 1); //so, we consider every possible alias and add it as a key to
                                                                                        //access information about a given country

                   collection.put(sub, inner);
                   collection.put(otherName, inner);
                }
            }
        }
        states.close();

        while(borders.hasNextLine()){ //here, we check the borders file to add bordering countries to what we created from state_name.tsv
                                      //we check borders.txt to make sure we add the right countries, using information in state_name.tsv
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
                            borderCountry = " " + adj.substring(0, i - 1) + " ";
                            cont = false;
                        }
                    }
                    if(exists(borderCountry)){
                        temp.put(borderCountry, 10.0);
                    }
                    //here's where we check for abbreviations, in which case only the US and UK are referenced
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
                //it's hard to explain this part, but collection.get(...) calls a layer of collection every time
                //in this case, we go layer by layer in our collection until we can put appropriate values in the country's border info
                //using temp
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
            double distance = Double.parseDouble(line[4]); //distance between capitals
            if(collection.get(line[0]) != null && collection.get(line[2]) != null){ //checking for cases where a country might be secluded(i.e. islands)

                String adj = collection.get(line[2]).keySet().toArray()[0].toString();
                
                if(collection.get(line[0]).get(collection.get(line[0]).keySet().toArray()[0].toString()) != null){
                    String adjCountries[] = turnString(collection.get(line[0]).get(collection.get(line[0]).keySet().toArray()[0].toString()).keySet().toArray());

                    for(String ac : adjCountries){
                        
                        if(adj.equals(ac)){
                            collection.get(line[0]).get(collection.get(line[0]).keySet().toArray()[0].toString()).put(adj, distance);
                        }
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



    public int[] similarityOf(String word1, String word2){ //not every country has equivalents across all files, but we can search for similarities
        int sim = 0, dif = 0; //based on how similar and not different two words or phrases are, we can guess that they are or aren't
                              //aliases of the same country
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

    public double getDistance(String country1, String country2){ //I was not able to finish my last two functions.
                                                                 //my idea for getDistance() was to check if country2 bordered country1,
                                                                 //that is to say collection.get(country1).get(collection.get(country1).keySet().toArray()[0].toString()) contained country2,
                                                                 //and if so return the distance associated with it. Otherwise, return -1.
        return collection.get(country1).get(collection.get(country1).keySet().toArray()[0].toString()).get(country2);
    }

    /*
     * public List<String> findPath (String country1, String country2){
     * for this function, my plan would have been to use Dijkstra's Algorithm to measure the weights(distances) returned from getDistance(),
     * and use recursion to keep checking paths and adding to their weight, iterating through every possible loop through the neighbors of
     * the country currently in recursion (specifically, I would need to look at the list of neighbors eliminating every previously visited
     * country). After finding a path or paths to country 2, I would compare all of them against one another to find the smallest total distance.
     * This could be achieved by putting every "path" into a minHeap and taking the topmost value, the smallest distance, and returning that path.
     * }
     * 
     */

    public static void main(String[] args)throws Exception{
        IRoadTrip t = new IRoadTrip(args);
    }
}