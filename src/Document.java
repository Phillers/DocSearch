import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.dictionary.Dictionary;

import java.util.TreeMap;

class Document{
    String title;
    String content;
    String processedContent;
    private TreeMap<String, Double> words = new TreeMap<>();
    private Double d;
    Double s;
    private static Dictionary dict;

    static {
        try {
            dict = Dictionary.getDefaultResourceInstance();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
    }

    void calculateTF(TreeMap<String, Double> keywords, int change){
        String[] wordsArr = processedContent.split(" +");
        Double count;
        Double sum = .0;
        for (String word : wordsArr){
            if((count = words.get(word)) != null) {
                words.put(word, count + 1);
                sum += 1;
            }
            else if((count = keywords.get(word)) != null){
                keywords.put(word, count + change);
                words.put(word, 1.0);
                sum += 1;
            }
        }
        for ( String key : words.keySet()){
            words.put(key, words.get(key)/sum);
        }
    }

    void calculateTFIDF(TreeMap<String, Double> keywords){
        double sum = .0;
        for(String key : words.keySet()){
            Double val = words.get(key) * keywords.get(key);
            words.put(key, val);
            sum += (val * val);
        }
        d = Math.sqrt(sum);
    }

    void processContent(String str) {
        content = str;
        str = str.toLowerCase();
        str = str.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit} ]", "");
        String[] words = str.split(" ");
//        if(title == null || title.length()<=1){
//            for(String word : words){
//                try {
//                    //System.out.println(PointerUtils.getDirectHypernyms(dict.lookupAllIndexWords(word).getIndexWordArray()[0].getSenses().get(0)).getWord());
//                } catch (JWNLException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        for(int i = 0 ; i < words.length; i++){
            Stemmer s = new Stemmer();
            s.add(words[i].toCharArray(), words[i].length());
            s.stem();
            words[i] = s.toString();
        }
        str = String.join(" ", words);
        processedContent = str;
    }



    void calculateSimilarity(Document query) {
        double sum = .0;
        for ( String key : query.words.keySet()) {
            Double a;
            if ((a =  words.get(key))!= null )
                sum += query.words.get(key) *a;
        }
        if(d == 0 || query.d == 0)
            s = .0;
        else
            s = sum/(d*query.d);
    }
}
