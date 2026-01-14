import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
    public void train(String fileName) {
        String window = "";
        In in = new In(fileName);
        
       
        while (window.length() < windowLength && !in.isEmpty()) {
            char c = in.readChar();
            if (c != '\r') {
                window += c;
            }
        }

       
        while (!in.isEmpty()) {
            char c = in.readChar();
            if (c == '\r') {
                continue; 
            }

            List probs = CharDataMap.get(window);
            if (probs == null) {
                probs = new List();
                CharDataMap.put(window, probs);
            }

            probs.update(c);
            window = window + c;
            window = window.substring(1);
        }

        for (List probs : CharDataMap.values())
            calculateProbabilities(probs);
    }

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
		ListIterator itr = probs.listIterator(0);
        int total = 0;

        while (itr.hasNext()) {
            CharData curr = itr.next();
            total = total + curr.count;
        }

        itr = probs.listIterator(0);
        double temp = 0.0;

        
        while (itr.hasNext()) {
            CharData curr = itr.next();
            curr.p = (double) curr.count / total;
            temp = temp + curr.p;            
            if (!itr.hasNext()) {
                curr.cp = 1.0;
            }
            else {
                curr.cp = temp;
            }
        }
	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
        ListIterator itr = probs.listIterator(0);

        double rnd = randomGenerator.nextDouble();

        while (itr.hasNext()) {
            CharData curr = itr.next();
            if (curr.cp > rnd) {
                return curr.chr;
            }
        }

		return probs.get(probs.getSize() - 1).chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength) {
            return initialText;
        }

        String generatedText = initialText;
        String currentWindow = generatedText.substring(generatedText.length() - windowLength);

        while (generatedText.length() < textLength + 6) {
            List tempList = CharDataMap.get(currentWindow);

            if (tempList == null) {
                break; 
            }
            char nextChar = getRandomChar(tempList);
            generatedText = generatedText + nextChar;
            currentWindow = generatedText.substring(generatedText.length() - windowLength);  
        }

        return generatedText;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        LanguageModel model = new LanguageModel(2);

        model.train("galileo.txt");
        model.train("shakespeareinlove.txt");
        model.train("originofspecies.txt");

        String res = model.generate("hi", 100);
        System.out.println(res);



    }
}
