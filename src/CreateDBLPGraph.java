import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Creates the DBLP graph author1 \t author 2 \t time
 * 
 * @author ksemer
 */
public class CreateDBLPGraph {
	private final String PATH_DBLP_INPUT = "dblp.txt";
	private final String PATH_DBLP_AUTHORS_MAP = "dblp_authors_ids";
	private final String PATH_DBLP_GRAPH = "dblp_graph";
	private final String PATH_DBLP_AUTHORS_CONFERENCES = "dblp_authors_conf";

	// Create graph from specific conferences
	private final Set<String> CONFERENCES = new HashSet<String>(Arrays.asList("ICDE", "VLDB", "EDBT",
			"SIGMOD Conference", "KDD", "KDD Cup", "WWW", "SIGIR", "CIKM", "SDM", "ICDM", "WWW (Companion Volume)"));

	// is used to replace authors names with a unique id.
	private HashMap<String, Integer> allAuthors = new HashMap<String, Integer>();
	private Set<Integer> authors_f = new HashSet<>();
	private FileWriter w_graph = new FileWriter(PATH_DBLP_GRAPH, false);
	private FileWriter w_authors = new FileWriter(PATH_DBLP_AUTHORS_MAP, false);
	private FileWriter w_authors_conf = new FileWriter(PATH_DBLP_AUTHORS_CONFERENCES, false);

	public CreateDBLPGraph() throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(PATH_DBLP_INPUT));
		int year = 0, id = 0;
		String line = null, booktitle = null, title = null;
		List<String> authors = new ArrayList<String>();

		while ((line = input.readLine()) != null) {
			if (line.contains("Author: ")) {

				if (booktitle != null) {
					writeF(booktitle, title, year, authors);
					authors.clear();
					title = null;
					booktitle = null;
				}

				String author = line.replace("Author: ", "");
				authors.add(author);

				// map author to a unique id
				if (!allAuthors.containsKey(author)) {
					allAuthors.put(author, id);
					id++;
				}
			} else if (line.contains("Title: ")) {
				title = line.replace("Title: ", "");
				title.replaceAll("[^A-Za-z0-9]", "");
			} else if (line.contains("Year: ") && !line.contains("Title:")) {
				year = Integer.parseInt(line.replace("Year: ", ""));
			} else if (line.contains("Booktitle: ")) {
				booktitle = line.replace("Booktitle: ", "");
			}
		}
		input.close();

		// for the last publication
		writeF(booktitle, title, year, authors);

		// write only authors that published a paper in CONFERENCES
		for (Entry<String, Integer> entry : allAuthors.entrySet()) {
			int authorID = entry.getValue();

			if (authors_f.contains(authorID))
				w_authors.write(authorID + "\t" + entry.getKey() + "\n");
		}

		w_graph.close();
		w_authors.close();
		w_authors_conf.close();

		System.out.println("DBLP graph is generated!");
	}

	/**
	 * Write dblp graph and authors conferences to files
	 * 
	 * @param booktitle
	 * @param title
	 * @param year
	 * @param authors
	 * @throws IOException
	 */
	private void writeF(String booktitle, String title, int year, List<String> authors) throws IOException {
		// Used to generate graph from specific conferences
		if (title != null && (CONFERENCES.isEmpty() || CONFERENCES.contains(booktitle))) {

			// Write the authors
			for (int i = 0; i < authors.size(); i++) {
				int authorA = allAuthors.get(authors.get(i));

				authors_f.add(authorA);
				w_authors_conf.write(authorA + "\t" + booktitle + "\n");

				for (int j = i + 1; j < authors.size(); j++) {
					int authorB = allAuthors.get(authors.get(j));
					authors_f.add(authorB);

					// write graph edge
					w_graph.write(authorA + "\t" + authorB + "\t" + year + "\n");
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new CreateDBLPGraph();
	}
}
