package sk.upjs.ics.novotnyr.akka;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SentenceCountActor extends UntypedActor {
	private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
	
    @Override
	public void onReceive(Object message) throws Exception {
    	// ak dostane vetu od mastera (manazera), spocita frekvencie
    	if (message instanceof String) {
    		Map<String, Integer> freq = calculateFrequencies((String) message);
    		
    		// posle masterovi, co vypocital 
    		getSender().tell(freq, getSelf());
    		logger.info((String) message);
		} else {
			unhandled(message);
		}
	}

    public Map<String, Integer> calculateFrequencies(String sentence) {
        Map<String, Integer> freqs = new HashMap<String, Integer>();

        Scanner scanner = new Scanner(sentence);
        while(scanner.hasNext()) {
            String word = scanner.next();

            int frequency = 1;
            if(freqs.containsKey(word)) {
                frequency += freqs.get(word);
            }
            freqs.put(word, frequency);
        }
        return freqs;
    }
}