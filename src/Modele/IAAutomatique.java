package Modele;

import Global.Configuration;
import Structures.Sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class IAAutomatique extends IA{

    private final int MAX = 2147483647;
    private final int[][] tab = new int[][]
            {
                    {-1,0},
                    {1,0},
                    {0,-1},
                    {0,1},
            };
    private HashMap<Integer,int[]> emplacements = new HashMap<>();
//    private HashMap<Integer, ArrayList<Integer>> parcourus = new HashMap();
    private HashMap<Integer, int[]> parcourus = new HashMap();


    @Override
    public Sequence<Coup> joue() {
        int pousseurL = niveau.lignePousseur();
        int pousseurC = niveau.colonnePousseur();
        Sequence<Coup> resultat = Configuration.nouvelleSequence();
        int source = convertToGraph(pousseurL,pousseurC);
        ArrayList<Integer> Caisses = getSommets(Niveau.CAISSE);
        ArrayList<Integer> Buts = getSommets(Niveau.BUT);


        ArrayList<Integer> result = cheminOptimal(0,source,Caisses.get(0),Buts.get(0));
        if(result == null){
            System.err.println("IMPOSSIBLE DE Résoudre le niveau");
        }else {

            //remettre la carte comme elle était avant
            int[] coord = convertToMap(Caisses.get(0));
            this.niveau.cases[coord[0]][coord[1]] = Niveau.CAISSE;
            this.niveau.cases[pousseurL][pousseurC] = Niveau.POUSSEUR;

            for (int i = 0; i < result.size() - 1; i++) {
                int[] de = getDeplacement(result.get(i), result.get(i + 1));
                resultat.insereQueue(niveau.deplace(de[0], de[1]));
            }
        }
        if(resultat.estVide()){
            resultat.insereQueue(niveau.deplace(0,0));
        }
        return resultat;
    }

    private ArrayList<Integer> cheminOptimal(int size, int source, int caisse, int but){
        // modification de la carte
        int[] coordP = convertToMap(source);
        this.niveau.cases[coordP[0]][coordP[1]] = Niveau.POUSSEUR;
        int[] coordC = convertToMap(caisse);
        this.niveau.cases[coordC[0]][coordC[1]] = Niveau.CAISSE;

        int[][] dijkstra = Dijkstra(source,true);
        this.niveau.cases[coordP[0]][coordP[1]] = Niveau.VIDE;
        this.niveau.cases[coordC[0]][coordC[1]] = Niveau.VIDE;

        int h = hash(dijkstra[1],caisse);
        if(this.parcourus.containsKey(h)){
            int[] value = this.parcourus.get(h);
            if(value[1] <= size){
                return null;
            }else{
                value[0] = source;
                value[1] = size;
                this.parcourus.replace(h,value);
            }
        }else{
            int[] value = {source,size};
            this.parcourus.put(h,value);
        }
        if(caisse == but) {
            System.out.println("BUT-> "+size);
            ArrayList<Integer> result = new ArrayList<>();
            result.add(source);
            result.add(caisse);
            return result;
        }else{
            //emplacement ou peut aller la caisse
            ArrayList<Integer> emplacement = getEmplacementCaisse(dijkstra[1], caisse);
            ArrayList<ArrayList<Integer>> possibilite = new ArrayList<>();
            for (Integer c : emplacement) {
                if(c == 58){
                    System.out.println("ici");
                }
                int[] chemins = getChemin(dijkstra[1],dijkstra[0],source,getEmplacementAvant(caisse,c));
                ArrayList<Integer> tmp = cheminOptimal(size + chemins.length, caisse, c, but);
                if(tmp != null) {
                    for(int i = 0 ; i< chemins.length; i++){
                        tmp.add(i, chemins[i]);
                    }
                    possibilite.add(tmp);
                }
            }
            if(possibilite.isEmpty()){
                return null;
            }
            if(caisse == 59 && source == 64){
                System.out.println("ICI");
            }
            ArrayList<Integer> result = possibilite.get(0);
            for (int i = 1; i < possibilite.size(); i++){
                if(result.size() > possibilite.get(i).size()){
                    result = possibilite.get(i);
                }
            }
            return result;
        }

    }

/*
    //ajouter chemin pour aller caisse (dijkstra)
    private ArrayList<Integer> cheminOptimal(int source, int caisse, int but, int size){
//        System.out.println(caisse);
        if(caisse == but){
            ArrayList<Integer> res = new ArrayList<>();
            res.add(but);
            System.out.println("BUT");
            return res;
        }
        ArrayList<Integer> result;

        //maj niveau
        int[] coordP = convertToMap(source);
        this.niveau.cases[coordP[0]][coordP[1]] = Niveau.POUSSEUR;
        int[] coordC = convertToMap(caisse);
        this.niveau.cases[coordC[0]][coordC[1]] = Niveau.CAISSE;

        int[][] dijkstra = Dijkstra(source,true);
        this.niveau.cases[coordP[0]][coordP[1]] = Niveau.VIDE;
        this.niveau.cases[coordC[0]][coordC[1]] = Niveau.VIDE;
        int h = hash(dijkstra[1],caisse);
        ArrayList<Integer> value = null;
        if(this.parcourus.containsKey(h)){
            value = this.parcourus.get(h);
            if(value == null || value.size() < size){
                return this.parcourus.get(h);
            }else if(value.size() > size){
                this.parcourus.replace(h,null);
            }
        }else {
            this.parcourus.put(h,null);
        }
        System.out.println("Parcous de: "+caisse+" -> "+size);
        if(value != null){
            System.out.println(value.size() +" < " +size );
        }

        ArrayList<ArrayList<Integer>> possibilite = new ArrayList<>();
        ArrayList<Integer> emplacement = getEmplacementCaisse(dijkstra[1],caisse);
//        System.out.println(caisse+": "+emplacement);
        for(Integer c : emplacement){
            result = cheminOptimal(caisse,c,but,size+1);
            if(result != null) {
                result.add(0,caisse);
                possibilite.add(result);
            }
        }
        if(possibilite.isEmpty()){
            return null;
        }
        result = possibilite.get(0);
        for(ArrayList<Integer> p : possibilite){
            if(result.size() > p.size()){
                result = p;
            }
        }
        value = this.parcourus.get(h);
        if(value == null || value.size() > result.size()){
            this.parcourus.replace(h,result);
        }
        return result;
    }
*/

    private int hash(int[] tab, int s){
        int result = s;
        for (int i = 0; i < tab.length; i++) {
            if(tab[i] == -1){
                result = 31 * result + 2;
            }else {
                result = 31 * result + 3;
            }
        }
        return result;
    }

    private int[] getDeplacement(int source, int destination){
        int[] result = new int[2];
        if(source+1 == destination){
            result[1] = 1;
        }else if(source-1 == destination){
            result[1] = -1;
        }else if(source + this.niveau.c == destination){
            result[0] = 1;

        }else if(source - this.niveau.c == destination){
            result[0] = -1;

        }
        return result;
    }

    private ArrayList<Integer> getSommets(int target){
        ArrayList<Integer> sommets = new ArrayList<>();
        for (int l = 0; l < this.niveau.l; l++) {
            for (int c = 0; c < this.niveau.c-1; c++) {
                if((this.niveau.cases[l][c] & target) != 0){
                    sommets.add(convertToGraph(l,c));
                }
            }
        }
        return sommets;
    }

    private int[] convertToMap(int sommet){
        return this.emplacements.get(sommet);
    }

    private int convertToGraph(int l, int c){
        int result = c+l*this.niveau.c;
        if(!this.emplacements.containsKey(result))
            this.emplacements.put(result, new int[]{l,c});
        return result;
    }

    public ArrayList<Integer> getEmplacementCaisse(int[] prev, int target){
        ArrayList<Integer> result = new ArrayList<>();
        int[] c = convertToMap(target);
        int x = c[0];
        int y = c[1];

        for(int[] t : tab){
            x+=t[0];
            y+=t[1];
            int s = convertToGraph(x,y);
            int dest = getEmplacementApres(s,target);
            int[] coord = convertToMap(dest);
            if(prev[s]!=-1 && !this.niveau.aMur(coord[0],coord[1])){
                result.add(dest);
            }
            x = c[0];
            y = c[1];

        }
        return result;
    }

    /**
     *
     * @param source
     * @param destination
     * @return renvoie le sommet de l'emplacement ou il faut etre pour pousser la caisse
     */
    private int getEmplacementAvant(int source, int destination){
        int result = source - destination;
        return source + result;
    }

    /**
     *
     * @param source emplacement du pousseur
     * @param destination emplacement de la caisse
     * @return renvoie le sommet de l'emplacement ou la caisse va etre poussé
     */
    private int getEmplacementApres(int source, int destination){
        int result = source - destination;
        return destination - result;
    }


    private int[] getChemin(int[] prev, int[]dist, int source, int destination){
        if(prev[destination] == -1){
            return null;
        }
        int[] chemins = new int[dist[destination]+1];
        chemins[0] = source;
        chemins[dist[destination]] = destination;
        for (int i = dist[destination]; i > 0; i--){
            destination = prev[destination];
            chemins[i-1] = destination;
        }
        return chemins;
    }

    /**
     * dist = distance entre la source et le sommet [taille = nb sommet]
     * prev = sommet précedent du sommet actuel [taille = nb sommet] (pour arriver au sommet v il faut passer par le sommet prev[v])
     */
    private int[][] Dijkstra(int source, boolean caisse){
        int size = this.niveau.l*this.niveau.c;
        int[] dist = new int[size];
        int[] prev = new int[size];
        ArrayList<Integer> Q = new ArrayList<>();
        int u;

        for (int l = 0; l < this.niveau.l; l++){
            for (int c = 0; c < this.niveau.c; c++){
                int v = convertToGraph(l,c);
                dist[v] = MAX;
                prev[v] = -1;
                Q.add(v);
            }
        }

        dist[source] = 0;
        prev[source] = source;
        while (!Q.isEmpty()){
            u = getSmallerDist(Q, dist);
            Q.remove((Object) u);
            int[] coord = convertToMap(u);
            for(int[] t : tab){
                int l = coord[0]+t[0];
                int c = coord[1]+t[1];
                if(l > -1 && c > -1 && l < this.niveau.l && c < this.niveau.c) {
                    int alt, v;
                    v = convertToGraph(l, c);
                    if (!this.niveau.aMur(l, c) && Q.contains(v) && dist[u] != MAX) {
                        if(!caisse || !this.niveau.aCaisse(l, c)) {
                            alt = dist[u] + 1;
                            if (alt < dist[v]) {
                                dist[v] = alt;
                                prev[v] = u;
                            }
                        }
                    }
                }
            }
        }
        return new int[][]{dist, prev};
    }

    private int getSmallerDist(ArrayList<Integer> Q, int[] dist){
        int result = Q.get(0);
        for (Integer u : Q){
            if(dist[result] > dist[u]){
                result = u;
            }
        }
        return result;
    }

}
