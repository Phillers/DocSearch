
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

class GUI implements Runnable{
    private DocSearch home;
    private List<Document> documents;
    private JTable table = new JTable();
    private JTextArea content = new JTextArea();
    private JFrame frame = new JFrame("Document Search");
    private JLabel question = new JLabel("Do You want to search for extended phrase? ");
    private JLabel extended = new JLabel("");
    private JButton search2 = new JButton("Search extended");

    @Override
    public void run() {
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        JLabel lbl1 = new JLabel("Keywords file:");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.weighty = 1;
        frame.add(lbl1, c);
        JTextField jtx1 = new JTextField("keywords.txt");
        c.gridx = 1;
        c.weightx = 2;
        frame.add(jtx1, c);
        JLabel lbl2 = new JLabel("Documents file:");
        c.gridx = 2;
        c.weightx = 0.5;
        frame.add(lbl2, c);
        JTextField jtx2 = new JTextField("documents.txt");
        c.gridx = 3;
        c.weightx = 2;
        frame.add(jtx2, c);
        JButton btn1 = new JButton("Open");
        btn1.addActionListener(e -> getDatabase(jtx1.getText(), jtx2.getText()));
        c.gridx = 4;
        c.weightx = 0.5;
        frame.add(btn1, c);
        JLabel lbl3 = new JLabel("Query text:");
        c.gridx = 5;
        c.weightx = 0.5;
        frame.add(lbl3, c);
        JTextField jtx3 = new JTextField("", 50);
        c.gridx = 6;
        c.weightx = 3;
        frame.add(jtx3, c);
        JButton btn2 = new JButton("Search");
        c.gridx = 7;
        c.weightx = 0.5;
        btn2.addActionListener(e -> search(jtx3.getText()));
        frame.add(btn2, c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        question.setVisible(false);
        frame.add(question, c);
        c.gridx = 2;
        frame.add(extended, c);
        c.gridx = 4;
        c.gridwidth = 1;
        search2.addActionListener(e -> search(extended.getText()));
        search2.setVisible(false);
        frame.add(search2, c);
        JScrollPane sp1 = new JScrollPane();
        c.weightx = 0;
        c.weighty = 5;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 5;
        c.fill = GridBagConstraints.BOTH;
        frame.add(sp1, c);
        table.setDefaultEditor(Object.class, null);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) {
                showDocument(((ListSelectionModel)e.getSource()).getMinSelectionIndex());
            }
        });
        sp1.getViewport().add(table);
        JScrollPane sp2 = new JScrollPane();
        c.weighty = 5;
        c.weightx = 0;
        c.gridx = 5;
        c.gridy = 2;
        c.gridwidth = 3;

        sp2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        frame.add(sp2, c);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setEditable(false);
        sp2.getViewport().add(content);
        frame.setVisible(true);
    }

    private void search(String expression) {
        documents = new ArrayList<>(home.search(expression));
        question.setVisible(true);
        extended.setText(expression + home.extension);
        System.out.println(extended.getText());
        search2.setVisible(true);
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Document title");
        model.addColumn("Similarity");
        for ( Document doc : documents) {
            model.addRow(new Object[]{doc.title, doc.s});
        }
        table.setModel(model);
        model.fireTableDataChanged();
    }

    private void showDocument(int index) {
        try {
            content.setText(documents.get(index).content + "\n\n\n" + documents.get(index).processedContent);
        } catch(ArrayIndexOutOfBoundsException e){
            content.setText("");
        }
    }

    private void getDatabase(String keywords, String documentFile) {
        try {
            documents = new ArrayList<>(home.prepareDb(keywords, documentFile));
            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("Document title");
            for (Document doc : documents) {
                model.addRow(new Object[]{doc.title});
            }
            table.setModel(model);
        } catch (FileNotFoundException e){
            JOptionPane.showMessageDialog(frame, e.getMessage());
        }
    }


    GUI(DocSearch home){
        this.home = home;
    }
}
class DocSearch {

    private TreeMap<String, Double> keywords = new TreeMap<>();
    private Vector<Document> documents = new Vector<>();
    String extension;

    public static void main(String[] args) {
        DocSearch ds = new DocSearch();
        GUI gui = new GUI(ds);
        SwingUtilities.invokeLater(gui);
        //ds.prepareDb("keywords.txt", "documents.txt");
        //Vector<Document> res = ds.search("machine learning");
        //for (Document doc : res){
        //    System.out.println(doc.s +" "+ doc.title);
        //}
    }

    Vector<Document> search(String expression) {
        Vector<Document> res = new Vector<>();
        Document query = new Document();
        query.processContent(expression);
        extension = query.title;
        query.calculateTF(keywords, 0);
        query.calculateTFIDF(keywords);
        for (Document doc : documents){
            doc.calculateSimilarity(query);
            addToResultSet(res, doc);
        }
        return res;
    }

    private void addToResultSet(Vector<Document> set, Document doc) {
        if(doc.s > 0 && set.size()>0 ){
            for(int i=0; i<set.size(); i++){
                if(doc.s > set.elementAt(i).s){
                    set.insertElementAt(doc, i);
                    return;
                }
            }
        }
        set.add(doc);
    }


    Vector<Document> prepareDb(String keyFile, String docFile) throws FileNotFoundException {
        readFiles(keyFile, docFile);
        calculate();
        return documents;
    }

    private void calculate() {
        for (Document doc : documents){
            doc.calculateTF(keywords,1);
        }
        int count = documents.size();
        keywords.replaceAll((k, v) -> Math.log10(count/v));
        for (Document doc : documents){
            doc.calculateTFIDF(keywords);
        }
    }

    private void readFiles(String keyFile, String docFile) throws FileNotFoundException {
        keywords.clear();
        try {
            BufferedReader br = new BufferedReader(new FileReader(keyFile));
            while (br.ready()) {
                String doc = br.readLine().trim();
                Stemmer s = new Stemmer();
                s.add(doc.toCharArray(), doc.length());
                s.stem();
                doc = s.toString();
                keywords.put(doc, .0);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No keyword database available.");
            e.printStackTrace();
            throw(e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        documents.clear();
        try {
            BufferedReader br = new BufferedReader(new FileReader(docFile));
            while (br.ready()) {
                StringBuilder str = new StringBuilder(br.readLine().trim());
                Document doc = new Document();
                doc.title = str.toString();
                str.append('\n');
                String str2;
                while(br.ready() &&(str2 = br.readLine().trim()).length() >0){
                    str.append(' ').append(str2);
                }
                doc.processContent(str.toString());
                documents.add(doc);
            }
        } catch (FileNotFoundException e) {
            System.out.println("No document database available.");
            e.printStackTrace();
            throw(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

