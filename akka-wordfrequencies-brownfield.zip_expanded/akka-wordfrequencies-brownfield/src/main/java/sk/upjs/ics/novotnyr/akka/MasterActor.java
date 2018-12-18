package sk.upjs.ics.novotnyr.akka;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.Broadcast;
import akka.routing.RoundRobinPool;
import scala.Option;

public class MasterActor extends UntypedActor {
	
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    private Map<String, Integer> allFreq = new HashMap<>(); // tu by sme v konkurentnej casti potrebovali synchronizovat
    
    // referencia na aktora CountActor - v nom vlastne vytvorime 3 ratacov, kt. spaja router
    private ActorRef counter = getContext().actorOf(Props.create(SentenceCountActor.class)
    		.withRouter(new RoundRobinPool(3)));   
    
    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
    	super.preRestart(reason, message);
    	// master sleduje countera, ak vidi, ze zomrel (Terminated), konci program
    	// bez toho by neziskal info Terminated
    	getContext().watch(counter);
    }

	@Override
	public void onReceive(Object message) throws Exception {
		// prijali sme text, v ktorom mame pocitat vyskyty
		if (message instanceof String) {
			// posleme ulohu counterovi
			counter.tell(message, getSelf()); // getSelf v Akke je ako this
			
		} else if (message instanceof Map) {
			// mapu, ktoru vypocital counter, pridamu do allFreq
			Map<String, Integer> freqFromCounter = (Map<String, Integer>) message;
			allFreq = MapUtils.aggregate(allFreq, freqFromCounter);
			logger.info(allFreq.toString()); // vypiseme, co zatial mame
			
		} else if (message instanceof Eof) {
			// ked prisla sprava oznacujuca koniec posielania, posielame PoisonPill na ukoncenie programu
			// broadcast - counter najprv otravi deti az potom seba
			counter.tell(new Broadcast(PoisonPill.getInstance()), getSelf()); 
			
		} else if (message instanceof Terminated) {
			// Terminated pride, ked counter zomrel kvoli PoisonPill, kt. sme poslali, zavrieme system
			logger.info(allFreq.toString());
			getContext().system().terminate();
		} else {
			unhandled(message);
		}
	}

}