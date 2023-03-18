package Modele;

import Global.Configuration;
import Structures.Sequence;

import java.util.ArrayList;

public class IAAutomatique extends IA{

    //0 = aucun lien
    //1 = lien
    private int graph[][];
    private final int MAX = 2147483647;


    public IAAutomatique(){

    }
    @Override
    public Sequence<Coup> joue() {
        int pousseurL = niveau.lignePousseur();
        int pousseurC = niveau.colonnePousseur();
        this.graph = genererGraphe();
        int source = pousseurC+pousseurL*this.niveau.c;
        int[][] result = Dijkstra(source);
//        for (Integer i : result[0]){
//            if(i != MAX)
//                System.out.print(i+" ");
//        }
//        System.out.println();
//        for (Integer i : result[1]){
//            if(i != -1)
//                System.out.print(i+" ");
//        }
        int l = 0;
        int c = 0;
        for (l = 0; l < this.niveau.l; l++) {
            for (c = 0; c < this.niveau.c-1; c++) {
                if(this.niveau.aBut(l,c)){
                    break;
                }
            }
            if(this.niveau.aBut(l,c)){
                break;
            }
        }

        int[] chemins = getChemin(result[1],result[0],source, c+l*this.niveau.c);
        Sequence<Coup> resultat = Configuration.nouvelleSequence();
        if(chemins.length == 1){
            resultat.insereQueue(niveau.deplace(0,0));
        }
        for(int i = 1; i < chemins.length ; i++){
            int[] deplacement = convertToMap(chemins[i-1],chemins[i]);
            System.out.println(deplacement[0]+" "+deplacement[1]);
            resultat.insereQueue(niveau.deplace(deplacement[0],deplacement[1]));
        }



        return resultat;
    }

    private int[] convertToMap(int source, int destination){
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



    public int[][] genererGraphe(){
        int[][] graph = new int[this.niveau.l*this.niveau.c][this.niveau.l*this.niveau.c];
        int value, num;
        for (int l = 0; l < this.niveau.l; l++){
            for (int c = 0; c < this.niveau.c; c++){
                num = c+l*this.niveau.c;

                if(!this.niveau.aMur(l,c)){
                    if(l-1 >= 0 && !this.niveau.aMur(l-1,c)){
                        graph[num][c+this.niveau.c*(l-1)]=1;
                    }
                    if(c-1 >= 0 && !this.niveau.aMur(l,c-1)){
                        graph[num][c-1+this.niveau.c*(l)]=1;

                    }
                    if(l+1 < this.niveau.l && !this.niveau.aMur(l+1,c)){
                        graph[num][c+this.niveau.c*(l+1)]=1;

                    }
                    if(c+1 < this.niveau.c && !this.niveau.aMur(l,c+1)){
                        graph[num][c+1+this.niveau.c*(l)]=1;

                    }


                }
            }
        }

        return graph;
    }

    private int[] getChemin(int[] prev, int[]dist, int source, int destination){
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
    private int[][] Dijkstra(int source){
        int[] dist = new int[this.graph.length];
        int[] prev = new int[this.graph.length];
        ArrayList<Integer> Q = new ArrayList<>();
        int u, alt;

        for(int v = 0; v < this.graph.length; v++){
            dist[v] = MAX;
            prev[v] = -1;
            Q.add(v);
        }
        dist[source] = 0;
        while (!Q.isEmpty()){
            u = getSmallerDist(Q, dist);
            Q.remove((Object) u);
            for(int v = 0; v < this.graph[u].length; v++){
                int value = this.graph[u][v];
                if(value > 0 && Q.contains(v)){
                    alt = dist[u] + 1;
                    if(alt < dist[v]){
                        dist[v] = alt;
                        prev[v] = u;
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
