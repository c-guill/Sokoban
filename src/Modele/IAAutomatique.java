package Modele;

import Global.Configuration;
import Structures.Sequence;

import java.util.ArrayList;
import java.util.HashMap;

public class IAAutomatique extends IA{

    //0 = aucun lien
    //1 = lien
    private HashMap<Integer,int[]> emplacements;
    private final int MAX = 2147483647;


    public IAAutomatique(){
        this.emplacements = new HashMap<>();
    }
    @Override
    public Sequence<Coup> joue() {
        int pousseurL = niveau.lignePousseur();
        int pousseurC = niveau.colonnePousseur();
        Sequence<Coup> resultat = Configuration.nouvelleSequence();
        int[][] graph = genererGraphe(Niveau.MUR,Niveau.MUR);
        int source = convertToGraph(pousseurL,pousseurC);
        ArrayList<Integer> Caisses = getSommets(Niveau.CAISSE);
        ArrayList<Integer> Buts = getSommets(Niveau.BUT);
        ArrayList<Integer> finis = new ArrayList<>();
        assert Caisses.size() >= 1;
        for (Integer c : Caisses){
            int[] coord = convertToMap(c);
            if(this.niveau.aBut(coord[0],coord[1])){
                finis.add(c);
                finis.add(c);
                this.niveau.cases[coord[0]][coord[1]]=Niveau.MUR;
            }
        }
        for(Integer c : finis){
            Caisses.remove((Object) c);
            Buts.remove((Object) c);
        }
        int[][] caisse = Dijkstra(graph, Caisses.get(0));


        int[] chemins = getChemin(caisse[1],caisse[0],Caisses.get(0),Buts.get(0));
        int valid;
        while ((valid = isValid(chemins)) != -1){
            graph = enleverLien(graph, chemins[valid],chemins[valid+1]);
            caisse = Dijkstra(graph, Caisses.get(0));
            chemins = getChemin(caisse[1],caisse[0],Caisses.get(0),Buts.get(0));
            if(chemins == null){
                resultat.insereQueue(niveau.deplace(0,0));
                System.err.println("IMPOSSIBLE");
                return resultat;
            }
        }

        for (int i = 0 ; i < chemins.length-1; i++){
            int destination = getEmplacementAvant(chemins[i],chemins[i+1]);
            if(source == destination)
                destination = chemins[i];
            graph = genererGraphe(Niveau.MUR,Niveau.CAISSE);
            int[][] pousseur = Dijkstra(graph, source);
            int[] cheminspousseur = getChemin(pousseur[1],pousseur[0],source,destination);
            if(cheminspousseur == null){
                graph = genererGraphe(Niveau.MUR,Niveau.MUR);
                pousseur = Dijkstra(graph, source);
                cheminspousseur = getChemin(pousseur[1],pousseur[0],source,destination);
            }
            if(cheminspousseur == null){
                System.err.println("IMPOSSIBLE");
                break;
            }
            boolean changement = false;
            for (int j =0; j < cheminspousseur.length; j++){
                int[] coord = convertToMap(cheminspousseur[j]);
                int[] deplacement = getDeplacement(source, cheminspousseur[j]);
                if(deplacement[0]+deplacement[1]!=0){
                    if(this.niveau.aCaisse(coord[0],coord[1])){
                        //Le chemin choisis va bouger la caisse
                        changement = true;
                    }
                    resultat.insereQueue(niveau.deplace(deplacement[0],deplacement[1]));
                }
                source = cheminspousseur[j];
                if(changement) {
                    break;
                }
            }
            if(changement) {
                break;
            }
            int[] deplacement = getDeplacement(source, chemins[i]);
            resultat.insereQueue(niveau.deplace(deplacement[0], deplacement[1]));
            source = chemins[i];

        }

        if(resultat.estVide()){
            resultat.insereQueue(niveau.deplace(0,0));
        }

        return resultat;
    }

    private int[][] enleverLien(int[][] graph, int s1, int s2){
//        for(int i = 0; i < graph.length; i++){
//            graph[sommet][i] = 0;
//            graph[i][sommet] = 0;
//        }
        graph[s1][s2] = 0;
        return graph;
    }

    /**
     * Vérifie si le chemin est valid pour la caisse (il doit toujours avoir la place de la pousser)
     * @param chemins de la caisse
     * @return si ca retourn -1 alors le chemin est valide, sinon il retourne l'indice du sommet qui pose problème
     */
    private int isValid(int[] chemins){
        for (int i = 0; i < chemins.length-1 ; i++){
            int chemin = chemins[i];
            int emplacement = getEmplacementAvant(chemin,chemins[i+1]);
            int[] coord = convertToMap(emplacement);
            if(this.niveau.aMur(coord[0],coord[1])){
                return i;
            }
        }
        return -1;
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

    private int[] convertToMap(int sommet){
        return this.emplacements.get(sommet);
    }

    private int convertToGraph(int l, int c){
        int result = c+l*this.niveau.c;
        this.emplacements.put(result, new int[]{l,c});
        return result;
    }



    public int[][] genererGraphe(int Mur, int caisse){
        int[][] graph = new int[this.niveau.l*this.niveau.c][this.niveau.l*this.niveau.c];
        int value, num;
        for (int l = 0; l < this.niveau.l; l++){
            for (int c = 0; c < this.niveau.c; c++){
                num = convertToGraph(l,c);

                if((this.niveau.cases[l][c] & (Mur | caisse)) == 0){
                    if(l-1 >= 0 && (this.niveau.cases[l-1][c] & (Mur | caisse)) == 0){
                        graph[num][c+this.niveau.c*(l-1)]=1;
                    }
                    if(c-1 >= 0 && (this.niveau.cases[l][c-1] & (Mur | caisse)) == 0){
                        graph[num][c-1+this.niveau.c*(l)]=1;

                    }
                    if(l+1 < this.niveau.l && (this.niveau.cases[l+1][c] & (Mur | caisse)) == 0){
                        graph[num][c+this.niveau.c*(l+1)]=1;

                    }
                    if(c+1 < this.niveau.c && (this.niveau.cases[l][c+1] & (Mur | caisse)) == 0){
                        graph[num][c+1+this.niveau.c*(l)]=1;

                    }
                }
            }
        }

        return graph;
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
    private int[][] Dijkstra(int[][] graph, int source){
        int[] dist = new int[graph.length];
        int[] prev = new int[graph.length];
        ArrayList<Integer> Q = new ArrayList<>();
        int u, alt;

        for(int v = 0; v < graph.length; v++){
            dist[v] = MAX;
            prev[v] = -1;
            Q.add(v);
        }
        dist[source] = 0;
        while (!Q.isEmpty()){
            u = getSmallerDist(Q, dist);
            Q.remove((Object) u);
            for(int v = 0; v < graph[u].length; v++){
                int value = graph[u][v];
                if(value > 0 && Q.contains(v) && dist[u] != MAX){
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
