class Methods{
    public String abbrev(String full) throws FileNotFoundException{ //finds abbreviation of a country's name
        Scanner scan = new Scanner(new File("state_name.tsv"));
        while(scan.hasNextLine()){
            String name = scan.nextLine();
            String line[] = name.split("\t");

            if (line[2].equals(full)){
                scan.close();
                return line[1];
            }
        }
        return "Country does not exist";
    }

    public String realName(String abr) throws FileNotFoundException{ //takes an abbreviation and finds the full name
        Scanner scan = new Scanner(new File("state_name.tsv"));
        while(scan.hasNextLine()){
            String name = scan.nextLine();
            String line[] = name.split("\t");

            if (line[1].equals(abr)){
                scan.close();
                return line[2];
            }
        }
        return "Country does not exist";
    }

    public String stateNum(String country) throws Exception{
        Scanner scan1 = new Scanner(new File("state_name.tsv"));
        while(scan1.hasNextLine()){
            String name = scan1.nextLine();
            String line[] = name.split("\t");

            if (line[1].equals(country) || line[2].equals(country)){
                scan1.close();
                return line[0];
            }
        }
        scan1.close();

        Scanner scan2 = new Scanner(new File("capdist.csv"));
        while(scan2.hasNextLine()){
            String name = scan2.nextLine();
            String line[] = name.split(",");

            if (line[1].equals(country)){
                scan2.close();
                return line[0];
            }
        }
        return "Country does not exist";
    }
}