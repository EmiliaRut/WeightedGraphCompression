/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;
import java.util.Random;
import linkedgraph.*;
/**
 *
 * @author aromualdo
 */
public class GraphTests {
    
    public static void main(String... args) {
        System.out.println("Working Directory = "
                + System.getProperty("user.dir"));
        LinkedGraph g = LinkedGraph.load("./test1.txt");
        System.out.println(g);
        // {{(0,6bc7c054) -> 1,4},{(1,232204a1) -> 0,2,4},{(2,4aa298b7) -> 1,3},{(3,7d4991ad) -> 2,5},{(4,28d93b30) -> 0,1},{(5,1b6d3586) -> 3}}
        System.out.println("New Structure: ");
        System.out.println(g);
        LinkedGraph h = g.deepCopy();
        System.out.println("FL Test 1: Merge(0,4) => 0");
        System.out.println(h.fakeLinks(0, 4));
        h = g.deepCopy();
        System.out.println("FL Test 2: Merge(2,5) => 2");
        System.out.println(h.fakeLinks(2, 5));
        h = g.deepCopy();
        System.out.println("FL Test 3: Merge(3,5) => 1");
        System.out.println(h.fakeLinks(3, 5));

        g = LinkedGraph.load("./test2.txt");
        // {{(0,4554617c) -> 1,3,4},{(1,74a14482) -> 0,3},{(2,1540e19d) -> 4},{(3,677327b6) -> 0,1,4},{(4,14ae5a5) -> 0,2,3}}
        System.out.println("New Structure: ");
        System.out.println(g);
        h = g.deepCopy();
        System.out.println("FL Test 4: Merge(1,3) => 1");
        System.out.println(h.fakeLinks(1, 3));
        System.out.println("FL Test 5: Merge(1,4) => 2");
        System.out.println(h.fakeLinks(1, 4));
        System.out.println("FL Test 6: Merge(0,3) => 0");
        System.out.println(h.fakeLinks(0, 3));

        g = LinkedGraph.load("./test1.txt");
        // {{(0,6bc7c054) -> 1,4},{(1,232204a1) -> 0,2,4},{(2,4aa298b7) -> 1,3},{(3,7d4991ad) -> 2,5},{(4,28d93b30) -> 0,1},{(5,1b6d3586) -> 3}}
        System.out.println("New Structure: ");
        System.out.println(g);
        h = g.deepCopy();
        System.out.println("BFS(4,2) => [0,1,2]");
        System.out.println(h.bfs(4, 2));
        System.out.println("BFS(4,100) => [0,1,2,3,4,5]");
        System.out.println(h.bfs(4, 100));
        System.out.println("BFS(2,1) => [1,3]");
        System.out.println(h.bfs(2, 1));
        System.out.println("Dist(0,3) => 3");
        System.out.println(h.distance(0, 3));

        
        g = LinkedGraph.load("./test1.txt");
        // {{(0,6bc7c054) -> 1,4},{(1,232204a1) -> 0,2,4},{(2,4aa298b7) -> 1,3},{(3,7d4991ad) -> 2,5},{(4,28d93b30) -> 0,1},{(5,1b6d3586) -> 3}}
        System.out.println("New Structure: ");
        h = g.deepCopy();
        System.out.println(h);
        System.out.println("Merge(0,4):");
        h.merge(0, 4);
        System.out.println(h);
        System.out.println("Merge(1,1):");
        h.merge(1, 1);
        System.out.println(h);
        System.out.println("Merge(1,0):");
        h.merge(1, 0);
        System.out.println(h);
        System.out.println("Merge(4,5):");
        h.merge(4, 5);
        System.out.println(h);
        
        LinkedGraph k1 = LinkedGraph.load("./ecoli.txt");
        LinkedGraph k2 = k1.deepCopy();
        Random rand = new Random();
        int number_of_merges=500;
        System.out.println("Applying "+500+" merges to K2");
        for(int i=0;i<number_of_merges;i++){
            int rand1 = rand.nextInt(k2.getSize());
            int rand2 = rand.nextInt(k2.getSize());
            //System.out.println("K2 Merge("+rand1+","+rand2+")");
            k2.merge(rand1, rand2);
        }
        System.out.println("K1 size: "+k1.getSize());
        System.out.println("K1 current size: "+k1.getCurrentSize());
        System.out.println("K1: "+k1);
        System.out.println("K2 size: "+k2.getSize());
        System.out.println("K2 current size: "+k2.getCurrentSize());
        System.out.println("K2: "+k2);
    }
}

