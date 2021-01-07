package display;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import linkedgraph.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolutions;

/**
 *
 * @author Angelo Romualdo <angelo.romualdo at brocku.ca>
 */
public class GraphDisplay {

	public static void displayLinkedGraph(File f) {
		Graph graph = new SingleGraph("LinkedGraph");
                Scanner scan = null;
                try {
                    scan = new Scanner(f);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(GraphDisplay.class.getName()).log(Level.SEVERE, null, ex);
                }

		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.stylesheet",
				"node {"
					+ "size: 3px;"
					+ "fill-color: #777;"
					+ "z-index: 0;"
					+ "text-background-mode: rounded-box;"
					+ "text-padding: 5;"
					+ "text-background-color: white;"
				+ "}"
				+ "edge {"
					+ "shape: line;"
					+ "fill-color: #222;"
					+ "z-index: -1;"
					+ "arrow-size: 5px, 2px;"
					+ "size-mode:fit;"
				+ "}"
		);
                
                int originalSize = scan.nextInt(); //read the first int int the file - number of original nodes
                
                while(scan.hasNextInt()) {
                    int node1 = scan.nextInt();
                    int node2 = scan.nextInt();
                    double weight = scan.nextDouble();
                    try {
                        graph.addNode("" + node1);
                        org.graphstream.graph.Node node = graph.getNode("" + node1);
                        node.addAttribute("ui.label", "" + node1);
                    } catch (Exception e) {}
                    try {
                        graph.addNode("" + node2);
                        org.graphstream.graph.Node node = graph.getNode("" + node2);
                        node.addAttribute("ui.label", "" + node2);
                    } catch (Exception e) {}
                    try {
                        graph.addEdge("n" + node1 + "n" + node2, "" + node1, "" + node2);
                        org.graphstream.graph.Edge edge = graph.getEdge("n" + node1 + "n" + node2);
                        edge.addAttribute("ui.label", weight);
                    } catch (Exception e) {}
                }

		FileSinkImages pic = new FileSinkImages(OutputType.PNG, Resolutions.VGA);
		pic.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
		try {
			pic.writeAll(graph, "data/out/"+f.getName()+".png");
		} catch (Exception ex) {
			System.out.println("Error writing to image: " + ex.getMessage());
		}
//		graph.display();
	}
}
