package Modele;

import Global.Configuration;
import Structures.Sequence;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
    private HashMap<Integer,int[]> emplacements;
    //    private HashMap<Integer, ArrayList<Integer>> parcourus = new HashMap();
    private HashMap<Integer, int[]> parcourus;
    private int min;


    @Override
    public Sequence<Coup> joue() {
        min = MAX;
        parcourus = new HashMap();
        emplacements = new HashMap();
        int pousseurL = niveau.lignePousseur();
        int pousseurC = niveau.colonnePousseur();
        Sequence<Coup> resultat = Configuration.nouvelleSequence();
        int source = convertToGraph(pousseurL,pousseurC);
        ArrayList<Integer> Caisses = getSommets(Niveau.CAISSE);
        ArrayList<Integer> Buts = getSommets(Niveau.BUT);

        long startTime = System.currentTimeMillis();
        ArrayList<Integer> result = cheminOptimal(0,source,Caisses,Buts);
        if(result == null){
            System.err.println("Impossible de résoudre le niveau");
            //modification de IA pour pouvoir skip de niveau
            resultat.insereQueue(this.niveau.deplace(0,0));
            this.jeu.prochainNiveau();
        }else {
            long endTime = System.currentTimeMillis();
            System.out.println("Temps execution: "+ (endTime - startTime));
            //remettre la carte comme elle était avant
            for(Integer c : Caisses) {
                int[] coordC = convertToMap(c);
                this.niveau.cases[coordC[0]][coordC[1]] = Niveau.CAISSE;
            }
            this.niveau.cases[pousseurL][pousseurC] = Niveau.POUSSEUR;

            for (int i = 0; i < result.size() - 1; i++) {
                int[] de = getDeplacement(result.get(i), result.get(i + 1));
                resultat.insereQueue(niveau.deplace(de[0], de[1]));
            }
        }
        return resultat;
    }

    private ArrayList<Integer> cheminOptimal(int size, int source, ArrayList<Integer> caisses, ArrayList<Integer> buts){
        if(size > min){
            return null;
        }


        int h = hash(caisses, source);
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
        if(finished(caisses,buts)) {
            ArrayList<Integer> result = new ArrayList<>();
            result.add(source);
            if(min > size){
                min = size;
            }
            return result;
        }else{
            // modification de la carte
            int[] coordP = convertToMap(source);
            this.niveau.cases[coordP[0]][coordP[1]] = Niveau.POUSSEUR;
            for(Integer c : caisses) {
                int[] coordC = convertToMap(c);
                this.niveau.cases[coordC[0]][coordC[1]] = Niveau.CAISSE;
            }
            int[][] dijkstra = Dijkstra(source,true);
            //emplacement ou peut aller la caisse
            ArrayList<int[]> emplacement = getEmplacementCaisse(dijkstra[1], caisses);
            this.niveau.cases[coordP[0]][coordP[1]] = Niveau.VIDE;
            for(Integer c : caisses) {
                int[] coordC = convertToMap(c);
                this.niveau.cases[coordC[0]][coordC[1]] = Niveau.VIDE;
            }
            ArrayList<ArrayList<Integer>> possibilite = new ArrayList<>();
            for (int[] c : emplacement) {
                int caisseid = c[0];
                int caisse = caisses.get(caisseid);
                int nouveau = c[1];
                int[] chemins = getChemin(dijkstra[1],dijkstra[0],source,getEmplacementAvant(caisse,nouveau));
                caisses.set(caisseid,nouveau);
                ArrayList<Integer> tmp = cheminOptimal(size + chemins.length, caisse, caisses, buts);
                caisses.set(caisseid,caisse);
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
            ArrayList<Integer> result = possibilite.get(0);
            for (int i = 1; i < possibilite.size(); i++){
                if(result.size() > possibilite.get(i).size()){
                    result = possibilite.get(i);
                }
            }
            return result;
        }

    }

    private boolean finished(ArrayList<Integer> caisses, ArrayList<Integer> buts){
        int i = 0;
        for (int c : caisses){
            for(int b : buts){
                if(c==b){
                    i++;
                    break;
                }
            }
        }
        return caisses.size() == i;

    }

    private int hash(ArrayList<Integer> caisses, int pousseur){
        int result = pousseur;
        for (int i = 0; i < caisses.size(); i++) {
            result = 31 * result + caisses.get(i);
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

    /**
     *
     * @param prev
     * @param caisses
     * @return int[ancien empalcement(id), nouveau emplacement]
     */
    public ArrayList<int[]> getEmplacementCaisse(int[] prev, ArrayList<Integer> caisses){
        ArrayList<int[]> result = new ArrayList<>();
        for(int i = 0; i < caisses.size();i++) {
            int caisse = caisses.get(i);
            int[] c = convertToMap(caisse);
            int x = c[0];
            int y = c[1];

            for (int[] t : tab) {
                x += t[0];
                y += t[1];
                int s = convertToGraph(x, y);
                int dest = getEmplacementApres(s, caisse);
                int[] coord = convertToMap(dest);
                if (prev[s] != -1 && !this.niveau.aMur(coord[0], coord[1]) && !this.niveau.aCaisse(coord[0], coord[1])) {
                    int j = 0;
                    result.add(j, new int[]{i,dest});
                }
                x = c[0];
                y = c[1];
            }
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
